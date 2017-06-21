/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.variables.IntVar;

/**
 * Value selector for optimization problems:
 * Branches on the value with the best objective bound (evaluated each possible assignment)
 *
 * @author Jean-Guillaume FAGES
 */
public final class IntDomainBest implements IntValueSelector {

    /**
     * Maximum enumerated domain size this selector falls into.
     * Otherwise, {@link #altern} is used.
     */
    private int maxdom;

    /**
     * Alternative value selector when an enumerated variable domain exceeds {@link #maxdom}.
     */
    private IntValueSelector altern;

    /**
     * The decision operator used to make the decision
     */
    private DecisionOperator<IntVar> dop;

    /**
     * Create a value selector that returns the best value wrt to the objective to optimize. When an
     * enumerated variable domain exceeds {@link #maxdom}, {@link #altern} value selector is used.
     *
     * @param maxdom a maximum domain size to satisfy to use this value selector.
     * @param altern value selector to use when an enumerated variable domain exceed {@link
     *               #maxdom}.
     * @param dop    the decision operator used to make the decision
     */
    public IntDomainBest(int maxdom, IntValueSelector altern, DecisionOperator<IntVar> dop) {
        this.maxdom = maxdom;
        this.altern = altern;
        this.dop = dop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        assert var.getModel().getObjective() != null;
        if (var.hasEnumeratedDomain()) {
            if (var.getDomainSize() < maxdom) {
                int bestCost = Integer.MAX_VALUE;
                int ub = var.getUB();
                int bestV = ub;
                for (int v = var.getLB(); v <= ub; v = var.nextValue(v)) {
                    int bound = bound(var, v);
                    if (bound < bestCost) {
                        bestCost = bound;
                        bestV = v;
                    }
                }
                return bestV;
            } else {
                return altern.selectValue(var);
            }
        } else {
            int lbB = bound(var, var.getLB());
            int ubB = bound(var, var.getUB());
            return lbB < ubB ? var.getLB() : var.getUB();
        }
    }

    private int bound(IntVar var, int val) {
        Model model = var.getModel();
        int cost;
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
