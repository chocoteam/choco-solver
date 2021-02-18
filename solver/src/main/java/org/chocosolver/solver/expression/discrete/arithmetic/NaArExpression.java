/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Binary arithmetic expression
 * <p/>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class NaArExpression implements ArExpression {

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
     * The expressions this expression relies on
     */
    private ArExpression[] es;

    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaArExpression(Operator op, ArExpression e, ArExpression... es) {
        this(op, ArrayUtils.append(new ArExpression[]{e}, es));
    }

    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaArExpression(Operator op, ArExpression... es) {
        this.op = op;
        this.es = es;
        this.model = es[0].getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    public Operator getOp() {
        return op;
    }

    @Override
    public IntVar intVar() {
        if (me == null) {
            IntVar[] vs = Arrays.stream(es).map(ArExpression::intVar).toArray(IntVar[]::new);
            int[] bounds;
            switch (op) {
                case ADD:
                    bounds = VariableUtils.boundsForAddition(vs);
                    me = model.intVar(model.generateName("sum_exp_"), bounds[0], bounds[1]);
                    model.sum(vs, "=", me).post();
                    break;
                case MUL:
                    bounds = VariableUtils.boundsForMultiplication(vs[0], vs[1]);
                    me = model.intVar(model.generateName("mul_exp_0_"), bounds[0], bounds[1]);
                    model.times(vs[0], vs[1], me).post();
                    for (int i = 2; i < vs.length; i++) {
                        IntVar pre = me;
                        bounds = VariableUtils.boundsForMultiplication(pre, vs[i]);
                        me = model.intVar(model.generateName("mul_exp_0_"), bounds[0], bounds[1]);
                        model.times(pre, vs[i], me).post();
                    }
                    break;
                case MIN:
                    bounds = VariableUtils.boundsForMinimum(vs);
                    me = model.intVar(model.generateName("min_exp_"), bounds[0], bounds[1]);
                    model.min(me, vs).post();
                    break;
                case MAX:
                    bounds = VariableUtils.boundsForMaximum(vs);
                    me = model.intVar(model.generateName("max_exp_"), bounds[0], bounds[1]);
                    model.max(me, vs).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public int ieval(int[] values, Map<IntVar, Integer> map) {
//        int r = es[0].eval(values, map);
        return Arrays.stream(es)
                .mapToInt(e -> e.ieval(values, map))
                .reduce(op.identity(), (e1, e2) -> op.eval(e1, e2));
    }

    @Override
    public int getNoChild() {
        return es.length;
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return es;
    }

    @Override
    public String toString() {
        return op.name() + "(" + es[0].toString() + ",... ," + es[es.length - 1].toString() + ")";
    }
}
