Release process
===============

Choco-solver is an open-source Java library for Constraint Programming.

This document reports the release process.

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)

    $ mvn versions:display-dependency-updates

    $ mvn versions:display-plugin-updates

And update README.md with the correct versions of dependencies

    $ mvn dependency:list | grep :compile | cut -c11- | cut -d : -f1-2,4

And run license check:

    $ mvn license:format -Dyear=2016

3. Generate PDF documentation

    $ cd src/sphinx/

    $ make latexpdf

4. Check that ALL issues are reported in CHANGES.md files

    $ ltag=`git describe --abbrev=0 --tags`;git log ${ltag}..develop | grep "#[0-9]"

5. Make sure the website is mounted to /Volume/htdocs/ and run the command :

    $ ./bin/release.sh

    $ ./bin/deploy.sh

    However, deployment and zip file (jar files, user guide and javadoc) need to be done locally:

    ./bin/package.sh X.Y.Z

6. Upload the zip file onto the website

===================
The Choco-solver dev team.


