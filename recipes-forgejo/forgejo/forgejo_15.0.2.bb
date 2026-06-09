SUMMARY = "Forgejo self-hosted lightweight software forge"
DESCRIPTION = "Forgejo is a self-hosted lightweight software forge."
HOMEPAGE = "https://forgejo.org/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://src/${GO_IMPORT}/LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

SRC_URI = " \
    git://codeberg.org/forgejo/forgejo.git;protocol=https;nobranch=1;destsuffix=${BP}/src/${GO_IMPORT} \
    file://app.ini \
    file://forgejo.service \
    file://forgejo-storage-prepare.service \
    file://forgejo-storage-prepare \
    file://forgejo-runtime-check \
    file://forgejo-postgresql-setup.service \
    file://forgejo-postgresql-setup \
    file://0001-Fix-passing-GOFLAGS-from-Yocto-environment.patch \
    file://npm-shrinkwrap.json \
"
SRCREV = "4e40eede0352619b8ddb3070ed3005c1eb88bfcb"

GO_IMPORT = "forgejo.org"

require forgejo-go-mods.inc

DEPENDS += "go-native nodejs-native"
RDEPENDS:${PN} += "bash git openssh coreutils postgresql git-lfs"

inherit go-mod pkgconfig systemd useradd

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r --user-group -u 2001 -d /data/forgejo --no-create-home --shell /bin/sh forgejo"
SYSTEMD_SERVICE:${PN} = "forgejo-storage-prepare.service forgejo-postgresql-setup.service forgejo.service"

export GO111MODULE = "on"

do_configure:append() {
    install -m 0644 ${UNPACKDIR}/npm-shrinkwrap.json ${B}/src/${GO_IMPORT}/npm-shrinkwrap.json
}

do_compile[network] = "1"

do_compile() {
    export GOFLAGS="-v -modcacherw"
    export EXTRA_GOFLAGS="${GO_PARALLEL_BUILD} -trimpath -buildmode=pie"
    export LDFLAGS=""
    export GOTOOLCHAIN="local"
    oe_runmake TAGS="bindata timetzdata" build
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/src/${GO_IMPORT}/gitea ${D}${bindir}/forgejo

    install -d ${D}${datadir}/forgejo/defaults
    install -m 0644 ${UNPACKDIR}/app.ini ${D}${datadir}/forgejo/defaults/app.ini

    install -m 0755 ${UNPACKDIR}/forgejo-storage-prepare ${D}${bindir}/forgejo-storage-prepare
    install -m 0755 ${UNPACKDIR}/forgejo-runtime-check ${D}${bindir}/forgejo-runtime-check
    install -m 0755 ${UNPACKDIR}/forgejo-postgresql-setup ${D}${bindir}/forgejo-postgresql-setup

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/forgejo.service ${D}${systemd_system_unitdir}/forgejo.service
    install -m 0644 ${UNPACKDIR}/forgejo-storage-prepare.service ${D}${systemd_system_unitdir}/forgejo-storage-prepare.service
    install -m 0644 ${UNPACKDIR}/forgejo-postgresql-setup.service ${D}${systemd_system_unitdir}/forgejo-postgresql-setup.service
}

FILES:${PN} += " \
    ${datadir}/forgejo/defaults/app.ini \
    ${bindir}/forgejo-storage-prepare \
    ${bindir}/forgejo-runtime-check \
    ${bindir}/forgejo-postgresql-setup \
    ${systemd_system_unitdir}/forgejo.service \
    ${systemd_system_unitdir}/forgejo-storage-prepare.service \
    ${systemd_system_unitdir}/forgejo-postgresql-setup.service \
"
