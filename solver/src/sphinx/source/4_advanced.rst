#############
Miscellaneous
#############

********
Settings
********

A ``Settings`` object is attached to each ``Model``.
It declares default behavior for various purposes: from general purpose (such as the welcome message), modelling purpose (such as enabling views) or solving purpose (such as the search binder).

Default settings can be accessed through ``DefaultSettings``.
This class can be extended to provide more settings and set to modified the default values.

Settings are declared in a ``Model`` constructor.
Settings are not immutable but modifying value after ``Model`` construction can lead to unexpected behavior.


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