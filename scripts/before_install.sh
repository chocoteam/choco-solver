#!/usr/bin/env bash

## WARNING: make sure $M4 is defined, otherwise flex fails

if [ "${TEST_SUITE}" == "ibex" ]
then
  set -ex
  # download Ibex and untar it
  ibexver=2.8.7
  curl https://codeload.github.com/ibex-team/ibex-lib/tar.gz/ibex-${ibexver} > ibex-${ibexver}.tar.gz
  tar -xzf ibex-${ibexver}.tar.gz
  # prepare installation
  cd ibex-lib-ibex-${ibexver}
  ./waf configure --enable-shared --with-jni --java-package-name=org.chocosolver.solver.constraints.real
  ./waf build
  sudo ./waf install
  export LD_LIBRARY_PATH=/usr/local/lib
  cd -
  sudo rm -r ibex-${ibexver}.tar.gz
  sudo rm -r ibex-lib-ibex-${ibexver}

  export LD_LIBRARY_PATH=/usr/local/lib
  mvn validate -Dibex.path=${TRAVIS_BUILD_DIR}/ibex/plugins/java
fi
