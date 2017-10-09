#!/usr/bin/env bash

set -ex
# download Ibex and untar it
curl http://www.ibex-lib.org/sites/default/files/ibex-2.6.0.tgz > ibex-2.6.0.tgz
tar -xzf ibex-2.6.0.tgz
# download java plugin
curl http://www.ibex-lib.org/sites/default/files/ibex-java.tar.gz > ibex-java.tar.gz
tar -xzf ibex-java.tar.gz -C ibex-2.6.0/plugins
# prepare installation
cd ibex-2.6.0
./waf configure --enable-shared --with-jni --java-package-name=org.chocosolver.solver.constraints.real
sudo ./waf install
export LD_LIBRARY_PATH=/usr/local/lib
#cd __build__/plugins/java/src
#java -Djava.library.path=/usr/local/lib Test
cd -
sudo rm -r ibex-2.6.0.tgz
sudo rm -r ibex-2.6.0
sudo rm -r ibex-java.tar.gz

