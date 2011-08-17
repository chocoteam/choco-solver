/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.nary.lex;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

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
    public int N;

    // total number of vector in the  lex chain constraint
    public int M;

    // array for holding lexicographically largest feasible  upper bound of each vector
    public int[][] UB;

    // array for holding lexicographically smallest  feasible  lower bound of each vector
    public int[][] LB;

    // If strict's value is true then  lexChain  is implemented  , if false lexChainEq
    public boolean strict;

    // array of vectors in the lex chain constraint
    public IntVar[][] x;

    public PropLexChain(IntVar[][] vars, boolean strict, Solver solver, Constraint<IntVar, Propagator<IntVar>> constraint) {
        super(ArrayUtils.flatten(vars), solver, constraint, PropagatorPriority.LINEAR, false);
        this.x = vars.clone();
        this.strict = strict;
        this.N = vars[0].length;
        M = vars.length;
        UB = new int[M][N];
        LB = new int[M][N];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate() throws ContradictionException {
        filter();
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> intVarIRequest, int idxVarInProp, int mask) throws ContradictionException {
        if (getNbRequestEnqued() == 0) {
            filter();
        }
//        filter();
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
        for (int j = 0; j < N;j++, index++) {
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
     * @param j index of the vector x  in  the chain
     * @throws ContradictionException
     */
    public void boundsLex(int[] a, IntVar[] x, int[] b, int j) throws ContradictionException {
        int i = 0;
        while (i < N && a[i] == b[i]) {
            x[i].updateLowerBound(a[i], this);
            x[i].updateUpperBound(b[i], this);
            i++;
        }
        if (i < N) {
            x[i].updateLowerBound(a[i], this);
            x[i].updateUpperBound(b[i], this);
        }
        if (i == N || x[i].nextValue(a[i]) < b[i]) {
            return;
        }
        i += 1;
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
    public int computeAlpha(IntVar[] x, int[] b) throws ContradictionException {
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
    public int computeBeta(IntVar[] x, int[] a) throws ContradictionException {
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
    public void computeUB(IntVar[] x, int[] b, int[] u) throws ContradictionException {
        int alpha = computeAlpha(x, b);
        if (alpha == -1) this.contradiction(null, "");
        for (int i = 0; i < N;i++) {
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
    public void computeLB(IntVar[] x, int[] a, int[] lower) throws ContradictionException {
        int beta = computeBeta(x, a);
        if (beta == -1) this.contradiction(null, "");
        for (int i = 0; i < N;i++) {
            if (i < beta) {
                lower[i] = a[i];
            } else if (i == beta) {
                lower[i] = x[i].nextValue(a[i]);

            } else {
                lower[i] = x[i].getLB();

            }
        }
    }

    /**
     * Implements the main filtering algorithm by calling
     * {@link  PropLexChain#boundsLex(int[], IntVar[], int[], int) boundsLex} for
     * each vector in the chain.
     * If there is no variable aliasing then the fixed-point is reached in one run.
     *
     * @throws ContradictionException
     */
    public void filter() throws ContradictionException {

        for (int i = 0; i < N;i++) {
            UB[M - 1][i] = x[M - 1][i].getUB();
        }

        for (int i = M - 2; i >= 0; i--) {
            computeUB(x[i], UB[i + 1], UB[i]);
        }

        for (int i = 0; i < N;i++) {
            LB[0][i] = x[0][i].getLB();
        }

        for (int i = 1; i < M; i++) {
            computeLB(x[i], LB[i - 1], LB[i]);
        }

        for (int i = 0; i < M; i++) {
            boundsLex(LB[i], x[i], UB[i], i);
        }
    }
}
