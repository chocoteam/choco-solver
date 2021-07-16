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
git checkout master || quit "unable to check master out"
git pull --rebase origin master || quit "unable to pull master"

#Establish the version, maven side, misc. side
./scripts/set_version.sh ${VERSION}
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
./scripts/set_version.sh --next ${VERSION}
git commit -m "Prepare the code for the next version" -a ||quit "Unable to commit to master"

#Push changes on develop, with the tag
git push origin master ||quit "Unable to push to master"

# Package the current version
GH_API="https://api.github.com/repos/chocoteam/choco-solver/"
GH_UPL="https://uploads.github.com/repos/chocoteam/choco-solver/"

AUTH="Authorization: token ${GH_TOKEN}"

# Validate token.
curl -o /dev/null -i -sH "${AUTH}" "${GH_API}releases" || quit "Error: Invalid repo, token or network issue!";

# prepare release comment

#find position of release separator in CHANGES.md, only keep the 2nd and 3rd
temp_file="tmpreadme.json"
$(touch ${temp_file}) || quit "Unable to create tmp file"

# extract release comment
extractReleaseComment ${VERSION} ${temp_file} || quit "Unable to extract release comment"

# create release
response=$(curl -i -sH "$AUTH" --data @${temp_file} "${GH_API}releases") || quit "Unable to create the release"

# get the asset id
ID=$(echo "$response" | grep -m 1 "id.:"| tr : = | tr -cd '[[:alnum:]]=') || quit "Error: Failed to get release id for tag: ${VERSION}"; echo "$response" | awk 'length($0)<100' >&2
ID=(${ID//=/ }) || quit "Error: Unable to split id: ${ID}"
id=${ID[1]} || quit "Error: Unable to get id: ${ID}"

# add asset
curl -i -sH "$AUTH" -H "Content-Type: application/zip" \
         -data-binary @choco-${VERSION}.zip \
         "${GH_UPL}/releases/${id}/assets?name=choco-${VERSION}.zip" \
         || quit "Unable to add asset"


# create the next milestone
NEXT=$(echo "${VERSION%.*}.$((${VERSION##*.}+1))") || quit "Unable to get next release number"
curl -i -sH "$AUTH" --data '{ "title": '\""${NEXT}"\"'}' "${GH_API}milestones"

if [ -d choco-${VERSION} ]; then
  rm -Rf choco-${VERSION} || quit "Unable to remove choco-${VERSION} dir"
fi
#rm choco-${VERSION}.zip
rm ${temp_file} || quit "Unable to remove tmp file"
