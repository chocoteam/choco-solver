/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.VariableUtils;

import static org.chocosolver.util.tools.VariableUtils.searchSpaceSize;

/**
 * Value selector for any type of problems:
 * Branches on the value with the best/worst impact on domains cardinality (evaluated each possible assignment)
 *
 * @author Jean-Guillaume FAGES, Charles Prud'homme
 */
public final class IntDomainImpact implements IntValueSelector {

    /**
     * Maximum enumerated domain size this selector falls into.
     * Otherwise, only bounds are considered.
     */
    private final int maxdom;

    /**
     * The decision operator used to make the decision
     */
    private final DecisionOperator<IntVar> dop;

    private IntVar[] allVars;

    private final int coeff;

    /**
	 * Create a value selector that returns the best value wrt to its impact on domains cardinality.
	 * When an enumerated variable domain exceeds {@link #maxdom}, only bounds are considered.
     *
     * @param maxdom a maximum domain size to satisfy to use this value selector.
     * @param dop    the decision operator used to make the decision
     * @param smallest set to true to select the value with the smallest impact,
     *                 set to false to select the value with the greatest impact
     */
	public IntDomainImpact(int maxdom, DecisionOperator<IntVar> dop, boolean smallest) {
        this.maxdom = maxdom;
        this.dop = dop;
        this.coeff = (smallest?1:-1);
    }

    /**
     * Create a value selector for assignments that returns the best value
     * wrt to its impact on domains cardinality.
     * When an enumerated variable domain exceeds 100, only bounds are considered.
     */
    public IntDomainImpact() {
		this(100, DecisionOperatorFactory.makeIntEq(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        if(allVars == null){
            allVars = var.getModel().retrieveIntVars(true);
        }
        assert var.getModel().getObjective() != null;
        double sspace = VariableUtils.searchSpaceSize(allVars);
        if (var.hasEnumeratedDomain() && var.getDomainSize() < maxdom) {
            double bestCost = 2D;
            int ub = var.getUB();
            // if decision is '<=', default value is LB, UB in any other cases
            int bestV = dop == DecisionOperatorFactory.makeIntReverseSplit() ? ub : var.getLB();
            for (int v = var.getLB(); v <= ub; v = var.nextValue(v)) {
                double bound = impact(var, v, sspace) * coeff;
                if (bound < bestCost) {
                    bestCost = bound;
                    bestV = v;
                }
            }
            return bestV;
        } else {
            double lbB = impact(var, var.getLB(), sspace) * coeff;
            double ubB = impact(var, var.getUB(), sspace) * coeff;
            // if values are equivalent
            if (lbB == ubB) {
                // if decision is '<=', default value is LB, UB in any other cases
                return dop == DecisionOperatorFactory.makeIntReverseSplit() ? var.getUB() : var.getLB();
            } else {
                return lbB < ubB ? var.getLB() : var.getUB();
            }
        }
    }

    private double impact(IntVar var, int val, double before) {
        Model model = var.getModel();
        double after;
        // // if decision is '<=' ('>='), UB (LB) should be ignored to avoid infinite loop
        if (dop == DecisionOperatorFactory.makeIntSplit() && val == var.getUB()
                || dop == DecisionOperatorFactory.makeIntReverseSplit() && val == var.getLB()) {
            return 1D;
        }
        model.getEnvironment().worldPush();
        try {
            var.instantiateTo(val, Cause.Null);
            model.getSolver().getEngine().propagate();
            after = searchSpaceSize(allVars);
            return 1D - (after / before);
        } catch (ContradictionException e) {
            model.getSolver().getEngine().flush();
            model.getEnvironment().worldPop();
            model.getEnvironment().worldPush();
            // if the value leads to fail, then the value can be removed from the domain
            try {
                var.removeValue(val, Cause.Null);
                model.getSolver().getEngine().propagate();
            } catch (ContradictionException ex) {
                model.getSolver().getEngine().flush();
            }
            return 1D;
        }finally {
            model.getEnvironment().worldPop();
        }
    }
}
