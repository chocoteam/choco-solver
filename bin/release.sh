#!/bin/bash

source ./commons.sh

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]" | grep -v "\[WARNING\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

set -ex

VERSION=$(getVersionToRelease)
git checkout master || quit "unable to check master out"
git pull --rebase origin master || quit "unable to pull master"

#Establish the version, maven side, misc. side
./bin/set_version.sh ${VERSION}
mvn license:format -q || quit "unable to update license"
git commit -m "initiate release ${VERSION}" -a || quit "unable to commit last changes"
git push origin master || quit "unable  to push on master"

# add new tag
#Quit if tag already exists
git ls-remote --exit-code --tags origin ${VERSION} && quit "tag ${VERSION} already exists"
# We assume the tests have been run before, and everything is OK for the release

# add the tag
git tag -a ${VERSION} -m "create tag ${VERSION}" || quit "Unable to tag with ${VERSION}"
git push --tags || quit "Unable to push the tag ${VERSION}"

#Set the next development version
echo "** Prepare master for the next version **"
./bin/set_version.sh --next ${VERSION}
git commit -m "Prepare the code for the next version" -a ||quit "Unable to commit to master"

#Push changes on develop, with the tag
git push origin master ||quit "Unable to push to master"

# Package the current version
./bin/package.sh ${VERSION}
