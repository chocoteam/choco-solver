/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;

import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A value selector that returns the last value assigned to a variable while propagating.
 * If no value was assigned, falls back to 'mainSelector'.
 * This value selector monitors the variables and stores the last value assigned to each variable.
 * Then, when a value has to be selected, it checks if the last assigned value is still in the domain of the variable.
 * If it is, it returns this value, otherwise it falls back to the main selector.
 *
 * @author Charles Prud'homme
 */
public final class IntDomainSticky implements IntValueSelector, Function<IntVar, OptionalInt>, IVariableMonitor<IntVar> {

    /**
     * The last solution found
     */
    private final TIntIntHashMap sticks;
    /**
     * The default value selector
     */
    private final IntValueSelector mainSelector;
    /**
     * The condition to force, when return true, or skip, when return false, sticky value selection.
     * Can be null, in that case will be considered as true.
     */
    private final BiPredicate<IntVar, Integer> condition;

    /**
     * Create a value selector that returns the last value assigned to a variable while propagating.
     * If no value was assigned, falls back to 'mainSelector'.
     *
     * @param observed     variables to observe
     * @param mainSelector falling back selector
     * @param condition    condition to force, when return true, or skip, when return false, phase saving.
     *                     Can be null, in that case will be considered as true.
     */
    public IntDomainSticky(IntVar[] observed, IntValueSelector mainSelector,
                           BiPredicate<IntVar, Integer> condition) {
        this.mainSelector = mainSelector;
        if (condition == null) {
            this.condition = (var, val) -> true;
        } else {
            this.condition = condition;
        }
        this.sticks = new TIntIntHashMap(observed.length, 1.5f, -1, Integer.MAX_VALUE);
        for (IntVar v : observed) {
            v.addMonitor(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        int value = sticks.get(var.getId());
        if (relevant(var, value) && condition.test(var, value)) {
            return value;
        }
        return mainSelector.selectValue(var);
    }

    @Override
    public OptionalInt apply(IntVar var) {
        int value = sticks.get(var.getId());
        if (relevant(var, value) && condition.test(var, value)) {
            return OptionalInt.of(value);
        }
        return OptionalInt.empty();
    }

    private boolean relevant(IntVar var, int value) {
        return (
                (var.hasEnumeratedDomain() && var.contains(value))
                        || (!var.hasEnumeratedDomain() &&
                        (var.getLB() == value || var.getUB() == value))
        );
    }

    @Override
    public void onUpdate(IntVar var, IEventType evt) {
        if (IntEventType.isInstantiate(evt.getMask())) {
            sticks.put(var.getId(), var.getValue());
        }
    }
}

