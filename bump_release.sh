#!/bin/sh
#Script to notify the website about a release

function sedInPlace {
	if [ $(uname) = "Darwin" ]; then
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

if [ $# -ne 2 ]; then
    echo "Usage: $0 code version_number"
    echo "'code': upgrade the version number in the code"
    exit 1
fi
VERSION=$2
REPO_URL="http://www.emn.fr/z-info/choco-repo/mvn/repository/choco"

case $1	in

code)

	## The README.md
	# Update of the version number for maven usage

	sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md

	snapshot=0
	echo $VERSION | grep "\-SNAPSHOT$" > /dev/null && snapshot=1

	if [ $snapshot = 0 ]; then
		# Update the bundle and the apidoc location
		sedInPlace "s%$REPO_URL.*choco\-solver.*%$REPO_URL/choco\-solver/$VERSION/choco\-solver\-$VERSION\-jar\-with\-dependencies\.jar%" README.md
	else
		# Update the bundle and the apidoc location
		sedInPlace "s%$REPO_URL.*choco\-solver.*%$REPO_URL/choco\-solver/$VERSION/choco\-solver\-$VERSION\-jar\-with\-dependencies\.jar%" README.md
	fi
	## The CHANGES.md file
	d=`LANG=en_US.utf8 date +"%d %b %Y"`
	REGEX="s%????*%${VERSION} - ${d}%"
	sedInPlace "${REGEX}" CHANGES.md

    echo -e "\n????\n-------------------\n" >> CHANGES.md

	;;
	*)
		echo "Target must be either 'site' or 'code'"
		exit 1
esac