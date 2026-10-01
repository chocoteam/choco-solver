#!/bin/bash
#
# Equivalence bench of ModelSpec on a set of instances, each one in its own JVM.
#
# Usage: spec-bench.sh <classpath> <output dir> <workers> <time limit (s)> <heap> <instance dir>...
# Instances: *.fzn, *.xml, *.xml.lzma found under the instance directories.
# Output: <output dir>/results.tsv (status, instance, seconds, detail), one log per instance in <output dir>/logs.
# Extra JVM options (e.g., -Dspec.nodes=2000) can be passed with the SPEC_OPTS environment variable, and another main
# class with SPEC_MAIN (default: org.chocosolver.parser.spec.SpecEquivalenceMain).
#
set -u
CP=$1; OUT=$2; WORKERS=$3; LIMIT=$4; HEAP=$5; shift 5
mkdir -p "$OUT/logs"
export CP OUT LIMIT HEAP
find "$@" -type f \( -name '*.fzn' -o -name '*.xml' -o -name '*.xml.lzma' \) | sort > "$OUT/instances.txt"
echo "$(wc -l < "$OUT/instances.txt") instances, $WORKERS workers, $LIMIT s, $HEAP"
tr '\n' '\0' < "$OUT/instances.txt" | xargs -0 -P "$WORKERS" -I{} sh -c '
  f="$1"; n=$(basename "$f")
  java -Xmx"$HEAP" ${SPEC_OPTS:-} -cp "$CP" ${SPEC_MAIN:-org.chocosolver.parser.spec.SpecEquivalenceMain} "$f" "$LIMIT" \
    > "$OUT/logs/$n.out" 2> "$OUT/logs/$n.err"
  code=$?
  if ! grep -q "^RESULT" "$OUT/logs/$n.out"; then
    reason=$(grep -m1 -o "OutOfMemoryError" "$OUT/logs/$n.err" || echo "exit $code")
    printf "RESULT\tCRASH\t%s\t-\t%s\n" "$n" "$reason" > "$OUT/logs/$n.out"
  fi
  grep "^RESULT" "$OUT/logs/$n.out" | cut -f2- >> "$OUT/results.tsv"
' _ {}
echo "done"; cut -f1 "$OUT/results.tsv" | sort | uniq -c
