/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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

/**
 * Last Conflict heuristic
 * Composite heuristic which hacks a mainStrategy by forcing the
 * use of variables involved in recent conflicts
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 03/05/2013
 */
public class LastConflict extends AbstractStrategy<Variable> implements IMonitorRestart, IMonitorSolution, IMonitorContradiction {

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
    protected AbstractStrategy<Variable> mainStrategy;

    /**
     * Set to <tt>true</tt> when this strategy is active
     */
    protected boolean active;

    /**
     * Number of conflicts stored
     */
    protected int nbCV;

    /**
     * Variables related to decision in conflicts
     */
    protected Variable[] conflictingVariables;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a last conflict heuristic
     * @param model the solver to attach this to
     * @param mainStrategy the main strategy declared
     * @param k the maximum number of conflicts to store
     */
    public LastConflict(Model model, AbstractStrategy<Variable> mainStrategy, int k) {
        super(mainStrategy.vars);
        assert k > 0 : "parameter K of last conflict must be strictly positive!";
        this.model = model;
        this.mainStrategy = mainStrategy;
        model.getSolver().plugMonitor(this);
        conflictingVariables = new Variable[k];
        nbCV = 0;
        active = false;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean init(){
        return mainStrategy.init();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decision<Variable> getDecision() {
        if (active) {
            Variable decVar = firstNotInst();
            if (decVar != null) {
                Decision d = mainStrategy.computeDecision(decVar);
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
        Variable curDecVar = model.getSolver().getDecisionPath().getLastDecision().getDecisionVariable();
        if (nbCV > 0 && conflictingVariables[nbCV - 1] == curDecVar) return;
        if (inScope(curDecVar)) {
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

    private Variable firstNotInst() {
        for (int i = nbCV - 1; i >= 0; i--) {
            if (!conflictingVariables[i].isInstantiated()) {
                return conflictingVariables[i];
            }
        }
        return null;
    }

    private boolean inScope(Variable target) {
        Variable[] scope = mainStrategy.vars;
        if (target != null) {
            for (Variable aScope : scope) {
                if (aScope.getId() == target.getId()) {
                    return true;
                }
            }
        }
        return false;
    }
}
