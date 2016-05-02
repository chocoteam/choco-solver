/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        solver.getMeasures().incDepth();
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
