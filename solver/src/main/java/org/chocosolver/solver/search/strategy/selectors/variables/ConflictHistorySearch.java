/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.variables.IntVar;
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
public class ConflictHistorySearch
        extends AbstractCriterionBasedVariableSelector
        implements IMonitorRestart {

    /**
     * Related to CHS,
     * Bottom alpha limit
     */
    private static final double ALPHA_LIMIT = 0.06;
    /**
     * Decreasing step for {@link #alpha}.
     */
    private static final double STEP = 10e-6;
    private static final double D = 10e-4;
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
     * Last {@link #conflicts} value where a propagator led to a failure.
     */
    private final TObjectIntMap<Propagator> conflict = new TObjectIntHashMap<>(10, 0.5f, 0);

    public ConflictHistorySearch(IntVar[] vars, long seed) {
        this(vars, seed, Integer.MAX_VALUE);
    }

    public ConflictHistorySearch(IntVar[] vars, long seed, int flushThs) {
        super(vars, seed, flushThs);
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
    protected double weight(IntVar v) {
        double w = 0.;
        int nbp = v.getNbProps();
        for (int i = 0; i < nbp; i++) {
            Propagator<?> prop = v.getPropagator(i);
            long fut = Stream.of(prop.getVars())
                    .filter(Variable::isInstantiated)
                    .limit(2)
                    .count();
            if (fut > 1) {
                w += refinedWeights.getOrDefault(prop, rw)[0] + D;
            }
        }
        return w;
    }

    @Override
    void increase(Propagator<?> prop, Element elt, double[] ws) {
        // for CHS, 0 stores the scoring
        // compute the reward
        double r = 1d / (conflicts - elt.ws[2] + 1);
        // update q
        ws[0] = (1 - alpha) * ws[0] + alpha * r;
        // decrease a
        alpha = Math.max(ALPHA_LIMIT, alpha - STEP);
        elt.ws[2] = conflicts;
    }

    @Override
    public void afterRestart() {
        if (flushWeights(q)) {
            q.clear();
            conflict.forEachEntry((a1, b) -> {
                conflict.put(a1, conflicts);
                return true;
            });
        } else {
            for (Propagator p : q.keySet()) {
                double qj = q.get(p);
                q.put(p, qj * Math.pow(DECAY, (conflicts - conflict.get(p))));
            }
            alpha = .4d;
        }
    }
}
