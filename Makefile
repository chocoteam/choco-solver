# This Makefile exists to ease common maven commands
# like running test suites

all: clean install

install: clean
	mvn -q install -DskipTests

clean:
	mvn -q clean

tests : 1s 10s ibex checker mzn xcsp mps

1s 10s ibex checker mzn xcsp mps: clean install
	mvn -q test -DtestFailureIgnore=true -Dgroups="$@"
