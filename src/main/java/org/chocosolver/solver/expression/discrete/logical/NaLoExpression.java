/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.logical;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * Binary arithmetic expression
 * <p/>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class NaLoExpression extends LoExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;

    /**
     * Lazy creation of the underlying variable
     */
    BoolVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    Operator op = null;

    /**
     * The expressions this expression relies on
     */
    private ReExpression[] es;

    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaLoExpression(Operator op, ReExpression... es) {
        this.op = op;
        this.es = es;
        this.model = es[0].getModel();
    }

    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaLoExpression(Operator op, ReExpression e, ReExpression... es) {
        this(op, ArrayUtils.append(new ReExpression[]{e}, es));
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public BoolVar boolVar() {
        if (me == null) {
            BoolVar[] vs = Arrays.stream(es).map(e -> e.boolVar()).toArray(BoolVar[]::new);
            me = model.boolVar(model.generateName(op + "_exp_"));
            switch (op) {
                case AND:
                    model.addClausesBoolAndArrayEqVar(vs, me);
                    break;
                case OR:
                    model.addClausesBoolOrArrayEqVar(vs, me);
                    break;
                case IFF:
                    if(vs.length == 2){
                        model.reifyXeqY(vs[0], vs[1], me);
                    }else {
                        IntVar count = model.intVar(op + "_count_", 1, 2);
                        model.atMostNValues(vs, count, false).post();
                        model.reifyXltC(count, 2, me);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("N-ary logical expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public void extractVar(HashSet<IntVar> variables) {
        Arrays.stream(es).forEach(e -> e.extractVar(variables));
    }

    @Override
    public Constraint decompose() {
        BoolVar[] vs = Arrays.stream(es).map(e -> e.boolVar()).toArray(BoolVar[]::new);
        switch (op) {
            case AND:
                return model.sum(vs, "=", vs.length);
            case OR:
                return model.sum(vs, ">", 0);
            case IFF:
                if(vs.length == 2){
                    return model.arithm(vs[0], "=", vs[1]);
                }else {
                    return model.allEqual(vs);
                }
            default:
                throw new UnsupportedOperationException("N-ary logical expressions does not support " + op.name());
        }
    }

    @Override
    public boolean beval(int[] values, Map<IntVar, Integer> map) {
        boolean eval = true;
        for(int i = 1; i < es.length; i++){
            eval &= op.eval(es[0].beval(values, map), es[i].beval(values, map));
        }
        return eval;
    }

    @Override
    public String toString() {
        return op.name() + "(" + es[0].toString() + ",... ," + es[es.length - 1].toString() + ")";
    }
}
