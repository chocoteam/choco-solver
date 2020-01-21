#!/bin/sh

set -ex

function quit() {
    echo "ERROR: $*"
    exit 1
}

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]" | grep -v "\[WARNING\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function guess() {
    v=$1
    echo "${v%.*}.$((${v##*.}+1))-SNAPSHOT"
}

function sedInPlace() {
	if [ $(uname) = "Darwin" ]; then
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

VERSION=$(getVersionToRelease)
NEXT=$(guess $VERSION)
TAG="${VERSION}"

git fetch || quit "unable to fetch master"
git checkout -b release || quit "unable to check master out"

#mvn -q dependency:purge-local-repository || quit "unable to purge local repo"
mvn clean install -DskipTests  || quit "unable to install "

echo "New version is ${VERSION}"
YEAR=`LANG=en_US.utf8 date +"%Y"`
sedInPlace "s%Copyright.*.%Copyright (c) $YEAR, IMT Atlantique%"  LICENSE
sedInPlace "s%choco-parsers-.*-with-dependencies.jar%choco-parsers-${VERSION}-with-dependencies.jar%"  README.md
sedInPlace "s%choco-parsers-.*-with-dependencies.jar%choco-parsers-${VERSION}-with-dependencies.jar%"  MINIZINC.md
sedInPlace "s%choco-parsers-.*-with-dependencies.jar%choco-parsers-${VERSION}-with-dependencies.jar%"  XCSP3.md
sedInPlace "s%choco-parsers-.*-with-dependencies.jar%choco-parsers-${VERSION}-with-dependencies.jar%"  ./src/main/bash/fzn-exec.sh
sedInPlace "s%choco-parsers-.*-with-dependencies.jar%choco-parsers-${VERSION}-with-dependencies.jar%"  ./src/main/bash/xcsp3-exec
#Update the poms:wq
mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false || quit "unable to set new version"
mvn license:format || quit "unable to format the license"
#
git commit -m "initiate release ${VERSION}" -a || quit "unable to commit initial release"
#
echo "Start release"
#Extract the version
COMMIT=$(git rev-parse HEAD)

#Quit if tag already exists
git ls-remote --exit-code --tags origin ${TAG} && quit "tag ${TAG} already exists"

#Working version ?
# Well, we assume the tests have been run before, and everything is OK for the release
#mvn clean test ||exit 1

git fetch origin master:refs/remotes/origin/master||quit "Unable to fetch master"
#Integrate with master and tag
echo "** Integrate to master **"
git checkout master ||quit "No master branch"
git pull origin master || quit "Unable to pull master"
git merge --no-ff -m "Merge tag '${TAG}' into master" ${COMMIT} ||quit "Unable to integrate to master"

#NOT USED FOR THE MOMENT
##Javadoc
#./bin/push_javadoc apidocs.git ${VERSION}

git tag ${TAG} ||quit "Unable to tag with ${TAG}"
git push --tags ||quit "Unable to push the tag ${TAG}"
git push origin master ||quit "Unable to push master"

#    #Deploy the artifacts
#echo "** Deploying the artifacts **"
mvn -s ~/.m2/settings.xml -P release clean javadoc:jar source:jar deploy -DskipTests ||quit "Unable to deploy"

#Set the next development version
#echo "** Prepare develop for the next version **"
mvn versions:set -DnewVersion=${NEXT} -DgenerateBackupPoms=false
git commit -m "Prepare the code for ${VERSION}" -a ||quit "Unable to commit to master"
#
##Push changes on develop, with the tag
git push origin master ||quit "Unable to push to master"

#Clean
git branch --delete release ||quit "Unable to delete release"
