# Building and testing

Java 17 (source and target) and Maven are required.

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

Continuous integration (`.github/workflows/maven-test.yml`) runs the groups `1s`, `10s`, `checker`, `lcg`,
then the parser groups, and Ibex. It is not triggered by changes limited to `docs/`.

## Style and headers

- Style: a Google Java Style variant, enforced by `checkstyle.xml`: `mvn checkstyle:check`.
- Every source file starts with the BSD 3-Clause header of `etc/header.txt`
  (`license-maven-plugin` checks and adds it).
- A new class has class-level Javadoc with an `@author` tag.
- In performance-sensitive code, prefer Trove4j primitive collections (e.g. `TIntArrayList`) to boxed collections.
