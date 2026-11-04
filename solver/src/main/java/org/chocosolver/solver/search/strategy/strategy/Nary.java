/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

/**
 * @param <V> the type of variables this strategy works with (e.g., {@link org.chocosolver.solver.variables.IntVar})
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @see MetaStrategy
 * @see IMonitorRestart
 * @see IMonitorSolution
 * @since 03/05/2013
 */
public class Nary<V extends Variable> extends MetaStrategy<V> implements IMonitorRestart, IMonitorSolution {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * @param model        the model to which this strategy will be attached
     * @param mainStrategy the underlying strategy to fall back to when no conflicting variables are available
     */
    public Nary(Model model, AbstractStrategy<V> mainStrategy) {
        super(model, mainStrategy);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public V getSelectedVariable() {
        //noinspection unchecked
        Decision<V> curDec = decisionPath.getLastDecision();
        V curDecVar = curDec.getDecisionVariable();
        if (curDecVar.isInstantiated()) {
            return null;
        }else{
            return curDecVar;
        }
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************

    @Override
    public void onContradiction(ContradictionException cex) {
        // nothing to do
    }


    @Override
    public void afterRestart() {
        active = false;
    }

    @Override
    public void onSolution() {
        active = false;
    }

}
