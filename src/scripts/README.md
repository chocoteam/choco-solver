Release process
===============

The release process relies on Travis and is executed automatically once a 'release' branch is pushed.
So simply call:

    ./src/scripts/release.sh

Then, everything is achieved by Travis.
However, deployment and zip file (jar files, user guide, logback configuration file and javadoc) need to be done locally:

    ./src/scripts/package.sh X.Y.Z
