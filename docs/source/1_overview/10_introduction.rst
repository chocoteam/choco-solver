What is Choco ?
===============

Choco is a Free and Open-Source Software [#f1]_ dedicated to Constraint Programming.
It aims at describing real combinatorial problems in the form of Constraint Satisfaction Problems and to solve them with Constraint Programming techniques.

Choco can be used for:

- teaching (a user-oriented constraint solver with open-source code)
- research (state-of-the-art algorithms and techniques, user-defined constraints, domains and variables)
- real-life applications (many application now embed CHOCO)

Choco is easy to manipulate, that’s why it is widely used for teaching. And Choco is also efficient, and we are proud to count industrial users too.

Choco is developed with `Intellij IDEA <http://www.jetbrains.com/idea/features/code_analysis.html>`_ and `JProfiler <http://www.ej-technologies.com/products/jprofiler/overview.html>`_.

.. [#f1] Choco is distributed under `BSD <http://opensource.org/licenses/BSD-3-Clause>`_ license (Copyright(c) 1999-2014, Ecole des Mines de Nantes).


Choco is one of the few Java libraries for constraint programming.
The first version dates from the early 2000s, Choco is one of the forerunners among the free solvers - Choco written under `BSD <http://opensource.org/licenses/BSD-3-Clause>`_ license.
Maintenance and development tools are provided by the members of INRIA TASC team,
especially by Charles Prud'homme and Jean-Guillaume Fages [#f2]_.
The latest version is Choco |version|.

Choco |version| is not the continuation of Choco2, but a completely rewritten version and there is no backward compatibility.
The current release, choco-solver-|release|, is hosted on `GitHub <https://github.com/chocoteam/choco3>`_.
Choco |version| comes with:

- various type of variables (integer, boolean, set, graph and real),
- various state-of-the-art constraints (alldifferent, count, nvalues, etc.),
- various search strategies, from basic ones (first_fail, smallest, etc.) to most complex (impact-based and activity-based search),
- explanation-based engine, that enables conflict-based back jumping, dynamic backtracking and path repair,

But also, a FlatZinc parser, facilities to interact with the search loop, factories to help modeling, many samples, Choco-Ibex interface, etc.

An overview of the features of Choco |version| can be found in the presentation made in the `"CP Solvers: Modeling, Applications, Integration, and Standardization" <http://www.choco-solver.org/sites/materials/cpsol2013_talk.pdf>`_ workshop of CP2013.

A `forum <http://www.choco-solver.org/?q=Forum>`_ is available on the website of Choco.
A support mailing list is also available: choco3-support@mines-nantes.fr.

.. [#f2] A complete list of contributors can be found on the website of Choco, team page.


By the way, what is Constraint Programming?
===========================================


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

