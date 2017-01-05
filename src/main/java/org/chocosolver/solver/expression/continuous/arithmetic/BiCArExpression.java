/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.tools.VariableUtils;

/**
 * Binary continuous arithmetic expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class BiCArExpression implements CArExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;

    /**
     * Lazy creation of the underlying variable
     */
    RealVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    Operator op = null;

    /**
     * The first expression this expression relies on
     */
    private CArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private CArExpression e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiCArExpression(Operator op, CArExpression e1, CArExpression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
        this.model = e1.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public RealVar realVar(double p) {
        if (me == null) {
            RealVar v1 = e1.realVar(p);
            RealVar v2 = e2.realVar(p);
            double[] bounds;
            switch (op) {

                case ADD:
                    bounds = VariableUtils.boundsForAddition(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}+{2}", me, v1, v2).post();
                    break;
                case SUB:
                    bounds = VariableUtils.boundsForSubstraction(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}-{2}", me, v1, v2).post();
                    break;
                case MUL:
                    bounds = VariableUtils.boundsForMultiplication(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}*{2}", me, v1, v2).post();
                    break;
                case DIV:
                    bounds = VariableUtils.boundsForDivision(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}/{2}", me, v1, v2).post();
                    break;
                case POW:
                    bounds = VariableUtils.boundsForPow(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}^{2}", me, v1, v2).post();
                    break;
                case MIN:
                    bounds = VariableUtils.boundsForMinimum(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}=min({1},{2})", me, v1, v2).post();
                    break;
                case MAX:
                    bounds = VariableUtils.boundsForMaximum(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}=max({1},{2})", me, v1, v2).post();
                    break;
                case ATAN2:
                    bounds = VariableUtils.boundsForAtan2(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}=atan2({1},{2})", me, v1, v2).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
