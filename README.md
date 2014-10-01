choco-parsers
=============

choco-parsers is an extension library for [Choco3](https://github.com/chocoteam/choco3).
It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for [MiniZinc](http://www.minizinc.org/).

Instructions
------------
The `src/choco-fzn/mzn-lib` directory lists the supported global constraints for Choco3 (See MiniZinc documentation for more details on how to convert mzn files into fzn files).


There are two ways to parse and solve a fzn file with Choco3:

* ###### Java front-end

  ```java -cp .:choco-solver-X.Y.Z.jar:choco-parsers-X.Y.Z.jar parser.flatzinc.ChocoFZN [<options>] [<file>]```
  
  
* ###### In a terminal (shell for Linux based OS)
  
  ```fzn_choco.sh [<options>] [<file>]```  

Common options are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* other Choco-specific options are available

The shell requires another option:
* ```-jar <j>```: Set the classpath, should be something like: ```choco-solver-X.Y.Z.jar:choco-parsers-X.Y.Z.jar```
