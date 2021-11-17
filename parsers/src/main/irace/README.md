# Run choco-solver with iRace and slurm
These are files to run choco-solver with [irace](https://iridia.ulb.ac.be/irace/) and [slurm](https://slurm.schedmd.com/documentation.html).

- `target_runner` defines a command to solve FlatZinc file with an output level set as `-lvl IRACE`.
Such a level provides two values each time a solution is found or proof is done.
This is required then by  `target_evaluator` to compute the hypervolume. 
- The `target_evaluator` requires to install [hypervolume](http://lopez-ibanez.eu/hypervolume) and [nondominated](https://github.com/MLopez-Ibanez/irace/tree/master/examples/mo-tools).
- In addition, a `Jars` directory should be created which contains jar files. 
- An `Instances` directory should be created and filled with instance to solve, in the current configuration: FlatZinc files.

**Anyone wishing to share improvements is welcome to do so.**