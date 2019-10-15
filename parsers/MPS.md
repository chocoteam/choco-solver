MPS parser
============

This is related to [MPS format](http://miplib.zib.de/).
Only file that matches [the description for MIPLib](http://miplib.zib.de/miplib3/mps_format.txt) is supported.

### Parsing and solving a MPS file

MPS file can be parsed and solve with Choco using the following command:

  ```java -cp .:/path/to/choco-parsers-4.0.4-with-dependencies.jar org.chocosolver.parser.mps.ChocoMPS [<options>] [<file>]```

Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* ```-max``` : Set the function to be maximized instead of minimized (default policy).
* ```-ninf``` : Define the negative infinity for unbounded variables.
* ```-pinf``` : Define the positive infinity for unbounded variables.
* ```-ibex``` : All non full integer equations are handled by Ibex. 
Note that Ibex should be installed first, and VM options adapted.
* ```-ninf``` : Override negative infinity (default: -21474836). 
* ```-pinf``` : Override positive infinity (default: 21474836). 
* ```-noeq``` : Split EQ constraints into a LQ and a GQ constraint. 
* ```-split``` : Split any contraints of cardinality greater than this value (default: 100). 
* other Choco-specific options may be available.
* ```-h```: When invoked, this options will print all options available.

