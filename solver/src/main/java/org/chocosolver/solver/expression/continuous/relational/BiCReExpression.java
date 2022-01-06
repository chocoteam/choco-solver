/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.relational;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.variables.RealVar;

import java.util.TreeSet;

/**
 * Binary relational expression over continuous expressions
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class BiCReExpression implements CReExpression {

    /**
     * Operator of the arithmetic expression
     */
    private final Operator op;

    /**
     * The first expression this expression relies on
     */
    private final CArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private final CArExpression e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiCReExpression(Operator op, CArExpression e1, CArExpression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public Constraint ibex(double p) {
        RealVar v1 = e1.realVar(p);
        RealVar v2 = e2.realVar(p);
        Model model = v1.getModel();
        switch (op) {
            case LT:
                return model.realIbexGenericConstraint("{0}<{1}", v1, v2);
            case LE:
                return model.realIbexGenericConstraint("{0}<={1}", v1, v2);
            case GE:
                return model.realIbexGenericConstraint("{0}>={1}", v1, v2);
            case GT:
                return model.realIbexGenericConstraint("{0}>{1}", v1, v2);
            case EQ:
                return model.realIbexGenericConstraint("{0}={1}", v1, v2);
        }
        throw new SolverException("Unexpected case");
    }


    @Override
    public Constraint equation() {
        CArExpression expression = e1.sub(e2);
        TreeSet<RealVar> vars = new TreeSet<>();
        expression.collectVariables(vars);
        return new Constraint("Equation", new PropEquation(vars.toArray(new RealVar[0]),expression, op));
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
