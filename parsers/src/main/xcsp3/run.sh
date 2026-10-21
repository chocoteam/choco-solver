#!/bin/sh

# Solve, one after the other, all the XCSP3 instances (*.xml, *.xml.lzma) found in a directory.
# Usage: ./run.sh <instances-dir> [<options passed to ChocoXCSP>]
# The jar is given by the CHOCO_JAR environment variable,
# otherwise the most recent light jar in the 'parsers/target' directory of the repository is used.

if test $# -eq 0 || test ! -d "$1"
then
    echo "Usage: $0 <instances-dir> [<options passed to ChocoXCSP>]" 1>&2
    exit 1
fi

DIR="$1"
shift

if test -z "${CHOCO_JAR}"
then
    CHOCO_JAR=$(ls -t "$(dirname "$0")"/../../../target/choco-solver-*-light.jar 2>/dev/null | head -n 1)
fi
if test ! -f "${CHOCO_JAR}"
then
    echo "Choco jar file not found, build it with 'mvn package -DskipTests' or set CHOCO_JAR" 1>&2
    exit 1
fi

find "${DIR}" -type f -name "*.xml*" | sort | while read -r file
do
    echo "Processing ${file}"
    java -cp ".:${CHOCO_JAR}" org.chocosolver.parser.xcsp.ChocoXCSP "${file}" "$@" < /dev/null
done
