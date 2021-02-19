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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashMap;

/**
 * Implementation of refined DowOverWDeg.
 *
 * @author Charles Prud'homme
 * @implNote This is based on "Refining Constraint Weighting." Wattez et al. ICTAI 2019.
 * <a href="https://dblp.org/rec/conf/ictai/WattezLPT19">https://dblp.org/rec/conf/ictai/WattezLPT19</a>
 * @since 12/06/20
 */
@SuppressWarnings("rawtypes")
public class DomOverWDegRef extends AbstractCriterionBasedStrategy implements IMonitorContradiction {

    /**
     * Map (propagator - weight), where weight is the number of times the propagator fails.
     */
    protected HashMap<Integer, double[]> p2w;

    private final IncWeight incWeight;

    /**
     * Creates a DomOverWDegRef variable selector with "CACD" as weight incrementer.
     *
     * @param variables     decision variables
     * @param seed          seed for breaking ties randomly
     * @param valueSelector a value selector
     */
    public DomOverWDegRef(IntVar[] variables, long seed, IntValueSelector valueSelector) {
        this(variables, seed, valueSelector, "CACD");
    }

    /**
     * Creates a DomOverWDeg variable selector
     *
     * @param variables     decision variables
     * @param seed          seed for breaking ties randomly
     * @param valueSelector a value selector
     * @param incWeight     name of the weight incrementer to use (among: "ONE", "IA", "CA", "CD", "CACD")
     */
    public DomOverWDegRef(IntVar[] variables, long seed, IntValueSelector valueSelector, String incWeight) {
        super(variables, seed, valueSelector);
        Model model = variables[0].getModel();
        p2w = new HashMap<>(10);
        this.incWeight = IncWeight.valueOf(incWeight.toUpperCase());
    }

    @Override
    public boolean init() {
        Solver solver = vars[0].getModel().getSolver();
        if (!solver.getSearchMonitors().contains(this)) {
            vars[0].getModel().getSolver().plugMonitor(this);
        }
        return true;
    }

    @Override
    public void remove() {
        Solver solver = vars[0].getModel().getSolver();
        if (solver.getSearchMonitors().contains(this)) {
            vars[0].getModel().getSolver().unplugMonitor(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onContradiction(ContradictionException cex) {
        if (cex.c instanceof Propagator) {
            Propagator<IntVar> p = (Propagator<IntVar>) cex.c;
            double[] weigths = p2w.computeIfAbsent(p.getId(), k -> new double[p.getNbVars()]);
            if (p.getNbVars() >= weigths.length) {
                // may happen propagators (like PropNogoods) with dynamic variable addition
                double[] nws = new double[p.getNbVars()];
                System.arraycopy(weigths, 0, nws, 0, weigths.length);
                p2w.replace(p.getId(), nws);
                weigths = nws;
            }
            incWeight.clearCache();
            for (int i = 0; i < p.getNbVars(); i++) {
                if (!p.getVar(i).isInstantiated()) {
                    weigths[i] += 1d / incWeight.inc(p, p.getVar(i));
                }
            }
        }
    }

    @Override
    protected double weight(IntVar v) {
        double w = 1d;
        int nbp = v.getNbProps();
        for (int i = 0; i < nbp; i++) {
            Propagator prop = v.getPropagator(i);
            if (futVars(prop) > 1) {
                double[] ws = p2w.get(prop.getId());
                if (ws != null) {
                    int idx = v.getIndexInPropagator(i);
                    if (idx >= ws.length) {
                        // may happen propagators (like PropNogoods) with dynamic variable addition
                        double[] nws = new double[prop.getNbVars()];
                        System.arraycopy(ws, 0, nws, 0, ws.length);
                        p2w.replace(prop.getId(), nws);
                        ws = nws;
                    }
                    w += ws[idx];
                }
            }
        }
        return w / v.getDomainSize();
    }


    private enum IncWeight {
        ONE {
            @Override
            public int inc(Propagator p, IntVar x) {
                return 1;
            }
        },
        IA {
            @Override
            public int inc(Propagator p, IntVar x) {
                return p.getNbVars();
            }
        },
        CA {
            Propagator p;
            int fv;

            @Override
            void clearCache() {
                p = null;
                fv = 0;
            }

            @Override
            public int inc(Propagator p, IntVar x) {
                if (this.p == p) {
                    return this.fv;
                }
                int futVars = 0;
                for (int i = 0; i < p.getNbVars(); i++) {
                    if (!p.getVar(i).isInstantiated()) {
                        futVars++;
                    }
                }
                this.p = p;
                fv = futVars;
                return futVars;
            }
        },
        CD {
            @Override
            public int inc(Propagator p, IntVar x) {
                return 1 + x.getDomainSize();
            }
        },
        CACD {
            @Override
            void clearCache() {
                CA.clearCache();
            }

            @Override
            public int inc(Propagator p, IntVar x) {
                return CA.inc(p, x) * CD.inc(p, x);
            }
        };

        abstract int inc(Propagator p, IntVar x);

        void clearCache() {
        }
    }
}
