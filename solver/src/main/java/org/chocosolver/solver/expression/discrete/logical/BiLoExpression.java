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
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

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
public class BiLoExpression extends LoExpression {

    /**
     * The first expression this expression relies on
     */
    private final ReExpression e1;

    /**
     * The second expression this expression relies on
     */
    private final ReExpression e2;
    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiLoExpression(Operator op, ReExpression e1, ReExpression e2) {
        super(e1.getModel(), op);
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public BoolVar boolVar() {
        if (me == null) {
            BoolVar b1 = e1.boolVar();
            BoolVar b2 = e2.boolVar();
            me = model.boolVar(model.generateName(op + "_exp_"));
            switch (op) {
                case XOR:
                    model.addClausesBoolXorEqVar(b1, b2, me);
                    break;
                case IFF:
                    model.addClausesBoolIsEqVar(b1, b2, me);
                    break;
                case IMP:
                    model.addClausesBoolOrEqVar(b1.not(), b2, me);
                    break;
                default:
                    throw new UnsupportedOperationException("Binary logical expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public void extractVar(HashSet<IntVar> variables) {
        e1.extractVar(variables);
        e2.extractVar(variables);
    }

    @Override
    public Constraint decompose() {
        BoolVar v1 = e1.boolVar();
        BoolVar v2 = e2.boolVar();
        Model model = v1.getModel();
        switch (op) {
            case XOR:
                return model.arithm(v1, "!=", v2);
            case IFF:
                return model.arithm(v1, "=", v2);
            case IMP:
                return model.arithm(v1.not(), "+", v2, ">", 0);
        }
        throw new SolverException("Unexpected case");
    }

    @Override
    public boolean beval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.beval(values, map), e2.beval(values, map));
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + ", " + e2.toString() + ")";
    }
}
