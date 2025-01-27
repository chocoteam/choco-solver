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

import gnu.trove.list.array.TLongArrayList;
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
public class PickOnDom<V extends Variable> extends AbstractCriterionBasedVariableSelector<V> implements IMonitorContradiction {

    private ArrayList<Variable> Lvars;
    private TLongArrayList Ldeltas;

    private final int variant;

    public PickOnDom(V[] vars) {
        this(vars, 0, 32);
    }

    public PickOnDom(V[] vars, int variant, int flushRate) {
        super(vars, 0, flushRate);
        this.variant = variant;
    }

    @Override
    public boolean init() {
        PropagationInsight.PickOnDom pi = new PropagationInsight.PickOnDom();
        solver.getEngine().setInsight(pi);
        Lvars = pi.getLvars();
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
        return weights.get(v);
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        long sum;
        double r;
        switch (variant) {
            case 0:
                for (Variable lvar : Lvars) {
                    weights.adjustOrPutValue(lvar, 1, 1);
                }
                break;
            case 1:
                for (int i = 0; i < Lvars.size(); i++) {
                    weights.adjustOrPutValue(Lvars.get(i), Ldeltas.get(i), Ldeltas.get(i));
                }
                break;
            case 2:
                sum = Ldeltas.sum();
                r = 100. / sum;
                for (int i = 0; i < Lvars.size(); i++) {
                    double amnt = r * Ldeltas.get(i);
                    weights.adjustOrPutValue(Lvars.get(i), amnt, amnt);
                }
                break;
            case 3:
                double n = solver.getModel().getNbVars() * 1.;
                double d = solver.getCurrentDepth() * 1.;
                sum = Ldeltas.sum();
                r = (n - d) / n * 100. / sum;
                for (int i = 0; i < Lvars.size(); i++) {
                    double amnt = r * Ldeltas.get(i);
                    weights.adjustOrPutValue(Lvars.get(i), amnt, amnt);
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
