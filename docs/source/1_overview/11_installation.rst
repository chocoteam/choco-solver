Installing Choco |version|
=========================

Choco |version| is a java library based on `Java 7 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_.
First of all, you need to be make sure that the right version of java is installed.

Update the classpath
~~~~~~~~~~~~~~~~~~~~

Simply add the jar file to the classpath of your project (in cli or in your favorite IDE).

.. code::

  $ java -cp .:choco- |version|.jar my.project.Main


Which jar to select ?
~~~~~~~~~~~~~~~~~~~~~

We provide a zip file which contains the following files:

apidocs-|version|.zip   
 Javadoc of Choco-|version|
choco-solver-3.1.1-jar-with-dependencies.jar
 An ready-to-use jar file which contains `choco-environment` and `choco-solver` artifacts and dependencies; it enable modeling and solving CP problems. 
choco-solver-|version|-sources.jar
 The source of the artifacts `choco-environment` and `choco-solver`.
choco-parser-|version|.jar
 A jar file base on the artifact `choco-parser` without any dependency; it should be selected to input FlatZinc files.
choco-parser-|version|-jar-with-dependencies.jar
 A ready-to-use jar file which contains the following artifacts: `choco-environment`, `choco-solver` and `choco-parser` and the required dependencies. **This should be the default choice**.   
choco-samples-|version|-sources.jar
 The source of the artifact `choco-samples` made of problems modeled with Choco.

As a Maven Dependency
~~~~~~~~~~~~~~~~~~~~~~

Choco is build and managed using `Maven3 <http://maven.apache.org/download.cgi>`_.
To declare Choco as a dependency of your project, simply update the ``pom.xml`` of your project by adding the following instruction:

.. code :: xml

 <dependency>
   <groupId>choco</groupId> 
   <artifactId>choco-solver</artifactId> 
   <version>|version|</version>
 </dependency>

You need to add a new repository to the list of declared ones in the ``pom.xml`` of your project:

.. code :: xml
 
 <repository>
   <id>choco.repos</id> 
   <url>http://www.emn.fr/z-info/choco-repo/mvn/repository/</url>
 </repository>


Compiling sources
~~~~~~~~~~~~~~~~~

As a Maven-based project, Choco can be installed in a few instructions. 
First, run the following command:

.. code::

  $ mvn install -DskipTests

This instruction downloads the dependencies required for Choco3 (such as the `trove4j <http://trove.starlight-systems.com/>`_ and `logback <http://logback.qos.ch/>`_) then compiles the sources. The instruction ``-DskipTests`` avoids running the tests after compilation (and saves you a couple of hours). Regression tests are run on a private continuous integration server.

Maven provides commands to generate files needed for an IDE project setup.
For example, to create the project files for your favorite IDE:

IntelliJ Idea
 ``$ mvn idea:idea``
Eclipse
 ``$ mvn eclipse:eclipse``


