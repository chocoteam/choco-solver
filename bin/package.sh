#!/bin/bash
echo "Start release with zip option on"
VERSION=$1
mkdir choco-${VERSION}
git checkout ${VERSION}
mvn clean install -DskipTests || exit 1

mv ./target/choco-solver-${VERSION}.jar ./choco-${VERSION}
mv ./target/choco-solver-${VERSION}-with-dependencies.jar ./choco-${VERSION}
mv ./target/choco-solver-${VERSION}-sources.jar ./choco-${VERSION}

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

# copy the apidocs and the html version of the user guide to the website:
rm -r /Volumes/htdocs/apidocs/*
cp -r ./target/site/apidocs/* /Volumes/htdocs/apidocs/
rm -r /Volumes/htdocs/user_guide/*
rm -r ../Choco3-docs/html/*
cd ./src/sphinx/ && make html && cd ../..
cp -r ../Choco3-docs/html/ /Volumes/htdocs/user_guide/

git checkout master
rmdir choco-${VERSION}
