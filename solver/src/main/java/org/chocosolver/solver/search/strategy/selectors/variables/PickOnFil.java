/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationInsight;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;

/**
 * Implementation of "Guiding Backtrack Search by Tracking Variables During Constraint Propagation", C. Lecoutre et al., CP 2023.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/02/2023
 */
public class PickOnFil<V extends Variable> extends AbstractCriterionBasedVariableSelector<V> implements IMonitorContradiction {

    private ArrayList<Propagator<?>> Lcstrs;
    private TLongArrayList Ldeltas;

    private final int variant;

    private final TObjectDoubleHashMap<Variable> scores;

    public PickOnFil(V[] vars) {
        this(vars, 3, 32);
    }

    public PickOnFil(V[] vars, int variant, int flushRate) {
        super(vars, 0, flushRate);
        this.scores = new TObjectDoubleHashMap<>(15, 1.5f, 0.);
        this.variant = variant;
    }

    @Override
    public boolean init() {
        PropagationInsight.PickOnFil pi = new PropagationInsight.PickOnFil();
        solver.getEngine().setInsight(pi);
        Lcstrs = pi.getLcstrs();
        Ldeltas = pi.getLdeltas();
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return true;
    }

    @Override
    public final void remove() {
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    @Override
    protected double weight(V v) {
        return scores.get(v);
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        long sum;
        double r;
        switch (variant) {
            case 0:
                for (int i = 0; i < Lcstrs.size(); i++) {
                    Propagator<?> lc = Lcstrs.get(i);
                    for (Variable lv : lc.getVars()) {
                        scores.adjustOrPutValue(lv, 1, 1);
                    }
                }
                break;
            case 1:
                for (int i = 0; i < Lcstrs.size(); i++) {
                    Propagator<?> lc = Lcstrs.get(i);
                    for (Variable lv : lc.getVars()) {
                        scores.adjustOrPutValue(lv, Ldeltas.get(i), Ldeltas.get(i));
                    }
                }
                break;
            case 2:
                sum = Ldeltas.sum();
                r = 100. / sum;
                for (int i = 0; i < Lcstrs.size(); i++) {
                    double amnt = r * Ldeltas.get(i);
                    Propagator<?> lc = Lcstrs.get(i);
                    for (Variable lv : lc.getVars()) {
                        scores.adjustOrPutValue(lv, amnt, amnt);
                    }
                }
                break;
            case 3:
                double n = solver.getModel().getNbVars() * 1.;
                double d = solver.getCurrentDepth() * 1.;
                sum = Ldeltas.sum();
                r = (n - d) / n * 100. / sum;
                for (int i = 0; i < Lcstrs.size(); i++) {
                    double amnt = r * Ldeltas.get(i);
                    Propagator<?> lc = Lcstrs.get(i);
                    for (Variable lv : lc.getVars()) {
                        scores.adjustOrPutValue(lv, amnt, amnt);
                    }
                }
                break;

        }
    }

    @Override
    void increase(Propagator<?> prop, Element elt, double[] ws) {
        // ignore
    }

    public void afterRestart() {
        if (flushWeights(weights)) {
            weights.clear();
        }
    }
}
