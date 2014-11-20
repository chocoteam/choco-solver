Release process
===============

Choco3 is an open-source Java library for Constraint Programming.

This document reports the release process.

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)

    $ mvn versions:display-dependency-updates
    $ mvn versions:display-plugin-updates

And update README.md with the correct versions of dependencies

    $ mvn dependency:list | grep :compile

3. Generate PDF documentation

    $ cd docs/
    $ make latexpdf

4. Check that ALL issues are reported in CHANGES.md files

5. Mount the /Volumes/choco-repo samba point (required to upload files for maven)

6. The release process relies on Travis and is executed automatically once a 'release' branch is pushed. So simply call:

    ./src/scripts/release.sh


7. Then, everything is achieved by Travis.
Or, one can run the command by itself:

    ./src/scripts/build.sh

However, deployment and zip file (jar files, user guide, logback configuration file and javadoc) need to be done locally:

    ./src/scripts/package.sh X.Y.Z

7. Publish choco-repo/ intranet to internet

8. Upload the zip file onto the website

===================
The Choco3 dev team.


