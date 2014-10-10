#!/bin/bash
gpg --allow-secret-key-import --import etc/private.key
mvn -s etc/sonatype.xml clean javadoc:jar source:jar gpg:sign -Dgpg.keyname=BF1447AC -Dgpg.passphrase=${GPG_PASSPHRASE} deploy ||exit 1