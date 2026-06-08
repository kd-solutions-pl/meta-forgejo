SUMMARY = "QEMU Image running Forgejo on read-only rootfs"
LICENSE = "MIT"

require recipes-core/images/albertow-image.bb

IMAGE_INSTALL:append = " \
    forgejo \
"
