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
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.StringUtils;

import static org.chocosolver.solver.constraints.IntConstraintFactory.*;

/**
 * A factory dedicated to scalar products analyses and reductions.
 * <p/>
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public class ScalarFactory {
    ;

    private ScalarFactory() {
    }

    public static Constraint reduce(IntVar[] VARS, String OPERATOR, IntVar SUM) {
        if (VARS.length == 1) {
            if (SUM.isInstantiated()) {
                return arithm(VARS[0], OPERATOR, SUM.getValue());
            } else {
                return arithm(VARS[0], OPERATOR, SUM);
            }
        } else if (VARS.length == 2 && SUM.isInstantiated()) {
            return arithm(VARS[0], "+", VARS[1], OPERATOR, SUM.getValue());
        } else {
            int nbBools = 0;
            for (IntVar left : VARS) {
                if ((left.getTypeAndKind() & Variable.KIND) == Variable.BOOL) {
                    nbBools++;
                }
            }
            if (nbBools == VARS.length) {
                BoolVar[] bvars = new BoolVar[nbBools];
                for (int i = 0; i < nbBools; i++) {
                    bvars[i] = (BoolVar) VARS[i];
                }
                return sum(bvars, OPERATOR, SUM);
            }
            if (OPERATOR.equals("=")) {
                return new Constraint("Sum", new PropSumEq(VARS, SUM));
            }
            int lb = 0;
            int ub = 0;
            for (IntVar v : VARS) {
                lb += v.getLB();
                ub += v.getUB();
            }
            IntVar p = VF.bounded(StringUtils.randomName(), lb, ub, SUM.getSolver());
            SUM.getSolver().post(new Constraint("Sum", new PropSumEq(VARS, p)));
            return arithm(p, OPERATOR, SUM);
        }
    }


    public static Constraint reduce(IntVar[] VARS, int[] COEFFS, String OPERATOR, IntVar SCALAR) {
        // detect unaries and binaries
        if (VARS.length == 0) {
            return arithm(VF.fixed(0, SCALAR.getSolver()), OPERATOR, SCALAR);
        }
        if (COEFFS.length == 2 && SCALAR.isInstantiated()) {
            int c = SCALAR.getValue();
            if (COEFFS[0] == 1 && COEFFS[1] == 1) {
                return arithm(VARS[0], "+", VARS[1], OPERATOR, c);
            } else if (COEFFS[0] == 1 && COEFFS[1] == -1) {
                return arithm(VARS[0], "-", VARS[1], OPERATOR, c);
            } else if (COEFFS[0] == -1 && COEFFS[1] == 1) {
                return arithm(VARS[1], "-", VARS[0], OPERATOR, c);
            } else if (COEFFS[0] == -1 && COEFFS[1] == -1) {
                return arithm(VARS[0], "+", VARS[1], Operator.getFlip(OPERATOR), -c);
            }
        }
        // detect sums
        int n = VARS.length;
        int nbOne = 0;
        int nbMinusOne = 0;
        int nbZero = 0;
        for (int i = 0; i < n; i++) {
            if (COEFFS[i] == 1) {
                nbOne++;
            } else if (COEFFS[i] == -1) {
                nbMinusOne++;
            } else if (COEFFS[i] == 0) {
                nbZero++;
            }
        }
        if (nbZero > 0) {
            IntVar[] nonZerosVars = new IntVar[n - nbZero];
            int[] nonZerosCoefs = new int[n - nbZero];
            int k = 0;
            for (int i = 0; i < n; i++) {
                if (COEFFS[i] != 0) {
                    nonZerosVars[k] = VARS[i];
                    nonZerosCoefs[k] = COEFFS[i];
                    k++;
                }
            }
            return scalar(nonZerosVars, nonZerosCoefs, OPERATOR, SCALAR);
        }
        if (nbOne + nbMinusOne == n) {
            if (nbOne == n) {
                return sum(VARS, OPERATOR, SCALAR);
            } else if (nbMinusOne == n) {
                return sum(VARS, Operator.getFlip(OPERATOR), VF.minus(SCALAR));
            } else if (SCALAR.isInstantiated()) {
                if (nbMinusOne == 1) {
                    IntVar[] v2 = new IntVar[n - 1];
                    IntVar s2 = null;
                    int k = 0;
                    for (int i = 0; i < n; i++) {
                        if (COEFFS[i] != -1) {
                            v2[k++] = VARS[i];
                        } else {
                            s2 = VARS[i];
                        }
                    }
                    return sum(v2, OPERATOR, VF.offset(s2, SCALAR.getValue()));
                } else if (nbOne == 1) {
                    IntVar[] v2 = new IntVar[n - 1];
                    IntVar s2 = null;
                    int k = 0;
                    for (int i = 0; i < n; i++) {
                        if (COEFFS[i] != 1) {
                            v2[k++] = VARS[i];
                        } else {
                            s2 = VARS[i];
                        }
                    }
                    return sum(v2, Operator.getFlip(OPERATOR), VF.offset(s2, -SCALAR.getValue()));
                }
            } else if (n == 2) {
                if (COEFFS[0] == 1) {
                    assert COEFFS[1] == -1;
                    return sum(new IntVar[]{VARS[1], SCALAR}, Operator.getFlip(OPERATOR), VARS[0]);
                } else {
                    assert COEFFS[0] == -1;
                    assert COEFFS[1] == 1;
                    return sum(new IntVar[]{VARS[0], SCALAR}, Operator.getFlip(OPERATOR), VARS[1]);
                }
            }
        }
        // scalar
        if (OPERATOR.equals("=")) {
            return makeScalar(VARS, COEFFS, SCALAR, 1);
        }
        int[] b = Scalar.getScalarBounds(VARS, COEFFS);
        Solver s = VARS[0].getSolver();
        IntVar p = VF.bounded(StringUtils.randomName(), b[0], b[1], s);
        s.post(makeScalar(VARS, COEFFS, p, 1));
        return arithm(p, OPERATOR, SCALAR);
    }

    private static Constraint makeScalar(IntVar[] VARS, int[] COEFFS, IntVar SCALAR, int SCALAR_COEF) {
        int maxDomSize = SCALAR.getDomainSize();
        int idx = -1;
        int n = VARS.length;
        int nbBools = 0;
        for (int i = 0; i < n; i++) {
            if (maxDomSize < VARS[i].getDomainSize()) {
                maxDomSize = VARS[i].getDomainSize();
                idx = i;
            }
            if ((VARS[i].getTypeAndKind() & Variable.KIND) == Variable.BOOL) {
                nbBools++;
            }
        }
        if (idx != -1 && nbBools != VARS.length) {
            IntVar[] VARS2 = VARS.clone();
            int[] COEFFS2 = COEFFS.clone();
            VARS2[idx] = SCALAR;
            COEFFS2[idx] = -SCALAR_COEF;
            return makeScalar(VARS2, COEFFS2, VARS[idx], -COEFFS[idx]);
        } else {
            if (tupleIt(VARS) && SCALAR.hasEnumeratedDomain()) {
                return table(ArrayUtils.append(VARS, new IntVar[]{SCALAR}), TuplesFactory.scalar(VARS, COEFFS, SCALAR, SCALAR_COEF), "");
            } else {
                return new Scalar(VARS, COEFFS, SCALAR, SCALAR_COEF);
            }
        }
    }

}
