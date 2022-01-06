/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.Variable;

import java.util.function.Predicate;

/**
 * Last Conflict heuristic
 * Composite heuristic which hacks a mainStrategy by forcing the
 * use of variables involved in recent conflicts
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 03/05/2013
 */
public class AntiLastConflict<V extends Variable> implements IMonitorRestart, IMonitorSolution, IMonitorContradiction, Predicate<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /**
     * The target solver
     */
    protected Model model;
    /**
     * Set to <tt>true</tt> when this strategy is active
     */
    protected boolean active;
    /**
     * Variables related to decision in conflicts
     */
    private V conflictingVariable;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a last conflict heuristic
     * @param model the solver to attach this to
     */
    public AntiLastConflict(Model model) {
        this.model = model;
        //noinspection unchecked
        conflictingVariable = null;
        active = false;
        model.getSolver().plugMonitor(this);
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************

    @Override
    public void onContradiction(ContradictionException cex) {
        //noinspection unchecked
        V curDecVar = (V) model.getSolver().getDecisionPath().getLastDecision().getDecisionVariable();
        conflictingVariable = curDecVar;
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        active = false;
    }

    @Override
    public void onSolution() {
        active = false;
    }
    //***********************************************************************************

    //***********************************************************************************


    @Override
    public boolean test(V v) {
        return v != conflictingVariable;
    }
}
