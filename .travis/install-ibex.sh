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
echo "parameters:"
pwd
echo ${TRAVIS_BUILD_DIR}