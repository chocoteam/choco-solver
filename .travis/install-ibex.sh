#!/usr/bin/env bash

set -ex
# download Ibex and untar it
ibexver=2.6.5
curl https://codeload.github.com/ibex-team/ibex-lib/tar.gz/ibex-${ibexver} > ibex-${ibexver}.tar.gz
tar -xzf ibex-${ibexver}.tar.gz
# prepare installation
cd ibex-lib-ibex-${ibexver}
./waf configure --enable-shared --with-jni --java-package-name=org.chocosolver.solver.constraints.real
sudo ./waf install
export LD_LIBRARY_PATH=/usr/local/lib
cd -
sudo rm -r ibex-${ibexver}.tar.gz
sudo rm -r ibex-lib-ibex-${ibexver}

