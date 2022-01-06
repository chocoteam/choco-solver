/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.search.strategy.assignments.GraphDecisionOperator;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.PoolManager;

public class GraphDecision extends Decision<GraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected GraphDecisionOperator operator;
    protected int from, to;
    protected final PoolManager<GraphDecision> poolManager;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public GraphDecision(PoolManager<GraphDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Object getDecisionValue() {
        if (to == -1) {
            return from;
        } else {
            return new int[]{from, to};
        }
    }

    public void setNode(GraphVar variable, int node, GraphDecisionOperator operator) {
        super.set(variable);
        this.from = node;
        this.to = -1;
        this.operator = operator;
    }

    public void setEdge(GraphVar variable, int from, int to, GraphDecisionOperator operator) {
        super.set(variable);
        this.from = from;
        this.to = to;
        this.operator = operator;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            if (to == -1) {
                operator.apply(var, from, this);
            } else {
                operator.apply(var, from, to, this);
            }
        } else if (branch == 2) {
            if (to == -1) {
                operator.unapply(var, from, this);
            } else {
                operator.unapply(var, from, to, this);
            }
        }
    }

    @Override
    public void free() {
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        if (to == -1) {
            return " node " + from + operator.toString();
        }
        return " edge (" + from + "," + to + ")" + operator.toString();
    }
}
