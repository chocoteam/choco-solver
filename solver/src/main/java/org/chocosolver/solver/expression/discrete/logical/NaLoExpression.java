/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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
     * The expressions this expression relies on
     */
    private final ReExpression[] es;

    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaLoExpression(Operator op, ReExpression... es) {
        super(es[0].getModel(), op);
        this.es = es;
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
            BoolVar[] vs = Arrays.stream(es).map(ReExpression::boolVar).toArray(BoolVar[]::new);
            me = model.boolVar(model.generateName(op + "_exp_"));
            switch (op) {
                case AND:
                    model.addClausesBoolAndArrayEqVar(vs, me);
                    break;
                case OR:
                    model.addClausesBoolOrArrayEqVar(vs, me);
                    break;
                case XOR:
                    int[] values = new int[vs.length % 2 == 0 ? vs.length / 2 : (vs.length + 1) / 2];
                    for (int i = 0, j = 1; i < values.length; i++, j += 2) {
                        values[i] = j;
                    }
                    IntVar res = model.intVar(model.generateName(), 0, vs.length);
                    model.sum(vs, "=", res).post();
                    IntVar exres = model.intVar(model.generateName(), values);
                    model.reifyXeqY(res, exres, me);
                    break;
                case IFF:
                    if (vs.length == 2) {
                        model.reifyXeqY(vs[0], vs[1], me);
                    } else {
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
        BoolVar[] vs = Arrays.stream(es).map(ReExpression::boolVar).toArray(BoolVar[]::new);
        switch (op) {
            case AND:
                return model.sum(vs, "=", vs.length);
            case OR:
                return model.sum(vs, ">", 0);
            case XOR:
                int[] values = new int[vs.length % 2 == 0 ? vs.length / 2 : (vs.length + 1) / 2];
                for (int i = 0, j = 1; i < values.length; i++, j += 2) {
                    values[i] = j;
                }
                IntVar res = model.intVar(model.generateName(), values);
                return model.sum(vs, "=", res);
            case IFF:
                if (vs.length == 2) {
                    return model.arithm(vs[0], "=", vs[1]);
                } else {
                    return model.allEqual(vs);
                }
            default:
                throw new UnsupportedOperationException("N-ary logical expressions does not support " + op.name());
        }
    }

    @Override
    public boolean beval(int[] values, Map<IntVar, Integer> map) {
        boolean eval = es[0].beval(values, map);
        for (int i = 1; i < es.length; i++) {
            eval = op.eval(eval, es[i].beval(values, map));
        }
        return eval;
    }

    @Override
    public String toString() {
        return op.name() + "(" + es[0].toString() + ",... ," + es[es.length - 1].toString() + ")";
    }
}
