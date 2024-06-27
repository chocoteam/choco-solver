MiniZinc Parser
===============

You can find here the instructions to parse and solve [MiniZinc](http://www.minizinc.org) files with Choco-solver.
The first thing to understand is that MiniZinc is a high-level constraint modeling language and 
choco-solver is not able to parse MiniZinc files directly.
Actually, MiniZinc files must be converted into [FlatZinc](https://docs.minizinc.dev/en/stable/part_4_reference.html) 
files before being parsed by Choco-solver.

There exists different ways to parse and solve MiniZinc files with Choco-solver.
The recommended way is to use the MiniZinc IDE, 
which is a graphical user interface that allows to write, parse and solve MiniZinc files.
             
## Pre-requisites

### Choco-solver jar file

Before starting, you need to download the Choco-solver jar file that contains the parser for FlatZinc files.
This jar file is available on the [Choco-solver repository](https://github.com/chocoteam/choco-solver/releases) on GitHub.
Or you can build it from the source code. 
In that case, you need to get the jar file from the `parsers/target` directory.
Select the jar file that contains the `-light` suffix.
                          
### Executable script
                      
To parse and solve FlatZinc files, you need an executable script that calls the Choco-solver parser.
This script is available in the [`parsers/src/main/minizinc` directory](https://github.com/chocoteam/choco-solver/tree/master/parsers/src/main/minizinc) 
of the Choco-solver repository on GitHub.
The entry point of this script is the `fzn-choco.py` file, which is a Python script that calls the Choco-solver parser.
Unfortunately, MiniZinc requires an executable script, so you need to create a shell script that calls the Python script.

Creating the executable script is quite straighforward with [Nuitka](https://nuitka.net/).

##### Building the executable script with Nuitka

First of all, you have to have a Python interpreter installed on your machine (version >=3.10).
Then, you can install Nuitka with the following command:
```bash                                     
pip install nuitka
```

Once Nuitka is installed, you need to adapt the `fzn-choco.py` script to set the path to the Choco-solver jar file.
Set the `JAR_FILE` variable to the path of the Choco-solver jar file.

Then, you can build the executable script with the following command:
```bash
python -m nuitka fzn-choco.py
```   

This command will create an executable script in the same directory as the Python script.
In the following, we will refer to this script as `fzn-choco` (without the `.py` extension).
     
**Important**:
For some reason, the executable script may be slower than the Python script.

## MiniZinc IDE
             
Now, Choco-solver can be added as a [third-Party Solver in MiniZinc IDE](https://docs.minizinc.dev/en/stable/fzn-spec.html#solver-configuration-files)
in MiniZincIDE. 
This step requires to create a *solver configuration file*  (with _.msc_ extension) that contains some basic information.
With respect to [MiniZinc specification](https://www.minizinc.org/doc-2.4.3/en/fzn-spec.html#solver-configuration-files), 
this file can be added in:
> the directory `$HOME/.minizinc/solvers` on Linux and macOS systems, and the Application Data directory on Windows systems.
             
### Important
Before moving the file to the correct directory, it must be edited to set the correct paths.
There are two fields in the _.msc_ file that must be set:
- `"mznlib"`: the absolute path to the directory containing the global constraints definition, i.e. `mzn_lib` directory.
- `"executable"`: the absolute path to the executable script `fzn_choco` or `fzn-choco.exe` depending on your OS.

The script referred to in this file (_choco.msc)_, together with global constraints definition can be downloaded from 
[the MiniZinc repository](https://github.com/chocoteam/choco-solver/tree/master/parsers/src/main/minizinc) on GitHub. 
           

1. Edit `choco.msc` to set the `"mznlib"` and `"executable"` fields.
   1. The `"executable"` field should be set to the path of the executable script `fzn-choco`. 
    You must set the full path to the script, including the extension (like `.exe` or `.bin`).
   2. The `"mznlib"` field should be set to the absolute path of the directory containing the global constraints definition,
   i.e. `mzn_lib` directory.
2. Copy `choco.msc` to the directory recommended by MiniZinc (please refer to the MiniZinc documentation).
3. Restart MiniZinc IDE.


### Dockerfile
Note that the `fzn-choco` script can be used in a Docker container.
A `Dockerfile` is available in the `parsers/src/main/minizinc` directory of the Choco-solver repository on GitHub.
Some Choco-solver Docker images for the MiniZinc Challenge are available on 
[hub.docker.com/](https://hub.docker.com/repository/docker/chocoteam/choco-solver-mzn/general).
