/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Map;

/**
 * Unary arithmetic expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class UnArExpression implements ArExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;

    /**
     * Lazy creation of the underlying variable
     */
    IntVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    Operator op;

    /**
     * The expression this expression relies on
     */
    private final ArExpression e;

    /**
     * Builds a unary expression
     *
     * @param op  operator
     * @param exp an arithmetic expression
     */
    public UnArExpression(Operator op, ArExpression exp) {
        this.op = op;
        this.e = exp;
        this.model = e.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    public Operator getOp() {
        return op;
    }

    @Override
    public int getNoChild() {
        return 1;
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return new ArExpression[]{e};
    }

    @Override
    public IntVar intVar() {
        if (me == null) {
            IntVar v = e.intVar();
            switch (op){
                case NEG:
                    me = model.intMinusView(v);
                    break;
                case ABS:
                    me = model.intAbsView(v);
                    break;
                case SQR:
                    int[] bounds = VariableUtils.boundsForMultiplication(v, v);
                    me = model.intVar(model.generateName("sqr_exp_"), bounds[0], bounds[1]);
                    model.times(v, v, me).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Unary arithmetic expressions does not support "+op.name());
            }
        }
        return me;
    }

    @Override
    public int ieval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e.ieval(values, map));
    }

    @Override
    public String toString() {
        return op.name() + "(" + e.toString() + ")";
    }
}
