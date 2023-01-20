/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.HashMap;

/**
 * A class to store warm_start hints.
 * <p>
 * This strategy is on the top of the declared one and will bypass the decision
 * it provides when a hint is on the variable.
 * <p>This is automatically deactivated once the first solution is found.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/05/2022
 */
public class WarmStart extends AbstractStrategy<Variable> implements IMonitorSolution {

    /**
     * Store hints per variable
     */
    private final HashMap<Variable, Object> hints;
    /**
     * The main strategy declared in the solver
     */
    private AbstractStrategy<Variable> mainStrategy;
    /**
     * Maintain solution count
     */
    private long solCount;
    private final Solver solver;

    public WarmStart(Solver solver) {
        super();
        this.solver = solver;
        this.hints = new HashMap<>();
    }

    /**
     * Attach the declared strategy, which will provide decision
     * @param strategy the declared search strategy
     */
    public void setStrategy(AbstractStrategy<Variable> strategy) {
        this.mainStrategy = strategy;
    }

    /**
     * @return the attached search strategy, that provides decisions
     */
    public AbstractStrategy<Variable> getStrategy() {
        return this.mainStrategy;
    }

    @Override
    public boolean init() {
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return this.mainStrategy.init();
    }

    @Override
    public void remove() {
        this.mainStrategy.remove();
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    /**
     * Add a hint on a variable value.
     * <p>There can be multiple hint for the same variable.
     * @param var a variable
     * @param o a value
     */
    public void addHint(IntVar var, int o) {
        if (var.isAConstant()) return;
        TIntList values = (TIntList) hints.get(var);
        if (values == null) {
            values = new TIntArrayList();
            hints.put(var, values);
        }
        if (!values.contains(o)) {
            values.add(o);
        }
    }

    /**
     * Clear the hints declared.
     */
    public void clearHints() {
        this.hints.clear();
    }

    /**
     * This is the main reason this class exists.
     * <p>Under some conditions, it bypasses the declared strategy to
     * build a decision based on the given hints.
     * <p>This is automatically skipped when a first solution is found.
     *
     * @implNote When the variable is bounded, it may return a decision
     * whom refutation will have no side-effect. This may lead to infinitely
     * providing the same decision again and again.
     *
     * @return a decision
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Decision getDecision() {
        Decision dec = mainStrategy.getDecision();
        if (solCount < 1 && dec != null) {
            Variable var = dec.getDecisionVariable();
            if (hints.containsKey(var)) {
                if (var instanceof IntVar) {
                    IntVar iv = (IntVar) var;
                    int[] values = ((TIntList) hints.get(var)).toArray();
                    for (int v : values) {
                        if (iv.contains(v)) {
                            dec.free();
                            dec = solver.getDecisionPath().makeIntDecision(
                                    iv,
                                    DecisionOperatorFactory.makeIntEq(),
                                    v
                            );
                            break;
                        }
                    }
                }
            }
        }
        return dec;
    }

    @Override
    public Variable[] getVariables() {
        return mainStrategy.getVariables();
    }

    @Override
    public void onSolution() {
        solCount++;
    }
}
