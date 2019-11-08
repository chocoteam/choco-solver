#!/usr/bin/env bash

if [ "${TEST_SUITE}" == "ibex" ]
then
  curl https://raw.githubusercontent.com/sormuras/bach/master/install-jdk.sh > install-jdk.sh
  export J11_HOME="$JAVA_HOME"
  export JAVA_HOME=$HOME/openjdk9
  /bin/bash $TRAVIS_BUILD_DIR/install-jdk.sh -f 9 -t "$JAVA_HOME"

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

  export LD_LIBRARY_PATH=/usr/local/lib

  export JAVA_HOME="$J11_HOME"
  echo "$JAVA_HOME"

  mvn validate -Dibex.path=${TRAVIS_BUILD_DIR}/ibex/plugins/java
fi
