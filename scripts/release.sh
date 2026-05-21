#!/bin/bash
dir="$(dirname "$0")"
# shellcheck disable=SC1090
source "${dir}"/commons.sh

DRY_RUN=false
if [ "$1" == "--dry-run" ]; then
    DRY_RUN=true
    echo "=== DRY-RUN MODE — no push, no deploy ==="
fi

function getVersionToRelease() {
    CURRENT_VERSION=$(mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate -Dexpression=project.version -q -DforceStdout)
    echo "${CURRENT_VERSION%%-SNAPSHOT}"
}

set -ex

# --- Pre-flight checks ---
MVN_VERSION=$(mvn --version 2>&1 | head -1)
[[ "$MVN_VERSION" == *"Maven 3."* ]] || quit "Maven 3.x is required (found: $MVN_VERSION)"

BRANCH=$(git rev-parse --abbrev-ref HEAD)
[[ "$BRANCH" == "develop" ]] || quit "Must be run from develop branch (currently on $BRANCH)"
# [[ -z "$(git status --porcelain)" ]] || quit "Working directory is not clean"

VERSION=$(getVersionToRelease)
[[ -n "$VERSION" ]] || quit "Unable to determine release version"
echo "Releasing version: ${VERSION}"

# Generate changelog summary from commits since last tag
echo "** Generating changelog with Claude agent **"
./scripts/generate_changelog.sh

read -p "Have you updated CHANGES.md with the generated changelog? [y/N] " -n 1 -r
echo
[[ $REPLY =~ ^[Yy]$ ]] || quit "Please update CHANGES.md before releasing"

git checkout -b "release-${VERSION}" develop || quit "unable to check release-${VERSION} out"

#Establish the version, maven side, misc. side
./scripts/set_version.sh "${VERSION}"
mvn license:format -q || quit "unable to update license"
git commit -m "initiate release ${VERSION}" -a || quit "unable to commit last changes"

git checkout master || quit "unable to check master out"
git merge --no-ff "release-${VERSION}" || quit "unable to merge release-${VERSION} into master"

if [ "$DRY_RUN" = true ]; then
    echo "=== DRY-RUN: skipping deploy ==="
else
    # Deploy BEFORE pushing to remote — if deployment fails, nothing is published
    mvn -P centralDeploy javadoc:jar source:jar deploy -DskipTests central-publishing:publish \
        || quit "Deployment failed — master and tag have NOT been pushed"
fi

if [ "$DRY_RUN" = true ]; then
    echo "=== DRY-RUN: skipping push to master and tag ==="
else
    # Only push after successful deployment
    git push origin master || quit "Unable to push master"
    git tag -a "v${VERSION}" -m "create tag ${VERSION}" || quit "Unable to tag with ${VERSION}"
    git push --tags || quit "Unable to push the tag ${VERSION}"
fi

## Merge back to develop (from master, to keep histories in sync)
git checkout develop || quit "unable to check develop out"
git merge --no-ff master || quit "unable to merge master into develop"

#Set the next development version
echo "** Prepare develop for the next version **"
./scripts/set_version.sh --next "${VERSION}"
git commit -m "Prepare the code for the next version" -a || quit "Unable to commit to develop"

if [ "$DRY_RUN" = true ]; then
    echo "=== DRY-RUN: skipping push to develop ==="
else
    #Push changes on develop
    git push origin develop || quit "Unable to push to develop"
fi

# Delete the release branch
git branch -d "release-${VERSION}" || quit "Unable to delete release-${VERSION} branch"

if [ "$DRY_RUN" = true ]; then
    echo "=== DRY-RUN complete. Review local state, then reset with: ==="
    echo "  git checkout develop && git reset --hard origin/develop"
    echo "  git branch -D release-${VERSION}"
    echo "  git checkout master && git reset --hard origin/master"
fi
