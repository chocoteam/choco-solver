Installing Choco |version|
==========================

Choco |version| is a java library based on `Java 7 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_.
The main library is named ``choco-solver`` and can be seen as the core library.
Some extensions are also provided, such as ``choco-parsers`` or ``choco-cpviz``, and rely on but not include ``choco-solver``.

Which jar to select ?
---------------------

We provide a zip file which contains the following files:

apidocs-|release|.zip
 Javadoc of Choco-|release|
choco-solver-|release|.jar
 An ready-to-use jar file ; it provides tools to declare a Solver, the variables, the constraints, the search strategies, etc. In a few words, it enables modeling and solving CP problems.
choco-solver-|release|-sources.jar
 The source of the core library.
choco-samples-|release|-sources.jar
 The source of the artifact `choco-samples` made of problems modeled with Choco. It is a good start point to see what it is possible to do with Choco.

There are also official extensions, thus maintained by the Choco team. They are provided apart from the zip file.
Each of the following extensions include dependencies but choco-solver classes, which ease their usage.
The available extensions are:

choco-parsers-|release|.jar
 This extension provides tools to parse modelling languages to Choco; it should be selected to work with MiniZinc and FlatZinc files.
choco-gui-|release|.jar
 This extension provides a Graphical User Interface to interact and visualize the search process.
choco-cpviz-|release|.jar
 This extension produces files required for the cpviz software.
choco-geost-|release|.jar
 This extension provides support for the well-known Geost constraint, which is almost a solver by itself.
choco-exppar-|release|.jar
 This extension provides an expression parser to ease modelling.

.. note::
    Each of those extensions include all dependencies but choco-solver classes, which ease their usage.


To start using Choco |version|, you need to be make sure that the right version of java is installed.
Then you can simply add the ``choco-solver`` jar file (and extension libraries) to your classpath or declare them as dependency of a Maven-based project.

Update the classpath
--------------------

Simply add the jar file to the classpath of your project (in cli or in your favorite IDE).

.. parsed-literal::

   java -cp .:choco-solver-|release|.jar my.project.Main
   java -cp .:choco-solver-|release|.jar:choco-parsers-|release|.jar my.other.project.Main



As a Maven Dependency
---------------------

Choco is build and managed using `Maven3 <http://maven.apache.org/download.cgi>`_.
To declare Choco as a dependency of your project, simply update the ``pom.xml`` of your project by adding the following instruction:

.. code-block:: xml

   <dependency>
    <groupId>choco</groupId>
    <artifactId>choco-solver</artifactId>
    <version>X.Y.Z</version>
   </dependency>

where ``X.Y.Z`` is replaced by |release|.

You need to add a new repository to the list of declared ones in the ``pom.xml`` of your project:

.. code-block:: xml
 
 <repository>
   <id>choco.repos</id> 
   <url>http://www.emn.fr/z-info/choco-repo/mvn/repository/</url>
 </repository>


Compiling sources
-----------------

As a Maven-based project, Choco can be installed in a few instructions.
Once you have downloaded the source (from the zip file or `GitHub <https://github.com/chocoteam/choco3>`_, simply run the following command:

.. code-block:: bash

  mvn clean install -DskipTests

This instruction downloads the dependencies required for Choco3 (such as the `trove4j <http://trove.starlight-systems.com/>`_ and `logback <http://logback.qos.ch/>`_) then compiles the sources. The instruction ``-DskipTests`` avoids running the tests after compilation (and saves you a couple of hours). Regression tests are run on a private continuous integration server.

Maven provides commands to generate files needed for an IDE project setup.
For example, to create the project files for your favorite IDE:

IntelliJ Idea
  .. code-block:: bash
   
   mvn idea:idea

Eclipse
  .. code-block:: bash

   mvn eclipse:eclipse


