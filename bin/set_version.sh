#!/bin/bash
source ./commons.sh
#Script to notify the website about a release

set -ex

if [ $1 == "--next" ]; then
    VERSION=$(guess $2)
    NEXTMIL="no"
else
    VERSION=$1
    NEXTMIL="yes"
fi
echo "New version is ${VERSION}"
#Update the poms
mvn -q versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false

if test "${NEXTMIL}" = "yes"
then
    DAT=`LANG=en_US.utf8 date +"%Y-%m"`
    YEAR=`LANG=en_US.utf8 date +"%Y"`
    d=`LANG=en_US.utf8 date +"%d %b %Y"`

    ## The README.md
    # Update of the version number for maven usage

    sedInPlace "s%Current stable version is .*.%Current stable version is $VERSION ($d).%"  README.md
    sedInPlace "s%The name of the jar file terms the packaging: .*%The name of the jar file terms the packaging: \`choco\-solver\-$VERSION.jar\` or \`choco\-solver\-$VERSION\-no\-dep\.jar\`.%" README.md
    sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md
    sedInPlace "s%Choco-solver is distributed.*.%Choco-solver is distributed under BSD 4-Clause License \(Copyright \(c\) 1999-$YEAR, IMT Atlantique).%"  README.md
    sedInPlace "s%\[tarball\].*%[tarball](https://github.com/chocoteam/choco-solver/releases/download/$VERSION/choco-$VERSION.zip) which contains%" README.md

    ## The LICENSE
    sedInPlace "s%Copyright.*.%Copyright (c) $YEAR, IMT Atlantique%"  LICENSE

    ## The configuration file
    sedInPlace "s%.*Constraint Programming Solver, Copyright.*%        \"** Choco $VERSION \($DAT\) : Constraint Programming Solver, Copyright \(c\) 2010-$YEAR\";%"  ./src/main/java/org/chocosolver/solver/DefaultSettings.java
    sedInPlace "s%.*Constraint Programming Solver, Copyright.*%        welcome.message=** Choco $VERSION \($DAT\) : Constraint Programming Solver, Copyright \(c\) 2010-$YEAR;%"  ./src/main/resources/DefaultSettings.properties
    ## The doc
    sedInPlace "s%\*\* Choco .*%** Choco $VERSION \($DAT\) : Constraint Programming Solver, Copyright \(c\) 2010-$YEAR%"  ./src/sphinx/source/3_solving.rst

    ## The CHANGES.md
    # replace the 'NEXT MILESTONE' version by VERSION
    REGEX="s%NEXT MILESTONE*%${VERSION} - ${d}%"
    sedInPlace "${REGEX}" CHANGES.md
    # add a new empty line in CHANGES.md
    sedInPlace "s%copyright = .*%copyright = u'${YEAR}, Jean-Guillaume Fages, Xavier Lorca, Charles Prud\\\'homme'%" ./src/sphinx/source/conf.py
    sedInPlace "s%release = .*%release = '${VERSION}'%" ./src/sphinx/source/conf.py

    cd ./src/sphinx/
    make latexpdf
else

    sedInPlace '6 i\
    \
    NEXT MILESTONE\
    -------------------\
    \
    ### Major features:\
    \
    ### Deprecated API (to be removed in next release):\
    \
    ### Closed issues and pull requests:\
    \
    ' CHANGES.md
fi
