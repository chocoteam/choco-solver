#!/usr/bin/env bash

## WARNING: make sure $M4 is defined, otherwise flex fails

if [ "${TEST_SUITE}" == "ibex" ]
then
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
  sudo rm -r ibex-lib-ibex-${ibexver}
  sudo rm -r ibex-java-${ibexjavaver}.tar.gz

  export LD_LIBRARY_PATH=/usr/local/lib
  mvn validate -Dibex.path=${TRAVIS_BUILD_DIR}/ibex/plugins/java
fi

#if [ "${TEST_SUITE}" == "mzn" ]
#then
#  set -ex
#  mznver=2.4.3
#  curl https://codeload.github.com/MiniZinc/libminizinc/tar.gz/${mznver} > libminizinc-${mznver}.tar.gz
#  tar -xzf libminizinc-${mznver}.tar.gz
#  # prepare installation
#  cd libminizinc-${mznver}
#  mkdir build
#  cd build
#  cmake -DCMAKE_BUILD_TYPE=Release ..
#  cmake --build .
#fi
