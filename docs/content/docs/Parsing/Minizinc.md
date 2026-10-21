---
title: "Deal with MiniZinc files"
date: 2024-07-02T18:41:10+01:00
weight: 41
description: >
  MiniZinc software stack
---

You can find here the instructions to parse and solve [MiniZinc](http://www.minizinc.org) files with Choco-solver.
The first thing to understand is that MiniZinc is a high-level constraint modeling language and 
choco-solver is not able to parse MiniZinc files directly.
Actually, MiniZinc files must be converted into [FlatZinc](https://docs.minizinc.dev/en/stable/part_4_reference.html) 
files before being parsed by Choco-solver.
MiniZinc does this conversion and then calls Choco-solver, declared as a
[third-party solver](https://docs.minizinc.dev/en/stable/fzn-spec.html#solver-configuration-files).

The recommended way is to use the MiniZinc IDE, 
which is a graphical user interface that allows to write, parse and solve MiniZinc files.
             
## Pre-requisites

- Java 17 or later,
- Python 3, available as `python3`: MiniZinc calls a Python script which runs Choco-solver,
- [MiniZinc](https://www.minizinc.org/downloads/), or the MiniZinc IDE bundle,
- the Choco-solver jar file with the `-light` suffix, `choco-solver-X.Y.Z-light.jar`,
  available with each [release](https://github.com/chocoteam/choco-solver/releases) on GitHub.

## Install Choco-solver in MiniZinc

The jar file embeds what MiniZinc needs and installs itself:

```sh
java -jar choco-solver-X.Y.Z-light.jar --install-minizinc
```

This creates, in the directory where MiniZinc looks for solvers
(`$HOME/.minizinc/solvers` on Linux and macOS, `%APPDATA%\MiniZinc\solvers` on Windows):

- `choco-X.Y.Z/`, which contains the definition of the global constraints (`mzn_lib/`)
  and the scripts which run Choco-solver (`fzn-choco.py`, called by `fzn-choco.sh` or `fzn-choco.bat`),
- `choco-X.Y.Z.msc`, the *solver configuration file*, which refers to them.

Restart the MiniZinc IDE: *Choco-solver X.Y.Z* now appears in the list of solvers.
From the command line, `minizinc --solvers` lists it too.

{{% alert title="Info" color="primary" %}}
The scripts run the jar file the installation was made from:
if the jar file is moved or deleted, run the installation again.
Installing another version adds a new solver, next to the previous ones.
{{% /alert %}}

Another directory can be given as last argument, e.g. `--install-minizinc /path/to/solvers`.
MiniZinc must then be told to look into it:
in the MiniZinc IDE, add it to the *Extra solver search paths* (*Preferences*, *MiniZinc* tab);
from the command line, set the `MZN_SOLVER_PATH` environment variable.

## Solve from the command line

Once installed, Choco-solver is selected with its identifier:

```sh
minizinc --solver org.choco.choco model.mzn data.dzn
```

The standard MiniZinc options are supported (`-a`, `-f`, `-n`, `-p`, `-r`, `-s`, `-t`),
as well as `--fzn-flag -lcg` to turn on Lazy Clause Generation.

## From the source code

When working from the source code, nothing needs to be installed:
the solver configuration file `parsers/src/main/minizinc/choco.msc` refers to its files with relative paths
and the scripts look for the jar file built in `parsers/target`.
Build the jar file (`mvn package -DskipTests`), then add the `parsers/src/main/minizinc` directory
to the *Extra solver search paths* of the MiniZinc IDE, or to the `MZN_SOLVER_PATH` environment variable.
See the [README](https://github.com/chocoteam/choco-solver/tree/master/parsers/src/main/minizinc) of this directory
for more details.

## Docker
Choco-solver Docker images for the MiniZinc Challenge are available on 
[hub.docker.com](https://hub.docker.com/r/chocoteam/choco-solver-mzn):
`chocoteam/choco-solver-mzn:X.Y.Z`, and `chocoteam/choco-solver-mzn:X.Y.Z-X` which runs Lazy Clause Generation.
They are built from the `Dockerfile.dms` file of the `parsers/src/main/minizinc/docker` directory.
