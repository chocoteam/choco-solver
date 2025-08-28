/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.HashSet;

/**
 * BiReSetExpression - Binary Relational Set Expression
 * <p>
 * This class represents a binary relational expression between two SetVarExpression instances.
 * It supports relational operations such as equality (EQ), inequality (NE), subset (SUBSET),
 * and containment (CONTAINS). The operations can be composed and posted as constraints.
 * <p>
 * Example:
 * setA.eq(setB).post();
 * setA.ne(setB).post();
 * </p>
 *
 * @author Gabriel Augusto David
 * @author Charles Prud'homme
 */
public class BiReSetExpression implements ReExpression {

    private final SetOperator op;
    private final ArSetExpression x;
    private final ArSetExpression y;
    private final Model model;
    private BoolVar result;

    public BiReSetExpression(SetOperator operator, ArSetExpression x, ArSetExpression y) {
        this.op = operator;
        this.model = x.getModel();
        this.x = x;
        this.y = y;
    }

    public Model getModel() {
        return this.model;
    }

    @Override
    public BoolVar boolVar() {
        if (result == null) {
            result = decompose().reify();
        }
        return result;
    }

    @Override
    public void extractVar(HashSet<IntVar> variables) {
        throw new UnsupportedOperationException("extractVar() is not supported for BiReSetExpression.");
    }

    @Override
    public Constraint decompose() {
        SetVar xSet = x.setVar();
        SetVar ySet = y.setVar();
        switch (op) {
            case SUBSET:
                return model.subsetEq(xSet, ySet);
            case EQ:
                return model.allEqual(xSet, ySet);
            case NE:
                return model.allDifferent(xSet, ySet);
            case CONTAINS:
                return model.subsetEq(ySet, xSet);
            case NOT_CONTAINS:
                return model.disjoint(xSet, ySet);
            case NOT_EMPTY:
                return model.notEmpty(xSet);
            default:
                throw new UnsupportedOperationException("Unsupported SetOperator: " + op);
        }
    }
}