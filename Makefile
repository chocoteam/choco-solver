# This Makefile exists to ease common maven commands
# like running test suites

ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PRETTY_DATE := $(shell date +'%m/%d/%Y %H:%M')
DATE := $(shell date +'%y%m%d_%H%M')
CURRENT_VERSION := $(shell mvn help:evaluate -Dexpression=project.version | grep -v "\[INFO\]" | grep -v "\[WARNING\]")

.PHONY: all clean compile tests 1s 10s ibex checker mzn xcsp mps dimacs expl update_date compet msc delmsc help
.DEFAULT_GOAL := package

help:
	@echo "Please use \`make <target>' where <target> is one of"
	@echo "  all        			to clean, compile and package the project"
	@echo "  package    			to clean and package the project"
	@echo "  install    			to clean and install the project"
	@echo "  clean      			to clean the project"
	@echo "  compile    			to compile the project"
	@echo "  tests      			to run all tests"
	@echo "  1s         			to run all tests in 1s"
	@echo "  10s        			to run all tests in 10s"
	@echo "  ibex       			to run all tests with ibex"
	@echo "  checker    			to run all tests with checker"
	@echo "  mzn        			to run all tests with mzn"
	@echo "  xcsp       			to run all tests with xcsp"
	@echo "  mps        			to run all tests with mps"
	@echo "  dimacs     			to run all tests with dimacs"
	@echo "  expl       			to run all tests with expl"
	@echo "  lcg       				to run all tests with lcg"
	@echo "  update_date 			to update the date in the parsers"
	@echo "  compet     			to update the date, clean and package the project"
	@echo "  msc        			for MiniZincIDE, to install the msc file in ~/.minizinc/solvers"
	@echo "  delmsc VERSION=xxx     for MiniZincIDE, to delete the msc file in ~/.minizinc/solvers"
	@echo "  antlr					to compile the antlr grammar"

all: clean package

package: clean
	mvn -q package -DskipTests

install: clean
	mvn -q install -DskipTests

clean:
	mvn -q clean

compile:
	mvn -q compile -DskipTests

tests : 1s 10s ibex checker mzn xcsp mps dimacs expl lcg

1s 10s ibex checker mzn xcsp mps dimacs: compile
	mvn -q test -DtestFailureIgnore=true -Dgroups="$@" -Dexcludegroups="lcg"

lcg: compile
	mvn -q test -DtestFailureIgnore=true -Dgroups="$@"

update_date:
	@sed -i '' 's|\s*System.out.printf("c Choco.*|System.out.printf("c Choco%s ($(PRETTY_DATE))\\n", lcg? " with LCG" : "");|' parsers/src/main/java/org/chocosolver/parser/xcsp/XCSP.java
	@sed -i '' 's|\s*System.out.printf("%% Choco%s (.*|System.out.printf("%% Choco%s ($(PRETTY_DATE))\\n", lcg? " with LCG" : "");|' parsers/src/main/java/org/chocosolver/parser/flatzinc/Flatzinc.java

compet: update_date clean package

msc: compet
	@sed -i '' 's|  "version" : .*|  "version" : "$(CURRENT_VERSION)",|' parsers/src/main/minizinc/choco.msc
	@sed -i '' 's|SNAPSHOT|$(DATE)|g' parsers/src/main/minizinc/choco.msc
	@sed -i '' 's|  "mznlib" : .*|  "mznlib" : "$(ROOT_DIR)/parsers/src/main/minizinc/mzn_lib/",|' parsers/src/main/minizinc/choco.msc
	@sed -i '' 's|  "executable" : .*|  "executable" : "$(ROOT_DIR)/parsers/src/main/minizinc/fzn-choco",|' parsers/src/main/minizinc/choco.msc
	@sed -i '' 's|^[^ ]*JAR_FILE=.*|JAR_FILE="$(ROOT_DIR)/parsers/target/choco-parsers-$(CURRENT_VERSION)-light.jar"|' parsers/src/main/minizinc/fzn-choco.py
	@cp $(ROOT_DIR)/parsers/src/main/minizinc/choco.msc ~/.minizinc/solvers/choco-$(CURRENT_VERSION)-$(DATE).msc

delmsc:
	@rm ~/.minizinc/solvers/choco-$(VERSION).msc

docker: compet
	@docker build -f $(ROOT_DIR)/parsers/src/main/minizinc/docker/Dockerfile.dms -t chocoteam/choco-solver-mzn:$(CURRENT_VERSION) $(ROOT_DIR)

antlr:
	cd parsers/
	mvn org.antlr:antlr4-maven-plugin:antlr4
	cd ..