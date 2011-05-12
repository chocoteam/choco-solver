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

package solver.constraints;

import gnu.trove.TObjectIntHashMap;
import solver.Solver;
import solver.constraints.binary.EqualX_YC;
import solver.constraints.binary.NotEqualX_YC;
import solver.constraints.nary.IntLinComb;
import solver.variables.IntVar;

import java.util.Arrays;

/**
 * A factory to simplify creation of <code>Constraint</code> objects, waiting for a model package.
 * This <code>ConstraintFactory</code> is not complete and does not tend to be. It only help users in declaring
 * basic and often-used constraints.
 *
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public class ConstraintFactory {

    protected ConstraintFactory() {
    }

    /**
     * Create a <b>X = c</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint eq(IntVar x, int c, Solver solver) {
        return new IntLinComb(new IntVar[]{x}, new int[]{1}, 1, IntLinComb.Operator.EQ, c, solver);
    }

    /**
     * Create a <b>X = Y</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint eq(IntVar x, IntVar y, Solver solver) {
//        return new IntLinComb(new IntVar[]{x,y}, new int[]{1,-1},1, IntLinComb.Operator.EQ, 0, solver, engine);
        return new EqualX_YC(x, y, 0, solver);
    }

    /**
     * Create a <b>X = Y + C</b> constraint.
     * <br/>Based on <code>NotEqualX_YC</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint eq(IntVar x, IntVar y, int c, Solver solver) {
//        return new IntLinComb(new IntVar[]{x,y}, new int[]{1,-1},1, IntLinComb.Operator.EQ, -c, solver, engine);
        return new EqualX_YC(x, y, c, solver);
    }

    /**
     * Create a <b>X =/= c</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint neq(IntVar x, int c, Solver solver) {
        return new IntLinComb(new IntVar[]{x}, new int[]{1}, 1, IntLinComb.Operator.NEQ, c, solver);
    }

    /**
     * Create a <b>X =/= Y</b> constraint.
     * <br/>Based on <code>NotEqualX_YC</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint neq(IntVar x, IntVar y, Solver solver) {
//        return new IntLinComb(new IntVar[]{x,y}, new int[]{1,-1},1, IntLinComb.Operator.NEQ, 0, engine);
        return new NotEqualX_YC(x, y, 0, solver);
    }

    /**
     * Create a <b>X =/= Y + C</b> constraint.
     * <br/>Based on <code>NotEqualX_YC</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint neq(IntVar x, IntVar y, int c, Solver solver) {
//        return new IntLinComb(new IntVar[]{x,y}, new int[]{1,-1},1, IntLinComb.Operator.NEQ, c, engine);
        return new NotEqualX_YC(x, y, c, solver);
    }

    /**
     * Create a <b>X <= Y</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint leq(IntVar x, IntVar y, Solver solver) {
        return new IntLinComb(new IntVar[]{x, y}, new int[]{1, -1}, 1, IntLinComb.Operator.LEQ, 0, solver);
    }

    /**
     * Create a <b>X <= c</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint leq(IntVar x, int c, Solver solver) {
        return new IntLinComb(new IntVar[]{x}, new int[]{1}, 1, IntLinComb.Operator.LEQ, -c, solver);
    }

    /**
     * Create a <b>X < Y</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint lt(IntVar x, IntVar y, Solver solver) {
        return new IntLinComb(new IntVar[]{x, y}, new int[]{1, -1}, 1, IntLinComb.Operator.LEQ, 1, solver);
    }

    /**
     * Create a <b>X >= Y</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint geq(IntVar x, IntVar y, Solver solver) {
        return new IntLinComb(new IntVar[]{x, y}, new int[]{1, -1}, 1, IntLinComb.Operator.GEQ, 0, solver);
    }

    /**
     * Create a <b>X >= c</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant object
     * @param solver
     */
    public static Constraint geq(IntVar x, int c, Solver solver) {
        return new IntLinComb(new IntVar[]{x}, new int[]{1}, 1, IntLinComb.Operator.GEQ, -c, solver);
    }

    /**
     * Create a <b>X > Y</b> constraint.
     * <br/>Based on <code>IntLinComb</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint gt(IntVar x, IntVar y, Solver solver) {
        return new IntLinComb(new IntVar[]{x, y}, new int[]{1, -1}, 1, IntLinComb.Operator.GEQ, 1, solver);
    }

    public static Constraint scalar(IntVar[] vars, int[] coeffs, IntLinComb.Operator op,
                                    int c, Solver solver) {
        TObjectIntHashMap<IntVar> map = new TObjectIntHashMap<IntVar>();
        for (int i = 0; i < vars.length; i++) {
            map.adjustOrPutValue(vars[i], coeffs[i], coeffs[i]);
            if (map.get(vars[i]) == 0) {
                map.remove(vars[i]);
            }
        }

        IntVar[] nvars = map.keys(new IntVar[map.size()]);
        int b = 0, e = map.size();
        IntVar[] tmpV = new IntVar[e];
        int[] tmpC = new int[e];
        // iteration over the paramater array to avoid non-deterministic behavior introduced by the map
        for (int i = 0; i < vars.length; i++) {
            IntVar var = vars[i];
            if (map.contains(vars[i])) {
                int coeff = map.get(var);
                if (coeff > 0) {
                    tmpV[b] = var;
                    tmpC[b++] = coeff;
                } else if (coeff < 0) {
                    tmpV[--e] = var;
                    tmpC[e] = coeff;
                }
            }
        }
        return new IntLinComb(tmpV, tmpC, b, op, -c, solver);
    }


    public static Constraint scalar(IntVar[] vars, int[] coeffs, IntLinComb.Operator op,
                                    IntVar v, int c, Solver solver) {
        int size = vars.length;
        IntVar[] vtmp = new IntVar[size + 1];
        System.arraycopy(vars, 0, vtmp, 0, size);
        vtmp[size] = v;
        int[] ctmp = new int[size + 1];
        System.arraycopy(coeffs, 0, ctmp, 0, size);
        ctmp[size] = -c;
        return scalar(vtmp, ctmp, op, 0, solver);

    }

    public static Constraint sum(IntVar[] vars, IntLinComb.Operator op,
                                 IntVar v, int c, Solver solver) {
        int[] coeffs = new int[vars.length];
        Arrays.fill(coeffs, 1);
        return scalar(vars, coeffs, op, v, c, solver);
    }

    public static Constraint sum(IntVar[] vars, IntLinComb.Operator op,
                                 int c, Solver solver) {

        int[] coeffs = new int[vars.length];
        Arrays.fill(coeffs, 1);
        return scalar(vars, coeffs, op, c, solver);
    }

}
