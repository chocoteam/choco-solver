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

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.function.BiPredicate;

/**
 * Value selector for optimization problems: Branches on the value in the last solution, if still in
 * domain
 *
 * @author Jean-Guillaume FAGES, Charles Prud'homme
 */
public final class IntDomainLast implements IntValueSelector {

    /**
     * The last solution found
     */
    private final Solution lastSolution;
    /**
     * The default value selector
     */
    private final IntValueSelector mainSelector;

    private final BiPredicate<IntVar, Integer> condition;


    /**
     * Create a value selector that returns the value in the last solution. If no solution was found
     * or value does not exist anymore, falls back to 'mainSelector'.
     *
     * @param solution container of the last solution
     * @param mainSelector falling back selector
     * @param condition condition to force, when return true, or skip, when return false, phase saving.
     * Can be null, in that case will be consider as true.
     */
    public IntDomainLast(Solution solution, IntValueSelector mainSelector,
        BiPredicate<IntVar, Integer> condition) {
        this.lastSolution = solution;
        this.mainSelector = mainSelector;
        if(condition == null){
            this.condition = (var, val) -> true;
        }else {
            this.condition = condition;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        if (lastSolution.exists()) {
            int value = lastSolution.getIntVal(var);
            if (relevant(var, value) && condition.test(var, value)) {
                return value;
            }
        }
        return mainSelector.selectValue(var);
    }

    private boolean relevant(IntVar var, int value) {
        return (
            (var.hasEnumeratedDomain() && var.contains(value))
                || (!var.hasEnumeratedDomain() &&
                (var.getLB() == value || var.getUB() == value))
        );
    }
}

