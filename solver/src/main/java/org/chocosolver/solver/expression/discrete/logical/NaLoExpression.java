/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.logical;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
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
                    // true iff an odd number of expressions are true
                    model.reifyXinS(res, new IntIterableRangeSet(values), me);
                    break;
                case IFF:
                    if (vs.length == 2) {
                        model.reifyXeqY(vs[0], vs[1], me);
                    } else {
                        IntVar sum = model.intVar(model.generateName(), 0, vs.length);
                        model.sum(vs, "=", sum).post();
                        // true iff all the expressions are equal
                        model.reifyXinS(sum, new IntIterableRangeSet(new int[]{0, vs.length}), me);
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
    public ArExpression[] getExpressionChild() {
        return Arrays.stream(es).toArray(ReExpression[]::new);
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
        if (op == Operator.IFF) {
            // n-ary equivalence: all the expressions are equal, as in decompose()
            for (int i = 1; i < es.length; i++) {
                if (eval != es[i].beval(values, map)) {
                    return false;
                }
            }
            return true;
        }
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
