---
title: 'Choco-solver: A Java library for constraint programming'
tags:
  - Java
  - constraint programming
  - constraint satisfaction problem
  - constraint optimisation problem
  - decision making 
authors:
  - name: Charles Prud'homme
    orcid: 0000-0002-4546-9027
    affiliation: 1
    corresponding: true
  - name: Jean-Guillaume Fages
    affiliation: 2
affiliations:
  - name: TASC, IMT-Atlantique, LS2N-CNRS, Nantes, France
    index: 1
  - name: COSLING S.A.S., Nantes, France
    index: 2
date: 25 August 2022
bibliography: paper.bib

---

# Summary

Constraint Programming (CP) is a powerful programming paradigm for solving 
combinatorial search problems [@DBLP:reference/fai/2].
CP is at the intersection of artificial intelligence, computer science, operations research, and many other fields.
One of the richness's of the paradigm lies in the wide variety of constraints it proposes. 
Thus, the benefit of CP is twofold: firstly to offer a rich declarative language to describe a combinatorial problem, 
and secondly to provide techniques to automatically solve this problem.

`Choco-solver` is Java library for constraint programming which was created in the early 2000s.
Since then, the library has evolved a great deal, but ease of use has always been a guiding principle in its development.
The `Choco-solver` API is designed to reduce entry points to a minimum and thus simplifies modelling for users.
The wide variety of constraints available allows the user to describe his problem as naturally as possible.
The black-box approach to solving allows everyone to focus on modelling.
However, `Choco-solver` is also open and modifiable.
The implementation of new constraints [@10.1007/978-3-031-08011-1_21] 
or strategies for exploring the search space [@DBLP:conf/cp/LiYL21;@fages:hal-01629182] 
is therefore possible.

As a result, `Choco-solver` is used by the academics for teaching and research, on the other hand it is used by the industry to solve real-world problems.
             
## CP in a nutshell
Constraint programming provides not only a declarative way for users to describe discrete problems,
but also technique for solving them automatically.  
In that sense, it is very close to integer linear programming or Boolean satisfaction
but is distinguished from them with its high-level modeling language and expressiveness.
Actually, one of the richness's of the paradigm lies in the wide variety of constraints 
it proposes, which are also central to the solving stage. 
Thus, the objective of CP is twofold: firstly to offer a rich declarative language to describe a combinatorial problem, 
and secondly to provide technics for solving the problem automatically.
In standard use, a user states a problem using variables, their domains (possible values for each
variable), and constraints which are called predicates that must hold on the variables.
The wide variety of constraints available allows the users to describe their problem as naturally as possible.
Each constraint ensures that it holds, otherwise a propagator filters  
the values that prevent the satisfiability.
It is the combination of the selected constraints that defines the problem to be solved.
The problem is solved by alternating space reduction (usually by a depth-first search) and propagation, 
thus ensuring the completeness of the approach.
This standard usage can be extended in different ways, for example, by hybridisation with local search, 
Boolean satisfiability, or linear programming techniques.

# Statement of need
For constraint programming to be used successfully, it is essential to have a library that incorporates the latest 
advances in the field, while ensuring reliability, performance, and responsiveness.
This was also the motivation for the creation of `Choco-solver`: Providing state-of-the-art algorithms 
and high resolution performance 
while offering ease of use and development, all in a free and open-source library.


## Achievement
With $20$ years of development, `Choco-solver` is now a stable, flexible, extensible, powerful, 
and user-friendly library. 
There is a community of users and contributors who actively participate in improving the library. 
In addition, `Choco-solver` relies on software quality standards (unit and performance tests, continuous integration, 
code review, etc.) and frequent updates are made.
Finally, the choice of Java as programming language makes the integration of the library simple  
into both academic and industrial projects.

# Features and Functionality

## Modeling                                                        

`Choco-solver` comes with the commonly used types of variables: 
Integer variables, 
Boolean variables, set variables [@gervet_1997], graph variables [@dooms_2005;@fages_2015], and real variables. 
Views [@DBLP:conf/cp/SchulteT05;@DBLP:journals/constraints/Justeau-Allaire22] 
but also arithmetical, relational and logical expressions are supported.

Up to 100 constraints are provided from classic ones, such as arithmetical constraints, 
to  must-have global constraints such as *AllDifferent* [@10.5555/199288.178024] or *Cumulative* [@aggoun:hal-00442821],
and include less common even though useful ones such as  *Tree* [@DBLP:conf/cpaior/BeldiceanuFL05] 
or *StableKeySort* [@beldiceanu:hal-01186680].
In many cases, the `Choco-solver` API provides various options in addition to the default signature - 
corresponding to a robust implementation – of a constraint. 
This allows users to experiment alternative approaches and tune the model to its instance. 
The users may also pick some existing propagators to compose a new constraint or 
create their own one in a straightforward way by implementing a filtering algorithm and a satisfaction checker.
Many models are available on the [Choco-solver website](https://choco-solver.org/tutos/) as modelling tutorials.

                           
## Solving

`Choco-solver` has been carefully designed to offer wide range of resolution configurations 
and good solving performances.
Backtrackable primitives and structures are based on trailing [@DBLP:conf/jfplc/AggounB90;@DBLP:conf/cp/ReischukSST09].
The propagation engine deals with seven priority levels [@DBLP:journals/toplas/SchulteS08;@DBLP:journals/constraints/PrudhommeLDJ14] 
and manages either fine or coarse grain events which enables to get efficient incremental constraint propagators.

The search algorithm relies on three components *Propagate*, *Learn*, and *Move* [@Jussien02unifyingsearch].
Such a generic search algorithm is then instantiated to depth-first search,
large neighbourhood search [@DBLP:conf/cp/Shaw98;@DBLP:journals/constraints/PrudhommeLJ14], 
limited discrepancy search [@Harvey:1995:LDS:1625855.1625935], 
depth-bounded discrepancy search [@Walsh97depth-boundeddiscrepancy] or 
hybrid breadth-first search [@allouche:hal-01198361].


The search process can also be greatly improved by various built-in search strategies such as 
*dom/wdeg* [@DBLP:conf/ecai/BoussemartHLS04] and its *ca-cd* variant [@DBLP:conf/ictai/WattezLPT19], 
*activity-based search* [@10.1007/978-3-642-29828-8_15], *failure-based searches* [@DBLP:conf/cp/LiYL21], 
*bound-impact value selector* [@fages:hal-01629182], *first-fail* [@Haralick:1979:ITS:1624861.1624942], and many others. 
Standard restart policies are also available, to take full advantage of the learning strategies.
Problem-adapted search strategies are also supported.

One can solve a problem by  
- simply checking satisfaction
- finding one or all solutions 
- optimizing one or more objectives
- solving on one or more thread.

The search process itself is observable and extensible.

## Community tools integration

Several useful extra features are also available such as parsers to [XCSP3 format](http://xcsp.org/) 
and [MiniZinc format](https://www.minizinc.org/resources.html). 
`Choco-solver` is also embedded in [PyCSP3](https://pypi.org/project/pycsp3/), 
a Python library for modeling and solving combinatorial constrained problems.
In addition to offering alternatives to modelling in Java, 
it also allows participation in the two major constraint solver competitions :
[MiniZinc Challenge](https://www.minizinc.org/challenge.html) and [XCSP3 Competition](http://www.xcsp.org/competitions/).

Finally, although it is originally designed to solve discrete mathematical problems, 
`Choco-solver` also supports natively real variables and constraints, and relies on [Ibex-lib](http://www.ibex-lib.org/) 
 to solve the continuous part of the problems [@fages:hal-00904069].
Equally, a Boolean satisfaction solver (based on [MiniSat](http://minisat.se/Main.html)) is integrated 
to offer better performance on logical constraints.

These aspects consolidate the place of `Choco-solver` as an important tool in the CP community.                                                                                                                         
    

Note that there are a couple of other Java constraint solvers of equivalent maturity, 
like [JaCoP](https://github.com/radsz/jacop) and [ACE](https://github.com/xcsp3team/ace).
Although the performances of these tools are direclty comparable, they are mainly distinguished by the functionalities in terms of modelling and resolution. 
Among the most noteworthy, `Choco-solver` allows integrating constraints which are based on graph variables 
or real variables, or it can parse both MiniZinc and XCSP3 input files.

# Industrial use

## Applications

`Choco-solver` is used by the industry to solve many real-world problems, 
such as cryptanalysis [@10.1007/978-3-030-78375-4_8], 
construction planning [@CANIZARES2022116149], 
automated testing and debugging [@LE2021100085], 
scheduling [@10.1007/978-3-319-44953-1_40], 
level design [@5887401], 
placement service [@8814186] and many others.
In the Railway industry, `Choco-solver` is used to optimize the rail traffic of French train stations, on a daily basis. 
It is also used at a higher level to run simulations for capacity and maintenance planning. 
The underlying mathematical problems, involving multi-objective functions with millions of variables and constraints, 
are solved within seconds by the solver. 
In the defense sector, `Choco-solver` is used for various applications. 
One publicly known is the long-term maintenance planning of the Mirage 2000 fleet [@OptaForce]. 
This planning and scheduling problem includes various capacity constraints, load balancing, and mission covering in an over-constrained environment. 
A fleet of hundred aircraft can be planned by `Choco-solver` for the next fifteen years within a few minutes.
Another type of industrial application of `Choco-solver` is Configuration [@Charpentier2021COSLINGC] 
where the solver is used to solve dynamical constraint models. 
The underlying mathematical problems are generally simpler than the ones in planning applications
and an optimal result is expected within milliseconds. 
This occurs in quotation systems for sales automation, but also design automation, and system configuration.

In most of those cases, experts set advanced solution techniques, such as specific search strategies, 
Large Neighborhood Search or *ad hoc* global constraints, in order to improve their model. 
`Choco-solver` is flexible enough to allow such fine-tuning to tackle challenging problems. 
With the right approach, `Choco-solver` can come up with nearly optimal solutions in a very short time.


# Acknowledgements

We acknowledge contributions from (in alphabetical order) 
Hadrien Cambazard, Arthur Godet, Fabien Hermenier, Narendra Jussien, 
Dimitri Justeau-Allaire, Tanguy Lapègue, Alexandre Lebrun, 
Jimmy Liang, Xavier Lorca, Arnaud Malapert, 
Guillaume Rochart, João Pedro Schmitt and Mohamed Wahbi.  

# References