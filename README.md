choco-parsers
=============

choco-parsers is an extension library for [choco-solver](https://github.com/chocoteam/choco-solver).
It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for [MiniZinc](http://www.minizinc.org/).

Instructions
------------
The `src/choco-fzn/mzn-lib` directory lists the supported global constraints for Choco3 (See MiniZinc documentation for more details on how to convert mzn files into fzn files).


There are two ways to parse and solve a fzn file with Choco3:

* ###### Java front-end

  ```java -cp .:/path/to/choco-parsers-4.0.0-with-dependencies.jar ChocoFZN [<options>] [<file>]```
  
  
* ###### In a terminal (shell for Linux based OS)
  
  ```sh ./src/chocofzn/fzn_exec -jar /path/to/choco-parsers-4.0.0-with-dependencies.jar [<options>] [<file>]```  

Common options are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* other Choco-specific options are available
