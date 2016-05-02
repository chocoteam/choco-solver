#############
Preliminaries
#############

******************
About Choco Solver
******************

Choco is a Free and Open-Source Software dedicated to Constraint Programming.
It is written in Java, under `BSD <http://opensource.org/licenses/BSD-3-Clause>`_ license.
It aims at describing real combinatorial problems in the form of Constraint Satisfaction Problems and
solving them with Constraint Programming techniques. Choco is used for:

- teaching : easy to use
- research : easy to extend
- real-life applications : easy to integrate

Choco is among the fastest CP solvers on the market.
In 2013 and 2014, Choco has been awarded two silver medals and three bronze medals
at the MiniZinc challenge that is the world-wide competition of constraint-programming solvers.
In addition to these performance results, Choco benefits from academic contributors, who provide
support and long term improvements, and the consulting company `COSLING <http://www.cosling.com>`_, which
provides services ranging from training to the development and the integration of CP models into larger applications.

Choco official website is: `<http://www.choco-solver.org>`_

Technical overview
==================

Choco Solver |version| is a `Java 8 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_ library including:

- various type of variables (integer, boolean, set and real),
- many constraints (alldifferent, count, nvalues, etc.),
- a configurable search (custom search, activity-based search, large neighborhood search, etc.),
- conflict explanations (conflict-based back jumping, dynamic backtracking, path repair, etc.).

It also includes facilities to interact with the search process, factories to help modeling, many samples, an interface to Ibex, etc.
Choco Solver has also many `extensions <http://choco-solver.org/?q=extensions>`_,
including a FlatZinc parser to solve minizinc instances and a graph module to better solve graph problems such as the TSP.
An overview of the features of Choco |version| may also be found in the presentation made in the
`"CP Solvers: Modeling, Applications, Integration, and Standardization" <http://www.choco-solver.org/sites/materials/cpsol2013_talk.pdf>`_ workshop of CP2013.
The source code of choco-solver-|version| is hosted on `GitHub <https://github.com/chocoteam/choco3>`_.

History
=======

The first version of Choco dates from the early 2000s.
A few years later, Choco 2 has encountered a great success in both the academic and the industrial world.
For maintenance issue, Choco has been completely rewritten in 2011, leading to Choco 3.
The first beta version of Choco 3 has been released in 2012.
The latest version is Choco |release|.

Main contributors
=================

+------------------------------------+-----------------------------------------------------------------------------------------------------------+
|**Core developers**                 |  Charles Prud'homme and Jean-Guillaume Fages                                                              |
+------------------------------------+-----------------------------------------------------------------------------------------------------------+
+------------------------------------+-----------------------------------------------------------------------------------------------------------+
|**Main contributors**               |  Xavier Lorca, Narendra Jussien, Fabien Hermenier, Jimmy Liang.                                           |
+------------------------------------+-----------------------------------------------------------------------------------------------------------+
+------------------------------------+-----------------------------------------------------------------------------------------------------------+
|**Previous versions contributors**  |  François Laburthe, Hadrien Cambazard, Guillaume Rochart, Arnaud Malapert,                                |
|                                    |  Sophie Demassey, Nicolas Beldiceanu, Julien Menana, Guillaume Richaud,                                   |
|                                    |  Thierry Petit, Julien Vion, Stéphane Zampelli.                                                           |
+------------------------------------+-----------------------------------------------------------------------------------------------------------+

If you want to contribute, let us know.

Choco is developed with `Intellij IDEA <http://www.jetbrains.com/idea/features/code_analysis.html>`_
and `JProfiler <http://www.ej-technologies.com/products/jprofiler/overview.html>`_, that are kindly provided for free.

How to get Support ?
====================

The company `COSLING <http://www.cosling.com>`_ can provide you with
professional support and specific software development related to Choco Solver.
Feel free to contact them at *contact@cosling.com* to discuss your upcoming projects.

A `forum <http://www.choco-solver.org/?q=Forum>`_ is also available on the website of Choco.
It is dedicated to technical questions about the Choco solver and basic modeling helps.
If you encounter any bug or would like some features to be added, please feel free to
open a discussion on the forum.

How to cite Choco ?
===================

A reference to this manual, or more generally to Choco |version|, is made like this:

.. code-block:: none

    @manual{choco3,
      author        = {Charles Prud'homme and Jean-Guillaume Fages and Xavier Lorca},
      title         = {Choco3 Documentation},
      year          = {2014},
      organization  = {TASC, INRIA Rennes, LINA CNRS UMR 6241, COSLING S.A.S.},
      timestamp     = {Tue, 9 Feb 2016},
      url           = {http://www.choco-solver.org },
    }


******************
Using Choco Solver
******************

Structure of the main package
=============================

You can download `Choco Solver <http://choco-solver.org/Download>`_ on the website of Choco Solver.
You will get a zip file which contains the following files:

choco-solver-|release|-with-dependencies.jar
    A ready-to-use jar file including dependencies;
    it provides tools to declare a Model, the variables, the constraints, the search strategies, etc.
    In a few words, it enables modeling and solving CP problems.

choco-solver-|release|.jar
    A jar file excluding all dependencies and configuration file;
    Enable using choco-solver as a dependency of an application.
    Otherwise, it provides the same code as the jar with dependencies.

choco-solver-|release|-sources.jar
    The source of the core library, to be loaded in your favourite IDE to benefit from the javadoc while coding.

choco-samples-|release|-sources.jar
    The source of the artifact `choco-samples` made of problems modeled with Choco. It is a good start point to see what it is possible to do with Choco.

apidocs-|release|.zip
    Javadoc of Choco-|release|

logback.xml
    The logback configuration file; may be needed when choco-solver is used as a library.

Please, refer to `README.md` for more details.

Adding Choco Solver to your project
===================================

Directly
--------

Simply add choco-solver-|release|-with-dependencies.jar to the classpath of your project (in a terminal or in your favorite IDE).

With Maven
----------

Choco Solver is available on the Maven Central Repository.
To declare Choco as a dependency of your project, simply update the ``pom.xml`` of your project by adding the following instruction:

.. code-block:: xml

   <dependency>
    <groupId>org.choco-solver</groupId>
    <artifactId>choco-solver</artifactId>
    <version>X.Y.Z</version>
   </dependency>

where ``X.Y.Z`` is replaced by |release|.
Note that the artifact does not include any dependencies or `logback.xml`.
Please, refer to `README.md` for the list of required dependencies.

With SBT
--------

To declare Choco as a dependency of your project, simply update the ``build.sbt`` of your project by adding the following instruction:

.. code-block:: sbt

   libraryDependencies ++= Seq(
     "org.choco-solver" % "choco-solver" % "X.Y.Z",
   )

where ``X.Y.Z`` is replaced by |release|.


Compiling sources
=================

As a Maven-based project, Choco Solver can be installed in a few instructions.
Once you have downloaded the source (from the zip file or `GitHub <https://github.com/chocoteam/choco3>`_, simply run the following command:

.. code-block:: bash

  mvn clean install -DskipTests

This instruction downloads the dependencies required for Choco Solver (such as the `trove4j <http://trove.starlight-systems.com/>`_ and `logback <http://logback.qos.ch/>`_) then compiles the sources. The instruction ``-DskipTests`` avoids running the tests after compilation (and saves you a couple of hours). Regression tests are run on a private continuous integration server.

Maven provides commands to generate files needed for an IDE project setup.
For example, to create the project files for your favorite IDE:

IntelliJ Idea
  .. code-block:: bash

   mvn idea:idea

Eclipse
  .. code-block:: bash

   mvn eclipse:eclipse


Example
=======

Simple example showing how to use Choco Solver ::

    import org.chocosolver.solver.Model;
    import org.chocosolver.solver.variables.IntVar;

    /**
     * Trivial example showing how to use Choco Solver
     * to solve the equation system
     * x + y < 5
     * x * y = 4
     * with x in [0,5] and y in {2, 3, 8}
     *
     * @author Charles Prud'homme, Jean-Guillaume Fages
     * @since 9/02/2016
     */
    public class Overview {

        public static void main(String[] args) {
            // 1. Create a Model
            Model model = new Model("my first problem");
            // 2. Create variables
            IntVar x = model.intVar("X", 0, 5);                 // x in [0,5]
            IntVar y = model.intVar("Y", new int[]{2, 3, 8});   // y in {2, 3, 8}
            // 3. Post constraints
            model.arithm(x, "+", y, "<", 5).post(); // x + y < 5
            model.times(x,y,4).post();              // x * y = 4
            // 4. Solve the problem
            model.solve();
            // 5. Print the solution
            System.out.println(x); // Prints X = 2
            System.out.println(y); // Prints Y = 2
        }
    }


**************
Change history
**************

Changes to the library are logged into the `CHANGES.md <https://github.com/chocoteam/choco3/blob/master/CHANGES.md>`_ file.
