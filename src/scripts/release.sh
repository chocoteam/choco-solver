#!/bin/bash

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

VERSION=$(getVersionToRelease)
git branch -d release 2> /dev/null
git checkout -b release || exit 1

#Establish the version, maven side, misc. side
./src/scripts/set_version.sh ${VERSION}
git commit -m "initiate release ${VERSION}" -a
git push origin release || exit 1
git checkout -