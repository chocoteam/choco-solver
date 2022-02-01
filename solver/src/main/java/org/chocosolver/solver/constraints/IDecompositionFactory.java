/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
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
                                ref().intLeView(starts[i], t),
                                ref().intGeView(starts[i], t - durations[i] + 1)
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
     * Creates an element constraint: value = matrix[rowIndex-offset][colIndex-colOffset]
     *
     * @param value     an integer variable taking its value in matrix
     * @param matrix    a matrix of integer values
     * @param rowIndex  index of the selected row
     * @param rowOffset offset for row index
     * @param colIndex  index of the selected column
     * @param colOffset offset for column index
     */
    default IntVar[] element(IntVar value, int[][] matrix, IntVar rowIndex, int rowOffset, IntVar colIndex, int colOffset) {
        IntVar[] results = new IntVar[matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            int min = IntStream.of(matrix[r]).min().orElse(IntVar.MIN_INT_BOUND);
            int max = IntStream.of(matrix[r]).max().orElse(IntVar.MAX_INT_BOUND);
            results[r] = ref().intVar("val[" + r + "]", min, max);
            ref().element(results[r], matrix[r], colIndex, colOffset).post();
        }
        ref().element(value, results, rowIndex, rowOffset).post();
        return results;
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
    default IntVar[] element(IntVar value, IntVar[][] matrix, IntVar rowIndex, int rowOffset, IntVar colIndex, int colOffset) {
        IntVar[] results = new IntVar[matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            int min = Stream.of(matrix[r]).mapToInt(IntVar::getLB).min().orElse(IntVar.MIN_INT_BOUND);
            int max = Stream.of(matrix[r]).mapToInt(IntVar::getUB).max().orElse(IntVar.MAX_INT_BOUND);
            results[r] = ref().intVar("val[" + r + "]", min, max);
            ref().element(results[r], matrix[r], colIndex, colOffset).post();
        }
        ref().element(value, results, rowIndex, rowOffset).post();
        return results;
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
                in[j] = ref().intEqView(bin[j], i + offset);
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
        IntVar[] t = ref().intVarArray("t", n - 1, 1 + offset, n - 1 + offset);
        ref().allDifferent(t, "AC3").post();
        ref().element(t[0], S, ref().intVar(offset), 0).post();
        for (int i = 1; i < n - 2; i++) {
            ref().element(t[i], S, t[i - 1], 0).post();
        }
        ref().element(ref().intVar(offset), S, t[n - 2], 0).post();
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
        z.ge(0).post();
        z.lt(vars.length).post();
        for (int j = 0; j < n; j++) {
            q[j] = ref().intAffineView(n, vars[j], n - j);
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
        for (int j = 0; j < n; j++) {
            q[j] = ref().intAffineView(n, vars[j], j);
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
        Model model = C[0][0].getModel();
        for(int i = 0; i < m; i++){
            for(int j = 0; j < p; j++){
                int finalI = i;
                int finalJ = j;
                model.sum(IntStream.range(0, n)
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
        Model model = C[0][0].getModel();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                int finalI = i;
                int finalJ = j;
                model.addClausesBoolOrArrayEqVar(
                        IntStream.range(0, n)
                                .mapToObj(k -> A[finalI][k].and(B[k][finalJ]).boolVar())
                                .toArray(BoolVar[]::new)
                        , C[i][j]);
            }
        }
    }

}
