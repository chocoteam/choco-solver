/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.relational;

import org.chocosolver.sat.Literalizer;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.arithmetic.BiArExpression;
import org.chocosolver.solver.expression.discrete.arithmetic.NaArExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;

import static org.chocosolver.solver.expression.discrete.arithmetic.ArExpression.Operator.*;

/**
 * Binary relational expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class UnCReExpression implements ReExpression {

    private static final EnumSet<ArExpression.Operator> ALLOWED = EnumSet.of(ADD, SUB, NOP);

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
    Operator op;

    /**
     * The first expression this expression relies on
     */
    private final ArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private final int e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public UnCReExpression(Operator op, ArExpression e1, int e2) {
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
            //me = model.boolVar(model.generateName(op+"_exp_"));
            switch (op) {
                case LT:
                    me = model.intLeView(v1, e2 - 1);
                    break;
                case LE:
                    me = model.intLeView(v1, e2);
                    break;
                case GE:
                    me = model.intGeView(v1, e2);
                    break;
                case GT:
                    me = model.intGeView(v1, e2 + 1);
                    break;
                case NE:
                    me = model.intNeView(v1, e2);
                    break;
                case EQ:
                    me = model.intEqView(v1, e2);
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
        variables.add(model.intVar(e2));
    }

    private static ArExpression.Operator detectOperator(ArExpression e) {
        int nochild = e.getNoChild();
        if (nochild == 1) {
            return NOP;
        }
        ArExpression.Operator o = null;
        ArExpression[] child = e.getExpressionChild();
        boolean madeOfLeaves = true;
        for (int i = 0; madeOfLeaves && i < nochild; i++) {
            madeOfLeaves = child[i].isExpressionLeaf();
        }
        if (madeOfLeaves) {
            if (nochild == 2 && e instanceof BiArExpression) {
                o = ((BiArExpression) e).getOp();
            } else if (e instanceof NaArExpression) {
                o = ((NaArExpression) e).getOp();
            }// todo: deal with NaLoExpression
        }
        return o;
    }

    @Override
    public Constraint decompose() {
        ArExpression.Operator o1 = detectOperator(e1);
        if (ALLOWED.contains(o1)) {
            IntVar[] vars = new IntVar[e1.getNoChild()];
            int[] coefs = new int[e1.getNoChild()];
            fill(vars, coefs, e1, o1, 0, 1);
            Model model = vars[0].getModel();
            org.chocosolver.solver.constraints.Operator ope;
            switch (op) {
                case LT:
                    ope = org.chocosolver.solver.constraints.Operator.LT;
                    break;
                case LE:
                    ope = org.chocosolver.solver.constraints.Operator.LE;
                    break;
                case GE:
                    ope = org.chocosolver.solver.constraints.Operator.GE;
                    break;
                case GT:
                    ope = org.chocosolver.solver.constraints.Operator.GT;
                    break;
                case NE:
                    ope = org.chocosolver.solver.constraints.Operator.NQ;
                    break;
                case EQ:
                    ope = org.chocosolver.solver.constraints.Operator.EQ;
                    break;
                default:
                    throw new SolverException("Unknown operator: " + op);
            }
            return model.scalar(vars, coefs, ope.toString(), e2);
        } else {
            IntVar v1 = e1.intVar();
            Model model = v1.getModel();
            switch (op) {
                case LT:
                    return model.arithm(v1, "<", e2);
                case LE:
                    return model.arithm(v1, "<=", e2);
                case GE:
                    return model.arithm(v1, ">=", e2);
                case GT:
                    return model.arithm(v1, ">", e2);
                case NE:
                    return model.arithm(v1, "!=", e2);
                case EQ:
                    return model.arithm(v1, "=", e2);
            }
            throw new SolverException("Unexpected case");
        }
    }

    private static void fill(IntVar[] vars, int[] coefs,
                             ArExpression e, ArExpression.Operator o1, int o, int m) {
        ArExpression[] child = e.getExpressionChild();
        if (e.isExpressionLeaf() || child.length == 1) {
            vars[o] = e.intVar();
        } else {
            vars[o] = child[0].intVar();
        }
        coefs[o] = m;
        for (int i = 1; i < child.length; i++) {
            vars[o + i] = child[i].intVar();
            assert ALLOWED.contains(o1);
            coefs[o + i] = (o1 == ADD ? 1 : -1) * m;
        }
    }

    @Override
    public boolean beval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.ieval(values, map), e2);
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2 + ")";
    }

    /**
     * Creates, or returns if already existing, the SAT variable corresponding
     * to the relationship.
     * @return the SAT variable of this relation
     */
    public int satVar() {
        IntVar v1 = e1.intVar();
        switch (op) {
            case LT:
                return model.satVar(v1, new Literalizer.IntLeLit(v1, e2 - 1));
            case LE:
                return model.satVar(v1, new Literalizer.IntLeLit(v1, e2));
            case EQ:
                return model.satVar(v1, new Literalizer.IntEqLit(v1, e2));
            default:
                throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
        }
    }
}
