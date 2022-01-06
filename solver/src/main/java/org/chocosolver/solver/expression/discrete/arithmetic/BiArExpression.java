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
public class BiArExpression implements ArExpression {

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
    private final ArExpression e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiArExpression(Operator op, ArExpression e1, ArExpression e2) {
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
            IntVar v2 = e2.intVar();
            int[] bounds;
            switch (op) {
                case ADD:
                    bounds = VariableUtils.boundsForAddition(v1, v2);
                    me = model.intVar(model.generateName("sum_exp_"), bounds[0], bounds[1]);
                    model.arithm(v1, "+", v2, "=", me).post();
                    break;
                case SUB:
                    bounds = VariableUtils.boundsForSubstraction(v1, v2);
                    me = model.intVar(model.generateName("sub_exp_"), bounds[0], bounds[1]);
                    model.arithm(v1, "-", v2, "=", me).post();
                    break;
                case MUL:
                    bounds = VariableUtils.boundsForMultiplication(v1, v2);
                    me = model.intVar(model.generateName("mul_exp_"), bounds[0], bounds[1]);
                    model.times(v1, v2, me).post();
                    break;
                case DIV:
                    bounds = VariableUtils.boundsForDivision(v1, v2);
                    me = model.intVar(model.generateName("div_exp_"), bounds[0], bounds[1]);
                    model.div(v1, v2, me).post();
                    break;
                case MOD:
                    bounds = VariableUtils.boundsForModulo(v1, v2);
                    me = model.intVar(model.generateName("mod_exp_"), bounds[0], bounds[1]);
                    model.mod(v1, v2, me).post();
                    break;
                case POW: // todo as intension constraint
                    bounds = VariableUtils.boundsForPow(v1, v2);
                    me = model.intVar(model.generateName("pow_exp_"), bounds[0], bounds[1]);
                    Tuples tuples = new Tuples(true);
                    for(int val1 : v1){
                        for(int val2 : v2){
                            int res = (int)Math.pow(val1, val2);
                            if(me.contains(res)) {
                                tuples.add(val1, val2, res);
                            }
                        }
                    }
                    model.table(new IntVar[]{v1, v2, me}, tuples).post();
                    break;
                case MIN:
                    bounds = VariableUtils.boundsForMinimum(v1, v2);
                    me = model.intVar(model.generateName("min_exp_"), bounds[0], bounds[1]);
                    model.min(me, v1, v2).post();
                    break;
                case MAX:
                    bounds = VariableUtils.boundsForMaximum(v1, v2);
                    me = model.intVar(model.generateName("max_exp_"), bounds[0], bounds[1]);
                    model.max(me, v1, v2).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public int ieval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.ieval(values, map), e2.ieval(values, map));
    }

    @Override
    public int getNoChild() {
        return 2;
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return new ArExpression[]{e1, e2};
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
