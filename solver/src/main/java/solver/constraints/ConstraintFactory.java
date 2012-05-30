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

import solver.Solver;
import solver.constraints.binary.EqualX_YC;
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.binary.NotEqualX_YC;
import solver.constraints.unary.EqualXC;
import solver.constraints.unary.NotEqualXC;
import solver.constraints.unary.Relation;
import solver.variables.IntVar;

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
     * <br/>Based on <code>EqualXC</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint eq(IntVar x, int c, Solver solver) {
        return new EqualXC(x, c, solver);
    }

    /**
     * Create a <b>X = Y</b> constraint.
     * <br/>Based on <code>EqualXC</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint eq(IntVar x, IntVar y, Solver solver) {
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
        return new EqualX_YC(x, y, c, solver);
    }

    /**
     * Create a <b>X =/= c</b> constraint.
     * <br/>Based on <code>NotEqualXC</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint neq(IntVar x, int c, Solver solver) {
        return new NotEqualXC(x, c, solver);
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
        return new NotEqualX_YC(x, y, c, solver);
    }

    /**
     * Create a <b>X <= Y</b> constraint.
     * <br/>Based on <code>Sum</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint leq(IntVar x, IntVar y, Solver solver) {
//        return Sum.leq(new IntVar[]{x, y}, new int[]{1, -1}, 0, solver);
        return new GreaterOrEqualX_YC(y, x, 0, solver);
    }

    /**
     * Create a <b>X <= c</b> constraint.
     * <br/>Based on <code>Sum</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant
     * @param solver
     */
    public static Constraint leq(IntVar x, int c, Solver solver) {
        return new Relation(x, Relation.R.LQ, c, solver);
    }

    /**
     * Create a <b>X < Y</b> constraint.
     * <br/>Based on <code>Sum</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint lt(IntVar x, IntVar y, Solver solver) {
        return new GreaterOrEqualX_YC(y, x, 1, solver);
    }

    /**
     * Create a <b>X >= Y</b> constraint.
     * <br/>Based on <code>Sum</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint geq(IntVar x, IntVar y, Solver solver) {
        return new GreaterOrEqualX_YC(x, y, 0, solver);
    }

    /**
     * Create a <b>X >= c</b> constraint.
     * <br/>Based on <code>Sum</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param c      a constant object
     * @param solver
     */
    public static Constraint geq(IntVar x, int c, Solver solver) {
        return new Relation(x, Relation.R.GQ, c, solver);
    }

    /**
     * Create a <b>X > Y</b> constraint.
     * <br/>Based on <code>Sum</code> constraint.
     *
     * @param x      a <code>IntVar</code> object
     * @param y      a <code>IntVar</code> object
     * @param solver
     */
    public static Constraint gt(IntVar x, IntVar y, Solver solver) {
        return new GreaterOrEqualX_YC(x, y, 1, solver);
    }

}
