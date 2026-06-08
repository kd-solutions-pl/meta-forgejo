# meta-forgejo

Yocto layer for integrating [Forgejo](https://forgejo.org/). `meta-forgejo` will integrate Forgejo into a read-only squashfs root
filesystem and use a writable `/data` partition for persistent runtime data.

## Database configuration

`meta-forgejo` is set up for a local PostgreSQL database by default. The default Forgejo configuration is located in `/data/forgejo/config/app.ini`and contains:

```ini
[database]
DB_TYPE = postgres
HOST = /tmp
NAME = forgejo
USER = forgejo
PASSWD_URI = file:/data/forgejo/db_passwd
SSL_MODE = disable
```

Set fixed password for forgejo DB by calling
```
sudo pg-forgejo-db-setup <PASSWORD>
```

## TLS certificate

Forgejo is configured to serve HTTPS by default. Install the certificate and private key before starting Forgejo:

```text
/data/forgejo/ssl/forgejo.crt
/data/forgejo/ssl/forgejo.key
```

`forgejo.service` checks that both files exist, and `forgejo-runtime-check` verifies that they are readable. If either
file is missing, Forgejo will not start.

## First-start installer limitation

When Forgejo shows the initial web setup page, it may write a `PASSWD = ...` entry into the `[database]` section of
`/data/forgejo/config/app.ini`. In this layer that entry is not valid because the database password is managed through
`PASSWD_URI`.

After completing the initial setup page, manually remove the generated `PASSWD = ...` line from `[database]` and keep:

```ini
PASSWD_URI = file:/data/forgejo/db_passwd
```

Forgejo's database section cannot contain both `PASSWD` and `PASSWD_URI`.
For `meta-forgejo`, only `PASSWD_URI` should be present.
