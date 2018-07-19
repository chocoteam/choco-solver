#!/usr/bin/env bash
# based on: https://dracoblue.net/dev/uploading-snapshots-and-releases-to-maven-central-with-travis/

function getVersionToRelease() {
    CURRENT_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION}
}


VERSION=$(getVersionToRelease)

if [[ ${VERSION} = *"SNAPSHOT"* ]]
then
    echo "not on a tag -> keep snapshot version in pom.xml"
else
    echo "on a tag -> set pom.xml <version> to ${VERSION}"
    mvn --settings .travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=${VERSION} 1>/dev/null 2>/dev/null
    SETTINGS=" javadoc:jar source:jar"
fi

mvn -s .travis/settings.xml -P release ${SETTINGS} deploy -DskipTests -B -U -q