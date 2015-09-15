/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.Arrays;

import static org.chocosolver.solver.constraints.ICF.arithm;
import static org.chocosolver.solver.constraints.ICF.times;

/**
 * A factory to reduce and detect specific cases related to integer linear combinations.
 * <p>
 * It aims at first reducing the input (merge coefficients) and then select the right implementation (for performance concerns).
 * <p>
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public class IntLinCombFactory {

    private IntLinCombFactory() {
    }

    /**
     * Reduce coefficients, and variables if required, when dealing with a sum (all coefficients are implicitly equal to 1)
     *
     * @param VARS     array of integer variables
     * @param OPERATOR an operator among "=", "!=", ">", "<", ">=",>" and "<="
     * @param SUM      the resulting variable
     * @return a constraint to post or reify
     */
    public static Constraint reduce(IntVar[] VARS, Operator OPERATOR, IntVar SUM, Solver SOLVER) {
        int[] COEFFS = new int[VARS.length];
        Arrays.fill(COEFFS, 1);
        return reduce(VARS, COEFFS, OPERATOR, SUM, SOLVER);
    }


    /**
     * Reduce coefficients, and variables if required, when dealing with a scalar product
     *
     * @param VARS     array of integer variables
     * @param COEFFS   array of integers
     * @param OPERATOR an operator among "=", "!=", ">", "<", ">=",>" and "<="
     * @param SCALAR   the resulting variable
     * @return a constraint to post or reify
     */
    public static Constraint reduce(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, IntVar SCALAR, Solver SOLVER) {
        // 0. normalize data
        IntVar[] NVARS = new IntVar[VARS.length + 1];
        System.arraycopy(VARS, 0, NVARS, 0, VARS.length);
        NVARS[VARS.length] = SCALAR;
        int[] NCOEFFS = new int[COEFFS.length + 1];
        System.arraycopy(COEFFS, 0, NCOEFFS, 0, COEFFS.length);
        NCOEFFS[COEFFS.length] = -1;
        int RESULT = 0;
        int k = 0;
        int nbools = 0;
        int nones = 0, nmones = 0;
        // 1. reduce coefficients and variables
        // a. quadratic iteration in order to detect multiple occurrences of a variable
        for (int i = 0; i < NVARS.length; i++) {
            if (NVARS[i].isInstantiated()) {
                RESULT += NVARS[i].getValue() * NCOEFFS[i];
                NCOEFFS[i] = 0;
            } else if (NCOEFFS[i] != 0) {
                int id = NVARS[i].getId();
                for (int j = i + 1; j < NVARS.length; j++) {
                    if (NVARS[j].getId() == id) {
                        NCOEFFS[i] += NCOEFFS[j];
                        NCOEFFS[j] = 0;
                    }
                }
            }
            if (NCOEFFS[i] != 0) {
                if (NVARS[i].isBool()) nbools++; // count number of boolean variables
                if (NCOEFFS[i] == 1) nones++; // count number of coeff set to 1
                if (NCOEFFS[i] == -1) nmones++; // count number of coeff set to -1
                NVARS[k] = NVARS[i];
                NCOEFFS[k] = NCOEFFS[i];
                k++;
            }
        }
        if(k == 0) {
            throw new SolverException("Cannot create an integer linear combination constraint with no variable.");
        }
        // 2. resize NVARS and NCOEFFS
        if (k < NVARS.length) {
            NVARS = Arrays.copyOf(NVARS, k, IntVar[].class);
            NCOEFFS = Arrays.copyOf(NCOEFFS, k);
        }
        if (nones + nmones == NVARS.length) {
            return selectSum(NVARS, NCOEFFS, OPERATOR, RESULT, SOLVER, nbools, nones);
        } else {
            return selectScalar(NVARS, NCOEFFS, OPERATOR, RESULT, SOLVER);
        }
    }


    /**
     * Select the most relevant Sum constraint to return
     *
     * @param VARS     array of integer variables
     * @param COEFFS   array of integers
     * @param OPERATOR on operator
     * @param RESULT   an integer
     * @param SOLVER   the solver
     * @param nbools   number of boolean variables
     * @param nones    of coefficients set to one
     * @return a constraint
     */
    public static Constraint selectSum(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, int RESULT, Solver SOLVER, int nbools, int nones) {
        // if the operator is "="
        if (VARS.length > 2 && OPERATOR == Operator.EQ) {
            // if all variables are BoolVar
            if (nbools == VARS.length) {
                //TODO: deal with clauses and reification
                if (nones == VARS.length) {
                    if (VARS.length > 10) {
                        return new Constraint("BoolSum", new PropBoolSumIncremental(VF.toBoolVar(VARS), VF.fixed(RESULT, SOLVER)));
                    } else {
                        return new Constraint("BoolSum", new PropBoolSumCoarse(VF.toBoolVar(VARS), VF.fixed(RESULT, SOLVER)));
                    }
                } else if (nones == 0) {
                    if (VARS.length > 10) {
                        return new Constraint("BoolSum", new PropBoolSumIncremental(VF.toBoolVar(VARS), VF.fixed(-RESULT, SOLVER)));
                    } else {
                        return new Constraint("BoolSum", new PropBoolSumCoarse(VF.toBoolVar(VARS), VF.fixed(-RESULT, SOLVER)));
                    }
                }
            }
            // if at most one variable is not a boolean and variable one coefficients is different from the others
            if (nbools >= VARS.length - 1 && (nones == 1 || nones == VARS.length - 1) && RESULT == 0) {
                int w = 0;
                int c = nones == 1 ? 1 : -1;
                while (w < COEFFS.length && COEFFS[w] != c) {
                    w++;
                }
                if (w < VARS.length && (nbools == VARS.length || !VARS[w].isBool())) {
                    IntVar vtmp = VARS[VARS.length - 1];
                    VARS[VARS.length - 1] = VARS[w];
                    VARS[w] = vtmp;
                    int itmp = COEFFS[COEFFS.length - 1];
                    COEFFS[COEFFS.length - 1] = COEFFS[w];
                    COEFFS[w] = itmp;
                    if (VARS.length > 10) {
                        return new Constraint("BoolSum", new PropBoolSumIncremental(VF.toBoolVar(Arrays.copyOf(VARS, VARS.length - 1)), VARS[VARS.length - 1]));
                    } else {
                        return new Constraint("BoolSum", new PropBoolSumCoarse(VF.toBoolVar(Arrays.copyOf(VARS, VARS.length - 1)), VARS[VARS.length - 1]));
                    }
                }
            }
        }
        // 4. detect and return small arity constraints
        switch (VARS.length) {
            case 1:
                if (COEFFS[0] == 1) {
                    return arithm(VARS[0], OPERATOR.toString(), RESULT);
                } else {
                    assert COEFFS[0] == -1;
                    return arithm(VARS[0], OPERATOR.toString(), -RESULT);
                }
            case 2:
                if (COEFFS[0] == 1 && COEFFS[1] == 1) {
                    return arithm(VARS[0], "+", VARS[1], OPERATOR.toString(), RESULT);
                } else if (COEFFS[0] == 1 && COEFFS[1] == -1) {
                    return arithm(VARS[0], "-", VARS[1], OPERATOR.toString(), RESULT);
                } else if (COEFFS[0] == -1 && COEFFS[1] == 1) {
                    return arithm(VARS[1], "-", VARS[0], OPERATOR.toString(), RESULT);
                } else {
                    assert COEFFS[0] == -1 && COEFFS[1] == -1;
                    return arithm(VARS[0], "+", VARS[1], Operator.getFlip(OPERATOR.toString()), -RESULT);
                }
            default:
                int b = 0, e = VARS.length;
                IntVar[] tmpV = new IntVar[e];
                for (int i = 0; i < VARS.length; i++) {
                    IntVar key = VARS[i];
                    if (COEFFS[i] > 0) {
                        tmpV[b++] = key;
                    } else if (COEFFS[i] < 0) {
                        tmpV[--e] = key;
                    }
                }
                return new Constraint("Sum", new PropSum(tmpV, b, OPERATOR, RESULT));
        }
    }

    /**
     * Select the most relevant ScalarProduct constraint to return
     *
     * @param VARS     array of integer variables
     * @param COEFFS   array of integers
     * @param OPERATOR on operator
     * @param RESULT   an integer
     * @param SOLVER   the solver
     * @return a constraint
     */
    public static Constraint selectScalar(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, int RESULT, Solver SOLVER) {
        if (VARS.length == 1 && OPERATOR == Operator.EQ) {
            return times(VARS[0], COEFFS[0], VF.fixed(RESULT, SOLVER));
        } else {
            int b = 0, e = VARS.length;
            IntVar[] tmpV = new IntVar[e];
            int[] tmpC = new int[e];
            for (int i = 0; i < VARS.length; i++) {
                IntVar key = VARS[i];
                if (COEFFS[i] > 0) {
                    tmpV[b] = key;
                    tmpC[b++] = COEFFS[i];
                } else if (COEFFS[i] < 0) {
                    tmpV[--e] = key;
                    tmpC[e] = COEFFS[i];
                }
            }
            return new Constraint("ScalarProduct", new PropScalar(tmpV, tmpC, b, OPERATOR, RESULT));
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int[] getScalarBounds(IntVar[] vars, int[] coefs) {
        int[] ext = new int[2];
        for (int i = 0; i < vars.length; i++) {
            int min = Math.min(0, vars[i].getLB() * coefs[i]);
            min = Math.min(min, vars[i].getUB() * coefs[i]);
            int max = Math.max(0, vars[i].getLB() * coefs[i]);
            max = Math.max(max, vars[i].getUB() * coefs[i]);
            ext[0] += min;
            ext[1] += max;
        }
        return ext;
    }

}
