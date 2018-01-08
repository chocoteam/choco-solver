#!/usr/bin/env bash

set -ex
# download Ibex and untar it
ibexver=2.6.3
curl https://codeload.github.com/ibex-team/ibex-lib/tar.gz/ibex-${ibexver} > ibex-${ibexver}.tar.gz
tar -xzf ibex-${ibexver}.tar.gz
# download java plugin
curl http://www.ibex-lib.org/sites/default/files/ibex-java.tar.gz > ibex-java.tar.gz
tar -xzf ibex-java.tar.gz -C ibex-lib-ibex-${ibexver}/plugins
# prepare installation
cd ibex-lib-ibex-${ibexver}
./waf configure --enable-shared --with-jni --java-package-name=org.chocosolver.solver.constraints.real
sudo ./waf install
export LD_LIBRARY_PATH=/usr/local/lib
#cd __build__/plugins/java/src
#java -Djava.library.path=/usr/local/lib Test
cd -
sudo rm -r ibex-${ibexver}.tgz
sudo rm -r ibex-lib-ibex-${ibexver}
sudo rm -r ibex-java.tar.gz

