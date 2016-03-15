#############
Miscellaneous
#############

********
Settings
********

A ``Settings`` object is attached to each ``Solver``.
It declares default behavior for various purposes: from general purpose (such as the welcome message), modelling purpose (such as enabling views) or solving purpose (such as the search binder).

The API is:

``String getWelcomeMessage()``
    Return the welcome message.

``Idem getIdempotencyStrategy()``
    Define how to react when a propagator is not ensured to be idempotent.

``boolean enableViews()``
    Set to 'true' to allow the creation of views in the ``VariableFactory``. Creates new variables with channeling constraints otherwise.

``int getMaxDomSizeForEnumerated()``
    Define the maximum domain size threshold to force integer variable to be enumerated instead of bounded while calling ``VariableFactory#integer(String, int, int, Solver)``.

``boolean enableTableSubstitution()``
    Set to true to replace intension constraints by extension constraints.

``int getMaxTupleSizeForSubstitution()``
    Define the maximum domain size threshold to replace intension constraints by extension constraints. Only checked when ``enableTableSubstitution()`` is set to true.

``boolean plugExplanationIn()``
    Set to true to plug explanation engine in.

``boolean enablePropagatorInExplanation()``
    Set to true to add propagators in explanations

``double getMCRPrecision()``
    Define the rounding precision for :ref:`51_icstr_mcreg`. MUST BE < 13 as java messes up the precisions starting from 10E-12 (34.0*0.05 == 1.70000000000005).

``double getMCRDecimalPrecision()``
    Defines the smallest used double for :ref:`51_icstr_mcreg`.

``short[] getFineEventPriority()``
    Defines, for fine events, for each priority, the queue in which a propagator of such a priority should be scheduled in.

``short[] getCoarseEventPriority()``
    Defines, for coarse events, for each priority, the queue in which a propagator of such a priority should be scheduled in

``ISearchBinder getSearchBinder()``
    Return the default :ref:`31_searchbinder`.

``ICondition getEnvironmentHistorySimulationCondition()``
    Return the condition to satisfy when rebuilding history of backtrackable objects is needed.

``boolean warnUser()``
    Return true if one wants to be informed of warnings detected during modeling/solving (default value is false).

``boolean enableIncrementalityOnBoolSum(int nbvars)``
    Return true if the incrementality is enabled on boolean sum, based on the number of variables involved.
    Default condition is : nbvars > 10.

``boolean outputWithANSIColors()``
    If your terminal support ANSI colors (Windows terminals don't), you can set this to true and decisions and solutions
    will be output with colors.

``boolean debugPropagation()``
    When this setting returns true, a complete trace of the events is output.
    This can be quite big, though, and it slows down the overall process.

``boolean cloneVariableArrayInPropagator()``
   If this setting is set to true (default value), a clone of the input variable array is made in any propagator constructors.
   This prevents, for instance, wrong behavior when permutations occurred on the input array (e.g., sorting variables).
   Setting this to false may limit the memory consumption during modelling.

***********************
Multi-thread resolution
***********************

Choco |version| provides a simple way to use several threads to treat a problem.
The main idea of that driver is to solve the *same* model with different search strategies and to share few information
to make these threads help each others.

To use a portfolio of solvers in parallel, use ``ParallelPortfolio`` as follows: ::

        ParallelPortfolio portfolio = new ParallelPortfolio();
        int nbModels = 5;
        for(int s=0;s<nbModels;s++){
            portfolio.addModel(makeModel(s));
        }
        portfolio.solve();

In this example, ``makeModel(int)`` is a method you have to implement to create a ``Model`` of the problem.
The parameter ``s`` enables to change the search strategy (e.g. with a ``switch``).

``portfolio.solve()`` enables to solve the problem.

When dealing with multithreading resolution, very few data is shared between threads:
everytime a solution has been found its value is shared among solvers. Moreover,
when a solver ends, it communicates an interruption instruction to the others.
This enables to explore the search space in various way, using different model settings such as search strategies
(this should be done in the dedicated method which builds the model, though).

.. _48_plm:

*******************
Recording solutions
*******************

Choco |version| requires each decision variable (those that are declared in the search strategy) to be instantiated in every solution.
Otherwise, an exception will be thrown.
Non-decision variables may be uninstantiated in a solution. However, if WARN logging is enable, a trace is shown to inform the user.

Choco |version| includes several ways to record solutions, the recommended way is to plug a `ISolutionMonitor` in.
See :ref:`44_monitors_label` for more details.

Solution storage
================

A solution is usually stored through a ``Solution`` object which maps every variable with its current value.
Such an object can be erased to store new solutions.

Solution recording
==================

Built-in solution recorders
---------------------------

A solution recorder (``ISolutionRecorder``) is an object in charge of recording variable values in solutions.
There exists several built-in solution recorders:

``LastSolutionRecorder`` only keeps variable values of the last solution found. It is the default solution recorder.
Furthermore, it is possible to restore that solution after the search process ends.

``BestSolutionsRecorder`` records all solutions that are equally good and removes from the solution set those that are worse than the best solution value found so far.
This may be used to enumerate all optimal (or at least, best) solutions of a problem.

``ParetoSolutionsRecorder`` records all solutions of the pareto front of the multi-objective problem.

Custom recorder
---------------

You can build you own way of manipulating and recording solutions by either implementing your own ``ISolutionRecorder`` object
or by simply using an ``ISolutionMonitor``, as follows:

.. literalinclude:: /../../choco-samples/src/main/java/samples/org/chocosolver/integer/SMPTSP.java
   :language: java
   :lines: 118-124
   :linenos:

Solution restoration
====================

A ``Solution`` object can be restored, i.e. variables are fixed back to their values in that solution.
This is achieved through the `Solver` by the calling one of the two following methods: ::

    model.getSolutionRecorder().restoreLastSolution();
    // or
    Solution aSolution= ...;
    model.getSolutionRecorder().restoreSolution(aSolution);

.. note::

    The restoration might detect inconsistencies, for instance when the model has been externally modified since the solution to be restored to has been found.

*******************
Extensions of Choco
*******************

choco-parsers
=============

choco-parsers is an extension of Choco |version|. It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for MiniZinc.
This module follows the flatzinc standards that are used for the annual MiniZinc challenge. It only supports integer variables.
You will find it at https://github.com/chocoteam/choco-parsers

choco-graph
===========

choco-graph is a Choco |version| module which allows to search for a graph, which may be subject to graph constraints.
The domain of a graph variable G is a graph interval in the form [G_lb,G_ub].
G_lb is the graph representing vertices and edges which must belong to any single solution whereas G_ub is the graph representing vertices and edges which may belong to one solution.
Therefore, any value G_v must satisfy the graph inclusion "G_lb subgraph of G_v subgraph of  G_ub".
One may see a strong connection with set variables.
A graph variable can be subject to graph constraints to ensure global graph properties (e.g. connectedness, acyclicity) and channeling constraints to link the graph variable with some other binary, integer or set variables.
The solving process consists of removing nodes and edges from G_ub and adding some others to G_lb until having G_lb = G_ub, i.e. until G gets instantiated.
These operations stem from both constraint propagation and search. The benefits of graph variables stem from modeling convenience and performance.

This extension has documentation. You will find it at https://github.com/chocoteam/choco-graph

choco-gui
=========

choco-gui is an extension of Choco |version|.
It provides a Graphical User Interface with various views which can be simply plugged on any Choco Model object.
You will find it at https://github.com/chocoteam/choco-gui

***********
Ibex Solver
***********

To manage continuous constraints with Choco, an interface with Ibex has been done.
It needs Ibex to be installed on your system.

    "IBEX is a C++ library for constraint processing over real numbers.

    It provides reliable algorithms for handling non-linear constraints.
    In particular, round off errors are also taken into account.
    It is based on interval arithmetic and affine arithmetic."
    -- http://www.ibex-lib.org/

Installing Ibex
===============

See the `installation instructions <http://www.ibex-lib.org/doc/install.html>`_ of Ibex to complied Ibex on your system.
More specially, take a look at `Installation as a dynamic library <http://www.ibex-lib.org/doc/install.html#installation-as-a-dynamic-library>`_
Do not forget to add the ``--with-java-package=org.chocosolver.solver.constraints.real`` configuration option.

Using Ibex
==========

Once the installation is completed, the JVM needs to know where Ibex is installed to fully benefit from the Choco-Ibex bridge and declare real variables and constraints.
This can be done either with an environment variable of by adding ``-Djava.library.path=path/to/ibex/lib`` to the JVM arguments.
The path `/path/to/ibex/lib` points to the `lib` directory of the Ibex installation directory.