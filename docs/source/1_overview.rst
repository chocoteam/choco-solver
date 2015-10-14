#######
IGNORED
#######

#############
Preliminaries
#############


*************
Main concepts
*************


What is Constraint Programming?
===============================


Such a paradigm takes its features from various domains (Operational Research, Artificial Intelligence, etc).
Constraint programming is now part of the portfolio of global solutions for processing real combinatorial problems.
Actually, this technique provides tools to deal with a wide range of combinatorial problems.
These tools are designed to allow non-specialists to address strategic as well as operational problems,
which include problems in planning, scheduling, logistics, financial analysis or bio-informatics.
Constraint programming differs from other methods of Operational Research by how it is implemented.
Usually, the algorithms must be adapted to the specifications of the problem addressed.
This is not the case in Constraint Programming where the problem addressed is described using the tools available in the library.
The exercise consists in choosing carefully what constraints combine to properly express the problem,
while taking advantage of the benefits they offer in terms of efficiency.

`[wikipedia] <http://en.wikipedia.org/wiki/Constraint_programming>`_

What is Choco ?
===============

Choco is a Free and Open-Source Software dedicated to Constraint Programming.
It is written in Java, under `BSD <http://opensource.org/licenses/BSD-3-Clause>`_ license.
It aims at describing real combinatorial problems in the form of Constraint Satisfaction Problems and
solving them with Constraint Programming techniques.

Choco is used for:

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

Choco |version| includes:

- various type of variables (integer, boolean, set and real),
- various state-of-the-art constraints (alldifferent, count, nvalues, etc.),
- various search strategies, from basic ones to most complex (impact-based and activity-based search),
- explanation-based engine, that enables conflict-based back jumping, dynamic backtracking and path repair,

But also facilities to interact with the search loop, factories to help modeling, many samples, an interface to Ibex, etc.
The source code of choco-solver-|version| is hosted on `GitHub <https://github.com/chocoteam/choco3>`_.

Choco also has many `extensions <http://choco-solver.org/?q=extensions>`_,
including a FlatZinc parser to solve minizinc instances
and a graph variable module to better solve graph problems such as the TSP.

An overview of the features of Choco |version| may also be found in the presentation made in the
`"CP Solvers: Modeling, Applications, Integration, and Standardization" <http://www.choco-solver.org/sites/materials/cpsol2013_talk.pdf>`_ workshop of CP2013.

History
=======

The first version of Choco dates from the early 2000s.
A few years later, Choco 2 has encountered a great success in both the academic and the industrial world.
For maintenance issue, Choco has been completely rewritten in 2011, leading to Choco 3.
The first beta version of Choco 3 has been released in 2012.
The latest version is Choco |release|.


How to get support ?
====================

A `forum <http://www.choco-solver.org/?q=Forum>`_ is available on the website of Choco.
It is dedicated to technical questions about the Choco solver and basic modeling helps.
If you encounter any bug or would like some features to be added, please feel free to
open a discussion on the forum.
You can also use the following support mailing list: choco3-support@mines-nantes.fr.
As can be seen on the Choco website, most support requests are answered very fast.
However, this free service is provided with no guarantee.

If you want an expert to build a CP model for your application
or if you need professional support, please contact `COSLING <http://www.cosling.com>`_.


How to cite Choco ?
===================

A reference to this manual, or more generally to Choco |version|, is made like this:

.. code-block:: none

    @manual{choco3,
      author        = {Charles Prud'homme and Jean-Guillaume Fages and Xavier Lorca},
      title         = {Choco3 Documentation},
      year          = {2014},
      organization  = {TASC, INRIA Rennes, LINA CNRS UMR 6241, COSLING S.A.S.},
      timestamp     = {Thu, 11 May 2015},
      url           = {http://www.choco-solver.org },
    }

Who contributes to Choco ?
==========================

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


***************
Getting started
***************

Installing Choco |version|
==========================

Choco |version| is a java library based on `Java 8 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_.
The main library is named ``choco-solver`` and can be seen as the core library.
Some extensions are also provided, such as ``choco-parsers`` or ``choco-cpviz``, and rely on but do not include ``choco-solver``.

Which jar to select ?
---------------------

We provide a zip file which contains the following files:

choco-solver-|release|-with-dependencies.jar
    An ready-to-use jar file including dependencies;
    it provides tools to declare a Solver, the variables, the constraints, the search strategies, etc.
    In a few words, it enables modeling and solving CP problems.

choco-solver-|release|.jar
    A jar file excluding all dependencies and configuration file;
    Enable using choco-solver as a dependency of an application.
    Otherwise, it provides the same code as the jar with dependencies.

choco-solver-|release|-sources.jar
    The source of the core library.

choco-samples-|release|-sources.jar
    The source of the artifact `choco-samples` made of problems modeled with Choco. It is a good start point to see what it is possible to do with Choco.

apidocs-|release|.zip
    Javadoc of Choco-|release|

logback.xml
    The logback configuration file; may be needed when choco-solver is used as a library.

Please, refer to `README.md` for more details.

.. note::

    Java 7 compliant jars are also available, post-fixed with 'jk7'.

Extensions
^^^^^^^^^^

There are also official extensions, thus maintained by the Choco team. They are provided apart from the zip file.
The available extensions are: :ref:`61_ext_pars`, :ref:`61_ext_gui`, :ref:`61_ext_cpviz`, :ref:`61_ext_graph`, :ref:`61_ext_geost`, :ref:`61_ext_exppar`.
.., :ref:`61_ext_eps`.

.. note::
    Each of those extensions include all dependencies but choco-solver classes, which ease their usage.


To start using Choco |version|, you need to be make sure that the right version of java is installed.
Then you can simply add the ``choco-solver`` jar file (and extension libraries) to your classpath or declare them as dependency of a Maven-based project.

Update the classpath
--------------------

Simply add the jar file to the classpath of your project (in a terminal or in your favorite IDE).

As a Maven Dependency
---------------------

Choco is build and managed using `Maven3 <http://maven.apache.org/download.cgi>`_.
Choco is available on Maven Central Repository, to declare Choco as a dependency of your project, simply update the ``pom.xml`` of your project by adding the following instruction:

.. code-block:: xml

   <dependency>
    <groupId>org.choco-solver</groupId>
    <artifactId>choco-solver</artifactId>
    <version>X.Y.Z</version>
   </dependency>

where ``X.Y.Z`` is replaced by |release|.
Note that the artifact does not include any dependencies or `logback.xml`.
Please, refer to `README.md` for the list of required dependencies.

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


.. _12_overview_label:

Overview of Choco |version|
===========================

The following steps should be enough to start using Choco |version|.
The minimal problem should at least contains a solver, some variables and constraints to linked them together.

To facilitate the modeling, Choco |version| provides factories for almost every required component of CSP and its resolution:


+------------------------------+--------------+-------------------------------------------+
| **Factory**                  | **Shortcut** |  **Enables to create**                    |
+==============================+==============+===========================================+
| ``VariableFactory``          | VF           | Variables and views                       |
|                              |              | (integer, boolean, set and real)          |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``IntConstraintFactory``     | ICF          | Constraints over integer variables        |
+------------------------------+--------------+-------------------------------------------+
| ``SetConstraintFactory``     | SCF          | Constraints over set variables            |
+------------------------------+--------------+-------------------------------------------+
| ``LogicalConstraintFactory`` | LCF          | (Manages constraint reification)          |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``IntStrategyFactory``       | ISF          | Custom or black-box search strategies     |
+------------------------------+--------------+-------------------------------------------+
| ``SetStrategyFactory``       | SSF          |                                           |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``Chatterbox``               |              | Output messages and statistics.           |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``SearchMonitorFactory``     | SMF          | resolution limits, restarts etc.          |
+------------------------------+--------------+-------------------------------------------+

Note that, in order to have a concise and readable model, factories have shortcut names. Furthermore, they can be imported in a static way:

.. code-block:: java

   import static org.chocosolver.solver.search.strategy.ISF.*;

Let say we want to model and solve the following equation: :math:`x + y < 5`, where the :math:`x \in [\![0,5]\!]` and :math:`y \in [\![0,5]\!]`.
Here is a short example which illustrates the main steps of a CSP modeling and resolution with Choco |version| to treat this equation.



.. literalinclude:: /../../choco-samples/src/test/java/org/chocosolver/docs/Overview.java
      :language: java
      :lines: 48-60
      :linenos:




One may notice that there is no distinction between model objects and solver objects. This makes easier for beginners to model and solve problems (reduction of concepts and terms to know) and for developers to implement their own constraints and strategies (short cutting process).

Don't be afraid to take a look at the sources, we think it is a good start point.

Choco |version| quick documentation
===================================

Solver
------

The ``Solver`` is a central object and must be created first: ``Solver solver = new Solver();``.

:ref:`[Solver] <21_solver_label>`

Variables
---------

The ``VariableFactory`` (``VF`` for short) eases the creation of variables.
Available variables are: ``BoolVar``, ``IntVar``, ``SetVar``, ``GraphVar`` and ``RealVar``.
Note, that an ``IntVar`` domain can be bounded (only bounds are stored) or enumerated (all values are stored); a boolean variable is a 0-1 IntVar.

:ref:`[Variables] <22_variables_label>`

Views
-----

A view is a variable whose domain is defined by a function over another variable domain.
Available views are: ``not``, ``offset``, ``eq``, ``minus``, ``scale`` and ``real``.

:ref:`[Views] <22_variables_view_label>`

Constants
---------

Fixed-value integer variables should be created with the specific ``VF.fixed(int, Solver)`` function.

:ref:`[Constants] <22_variables_constant_label>`

Constraints
-----------

Several constraint factories ease the creation of constraints: ``LogicalConstraintFactory`` (``LCF``),
``IntConstraintFactory`` (``ICF``) and ``SetConstraintsFactory`` (``SCF``).

``RealConstraint`` is created with a call to new and to ``addFunction`` method. It requires the `Ibex <http://www.ibex-lib.org/>`_ solver.

**Constraints hold once posted**: ``solver.post(c);``

**Reified constraints should not be posted**.

:ref:`[Constraints] <23_constraints_label>`

Search
------

Defining a specific way to traverse the search space is made thanks to: ``solver.set(AbstractStrategy)``.
Predefined strategies are available in ``IntStrategyFactory`` (``ISF``), ``SetStrategyFactory`` and ``RealStrategyFactory``.

Large Neighborhood Search (LNS)
-------------------------------

Various LNS (random, propagation-guided, etc.) can be created from the LNSFactory to improve performance on optimization problems.

Monitors
--------

An ``ISearchMonitor`` is a callback which enables to react on some resolution events (failure, branching, restarts, solutions, etc.).
``SearchMonitorFactory`` (``SMF``) lists most useful monitors.
User-defined monitors can be added with ``solver.plugMonitor(...)``.

Limits
------

A limit may be imposed on the search. The search stops once a limit is reached. Available limits are ``SMF.limitTime(solver, 5000)``, ``SMF.limitFail(solver, 100)``, etc.

Restarts
--------

Restart policies may also be applied ``SMF.geometrical(...)`` and ``SMF.luby(...)`` are available.

Logging
-------

Logging the search is possible.
There are variants but the main way to do it is made through the ``Chatterbox.printStatistics(solver)``.
It prints the main statistics of the search (time, nodes, fails, etc.)


Solving
-------

Finding if a problem has a solution is made through a call to: ``solver.findSolution()``.
Looking for the next solution is made thanks to ``nextSolution()``.
``findAllSolutions()`` enables to enumerate all solutions of a problem.
To optimize an objective function, call ``findOptimalSolution(...)``.
Resolutions perform a Depth First Search.

Solutions
---------

By default, the last solution is restored at the end of the search.
Solutions can be accessed as they are discovered by using an ``IMonitorSolution``.

Explanations
------------

Choco natively supports explained constraints to reduce the search space and to give feedback to the user.
Explanations are disabled by default.


Choco |version| : changes
=========================

3.3.2
-----

- Addition:
    - :ref:`51_icstr_nvpc`
    - :ref:`51_icstr_ksor`
    - :ref:`55_smf`

-  Major modification:
    - :ref:`44_multithreading_label`

3.3.1
-----

- Addition:
    - :ref:`55_smf`
    - :ref:`46_define_constraint_label`

- Major modification:
    - :ref:`43_explanations_label`
    - :ref:`44_monitors_label`

3.3.0
-----

- Addition:
    - :ref:`512_constraint_things_to_know`
    - :ref:`512_automaton`
    - :ref:`542_complex_clauses`
    - :ref:`41_settings_label`
    - :ref:`31_searchbinder`
    - :ref:`34_chatternbox_label`

- New constraints:
    - :ref:`51_icstr_mdd`
    - :ref:`51_scstr_nme`

-  Major modification:
    - :ref:`44_multithreading_label`
    - :ref:`45_define_search_label`
