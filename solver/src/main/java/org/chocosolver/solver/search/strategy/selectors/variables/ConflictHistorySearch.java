/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

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
    extends AbstractCriterionBasedStrategy
    implements IMonitorContradiction, IMonitorRestart {

    /**
     * Decreasing step for {@link #a}.
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
    private double a = .4d;
    /**
     * The number of conflicts which have occurred since the beginning of the search.
     */
    private int conflicts = 0;
    /**
     * Last {@link #conflicts} value where a propagator led to a failure.
     */
    private final TObjectIntMap<Propagator> conflict = new TObjectIntHashMap<>(10, 0.5f, 0);


    public ConflictHistorySearch(IntVar[] vars, long seed, IntValueSelector valueSelector) {
        super(vars, seed, valueSelector);
        vars[0].getModel().getSolver().plugMonitor(this);
    }

    @Override
    public boolean init() {
        Solver solver = vars[0].getModel().getSolver();
        if(!solver.getSearchMonitors().contains(this)) {
            vars[0].getModel().getSolver().plugMonitor(this);
        }
        return true;
    }

    @Override
    public void remove() {
        Solver solver = vars[0].getModel().getSolver();
        if(solver.getSearchMonitors().contains(this)) {
            vars[0].getModel().getSolver().unplugMonitor(this);
        }
    }


    @Override
    public void onContradiction(ContradictionException cex) {
        if (cex.c instanceof Propagator) {
            Propagator p = (Propagator) cex.c;
            double qj = q.get(p);
            // compute the reward
            double r = 1d / (conflicts - conflict.get(p) + 1);
            // update q
            q.put(p, (1 - a) * qj + a * r);
            // decrease a
            a = Math.max(0.06, a - STEP);
            // update conflicts
            conflict.put(p, conflicts);
            conflicts++;
        }
    }

    @Override
    protected double weight(IntVar v) {
        double w = 0.;
        int nbp = v.getNbProps();
        for (int i = 0; i < nbp; i++) {
            Propagator prop = v.getPropagator(i);
            if (futVars(prop) > 1) {
                w += q.get(prop) + D;
            }
        }
        return w / v.getDomainSize();
    }

    @Override
    public void afterRestart() {
        for (Propagator p : q.keySet()) {
            double qj = q.get(p);
            q.put(p, qj * Math.pow(DECAY, (conflicts - conflict.get(p))));
        }
        a = .4d;
    }
}
