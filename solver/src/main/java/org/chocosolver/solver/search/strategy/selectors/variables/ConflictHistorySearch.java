/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.variables.Variable;

import java.util.stream.Stream;

/**
 * Source: "Conflict History Based Branching Heuristic for CSP Solving", Habet and Terrioux.
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 25/02/2020.
 */
@SuppressWarnings("rawtypes")
public class ConflictHistorySearch<V extends Variable>
        extends AbstractCriterionBasedVariableSelector<V>
        implements IMonitorRestart {

    /**
     * Related to CHS,
     * Bottom alpha limit
     */
    private static final double ALPHA_LIMIT = 0.06;
    /**
     * Decreasing step for {@link #alpha}.
     */
    private static final double STEP = 1e-6;
    private static final double D = 1e-4;
    private static final double DECAY = .995;

    /**
     * Score of each propagator.
     */
    private final TObjectDoubleMap<Propagator> q = new TObjectDoubleHashMap<>(10, 0.5f, 0.0);
    /**
     * Step-size, 0 < a < 1.
     */
    private double alpha = .4d;
    /**
     * The number of conflicts which have occurred since the beginning of the search.
     */
    int conflicts = 0;

    /**
     * Last {@link #conflicts} value where a propagator led to a failure.
     */
    private final TObjectIntMap<Propagator> conflict = new TObjectIntHashMap<>(10, 0.5f, 0);

    /**
     * Create a Conflict History Search variable selector.
     * @param vars decision variables
     * @param seed seed for breaking ties randomly
     */
    public ConflictHistorySearch(V[] vars, long seed) {
        super(vars, seed, Integer.MAX_VALUE);
    }

    @Override
    public boolean init() {
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return true;
    }

    @Override
    public void remove() {
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    @Override
    protected double weight(Variable v) {
        double[] w = {0.};
        v.streamPropagators().forEach(prop -> {
            long fut = Stream.of(prop.getVars())
                    .filter(var -> !var.isInstantiated())
                    .limit(2)
                    .count();
            if (fut > 1) {
                w[0] += q.get(prop) + D;
            }
        });
        return w[0];
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        conflicts++;
        if (cex.c instanceof Propagator) {
            Propagator p = (Propagator) cex.c;
            double qj = q.get(p);
            // compute the reward
            double r = 1d / (conflicts - conflict.get(p) + 1);
            // update q
            double qcj = (1 - alpha) * qj + alpha * r;
            q.adjustOrPutValue(p, qcj - qj, qcj);
            // decrease a
            alpha = Math.max(ALPHA_LIMIT, alpha - STEP);
            // update conflicts
            conflict.put(p, conflicts);
        }
    }

    @Override
    public void afterRestart() {
        for (Propagator p : q.keySet()) {
            double qj = q.get(p);
            q.put(p, qj * Math.pow(DECAY, (conflicts - conflict.get(p))));
        }
        alpha = .4d;
    }
}
