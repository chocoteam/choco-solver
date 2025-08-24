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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

import java.util.ArrayList;

/**
 * Implementation of "Guiding Backtrack Search by Tracking Variables During Constraint Propagation", C. Lecoutre et al., CP 2023.
 * <br/>
 * This implementation is not exactly the same as the one described in the paper.
 * It is a simplified version, which does not require to store the number of values removed from the domain of each variable.
 * It is based on the number of times a variable is involved in a propagation.
 * The weight of a variable is increased by the number of variables that have been involved in the same propagation
 * and takes into account the number of variables that have been involved in the same propagation
 * and the position of the variable in the list of variables involved in the propagation.
 *
 * @author Charles Prud'homme
 * @implNote <code>lVars</code> is defined as an <code>ArrayList</code>.
 * This means that a variable can be added multiple times in the list.
 * Changing this to a <code>LinkedHashSet</code> would ensure that a variable is added only once but
 * it leads to poorer performance.
 * @since 14/02/2023
 */
public class PickOnDom<V extends Variable> extends AbstractCriterionBasedVariableSelector<V>
        implements IMonitorDownBranch, IVariableMonitor<V> {

    private final ArrayList<Variable> lVars;

    public PickOnDom(V[] vars) {
        this(vars, 32);
    }

    public PickOnDom(V[] vars, int flushRate) {
        super(vars, 0, flushRate);
        this.lVars = new ArrayList<>(vars.length);
    }

    @Override
    public boolean init() {
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
            Variable[] vars = solver.getModel().getVars();
            for (Variable var : vars) {
                var.addMonitor(this);
            }
        }
        return true;
    }

    @Override
    public final void remove() {
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
            Variable[] vars = solver.getModel().getVars();
            for (Variable var : vars) {
                var.removeMonitor(this);
            }
        }
    }

    @Override
    protected double weight(V v) {
        return weights.get(v);
    }

    @Override
    public void beforeDownBranch(boolean left) {
        lVars.clear();
    }

    @Override
    public void onUpdate(V var, IEventType evt) {
        lVars.add(var);
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        int l = lVars.size();
        for(int i = 0; i < l; i++){
            weights.inc(lVars.get(i), (l - i) * 1. / l);
            i++;
        }
    }

    public void afterRestart() {
        if (flushWeights()) {
            weights.clear();
        }
    }
}
