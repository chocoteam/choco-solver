#!/bin/bash
dir="$(dirname "$0")"
# shellcheck disable=SC1090
source "${dir}"/commons.sh

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]" | grep -v "\[WARNING\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

set -ex

VERSION=$(getVersionToRelease)
git checkout -b release-${VERSION} develop || quit "unable to check release-${VERSION} out"

#Establish the version, maven side, misc. side
./scripts/set_version.sh ${VERSION}
mvn license:format -q || quit "unable to update license"
git commit -m "initiate release ${VERSION}" -a || quit "unable to commit last changes"

git checkout master || quit "unable to check master out"
git merge --no-ff release-${VERSION} || quit "unable to merge release-${VERSION} into master"
git push origin master || quit "Unable to push the tag ${VERSION}"
# add new tag
# #Quit if tag already exists
# git ls-remote --exit-code --tags origin v${VERSION} && quit "tag ${VERSION} already exists"
# We assume the tests have been run before, and everything is OK for the release
# add the tag
git tag -a v${VERSION} -m "create tag ${VERSION}" || quit "Unable to tag with ${VERSION}"
git push --tags || quit "Unable to push the tag ${VERSION}"

# Proceed to the deployment
mvn -P ossrhDeploy  javadoc:jar source:jar deploy -DskipTests -B -U  ||quit "Unable to deploy to master"

## Merge back to develop
git checkout develop || quit "unable to check develop out"
git merge --no-ff release-${VERSION} || quit "unable to merge release-${VERSION} into develop"

#Set the next development version
echo "** Prepare master for the next version **"
./scripts/set_version.sh --next ${VERSION}
git commit -m "Prepare the code for the next version" -a ||quit "Unable to commit to master"

read -p "Do you set the milestone number in CHANGES.md?" -n 1 -r
echo    # (optional) move to a new line
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    quit "Milestone version must be set"
fi

#Push changes on develop, with the tag
git push origin develop ||quit "Unable to push to master"

# Delete the release branch
git branch -d release-${VERSION} || quit "Unable to delete release-${VERSION} branch"
