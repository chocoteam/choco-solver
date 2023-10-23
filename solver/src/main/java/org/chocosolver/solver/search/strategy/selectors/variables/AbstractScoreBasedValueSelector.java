/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.variables.Variable;

import java.util.Comparator;

/**
 * Score-based variable selector.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/05/2023
 */
public abstract class AbstractScoreBasedValueSelector<V extends Variable> implements VariableSelector<V>, IMonitorRestart {
    /**
     * A reference to the Solver
     */
    protected final Solver solver;
    /**
     * Needed to save operations
     */
    final IEnvironment environment;
    /***
     * Pointer to the last free variable
     */
    private final IStateInt last;

    /**
     * Default tie breaker: lexical ordering
     */
    private final Comparator<V> tieBreaker;

    private final int flushRate;

    /**
     * Create a value selector based on the score of each variable
     *
     * @param vars       variables to branch on
     * @param tieBreaker a tiebreaker when scores are equal
     * @param flushRate  the rate at which scores are flushed, based on restart number
     */
    public AbstractScoreBasedValueSelector(V[] vars, Comparator<V> tieBreaker, int flushRate) {
        this.solver = vars[0].getModel().getSolver();
        this.environment = vars[0].getModel().getEnvironment();
        this.last = environment.makeInt(vars.length - 1);
        this.tieBreaker = tieBreaker;
        this.flushRate = flushRate;
    }

    @Override
    public final V getVariable(V[] vars) {
        V best = null;
        double w = Double.NEGATIVE_INFINITY;
        int to = last.get();
        for (int idx = 0; idx <= to; idx++) {
            int domSize = vars[idx].getDomainSize();
            if (domSize > 1) {
                double weight = score(vars[idx]) / domSize;
                if (w < weight || (w == weight && tieBreaker.compare(vars[idx], best) < 0)) {
                    best = vars[idx];
                    w = weight;
                }
            } else {
                // swap
                V tmp = vars[to];
                vars[to] = vars[idx];
                vars[idx] = tmp;
                idx--;
                to--;
            }
        }
        last.set(to);
        return best;
    }

    /**
     * Compute the score of a variable
     *
     * @param v a variable
     * @return a score
     */
    protected abstract double score(V v);

    @Override
    public void afterRestart() {
        if (solver.getRestartCount() % (flushRate + 1) == flushRate) {
            flushScores();
        }
    }

    /**
     *
     */
    protected abstract void flushScores();


}
