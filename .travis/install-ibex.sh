#!/usr/bin/env bash

set -ex
wget http://www.ibex-lib.org/sites/default/files/ibex-2.3.4.tgz
wget http://www.ibex-lib.org/sites/default/files/ibex-java-ibex2.3.tar.gz
tar -xzvf ibex-2.3.4.tgz ibex-2.3.4/
HIBEX=`pwd`
mv ibex-java-ibex2.3.tar.gz ibex-2.3.4/plugins/ibex-java-ibex2.3.tar.gz
cd ibex-2.3.4/plugins
tar -xzvf ibex-java-ibex2.3.tar.gz
cd ..
./waf configure --enable-shared --with-jni --with-java-package=org.chocosolver.solver.constraints.real
sudo ./waf install
# to avoid error with LICENSE header update
export LD_LIBRARY_PATH=/usr/local/lib
cd __build__/src/java
java Test
cd ${HIBEX}
mv __build__ ../ibex
cd ..
sudo rm -r ibex-2.3.4/
sudo find ./ibex -name "*.java" -exec rm {} \;
