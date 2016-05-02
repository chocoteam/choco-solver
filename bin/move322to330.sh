#!/bin/bash

find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%package samples%package org\.chocosolver\.samples%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%package solver%package org\.chocosolver\.solver%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%package memory%package org\.chocosolver\.memory%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%package util%package org\.chocosolver\.util%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%package docs%package org\.chocosolver\.docs%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%package choco%package org\.chocosolver\.choco%g"

find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import solver%import org\.chocosolver\.solver%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import samples%import org\.chocosolver\.samples%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import memory%import org\.chocosolver\.memory%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import util%import org\.chocosolver\.util%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import choco%import org\.chocosolver\.choco%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import static solver%import static org\.chocosolver\.solver%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import static samples%import static org\.chocosolver\.samples%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import static util%import static org\.chocosolver\.util%g"
find . -type f -name *.java -print0 | xargs -0 sed -i '' "s%import static choco%import static org\.chocosolver\.choco%g"
