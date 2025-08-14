/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/03/2025
 */

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

public abstract class MetaStrategy<V extends Variable> extends AbstractStrategy<V> implements IMonitorContradiction {


    /**
     * The target solver
     */
    protected Model model;
    /**
     * The main strategy declared in the solver
     */
    protected final AbstractStrategy<V> mainStrategy;
    /**
     * Set to <tt>true</tt> when this strategy is active
     */
    protected boolean active;

    public MetaStrategy(Model model, AbstractStrategy<V> mainStrategy) {
        super(model, mainStrategy.vars);
        this.mainStrategy = mainStrategy;
        this.model = model;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************
    @Override
    public boolean init() {
        if (!model.getSolver().getSearchMonitors().contains(this)) {
            model.getSolver().plugMonitor(this);
        }
        return mainStrategy.init();
    }

    @Override
    public void remove() {
        this.mainStrategy.remove();
        if (model.getSolver().getSearchMonitors().contains(this)) {
            model.getSolver().unplugMonitor(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decision<V> getDecision() {
        if (active) {
            V decVar = getSelectedVariable();
            if (decVar != null) {
                Decision<V> d = mainStrategy.computeDecision(decVar);
                if (d != null) {
                    return d;
                }
            }
        }
        active = true;
        return mainStrategy.getDecision();
    }

    public abstract V getSelectedVariable();
}
