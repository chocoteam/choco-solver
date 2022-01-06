/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.continuous.relational.BiCReExpression;
import org.chocosolver.solver.expression.continuous.relational.CReExpression;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.objects.RealInterval;
import org.chocosolver.util.tools.RealUtils;

import java.util.List;
import java.util.TreeSet;

/**
 *
 * arithmetic expression over reals
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public interface CArExpression extends RealInterval {

    /**
     * Return the associated model
     *
     * @return a Model object
     */
    Model getModel();

    /**
     * Computes the narrowest bounds with respect to sub terms.
     */
    void tighten();

    /**
     * Projects computed bounds to the sub expressions.
     * @param cause reference to the instance of
     * {@link org.chocosolver.solver.expression.continuous.relational.PropEquation} involving this
     */
    void project(ICause cause) throws ContradictionException;

    /**
     * Collects real variables involved in this expression and add them into 'set'.
     * @param set an ordered set of involved real variables
     */
    void collectVariables(TreeSet<RealVar> set);

    /**
     * Collects sub-expressions composing this expression and add them to 'list'.
     * @param list list of sub-expressions of this.
     */
    void subExps(List<CArExpression> list);

    /**
     * Considering 'var' and this expression,
     * fills 'wx' with sub-expressions involving 'var' and
     * fills 'wox' with sub-expressions not involving 'var'.
     * @param var a real variable
     * @param wx list of sub-expressions involving 'var'
     * @param wox list of sub-expressions not involving 'var'
     * @return 'true' if this expression involves 'var'.
     */
    boolean isolate(RealVar var, List<CArExpression> wx, List<CArExpression> wox);

    /**
     * Initializes this expression when this serves as input of
     * {@link org.chocosolver.solver.expression.continuous.relational.PropEquation}.
     */
    void init();

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
         * square operator
         */
        SQR {
        },
        /**
         * cubic operator
         */
        CUB {
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
         * cubic root operator
         */
        CBRT {
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
     * @return return the expression "x^y" where this is "x"
     */
    default CArExpression pow(double y) {
        return new BiCArExpression(CArExpression.Operator.POW, this, this.getModel().realVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x ^ y" where this is "x"
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
     * @return return the expression "x^2" where this is "x"
     */
    default CArExpression sqr() {
            return new UnCArExpression(Operator.SQR, this);
        }

    /**
     * @return return the expression "sqrt(x)" where this is "x"
     */
    default CArExpression sqrt() {
        return new UnCArExpression(Operator.SQRT, this);
    }

    /**
     * @return return the expression "x^3" where this is "x"
     */
    default CArExpression cub() {
        return new UnCArExpression(Operator.CUB, this);
    }

    /**
     * @return return the expression "cbrt(x)" where this is "x"
     */
    default CArExpression cbrt() {
        return new UnCArExpression(Operator.CBRT, this);
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
     * @implNote if {@literal y} is equal to {@literal 0.}, then {@literal y} is updated to {@code RealUtils.prevFloat(0)}.
     * Indeed, {@literal 0.} is a special case in continuous.
     */
    default CReExpression le(double y) {
        if(y == 0.){
            y = RealUtils.prevFloat(0);
        }
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
     * @implNote if {@literal y} is equal to {@literal 0.}, then {@literal y} is updated to {@code RealUtils.nextFloat(0)}.
     * Indeed, {@literal 0.} is a special case in continuous.
     */
    default CReExpression ge(double y) {
        if(y == 0.){
            y = RealUtils.nextFloat(-0);
        }
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
