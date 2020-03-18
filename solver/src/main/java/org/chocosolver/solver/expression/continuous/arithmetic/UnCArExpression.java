/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.RealVar;

/**
 * Unary arithmetic continuous expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class UnCArExpression implements CArExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;

    /**
     * Lazy creation of the underlying variable
     */
    RealVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    Operator op;

    /**
     * The expression this expression relies on
     */
    private CArExpression e;

    /**
     * Builds a unary expression
     *
     * @param op  operator
     * @param exp an continuous arithmetic expression
     */
    public UnCArExpression(Operator op, CArExpression exp) {
        this.op = op;
        this.e = exp;
        this.model = e.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public RealVar realVar(double p) {
        if (me == null) {
            RealVar v = e.realVar(p);
            switch (op) {
                case NEG:
                    me = model.realVar(-v.getUB(), -v.getLB(), p);
                    model.realIbexGenericConstraint("{0}=-{1}", me, v).post();
                    break;
                case ABS:
                    me = model.realVar(0.0, Math.max(Math.abs(v.getLB()), Math.abs(v.getUB())) , p);
                    model.realIbexGenericConstraint("{0}=abs({1})", me, v).post();
                    break;
                case EXP:
                    me = model.realVar(Math.min(Math.exp(v.getLB()), Math.exp(v.getUB())),
                            Math.max(Math.exp(v.getLB()), Math.exp(v.getUB())), p);
                    model.realIbexGenericConstraint("{0}=exp({1})", me, v).post();
                    break;
                case LN:
                    me = model.realVar(Math.min(Math.log(v.getLB()), Math.log(v.getUB())),
                            Math.max(Math.log(v.getLB()), Math.log(v.getUB())), p);
                    model.realIbexGenericConstraint("{0}=ln({1})", me, v).post();
                    break;
                case SQRT:
                    me = model.realVar(Math.min(Math.sqrt(v.getLB()), Math.sqrt(v.getUB())),
                            Math.max(Math.sqrt(v.getLB()), Math.sqrt(v.getUB())), p);
                    model.realIbexGenericConstraint("{0}=sqrt({1})", me, v).post();
                    break;
                case COS:
                    me = model.realVar(-1.0, 1.0, p);
                    model.realIbexGenericConstraint("{0}=cos({1})", me, v).post();
                    break;
                case SIN:
                    me = model.realVar(0.0, 1.0, p);
                    model.realIbexGenericConstraint("{0}=sin({1})", me, v).post();
                    break;
                case TAN:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=tan({1})", me, v).post();
                    break;
                case ACOS:
                    me = model.realVar(0.0, Math.PI, p);
                    model.realIbexGenericConstraint("{0}=acos({1})", me, v).post();
                    break;
                case ASIN:
                    me = model.realVar(-Math.PI / 2, Math.PI / 2, p);
                    model.realIbexGenericConstraint("{0}=asin({1})", me, v).post();
                    break;
                case ATAN:
                    me = model.realVar(-Math.PI / 2, Math.PI / 2, p);
                    model.realIbexGenericConstraint("{0}=atan({1})", me, v).post();
                    break;
                case COSH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=cosh({1})", me, v).post();
                    break;
                case SINH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=sinh({1})", me, v).post();
                    break;
                case TANH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=tanh({1})", me, v).post();
                    break;
                case ACOSH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=acosh({1})", me, v).post();
                    break;
                case ASINH:
                    me = model.realVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=asinh({1})", me, v).post();
                    break;
                case ATANH:
                    me = model.realVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=atanh({1})", me, v).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Unary arithmetic expressions does not support "+op.name());
            }
        }
        return me;
    }

    @Override
    public String toString() {
        return op.name() + "(" + e.toString() + ")";
    }
}
