#!/bin/bash
echo "Start release with zip option on"
VERSION=$1
mkdir choco-${VERSION}
git checkout choco-${VERSION}
mvn clean install -DskipTests || exit 1

mv ./choco-solver/target/choco-solver-${VERSION}.jar ./choco-${VERSION}
mv ./choco-solver/target/choco-solver-${VERSION}-with-dependencies.jar ./choco-${VERSION}
mv ./choco-solver/target/choco-solver-${VERSION}-sources.jar ./choco-${VERSION}
mv ./choco-samples/target/choco-samples-${VERSION}-sources.jar ./choco-${VERSION}

cp ./user_guide.pdf ./choco-${VERSION}/user_guide-${VERSION}.pdf
cp ./README.md ./choco-${VERSION}
cp ./CHANGES.md ./choco-${VERSION}
cp ./LICENSE ./choco-${VERSION}

mvn javadoc:aggregate  || exit 1
cd target/site/
zip -r apidocs-${VERSION}.zip ./apidocs/*
mv apidocs-${VERSION}.zip ../../choco-${VERSION}
cd ../../

zip choco-${VERSION}.zip ./choco-${VERSION}/*

git checkout develop
rmdir choco-${VERSION}
