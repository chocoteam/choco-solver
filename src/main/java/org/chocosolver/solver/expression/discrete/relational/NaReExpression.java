/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.relational;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * Binary relational expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class NaReExpression extends ReExpression {

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
    private ArExpression[] es;

    /**
     * Builds a nary expression
     *
     * @param op an operator
     * @param e an expression
     * @param es some expressions
     */
    public NaReExpression(Operator op, ArExpression e, ArExpression... es) {
        this(op, ArrayUtils.append(new ArExpression[]{e}, es));
    }

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaReExpression(Operator op, ArExpression... es) {
        this.model = es[0].getModel();
        this.op = op;
        this.es = es;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public BoolVar boolVar() {
        if (me == null) {
            IntVar[] vs = Arrays.stream(es).map(ArExpression::intVar).toArray(IntVar[]::new);
            me = model.boolVar(model.generateName(op+"_exp_"));
            switch (op) {
                case EQ:
//                    model.allEqual(vs).reifyWith(me);
                    BoolVar[] bvars = model.boolVarArray(es.length -1);
                    for(int i = 1; i < es.length; i++){
                        model.reifyXeqY(vs[0],vs[i],bvars[i - 1]);
                    }
                    model.addClausesBoolAndArrayEqVar(bvars, me);
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
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
        IntVar[] vs = Arrays.stream(es).map(ArExpression::intVar).toArray(IntVar[]::new);
        switch (op) {
            case EQ:
                return model.allEqual(vs);
        }
        throw new SolverException("Unexpected case");
    }

    @Override
    public boolean eval(int[] values, Map<IntVar, Integer> map) {
        boolean eval = true;
        for(int i = 1; i < es.length; i++){
            eval &= op.eval(es[0].eval(values, map), es[i].eval(values, map));
        }
        return eval;
    }

    @Override
    public String toString() {
        return op.name() + "(" + es[0].toString() + ", ...," + es[es.length - 1].toString() + ")";
    }
}
