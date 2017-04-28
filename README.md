choco-parsers
=============

`choco-parsers` is an extension library for [choco-solver](https://github.com/chocoteam/choco-solver).
It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for [MiniZinc](http://www.minizinc.org/) 
and a parser for [XCSP3](http://xcsp.org), an intermediate integrated XML-based format.

* [Download](https://github.com/chocoteam/choco-parsers/releases/latest)
* [MiniZinc](#mzn)
* [XCSP3](#xcsp)


<a name="mzn"></a>
## MiniZinc

This is related to [MiniZinc and FlatZinc format](http://www.minizinc.org).

First, you need to turn the MiniZinc file (and its datafile, if any) into a choco-readable flatzinc file.

### Converting from MiniZinc to FlatZinc

This step is required.

The `src/chocofzn/globals` directory lists the supported global constraints for Choco.


##### CLI
    
```mzn2fzn -I "/path/to/choco-parsers/src/chocofzn/globals" model.mzn -d data.dzn -o output.fzn```

where `model.mzn` is the MiniZinc file describing the model, 
`data.dzn` (optional) contains data to instantiate the MiniZinc file and
`output.fzn` is the output FlatZinc filename you want.

The `-I` argument defines global constraints provided by Choco, it is optional but strongly encouraged.

See MiniZinc documentation for more details on how to convert MiniZinc files into FlatZinc files.

##### Online tool

Alternatively, you can use [this online tool](http://chocozn.cosling.com).

### Parsing and solving a FlatZinc file

Then, there are two ways to parse and solve a FlatZinc file with Choco:

* ##### Java front-end

  ```java -cp .:/path/to/choco-parsers-4.0.2-with-dependencies.jar org.chocosolver.parser.flatzinc.ChocoFZN [<options>] [<file>]```
  
Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* other Choco-specific options are available
  
* ##### In a terminal (shell for Linux based OS)
  
  ```sh ./src/chocofzn/fzn-exec -jar /path/to/choco-parsers-4.0.2-with-dependencies.jar [<options>] [<file>]```  

Calling the shell with `-h` option will print the help message.

To avoid declaring the `-jar` option, you can export it once before calling the shell:

   `PARSER_JAR=/path/to/choco-parsers-4.0.2-with-dependencies.jar`
   
   `sh ./src/chocoxcsp/xcsp3_exec [<options>] [<file>]`

<a name="xcsp"></a>
## XCSP3

This is related to [XCSP3 format](http://xcsp.org).

### Parsing and solving a XCSP3 file

Then, there are two ways to parse and solve a XCSP3 file with Choco:

* ##### Java front-end

  ```java -cp .:/path/to/choco-parsers-4.0.2-with-dependencies.jar org.chocosolver.parser.xcsp.ChocoXCSP [<options>] [<file>]```

Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* ```-cs``` : Check solutions with `org.xcsp.checker.SolutionChecker`
* other Choco-specific options are available

  
* ##### In a terminal (shell for Linux based OS)
  
  ```sh ./src/chocoxcsp/xcsp3_exec -jar /path/to/choco-parsers-4.0.2-with-dependencies.jar [<options>] [<file>]```  

Calling the shell with `-h` option will print the help message.

To avoid declaring the `-jar` option, you can export it once before calling the shell:

   `PARSER_JAR=/path/to/choco-parsers-4.0.2-with-dependencies.jar`
   
   `sh ./src/chocoxcsp/xcsp3_exec [<options>] [<file>]`


