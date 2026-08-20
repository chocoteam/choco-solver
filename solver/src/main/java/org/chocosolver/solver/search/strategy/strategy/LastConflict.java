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
 * Last Conflict search strategy.
 * <p>
 * A conflict-driven variable selection heuristic that prioritizes variables involved in recent
 * contradictions (conflicts) during the search process. This strategy wraps a main strategy and
 * overrides its variable selection to first consider variables from the most recent conflicts,
 * helping the solver escape from regions of the search space that are prone to failures.
 * <p>
 * When a contradiction occurs, the variable responsible for the failing decision is recorded in
 * a fixed-size history (sliding window) of maximum size <i>k</i>. The strategy then attempts to select
 * a non-instantiated variable from this history before falling back to the main strategy.
 * <p>
 * This strategy implements both {@link IMonitorRestart} and {@link IMonitorSolution} to reset its
 * internal state after restarts and when solutions are found, ensuring a fresh conflict history
 * for each new search phase.
 *
 * @param <V> the type of variables this strategy works with (e.g., {@link org.chocosolver.solver.variables.IntVar})
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 03/05/2013
 * @see MetaStrategy
 * @see IMonitorRestart
 * @see IMonitorSolution
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
     * Constructs a Last Conflict search strategy.
     * <p>
     * Creates a conflict-driven variable selection heuristic that wraps the specified main strategy.
     * The strategy will prioritize variables involved in the most recent contradictions during the search.
     * <p>
     * The strategy maintains an internal history of size {@code k} to track variables from the most recent
     * conflicts. When this history is full, older conflicts are discarded as new ones are added (sliding window).
     * <p>
     *
     * @param model the model to which this strategy will be attached
     * @param mainStrategy the underlying strategy to fall back to when no conflicting variables are available
     * @param k the maximum size of the conflict history (number of conflicts to store)
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

    /**
     * Returns the first non-instantiated variable from the recent conflicts list.
     * <p>
     * This method prioritizes variables involved in recent conflicts to guide the search
     * toward resolving the sources of failure. It scans the conflict history in reverse
     * order (from most to least recent) and returns the first variable that has not been
     * instantiated yet. If all conflicting variables are instantiated or the history is empty,
     * it returns {@code null}, allowing the main strategy to take over.
     *
     * @return the first non-instantiated conflicting variable, or {@code null} if none exist
     */
    @Override
    public V getSelectedVariable() {
        return firstNotInst();
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************


    /**
     * Callback method invoked when a contradiction occurs during the search.
     * <p>
     * This method records the variable involved in the failing decision and updates the conflict
     * history. It implements a conflict-driven heuristic by tracking variables that lead to
     * contradictions, enabling the search to focus on resolving these conflicts first.
     * <p>
     * The method first identifies the failing decision from the decision path.
     * The variable from this decision is then added to the conflict history.
     * <p>
     * The conflict history is maintained as a sliding window of maximum size {@code k} (as defined
     * in the constructor). When the history is full, the oldest conflict is dropped to make room
     * for the new one. Duplicate consecutive conflicts on the same variable are ignored.
     *
     * @param cex the contradiction exception that triggered this callback
     */
    @Override
    public void onContradiction(ContradictionException cex) {
        int k = decisionPath.size() - 1;
        Decision<?> curDec = decisionPath.getDecision(k);
        if (!curDec.hasNext() && curDec.getArity() > 1) {
            return;
        }
        //noinspection unchecked
        V curDecVar = (V) curDec.getDecisionVariable();
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


    /**
     * Callback method invoked after a restart occurs in the search.
     * <p>
     * Deactivates the conflict-driven heuristic by setting the active flag to {@code false}.
     * This ensures that the conflict history is cleared and the main strategy takes over
     * after a restart, providing a fresh start for the search process.
     * <p>
     * This implementation satisfies the {@link IMonitorRestart} interface.
     */
    @Override
    public void afterRestart() {
        active = false;
    }

    /**
     * Callback method invoked when a solution is found during the search.
     * <p>
     * Deactivates the conflict-driven heuristic by setting the active flag to {@code false}.
     * This ensures that the conflict history is reset when a solution is found, allowing
     * the search to begin anew with the main strategy for subsequent solution searches.
     * <p>
     * This implementation satisfies the {@link IMonitorSolution} interface.
     */
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
