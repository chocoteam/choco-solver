JSON utilities
==============

JSON utilities make possible to export variables and constraints declared in a `Model` object.
They also provide possibility to import a JSON file into a `Model` object. 

The import/export is limited to model variables and constraints.

Such utilities exist to ease sharing code with us (for analysis or bugs).
For instance, when model is generated, depends on external parameters or difficult to extract.

That may also help us (choco-solver dev team) to build better regression tests, 
which integrate your own usage of the library. 

## Export

Exporting a `Model` is done thanks to `JSON` utility class:
```java
Model model = new Model();
// variables and constraints declaration goes here
JSON.write(model, new File("./model.json"));
// resolution starts here
Solver solver = model.getSolver();
```
Then, a call to `JSON.write(model, file)` transforms a choco model into a JSON file.
It is recommended to serialize a model before starting the resolution.

`JSON` class offers API to export to an `Appendable` strem or to a `String`.

Note that the JSON format is quite human-readable.
Consequently, one may want to add a description of the model.
This can be achieved by calling 
```java
model.addHook("description", "A very short description.");
```

The description will not be imported, it only helps reading the JSON file.

### Limitations

#### Solver
Only the **modelling part** can be serialized, the resolution part is ignored.
That means that the JSON file contains, for example, no search strategy, limit or hook.

#### Model
Some limitations concerning constraints exist.
So it may happen that the exportation phase either fails or does not fully describe the underlying model. 
Such an issue occurs when:
- a native constraint is not supported (yet). 
- combining existing propagators into a constraint, like :
```java
new Constraint(
    "Combination", 
    new PropLessOrEqualXC(X, 3), 
    new PropGreaterOrEqualXC(X, 1))
.post();
```
- using your own constraints or propagators (or variables).

Depending on the failure, opening an issue may accelerate the process.
Otherwise, there is a chance that you need to extend the library.

Sometimes, slightly changing a model may prevent the failure.
For example, table constraint with "STR2+" algorithm (not supported) can be replaced by "GAC3rm".

## Import

Programmatically, calling: 
```java
Model model = JSON.readInstance(new File("./model.json"));
```
is enough to load a model. 
Keep in mind that no solver configuration is supported. 

### Parsing and solving a JSON file

Then, there are two ways to parse and solve a JSON file with Choco.

##### Java front-end

  ```java -cp .:/path/to/choco-parsers-4.0.3-with-dependencies.jar org.chocosolver.parser.json.ChocoJSON [<options>] [<file>]```
  
Common __options__ are:
* ```-a``` : This causes the solver to search for, and output all solutions in case of satisfaction problems. For optimization problems, the solver search for an optimal solution and outputs all intermediate solutions. When this option is not given the solver should search for and output only the first solution (for satisfaction problems) or the best known one (for optimization problems).
* ```-f```: When invoked with this option the solver ignores any specified search strategy.
* ```-p <n>```: When invoked with this option the solver is free to use multiple threads and/or cores during search.  The argument <n> specifies the number of cores that are available. 
* ```-tl <n>```: Limit the resolution time of each problem instance to <n> ms.
* other Choco-specific options are available
  
##### In a terminal (shell for Linux based OS)
  
  ```sh ./src/chocojson/json-exec -jar /path/to/choco-parsers-4.0.3-with-dependencies.jar [<options>] [<file>]```  

Calling the shell with `-h` option will print the help message.

To avoid declaring the `-jar` option, you can export it once before calling the shell:

   `PARSER_JAR=/path/to/choco-parsers-4.0.3-with-dependencies.jar`
   
   `sh ./src/chocojson/json_exec [<options>] [<file>]`
