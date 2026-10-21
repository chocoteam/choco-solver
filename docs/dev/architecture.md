# Architecture

## Maven modules

| Module     | Content                                                                                     |
|------------|---------------------------------------------------------------------------------------------|
| `solver`   | the library: modeling API, propagation, search, LCG, SAT and LP components                  |
| `parsers`  | front-ends for FlatZinc (ANTLR4 grammar), XCSP3 (`xcsp3-tools`), DIMACS CNF and MPS          |
| `examples` | runnable models, also used as documentation (`org.chocosolver.examples`)                    |

The user documentation (website pages) and this guide live in `docs/` and are not part of the Maven build.

## The `solver` module

Root package: `org.chocosolver`.

### Model / Solver separation

- **`solver.Model`**: the static problem definition. Variables, views and constraints are created through
  the factory interfaces it implements (`IVariableFactory`, `IViewFactory`, `IConstraintFactory`, ...).
- **`solver.Solver`**: the dynamic part, one per model (`model.getSolver()`).
  Its search loop chains three interchangeable components, in `solver.search.loop`:
  - `propagate.Propagate`: runs the propagation engine to a fix point;
  - `learn.Learn`: what is learnt from a failure (`LearnNothing`, or `LazyClauseGeneration` in LCG mode);
  - `move.Move`: the search policy (`MoveBinaryDFS`, `MoveBinaryLDS`, `MoveBinaryDDS`, `MoveBinaryHBFS`, `MoveLNS`, `MoveSeq`).
- **`solver.Settings`** (built with `SettingsBuilder`): global behaviour of a model, such as whether views
  are enabled or which checks are run.

### Variables (`solver.variables`)

`IntVar`, `BoolVar`, `SetVar`, `RealVar`, `GraphVar` (`DirectedGraphVar`, `UndirectedGraphVar`), plus `Task`.
Implementations are in `variables.impl`; **views** (`variables.view`) are variables defined as a function
of another one, without their own storage.
Domain modifications are notified as **events** (`variables.events`) and recorded in **deltas**
(`variables.delta`) that propagators can iterate over.

### Constraints and propagators (`solver.constraints`)

A `Constraint` groups one or more `Propagator`s, which implement the filtering algorithms.
A factory method only *declares* a constraint; the caller posts, reifies or ignores it
(see [best-practices.md](best-practices.md), "lifecycle of a Constraint").

### Propagation (`solver.propagation`)

`PropagationEngine` keeps the queue of propagators to run and dispatches domain events to them.

### Backtrackable memory (`org.chocosolver.memory`)

`IEnvironment` provides backtrackable (trailed) data structures: `IStateInt`, `IStateBool`, `IStateBitSet`, ...
Any state of a propagator that must be restored on backtrack has to be stored in such structures.

### Search (`solver.search`)

- `strategy`: variable and value selectors, and search strategies built from them;
- `limits`, `restart`: stop criteria and restart policies;
- `loop.monitors`: search monitors (listeners on the search loop);
- `loop.lns`: neighbourhoods for Large Neighbourhood Search.

### SAT, LCG and LP

- `org.chocosolver.sat`: an embedded MiniSat (`MiniSat`, `Clause`, `Reason`, ...), used for clauses
  and as the core of Lazy Clause Generation.
- In LCG mode (`solver.isLCG()`), every domain modification must come with an explanation (`Reason`),
  see [best-practices.md](best-practices.md), section 3.
- `org.chocosolver.lp`: linear programming support (`LinearProgram`, `MILP`).

## The `parsers` module

Main class: `org.chocosolver.parser.Parser`. Each format (`flatzinc`, `xcsp`, `dimacs`, `mps`) maps its input
to `Model` and `Solver` API calls. Parser tests are run with dedicated TestNG groups
(see [building-and-testing.md](building-and-testing.md)).

`parsers/src/main/minizinc` holds what MiniZinc needs to call Choco-solver (`choco.msc`, the `mzn_lib`
global constraints definition, the `fzn-choco` scripts). It is embedded in the jar under `minizinc/`,
which `MiniZincInstaller` extracts on `java -jar ... --install-minizinc`.
`parsers/src/main/xcsp3` holds scripts to run XCSP3 instances from the source code.
