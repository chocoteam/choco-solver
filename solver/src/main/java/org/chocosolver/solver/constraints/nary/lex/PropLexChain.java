/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.lex;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Solver constraint of the LexChain constraint.
 * Allows to sort lexical chain with strict lexicographic ordering or not.
 * <br/>
 *
 * @author Ashish
 * @author Charles Prud'homme
 * @since 09/08/11
 */
public class PropLexChain extends Propagator<IntVar> {

    // the number of variables in each vector of the chain - v1 <= lexChainEq/lexChain <= v2 .....
    private final int N;

    // total number of vector in the  lex chain constraint
    private final int M;

    // array for holding lexicographically largest feasible  upper bound of each vector
    private final int[][] UB;

    // array for holding lexicographically smallest  feasible  lower bound of each vector
    private final int[][] LB;

    // If strict's value is true then  lexChain  is implemented  , if false lexChainEq
    private final boolean strict;

    // array of vectors in the lex chain constraint
    private final IntVar[][] x;

    public PropLexChain(IntVar[][] variables, boolean strict) {
        super(ArrayUtils.flatten(variables), PropagatorPriority.LINEAR, true);
        M = variables.length;
        this.N = variables[0].length;
        this.x = new IntVar[M][N];
        int p = 0;
        for (int i = 0; i < M; i++) {
            System.arraycopy(vars, p, x[i], 0, N);
            p += N;
        }
        this.strict = strict;
        UB = new int[M][N];
        LB = new int[M][N];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < N; i++) {
                UB[M - 1][i] = x[M - 1][i].getUB();
            }

            for (int i = M - 2; i >= 0; i--) {
                computeUB(x[i], UB[i + 1], UB[i]);
            }

            for (int i = 0; i < N; i++) {
                LB[0][i] = x[0][i].getLB();
            }

            for (int i = 1; i < M; i++) {
                computeLB(x[i], LB[i - 1], LB[i]);
            }
        }
        for (int i = 0; i < M; i++) {
            boundsLex(LB[i], x[i], UB[i]);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int vec_idx = idxVarInProp % M;
        if (IntEventType.isDecupp(mask)) {
            for (int i = 0; i < N; i++) {
                UB[vec_idx][i] = x[vec_idx][i].getUB();
            }
            for (int i = vec_idx - 1; i >= 0; i--) {
                computeUB(x[i], UB[i + 1], UB[i]);
            }
        }
        if (IntEventType.isInclow(mask)) {
            for (int i = 0; i < N; i++) {
                LB[vec_idx][i] = x[vec_idx][i].getLB();
            }

            for (int i = vec_idx + 1; i < M; i++) {
                computeLB(x[i], LB[i - 1], LB[i]);
            }
        }
        forcePropagate(PropagatorEventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(checkTuple(0));
        }
        return ESat.UNDEFINED;
    }

    /**
     * check the feasibility of a tuple, recursively on each pair of consecutive vectors.
     * Compare vector xi with vector x(i+1):
     * return false if xij > x(i+1)j or if (strict && xi=x(i+1)), and checkTuple(i+1, tuple) otherwise.
     *
     * @param i the index of the first vector to be considered
     * @return true iff lexChain(xi,x(i+1)) && lexChain(x(i+1),..,xk)
     */
    private boolean checkTuple(int i) {
        if (i == x.length - 1) return true;
        int index = N * i;
        for (int j = 0; j < N; j++, index++) {
            if (vars[index].getValue() > vars[index + N].getValue())
                return false;
            if (vars[index].getValue() < vars[index + N].getValue())
                return checkTuple(i + 1);
        }
        return (!strict) && checkTuple(i + 1);
    }


    /////////////////////////////

    /**
     * Filtering algorithm for between(a,x,b)
     * Ensures that x is  lexicographically greater than  a and less than  b if strict is false
     * otherwise  x is  lexicographically greater than or equal to    a and less than or equal to   b
     *
     * @param a lexicographically smallest feasible lower bound
     * @param x the vector of variables among other vectors in the chain of vectors
     * @param b lexicographically largest feasible  upper bound
     * @throws ContradictionException
     */
    private void boundsLex(int[] a, IntVar[] x, int[] b) throws ContradictionException {
        int i = 0;
        while (i < N && a[i] == b[i]) {
            x[i].updateBounds(a[i], b[i], this);
            i++;
        }
        if (i < N) {
            x[i].updateBounds(a[i], b[i], this);
        }
        if (i == N || x[i].nextValue(a[i]) < b[i]) {
            return;
        }
        i++;
        while (i < N && x[i].getLB() == b[i] && x[i].getUB() == a[i]) {
            if (x[i].hasEnumeratedDomain()) {
                x[i].removeInterval(b[i] + 1, a[i] - 1, this);
            }
            i++;
        }
        if (i < N) {
            if (x[i].hasEnumeratedDomain()) {
                x[i].removeInterval(b[i] + 1, a[i] - 1, this);
            }
        }
    }

    /**
     * computes alpha for use in computing lexicographically largest feasible upper  bound  of x in
     * {@link  PropLexChain#computeUB(IntVar[], int[], int[]) computUB}
     *
     * @param x the vector of  variables whose  lexicographically largest feasible  upper bound is to be computed
     * @param b the vector of integers claimed  to be the feasible upper  bound
     * @return an integer greater than or equal to  -1 which is used in the computation of   lexicographically smallest  feasible upper bound vector of integers of x
     * @throws ContradictionException
     */
    private int computeAlpha(IntVar[] x, int[] b) throws ContradictionException {
        int i = 0;
        int alpha = -1;
        while (i < N && x[i].contains(b[i])) {
            if (b[i] > x[i].getLB()) {
                alpha = i;
            }
            i++;
        }
        if (!strict) {
            if (i == N || b[i] > x[i].getLB()) {
                alpha = i;
            }
        } else {
            if (i < N && b[i] > x[i].getLB()) {
                alpha = i;
            }
        }
        return alpha;
    }

    /**
     * computes beta for use in computing lexicographically smallest feasible lower bound  of x in
     * {@link  PropLexChain#computeLB(IntVar[], int[], int[]) computeLB}
     *
     * @param x the vector of  variables whose  lexicographically smallest feasible lower bound is to be computed
     * @param a the vector of integers claimed  to be the feasible lower bound
     * @return an integer greater than or equal to  -1  which is used in the computation of   lexicographically smallest  feasible upper bound vector of integers of x
     * @throws ContradictionException
     */
    private int computeBeta(IntVar[] x, int[] a) throws ContradictionException {
        int i = 0;
        int beta = -1;
        while (i < N && x[i].contains(a[i])) {
            if (a[i] < x[i].getUB()) {
                beta = i;
            }
            i++;

        }
        if (!strict) {
            if (i == N || a[i] < x[i].getUB()) {
                beta = i;
            }
        } else {
            if (i < N && a[i] < x[i].getUB()) {
                beta = i;
            }
        }
        return beta;
    }

    /**
     * Computes the   lexicographically largest  feasible upper bound vector of integers of x .
     * if aplha computed in  {@link  PropLexChain#computeAlpha(IntVar[], int[]) computeAlpha} is -1 then
     * the current domain values  can't satisfy the constraint .So the current intantiations if any are dropped and fresh search is continued.
     *
     * @param x the vector of  variables whose  lexicographically largest feasible  upper bound is to be computed
     * @param b the vector of integers claimed  to be the feasible upper  bound
     * @param u lexicographically largest  feasible upper  bound  of x
     * @throws ContradictionException
     */
    private void computeUB(IntVar[] x, int[] b, int[] u) throws ContradictionException {
        int alpha = computeAlpha(x, b);
        if (alpha == -1) fails();
        for (int i = 0; i < N; i++) {
            if (i < alpha) {
                u[i] = b[i];
            } else if (i == alpha) {
                u[i] = x[i].previousValue(b[i]);

            } else {
                u[i] = x[i].getUB();

            }
        }
    }

    /**
     * Computes the   lexicographically smallest feasible  lower bound vector of integers of x .
     * if beta computed in  {@link  PropLexChain#computeBeta(IntVar[], int[]) computeBeta} is -1 then
     * the current domain values  can't satisfy the constraint .So the current intantiations if any are dropped and fresh search is continued.
     *
     * @param x     the vector of  variables whose  lexicographically smallest feasible
     *              lower bound is to be computed
     * @param a     the vector of integers claimed  to be the feasible lower bound
     * @param lower lexicographically smallest feasible lower bound   of x
     * @throws ContradictionException
     */
    private void computeLB(IntVar[] x, int[] a, int[] lower) throws ContradictionException {
        int beta = computeBeta(x, a);
        if (beta == -1) fails();
        for (int i = 0; i < N; i++) {
            if (i < beta) {
                lower[i] = a[i];
            } else if (i == beta) {
                lower[i] = x[i].nextValue(a[i]);

            } else {
                lower[i] = x[i].getLB();
            }
        }
    }
}
