/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;

/**
 * An interface that defines services required for the LNS
 * to select variables to freeze-unfreeze (for any type of variables). <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public interface INeighbor extends ICause {

    /**
     * Initialize this neighbor
     */
    default void init(){
        // Intentionally left empty.
    }

    /**
     * Record values of decision variables to freeze some ones during the next LNS run
     */
    void recordSolution();

    /**
     * Freezes some variables in order to have a fast computation. The fixed variables are declared
     * as decisions in the decision path.
     */
    void fixSomeVariables() throws ContradictionException;

    /**
     * Use less restriction at the beginning of a LNS run in order to get better solutions Called
     * when no solution was found during a LNS run (trapped into a local optimum)
     */
    default void restrictLess(){
        // Intentionally left empty.
    }

    /**
     * @return true iff the search is in a complete mode (no fixed variable)
     */
    default boolean isSearchComplete(){
        return false;
    }

    /**
     * Load a solution and record it
     *
     * @param solution a solution to record
     */
    void loadFromSolution(Solution solution);
}
