/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.bandit.Policy;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.function.ToDoubleBiFunction;

/**
 * Implementattion of a strategy sequencer based on Multi-armed bandit strategy selector.
 * <p>
 * This is based on :
 * <i>Perturbing Branching Heuristics in Constraint Solving</i>,
 * Anastasia Paparrizou, Hugues Wattez. CP 2020.
 * <a href="https://dblp.org/rec/conf/cp/PaparrizouW20">DBLP</a></url>.
 * </p>
 * @author Charles Prud'homme
 * @since 27/06/2020
 */
public class MultiArmedBanditSequencer<V extends Variable> extends AbstractStrategy<V> implements IMonitorRestart {

    private final Policy bandit;
    private final ToDoubleBiFunction<Integer, Integer> reward;
    private final AbstractStrategy<V>[] strategies;
    private int action;
    private int step;

    @SafeVarargs
    private static <V extends Variable> V[] make(AbstractStrategy<V>... strategies) {
        Variable[] vars = new Variable[0];
        for (AbstractStrategy<? extends Variable> strategy : strategies) {
            vars = ArrayUtils.append(vars, strategy.vars);
        }
        //noinspection unchecked
        return (V[]) vars;
    }

    /**
     * A multi-armed bandit selection of search strategies.
     *
     * @param strategies set of strategies to choose between
     * @param bandit     the selection policy
     * @param reward     the reward function : {@code reward(action, step)}
     */
    public MultiArmedBanditSequencer(AbstractStrategy<V>[] strategies,
                                     Policy bandit,
                                     ToDoubleBiFunction<Integer, Integer> reward) {
        super(make(strategies));
        this.bandit = bandit;
        this.strategies = strategies;
        this.step = 0;
        this.action = 0;
        this.reward = reward;
    }

    @Override
    public boolean init() {
        this.vars[0].getModel().getSolver().plugMonitor(this);
        boolean ok = true;
        for (AbstractStrategy<V> strategy : strategies) {
            ok &= strategy.init();
        }
        bandit.init();
        return ok;
    }

    @Override
    public void remove() {
        this.vars[0].getModel().getSolver().unplugMonitor(this);
        for (AbstractStrategy<V> strategy : strategies) {
            strategy.remove();
        }
    }

    @Override
    protected Decision<V> computeDecision(V variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        return strategies[action].computeDecision(variable);
    }

    @Override
    public Decision<V> getDecision() {
        return strategies[action].getDecision();
    }

    @Override
    public void afterRestart() {
        step++;
        bandit.update(action, reward.applyAsDouble(action, step));
        action = bandit.nextAction(step);
    }


}