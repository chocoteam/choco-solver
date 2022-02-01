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
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;

/**
 * A move dedicated to run an Limited Discrepancy Search[1] (LDS) with binary decisions.
 * <p>
 * [1]:W.D. Harvey and M.L.Ginsberg, Limited Discrepancy Search, IJCAI-95.
 * <p>
 * Created by cprudhom on 07/10/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since  07/10/15
 */
public class MoveBinaryLDS extends MoveBinaryDFS {

    /**
     * current discrepancy, maintained incrementally
     */
    protected IStateInt dis;
    /**
     * max discrepancy allowed
     */
    protected int DIS;

    /**
     * Create a DFS with binary decisions
     *
     * @param strategy    how (binary) decisions are selected
     * @param discrepancy maximum discrepancy
     * @param environment backtracking environment
     */
    public MoveBinaryLDS(AbstractStrategy strategy, int discrepancy, IEnvironment environment) {
        super(strategy);
        this.dis = environment.makeInt(0);
        this.DIS = discrepancy;
    }

    @Override
    public boolean init() {
        dis.set(0);
        return super.init();
    }

    @Override
    public boolean repair(Solver solver) {
        solver.getMeasures().incBackTrackCount();
        solver.getEnvironment().worldPop();
        boolean repaired = rewind(solver);
        // increase the discrepancy max, if allowed, when the root node is reached
        Decision head = solver.getDecisionPath().getLastDecision();
        if (head.getPosition() == topDecisionPosition && dis.get() < DIS) {
            dis.add(1);
            solver.restart();
            repaired = true;
        }
        return repaired;
    }

    @Override
    protected boolean rewind(Solver solver) {
        boolean repaired = false;
        DecisionPath path = solver.getDecisionPath();
        Decision head = path.getLastDecision();
        while (!repaired && head.getPosition() != topDecisionPosition) {
            solver.setJumpTo(solver.getJumpTo()-1);
            if (dis.get() > 0 && solver.getJumpTo() <= 0 && head.hasNext()) {
                solver.getEnvironment().worldPush();
                repaired = true;
                dis.add(-1);
            } else {
                prevDecision(solver);
            }
            head = solver.getDecisionPath().getLastDecision();
        }
        return repaired;
    }
}
