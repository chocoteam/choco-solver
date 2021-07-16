choco-parsers
=============

`choco-parsers` is an extension library for [choco-solver](https://github.com/chocoteam/choco-solver).

It aims at importing models to various format.
It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for [MiniZinc](http://www.minizinc.org/), 
a parser for [XCSP3](http://xcsp.org), an intermediate integrated XML-based format,
a parser for [DIMACS CNF](http://www.satcompetition.org/2009/format-benchmarks2009.html), the SAT solver competitions input file format  
and a parser for [MPS](http://miplib.zib.de/) a file format for presenting and archiving linear programming (LP) and mixed integer programming problems.

* [Download](https://github.com/chocoteam/choco-parsers/releases/latest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.choco-solver/choco-parsers/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.choco-solver/choco-parsers)
* [Parsing a file](#par)
* [MiniZinc](./MINIZINC.md)
* [XCSP3](XCSP3.md)
* [DIMACS CNF](DIMACS.md)
* [MPS](MPS.md)


<a name="par"></a>
### Parsing a file

By default, the extension of a file helps choosing the accurate parser.
Thus, any supported file (FlatZinc, XCSP3, DIMACS CNF or MPS) can be parsed and solved using the following command:

  ```java -jar .:/path/to/choco-parsers-4.0.5-with-dependencies.jar [options] <file>```

Only the file name is mandatory.
Alternatively, if the file has no explicit extension, the option ```-pa``` followed a digit between 1 and 4 
can be declared to specify the parser to use; 1: FlatZinc, 2: XCSP3, 3: DIMACS and 4: MPS.

A Bash file named ```parse.sh``` can also be found in `./src/main/bash/` that handles basic options.

Finally, if one can to use a specific parser directly, in that case, click on the right link above.
