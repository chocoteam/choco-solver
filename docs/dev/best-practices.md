# Development best practices for choco-solver

This document is for developers, human or AI, who know constraint programming and Java but not the
specifics of choco-solver. It collects rules learned from real fixes.
Each rule states **why** it exists, so readers can judge the edge cases.

> Living document: add an entry whenever a fix reveals a non-obvious pitfall
> (see [Adding an entry](#adding-an-entry)).

## Contents

1. [Building constraints (factories)](#1-building-constraints-factories)
2. [Variables and domains](#2-variables-and-domains)
3. [Writing a propagator](#3-writing-a-propagator)
4. [Testing](#4-testing)
5. [Delivering a change](#5-delivering-a-change)

---

## Reminder: the lifecycle of a `Constraint`

A `Constraint` returned by a factory (`model.arithm(...)`, `model.count(...)`, etc.) is
**only declared** (status `FREE`). The caller then decides what to do with it:

| Call                                | Effect                                                            |
|-------------------------------------|-------------------------------------------------------------------|
| `c.post()`                          | `c` must hold (status `POSTED`)                                   |
| `c.reify()` / `c.reifyWith(b)`      | `b ⇔ c`; posts a `ReificationConstraint(b, c, c.getOpposite())`   |
| `c.impliedBy(b)`                    | `b ⇒ c`                                                           |
| `Constraint.merge(name, c1, c2...)` | new constraint holding the propagators of `c1, c2...` (which are `ignore()`d) |
| `c.ignore()`                        | `c` will not be reported as "declared but never posted" (`Settings.checkDeclaredConstraints()`) |

⚠️ `model.ifThen(...)` and `model.ifThenElse(...)` **post immediately** (they return `void`).

---

## 1. Building constraints (factories)

### 1.1 A factory that returns a `Constraint` posts nothing

**Rule.** In a method that returns a `Constraint`, do not call `.post()`, `ifThen(...)` or `ifThenElse(...)`
on constraints over the user's variables.

**Why.** The caller may reify, ignore or merge the returned constraint. Whatever the factory posts
applies anyway, unconditionally, and changes the meaning of the model.
Fixed example: `absolute(var1, var2)` with `var1` instantiated to 0 posted `var2 = 0`. Reifying
`|var2| = var1` therefore forced `var2 = 0` whatever the value of the boolean.

**How.**
- Return the constraint: `return arithm(var2, "=", 0);`
- Combine several sub-constraints with `Constraint.merge(...)`, **following rule 1.2**.

**Accepted exception.** When `ref().getSolver().isLCG()` is true, several global constraints are decomposed:
the sub-constraints are posted and the factory returns `ref().voidConstraint()`. See rule 1.5.

**Ref.** commit `7919af5bd` (fixes #1231, #1235), `CHANGES.md`.

### 1.2 `merge` breaks reification when an auxiliary variable is involved

**Rule.** Never merge into the returned constraint the *definition* of an auxiliary variable
created by the factory (e.g. `merge(times(x, y, aux), arithm(aux, "<", c))`).

**Why.** The default negation (`Constraint.makeOpposite()` → `Opposite` → `PropOpposite`)
fails only when the constraint is *entirely satisfied*. When `b = 0`, the solver may give `aux`
a value that violates its definition. The negation then holds even when the original relation
is true, so `b = 0` is wrongly accepted.

Verified example: `model.arithm(x, "*", y, "<", 2).reify()` with `x, y ∈ [-3, 3]` enumerates 82 distinct
`(x, y, b)` triples instead of 49, 33 of them wrong.
The same issue arises if the operator is "absorbed" into the domain of the auxiliary variable
(`times(x, y, intVar(lb, c - 1))`): the variable stays free under negation.

**How**, in order of preference:
1. **No auxiliary variable**: use a view or reformulate (rule 1.3).
2. **Post the auxiliary definition**, and return only the constraint that uses it.
   This is valid **only if the definition is functional and total**: every combination of values
   of the original variables has an image in the domain of `aux`. Example: the enumerated copy
   `evalue = value` in `count(IntVar, IntVar[], IntVar)`. Posting it then does not restrict the
   user's variables. Counter-examples: `aux = x / y` when `y` can be 0; a domain of `aux`
   narrowed by the operator. Flag this exception with a comment.
3. Otherwise, write a dedicated propagator or override `makeOpposite()` to provide a proper negation.

In every case, **test reification** (rule 4.4).

### 1.3 Prefer views and reformulations to auxiliary variables

**Rule.** Before creating an intermediate variable and a linking constraint, look for a view
or an existing constraint that expresses the relation directly.

**Why.** A view stores nothing new and introduces no free variable: no extra propagator,
and reification stays correct.

**How.**
- `v1 op v2 * cste` → `arithm(v1, op, model.intView(cste, v2, 0))` (affine view `a·x + b`).
- `and(bools)` → `sum(bools, "=", bools.length)`; `or(bools)` → `sum(bools, ">=", 1)`.

**Ref.** commits `ebad6b9b9`, `1358cf97e`.

### 1.4 Sub-constraints built only for their propagators: `ignore()`

**Rule.** If a factory creates a `Constraint` only to retrieve its propagators
(`c.getPropagators()`, `c.propagators`), call `c.ignore()`. `Constraint.merge` already does it.

**Why.** When `checkDeclaredConstraints` is enabled, a constraint that is neither posted nor reified
is reported as a user oversight.

**Example.** `keySort`: `allDiff.ignore()` then `ArrayUtils.append(allDiff.propagators, ...)`.
A constraint that is already posted or reified cannot be ignored (assertion on status `FREE`).

### 1.5 LCG mode: decompose or weaken, and say so

**Rule.** When a propagator cannot explain its filtering (no `Reason`), the factory
checks `ref().getSolver().isLCG()`, then decomposes or picks a weaker version. It warns
the user as follows:

```java
if (ref().getSolver().isLCG()) {
    if (ref().getSettings().warnUser()) {
        ref().getSolver().log().white().println("Warning: xxx constraint is decomposed (due to LCG).");
    }
    // decomposition...
}
```

**Why.** In LCG, every domain modification needs an explanation (rule 3.1).
A propagator that does not provide one makes clause learning unsound.

### 1.6 Name internally created objects

Use `model.generateName("PREFIX_")` for auxiliary variables. Names stay unique
and readable in traces.

---

## 2. Variables and domains

### 2.1 Bounded domain ≠ enumerated domain: `removeValue` may do nothing

**Rule.** A propagator that removes interior values does not reach arc consistency on a
variable with a bounded domain.

**Why.** On `IntervalIntVarImpl`, `removeValue(v, ...)` changes the domain only if `v` is a
bound. Otherwise, the method returns `false` **without any error**.
- `model.intVar(name, lb, ub, true)` creates a **bounded** domain; `false` creates an enumerated one.
- `model.intVar(name, lb, ub)` picks a bounded domain if `ub - lb + 1 >= settings.getEnumeratedDomainSizeThreshold()`.
- `model.intVar(name, int[])` creates an enumerated domain, possibly with holes.

**How**, depending on the case:
- **Accept bound consistency** and tighten bounds in cascade: while the current bound is unsupported,
  remove it. Do it at the end of the initial propagation **and** after every event, including events on the
  bounded variable itself (see `PropEnumDomainChanneling#tightenBounds`, and rule 3.4).
- **Create an enumerated copy** linked by equality when arc consistency is required (see `count` with
  a bounded `value`). Rule 1.2, option 2, then applies.
- In every case, **document in the Javadoc** the consistency level reached for each domain type.

### 2.2 Do not express a contract with `assert`

**Rule.** A precondition on the domain type (e.g. `assert var.hasEnumeratedDomain()`) is not
enough: assertions are disabled in production. Either handle the case, transform it
in the factory, or throw an explicit `SolverException`.

---

## 3. Writing a propagator

### 3.1 Always provide an LCG-compatible `Reason`

Every domain modification passes a reason: an explicit one in LCG, `Reason.undef()` otherwise.

```java
vars[n].removeValue(i + offSet, this,
        lcg() ? this.r(vars[i].getValLit()) : Reason.undef());
```

`lcg()` and the `r(...)` overloads are defined in `Propagator`.

### 3.2 `Task` is a propagator that no longer posts itself

`new Task(s, d, e)` (or `model.taskVar(...)`) no longer enforces `s + d = e`. Call `task.post()`,
or pass the task to a constraint that includes its non-passive propagators (`cumulative`).

**Ref.** commits `7919af5bd`, `ebad6b9b9`.

### 3.3 LCG: bounded domains provide no equality literals

**Rule.** A propagator whose reasons use `x.getLit(v, IntVar.LR_EQ)` must not receive a bounded variable in LCG mode.

**Why.** In LCG, a bounded variable is wrapped in `IntVarLazyLit`, which only supports bound literals:
`getLit(v, LR_EQ)` throws `UnsupportedOperationException: IntVarLazyLit does not support this type of literal`.
Enumerated variables are wrapped in `IntVarEagerLit`, which supports them.

**How.** Either write reasons with bound literals only, or, in the factory, channel an enumerated copy when
`isLCG() && !x.hasEnumeratedDomain()` (see `boolsIntChanneling`; the copy follows rule 1.2, option 2).
Cover the case with a test run with `SettingsBuilder.init().setLCG(true)` on a bounded domain.

The same limit applies to **search decisions**: `=` and `!=` decisions on a bounded variable cannot be turned
into literals when learning (`LazyClauseGeneration#extractFromDecisions` throws). `FullyRandom` and
`Search.randomSearch` make such decisions. In LCG tests over bounded variables, branch with bound splits, e.g.
`Search.intVarSearch(new Random<>(seed), new IntDomainMin(), DecisionOperatorFactory.makeIntSplit(), vars)`.

### 3.4 A propagator is not called back on its own modifications

**Rule.** Each call to `propagate(evtmask)` or `propagate(varIdx, mask)` must leave the propagator at its own
fixpoint: do not rely on the engine to call it again after its own domain modifications.

**Why.** Each variable records the cause of its pending events (`AbstractVariable#storeEvents`).
When the propagator is the only cause, `BipartiteList#schedule` skips it (`cause != prop`).
It is called again only if another cause also modified the same variable in the meantime (the recorded cause is
then `Cause.Null`), which cannot be relied upon.
Fixed example: in `PropEnumDomainChanneling`, removing an upper bound could expose a new bound whose
boolean was already fixed to 0. Nothing removed it, so bound consistency was lost.

**How.** Loop inside the propagator until no more deduction applies (e.g. `tightenBounds()`),
and check the fixpoint in tests: after each `model.getSolver().propagate()`, assert the expected consistency
on every variable (see `BoolsIntChannelingTest#testFixpointConsistency`).

### 3.5 Union-find "path" structures (`t[]`/`pathmax`/`pathset`): pre-collapse capacities that start at zero

**Rule.** In Hall-interval algorithms built on a union-find-style array (`t[]`, walked with
`pathmax`/`pathmin` and redirected with `pathset` when a slot's remaining capacity `d[z]` hits
`0`), a slot whose capacity is **already** `0` right after initialization (not reached via a
decrement during the main loop) must be pre-redirected to its neighbour *before* the main loop
runs. Do not "fix" a resulting hang by failing whenever `--d[z] < 0`: that turns the hang into a
different bug — false failures on feasible states, because `d[z]`/the matching `u.sum(...)`
bounds check both end up computed over the wrong (already-redirected-past) sub-range.

**Why.** `AllDifferent`'s equivalent structure (`AlgoAllDiffBC`) never hits this: its per-slot
capacity is a value-range width, always ≥ 1. GCC's `AlgoGccBC` sums `maxOcc`/`minOcc` instead,
which is legitimately `0` over any run of values with `maxOcc == 0` — a case that doesn't exist
in AllDifferent's model at all. A variable landing exactly on such a pre-existing zero slot
decrements it to `-1` without ever passing through the `== 0` transition that triggers the
redirect, so `t[]` is left inconsistent: later queries either cycle forever in `pathset` (a real
production hang, found on a 17k-variable FlatZinc instance, `blocks_16-4-5.fzn`), or, if a naive
"fail whenever negative" guard is added instead, the algorithm fails on states that are actually
feasible (found on `handball_handball8.fzn`: BC proved a worse "optimum" than `"DEFAULT"`/`"AC"`
without erroring).

**How.** In the init loop that fills `d[]`/`t[]`, redirect every already-zero slot to its
neighbour right after computing `d[i]` (`if (d[i] == 0) t[i] = i ± 1;` in `AlgoGccBC#filterLowerMax`
/ `#filterUpperMax`), *before* the main loop starts. Then the original `if (--d[z] == 0) { ... }` redirect (no extra guard) is
correct again, because `pathmax`/`pathmin` naturally skip pre-collapsed slots, so a query can only
ever land on `z` with `d[z] > 0` on its first descent. Not every union-find init in this codebase
needs this: check whether the capacity being summed can legitimately be `0` for a whole sub-range
that no variable has touched yet (here: unlisted/forbidden values) — if it can, some slots start
zero and need the extra pass; if the capacity is a value-range width (always ≥ 1, as in
`AlgoAllDiffBC`), it doesn't. **Verify with more than small random fuzzing**: both bugs survived
thousands of small random instances (`n ≤ 8`) and only surfaced on real, larger FlatZinc models —
prefer testing against real corpora for this class of structure, per rule 4.2's spirit.

**Ref.** `AlgoGccBC#filterLowerMax`/`#filterUpperMax` (init loop), `GlobalCardinalityBCTest
#testForcedValueWithZeroCapacityFailsFast` / `#testZeroCapacityRunsDoNotOverFilter`.

---

### 3.6 Warm-started matching: a growing lower bound can leave the augmenting search with no entry point

**Rule.** An augmenting-path/matching algorithm that is **warm-started** (reuses last call's
`matching[]`/`flow[]` instead of recomputing from scratch, for performance) must not assume a free
(unmatched) node will always exist to anchor the search. If a lower bound tightens between two
calls on the *same persistent instance* (e.g. a cardinality variable's `LB` rises) and the
warm-started state happens to leave **zero** free nodes — every node already matched, even to a
node with slack — the search has no entry point left, even though a trivial reassignment (move one
already-matched node away from a slack-having match) would trivially fix the new deficit.

**Why.** The search only knows how to walk a chain *backward from a free node* to the deficit
root; reaching a node with slack mid-chain does not by itself terminate the path (in general it
may need to relay through other, farther deficits). When **several** values are deficient at once
and a warm-started matching has piled every variable onto one shared, slack-having value, the
BFS's own `visited` bookkeeping marks every one of those deficits as already-seen *before* any path
can route the shared value's spare capacity toward them — so no augmenting path is ever completed,
and the algorithm wrongly fails or, under a `min`/objective search, proves an under-estimated bound
instead of the true optimum. Small random fuzzing did not catch this: it needs a scenario where a
`LB` genuinely *rises* between two `propagate()` calls on one instance, which barely happens outside
a `MAXIMIZE`/`MINIMIZE` search tightening an objective bound that feeds into cardinality variables.
Found on `peaceable_queens_n9_q5.fzn`/`peaceable_queens_n8_q3.fzn`: `AlgoGccAC` (Régin's flow-based
AC for the global cardinality constraint) proved `1`/`3` as optimal instead of the true `2`/`4`
that `BC` correctly proved.

**How.** In the warm-start repair step (`prepare()`), after the usual "drop matches that now
exceed a shrunk upper bound" pass, add a symmetric one: compute the total deficit
(`Σ max(0, minOcc[j] - flow[j])`), and if positive, proactively unmatch that many variables, taken
from values that currently have slack (`flow[j] > minOcc[j]`). This guarantees the subsequent
augmenting search always has enough free variables to anchor on directly — no path through a
"donor" value or pseudo-source node is needed for what is fundamentally a trivial reassignment.

**Ref.** `AlgoGccAC#prepare` (the "a cardinality lower bound may have grown..." block),
`GlobalCardinalityACTest#testDeficitAppearingAfterWarmStart`.

### 3.7 Incrementally-maintained state fed by delta monitors must itself be backtrackable

**Rule.** When a propagator maintains a structure incrementally across `propagate()` calls by
patching it from an `IIntDeltaMonitor`'s removed values (instead of rebuilding it from scratch
every call), that structure must be backed by trail-aware storage (e.g.
`SetFactory.makeStoredSet(type, offset, model)`, or a `DirectedGraph` built with the `model`
constructor overload), **not** a plain in-memory one (e.g. `SetFactory.makeBitSet(offset)`).

**Why.** A delta-driven patch (`onValueRemoved`: "remove `i` from `domVars[j]`") is a one-way
forward operation: nothing ever calls the symmetric "add `i` back to `domVars[j]`" when the
solver backtracks and the variable's own domain is restored by the trail. If the patched
structure is not itself trailed, the removal silently outlives the search node that caused it,
so after backtracking to try a different branch, the structure is **stale** (too restrictive)
relative to the variables it is supposed to mirror — found while prototyping an incremental
variant of `AlgoGccAC` (`domVars[]` built with `SetFactory.makeBitSet(0)`): a search enumerating
a single variable over 4 values found only 1 solution instead of 4, because the first branch's
removals from `domVars` were never undone for the following branches. Full-rebuild propagators
(e.g. `AlgoGccAC`'s own `prepare()`) never hit this, because they always recompute the whole
structure from the *current* (correctly-backtracked) domains on every call — backtracking
correctness is "free" as a side effect of not caching anything across calls in the first place.

**How.** Build any delta-patched structure with the model-backed/stored variant of its factory
(`SetFactory.makeStoredSet(SetType.BITSET, 0, model)` for a set, `new DirectedGraph(model, ...)`
for a graph adjacency, as `PropAtLeastNValues_AC` already does for its own digraph) so every
mutation is trailed exactly like the decision variables' own domains are, and reverted
automatically on backtrack — no manual "undo" bookkeeping needed.

**Ref.** `PropAtLeastNValues_AC` (`digraph`) for the established pattern. Found and fixed while
prototyping an incremental fork of `AlgoGccAC` (`domVars`); that fork was later abandoned on
performance grounds (no measurable gain on a real-instance benchmark, see GCC_REVIEW_MEMO.md)
and removed from the repo, but the lesson about backtrackability stands on its own.

---

## 4. Testing

Framework: TestNG. Groups: `1s`, `10s`, `checker`, `lcg`… Always set `timeOut`.

### 4.1 Deterministic tests: explicit seed

Any randomness depends on a seed. For a random search: `new FullyRandom(vars, seed)`.
To sweep over seeds, use the shared provider:

```java
@Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
@Providers.Arguments(values = {"1", "20"})   // seeds 1..19 ; {"n"} → 0..n-1 ; {"a","b","step"}
public void testXxx(int seed) { ... }
```

`Providers.trueOrFalse` serves boolean parameters (e.g. bounded / enumerated).
Put the seed in the assertion message: `assertEquals(actual, expected, "seed=" + seed)`.

### 4.2 Compare a global constraint with a reference decomposition

Build two identical models: one with the constraint under test, the other with an elementary,
trusted decomposition (e.g. `ifThenElse` + `sum`). Enumerate all solutions with the same
strategy and the same seed, then compare the solution counts.
Example: `CountTest#testCountBoundedValueVsDecomposition`.

### 4.3 Cover the three domain shapes

**Enumerated**, **bounded** and **with holes** (`intVar(name, int[])`). Many bugs only show up
on a bounded domain (rule 2.1).

### 4.4 Test reification, not only `post()`

Any factory that combines sub-constraints or creates auxiliary variables must be tested reified,
with the boolean left free. Check each solution projected onto the original variables:

```java
BoolVar b = model.arithm(x, "*", y, "<", 2).reify();
Set<String> seen = new HashSet<>();
while (model.getSolver().solve()) {
    if (seen.add(x.getValue() + "," + y.getValue() + "," + b.getValue())) {
        boolean truth = x.getValue() * y.getValue() < 2;
        assertEquals(b.getValue() == 1, truth, "x=" + x.getValue() + " y=" + y.getValue());
    }
}
assertEquals(seen.size(), x.getDomainSize() * y.getDomainSize()); // exactly one b per (x, y)
```

Distinct triples are counted because auxiliary variables can duplicate solutions.

### 4.5 `PerformanceTest` statistics

The parsers' performance tests (`parsers/src/test/resources/{flatzinc,xcsp,mps,dimacs}/instances.csv`)
check node and failure counts. A filtering change may alter them. Only update a line
once the difference is understood, and explain why in the commit message.

### 4.6 Test fixed-size bit-set boundaries and restoration

**Rule.** Test the last valid bit separately from the exclusive end of a range,
including the empty range at the word size. Check that rejected writes preserve
the contents, and that valid writes are undone by `worldPop()`.

**Why.** A range ending at the word size is valid even though a single-bit write
at that index is not. Java masks shift distances, so an unchecked index beyond
a word can silently modify another bit.

**How.** Compare all ranges of the 32-bit and 64-bit implementations with
`java.util.BitSet`, then verify restoration after each range operation.

**Ref.** #934, `OneWordBitSetRangeTest`.

---

## 5. Delivering a change

- Entry in `CHANGES.md` (next release section) and contributor added if needed.
- BSD license header (`etc/header.txt`), `@author` and class-level Javadoc for every new file.
- `mvn checkstyle:check`.
- At least `mvn test -Dgroups=1s` on `solver`; `-Dgroups=lcg` if a propagator or a factory
  has an LCG branch.

---

## Adding an entry

Put the entry in the relevant section (or create one), using this template:

```markdown
### N.M Short imperative title

**Rule.** What to do or avoid, in one or two sentences.

**Why.** The internal mechanism at play; ideally a verified minimal example.

**How.** Recommended solution(s), with a code excerpt if useful.

**Ref.** commit, issue, class or illustrative test.
```

Keep examples **verified** (actually run) and references up to date. Delete a rule that has become wrong
rather than annotating it.
