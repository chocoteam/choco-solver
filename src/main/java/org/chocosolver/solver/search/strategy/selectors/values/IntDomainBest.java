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
import org.chocosolver.solver.variables.IntVar;

/**
 *
 * Value selector for optimization problems:
 * Branches on the value with the best objective bound (evaluated each possible assignment)
 *
 * @author Jean-Guillaume FAGES
 */
public final class IntDomainBest implements IntValueSelector {

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        assert var.getModel().getObjective()!=null;
		if(var.hasEnumeratedDomain()) {
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
		}else{
			int lbB = bound(var,var.getLB());
			int ubB = bound(var,var.getUB());
			return Math.min(lbB,ubB);
		}
    }

	private int bound(IntVar var, int val){
		Model model = var.getModel();
		int cost;
		model.getEnvironment().worldPush();
		try {
			var.instantiateTo(val, Cause.Null);
			model.getSolver().getEngine().propagate();
			ResolutionPolicy rp = model.getSolver().getObjectiveManager().getPolicy();
			if(rp == ResolutionPolicy.SATISFACTION){
				cost = 1;
			}else if(rp == ResolutionPolicy.MINIMIZE){
				cost = ((IntVar) model.getObjective()).getLB();
			}else {
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
