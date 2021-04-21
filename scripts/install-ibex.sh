#!/usr/bin/env bash  
set -ex
# download Ibex and untar it
ibexver=2.8.8
ibexjavaver=1.2.0
curl https://codeload.github.com/ibex-team/ibex-lib/tar.gz/ibex-${ibexver} > ibex-${ibexver}.tar.gz
tar -xzf ibex-${ibexver}.tar.gz
curl https://codeload.github.com/ibex-team/ibex-java/tar.gz/${ibexjavaver} > ibex-java-${ibexjavaver}.tar.gz
tar -xzf ibex-java-${ibexjavaver}.tar.gz --directory ibex-lib-ibex-${ibexver}/plugins/
# prepare installation
cd ibex-lib-ibex-${ibexver}
./waf configure --enable-shared --with-jni --java-package-name=org.chocosolver.solver.constraints.real
./waf build
sudo ./waf install
export LD_LIBRARY_PATH=/usr/local/lib
cd -
sudo rm -r ibex-${ibexver}.tar.gz
sudo rm -r ibex-java-${ibexjavaver}.tar.gz

ibexsolve -v