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
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Last Conflict heuristic
 * Composite heuristic which hacks a mainStrategy by forcing the
 * use of variables involved in recent conflicts
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 03/05/2013
 */
public class LastConflict<V extends Variable> extends AbstractStrategy<V> implements IMonitorRestart, IMonitorSolution, IMonitorContradiction {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /**
     * The target solver
     */
    protected Model model;

    /**
     * The main strategy declared in the solver
     */
    private final AbstractStrategy<V> mainStrategy;

    /**
     * Set to <tt>true</tt> when this strategy is active
     */
    protected boolean active;

    /**
     * Number of conflicts stored
     */
    private int nbCV;

    /**
     * Variables related to decision in conflicts
     */
    private final V[] conflictingVariables;

    protected Set<Variable> scope;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a last conflict heuristic
     * @param model the solver to attach this to
     * @param mainStrategy the main strategy declared
     * @param k the maximum number of conflicts to store
     */
    public LastConflict(Model model, AbstractStrategy<V> mainStrategy, int k) {
        super(mainStrategy.vars);
        assert k > 0 : "parameter K of last conflict must be strictly positive!";
        this.model = model;
        this.mainStrategy = mainStrategy;
        this.scope = new HashSet<>(Arrays.asList(mainStrategy.vars));
        //noinspection unchecked
        conflictingVariables = (V[])new Variable[k];
        nbCV = 0;
        active = false;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean init(){
        if(!model.getSolver().getSearchMonitors().contains(this)) {
            model.getSolver().plugMonitor(this);
        }

        return mainStrategy.init();
    }

    @Override
    public void remove() {
        this.mainStrategy.remove();
        if(model.getSolver().getSearchMonitors().contains(this)) {
            model.getSolver().unplugMonitor(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decision<V> getDecision() {
        if (active) {
            V decVar = firstNotInst();
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

    //***********************************************************************************
    // Monitor
    //***********************************************************************************


    @Override
    public void onContradiction(ContradictionException cex) {
        //noinspection unchecked
        V curDecVar = (V) model.getSolver().getDecisionPath().getLastDecision().getDecisionVariable();
        if (nbCV > 0 && conflictingVariables[nbCV - 1] == curDecVar) return;
        if (scope.contains(curDecVar)) {
            if (nbCV < conflictingVariables.length) {
                conflictingVariables[nbCV++] = curDecVar;
            } else {
                assert nbCV == conflictingVariables.length;
                System.arraycopy(conflictingVariables, 1, conflictingVariables, 0, nbCV - 1);
                conflictingVariables[nbCV - 1] = curDecVar;
            }
        }
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

    private V firstNotInst() {
        for (int i = nbCV - 1; i >= 0; i--) {
            if (!conflictingVariables[i].isInstantiated()) {
                return conflictingVariables[i];
            }
        }
        return null;
    }
}
