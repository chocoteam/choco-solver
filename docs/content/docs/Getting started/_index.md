---
author: "Charles Prud'homme"
title: "Getting Started"
linkTitle: "Getting Started"
weight: 2
description: >
  Get started with Choco-solver.
---
## Quick start

Interested in using Choco-solver in your project?
Choco-solver is available as a Java library or a Python package.

### Java
The only prerequisite for running Choco-solver is to have **Java 17** (or later) installed on your machine.
We strongly recommend to use a build automation tool, like Maven (3.6.0+) or Gradle (6+), but adding manually the JAR file to the classpath is still an option.

**Important**: `slf4j-nop` is no longer included as a runtime dependency. You must provide your own SLF4J binding (e.g., `slf4j-simple` or `logback-classic`) to avoid SLF4J warnings, or add `slf4j-nop` explicitly if you want to suppress logging:
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-nop</artifactId>
    <version>2.0.9</version>
</dependency>
```

#### Maven 3.6.0+

Copy-paste the artifact description to your `pom.xml`:

{{< highlight xml >}}
<dependency>
   <groupId>org.choco-solver</groupId>
   <artifactId>choco-solver</artifactId>
   <version>{{< param "choco_version" >}}</version>
</dependency>
{{< /highlight >}}

To test the development version, build it from the sources of the `develop` branch
and install it in your local Maven repository:

```bash
git clone -b develop https://github.com/chocoteam/choco-solver.git
cd choco-solver
mvn install -DskipTests
```

#### Gradle 6+ (requires Java 17)

Copy-paste the dependency declaration in your `build.gradle`:

{{< highlight groovy >}}
repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.choco-solver:choco-solver:{{< param "choco_version" >}}'
}
{{< /highlight >}}

#### As a stand-alone application

When one wants to manually add choco-solver as a dependency of a project, it's important to pay attention to the following points:

- Each [release](https://github.com/chocoteam/choco-solver/releases/latest) provides a single JAR file, `choco-solver-<version>-light.jar`, that defines what is necessary and sufficient to model and solve problems programmatically and includes additional functions to parse FlatZinc, XCSP3, DIMACS or MPS files directly.
- This archive embeds its dependencies. If one wants an archive without any dependencies, it is recommended to use Maven or Gradle.
- Finally, the [javadoc](https://javadoc.io/doc/org.choco-solver/choco-solver/latest/org.chocosolver/module-summary.html) of each version is also available on line.

The next step is simply to add the jar file to the classpath of your application and eventually the javadoc.

### Python

[pychoco](https://github.com/chocoteam/pychoco) provides Python bindings to Choco-solver. 64-bit wheels are built for Python 3.10 and later, on Linux, Windows and macOS. They can be directly downloaded from PyPI:

```bash
pip install pychoco
```


## Community

Stay up to date on the development of Choco solver and reach out to the community with these helpful resources.

- <i class="fa-brands fa-discord"></i> Join [the Discord server](https://discord.gg/aH6zxa7e64) for discussion and quick help.
- <i class="fa-brands fa-stack-overflow"></i> Get help on [Stack Overflow](https://stackoverflow.com/questions/tagged/choco) (tagged [`choco`](https://stackoverflow.com/questions/tagged/choco)).
- <i class="fa-brands fa-github"></i> Use the [issue tracker](https://github.com/chocoteam/choco-solver/issues) on GitHub to report issues. As far as possible, provide a [Minimal Working Example](https://en.wikipedia.org/wiki/Minimal_Working_Example).


Feel free to meet cho-coders : [@cprudhom](https://github.com/cprudhom) (Charles Prud'homme) and [@jgFages](https://github.com/jgFages) (Jean-Guillaume Fages) 

