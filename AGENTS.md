# AGENTS.md

Instructions for coding agents working in this repository. Humans: the same content, with more details,
is in the developer guide, [docs/dev/](docs/dev/README.md).

## Project

Choco-solver is an open-source Java library for Constraint Programming (CP): variables, constraints,
search strategies, Lazy Clause Generation. Maven modules: `solver` (the library), `parsers` (FlatZinc, XCSP3,
DIMACS, MPS), `examples`. The user documentation (website pages) and the developer guide are in `docs/` (not built by Maven).

## Read before changing code

- [docs/dev/architecture.md](docs/dev/architecture.md): modules, packages, main abstractions.
- [docs/dev/best-practices.md](docs/dev/best-practices.md): rules learnt from real fixes. **Mandatory** before
  writing a constraint factory, a propagator or a test.
- [docs/dev/documentation.md](docs/dev/documentation.md): a user-visible change updates `docs/content/` in the same pull request.

## Build and test

```bash
mvn clean package -DskipTests              # build
mvn test -Dgroups=1s                       # fast tests; also 10s, checker, lcg
mvn test -Dtest=ClassName#methodName       # one test
mvn test -Dgroups=xcsp                     # parsers: xcsp, dimacs, mps (build the solver first)
mvn checkstyle:check                       # style
```

Details: [docs/dev/building-and-testing.md](docs/dev/building-and-testing.md).

## Conventions

- Java 17 (`<release>17</release>`); Google Java Style variant (`checkstyle.xml`).
- BSD 3-Clause header (`etc/header.txt`) in every source file; class-level Javadoc with `@author` in new classes.
- TestNG, `@Test(groups = "1s")`; tests are deterministic (settable seed).
- Prefer Trove4j primitive collections in performance-sensitive code.
- Pull requests target `develop`; `master` only receives releases.
