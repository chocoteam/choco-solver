/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.extension;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * A Factory to ease generation of tuples.
 * One may keep in mind that tuples generation directly depends on the product of domain cardinality, but also on the algorithm defines in the filter.
 * and thus may be time consuming!
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/2014
 */
public class TuplesFactory {

    TuplesFactory() {
    }


    /**
     * A method that generates all tuples from a set of variables and stores (and returns) the valid tuples wrt to the <code>filter</code>.
     * One may keep in mind that tuples generation directly depends on the product of domain cardinality, but also on the algorithm defines in the filter.
     *
     * @param filter   tuple validator
     * @param feasible are tuples feasible (or infeasible)
     * @param doms     domains
     * @return the valid tuples wrt to <code>filter</code>
     * @see <a href="http://stackoverflow.com/questions/8804852/generate-all-tuples-with-c-better-way-than-nested-loops">source</a>
     */
    public static Tuples generateTuples(TupleValidator filter, boolean feasible, int[]... doms) {
        Tuples tuples = new Tuples(feasible);
        int n = doms.length;
        int[] t = new int[n];
        int[] i = new int[n];
        for (int j = 0; j < n; j++) {
            t[j] = doms[j][0];
        }
        while (true) {
            if (filter.valid(t)) tuples.add(t.clone());
            int j;
            for (j = 0; j < n; j++) {
                i[j]++;
                if (i[j] < doms[j].length) {
                    t[j] = doms[j][i[j]];
                    break;
                }
                i[j] = 0;
                t[j] = doms[j][0];
            }
            if (j == n) break;
        }
        return tuples;

    }

    /**
     * A method that generates all tuples from a set of variables and stores (and returns) the valid tuples wrt to the <code>filter</code>.
     * One may keep in mind that tuples generation directly depends on the product of domain cardinality, but also on the algorithm defines in the filter.
     *
     * @param filter   tuple validator
     * @param feasible are tuples feasible (or infeasible)
     * @param vars     concerned variables
     * @return the valid tuples wrt to <code>filter</code>
     */
    public static Tuples generateTuples(TupleValidator filter, boolean feasible, IntVar... vars) {
        Tuples tuples = new Tuples(feasible);
        int n = vars.length;
        int[] cvalue = new int[n];
        int[] t = new int[n];
        for (int j = 0; j < n; j++) {
            t[j] = cvalue[j] = vars[j].getLB();
        }
        while (true) {
            if (filter.valid(t)) tuples.add(t.clone());
            int j;
            for (j = 0; j < n; j++) {
                int v = t[j] = cvalue[j] = vars[j].nextValue(cvalue[j]);
                if (v < Integer.MAX_VALUE) {
                    break;
                }
                t[j] = cvalue[j] = vars[j].getLB();
            }
            if (j == n) break;
        }
        return tuples;

    }

    // BEWARE: PLEASE, keep signatures sorted by increasing arity and alphabetical order!!

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BINARIES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Generate valid tuples for absolute constraint: VAR1 = |VAR2|
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples absolute(IntVar VAR1, IntVar VAR2) {
        return generateTuples(values -> values[0] == Math.abs(values[1]), true, VAR1, VAR2);
    }

    /**
     * Generate valid tuples for VAR1 OP VAR2, where OP in {"=", "!=", ">","<",">=","<="}
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples arithm(IntVar VAR1, String OP, IntVar VAR2) {
        final Operator op = Operator.get(OP);
        return generateTuples(values -> {
            switch (op) {
                case LT:
                    return values[0] < values[1];
                case GT:
                    return values[0] > values[1];
                case LE:
                    return values[0] <= values[1];
                case GE:
                    return values[0] >= values[1];
                case NQ:
                    return values[0] != values[1];
                case EQ:
                    return values[0] == values[1];
                default:
                    throw new SolverException("Unexpected Tuple operator " + op
                            + " (should be in {\"=\", \"!=\", \">\",\"<\",\">=\",\"<=\"})");
            }
        }, true, VAR1, VAR2);
    }

    /**
     * Generate valid tuples for an element constraint : TABLE[INDEX-OFFSET] = VALUE
     *
     * @param VALUE  an integer variable taking its value in TABLE
     * @param TABLE  an array of integer values
     * @param INDEX  an integer variable representing the value of VALUE in TABLE
     * @param OFFSET offset matching INDEX.LB and TABLE[0] (Generally 0)
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET) {
        Tuples t = new Tuples(true);
        for (int v = INDEX.getLB(); v <= INDEX.getUB(); v = INDEX.nextValue(v)) {
            if (v - OFFSET >= 0 && v - OFFSET < TABLE.length && VALUE.contains(TABLE[v - OFFSET])) {
                t.add(TABLE[v - OFFSET], v);
            }
        }
        return t;
    }

    /**
     * Generate valid tuples for absolute constraint: VAR1  = VAR2^POWER
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples power(IntVar VAR1, IntVar VAR2, final int POWER) {
        return generateTuples(values -> values[0] == Math.pow(values[1], POWER), true, VAR1, VAR2);
    }

    /**
     * Generate valid tuples for absolute constraint: VAR1  = VAR2^2
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples square(IntVar VAR1, IntVar VAR2) {
        return generateTuples(values -> values[0] == Math.pow(values[1], 2), true, VAR1, VAR2);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TERNARIES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Generate valid tuples for euclidean division constraint: DIVIDEND / DIVISOR = RESULT, rounding towards 0
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples eucl_div(IntVar DIVIDEND, IntVar DIVISOR, IntVar RESULT) {
        return generateTuples(values -> values[0] / values[1] == values[2], true, DIVIDEND, DIVISOR, RESULT);
    }

    /**
     * Generate valid tuples for minus constraint: MAX = max(VAR1,VAR2)
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples maximum(IntVar VAR1, IntVar VAR2, IntVar MAX) {
        return generateTuples(values -> values[0] == Math.max(values[1], values[2]), true, MAX, VAR1, VAR2);
    }

    /**
     * Generate valid tuples for minimum constraint: MIN = min(VAR1,VAR2)
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples minimum(IntVar VAR1, IntVar VAR2, IntVar MIN) {
        return generateTuples(values -> values[0] == Math.min(values[1], values[2]), true, MIN, VAR1, VAR2);
    }

    /**
     * Generate valid tuples for minimum constraint: VAR1 % VAR2 = MOD
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples modulo(IntVar VAR1, IntVar VAR2, IntVar MOD) {
        return generateTuples(values -> values[0] == values[1] % values[2], true, MOD, VAR1, VAR2);
    }


    /**
     * Generate valid tuples for minus constraint: VAR1 - VAR2 = RESULT
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples minus(IntVar VAR1, IntVar VAR2, IntVar RESULT) {
        return generateTuples(values -> values[0] - values[1] == values[2], true, VAR1, VAR2, RESULT);
    }

    /**
     * Generate valid tuples for plus constraint: VAR1 + VAR2 = RESULT
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples plus(IntVar VAR1, IntVar VAR2, IntVar RESULT) {
        return generateTuples(values -> values[0] + values[1] == values[2], true, VAR1, VAR2, RESULT);
    }

    /**
     * Generate valid tuples for times constraint: VAR1 * VAR2 = RESULT
     *
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples times(IntVar VAR1, IntVar VAR2, IntVar RESULT) {
        return generateTuples(values -> values[0] * values[1] == values[2], true, VAR1, VAR2, RESULT);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NARIES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Generate valid tuples for allDifferent constraint
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples allDifferent(IntVar... VARS) {
        return generateTuples(values -> {
            for (int i = 0; i < values.length - 1; i++) {
                for (int j = i + 1; j < values.length; j++) {
                    if (values[j] == values[i]) return false;
                }
            }
            return true;
        }, true, VARS);
    }

    /**
     * Generate valid tuples for allEquals constraint
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples allEquals(IntVar... VARS) {
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        for (int i = 0; i < VARS.length; i++) {
            min = Math.max(min, VARS[i].getLB());
            max = Math.min(max, VARS[i].getUB());
        }
        Tuples tuples = new Tuples(true);
        for (int k = min; k <= max; k++) {
            int[] t = new int[VARS.length];
            for (int i = 0; i < VARS.length; i++) {
                t[i] = k;
            }
            tuples.add(t.clone());
        }
        return tuples;
        /*return generateTuples(new TupleValidator() {
            @Override
            public boolean valid(int... values) {
                for (int i = 0; i < values.length - 1; i++) {
                    if (values[i] != values[i + 1]) return false;
                }
                return true;
            }
        }, true, VARS);*/
    }

    /**
     * Generate valid tuples for lexChainLess constraint
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples lex_chain_less(IntVar... VARS) {
        return generateTuples(values -> {
            for (int i = 0; i < values.length - 1; i++) {
                if (values[i] < values[i + 1]) return false;
            }
            return true;
        }, true, VARS);
    }

    /**
     * Generate valid tuples for lexChainLessEq constraint
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples lex_chain_less_eq(IntVar... VARS) {
        return generateTuples(values -> {
            for (int i = 0; i < values.length - 1; i++) {
                if (values[i] <= values[i + 1]) return false;
            }
            return true;
        }, true, VARS);
    }

    /**
     * Generate valid tuples for &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub>*COEFFS<sub>i</sub> OPERATOR SCALAR
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples scalar(IntVar[] VARS, final int[] COEFFS, IntVar SCALAR, final int SCALAR_COEFF) {
        Tuples left = generateTuples(TupleValidator.TRUE, true, VARS);
        Tuples tuples = new Tuples(true);
        int n = VARS.length;
        for (int[] tleft : left.tuples) {
            int right = 0;
            for (int i = 0; i < n; i++) {
                right += tleft[i] * COEFFS[i];
            }
            if (right % SCALAR_COEFF == 0 && SCALAR.contains(right / SCALAR_COEFF)) {
                int[] t = new int[n + 1];
                System.arraycopy(tleft, 0, t, 0, n);
                t[n] = right / SCALAR_COEFF;
                tuples.add(t);
            }
        }
        return tuples;
    }

    /**
     * Generate valid tuples for &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub>*COEFFS<sub>i</sub> OPERATOR SCALAR + CSTE
     *
     * with OPERATOR in {"=", "!=", ">","<",">=","<="}
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples scalar(IntVar[] VARS, final int[] COEFFS, final String OPERATOR, IntVar SCALAR, final int SCALAR_COEFF, int CSTE) {
        if ("=".equals(OPERATOR) && CSTE == 0) {
            return scalar(VARS, COEFFS, SCALAR, SCALAR_COEFF);
        }
        final Operator op = Operator.get(OPERATOR);
        return generateTuples(values -> {
            int scalar = 0;
            for (int i = 0; i < values.length - 1; i++) {
                scalar += values[i] * COEFFS[i];
            }
            switch (op) {
                case LT:
                    return scalar < values[values.length - 1] * SCALAR_COEFF + CSTE;
                case GT:
                    return scalar > values[values.length - 1] * SCALAR_COEFF + CSTE;
                case LE:
                    return scalar <= values[values.length - 1] * SCALAR_COEFF + CSTE;
                case GE:
                    return scalar >= values[values.length - 1] * SCALAR_COEFF + CSTE;
                case NQ:
                    return scalar != values[values.length - 1] * SCALAR_COEFF + CSTE;
                case EQ:
                    return scalar == values[values.length - 1] * SCALAR_COEFF + CSTE;
                default:
                    throw new SolverException("Unexpected Tuple operator " + op
                            + " (should be in {\"=\", \"!=\", \">\",\"<\",\">=\",\"<=\"})");
            }
        }, true, ArrayUtils.append(VARS, new IntVar[]{SCALAR}));
    }

    /**
     * Generate valid tuples for &#8721;<sub>i in |VARS|</sub>VARS<sub>i</sub> OPERATOR SUM + CSTE
     *
     * with OPERATOR in {"=", "!=", ">","<",">=","<="}
     *
     * @param VARS concerned variables
     * @return a Tuples object, reserved for a table constraint
     */
    public static Tuples sum(IntVar[] VARS, final String OPERATOR, IntVar SUM, int CSTE) {
        final Operator op = Operator.get(OPERATOR);
        return generateTuples(values -> {
            int sum = 0;
            for (int i = 0; i < values.length - 1; i++) {
                sum += values[i];
            }
            switch (op) {
                case LT:
                    return sum < values[values.length - 1] + CSTE;
                case GT:
                    return sum > values[values.length - 1] + CSTE;
                case LE:
                    return sum <= values[values.length - 1] + CSTE;
                case GE:
                    return sum >= values[values.length - 1] + CSTE;
                case NQ:
                    return sum != values[values.length - 1] + CSTE;
                case EQ:
                    return sum == values[values.length - 1] + CSTE;
                default:
                    throw new SolverException("Unexpected Tuple operator " + op
                            + " (should be in {\"=\", \"!=\", \">\",\"<\",\">=\",\"<=\"})");
            }
        }, true, ArrayUtils.append(VARS, new IntVar[]{SUM}));
    }

    /**
     * Check whether the intension constraint to extension constraint substitution is enabled and can be achieved
     *
     * @param VARS list of variables involved
     * @return a boolean
     */
    public static boolean canBeTupled(IntVar... VARS) {
        Settings settings = VARS[0].getModel().getSettings();
        if (!settings.enableTableSubstitution()) {
            return false;
        }
        long doms = 1;
        for (int i = 0; i < VARS.length && doms < settings.getMaxTupleSizeForSubstitution(); i++) {
            if (!VARS[i].hasEnumeratedDomain()) {
                return false;
            }
            doms *= VARS[i].getDomainSize();
        }
        return (doms < settings.getMaxTupleSizeForSubstitution());
    }
}
