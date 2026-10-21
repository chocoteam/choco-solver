# xcsp3-exec

Shell script to parse and solve XCSP3 constraint models using Choco-solver.

## Usage

```sh
./xcsp3-exec [options] <file.xml>
```

## Options

| Option | Description | Default                             |
|---|---|-------------------------------------|
| `-a` | Search for and output all solutions | First/best solution only            |
| `-tl <n>` | Time limit in seconds | 500                                 |
| `-ml <n>` | Memory limit in MiB | 4000                                |
| `-p <n>` | Number of processing units | 1                                   |
| `-dir <s>` | Directory where the Choco JAR resides | `parsers/target`                    |
| `-tdir <s>` | Temporary files directory (ignored) | `./`                                |
| `-seed <n>` | Random seed (ignored) | 0                                   |
| `-jar <j>` | Override the Choco JAR path | `$CHOCO_JAR`, or latest light JAR in `-dir` |
| `--jargs <args>` | Override JVM arguments | `-Xss64m -Xms64m -Xmx4096m -server` |

## Examples

Solve a single instance:

```sh
./xcsp3-exec alpha.xml
```

Find all solutions with a 100s time limit using 2 threads:

```sh
./xcsp3-exec -a -tl 100 -p 2 alpha.xml
```

## Prerequisites

- Java 17+
- The Choco parsers JAR (build with `mvn package -DskipTests` from the repository root)

# run.sh

Shell script to solve, one after the other, all the XCSP3 instances (`*.xml`, `*.xml.lzma`) found in a directory.

```sh
./run.sh <instances-dir> [<options passed to ChocoXCSP>]
```

The JAR is given by the `CHOCO_JAR` environment variable, otherwise the most recent light JAR in `parsers/target` is used.
For example, to give each instance 60 seconds:

```sh
./run.sh ~/XCSP3/CSP22to24 '-limit=[60s]'
```
