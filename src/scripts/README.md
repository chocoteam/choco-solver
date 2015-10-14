Release process
===============

Choco3 is an open-source Java library for Constraint Programming.

This document reports the release process.

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)

    $ mvn versions:display-dependency-updates

    $ mvn versions:display-plugin-updates

And update README.md with the correct versions of dependencies

    $ cd choco-solver/ && mvn dependency:list | grep :compile | cut -c11- | cut -d : -f1-2,4

And run license check:

    $ mvn license:format -Dyear=2015

3. Generate PDF documentation

    $ cd docs/

    $ make latexpdf

4. Check that ALL issues are reported in CHANGES.md files

    $ git log master..develop | grep "#[0-9]"

5. Run the command :

    $ ./src/scripts/release.sh

    $ ./src/scripts/build.sh

    However, deployment and zip file (jar files, user guide, logback configuration file and javadoc) need to be done locally:

    ./src/scripts/package.sh X.Y.Z

6. Upload the zip file onto the website

===================
The Choco3 dev team.


