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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.PoolManager;

/**
 * A decision based on a {@link SetVar}
 *
 * @author Jean-Guillaume Fages
 * @since Jan. 2013
 */
public class SetDecision extends Decision<SetVar> {

    private static final long serialVersionUID = -7798444054596001059L;
    /**
     * The decision value
     */
    private int value;
    /**
     * The assignment operator
     */
    private DecisionOperator<SetVar> operator;
    /**
     * Decision pool manager, to recycle decisions
     */
    transient private final PoolManager<SetDecision> poolManager;

    /**
     * Create an decision based on an {@link SetVar}
     *
     * @param poolManager decision pool manager, to recycle decisions
     */
    public SetDecision(PoolManager<SetDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Integer getDecisionValue() {
        return value;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            operator.apply(var, value, this);
        } else if (branch == 2) {
            operator.unapply(var, value, this);
        }
    }

    /**
     * Instantiate this decision with the parameters
     *
     * @param v     a variable
     * @param value a value
     */
    public void set(SetVar v, int value, DecisionOperator<SetVar> operator) {
        super.set(v);
        this.var = v;
        this.value = value;
        this.operator = operator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reverse() {
        this.operator = operator.opposite();
    }

    @Override
    public void free() {
        poolManager.returnE(this);
    }

    /**
     * @return the current decision operator
     */
    public DecisionOperator<SetVar> getDecOp() {
        return operator;
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s", (branch < 2 ? "" : "!"), var.getName(), operator.toString(), value);
    }
}
