#!/usr/bin/env bash

set -ex
curl http://www.ibex-lib.org/sites/default/files/ibex-2.3.4.tgz > ibex-2.3.4.tgz
curl http://www.ibex-lib.org/sites/default/files/ibex-java-ibex2.3.tar.gz > ibex-java-ibex2.3.tar.gz
tar -xzf ibex-2.3.4.tgz ibex-2.3.4/
HIBEX=`pwd`
mv ibex-java-ibex2.3.tar.gz ibex-2.3.4/plugins/ibex-java-ibex2.3.tar.gz
cd ibex-2.3.4/plugins
tar -xzvf ibex-java-ibex2.3.tar.gz
cd ..
./waf configure --enable-shared --with-jni --with-java-package=org.chocosolver.solver.constraints.real
sudo ./waf install
ls -la /usr/local/lib/libibex*
cd __build__/plugins/java/src
java -Djava.library.path=/usr/local/lib Test
cd ${HIBEX}/
sudo rm -r ibex-2.3.4.tgz
sudo rm -r ibex-2.3.4/

