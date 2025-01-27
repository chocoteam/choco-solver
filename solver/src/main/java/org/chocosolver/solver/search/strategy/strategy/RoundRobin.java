/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * A search strategy that selects the next strategy to apply in a round-robin fashion.
 * A strategy is composed of a variable selector and a value selector.
 * On each restart, the strategy is updated to the next one in the list of strategies.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/10/2024
 */
public class RoundRobin extends AbstractStrategy<IntVar> implements IMonitorRestart {

    // the variable selectors to select the next variable to branch on
    final VariableSelector<IntVar>[] variableSelectors;
    // the index of the current combination
    int currentCombination;
    // the value selectors to select the next value to assign to the selected variable
    final IntValueSelector[] valueSelectors;
    // true if the solution has to be saved
    Function<IntVar, OptionalInt> solutionFirst = v -> OptionalInt.empty();
    Function<IntVar, OptionalInt> bestFirst = v -> OptionalInt.empty();
    final List<int[]> combinations;

    /**
     * Creates a RoundRobin strategy.
     * The strategy selects the next variable to branch on using the variable selectors in the order they are given.
     * The strategy selects the next value to assign to the selected variable using the value selectors in the order they are given.
     * The selectors are changed on each restart.
     *
     * @param variables              the variables to branch on
     * @param variableSelectors      the variable selectors
     * @param valueSelectors         the value selectors
     * @param solutionSaving         true if the solution has to be saved
     *                               (the first solution found is saved and used to guide the search)
     * @param bestUntilFirstSolution true if the value selection is guided by the objective function
     *                               until the first solution is found
     */
    public RoundRobin(IntVar[] variables,
                      VariableSelector<IntVar>[] variableSelectors,
                      IntValueSelector[] valueSelectors,
                      boolean solutionSaving,
                      boolean bestUntilFirstSolution) {
        super(variables);
        this.variableSelectors = variableSelectors;
        this.valueSelectors = valueSelectors;
        this.currentCombination = 0;
        if (solutionSaving) {
            Solution solution = vars[0].getModel().getSolver().defaultSolution();
            solutionFirst = new IntDomainLast(solution, valueSelectors[0], null);
        }
        if (bestUntilFirstSolution) {
            bestFirst = new IntDomainBest(valueSelectors[0], v -> variables[0].getModel().getSolver().getSolutionCount() == 0);
        }
        this.combinations = new ArrayList<>();
        for (int i = 0; i < variableSelectors.length; i++) {
            for (int j = 0; j < valueSelectors.length; j++) {
                combinations.add(new int[]{i, j});
            }
        }
    }

    @Override
    public boolean init() {
        Solver solver = vars[0].getModel().getSolver();
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        boolean init = true;
        for (VariableSelector<IntVar> vs : variableSelectors) {
            init &= vs.init();
        }
        return init;
    }

    @Override
    public void remove() {
        for (VariableSelector<IntVar> vs : variableSelectors) {
            vs.remove();
        }
        Solver solver = vars[0].getModel().getSolver();
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar variable = variableSelectors[combinations.get(currentCombination)[0]].getVariable(vars);
        return computeDecision(variable);
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        OptionalInt opt = bestFirst.apply(variable);
        if (!opt.isPresent()) {
            opt = solutionFirst.apply(variable);
        }
        if (!opt.isPresent()) {
            opt = OptionalInt.of(valueSelectors[combinations.get(currentCombination)[1]].selectValue(variable));
        }
        return variable.getModel().getSolver().getDecisionPath().makeIntDecision(variable, DecisionOperatorFactory.makeIntEq(), opt.getAsInt());
    }

    @Override
    public void afterRestart() {
        this.currentCombination = (currentCombination + 1) % combinations.size();
    }
}
