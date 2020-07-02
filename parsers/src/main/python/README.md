## Running an evaluation with `benchmark.py`

The Python script `benchmark.py` requires a YAML configuraiton file as input. 
Thus, the first step is to set the configuration file up. 

### `configuration.yaml`

The configuration file consists in a sequence of scalars and mapping.
Expected keys are:
- `name`: the name of the benchmark
- `inputs`: a sequence of absolute paths of directories to scan instances from
- `output`: absolute path of the directory to store log files and error files in 
- `jar`: the absolute path of the JAR file to use
- `process`: number of CPU to use. Set to `-1` to use all CPUs in the system
- `time`: define a timeout (in seconds). When expires, the command is killed
- `configurations`: a mapping of `key: "value"` where the `key` is the name (unique) of the configuration and `"value"` defines the argument to pass to the parser.
Example:  `DEFAULT:"-stat"` defines a configuration named `DEFAULT` and with `"-stat"` argument.
- `runner`: the command to run. It will be called with three arguments: *jar*, *args* and *file* which denote respectively the jar file to use, the arguments to pass and the instance file to treat.
Example: `"sh ./runner_xcsp.sh"`.

Note that paths can be defined relatively to home, usig `~` character. 

### Running the evaluation

A call to the following command will read the YAML configuration file and orchestrates the resolutions:
```shell script
python3 benchmark.py -c configuration.yaml
```

The `-c` argument is required and should point to the YAML configuration file.

When called, the `output` directory will be populated with log files and error files.
This suppose that the different configurations log some informations, to be post-processed.
For instance, `"-stat"` or `"-csv"` will output some resolution statistics.
