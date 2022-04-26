#!/bin/bash
dir="$(dirname "$0")"
# shellcheck disable=SC1090
source "${dir}"/commons.sh
#Script to notify the website about a release

function sedInPlace() {
	if [ $(uname) = "Darwin" ]; then
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

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
    sedInPlace "s%The name of the jar file terms the packaging: .*%The name of the jar file terms the packaging:- \`choco-solver-$VERSION-jar-with-dependencies.jar\` or - \`choco-solver-$VERSION.jar\`.%" README.md
    sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md
    sedInPlace "s%Choco-solver is distributed.*.%Choco-solver is distributed under BSD 4-Clause License \(Copyright \(c\) 1999-$YEAR, IMT Atlantique).%"  README.md

    ## The LICENSE
    sedInPlace "s%Copyright.*.%Copyright (c) $YEAR, IMT Atlantique%"  LICENSE

    ## The configuration file
    sedInPlace "s%.*Constraint Programming Solver, Copyright.*%        \"** Choco $VERSION \($DAT\) : Constraint Programming Solver, Copyright \(c\) 2010-$YEAR\";%"  ./solver/src/main/java/org/chocosolver/solver/trace/IOutputFactory.java

    ## For MiniZinc
    sedInPlace "s%    git fetch -q && git checkout -q master.*%    git fetch -q && git checkout -q $VERSION && \\ %"  ./parsers/src/main/minizinc/docker/Dockerfile_Choco.dms
    sedInPlace "s%  \"version\": .*%  \"version\": \"$VERSION\",%"  ./parsers/src/main/minizinc/choco.msc
    sedInPlace "s%CHOCO_JAR=~/.m2/.*%CHOCO_JAR=~/.m2/repository/org/choco-solver/choco-parsers/$VERSION/choco-parsers-$VERSION-jar-with-dependencies.jar%" ./parsers/src/main/minizinc/fzn-choco

    ## The CHANGES.md
    # replace the 'NEXT MILESTONE' version by VERSION
    REGEX="s%NEXT MILESTONE*%${VERSION} - ${d}%"
    sedInPlace "${REGEX}" CHANGES.md
  
else

    # shellcheck disable=SC2016
    sedInPlace '6 i\
    \
    NEXT MILESTONE\
    -------------------\
    \
    ### Major features:\
    \
    ### Deprecated API (to be removed in next release):\
    \
    ### Other closed issues and pull requests:\
    See [milestone '${VERSION%%-SNAPSHOT}'](https://github.com/chocoteam/choco-solver/milestone/xx)\
    \
    #### Contributors to this release:\
    \
    ' CHANGES.md
fi
