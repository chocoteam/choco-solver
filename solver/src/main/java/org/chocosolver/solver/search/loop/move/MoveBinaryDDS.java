/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;

/**
 * A move dedicated to run an Depth-bounded Discrepancy Search[1] (DDS) with binary decisions.
 * <p>
 * [1]:T. Walsh, Depth-bounded Discrepancy Search, IJCAI-97.
 * <p>
 * <p>
 * Note that the depth is not maintained since it is useful only when max discrepancy is greater than max depth,
 * which should not happen.
 * Created by cprudhom on 07/10/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 3.3.1
 */
public class MoveBinaryDDS extends MoveBinaryLDS {

    /**
     * Create a DFS with binary decisions
     *
     * @param strategy    how (binary) decisions are selected
     * @param discrepancy maximum discrepancy
     * @param environment backtracking environment
     */
    public MoveBinaryDDS(AbstractStrategy strategy, int discrepancy, IEnvironment environment) {
        super(strategy, discrepancy, environment);
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extended = false;
        Decision current = strategy.getDecision();
        if (current != null) { // null means there is no more decision
            solver.getDecisionPath().pushDecision(current);
            solver.getEnvironment().worldPush();
            if (dis.get() == 1) {
                solver.getDecisionPath().getLastDecision().buildNext();
            }
            dis.add(-1);
            extended = true;
        }
        return extended;
    }
}
