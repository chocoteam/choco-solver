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
public class BiReExpression extends ReExpression {

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
    ReExpression.Operator op = null;

    /**
     * The first expression this expression relies on
     */
    private ArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private ArExpression e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiReExpression(ReExpression.Operator op, ArExpression e1, ArExpression e2) {
        this.model = e1.getModel();
        this.op = op;
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
            IntVar v1 = e1.intVar();
            IntVar v2 = e2.intVar();
            me = model.boolVar(model.generateName(op+"_exp_"));
            switch (op) {
                case LT:
                    model.reifyXltY(v1,v2, me);
                    break;
                case LE:
                    model.reifyXleY(v1,v2, me);
                    break;
                case GE:
                    model.reifyXleY(v2,v1, me);
                    break;
                case GT:
                    model.reifyXltY(v2,v1, me);
                    break;
                case NE:
                    model.reifyXneY(v1,v2, me);
                    break;
                case EQ:
                    model.reifyXeqY(v1,v2, me);
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
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
        IntVar v1 = e1.intVar();
        IntVar v2 = e2.intVar();
        Model model = v1.getModel();
        switch (op) {
            case LT:
                return model.arithm(v1, "<", v2);
            case LE:
                return model.arithm(v1, "<=", v2);
            case GE:
                return model.arithm(v1, ">=", v2);
            case GT:
                return model.arithm(v1, ">", v2);
            case NE:
                return model.arithm(v1, "!=", v2);
            case EQ:
                return model.arithm(v1, "=", v2);
        }
        throw new SolverException("Unexpected case");
    }

    @Override
    public boolean beval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.ieval(values, map), e2.ieval(values, map));
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
