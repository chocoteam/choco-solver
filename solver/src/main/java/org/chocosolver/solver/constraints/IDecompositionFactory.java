/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.constraints.nary.flow.PropMinCostMaxFlow;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * An interface dedicated to list decomposition of some constraints.
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 12/06/2018.
 */
public interface IDecompositionFactory extends ISelf<Model> {

    /**
     * Posts a decomposition of an among constraint.
     * nbVar is the number of variables of the collection vars that take their value in values.
     * <br/><a href="https://sofdem.github.io/gccat/gccat/Camong.html">gccat among</a>
     * <br/>
     * Decomposition described in :
     * C. Bessiere, E. Hebrard, B. Hnich, Z. Kiziltan, T. Walsh,
     * Among, common and disjoint Constraints
     * CP-2005
     *
     * @param nbVar  a variable
     * @param vars   vector of variables
     * @param values set of values
     */
    default void amongDec(IntVar nbVar, IntVar[] vars, IntVar[] values) {
        BoolVar[] ins = ref().boolVarArray("ins", vars.length);
        for (int i = 0; i < vars.length; i++) {
            BoolVar[] eqs = ref().boolVarArray("ins", values.length);
            for (int j = 0; j < values.length; j++) {
                ref().reifyXeqY(vars[i], values[j], eqs[j]);
            }
            ref().addClausesBoolOrArrayEqVar(eqs, ins[i]);
        }
        ref().sum(ins, "=", nbVar).post();
    }

    /**
     * Creates and <b>posts</b> a decomposition of a cumulative constraint: associates a boolean
     * variable to each task and each point of time sich that the scalar product of boolean
     * variables per heights for each time never exceed capacity.
     *
     * @param starts    starting time of each task
     * @param durations processing time of each task
     * @param heights   resource consumption of each task
     * @param capacity  resource capacity
     * @see org.chocosolver.solver.constraints.IIntConstraintFactory#cumulative(IntVar[], int[],
     * int[], int)
     */
    default void cumulativeTimeDec(IntVar[] starts, int[] durations, int[] heights, int capacity) {
        int n = starts.length;
        // 1. find range of 't' parameters while creating variables
        int min_t = MAX_VALUE, max_t = MIN_VALUE;
        for (int i = 0; i < n; i++) {
            min_t = min(min_t, starts[i].getLB());
            max_t = max(max_t, starts[i].getUB() + durations[i]);
        }
        for (int t = min_t; t <= max_t; t++) {
            BoolVar[] bit = ref().boolVarArray(format("b_%s_", t), n);
            for (int i = 0; i < n; i++) {
                ref().addClausesBoolAndArrayEqVar(
                        new BoolVar[]{
                                ref().isLeq(starts[i], t),
                                ref().isGeq(starts[i], t - durations[i] + 1)
                        },
                        bit[i]);
            }
            ref().scalar(
                    bit,
                    Arrays.stream(heights, 0, n).toArray(),
                    "<=",
                    capacity
            ).post();
        }
    }

    /**
     * Creates and <b>posts</b> a decomposition of a cumulative constraint: associates a boolean
     * variable to each task and each point of time sich that the scalar product of boolean
     * variables per heights for each time never exceed capacity.
     *
     * @param tasks    set of tasks
     * @param heights  resource consumption of each task
     * @param capacity resource capacity
     * @see org.chocosolver.solver.constraints.IIntConstraintFactory#cumulative(IntVar[], int[],
     * int[], int)
     */
    default void cumulativeDec(Task[] tasks, IntVar[] heights, IntVar capacity) {
        cumulativeDec(
                Arrays.stream(tasks).map(Task::getStart).toArray(IntVar[]::new),
                Arrays.stream(tasks).map(Task::getDuration).toArray(IntVar[]::new),
                heights,
                capacity);
    }

    /**
     * Creates and <b>posts</b> a decomposition of a cumulative constraint: associates a boolean
     * variable to each task and each point of time sich that the scalar product of boolean
     * variables per heights for each time never exceed capacity.
     *
     * @param starts    starting time of each task
     * @param durations processing time of each task
     * @param heights   resource consumption of each task
     * @param capacity  resource capacity
     * @see org.chocosolver.solver.constraints.IIntConstraintFactory#cumulative(IntVar[], int[],
     * int[], int)
     */
    default void cumulativeDec(IntVar[] starts, IntVar[] durations, IntVar[] heights, IntVar capacity) {
        int n = starts.length;
        // 1. find range of 't' parameters while creating variables
        int min_t = MAX_VALUE, max_t = MIN_VALUE;
        for (int i = 0; i < n; i++) {
            min_t = min(min_t, starts[i].getLB());
            max_t = max(max_t, starts[i].getUB() + durations[i].getUB());
            if (max_t - min_t > 5000) {
                break;
            }
        }
        if (max_t - min_t > 5000) {
            for (int j = 0; j < n; j++) {
                BoolVar[] bit = ref().boolVarArray(format("b_%s_", j), n);
                IntVar[] hs = new IntVar[n];
                for (int i = 0; i < n; i++) {
                    BoolVar b1 = ref().boolVar(String.format("%s ≤ %s", starts[i].getName(), starts[j].getName()));
                    ref().reifyXleY(starts[i], starts[j], b1);
                    BoolVar b2 = ref().boolVar(String.format("%s < %s + %s",
                            starts[j].getName(), starts[i].getName(), durations[i].getName()));
                    ref().scalar(new IntVar[]{starts[j], starts[i]}, new int[]{1, -1}, "<", durations[i]).reifyWith(b2);
                    ref().addClausesBoolAndArrayEqVar(new BoolVar[]{b1, b2}, bit[i]);
                    hs[i] = ref().intVar("nH" + i + "_" + j, 0, heights[i].getUB());
                    ref().times(bit[i], heights[i], hs[i]).post();
                }
                ref().sum(hs, "<=", capacity).post();
            }
        } else {
            for (int t = min_t; t <= max_t; t++) {
                BoolVar[] bit = ref().boolVarArray(format("b_%s_", t), n);
                IntVar[] hs = new IntVar[n];
                for (int i = 0; i < n; i++) {
                    BoolVar b1 = ref().isLeq(starts[i], t);
                    BoolVar b2 = ref().boolVar(String.format("(%d < %s + %s)", t, starts[i].getName(), durations[i].getName()));
                    ref().reifyXgtYC(starts[i], ref().intView(-1, durations[i], t), 0, b2);
                    ref().addClausesBoolAndArrayEqVar(new BoolVar[]{b1, b2}, bit[i]);
                    hs[i] = ref().intVar("nH" + i + "_" + t, 0, heights[i].getUB());
                    ref().times(bit[i], heights[i], hs[i]).post();
                }
                ref().sum(hs, "<=", capacity).post();
            }
        }
    }


    /**
     * Creates an element constraint: value = matrix[rowIndex-offset][colIndex-colOffset]
     *
     * @param value     an integer variable taking its value in matrix
     * @param matrix    a matrix of integer values
     * @param rowIndex  index of the selected row
     * @param rowOffset offset for row index
     * @param colIndex  index of the selected column
     * @param colOffset offset for column index
     */
    default void element(IntVar value, int[][] matrix, IntVar rowIndex, int rowOffset, IntVar colIndex, int colOffset) {
        int d0 = matrix.length;
        int d1 = matrix[0].length;
        // Create the index as idx = row * d1 + col
        IntVar rv = ref().intView(1, rowIndex, -rowOffset);
        IntVar cv = ref().intView(1, colIndex, -colOffset);
        IntVar idx = ref().intVar(0, d0 * d1 - 1);
        ref().scalar(new IntVar[]{rv, cv}, new int[]{d1, 1}, "=", idx).post();
        // flatten the array
        int[] mvars = ArrayUtils.flatten(matrix);
        // post the element constraint
        ref().element(value, mvars, idx, 0).post();
    }

    /**
     * Creates an element constraint: value = matrix[rowIndex-offset][colIndex-colOffset]
     *
     * @param value     an integer variable taking its value in matrix
     * @param matrix    a matrix of integer variables
     * @param rowIndex  index of the selected row
     * @param rowOffset offset for row index
     * @param colIndex  index of the selected column
     * @param colOffset offset for column index
     */
    default void element(IntVar value, IntVar[][] matrix, IntVar rowIndex, int rowOffset, IntVar colIndex, int colOffset) {
        int d0 = matrix.length;
        int d1 = matrix[0].length;
        // Create the index as idx = row * d1 + col
        IntVar rv = ref().intView(1, rowIndex, -rowOffset);
        IntVar cv = ref().intView(1, colIndex, -colOffset);
        IntVar idx = ref().intVar(0, d0 * d1 - 1);
        ref().scalar(new IntVar[]{rv, cv}, new int[]{d1, 1}, "=", idx).post();
        // flatten the array
        IntVar[] mvars = ArrayUtils.flatten(matrix);
        // post the element constraint
        ref().element(value, mvars, idx, 0).post();
    }


    /**
     * Creates a global cardinality constraint (GCC):
     * Each value values[i] should be taken by exactly occurrences[i] variables of vars.
     * <br/>
     * This constraint does not ensure any well-defined level of consistency, yet.
     *
     * @param vars        collection of variables
     * @param values      collection of constrained values
     * @param occurrences collection of cardinality variables
     * @param closed      restricts domains of vars to values if set to true
     */
    default void globalCardinalityDec(IntVar[] vars, IntVar[] values, IntVar[] occurrences, boolean closed) {
        assert values.length == occurrences.length;
        for (int i = 0; i < values.length; i++) {
            ref().count(values[i], vars, occurrences[i]).post();
        }
        if (closed) {
            SetVar svars = ref().setVar(new int[]{},
                    Arrays.stream(vars)
                            .flatMapToInt(IntVar::stream)
                            .boxed()
                            .collect(Collectors.toSet())
                            .stream().mapToInt(i -> i)
                            .sorted().toArray());
            SetVar svalues = ref().setVar(new int[]{},
                    Arrays.stream(values)
                            .flatMapToInt(IntVar::stream)
                            .boxed()
                            .collect(Collectors.toSet())
                            .stream().mapToInt(i -> i)
                            .sorted().toArray());
            ref().subsetEq(svars, svalues).post();
        }
    }

    /**
     * Creates and <b>posts</b> a decomposition of a regular constraint.
     * Enforces the sequence of vars to be a word
     * recognized by the deterministic finite automaton.
     * For example regexp = "(1|2)(3*)(4|5)";
     * The same dfa can be used for different propagators.
     *
     * @param vars      sequence of variables
     * @param automaton a deterministic finite automaton defining the regular language
     * @return array of variables that encodes the states, which can optionally be constrained too.
     */
    @SuppressWarnings("UnusedReturnValue")
    default IntVar[] regularDec(IntVar[] vars, IAutomaton automaton) {
        int n = vars.length;
        IntVar[] states = new IntVar[n + 1];
        TIntHashSet[] layer = new TIntHashSet[n + 1];
        for (int i = 0; i <= n; i++) {
            layer[i] = new TIntHashSet();
        }
        layer[0].add(automaton.getInitialState());
        states[0] = ref().intVar("Q_" + ref().nextId(), layer[0].toArray());
        TIntHashSet nexts = new TIntHashSet();
        for (int i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            Tuples tuples = new Tuples(true);
            for (int j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                TIntIterator layerIter = layer[i].iterator();
                while (layerIter.hasNext()) {
                    int k = layerIter.next();
                    nexts.clear();
                    automaton.delta(k, j, nexts);
                    for (TIntIterator it = nexts.iterator(); it.hasNext(); ) {
                        int succ = it.next();
                        if (i + 1 < n || automaton.isFinal(succ)) {
                            layer[i + 1].add(succ);
                            tuples.add(k, succ, j);
                        }
                    }
                }
            }
            states[i + 1] = ref().intVar("Q_" + ref().nextId(), layer[i + 1].toArray());
            ref().table(new IntVar[]{states[i], states[i + 1], vars[i]}, tuples, "CT+").post();
        }
        return states;
    }

    /**
     * Creates and <b>posts</b> a decomposition of a bin packing constraint. Bin Packing
     * formulation: forall b in [0,binLoad.length-1], load[b]=sum(w[i] | i in [0,w.length-1], bin[i]
     * = b+offset) forall i in [0,w.length-1], bin is in [offset,load.length-1+offset],
     *
     * @param bin    IntVar representing the bin of each item
     * @param w      int representing the size of each item
     * @param load   IntVar representing the load of each bin (i.e. the sum of the size of the items
     *               in it)
     * @param offset 0 by default but typically 1 if used within MiniZinc (which counts from 1 to n
     *               instead of from 0 to n-1)
     */
    default void binPackingDec(IntVar[] bin, int[] w, IntVar[] load, int offset) {
        ref().sum(load, "=", Arrays.stream(w).sum()).post();
        for (int i = 0; i < bin.length; i++) {
            ref().member(bin[i], offset, load.length - 1 + offset).post();
        }
        for (int i = 0; i < load.length; i++) {
            BoolVar[] in = new BoolVar[bin.length];
            for (int j = 0; j < bin.length; j++) {
                in[j] = ref().isEq(bin[j], i + offset);
            }
            ref().scalar(in, w, "=", load[i]).post();
        }
    }

    /**
     * <p>
     * Creates and posts a decomposition of the {@link IIntConstraintFactory#circuit(IntVar[], int)} constraint.
     * </p>
     * <p>
     * It relies on two {@link IIntConstraintFactory#allDifferent(IntVar[], String)} constraints and some
     * {@link IIntConstraintFactory#element(IntVar, IntVar[], IntVar, int)} constraints.
     * </p>
     *
     * @param S      successors variables
     * @param offset 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     */
    default void circuitDec(IntVar[] S, int offset) {
        int n = S.length;
        ref().allDifferent(S, "AC").post();
        IntVar[] t = ref().intVarArray("t", n - 1, offset + 1, n + offset -1 );
        ref().allDifferent(t, "AC").post();
        ref().arithm(t[0], "=", S[0]).post();
        for (int i = 1; i < n - 2; i++) {
            ref().element(t[i], S, t[i - 1], offset).post();
        }
        ref().element(ref().intVar(offset), S, t[n - 2], offset).post();
    }

    /**
     * Creates a decomposition of the Argmax constraint.
     * z is the index of the maximum value of the collection of domain variables vars.
     *
     * @param z      a variable
     * @param offset offset wrt to 'z'
     * @param vars   a vector of variables, of size > 0
     */
    default void argmaxDec(IntVar z, int offset, IntVar[] vars) {
        int n = vars.length;
        //noinspection OptionalGetWithoutIsPresent
        int min = Stream.of(vars).mapToInt(IntVar::getLB).min().getAsInt();
        int max = Stream.of(vars).mapToInt(IntVar::getUB).max().getAsInt();
        IntVar[] q = new IntVar[n];
        IntVar M = ref().intVar("M", n * min, n * (max + 1));
        z.ge(offset).post();
        z.lt(vars.length + offset).post();
        for (int j = 0; j < n; j++) {
            q[j] = ref().intView(n, vars[j], n - j);
            z.ne(j + offset).iff(M.gt(q[j])).post();
        }
        ref().max(M, q).post();
    }

    /**
     * Creates a decomposition of the Argmin constraint.
     * z is the index of the minimum value of the collection of domain variables vars.
     *
     * @param z      a variable
     * @param offset offset wrt to 'z'
     * @param vars   a vector of variables, of size > 0
     */
    default void argminDec(IntVar z, int offset, IntVar[] vars) {
        int n = vars.length;
        //noinspection OptionalGetWithoutIsPresent
        int min = Stream.of(vars).mapToInt(IntVar::getLB).min().getAsInt();
        int max = Stream.of(vars).mapToInt(IntVar::getUB).max().getAsInt();
        IntVar[] q = new IntVar[n];
        IntVar M = ref().intVar("M", n * min, n * (max + 1));
        z.ge(offset).post();
        z.lt(vars.length + offset).post();
        for (int j = 0; j < n; j++) {
            q[j] = ref().intView(n, vars[j], j);
            z.ne(j + offset).iff(M.lt(q[j])).post();
        }
        ref().min(M, q).post();
    }

    /**
     * <p>
     * Creates a decomposition that encodes an "if-then-else" constraint.
     * </p>
     * <p>
     * If c[0] then y = x[0]
     * <br>else if c[1] then y = x[1]
     * <br>...
     * <br>else y is not constrained.
     * </p>
     *
     * @param c array of boolean variables
     * @param x array of ints
     * @param y a integer variable
     * @implNote This is encoded thanks to a table constraint.
     */
    default void ifThenElseDec(BoolVar[] c, int[] x, IntVar y) {
        Tuples tuples = new Tuples();
        int star = Math.max(2, y.getUB() + 1);
        tuples.setUniversalValue(star);
        int[] t = new int[c.length + 1];
        Arrays.fill(t, 0);
        t[c.length] = star;
        tuples.add(t.clone());
        Arrays.fill(t, star);
        for (int i = 0; i < c.length; i++) {
            if (i > 0) t[i - 1] = 0;
            t[i] = 1;
            t[c.length] = x[i];
            tuples.add(t.clone());
        }
        ref().table(ArrayUtils.append(c, new IntVar[]{y}), tuples).post();
    }

    /**
     * <p>
     * Creates a decomposition that encodes an "if-then-else" constraint.
     * </p>
     * <p>
     * If c[0] then y = x[0]
     * <br>else if c[1] then y = x[1]
     * <br>...
     * <br>else y is not constrained.
     * </p>
     *
     * @param c array of boolean variables
     * @param x array of integer variables
     * @param y a integer variable
     * @implNote This introduces an additional variable
     * and is based on a table constraint and an element constraint.
     */
    default void ifThenElseDec(BoolVar[] c, IntVar[] x, IntVar y) {
        /*
        BoolVar[] d = ref().boolVarArray(c.length);
        d[0] = ref().boolVar(true);
        //y.eq(x[0]).decompose().impliedBy(c[0]);
        c[0].imp(y.eq(x[0])).post();
        for (int i = 1; i < c.length; i++) {
            d[i].eq(c[i - 1].not().and(d[i - 1])).post();
            //y.eq(x[i]).decompose().impliedBy(c[i].and(d[i]).boolVar());
            c[i].and(d[i]).imp(y.eq(x[i])).post();
        }/*/
        Tuples tuples = new Tuples();
        int univ = Math.max(2, y.getUB() + 1);
        tuples.setUniversalValue(univ);
        int[] t = new int[c.length + 1];
        Arrays.fill(t, 0);
        t[c.length] = c.length;
        tuples.add(t.clone());
        Arrays.fill(t, univ);
        for (int i = 0; i < c.length; i++) {
            if (i > 0) t[i - 1] = 0;
            t[i] = 1;
            t[c.length] = i;
            tuples.add(t.clone());
        }
        IntVar idx = ref().intVar(0, c.length);
        ref().table(ArrayUtils.append(c, new IntVar[]{idx}), tuples).post();
        ref().element(y, ArrayUtils.append(x, new IntVar[]{y}), idx, 0).post();
        //*/
    }

    /**
     * Matrix multiplication A x B = C.
     *
     * @param A a m x n matrix
     * @param B a n x p matrix
     * @param C a m x p matrix
     */
    default void product(IntVar[][] A, IntVar[][] B, IntVar[][] C) {
        assert A.length > 0 && B.length > 0 && C.length > 0;
        assert A[0].length > 0 && B[0].length > 0 && C[0].length > 0;
        assert A[0].length == B[0].length;
        assert A.length == C.length;
        assert B[0].length == C[0].length;
        int n = B.length;
        int m = C.length;
        int p = C[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                int finalI = i;
                int finalJ = j;
                ref().sum(IntStream.range(0, n)
                                .mapToObj(k -> A[finalI][k].mul(B[k][finalJ]).intVar())
                                .toArray(IntVar[]::new),
                        "=", C[i][j]).post();
            }
        }
    }

    default void product(BoolVar[][] A, BoolVar[][] B, BoolVar[][] C) {
        assert A.length > 0 && B.length > 0 && C.length > 0;
        assert A[0].length > 0 && B[0].length > 0 && C[0].length > 0;
        assert A[0].length == B[0].length;
        assert A.length == C.length;
        assert B[0].length == C[0].length;
        int n = B.length;
        int m = C.length;
        int p = C[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                int finalI = i;
                int finalJ = j;
                ref().addClausesBoolOrArrayEqVar(
                        IntStream.range(0, n)
                                .mapToObj(k -> A[finalI][k].and(B[k][finalJ]).boolVar())
                                .toArray(BoolVar[]::new)
                        , C[i][j]);
            }
        }
    }

    /**
     * A decomposition for the cost flow constraint.
     * <p>
     * The network is defined by a set of arc, each of them is made of
     * a starting node,
     * an ending node,
     * a supply (if positive) -- or demand (if negative),
     * a unit cost and
     * a flow variable that stores the quantity that goes on the arc.
     * </p>
     *
     * <p>
     * Since each arc comes with a cost and a flow that goes through it, a global cost of the total flow is defined.
     * </p>
     *
     * @param starts    list of starting nodes, one per arc
     * @param ends      ending nodes, one per arc
     * @param supplies  supplies, one per arc
     * @param unitCosts unit cost, one per arc
     * @param flows     amount flow, one per arc
     * @param cost      cost of the flow
     * @param offset    index of the smallest node
     */
    default void costFlow(int[] starts, int[] ends, int[] supplies, int[] unitCosts, IntVar[] flows, IntVar cost, int offset) {
        // cost function
        ref().scalar(flows, unitCosts, "=", cost).post();
        for (int i = 0; i < supplies.length; i++) {
            int io = i + offset;
            List<IntVar> src = new ArrayList<>();
            List<IntVar> snk = new ArrayList<>();
            for (int j = 0; j < starts.length; j++) {
                if (starts[j] == io) {
                    src.add(flows[j]);
                }
                if (ends[j] == io) {
                    snk.add(flows[j]);
                }
            }
            snk.add(ref().intVar(supplies[i]));
            ref().sum(src.toArray(new IntVar[0]), "=", snk.toArray(new IntVar[0])).post();
        }
        new Constraint("", new PropMinCostMaxFlow(starts, ends, supplies, unitCosts, flows, cost, offset)).post();
    }

    /**
     * Creates a decomposed version of tje intValuePrecedeChain(X, S, T) constraint.
     * Ensure that if there exists <code>j</code> such that X[j] = T, then, there must exist <code>i</code> < <code>j</code> such that
     * X[i] = S.
     *
     * @param X an array of variables
     * @param S a value
     * @param T another value
     */
    default void intValuePrecedeChainDec(IntVar[] X, int S, int T) {
        Model model = X[0].getModel();
        model.arithm(X[0], "!=", T).post();
        for (int j = 1; j < X.length; j++) {
            BoolVar bj = model.arithm(X[j], "=", T).reify();
            BoolVar[] bis = new BoolVar[j];
            for (int i = 0; i < j; i++) {
                bis[i] = model.arithm(X[i], "=", S).reify();
            }
            model.ifThen(bj, model.or(bis));
        }
    }

    /**
     * Creates a decomposed version of the intValuePrecedeChain(X, V) constraint.
     * Ensure that, for each pair of V[k] and V[l] of values in V, such that k < l,
     * if there exists <code>j</code> such that X[j] = V[l], then, there must exist <code>i</code> < <code>j</code> such that
     * X[i] = V[k].
     *
     * @param X array of variables
     * @param V array of (distinct) values
     */
    default void intValuePrecedeChainDec(IntVar[] X, int[] V) {
        if (V.length > 1) {
            TIntHashSet values = new TIntHashSet();
            values.add(V[0]);
            for (int i = 1; i < V.length; i++) {
                if (values.contains(V[i])) {
                    throw new SolverException("\"int_value_precede\" requires V to be made of distinct values");
                }
                values.add(V[i]);
                intValuePrecedeChainDec(X, V[i - 1], V[i]);
            }
        }
    }

}
