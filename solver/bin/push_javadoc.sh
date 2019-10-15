#!/bin/bash
set -ex

LOCAL=`mktemp -d -t choco.XXX`
REMOTE=$1
VERSION=$2
echo "${LOCAL}"
set -x
git -C ${LOCAL} init
git -C ${LOCAL} remote add origin git@github.com:chocoteam/${REMOTE} || exit 1
git -C ${LOCAL} pull origin gh-pages || exit 1
git -C ${LOCAL} checkout gh-pages || exit 1


#Generate and copy
mvn javadoc:aggregate
cp -r target/site/apidocs/* ${LOCAL}/

#Publish
git -C ${LOCAL} commit -m "apidoc for version ${VERSION}" -a || exit 1
git -C ${LOCAL} push || exit 1
rm -rf ${LOCAL}