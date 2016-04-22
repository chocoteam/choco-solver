#!/bin/bash

function quit() {
    echo "ERROR: $*"
    exit 1
}

function getVersion {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"
}

if test -z "$TRAVIS_BRANCH"; then
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
else
    BRANCH=${TRAVIS_BRANCH}
fi

if [ ${BRANCH} = "release" ]; then
    echo "Start release"
    #Extract the version
    VERSION=$(getVersion)
    TAG="choco-${VERSION}"
    COMMIT=$(git rev-parse HEAD)
    echo $VERSION

    #Quit if tag already exists
    git ls-remote --exit-code --tags origin ${TAG} && quit "tag ${TAG} already exists"

    #Working version ?
    # Well, we assume the tests have been run before, and everything is OK for the release
    #mvn clean test ||exit 1

    git fetch origin master:refs/remotes/origin/master||quit "Unable to fetch master"
    #Integrate with master and tag
    echo "** Integrate to master **"
    git checkout master ||quit "No master branch"
    git merge --no-ff ${COMMIT} ||quit "Unable to integrate to master"

#    NOT USED FOR THE MOMENT
#    #Javadoc
#    ./bin/push_javadoc apidocs.git ${VERSION}

    git tag ${TAG} ||quit "Unable to tag with ${TAG}"
    git push --tags ||quit "Unable to push the tag ${TAG}"
    git push origin master ||quit "Unable to push master"

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
else
    mvn clean install
fi