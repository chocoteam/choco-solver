/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.IntVar;

/**
 * Value selector for optimization problems:
 * Branches on the value with the best objective bound (evaluated each possible assignment)
 *
 * @author Jean-Guillaume FAGES, Charles Prud'homme
 */
public final class IntDomainBest implements IntValueSelector {

    /**
     * Maximum enumerated domain size this selector falls into.
     * Otherwise, only bounds are considered.
     */
    private int maxdom;

    /**
     * The decision operator used to make the decision
     */
    private DecisionOperator<IntVar> dop;

    /**
	 * Create a value selector that returns the best value wrt to the objective to optimize.
	 * When an enumerated variable domain exceeds {@link #maxdom}, only bounds are considered.
     *
     * @param maxdom a maximum domain size to satisfy to use this value selector.
     * @param dop    the decision operator used to make the decision
     */
	public IntDomainBest(int maxdom, DecisionOperator<IntVar> dop) {
        this.maxdom = maxdom;
        this.dop = dop;
    }

    /**
     * Create a value selector for assignments that returns the best value wrt to the objective to
     * optimize. When an enumerated variable domain exceeds 100, only bounds are considered.
     */
    public IntDomainBest() {
		this(100, DecisionOperatorFactory.makeIntEq());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        assert var.getModel().getObjective() != null;
        if (var.hasEnumeratedDomain() && var.getDomainSize() < maxdom) {
            int bestCost = Integer.MAX_VALUE;
            int ub = var.getUB();
            // if decision is '<=', default value is LB, UB in any other cases
            int bestV = dop == DecisionOperatorFactory.makeIntReverseSplit() ? ub : var.getLB();
            for (int v = var.getLB(); v <= ub; v = var.nextValue(v)) {
                int bound = bound(var, v);
                if (bound < bestCost) {
                    bestCost = bound;
                    bestV = v;
                }
            }
            return bestV;
        } else {
            int lbB = bound(var, var.getLB());
            int ubB = bound(var, var.getUB());
            // if values are equivalent
            if (lbB == ubB) {
                // if decision is '<=', default value is LB, UB in any other cases
                return dop == DecisionOperatorFactory.makeIntReverseSplit() ? var.getUB() : var.getLB();
            } else {
                return lbB < ubB ? var.getLB() : var.getUB();
            }
        }
    }

    private int bound(IntVar var, int val) {
        Model model = var.getModel();
        int cost;
        // // if decision is '<=' ('>='), UB (LB) should be ignored to avoid infinite loop
        if (dop == DecisionOperatorFactory.makeIntSplit() && val == var.getUB()
                || dop == DecisionOperatorFactory.makeIntReverseSplit() && val == var.getLB()) {
            return Integer.MAX_VALUE;
        }
        model.getEnvironment().worldPush();
        try {
            dop.apply(var, val, Cause.Null);
            model.getSolver().getEngine().propagate();
            ResolutionPolicy rp = model.getSolver().getObjectiveManager().getPolicy();
            if (rp == ResolutionPolicy.SATISFACTION) {
                cost = 1;
            } else if (rp == ResolutionPolicy.MINIMIZE) {
                cost = ((IntVar) model.getObjective()).getLB();
            } else {
                cost = -((IntVar) model.getObjective()).getUB();
            }
        } catch (ContradictionException cex) {
            cost = Integer.MAX_VALUE;
        }
        model.getSolver().getEngine().flush();
        model.getEnvironment().worldPop();
        return cost;
    }
}
