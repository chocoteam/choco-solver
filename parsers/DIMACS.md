DIMACS parser
=============

This is related to [DIMACS CNF format](http://www.satcompetition.org/2009/format-benchmarks2009.html).
Only file that matches such format is supported.

### Parsing and solving a DIMACS CNF file

DIMACS CNF file can be parsed and solve with Choco using the following command:

  ```java -cp .:/path/to/choco-parsers-4.X.Y-with-dependencies.jar org.chocosolver.parser.mps.ChocoDIMACS [<options>] [<file>]```

Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. 
  When this option is not given the solver should search for and output only the first solution.
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* ```-cp```: When invoked, this option will turn clauses into sum constraints.
* ```-h```: When invoked, this options will print all options available.

