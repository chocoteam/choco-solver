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

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 26/02/2020.
 */
public abstract class AbstractFailureBasedVariableSelector<V extends Variable>
        extends AbstractScoreBasedValueSelector<V>
        implements IVariableMonitor<V>, IMonitorContradiction {

    /**
     * An element helps to keep 2 things up to date:
     * 1. failure counter (for DOMWDEG and DOMWDEG_REFINED) or failure history (for CHS)
     * 2. watched literals of non instantiated variables
     */
    static class Element {
        int[] ws;

        public Element(int count, int w0, int w1) {
            this.ws = new int[]{w0, w1, count};
        }
    }

    private final BiFunction<Propagator<?>, double[], double[]> remapWeights =
            (p, w) -> {
                if (w == null) {
                    // if absent
                    w = new double[p.getNbVars()];
                } else if (w.length < p.getNbVars()) {
                    // may happen propagators (like PropSat) with dynamic variable addition
                    w = Arrays.copyOf(w, p.getNbVars());
                }
                return w;
            };

    /**
     * The number of conflicts which have occurred since the beginning of the search.
     */
    int conflicts = 0;
    /**
     * Stores for each propagator, its {@link Element}.
     */
    final HashMap<Propagator<?>, Element> failCount = new HashMap<>();
    /**
     * Required to store for each variable the number of watchers (ie, propagator and futvars)
     */
    private final HashMap<Variable, Integer> observed = new HashMap<>();
    /**
     * Scoring for each variable, is updated dynamically.
     */
    final TObjectDoubleMap<Variable> weights = new TObjectDoubleHashMap<>(15, 1.5f, 0.);
    /**
     * Refined weights,
     * for DOMWDEG_REFINED and CHS
     */
    final HashMap<Propagator<?>, double[]> refinedWeights = new HashMap<>();
    static final double[] rw = {0.};

    final BiConsumer<Variable, Propagator<?>> update = (v, p) -> {
        Element elt = failCount.get(p);
        if (elt != null) {
            if (p.getVar(elt.ws[0]) == v) {
                updateFutvars(p, elt, 0);
            } else if (p.getVar(elt.ws[1]) == v) {
                updateFutvars(p, elt, 1);
            }
        }
    };


    /**
     * Create a failure based variable selector
     * @param vars scope variables
     * @param tieBreaker a tiebreaker when scores are equal
     * @param flushRate the number of restarts before cleaning the scores
     */
    public AbstractFailureBasedVariableSelector(V[] vars, Comparator<V> tieBreaker, int flushRate) {
        super(vars, tieBreaker, flushRate);
    }

    @Override
    public final void onContradiction(ContradictionException cex) {
        conflicts++;
        if (cex.c instanceof Propagator) {
            Propagator<?> prop = (Propagator<?>) cex.c;
            if (prop.getNbVars() < 2 /*|| prop instanceof PropSat*/) return;
            // store the propagator if needed
            // then update its failure counter
            Element elt = failCount.get(prop);
            if (elt == null) {
                elt = new Element(0, 0, 1);
                failCount.put(prop, elt);
            } else {
                unplug(prop.getVar(elt.ws[0]));
                unplug(prop.getVar(elt.ws[1]));
            }
            // Select two not instantiated variables
            // OR the last instantiated ones.
            int s = prop.getModel().getEnvironment().getWorldIndex();
            int j = 0;
            int dj = prop.getVar(j).instantiationWorldIndex();
            int k = 1;
            int dk = prop.getVar(k).instantiationWorldIndex();
            int i, di;
            if (dj < dk) { // j is always the deepest
                i = j;
                j = k;
                k = i;
                di = dj;
                dj = dk;
                dk = di;
            }
            for (i = 2; i < prop.getNbVars() && (dj <= s || dk <= s); i++) {
                di = prop.getVar(i).instantiationWorldIndex();
                if (di > dj) {
                    k = j;
                    dk = dj;
                    j = i;
                    dj = di;
                } else if (di > dk) {
                    k = i;
                    dk = di;
                }
            }
            elt.ws[0] = j;
            elt.ws[1] = k;
            plug(prop.getVar(j));
            plug(prop.getVar(k));

            // create, update or get weights
            elt.ws[2] += remapInc();
            double[] ws = refinedWeights.compute(prop, remapWeights);
            increase(prop, elt, ws);
        }
    }

    abstract void increase(Propagator<?> prop, Element elt, double[] ws);

    int remapInc() {
        return 0;
    }
    //////////////////////////////////////////////////////////////////////
    ////////////////// THIS IS RELATED TO INCREMENTAL FUTVARS ////////////
    //////////////////////////////////////////////////////////////////////

    private final BiFunction<Variable, Integer, Integer> inc = (v, i) -> i + 1;
    final void plug(Variable var) {
        if (!observed.containsKey(var)) {
            observed.put(var, 1);
            var.addMonitor(this);
        } else {
            observed.computeIfPresent(var, inc);
        }
    }

    private final BiFunction<Variable, Integer, Integer> dec = (v, i) -> i - 1;

    private void unplug(Variable var) {
        assert observed.containsKey(var);
        Integer obs = observed.computeIfPresent(var, dec);
        if (obs != null && obs == 0) {
            var.removeMonitor(this);
            observed.remove(var);
        }
    }

    private int next(Propagator<?> pro, int w0, int w1) {
        // there is no need to check decision level
        for (int i = 0; i < pro.getNbVars(); i++) {
            if (i != w0 && i != w1 && !pro.getVar(i).isInstantiated()) {
                return i;
            }
        }
        return w0;
    }

    @Override
    public final void onUpdate(Variable var, IEventType evt) {
        if (var.isInstantiated()) {
            var.forEachPropagator(update);
        }
    }

    private void updateFutvars(Propagator<?> p, Element elt, int i) {
        assert (p.getVar(elt.ws[i]).isInstantiated());
        // look for another free and unwatched variable
        assert i == 0 || i == 1;
        int w = next(p, elt.ws[i], elt.ws[1 - i]);
        // if it could find one
        if (w != elt.ws[i]) {
            unplug(p.getVar(elt.ws[i]));
            plug(p.getVar(w));
            elt.ws[i] = w;
        } else {
            // It means that futvars <= 1
            int k = 1 - i;
            Variable other = p.getVar(elt.ws[k]);
            if (!other.isInstantiated()) {
                // 'var' is the last one not instantiated,
                // so this counter will not be taken into account
                double[] delta = {0.};
                double[] ws = refinedWeights.get(p);
                if (elt.ws[k] < ws.length) {
                    // may happen propagators (like PropSat) with dynamic variable addition
                    delta[0] = ws[elt.ws[k]];
                }
                weights.adjustValue(other, -delta[0]);
                // but it should be restored upon backtrack
                environment.save(() -> {
                    double ww = weights.get(other) + delta[0];
                    ww = Math.max(ww, 0.);
                    weights.put(other, ww);
                });
            }
        }
    }
}
