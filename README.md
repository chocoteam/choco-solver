![logo](http://choco-solver.org/sites/default/files/ChocoLogo-160x135.png)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/chocoteam/choco3?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Build Status](https://travis-ci.org/chocoteam/choco3.svg?branch=master)](https://travis-ci.org/chocoteam/choco3) [![codecov.io](https://codecov.io/github/chocoteam/choco3/coverage.svg?branch=master)](https://codecov.io/github/chocoteam/choco3?branch=master)

* [Documentation, Support and Issues](#doc)
* [Contributing](#con)
* [Download and installation](#dow)

Choco3 is an open-source Java library for Constraint Programming.

Choco3 is not the continuation of Choco2, but a completely rewritten version and there is no backward compatibility.

Current stable version is 3.3.2 (17 Nov 2015).

Choco3 comes with:
- various type of variables (integer, boolean, set, graph and real),
- various state-of-the-art constraints (alldifferent, count, nvalues, etc.),
- various search strategies, from basic ones (first_fail, smallest, etc.) to most complex (impact-based and activity-based search),
- explanation-based engine, that enables conflict-based back jumping, dynamic backtracking and path repair,

But also, facilities to interact with the search loop, factories to help modelling, many samples, etc.

Choco3 is distributed under BSD 4-Clause License (Copyright (c) 1999-2015, Ecole des Mines de Nantes).

Contact: choco@mines-nantes.fr

### Overview

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

<a name="doc"></a>
## Documentation, Support and Issues

The archive file downloaded from the [official website](http://choco-solver.org/?q=Download) contains
both the user guide (pdf) and the apidocs (zip).

You can get help on our [forum](http://choco-solver.org/?q=Forum).
Most support requests are answered very fast.

Use the [issue tracker](https://github.com/chocoteam/choco3/issues) here on GitHub to report issues.
As far as possible, provide a [Minimal Working Example](https://en.wikipedia.org/wiki/Minimal_Working_Example).

<a name="con"></a>
## Contributing

Anyone can contribute to the project, from the **source code** to the **documentation**.
In order to ease the process, we established a [contribution guide](CONTRIBUTION.md)
that should be reviewed before starting any contribution as
it lists the requirements and good practices to ease the contribution process.

##### Promoting is contributing !  

[![Choco3](http://choco-solver.org/sites/default/files/banner.svg)](http://choco-solver.org/?utm_source=badge&utm_medium=badge&utm_campaign=badge)

Following are code snippets to add on your website to help us promoting Choco3.

**html**:

```html
<a href="http://choco-solver.org/?utm_source=badge&utm_medium=badge&utm_campaign=badge">
<img border="0" alt="Choco3" src="http://choco-solver.org/sites/default/files/banner.svg" width="160" height="18">
</a>
```

**Markdown**:

```md
[![Choco3](http://choco-solver.org/sites/default/files/banner.svg)](http://choco-solver.org/?utm_source=badge&utm_medium=badge&utm_campaign=badge)
```


<a name="dow"></a>
## Download and installation ##

Requirements:
* JDK 8+ (JDK 7 compliant jars are also available)
* maven 3+

Choco3 is available on [Maven Central Repository](http://search.maven.org/#search%7Cga%7C1%7Corg.choco-solver),
or directly from the [official website](http://choco-solver.org/?q=Download).

[Snapshot releases](https://oss.sonatype.org/content/repositories/snapshots/org/choco-solver/choco-solver/) are also available for curious.

In the following, we distinguish two usages of Choco:

- as a standalone application: the jar file includes all required dependencies,
- as a library: the jar file excludes all dependencies.

The name of the jar file terms the packaging: `choco-solver-3.3.2-with-dependencies.jar` or `choco-solver-3.3.2.jar`.

A [Changelog file](./CHANGES.md) is maintained for each release.

### Inside a maven project ###

Choco is available on Maven Central Repository.
So you only have to edit your `pom.xml` to declare the following library dependency:

```xml
<dependency>
   <groupId>org.choco-solver</groupId>
   <artifactId>choco-solver</artifactId>
   <version>3.3.2</version>
</dependency>
```

### As a stand-alone application ###

The jar file contains all required dependencies.
The next step is simply to add the jar file to your classpath of your application.
Note that if your program depends on dependencies declared in the jar file,
you should consider using choco as a library.

### As a library ###

The jar file does not contains any dependencies,
as of being used as a dependency of another application.
The next step is to add the jar file to your classpath of your application and also add the required dependencies.


### Dependencies ###

The required dependencies for compilation are:

    dk.brics.automaton:automaton:1.11-8
    args4j:args4j:2.32
    org.javabits.jgrapht:jgrapht-core:0.9.3
    net.sf.trove4j:trove4j:3.0.3
    com.github.cp-profiler:cpprof-java:1.1.0
    org.zeromq:jeromq:0.3.4
    com.google.protobuf:protobuf-java:2.6.1

They are available on Maven Repository (http://mvnrepository.com/).


### Building from sources ###

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean package -DskipTests

If the build succeeded, the resulting jar will be automatically
installed in your local maven repository and available in the `target` sub-folders.


===================
The Choco3 dev team.
