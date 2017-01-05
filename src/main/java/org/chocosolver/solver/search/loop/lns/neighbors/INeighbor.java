/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;

/**
 * An interface defining services required for the LNS to select variables to freeze-unfreeze.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public interface INeighbor {

    /**
     * Initialize this neighbor
     */
    void init();

    /**
     * Record values of decision variables to freeze some ones during the next LNS run
     */
    void recordSolution();

    /**
     * Freezes some variables in order to have a fast computation.
     * The fixed variables are declared as decisions in the decision path.
     *
     * @param decisionPath the decision path in which declaring variable to freeze
     */
    void fixSomeVariables(DecisionPath decisionPath);

    /**
     * Use less restriction at the beginning of a LNS run
     * in order to get better solutions
     * Called when no solution was found during a LNS run (trapped into a local optimum)
     */
    void restrictLess();

    /**
     * @return true iff the search is in a complete mode (no fixed variable)
     */
    boolean isSearchComplete();

    /**
     * Load a solution and record it
     * @param solution a solution to record
     */
    void loadFromSolution(Solution solution);
}
