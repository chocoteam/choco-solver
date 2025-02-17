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

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.IntVar;

import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Value selector for optimization problems:
 * Branches on the value with the best objective bound (evaluated each possible assignment)
 *
 * @author Jean-Guillaume FAGES, Charles Prud'homme
 */
public final class IntDomainBest implements IntValueSelector, Function<IntVar, OptionalInt> {

    /**
     * Maximum enumerated domain size this selector falls into.
     * Otherwise, only bounds are considered.
     */
    private final int maxdom;

    /**
     * The decision operator used to make the decision
     */
    private final DecisionOperator<IntVar> dop;

    /**
     * Condition for tie breaking
     */
    private final BiPredicate<IntVar, Integer> condition;

    private final IntValueSelector fallbackValueSelector;

    private final Function<IntVar, Boolean> trigger;

    /**
     * Create a value selector that returns the best value wrt to the objective to optimize.
     * When an enumerated variable domain exceeds {@link #maxdom}, only bounds are considered.
     *
     * <p>
     * {@code condition} is called when the evaluated {@code value} returns a score
     * equals to the current best one. In that case, if {@code condition} returns {@code true}
     * then {@code value} is retained as the new best candidate, otherwise the previous one
     * is kept.
     * </p>
     *
     * @param maxdom           a maximum domain size to satisfy to use this value selector
     * @param intValueSelector fallback value selector
     * @param trigger          the function that indicates when the best value selector is applied.
     *                         When it returns true, the best value selector is applied.
     *                         Otherwise, the fallback value selector is applied.
     * @param dop              the decision operator used to make the decision
     * @param condition        predicate to break ties
     */
    public IntDomainBest(int maxdom, IntValueSelector intValueSelector, Function<IntVar, Boolean> trigger, DecisionOperator<IntVar> dop, BiPredicate<IntVar, Integer> condition) {
        this.maxdom = maxdom;
        this.dop = dop;
        this.condition = condition;
        this.fallbackValueSelector = intValueSelector;
        this.trigger = trigger;
    }

    /**
     * Create a value selector that returns the best value wrt to the objective to optimize.
     * When an enumerated variable domain exceeds {@link #maxdom}, only bounds are considered.
     *
     * <p>
     * {@code condition} is called when the evaluated {@code value} returns a score
     * equals to the current best one. In that case, if {@code condition} returns {@code true}
     * then {@code value} is retained as the new best candidate, otherwise the previous one
     * is kept.
     * </p>
     *
     * @param intValueSelector fallback value selector
     * @param trigger          the function that indicates when the best value selector is applied.
     *                         When it returns true, the best value selector is applied.
     *                         Otherwise, the fallback value selector is applied.
     */
    public IntDomainBest(IntValueSelector intValueSelector, Function<IntVar, Boolean> trigger) {
        this(100, intValueSelector, trigger, DecisionOperatorFactory.makeIntEq(), (k, v) -> false);
    }

    /**
     * Create a value selector for assignments that returns the best value wrt to the objective to
     * optimize. When an enumerated variable domain exceeds 100, only bounds are considered.
     *
     * <p>
     * {@code condition} is called when the evaluated {@code value} returns a score
     * equals to the current best one. In that case, if {@code condition} returns {@code true}
     * then {@code value} is retained as the new best candidate, otherwise the previous one
     * is kept.
     * </p>
     *
     * @param condition predicate to break ties
     * @apiNote The default values are:
     * <ul>
     *     <li>maxdom is set to 100</li>
     *     <li>the trigger is set to restart count % 16 == 0</li>
     *     <li>the decision operator is set to '='</li>
     * </ul>
     */
    public IntDomainBest(BiPredicate<IntVar, Integer> condition) {
        this(100,
                new IntDomainMin(),
                v -> true,
                DecisionOperatorFactory.makeIntEq(),
                condition);
    }


    /**
     * Create a value selector for assignments that returns the best value wrt to the objective to
     * optimize. When an enumerated variable domain exceeds 100, only bounds are considered.
     * Always-false condition is set by default.
     *
     * @apiNote The default values are:
     * <ul>
     *     <li>maxdom is set to 100</li>
     *     <li>the trigger is set to restart count % 16 == 0</li>
     *     <li>the decision operator is set to '='</li>
     *     <li>the predicate to break ties is lexico</li>
     * </ul>
     */
    public IntDomainBest() {
        this(100,
                new IntDomainMin(),
                v -> true,
                DecisionOperatorFactory.makeIntEq(),
                (k, v) -> false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        if (!trigger.apply(var)) {
            return fallbackValueSelector.selectValue(var);
        }
        assert var.getModel().getObjective() != null;
        return getBestValue(var);
    }

    @Override
    public OptionalInt apply(IntVar var) {
        if (!trigger.apply(var)) {
            return OptionalInt.empty();
        }
        assert var.getModel().getObjective() != null;
        return OptionalInt.of(getBestValue(var));
    }

    private int getBestValue(IntVar var) {
        if (var.hasEnumeratedDomain() && var.getDomainSize() < maxdom) {
            int bestCost = Integer.MAX_VALUE;
            int ub = var.getUB();
            // if decision is '<=', default value is LB, UB in any other cases
            int bestV = dop == DecisionOperatorFactory.makeIntReverseSplit() ? ub : var.getLB();
            for (int v = var.getLB(); v <= ub; v = var.nextValue(v)) {
                int bound = bound(var, v);
                if (bound < bestCost || (bound == bestCost && condition.test(var, v))) {
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
        model.getSolver().pushTrail();
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
        model.getSolver().cancelTrail();
        return cost;
    }
}
