![logo](https://github.com/chocoteam/choco-solver/blob/master/src/resources/png/ChocoLogo-160x135.png)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/chocoteam/choco-solver?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.choco-solver/choco-solver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.choco-solver/choco-solver)
[![Documentation Status](https://readthedocs.org/projects/choco-solver/badge/?version=latest)](http://choco-solver.readthedocs.org/en/latest/?badge=latest)

[![Build Status](https://travis-ci.org/chocoteam/choco-solver.svg?branch=master)](https://travis-ci.org/chocoteam/choco-solver) 
[![codecov.io](https://codecov.io/github/chocoteam/choco-solver/coverage.svg?branch=master)](https://codecov.io/github/chocoteam/choco-solver?branch=master)
[![Coverity](https://scan.coverity.com/projects/8383/badge.svg)](https://scan.coverity.com/projects/choco-solver) 
[![Codacy Badge](https://api.codacy.com/project/badge/grade/b0ab28bdd7fd4da095ad72c2c46bce57)](https://www.codacy.com/app/cprudhom/choco-solver)

* [Documentation, Support and Issues](#doc)
* [Contributing](#con)
* [Download and installation](#dow)

Choco-solver is an open-source Java library for Constraint Programming.

Choco-solver is not the continuation of Choco2, but a completely rewritten version and there is no backward compatibility.

Current stable version is 4.0.0 (13 Sep 2016).

Choco-solver comes with:
- various type of variables (integer, boolean, set, graph and real),
- various state-of-the-art constraints (alldifferent, count, nvalues, etc.),
- various search strategies, from basic ones (first_fail, smallest, etc.) to most complex (impact-based and activity-based search),
- explanation-based engine, that enables conflict-based back jumping, dynamic backtracking and path repair,

But also, facilities to interact with the search loop, factories to help modelling, many samples, etc.

Choco-solver is distributed under BSD 4-Clause License (Copyright (c) 1999-2016, Ecole des Mines de Nantes).

Contact: choco@mines-nantes.fr

### Overview

```java
// 1. Create a Model
Model model = new Model("my first problem");
// 2. Create variables
IntVar x = model.intVar("X", 0, 5);
IntVar y = model.intVar("Y", 0, 5);
// 3. Create and post constraints by using constraint factories
x.add(y).lt(5).post();
// 4. Define the search strategy
model.getSolver().setSearch(Search.inputOrderLBSearch(x, y));
// 5. Launch the resolution process
model.getSolver().solve();
// 6. Print search statistics
model.getSolver().printStatistics();
```

<a name="doc"></a>
## Documentation, Support and Issues

The [latest release](https://github.com/chocoteam/choco-solver/releases/latest) points to a 
[tarball](https://github.com/chocoteam/choco-solver/releases/download/choco-4.0.0/choco-4.0.0.zip) which contains
the binary, the source code, the user guide (pdf) and the apidocs (zip).

You can get help on our [google group](https://groups.google.com/forum/#!forum/choco-solver).
Most support requests are answered very fast.

Use the [issue tracker](https://github.com/chocoteam/choco-solver/issues) here on GitHub to report issues.
As far as possible, provide a [Minimal Working Example](https://en.wikipedia.org/wiki/Minimal_Working_Example).

<a name="con"></a>
## Contributing

Anyone can contribute to the project, from the **source code** to the **documentation**.
In order to ease the process, we established a [contribution guide](CONTRIBUTION.md)
that should be reviewed before starting any contribution as
it lists the requirements and good practices to ease the contribution process.

##### Promoting is contributing !  

[![Choco-solver](http://www.choco-solver.org/img/banner.svg)](http://www.choco-solver.org/?utm_source=badge&utm_medium=badge&utm_campaign=badge)

Following are code snippets to add on your website to help us promoting Choco-solver.

**html**:

```html
<a href="http://www.choco-solver.org/?utm_source=badge&utm_medium=badge&utm_campaign=badge">
<img border="0" alt="Choco-solver" src="http://www.choco-solver.org/img/banner.svg" width="160" height="18">
</a>
```

**Markdown**:

```md
[![Choco-solver](http://www.choco-solver.org/img/banner.svg)](http://www.choco-solver.org/?utm_source=badge&utm_medium=badge&utm_campaign=badge)
```

And thank you for giving back to choco-solver.
Please meet our team of cho-coders : 

- [@jgFages](https://github.com/jgFages) (Jean-Guillaume Fages)
- [@cprudhom](https://github.com/cprudhom) (Charles Prud'homme)



<a name="dow"></a>
## Download and installation ##

Requirements:
* JDK 8+
* maven 3+

Choco-solver is available on [Maven Central Repository](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.choco-solver%22%20AND%20a%3A%22choco-solver%22),
or directly from the [latest release](https://github.com/chocoteam/choco-solver/releases/latest).

[Snapshot releases](https://oss.sonatype.org/content/repositories/snapshots/org/choco-solver/choco-solver/) are also available for curious.

In the following, we distinguish two usages of Choco:

- as a standalone application: the jar file includes all required dependencies,
- as a library: the jar file excludes all dependencies.

The name of the jar file terms the packaging: `choco-solver-4.0.0-with-dependencies.jar` or `choco-solver-4.0.0.jar`.

A [Changelog file](./CHANGES.md) is maintained for each release.

### Inside a maven project ###

Choco is available on Maven Central Repository.
So you only have to edit your `pom.xml` to declare the following library dependency:

```xml
<dependency>
   <groupId>org.choco-solver</groupId>
   <artifactId>choco-solver</artifactId>
   <version>4.0.0</version>
</dependency>
```

Note that if you want to test snapshot release, you should update your `pom.xml` with :

```xml
<repository>
    <id>sonatype</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
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
    com.github.cp-profiler:cpprof-java:1.2.0
    args4j:args4j:2.33
    org.javabits.jgrapht:jgrapht-core:0.9.3
    org.choco-solver:choco-sat:1.0.2
    net.sf.trove4j:trove4j:3.0.3
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
The Choco-solver dev team.
