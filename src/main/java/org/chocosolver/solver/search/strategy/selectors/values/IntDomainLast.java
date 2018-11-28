/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

/**
 * Value selector for optimization problems:
 * Branches on the value in the last solution, if still in domain
 *
 * @author Jean-Guillaume FAGES, Charles Prud'homme
 */
public final class IntDomainLast implements IntValueSelector {

    /**
     * The last solution found
     */
    Solution lastSolution;
    /**
     * The default value selector
     */
    IntValueSelector mainSelector;

    /**
	 * Create a value selector that returns the value in the last solution.
     * If no solution was found or value does not exist anymore, falls back to 'mainSelector'.
     *
     * @param solution container of the last solution
     * @param mainSelector falling back selector
     */
	public IntDomainLast(Solution solution, IntValueSelector mainSelector) {
        this.lastSolution = solution;
        this.mainSelector = mainSelector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        if(lastSolution.exists()){
            int value = lastSolution.getIntVal(var);
            if((var.hasEnumeratedDomain() && var.contains(value))
            || (!var.hasEnumeratedDomain() && var.getLB() == value || var.getUB() == value)){
                return value;
            }
        }
        return mainSelector.selectValue(var);
    }
}
