XCSP3 parser
============

This is related to [XCSP3 format](http://xcsp.org).

### Parsing and solving a XCSP3 file

Then, there are two ways to parse and solve a XCSP3 file with Choco:

* ##### Java front-end

  ```java -cp .:/path/to/choco-parsers-4.0.5-with-dependencies.jar org.chocosolver.parser.xcsp.ChocoXCSP [<options>] [<file>]```

Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* ```-cs``` : Check solutions with `org.xcsp.checker.SolutionChecker`
* other Choco-specific options are available

  
* ##### In a terminal (shell for Linux based OS)
  
  ```sh ./src/chocoxcsp/xcsp3_exec -jar /path/to/choco-parsers-4.0.5-with-dependencies.jar [<options>] [<file>]```  

Calling the shell with `-h` option will print the help message.

To avoid declaring the `-jar` option, you can export it once before calling the shell:

   `PARSER_JAR=/path/to/choco-parsers-4.0.5-with-dependencies.jar`
   
   `sh ./src/chocoxcsp/xcsp3_exec [<options>] [<file>]`


