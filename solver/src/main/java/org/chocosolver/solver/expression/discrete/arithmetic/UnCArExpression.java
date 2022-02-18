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
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Map;

/**
 * Binary arithmetic expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class UnCArExpression implements ArExpression {

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
     * The first expression this expression relies on
     */
    private final ArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private final int e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public UnCArExpression(Operator op, ArExpression e1, int e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
        this.model = e1.getModel();
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
            IntVar v1 = e1.intVar();
            int[] bounds;
            switch (op) {
                case ADD:
                    me = model.intOffsetView(v1, e2);
                    break;
                case SUB:
                    me = model.intOffsetView(v1, -e2);
                    break;
                case MUL:
                    if(e2 > 0){
                        me = model.intScaleView(v1, e2);
                    }else{
                        bounds = new int[]{
                                Math.min(v1.getLB()*e2, v1.getUB()*e2),Math.max(v1.getLB()*e2, v1.getUB()*e2)};
                        me = model.intVar(model.generateName("mul_exp_"), bounds[0], bounds[1]);
                        model.times(v1, e2, me).post();
                    }
                    break;
                case DIV: {
                    IntVar v2 = model.intVar(e2);
                    bounds = VariableUtils.boundsForDivision(v1, v2);
                    me = model.intVar(model.generateName("div_exp_"), bounds[0], bounds[1]);
                    model.div(v1, v2, me).post();
                }
                break;
                case MOD: {
                    int min = v1.stream().map(v -> v % e2).min().orElse(0);
                    int max = v1.stream().map(v -> v % e2).max().orElse(v1.getDomainSize());
                    me = model.intVar(model.generateName("mod_exp_"), min, max);
                    model.mod(v1, e2, me).post();
                }
                break;
                case POW: // todo as intension constraint
                {
                    int min = v1.stream().map(v -> (int)Math.floor(Math.pow(v, e2))).min().orElse(IntVar.MIN_INT_BOUND);
                    int max = v1.stream().map(v -> (int)Math.ceil(Math.pow(v, e2))).max().orElse(IntVar.MAX_INT_BOUND);
                    me = model.intVar(model.generateName("pow_exp_"), min, max);
                    Tuples tuples = new Tuples(true);
                    for (int val1 : v1) {
                        int res = (int) Math.pow(val1, e2);
                        if (me.contains(res)) {
                            tuples.add(val1, res);
                        }
                    }
                    model.table(new IntVar[]{v1, me}, tuples).post();
                }
                    break;
                case MIN:
                    me = model.intVar(model.generateName("min_exp_"),
                            Math.min(v1.getLB(), e2), Math.min(v1.getUB(), e2));
                    model.min(me, v1, model.intVar(e2)).post();
                    break;
                case MAX:
                    me = model.intVar(model.generateName("max_exp_"),
                            Math.max(v1.getLB(), e2), Math.max(v1.getUB(), e2));
                    model.max(me, v1, model.intVar(e2)).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public int ieval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.ieval(values, map), e2);
    }

    @Override
    public int getNoChild() {
        return 1;
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return new ArExpression[]{e1};
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2 + ")";
    }
}
