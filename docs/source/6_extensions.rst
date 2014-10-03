###################
Extensions of Choco
###################

.. Choco |version| has many extensions which can be found on `GitHub <https://github.com/chocoteam>`_.

*************
IO extensions
*************

.. _61_ext_pars:

choco-parsers
=============

choco-parsers is an extension of Choco |version|. It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for MiniZinc.
This module follows the flatzinc standards that are used for the annual MiniZinc challenge. It only supports integer variables.
You will find it at https://github.com/chocoteam/choco-parsers

.. _61_ext_gui:

choco-gui
=========

choco-gui is an extension of Choco |version|.
It provides a Graphical User Interface with various views which can be simply plugged on any Choco Solver object.
You will find it at https://github.com/chocoteam/choco-gui

.. _61_ext_cpviz:

choco-cpviz
===========

choco-cpviz is an extension of Choco |version| to deal with cpviz library.
You will find it at https://github.com/chocoteam/choco-cpviz

*******************
Modeling extensions
*******************

.. _61_ext_graph:

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

.. _61_ext_geost:

choco-geost
===========

choco-geost is a Choco |version| module which provides the GEOST global constraint.
This constraint is designed for geometrical and packing applications (see http://www.emn.fr/z-info/sdemasse/gccat/Cgeost.html).
You will find it at https://github.com/chocoteam/choco-geost

.. _61_ext_exppar:

choco-exppar
============

choco-exppar is a Choco |version| module which provides an expression parser. This enables to simplify the modeling step.
You will find it at https://github.com/chocoteam/choco-exppar

.. _61_ext_eps:

choco-eps
=========

Embarrassingly Parallel Search for Choco |version| enables to speed up search on multi-core systems.
This extension is currently under development. You will find it at https://github.com/chocoteam/choco-eps

