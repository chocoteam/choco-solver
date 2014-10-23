#!/bin/sh

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function guess() {
    v=$1
    echo "${v%.*}.$((${v##*.}+1))-SNAPSHOT"
}


VERSION=$(getVersionToRelease)
NEXT=$(guess $VERSION)
TAG="choco-parsers-${VERSION}"

git fetch
git checkout -b release

mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false
git commit -m "initiate release ${VERSION}" -a

COMMIT=$(git rev-parse HEAD)

git ls-remote --exit-code --tags origin ${TAG}

## MASTER
git checkout master
git merge --no-ff ${COMMIT}
git tag ${TAG}
git push --tags
git pull origin master
git push origin master

## DEVELOP
git checkout develop
git merge --no-ff ${TAG}
mvn versions:set -DnewVersion=${NEXT} -DgenerateBackupPoms=false
git commit -m "Prepare the code for the next version" -a
git push origin develop
git push --all && git push --tags
git push origin --delete release

git checkout $TAG
mvn clean install -DskipTests
git checkout develop