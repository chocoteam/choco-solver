/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sum;

import gnu.trove.map.hash.TIntIntHashMap;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.ternary.PropXplusYeqZ;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Arrays;

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
    public static Constraint reduce(IntVar[] VARS, Operator OPERATOR, IntVar SUM, int minCardForDecomposition) {
        int[] COEFFS = new int[VARS.length];
        Arrays.fill(COEFFS, 1);
        return reduce(VARS, COEFFS, OPERATOR, SUM, minCardForDecomposition);
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
    public static Constraint reduce(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, IntVar SCALAR,
                                    int minCardForDecomposition) {
        // 0. normalize data
        Model model = SCALAR.getModel();
        if (VARS.length > minCardForDecomposition) {
            int k = VARS.length;
            int d1 = (int) Math.sqrt(k);
            int d2 = k / d1 + (k % d1 == 0?0:1);
            IntVar[] intermVar = new IntVar[d1];
            IntVar[] copyV;
            int[] copyC;
            int[] bounds;
            for (int i = 0, z = 0; i < k; i += d2, z++) {
                int size = Math.min(i + d2, k);
                copyV = Arrays.copyOfRange(VARS, i, size);
                copyC = Arrays.copyOfRange(COEFFS, i, size);
                bounds = VariableUtils.boundsForScalar(copyV, copyC);
                intermVar[z] = model.intVar(bounds[0], bounds[1]);
                model.scalar(copyV, copyC, "=", intermVar[z]).post();
            }
            return model.sum(intermVar, OPERATOR.toString(), SCALAR);
        }
        IntVar[] NVARS;
        int[] NCOEFFS;
        long RESULT = 0;
        if (VariableUtils.isConstant(SCALAR)) {
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
        // a. first loop to detect constant and merge duplicate variable/coefficient
        TIntIntHashMap map = new TIntIntHashMap(NVARS.length, 1.5f, -1, -1);
        for (int i = 0; i < NVARS.length; i++) {
            if (VariableUtils.isConstant(NVARS[i])) {
                RESULT -= (long)NVARS[i].getValue() * NCOEFFS[i]; // potential overflow
                NCOEFFS[i] = 0;
            } else if (NCOEFFS[i] != 0) {
                int id = NVARS[i].getId();
                int pos = map.get(id);
                if (pos == -1) {
                    map.put(id, k);
                    NVARS[k] = NVARS[i];
                    NCOEFFS[k] = NCOEFFS[i];
                    k++;
                } else {
                    NCOEFFS[pos] += NCOEFFS[i];
                    NCOEFFS[i] = 0;
                }
            }
        }
        // b. second step to remove variable with coeff set to 0
        int _k = k;
        k = 0;
        long slb = 0, sub = 0;
        for (int i = 0; i < _k; i++) {
            if (NCOEFFS[i] != 0) {
                if(NCOEFFS[i]>0){
                    slb += (long)NVARS[i].getLB() * NCOEFFS[i]; // potential overflow
                    sub += (long)NVARS[i].getUB() * NCOEFFS[i]; // potential overflow
                }else{
                    slb += (long)NVARS[i].getUB() * NCOEFFS[i]; // potential overflow
                    sub += (long)NVARS[i].getLB() * NCOEFFS[i]; // potential overflow
                }
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
                    return RESULT == 0 ? model.trueConstraint() : model.falseConstraint();
                case NQ:
                    return RESULT != 0 ? model.trueConstraint() : model.falseConstraint();
                case LE:
                    return RESULT >= 0 ? model.trueConstraint() : model.falseConstraint();
                case LT:
                    return RESULT > 0 ? model.trueConstraint() : model.falseConstraint();
                case GE:
                    return RESULT <= 0 ? model.trueConstraint() : model.falseConstraint();
                case GT:
                    return RESULT < 0 ? model.trueConstraint() : model.falseConstraint();
                default:
                    throw new SolverException("Unexpected operator " + OPERATOR
                            + " (should be in {\"=\", \"!=\", \">\",\"<\",\">=\",\"<=\"})");
            }
        }
        if(slb < Integer.MIN_VALUE || slb> Integer.MAX_VALUE){
            throw new SolverException("Sum of lower bounds under/overflows. Consider reducing variables' domain to prevent this.");
        }
        if(sub < Integer.MIN_VALUE || sub > Integer.MAX_VALUE){
            throw new SolverException("Sum of upper bounds under/overflows. Consider reducing variables' domain to prevent this.");
        }
        if(RESULT< Integer.MIN_VALUE || RESULT> Integer.MAX_VALUE){
            throw new SolverException("RHS under/overflows. Consider reducing it to prevent this.");
        }
        if(RESULT - slb < Integer.MIN_VALUE || RESULT - slb > Integer.MAX_VALUE
          || sub - RESULT < Integer.MIN_VALUE || sub - RESULT> Integer.MAX_VALUE){
            throw new SolverException("Integer under/overflow detected. Consider reducing variables' domain and/or RHS to prevent this.");
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
            return selectSum(NVARS, NCOEFFS, OPERATOR, (int)RESULT, nbools);
        } else {
            return selectScalar(NVARS, NCOEFFS, OPERATOR, (int)RESULT);
        }
    }

    /**
     * Select the most relevant Sum constraint to return
     *
     * @param VARS     array of integer variables
     * @param COEFFS   array of integers
     * @param OPERATOR on operator
     * @param RESULT   an integer
     * @param nbools   number of boolean variables
     * @return a constraint
     */
    public static Constraint selectSum(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, int RESULT, int nbools) {
        // if the operator is "="
        // 4. detect and return small arity constraints
        Model s = VARS[0].getModel();
        switch (VARS.length) {
            case 1:
                if (COEFFS[0] == 1) {
                    return s.arithm(VARS[0], OPERATOR.toString(), RESULT);
                } else {
                    assert COEFFS[0] == -1;
                    return s.arithm(VARS[0], Operator.getFlip(OPERATOR.toString()), -RESULT);
                }
            case 2:
                if (COEFFS[0] == 1 && COEFFS[1] == 1) {
                    return s.arithm(VARS[0], "+", VARS[1], OPERATOR.toString(), RESULT);
                } else if (COEFFS[0] == 1 && COEFFS[1] == -1) {
                    return s.arithm(VARS[0], "-", VARS[1], OPERATOR.toString(), RESULT);
                } else if (COEFFS[0] == -1 && COEFFS[1] == 1) {
                    return s.arithm(VARS[1], "-", VARS[0], OPERATOR.toString(), RESULT);
                } else {
                    assert COEFFS[0] == -1 && COEFFS[1] == -1;
                    return s.arithm(VARS[0], "+", VARS[1], Operator.getFlip(OPERATOR.toString()), -RESULT);
                }
            case 3:
                if(RESULT == 0 && OPERATOR == Operator.EQ) {
                    // deal with X + Y = Z
                    if ((COEFFS[0] == 1 && COEFFS[1] == 1 && COEFFS[2] == -1)
                            || (COEFFS[0] == -1 && COEFFS[1] == -1 && COEFFS[2] == 1)) {
                        return new Constraint(ConstraintsName.SUM,
                                new PropXplusYeqZ(VARS[0], VARS[1], VARS[2]));
                    }
                    // deal with X + Z  = Y
                    if ((COEFFS[0] == 1 && COEFFS[1] == -1 && COEFFS[2] == 1)
                            || (COEFFS[0] == -1 && COEFFS[1] == 1 && COEFFS[2] == -1)) {
                        return new Constraint(ConstraintsName.SUM,
                                new PropXplusYeqZ(VARS[0], VARS[2], VARS[1]));
                    }
                    // deal with Y + Z  = X
                    if ((COEFFS[0] == -1 && COEFFS[1] == 1 && COEFFS[2] == 1)
                            || (COEFFS[0] == 1 && COEFFS[1] == -1 && COEFFS[2] == -1)) {
                        return new Constraint(ConstraintsName.SUM,
                                new PropXplusYeqZ(VARS[1], VARS[2], VARS[0]));
                    }
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
                Model model = VARS[0].getModel();
                if (nbools == VARS.length) {
                    if (model.getSettings().enableIncrementalityOnBoolSum(tmpV.length)) {
                        return new SumConstraint(new PropSumFullBoolIncr(model.toBoolVar(tmpV), b, OPERATOR, RESULT));
                    } else {
                        return new SumConstraint(new PropSumFullBool(model.toBoolVar(tmpV), b, OPERATOR, RESULT));
                    }
                }
                if (nbools == VARS.length - 1 && !tmpV[tmpV.length - 1].isBool() && COEFFS[VARS.length - 1] == -1) {
                    // the large domain variable is on the last idx
                    if (model.getSettings().enableIncrementalityOnBoolSum(tmpV.length)) {
                        return new SumConstraint(new PropSumBoolIncr(model.toBoolVar(Arrays.copyOf(tmpV, tmpV.length - 1)),
                                b, OPERATOR, tmpV[tmpV.length - 1], RESULT));

                    } else {
                        return new SumConstraint(new PropSumBool(model.toBoolVar(Arrays.copyOf(tmpV, tmpV.length - 1)),
                                b, OPERATOR, tmpV[tmpV.length - 1], RESULT));

                    }
                }
                return new SumConstraint( new PropSum(tmpV, b, OPERATOR, RESULT));
        }
    }

    /**
     * Select the most relevant ScalarProduct constraint to return
     *
     * @param VARS     array of integer variables
     * @param COEFFS   array of integers
     * @param OPERATOR on operator
     * @param RESULT   an integer
     * @return a constraint
     */
    public static Constraint selectScalar(IntVar[] VARS, int[] COEFFS, Operator OPERATOR, int RESULT) {
        Model s = VARS[0].getModel();
        if (VARS.length == 1 && OPERATOR == Operator.EQ) {
            return s.times(VARS[0], COEFFS[0], s.intVar(RESULT));
        }
        if (VARS.length == 2 && OPERATOR == Operator.EQ && RESULT == 0) {
            if (COEFFS[0] == 1) {
                return s.times(VARS[1], -COEFFS[1], VARS[0]);
            }
            if (COEFFS[0] == -1) {
                return s.times(VARS[1], COEFFS[1], VARS[0]);
            }
            if (COEFFS[1] == 1) {
                return s.times(VARS[0], -COEFFS[0], VARS[1]);
            }
            if (COEFFS[1] == -1) {
                return s.times(VARS[0], COEFFS[0], VARS[1]);
            }
        }
        if (Operator.EQ == OPERATOR && VARS[VARS.length - 1].hasEnumeratedDomain() && TuplesFactory.canBeTupled(Arrays.copyOf(VARS, VARS.length - 1))) {
            return s.table(VARS, TuplesFactory.scalar(Arrays.copyOf(VARS, VARS.length - 1), Arrays.copyOf(COEFFS, COEFFS.length - 1),
                    OPERATOR.toString(), VARS[VARS.length - 1], -COEFFS[COEFFS.length - 1], RESULT));
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
            RESULT++;
        } else if (OPERATOR == Operator.LT) {
            OPERATOR = Operator.LE;
            RESULT--;
        }
        return new SumConstraint(new PropScalar(tmpV, tmpC, b, OPERATOR, RESULT));
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
