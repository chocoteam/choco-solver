# This Makefile exists to ease common maven commands
# like running test suites

all: clean install

install: clean
	mvn -q install -DskipTests

clean:
	mvn -q clean

compile:
	mvn -q compile -DskipTests
  
tests : 1s 10s ibex checker mzn xcsp mps dimacs expl

1s 10s ibex checker mzn xcsp mps dimacs : compile
	mvn -q test -DtestFailureIgnore=true -Dgroups="$@"
