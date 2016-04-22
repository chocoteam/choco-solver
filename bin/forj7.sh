#!/bin/bash

function quit() {
    echo "ERROR: $*"
    exit 1
}

function getVersionToRelease() {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function sedInPlace() {
	if [ $(uname) = "Darwin" ]; then
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

VERSION=$(getVersionToRelease)
echo $VERSION

sedInPlace  "s#.*<version>3.3.1</version>.*#    <version>3.3.1-j7</version>#" pom.xml
sedInPlace  "s#.*<source>1.8</source>.*#                    <source>1.7</source>#" pom.xml
sedInPlace  "s#.*<target>1.8</target>.*#                    <target>1.7</target>#" pom.xml
sedInPlace  "s#.*<additionalparam>-Xdoclint:none</additionalparam>.*#<!--<additionalparam>-Xdoclint:none</additionalparam>-->#" pom.xml

sedInPlace  "s#.*<version>3.3.1</version>.*#    <version>3.3.1-j7</version>#" choco-solver/pom.xml
sedInPlace  "s#.*<version>3.3.1</version>.*#    <version>3.3.1-j7</version>#" choco-samples/pom.xml
OLD_JVM=`echo $JAVA_HOME`
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home
mvn clean compile || quit "Does not compile"
# take 45 min to make it works
# java7:
# sudo ln -s /Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/bin/java /usr/bin/java7
TAG="choco-${VERSION}-j7"
git commit -m "${TAG}"

git tag ${TAG} ||quit "Unable to tag with ${TAG}"
git push --tags ||quit "Unable to push the tag ${TAG}"
#    #Deploy the artifacts
echo "** Deploying the artifacts **"
mvn -q -P release clean javadoc:jar source:jar deploy -DskipTests ||quit "Unable to deploy"

#Set the next development version
echo "** Prepare develop for the next version **"
git checkout develop ||quit "Unable to checkout develop"
git merge --no-ff ${TAG} ||quit "Unable to integrate to develop"
./bin/set_version.sh --next ${VERSION}
git commit -m "Prepare the code for the next version" -a ||quit "Unable to commit to develop"

#Push changes on develop, with the tag
git push origin develop ||quit "Unable to push to develop"


#Clean
git push origin --delete release ||quit "Unable to delete release"

export JAVA_HOME=${OLD_JVM}