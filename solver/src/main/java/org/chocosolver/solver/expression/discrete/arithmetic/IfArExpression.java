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
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashSet;
import java.util.Map;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 05/06/2018.
 */
public class IfArExpression implements ArExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;
    /**
     * Lazy creation of the underlying variable
     */
    IntVar me = null;

    /**
     * The expressions this expression relies on
     */
    private ArExpression e1, e2;

    private ReExpression b0;


    public IfArExpression(ReExpression b, ArExpression y1, ArExpression y2) {
        this.model = y1.getModel();
        this.b0 = b;
        this.e1 = y1;
        this.e2 = y2;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public IntVar intVar() {
        if (me == null) {
            BoolVar v0 = b0.boolVar();
            IntVar v1 = e1.intVar();
            IntVar v2 = e2.intVar();
            me = model.intVar(model.generateName("if_exp_"),
                    Math.min(v1.getLB(), v2.getLB()),
                    Math.max(v1.getUB(), v2.getUB()));
            model.reifyXeqY(me, v1, v0);
            model.reifyXeqY(me, v2, v0.not());
        }
        return me;
    }

    @Override
    public void extractVar(HashSet<IntVar> variables) {
        b0.extractVar(variables);
        e1.extractVar(variables);
        e2.extractVar(variables);
    }

    @Override
    public int ieval(int[] values, Map<IntVar, Integer> map) {
        if(b0.beval(values, map)){
            return e1.ieval(values, map);
        }else{
            return e2.ieval(values, map);
        }
    }

    @Override
    public int getNoChild() {
        return 3;
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return new ArExpression[]{b0, e1, e2};
    }

    @Override
    public String toString() {
        return "IF(" + b0.toString() + "," + e1.toString() + "," + e2.toString() + ")";
    }
}
