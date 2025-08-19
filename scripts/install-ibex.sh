#!/usr/bin/env bash  
set -ex
# download Ibex and untar it
root=$(pwd)
ibexver=2.9.1
curl https://codeload.github.com/ibex-team/ibex-lib/tar.gz/ibex-${ibexver} > ibex-${ibexver}.tar.gz
tar -xzf ibex-${ibexver}.tar.gz

# prepare installation
cd ibex-lib-ibex-${ibexver}
mkdir build
cd build
cmake -DLP_LIB=soplex -DBUILD_JAVA_INTERFACE=ON -DBUILD_SHARED_LIBS=ON -DINTERVAL_LIB=filib -DJAVA_PACKAGE=org.chocosolver.solver.constraints.real ..
make
sudo make install
export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib/ibex/3rd

cd ${root}
sudo rm -r ibex-${ibexver}.tar.gz

ibexsolve -v