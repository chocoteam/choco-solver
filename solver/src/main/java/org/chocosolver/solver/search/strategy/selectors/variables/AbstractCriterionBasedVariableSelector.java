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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 26/02/2020.
 */
public abstract class AbstractCriterionBasedVariableSelector implements VariableSelector<IntVar>,
        IVariableMonitor<Variable>, IMonitorContradiction, IMonitorRestart {

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
                    w = new double[p.getNbVars()];
                    double[] nw = new double[p.getNbVars()];
                    System.arraycopy(w, 0, nw, 0, w.length);
                    w = nw;
                }
                return w;
            };

    protected static final int FLUSH_TOPS = 20;
    protected static final double FLUSH_RATIO = .9 * FLUSH_TOPS;
    protected int flushThs;

    protected final HashSet<Object> tops = new HashSet<>();
    protected int loop = 0;

    /**
     * Randomness to break ties
     */
    private final java.util.Random random;
    /***
     * Pointer to the last free variable
     */
    private final IStateInt last;
    /**
     * Temporary. Stores index of variables with the same (best) score
     */
    private final TIntArrayList bests = new TIntArrayList();
    /**
     * A reference to the Solver
     */
    protected final Solver solver;
    /**
     * Needed to save operations
     */
    final IEnvironment environment;
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
     * Scoring for each variables, is updated dynamically.
     */
    final TObjectDoubleMap<Variable> weights = new TObjectDoubleHashMap<>(15, 1.5f, 0.);
    /**
     * Refined weights,
     * for DOMWDEG_REFINED and CHS
     */
    final HashMap<Propagator<?>, double[]> refinedWeights = new HashMap<>();
    static final double[] rw = {0.};

    public AbstractCriterionBasedVariableSelector(IntVar[] vars, long seed, int flush) {
        this.random = new java.util.Random(seed);
        this.solver = vars[0].getModel().getSolver();
        this.environment = vars[0].getModel().getEnvironment();
        this.last = environment.makeInt(vars.length - 1);
        this.flushThs = flush;
    }

    @Override
    public final IntVar getVariable(IntVar[] vars) {
        IntVar best = null;
        bests.resetQuick();
        double w = Double.NEGATIVE_INFINITY;
        int to = last.get();
        for (int idx = 0; idx <= to; idx++) {
            int domSize = vars[idx].getDomainSize();
            if (domSize > 1) {
                double weight = weight(vars[idx]) / domSize;
                //System.out.printf("%3f%n", weight);
                if (w < weight) {
                    bests.resetQuick();
                    bests.add(idx);
                    w = weight;
                } else if (w == weight) {
                    bests.add(idx);
                }
            } else {
                // swap
                IntVar tmp = vars[to];
                vars[to] = vars[idx];
                vars[idx] = tmp;
                idx--;
                to--;
            }
        }
        last.set(to);
        if (bests.size() > 0) {
            //System.out.printf("%s%n", bests);
            int currentVar = bests.get(random.nextInt(bests.size()));
            best = vars[currentVar];
        }
        return best;
    }

    protected abstract double weight(IntVar v);

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

    /**
     * This method sorts elements wrt to their weight.
     * If 90% of the top 20 elements remain unchanged, then weights are flushed
     *
     * @return <i>true</i> if the weights should be flushed
     */
    protected boolean flushWeights(TObjectDoubleMap<?> q) {
        //if(true)return false;
        List<Variable> temp = weights.keySet().stream()
                .sorted(Comparator.comparingDouble(q::get))
                .limit(FLUSH_TOPS)
                .collect(Collectors.toList());
        long cnt = temp.stream().filter(tops::contains).count();
        if (cnt >= FLUSH_RATIO) {
            loop++;
        } else {
            loop = 0;
        }
        tops.clear();
        if (loop == flushThs) {
            loop = 0;
            return true;
        } else {
            tops.addAll(temp);
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////////////////// THIS IS RELATED TO INCREMENTAL FUTVARS ////////////
    //////////////////////////////////////////////////////////////////////


    final void plug(Variable var) {
        if (!observed.containsKey(var)) {
            observed.put(var, 1);
            var.addMonitor(this);
        } else {
            observed.computeIfPresent(var, (v, c) -> c + 1);
        }
    }

    private void unplug(Variable var) {
        assert observed.containsKey(var);
        Integer obs = observed.computeIfPresent(var, (v, c) -> c - 1);
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
        if (evt == IntEventType.INSTANTIATE) {
            for (Propagator<?> p : var.getPropagators()) {
                Element elt = failCount.get(p);
                if (elt != null) {
                    if (p.getVar(elt.ws[0]) == var) {
                        updateFutvars(p, elt, 0);
                    } else if (p.getVar(elt.ws[1]) == var) {
                        updateFutvars(p, elt, 1);
                    }
                }
            }
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
