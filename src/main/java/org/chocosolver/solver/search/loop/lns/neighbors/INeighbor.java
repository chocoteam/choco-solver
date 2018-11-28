/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
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
 * @deprecated see {@link Neighbor} instead, will be removed in next release
 */
@Deprecated
public interface INeighbor {

    /**
     * @deprecated
     */
    @Deprecated
    void init();

    /**
     * @deprecated
     */
    @Deprecated
    void recordSolution();

    /**
     * @deprecated
     */
    @Deprecated
    void fixSomeVariables(DecisionPath decisionPath);

    /**
     * @deprecated
     */
    @Deprecated
    void restrictLess();

    /**
     * @deprecated
     */
    @Deprecated
    boolean isSearchComplete();

    /**
     * @deprecated
     */
    @Deprecated
    void loadFromSolution(Solution solution);
}
