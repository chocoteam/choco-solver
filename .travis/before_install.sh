#!/usr/bin/env bash

curl https://github.com/codacy/codacy-coverage-reporter/releases/download/6.0.0/codacy-coverage-reporter-6.0.0-assembly.jar -o codacy-coverage-reporter-assembly.jar

if [ "${TEST_SUITE}" == "ibex" ]
then
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

  mvn validate -Dibex.path=${TRAVIS_BUILD_DIR}/ibex/plugins/java
  export LD_LIBRARY_PATH=/usr/local/lib

  wget https://github.com/sormuras/bach/raw/master/install-jdk.sh
  export JAVA_HOME=$HOME/openjdk11
  sudo $TRAVIS_BUILD_DIR/install-jdk.sh -f 11 --target $JAVA_HOME
fi
