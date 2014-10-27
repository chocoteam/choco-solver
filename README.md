## Choco3 ##

Choco3 is an open-source Java library for Constraint Programming.

Choco3 is not the continuation of Choco2, but a completely rewritten version and there is no backward compatibility.

Current stable version is 3.2.1 (13 Oct 2014).

Choco3 comes with:
- various type of variables (integer, boolean, set, graph and real),
- various state-of-the-art constraints (alldifferent, count, nvalues, etc.),
- various search strategies, from basic ones (first_fail, smallest, etc.) to most complex (impact-based and activity-based search),
- explanation-based engine, that enables conflict-based back jumping, dynamic backtracking and path repair,

But also, facilities to interact with the search loop, factories to help modelling, many samples, etc.

Choco3 is distributed under BSD licence (Copyright (c) 1999-2014, Ecole des Mines de Nantes).

Contact: choco@mines-nantes.fr


## Usage ##
### Inside a maven project ###

The maven artifact is available through a private repository
so you have first to edit your `pom.xml` to declare it:

```xml
<repositories>
    <repository>
        <id>choco.repos</id>
        <url>http://www.emn.fr/z-info/choco-repo/mvn/repository/</url>
    </repository>
</repositories>
```

Next, just declare the dependency:

```xml
<dependency>
   <groupId>choco</groupId>
   <artifactId>choco-solver</artifactId>
   <version>3.2.1</version>
</dependency>
```

### Inside a non-maven project ###

The jar can be downloaded from this URL:

* http://www.emn.fr/z-info/choco-repo/mvn/repository/choco/choco-solver/3.2.1/choco-solver-3.2.1.jar

The file contains `choco-solver` artifacts and its dependencies.

## Building from sources ##

Requirements:
* JDK 8+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install -DskipTests

If the build succeeded, the resulting jar will be automatically
installed in your local maven repository and available in the `target` sub-folders.

===================
The Choco3 dev team.
