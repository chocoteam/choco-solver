# Building and testing

Java 17 (source and target) and Maven are required.

## Initialize the submodule (CPProfiler)

The profiler integration (`solver/src/main/java/cpp`, used by `CPProfiler`) is a git submodule
([chocoteam/cpp-integration](https://github.com/chocoteam/cpp-integration)). If it is not initialized, `solver` does not
compile (`package cpp does not exist`, `cannot find symbol Connector` in `CPProfiler.java`).

```bash
git clone --recurse-submodules https://github.com/chocoteam/choco-solver.git   # new clone
git submodule update --init                                                     # existing clone
```

`git worktree add` does not initialize submodules: run `git submodule update --init` in each new worktree.

## Build

```bash
mvn clean package -DskipTests
```

## Tests

Tests use **TestNG** and are sorted into groups; run the group that matches what you changed.

```bash
mvn test -Dgroups=1s        # tests under 1 second
mvn test -Dgroups=10s       # tests under 10 seconds
mvn test -Dgroups=checker   # constraint checkers
mvn test -Dgroups=lcg       # Lazy Clause Generation
mvn test -Dtest=ClassName                 # one class
mvn test -Dtest=ClassName#methodName      # one method

# parsers (build the solver first)
mvn test -Dgroups=xcsp
mvn test -Dgroups=dimacs
mvn test -Dgroups=mps

mvn test -Dgroups=ibex      # needs Ibex, see scripts/install-ibex.sh
mvn test -Pcoverage         # with code coverage
```

Rules (details and rationale in [best-practices.md](best-practices.md), section 4):

- a test must be deterministic: any randomness uses a seed that can be set;
- tag each test with its group, e.g. `@Test(groups = "1s")`;
- prefer many short tests to a long one.

Continuous integration (`.github/workflows/maven-test.yml`) runs, in parallel, the groups `1s`, `10s`, `checker`, `lcg`,
the parser groups, `mzn` (a job of its own, re-run alone when it fails on a slow runner) and Ibex. It is not triggered by changes limited to `docs/`.
A failed test does not stop the run: each job ends with the list of all its failed tests
(`scripts/test-failures.py`, also in the job summary). Locally, `mvn test -Dmaven.test.failure.ignore=true` tests every module
whatever the failures, and `python3 scripts/test-failures.py` lists them.

## Style and headers

- Style: a Google Java Style variant, enforced by `checkstyle.xml`: `mvn checkstyle:check`.
- Every source file starts with the BSD 3-Clause header of `etc/header.txt`
  (`license-maven-plugin` checks and adds it).
- A new class has class-level Javadoc with an `@author` tag.
- In performance-sensitive code, prefer Trove4j primitive collections (e.g. `TIntArrayList`) to boxed collections.
