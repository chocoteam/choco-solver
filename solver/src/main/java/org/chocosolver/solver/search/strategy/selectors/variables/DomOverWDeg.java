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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Implementation of DowOverWDeg[1].
 * <p>
 * [1]: F. Boussemart, F. Hemery, C. Lecoutre, and L. Sais, Boosting Systematic Search by Weighting
 * Constraints, ECAI-04. <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/12
 */
public class DomOverWDeg<V extends Variable>
        extends AbstractCriterionBasedVariableSelector<V>
        implements IMonitorRestart, IVariableMonitor<V> {

    /**
     * An element helps to keep 2 things up to date:
     * 1. failure counter
     * 2. watched literals of non instantiated variables
     */
    final static class Element {
        final int[] ws;
        public Element(int count, int w0, int w1) {
            this.ws = new int[]{w0, w1, count};
        }
    }

    /**
     * Stores for each propagator, its {@link Element}.
     */
    final HashMap<Propagator<?>, Element> failCount = new HashMap<>();

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
     * Required to store for each variable the number of watchers (ie, propagator and futvars)
     */
    private final HashMap<Variable, Integer> observed = new HashMap<>();

    /**
     * Refined weights for DOMWDEG_REFINED
     */
    final HashMap<Propagator<?>, double[]> refinedWeights = new HashMap<>();

    /**
     * Update the watched literals of a propagator when a variable is instantiated
     */
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
     * Creates a DomOverWDeg variable selector
     *
     * @param variables decision variables
     * @param seed      seed for breaking ties randomly
     */
    public DomOverWDeg(V[] variables, long seed) {
        this(variables, seed, Integer.MAX_VALUE);
    }

    /**
     * Creates a DomOverWDeg variable selector
     *
     * @param variables decision variables
     * @param seed      seed for breaking ties randomly
     * @param flushThs  flush threshold, when reached, it flushes scores
     */
    public DomOverWDeg(V[] variables, long seed, int flushThs) {
        super(variables, seed, flushThs);
    }


    @Override
    public final boolean init() {
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
    protected final double weight(Variable v) {
        //assert weightW(v) == weights.get(v) : "wrong weight for " + v + ", expected " + weightW(v) + ", but found " + weights.get(v);
        return 1 + weights.get(v);
    }

    @Override
    public void onContradiction(ContradictionException cex) {
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

    void increase(Propagator<?> prop, Element elt, double[] ws) {
        // Increase weights of all variables in this propagator
        // even if they are already instantiated
        int s = prop.getModel().getEnvironment().getWorldIndex();
        int dj = prop.getVar(elt.ws[0]).instantiationWorldIndex();
        int dk = prop.getVar(elt.ws[1]).instantiationWorldIndex();
        boolean futVar1 = Math.min(dj, dk) < s; // that is, futvars == 1 until we reach 'dk'
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (prop.getVar(i).isAConstant() || !VariableUtils.isInt(prop.getVar(i))) continue;
            IntVar ivar = (IntVar) prop.getVar(i);
            // recall that variable at 0 is the 'deepest' one
            if (i == elt.ws[0] && futVar1) {
                // it should be restored upon backtrack
                environment.saveAt(() -> weights.inc(ivar, 1), dk);
            } else {
                weights.inc(ivar, 1.);
            }
            ws[i] += 1;
        }
    }

    @Override
    final int remapInc() {
        return 1;
    }

    @Override
    public void afterRestart() {
        if (flushWeights()) {
            weights.clear();
        }
    }


    /// ///////////////////////////////////////////////////////////////////
    /// /////////////// THIS IS RELATED TO INCREMENTAL FUTVARS ////////////
    /// ///////////////////////////////////////////////////////////////////

    private final BiFunction<Variable, Integer, Integer> inc = (v, i) -> i + 1;

    final void plug(Variable var) {
        if (!observed.containsKey(var)) {
            observed.put(var, 1);
            var.addMonitor(this);
        } else {
            observed.compute(var, inc);
        }
    }

    private final BiFunction<Variable, Integer, Integer> dec = (v, i) -> i - 1;

    private void unplug(Variable var) {
        assert observed.containsKey(var);
        Integer obs = observed.compute(var, dec);
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

    /**
     * Update the watched literals of a propagator when a variable is instantiated
     * @param p the propagator
     * @param elt the element associated with the propagator
     * @param i the index of the watched variable to update
     */
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
                weights.inc(other, -delta[0]);
                // but it should be restored upon backtrack
                environment.save(() -> {
                    double ww = weights.get(other) + delta[0];
                    ww = Math.max(ww, 0.);
                    weights.set(other, ww);
                });
            }
        }
    }

    // <-- FOR DEBUGGING PURPOSE ONLY
    /*double weightW(IntVar v) {
        int w = 0;
        int nbp = v.getNbProps();
        for (int i = 0; i < nbp; i++) {
            Propagator<?> prop = v.getPropagator(i);
            // BEWARE: propagators that accept to add dynamically variables led to trouble
            // when it comes to compute their weight incrementally.
            w += futvarsW(prop);
        }
        return w;
    }

    int futvarsW(Propagator<?> prop) {
        int futVars = 0;
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (!prop.getVar(i).isInstantiated()) {
                if (++futVars > 1) {
                    Element elt = failCount.get(prop);
                    if (elt != null) {
                        return elt.ws[2];
                    } else break;
                }
            }
        }
        return 0;
    }*/
    // FOR DEBUGGING PURPOSE ONLY  -->
}
