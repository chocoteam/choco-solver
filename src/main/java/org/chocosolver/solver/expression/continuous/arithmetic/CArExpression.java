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
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.continuous.relational.BiCReExpression;
import org.chocosolver.solver.expression.continuous.relational.CReExpression;
import org.chocosolver.solver.variables.RealVar;

/**
 *
 * arithmetic expression over reals
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public interface CArExpression {

    /**
     * A default empty array
     */
    CArExpression[] NO_CHILD = new CArExpression[0];

    /**
     * Return the associated model
     *
     * @return a Model object
     */
    Model getModel();

    /**
     * List of available operator for arithmetic expression
     */
    enum Operator {
        /**
         * negation operator
         */
        NEG {
        },
        /**
         * absolute operator
         */
        ABS {
        },
        /**
         * addition operator
         */
        ADD {
        },
        /**
         * subtraction operator
         */
        SUB {
        },
        /**
         * multiplication operator
         */
        MUL {
        },
        /**
         * division operator
         */
        DIV {
        },
        /**
         * power operator
         */
        POW {
        },
        /**
         * min operator
         */
        MIN {
        },
        /**
         * max operator
         */
        MAX {
        },
        /**
         * atan2 operator
         */
        ATAN2 {
        },
        /**
         * exponential operator
         */
        EXP {
        },
        /**
         * neperian operator
         */
        LN {
        },
        /**
         * square root operator
         */
        SQRT {
        },
        /**
         * cosine operator
         */
        COS {
        },
        /**
         * sine operator
         */
        SIN {
        },
        /**
         * tangent operator
         */
        TAN {
        },
        /**
         * inverse cosine operator
         */
        ACOS {
        },
        /**
         * inverse sine operator
         */
        ASIN {
        },
        /**
         * inverse tangent operator
         */
        ATAN {
        },
        /**
         * hyperbolic cosine operator
         */
        COSH {
        },
        /**
         * hyperbolic sine operator
         */
        SINH {
        },
        /**
         * hyperbolic tangent operator
         */
        TANH {
        },
        /**
         * inverse hyperbolic cosine operator
         */
        ACOSH {
        },
        /**
         * inverse hyperbolic sine operator
         */
        ASINH {
        },
        /**
         * inverse hyperbolic tangent operator
         */
        ATANH {
        }
    }

    /**
     * @param precision precision of the variable to return
     * @return the arithmetic expression as an {@link RealVar}.
     * If necessary, it creates intermediary variable and posts intermediary constraints
     */
    RealVar realVar(double precision);

    /**
     * @return return the expression "-x" where this is "x"
     */
    default CArExpression neg() {
        return new UnCArExpression(CArExpression.Operator.NEG, this);
    }

    /**
     * @return return the expression "|x|" where this is "x"
     */
    default CArExpression abs() {
        return new UnCArExpression(CArExpression.Operator.ABS, this);
    }

    /**
     * @param y a double
     * @return return the expression "x + y" where this is "x"
     */
    default CArExpression add(double y) {
        return new BiCArExpression(CArExpression.Operator.ADD, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x + y" where this is "x"
     */
    default CArExpression add(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.ADD, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "x - y" where this is "x"
     */
    default CArExpression sub(double y) {
        return new BiCArExpression(CArExpression.Operator.SUB, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x - y" where this is "x"
     */
    default CArExpression sub(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.SUB, this, y);
    }


    /**
     * @param y a double
     * @return return the expression "x * y" where this is "x"
     */
    default CArExpression mul(double y) {
        return new BiCArExpression(CArExpression.Operator.MUL, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x * y" where this is "x"
     */
    default CArExpression mul(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.MUL, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "x / y" where this is "x"
     */
    default CArExpression div(double y) {
        return new BiCArExpression(CArExpression.Operator.DIV, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x / y" where this is "x"
     */
    default CArExpression div(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.DIV, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "x + y" where this is "x"
     */
    default CArExpression pow(double y) {
        return new BiCArExpression(CArExpression.Operator.POW, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x + y" where this is "x"
     */
    default CArExpression pow(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.POW, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "min(x, y)" where this is "x"
     */
    default CArExpression min(double y) {
        return new BiCArExpression(CArExpression.Operator.MIN, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "min(x, y)" where this is "x"
     */
    default CArExpression min(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.MIN, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "max(x, y)" where this is "x"
     */
    default CArExpression max(double y) {
        return new BiCArExpression(CArExpression.Operator.MAX, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "max(x, y)" where this is "x"
     */
    default CArExpression max(CArExpression y) {
        return new BiCArExpression(CArExpression.Operator.MAX, this, y);
    }

    /**
     * @param y an expression
     * @return return the expression "atan2(x, y)" where this is "x"
     */
    default CArExpression atan2(CArExpression y) {
        return new BiCArExpression(Operator.ATAN2, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "atan2(x, y)" where this is "x"
     */
    default CArExpression atan2(double y) {
        return new BiCArExpression(Operator.ATAN2, this, this.getModel().realVar(y));
    }

    /**
     * @return return the expression "exp(x)" where this is "x"
     */
    default CArExpression exp() {
        return new UnCArExpression(Operator.EXP, this);
    }

    /**
     * @return return the expression "ln(x)" where this is "x"
     */
    default CArExpression ln() {
        return new UnCArExpression(Operator.LN, this);
    }

    /**
     * @return return the expression "sqrt(x)" where this is "x"
     */
    default CArExpression sqrt() {
        return new UnCArExpression(Operator.SQRT, this);
    }

    /**
     * @return return the expression "cos(x)" where this is "x"
     */
    default CArExpression cos() {
        return new UnCArExpression(Operator.COS, this);
    }

    /**
     * @return return the expression "sin(x)" where this is "x"
     */
    default CArExpression sin() {
        return new UnCArExpression(Operator.SIN, this);
    }

    /**
     * @return return the expression "tan(x)" where this is "x"
     */
    default CArExpression tan() {
        return new UnCArExpression(Operator.TAN, this);
    }

    /**
     * @return return the expression "acos(x)" where this is "x"
     */
    default CArExpression acos() {
        return new UnCArExpression(Operator.ACOS, this);
    }

    /**
     * @return return the expression "asin(x)" where this is "x"
     */
    default CArExpression asin() {
        return new UnCArExpression(Operator.ASIN, this);
    }

    /**
     * @return return the expression "atan(x)" where this is "x"
     */
    default CArExpression atan() {
        return new UnCArExpression(Operator.ATAN, this);
    }

    /**
     * @return return the expression "cosh(x)" where this is "x"
     */
    default CArExpression cosh() {
        return new UnCArExpression(Operator.COSH, this);
    }

    /**
     * @return return the expression "sinh(x)" where this is "x"
     */
    default CArExpression sinh() {
        return new UnCArExpression(Operator.SINH, this);
    }

    /**
     * @return return the expression "tanh(x)" where this is "x"
     */
    default CArExpression tanh() {
        return new UnCArExpression(Operator.TANH, this);
    }

    /**
     * @return return the expression "acosh(x)" where this is "x"
     */
    default CArExpression acosh() {
        return new UnCArExpression(Operator.ACOSH, this);
    }

    /**
     * @return return the expression "asinh(x)" where this is "x"
     */
    default CArExpression asinh() {
        return new UnCArExpression(Operator.ASINH, this);
    }

    /**
     * @return return the expression "atanh(x)" where this is "x"
     */
    default CArExpression atanh() {
        return new UnCArExpression(Operator.ATANH, this);
    }

    /**
     * @param y a double
     * @return return the expression "x < y" where this is "x"
     */
    default CReExpression lt(double y) {
        return new BiCReExpression(CReExpression.Operator.LT, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x < y" where this is "x"
     */
    default CReExpression lt(CArExpression y) {
        return new BiCReExpression(CReExpression.Operator.LT, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "x <= y" where this is "x"
     */
    default CReExpression le(double y) {
        return new BiCReExpression(CReExpression.Operator.LE, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x <= y" where this is "x"
     */
    default CReExpression le(CArExpression y) {
        return new BiCReExpression(CReExpression.Operator.LE, this, y);
    }

    /**
     * @param y an ibt
     * @return return the expression "x > y" where this is "x"
     */
    default CReExpression gt(double y) {
        return new BiCReExpression(CReExpression.Operator.GT, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x > y" where this is "x"
     */
    default CReExpression gt(CArExpression y) {
        return new BiCReExpression(CReExpression.Operator.GT, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "x >= y" where this is "x"
     */
    default CReExpression ge(double y) {
        return new BiCReExpression(CReExpression.Operator.GE, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x >= y" where this is "x"
     */
    default CReExpression ge(CArExpression y) {
        return new BiCReExpression(CReExpression.Operator.GE, this, y);
    }

    /**
     * @param y a double
     * @return return the expression "x = y" where this is "x"
     */
    default CReExpression eq(double y) {
        return new BiCReExpression(CReExpression.Operator.EQ, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x = y" where this is "x"
     */
    default CReExpression eq(CArExpression y) {
        return new BiCReExpression(CReExpression.Operator.EQ, this, y);
    }
}
