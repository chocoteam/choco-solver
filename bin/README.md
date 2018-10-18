Release process
===============

Choco-solver is an open-source Java library for Constraint Programming.

This document reports the release process.

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)


    $ mvn -U versions:display-dependency-updates

    $ mvn -U versions:display-plugin-updates

And update README.md with the correct versions of dependencies

    $ mvn dependency:list | grep :compile | cut -c11- | cut -d : -f1-2,4

And run license check:

    $ mvn license:format

3. Check that ALL issues are reported in **CHANGES.md** files


    $ ltag=`git describe --abbrev=0 --tags`;git log ${ltag}..master | grep "#[0-9]"

4. Now you can run the command: 


    $ ./bin/release.sh

However, deployment and zip file (jar files, user guide and javadoc) need to be done locally:

    ./bin/package.sh X.Y.Z

5. Upload the zip file onto the website

===================
The Choco-solver dev team.


