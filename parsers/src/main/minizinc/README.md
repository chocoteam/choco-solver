# Declare Choco-solver in MiniZinc IDE
         
See [minizinc documentation](https://www.minizinc.org/doc-2.5.5/en/fzn-spec.html#solver-configuration-files) for more details
or alternatives.

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