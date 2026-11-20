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
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * BiArSetExpression - Binary Arithmetic Set Expression
 * <p>
 * This class represents a binary arithmetic expression between two SetVarExpression instances.
 * It supports set operations such as union (UNION) and intersection (INTERSECTION),
 * returning a new SetVar as the result of the operation.
 * <p>
 * Example:
 * setA.eq(setB.union(setC)).post();
 * setA.eq(setB.intersection(setC)).post();
 *
 * @author Gabriel Augusto David
 * @author Charles Prud'homme
 */

public class BiArSetExpression implements ArSetExpression {

    private final SetOperator op;
    private final ArSetExpression x;
    private final ArSetExpression y;
    private final Model model;
    private SetVar resultSet;

    public BiArSetExpression(SetOperator operator, ArSetExpression x, ArSetExpression y) {
        this.op = operator;
        this.x = x;
        this.y = y;
        this.model = x.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public SetVar setVar() {
        if (resultSet == null) {
            SetVar v1 = x.setVar();
            SetVar v2 = y.setVar();
            int[] setDomain = IntStream.concat(Arrays.stream(v1.getUB().toArray()),
                            Arrays.stream(v2.getUB().toArray())).
                    toArray();
            resultSet = model.setVar(model.generateName("aux_setvar_"), new int[]{}, setDomain);

            switch (op) {
                case UNION:
                    model.union(new SetVar[]{v1, v2}, resultSet).post();
                    break;
                case INTERSECTION:
                    model.intersection(new SetVar[]{v1, v2}, resultSet).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported SetOperator: " + op);
            }
        }
        return resultSet;
    }
}
