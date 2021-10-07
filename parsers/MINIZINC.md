MiniZinc Parser
===============

This is related to [MiniZinc and FlatZinc format](http://www.minizinc.org).

There exists different ways to parse and solve MiniZinc files with Choco-solver.

### MiniZinc IDE

The first approach, that should be favoured, is to add Choco-solver as a [third-Party Solver in MiniZinc IDE](https://www.minizinc.org/doc-2.5.5/en/fzn-spec.html#solver-configuration-files). 
A *solver configuration file* that contains some basic information is available, named [choco.msc](https://github.com/chocoteam/choco-solver/blob/master/parsers/src/main/minizinc/choco.msc).
With respect to MiniZinc specification, this file should be added in:
> the directory `$HOME/.minizinc/solvers` on Linux and macOS systems, and the Application Data directory on Windows systems.
> [Source](https://www.minizinc.org/doc-2.4.3/en/fzn-spec.html#solver-configuration-files)

The executable `fzn_choco` referred to in this file, together with global constraints definition can be downloaded from [the MiniZinc repository](https://github.com/chocoteam/choco-solver/tree/master/parsers/src/main/minizinc) on GitHub.
    
1. Edit `./parsers/src/main/minizinc/choco.msc` and 
update the follwing fields with the right path: `"mznlib"` and `"executable"`
                                                                             
2. Either copy `choco.msc` to `~/.minizinc/solvers/` directory :

````shell
cp ./parsers/src/main/minizinc/choco.msc ~/.minizinc/solvers/
````
or declare a symbolic link:
````shell
ln -s ./parsers/src/main/minizinc/choco.msc ~/.minizinc/solvers/
````


### Converting from MiniZinc to FlatZinc

If one wants to manually convert MiniZinc files into FlatZinc files parsable by Choco-solver. 

The [`mzn_lib`](https://github.com/chocoteam/choco-solver/blob/master/parsers/src/main/minizinc/mzn_lib) directory lists the supported global constraints for Choco-solver.
It is needed as an argument of `minizinc`, a tool to convert MiniZinc files to FlatZinc files. 
    
```bash
minizinc -c --solver org.minizinc.mzn-fzn -I "/path/to/mzn_lib" model.mzn -d data.dzn -o output.fzn
```

where `model.mzn` is the MiniZinc file describing the model, 
`data.dzn` (optional) contains data to instantiate the MiniZinc file and
`output.fzn` is the output FlatZinc filename you want.

The `-I` argument defines global constraints provided by Choco-solver.

See MiniZinc documentation for more details on how to convert MiniZinc files into FlatZinc files.

### Parsing and solving a FlatZinc file

Then, there are two ways to parse and solve a FlatZinc file with Choco:

* ##### Java front-end

```bash
java -cp .:/path/to/choco-parsers-4.10.3-jar-with-dependencies.jar org.chocosolver.parser.flatzinc.ChocoFZN [<options>] [<file>]
```
  
Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* other Choco-specific options are available
  
* ##### In a terminal (shell for Linux based OS)
  
```bash 
sh ./parser/src/main/minizinc/fzn-choco -jar /path/to/choco-parsers-4.10.3-jar-with-dependencies.jar [<options>] [<file>]
```

Calling the shell with `-h` option will print the help message.

To avoid declaring the `-jar` option, you can export it once before calling the shell:

   `PARSER_JAR=/path/to/choco-parsers-4.10.3-jar-with-dependencies.jar`
   
   `sh ./parser/src/main/minizinc/fzn-choco [<options>] [<file>]`


### Dockerfile

Some Choco-solver Docker images for the MiniZinc Challenge are available on [hub.docker.com/](https://hub.docker.com/repository/docker/chocoteam/choco-solver-mzn/general).
