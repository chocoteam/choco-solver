## Choco3 ##

Choco3 is an open-source Java library for Constraint Programming.

Choco3 is not the continuation of Choco2, but a completely rewritten version and there is no backward compatibility.

Current stable version is 3.3.0 (04 Dec 2014).

Choco3 comes with:
- various type of variables (integer, boolean, set, graph and real),
- various state-of-the-art constraints (alldifferent, count, nvalues, etc.),
- various search strategies, from basic ones (first_fail, smallest, etc.) to most complex (impact-based and activity-based search),
- explanation-based engine, that enables conflict-based back jumping, dynamic backtracking and path repair,

But also, facilities to interact with the search loop, factories to help modelling, many samples, etc.

Choco3 is distributed under BSD licence (Copyright (c) 1999-2014, Ecole des Mines de Nantes).

Contact: choco@mines-nantes.fr

## Overview ##


```java
// 1. Create a Solver
Solver solver = new Solver("my first problem");
// 2. Create variables through the variable factory
IntVar x = VariableFactory.bounded("X", 0, 5, solver);
IntVar y = VariableFactory.bounded("Y", 0, 5, solver);
// 3. Create and post constraints by using constraint factories
solver.post(IntConstraintFactory.arithm(x, "+", y, "<", 5));
// 4. Define the search strategy
solver.set(IntStrategyFactory.lexico_LB(new IntVar[]{x, y}));
// 5. Launch the resolution process
solver.findSolution();
//6. Print search statistics
Chatterbox.printStatistics(solver);
```

## Download and installation ##

Requirements:
* JDK 8+
* maven 3+

In the following, we distinguish two usages of Choco:

- as a standalone application: the jar file includes all required dependencies and configuration file (that is, `logback.xml`),
- as a library: the jar file excludes all dependencies and configuration file (that is, `logback.xml`).

The name of the jar file terms the packaging: `choco-solver-3.3.0-with-dependencies.jar` or `choco-solver-3.3.0.jar`.

The jar files can be downloaded from this URL:

* http://choco-solver.org/?q=Download

### As a stand-alone application ###

The jar file contains all required dependencies (including SLF4J, Logback and logback.xml).
The next step is simply to add the jar file to your classpath of your application.
Note that if your program depends on dependencies declared in the jar file,
you should consider using choco as a library.

### As a library ###

The jar file does not contains any dependencies or Logback configuration file,
as of being used as a dependency of another application.
The next step is to add the jar file to your classpath of your application and add also add the required dependencies.


### Inside a maven project ###

Choco is available on Maven Central Repository.
So you only have to edit your `pom.xml` to declare the following library dependency:

```xml
<dependency>
   <groupId>choco</groupId>
   <artifactId>choco-solver</artifactId>
   <version>3.3.0</version>
</dependency>
```

### Dependencies ###

The required dependencies for compilation are:

    org.javabits.jgrapht:jgrapht-core:jar:0.9.3
    dk.brics.automaton:automaton:jar:1.11-8
    args4j:args4j:jar:2.0.29
    net.sf.trove4j:trove4j:jar:3.0.3
    org.slf4j:slf4j-api:jar:1.7.7

They are available on Maven Repository (http://mvnrepository.com/).


### Building from sources ###

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install -DskipTests

If the build succeeded, the resulting jar will be automatically
installed in your local maven repository and available in the `target` sub-folders.

===================
The Choco3 dev team.
