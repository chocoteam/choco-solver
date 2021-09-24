These are files to run choco-solver with [irace](https://iridia.ulb.ac.be/irace/) and [slurm](https://slurm.schedmd.com/documentation.html).

- `target_runner` defines a command to solve FlatZinc file with an output level set as `-lvl IRACE`.
Such a level provides two values each time a solution is found or proof is done.
This defines 
- The `target_evaluator` requires to install [hypervolume](http://lopez-ibanez.eu/hypervolume) and [nondominated](https://github.com/MLopez-Ibanez/irace/tree/master/examples/mo-tools).