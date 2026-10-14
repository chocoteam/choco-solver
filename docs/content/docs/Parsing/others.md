---
title: "XCSP3 and DIMACS"
date: 2024-07-03T10:14:19+02:00
weight: 42
description: >
  XCSP3 and DIMACS formats
---

Dealing with XCSP3 and DIMACS formats is simpler than dealing with MiniZinc files.
Actually, Choco-solver is able to parse and solve instances in these formats directly, without any conversion.


The command line to parse and solve XCSP3 files is the following:
```sh
java -cp /path/to/choco-X.Y.Z-light.jar org.chocosolver.parser.xcsp.ChocoXCSP [<options>] [<file>]
```

The command line to parse and solve DIMACS files is the following:
```sh
java -cp /path/to/choco-X.Y.Z-light.jar org.chocosolver.parser.dimacs.ChocoDIMACS [<options>] [<file>]
```

To have an up-to-date lists of options, you can run the command with the `-h` option.
