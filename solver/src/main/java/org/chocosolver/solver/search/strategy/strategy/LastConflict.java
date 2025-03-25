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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.Variable;

/**
 * Last Conflict heuristic
 * Composite heuristic which hacks a mainStrategy by forcing the
 * use of variables involved in recent conflicts
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 03/05/2013
 */
public class LastConflict<V extends Variable> extends MetaStrategy<V> implements IMonitorRestart, IMonitorSolution {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /**
     * Number of conflicts stored
     */
    private int nbCV;

    /**
     * Variables related to decision in conflicts
     */
    private final V[] conflictingVariables;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a last conflict heuristic
     *
     * @param model        the solver to attach this to
     * @param mainStrategy the main strategy declared
     * @param k            the maximum number of conflicts to store
     */
    public LastConflict(Model model, AbstractStrategy<V> mainStrategy, int k) {
        super(model, mainStrategy);
//        assert k > 0 : "parameter K of last conflict must be strictly positive!";
        //noinspection unchecked
        conflictingVariables = (V[]) new Variable[k];
        nbCV = 0;
        active = false;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    @Override
    public V getSelectedVariable() {
        return firstNotInst();
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************


    @Override
    public void onContradiction(ContradictionException cex) {
        //noinspection unchecked
        V curDecVar = (V) decisionPath.getLastDecision().getDecisionVariable();
        if (nbCV > 0 && conflictingVariables[nbCV - 1] == curDecVar) return;
        if (curDecVar != null && isVarInScope(curDecVar)) {
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
