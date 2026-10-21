---
title: "XCSP3 and DIMACS"
date: 2024-07-03T10:14:19+02:00
weight: 42
description: >
  XCSP3 and DIMACS formats
---

Dealing with XCSP3 and DIMACS formats is simpler than dealing with MiniZinc files.
Actually, Choco-solver is able to parse and solve instances in these formats directly, without any conversion.
All you need is Java 17 or later and the Choco-solver jar file with the `-light` suffix, `choco-solver-X.Y.Z-light.jar`,
available with each [release](https://github.com/chocoteam/choco-solver/releases) on GitHub.

The format is deduced from the file extension
(`.xml` or `.lzma` for XCSP3, `.cnf` for DIMACS, `.mps` for MPS, `.fzn` for FlatZinc):

```sh
java -jar /path/to/choco-solver-X.Y.Z-light.jar [<options>] <file>
```

Each format can also be called explicitly. The command line to parse and solve XCSP3 files is the following:
```sh
java -cp /path/to/choco-solver-X.Y.Z-light.jar org.chocosolver.parser.xcsp.ChocoXCSP [<options>] <file>
```

The command line to parse and solve DIMACS files is the following:
```sh
java -cp /path/to/choco-solver-X.Y.Z-light.jar org.chocosolver.parser.dimacs.ChocoDIMACS [<options>] <file>
```

To have an up-to-date lists of options, you can run the command with the `-h` option.

When working from the source code, the `parsers/src/main/xcsp3` directory provides two scripts:
`xcsp3-exec`, which solves one XCSP3 instance with competition-like options, 
and `run.sh`, which solves all the instances of a directory.
See the [README](https://github.com/chocoteam/choco-solver/tree/master/parsers/src/main/xcsp3) of this directory.
