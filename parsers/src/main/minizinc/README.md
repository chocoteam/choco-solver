# Choco-solver for MiniZinc

This directory contains what is needed to use Choco-solver as a FlatZinc backend of
[MiniZinc](https://www.minizinc.org/), from the command line or from the MiniZincIDE.

| File | Description |
|---|---|
| `choco.msc` | Solver configuration file, read by MiniZinc |
| `mzn_lib/` | Choco-specific redefinitions of the MiniZinc global constraints |
| `fzn-choco.sh` / `fzn-choco.bat` | Entry point called by MiniZinc (Unix / Windows) |
| `fzn-choco.py` | Translates the MiniZinc options and runs Choco on a FlatZinc file |
| `docker/` | Docker images for the MiniZinc Challenge |

## Prerequisites

- Java 17+
- Python 3, available as `python3`
- MiniZinc (or the MiniZincIDE bundle)

## Install from the jar file

The jar file attached to each [release](https://github.com/chocoteam/choco-solver/releases/latest)
(`choco-solver-<version>-light.jar`) embeds the content of this directory and can install itself:

```sh
java -jar choco-solver-<version>-light.jar --install-minizinc
```

This creates, in the MiniZinc solvers directory (`~/.minizinc/solvers`, or `%APPDATA%\MiniZinc\solvers` on Windows):

- `choco-<version>/`, which contains `mzn_lib/` and the wrapper scripts, set to run this jar file,
- `choco-<version>.msc`, the solver configuration file.

Restart the MiniZincIDE: *Choco-solver* now appears in the solver list.
Since the wrapper points to the jar file, run the installation again if the jar is moved.
Another solvers directory can be given as last argument; it must then be declared to MiniZinc as explained below.

The rest of this page is for those who work from the sources.

## Build the jar

From the repository root:

```sh
mvn package -DskipTests
```

This produces `parsers/target/choco-solver-<version>-light.jar`.

## Declare Choco to MiniZinc

The paths in `choco.msc` are relative to its own location, so MiniZinc only needs to know this directory.
Nothing has to be copied or edited.

### MiniZincIDE

1. Open *Preferences* (*MiniZincIDE > Preferences* on macOS, *Edit > Preferences* otherwise).
2. In the *MiniZinc* tab, add `<path-to-choco-solver>/parsers/src/main/minizinc` to the
   *Extra solver search paths* (*Add new*).
3. Close the preferences: *Choco-solver* now appears in the solver list.

The options declared in `choco.msc` are available in the solver configuration pane,
including *Use LCG* (lazy clause generation).

### Command line

Either set the search path for the session:

```sh
export MZN_SOLVER_PATH=<path-to-choco-solver>/parsers/src/main/minizinc
minizinc --solver org.choco.choco model.mzn data.dzn
```

or give the configuration file directly:

```sh
minizinc --solver <path-to-choco-solver>/parsers/src/main/minizinc/choco.msc model.mzn data.dzn
```

Run `minizinc --solvers` to check that Choco-solver is listed.

> The `.msc` file must stay in this directory: if it is copied elsewhere (e.g. in `~/.minizinc/solvers`),
> its relative paths no longer resolve. In that case, use `make msc` from the repository root,
> which builds the project and installs a copy with absolute paths in `~/.minizinc/solvers`.

## Which jar is used

`fzn-choco.py` looks for the jar in this order:

1. the `--jar-file` option,
2. the `CHOCO_JAR` environment variable,
3. the most recent `choco-solver-*-light.jar` in `parsers/target`,
4. `JAR_FILE`: the released jar in the local Maven repository (`~/.m2`),
   or the jar file which installed the script with `--install-minizinc`.

To use a specific jar from MiniZincIDE, set `CHOCO_JAR` in the environment the IDE is launched from.

## Running `fzn-choco.py` directly

The script can also solve a FlatZinc file without MiniZinc:

```sh
python3 fzn-choco.py -t 10000 model.fzn
```

Run `python3 fzn-choco.py --help` for the list of options
(`--jvm-args` changes the JVM settings, `-Xmx8G` by default).
