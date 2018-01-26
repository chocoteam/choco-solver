choco-parsers
=============

`choco-parsers` is an extension library for [choco-solver](https://github.com/chocoteam/choco-solver).

It aims at importing models to various format.
It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for [MiniZinc](http://www.minizinc.org/), 
a parser for [XCSP3](http://xcsp.org), an intermediate integrated XML-based format
and a parser for [MPS](http://miplib.zib.de/) a file format for presenting and archiving linear programming (LP) and mixed integer programming problems.

It also provides utilities to export a model written with Choco to a JSON format 
and to import a JSON format file into a `Model`.

* [Download](https://github.com/chocoteam/choco-parsers/releases/latest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.choco-solver/choco-parsers/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.choco-solver/choco-parsers)
* [MiniZinc](./MINIZINC.md)
* [XCSP3](./XCSP3.md)
* [MPS](./MPS.md)
* [JSON](./JSON.md)
