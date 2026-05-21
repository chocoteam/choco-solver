#!/bin/sh

jar=/Users/kyzrsoze/Sources/CHOCO/develop/parsers/target/choco-solver-5.0.0-beta.2-light.jar
#files=$(find -f "/Users/kyzrsoze/Sources/XCSP3/COP22to24/")
files=$(find -f "/Users/kyzrsoze/Sources/XCSP3/CSP22to24")
for file in $files;
do
    echo "Processing"
    java -cp .:$jar org.chocosolver.parser.xcsp.ChocoXCSP $file
done