/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.Arrays;

import static org.chocosolver.solver.constraints.ICF.*;

/**
 * A factory to reduce and detect specific cases related to integer linear combinations.
 * <p>
 * It aims at first reducing the input (merge coefficients) and then select the right implementation (for performance concerns).
 * <p>
 * 2015.09.24 (cprudhom)
 * <q>
 * dealing with tuples is only relevant for scalar in some very specific cases (eg. mzn 2014, elitserien+handball+handball14.fzn)
 * </q>
 * <p>
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 * @author Charles Prud'homme
 */
public class IntLinCombFactory {

    /**
     * Default algorithm for extension transformation
     */
    private static final String AC = "";

    private IntLinCombFactory() {
    }

    /**
     * Reduce coefficients, and variables if required, when dealing with a sum (all coefficients are implicitly equal to 1)
     *
     * @param VARS     array of integer variables
     * @param OPERATOR an operator among "=", "!=", ">", "<", ">=",>" and "<="
     * @param SUM      the resulting variable
     * @param SOLVER   declaring solver
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
     * @param SOLVER    declaring solver
     * @return a constraint to post or reify
     */
    public static Constraint reduce(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, IntVar SCALAR, Solver SOLVER) {
        // 0. normalize data
        IntVar[] NVARS;
        int[] NCOEFFS;
        int RESULT = 0;
        if (SCALAR.isInstantiated()) {
            RESULT = SCALAR.getValue();
            NVARS = VARS.clone();
            NCOEFFS = COEFFS.clone();
        } else {
            NVARS = new IntVar[VARS.length + 1];
            System.arraycopy(VARS, 0, NVARS, 0, VARS.length);
            NVARS[VARS.length] = SCALAR;
            NCOEFFS = new int[COEFFS.length + 1];
            System.arraycopy(COEFFS, 0, NCOEFFS, 0, COEFFS.length);
            NCOEFFS[COEFFS.length] = -1;
        }
        int k = 0;
        int nbools = 0;
        int nones = 0, nmones = 0;
        int ldom = 0, lidx = -1;
        // 1. reduce coefficients and variables
        // a. quadratic iteration in order to detect multiple occurrences of a variable
        for (int i = 0; i < NVARS.length; i++) {
            if (NVARS[i].isInstantiated()) {
                RESULT -= NVARS[i].getValue() * NCOEFFS[i];
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
                if (NVARS[k].getDomainSize() > ldom) {
                    lidx = k;
                    ldom = NVARS[k].getDomainSize();
                }
                k++;
            }
        }
        // b. resize arrays if needed
        if (k == 0) {
            switch (OPERATOR) {
                case EQ:
                    return RESULT == 0 ? SOLVER.TRUE() : SOLVER.FALSE();
                case NQ:
                    return RESULT != 0 ? SOLVER.TRUE() : SOLVER.FALSE();
                case LE:
                    return RESULT >= 0 ? SOLVER.TRUE() : SOLVER.FALSE();
                case LT:
                    return RESULT > 0 ? SOLVER.TRUE() : SOLVER.FALSE();
                case GE:
                    return RESULT <= 0 ? SOLVER.TRUE() : SOLVER.FALSE();
                case GT:
                    return RESULT < 0 ? SOLVER.TRUE() : SOLVER.FALSE();
            }
        }
        // 2. resize NVARS and NCOEFFS
        if (k < NVARS.length) {
            NVARS = Arrays.copyOf(NVARS, k, IntVar[].class);
            NCOEFFS = Arrays.copyOf(NCOEFFS, k);
        }
        // and move the variable with the largest domain at the end, it helps when considering extension representation
        if (ldom > 2 && lidx < k - 1) {
            IntVar t = NVARS[k - 1];
            NVARS[k - 1] = NVARS[lidx];
            NVARS[lidx] = t;
            int i = NCOEFFS[k - 1];
            NCOEFFS[k - 1] = NCOEFFS[lidx];
            NCOEFFS[lidx] = i;
        }
        if (nones + nmones == NVARS.length) {
            return selectSum(NVARS, NCOEFFS, OPERATOR, RESULT, SOLVER, nbools);
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
     * @return a constraint
     */
    public static Constraint selectSum(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, int RESULT, Solver SOLVER, int nbools) {
        // if the operator is "="
        // 4. detect and return small arity constraints
        switch (VARS.length) {
            case 1:
                if (COEFFS[0] == 1) {
                    return arithm(VARS[0], OPERATOR.toString(), RESULT);
                } else {
                    assert COEFFS[0] == -1;
                    return arithm(VARS[0], Operator.getFlip(OPERATOR.toString()), -RESULT);
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
                // go down to 0 to ensure that the largest domain variable is on last position
                for (int i = VARS.length - 1; i >= 0; i--) {
                    IntVar key = VARS[i];
                    if (COEFFS[i] > 0) {
                        tmpV[b++] = key;
                    } else if (COEFFS[i] < 0) {
                        tmpV[--e] = key;
                    }
                }
                if (OPERATOR == Operator.GT) {
                    OPERATOR = Operator.GE;
                    RESULT++;
                } else if (OPERATOR == Operator.LT) {
                    OPERATOR = Operator.LE;
                    RESULT--;
                }
                //TODO: deal with clauses and reification
                if (nbools == VARS.length) {
                    if (SOLVER.getSettings().enableIncrementalityOnBoolSum(tmpV.length)) {
                        return new Constraint("BoolSum", new PropSumBoolIncr(VF.toBoolVar(tmpV), b, OPERATOR,
                                VF.fixed(RESULT, SOLVER), 0));
                    } else {
                        return new Constraint("BoolSum", new PropSumBool(VF.toBoolVar(tmpV), b, OPERATOR,
                                VF.fixed(RESULT, SOLVER), 0));
                    }
                }
                if (nbools == VARS.length - 1 && !tmpV[tmpV.length - 1].isBool()) {
                    // the large domain variable is on the last idx
                    assert COEFFS[VARS.length - 1] == -1;
                    if (SOLVER.getSettings().enableIncrementalityOnBoolSum(tmpV.length)) {
                        return new Constraint("BoolSum", new PropSumBoolIncr(VF.toBoolVar(Arrays.copyOf(tmpV, tmpV.length - 1)),
                                b, OPERATOR, tmpV[tmpV.length - 1], RESULT));

                    } else {
                        return new Constraint("BoolSum", new PropSumBool(VF.toBoolVar(Arrays.copyOf(tmpV, tmpV.length - 1)),
                                b, OPERATOR, tmpV[tmpV.length - 1], RESULT));

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
        }
        if (VARS.length == 2 && OPERATOR == Operator.EQ && RESULT == 0) {
            if (COEFFS[0] == 1) {
                return times(VARS[1], -COEFFS[1], VARS[0]);
            }
            if (COEFFS[0] == -1) {
                return times(VARS[1], COEFFS[1], VARS[0]);
            }
            if (COEFFS[1] == 1) {
                return times(VARS[0], -COEFFS[0], VARS[1]);
            }
            if (COEFFS[1] == -1) {
                return times(VARS[0], COEFFS[0], VARS[1]);
            }
        }
        if (Operator.EQ == OPERATOR && VARS[VARS.length - 1].hasEnumeratedDomain() && tupleIt(Arrays.copyOf(VARS, VARS.length - 1))) {
            return table(VARS, TuplesFactory.scalar(Arrays.copyOf(VARS, VARS.length - 1), Arrays.copyOf(COEFFS, COEFFS.length - 1),
                    OPERATOR.toString(), VARS[VARS.length - 1], -COEFFS[COEFFS.length - 1], RESULT), AC);
        }
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
        if (OPERATOR == Operator.GT) {
            OPERATOR = Operator.GE;
            RESULT--;
        } else if (OPERATOR == Operator.LT) {
            OPERATOR = Operator.LE;
            RESULT++;
        }
        return new Constraint("ScalarProduct", new PropScalar(tmpV, tmpC, b, OPERATOR, RESULT));
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param vars array of integer variables
     * @param coefs array of ints
     * @return compute the bounds of the result of scalar product vars*coefs.
     */
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
