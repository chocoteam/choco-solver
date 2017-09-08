#!/usr/bin/env bash

set -ex
wget http://www.ibex-lib.org/sites/default/files/ibex-2.3.4.tgz
wget http://www.ibex-lib.org/sites/default/files/ibex-java-ibex2.3.tar.gz
tar -xzvf ibex-2.3.4.tgz ibex-2.3.4/
mv ibex-java-ibex2.3.tar.gz ibex-2.3.4/plugins/ibex-java-ibex2.3.tar.gz
cd ibex-2.3.4/plugins
tar -xzvf ibex-java-ibex2.3.tar.gz
cd ..
./waf configure --enable-shared --with-jni --with-java-package=org.chocosolver.solver.constraints.real
sudo ./waf install
# to avoid error with LICENSE header update
mv __build__ ../ibex
cd ..
rm -r ibex-2.3.4/
find ./ibex -name "*.java" -exec rm {} \;
