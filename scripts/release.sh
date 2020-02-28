#!/usr/bin/env bash

# This script intends to release choco-solver

#####
#####
function sedInPlace() {
	# shellcheck disable=SC2046
	if [ $(uname) = "Darwin" ]; then
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}
function guess() {
    v=$1
    if [[ ${v} == *-SNAPSHOT ]]; then
        echo "${v%%-SNAPSHOT}"
    else
        echo "${v%.*}.$((${v##*.}+1))-SNAPSHOT"
    fi
}
#####
#####


set -ex

echo "Move to the master branch"
git checkout master

echo "Make sure it is up-to-date"
git pull --rebase origin master

echo "Get current version of the project"
VERSION=$(mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate \
                -Dexpression=project.version | grep -v "\[INFO\]" | grep -v "\[WARNING\]")

if [ $1 == "--next" ]; then
    VERSION=$(guess $2)
    NEXTMIL="no"
else
    VERSION=$1
    NEXTMIL="yes"
fi
echo "New version is ${VERSION}"

echo "Update the poms"
mvn -q versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false

echo "Update some files wrt to the release"
if test "${NEXTMIL}" = "yes"
then
    DAT=$(LANG=en_US.utf8 date +"%Y-%m")
    YEAR=$(LANG=en_US.utf8 date +"%Y")
    d=$(LANG=en_US.utf8 date +"%d %b %Y")

    echo "...LICENSE"
    sedInPlace "s%Copyright.*.%Copyright (c) $YEAR, IMT Atlantique%"  LICENSE

    echo "... Settings"
    sedInPlace "s%.*Constraint Programming Solver, Copyright.*%        \"** Choco $VERSION \($DAT\) : Constraint Programming Solver, Copyright \(c\) 2010-$YEAR\";%"  ./solver/src/main/java/org/chocosolver/solver/DefaultSettings.java
    sedInPlace "s%.*Constraint Programming Solver, Copyright.*%        welcome.message=** Choco $VERSION \($DAT\) : Constraint Programming Solver, Copyright \(c\) 2010-$YEAR;%"  ./solver/src/main/resources/DefaultSettings.properties

    echo "...CHANGES.md"
    # replace the 'NEXT MILESTONE' version by VERSION
    REGEX="s%NEXT MILESTONE*%${VERSION} - ${d}%"
    sedInPlace "${REGEX}" CHANGES.md
else
    echo "...CHANGES.md"
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

echo "Make sure headers are OK"
mvn license:format -q

echo "Then commit changes"
git commit -m "initiate release ${VERSION}" -a

echo "Push changes"
git push origin master



