/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.IntCircularQueue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/13
 */
public class PGN4Explanation extends PropagationGuidedNeighborhood implements IMonitorUpBranch {

    IntCircularQueue queue;
    private Decision duplicator;
    private Decision last; // needed to catch up the case when a subtree is closed, and this imposes the fgmt


    public PGN4Explanation(Solver solver, IntVar[] vars, long seed, int fgmtSize, int listSize) {
        super(solver, vars, seed, fgmtSize, listSize);
        queue = new IntCircularQueue(vars.length);
    }

    @Override
    public void recordSolution() {
        super.recordSolution();
        if (duplicator == null) {
            duplicator = mSolver.getSearchLoop().getLastDecision().duplicate();
        }
    }

    @Override
    public void restrictLess() {
        last = null;
        super.restrictLess();
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        queue.clear();
        mSolver.getEnvironment().worldPush();
        super.fixSomeVariables(cause);
        mSolver.getEnvironment().worldPop();
        while (!queue.isEmpty()) {
            int id = queue.pollFirst();
            FastDecision d = (FastDecision) duplicator.duplicate();
            d.set(vars[id], bestSolution[id], DecisionOperator.int_eq);
            last = d;
            imposeDecisionPath(mSolver, d);
        }
    }

    @Override
    protected void impose(int id, ICause cause) throws ContradictionException {
        super.impose(id, cause);
        queue.addLast(id);
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        // we need to catch up that case when the sub tree is closed and this imposes a fragment
        if (last != null && mSolver.getSearchLoop().getLastDecision().getId() == last.getId()) {
            mSolver.getSearchLoop().restart();
        }
    }

    /**
     * Simulate a decision path, with backup
     *
     * @param aSolver  the concerned solver
     * @param decision the decision to apply
     * @throws ContradictionException
     */
    private static void imposeDecisionPath(Solver aSolver, Decision decision) throws ContradictionException {
        IEnvironment environment = aSolver.getEnvironment();
        ObjectiveManager objectiveManager = aSolver.getObjectiveManager();
        // 1. simulates open node
        Decision current = aSolver.getSearchLoop().getLastDecision();
        decision.setPrevious(current);
        decision.setWorldIndex(environment.getWorldIndex());
        aSolver.getSearchLoop().setLastDecision(decision);
        if (decision.triesLeft() == 2) {
            aSolver.getSearchLoop().getSMList().beforeDownLeftBranch();
        } else {
            aSolver.getSearchLoop().getSMList().beforeDownRightBranch();
        }
        // 2. simulates down branch
        environment.worldPush();
        decision.buildNext();
        objectiveManager.apply(decision);
        objectiveManager.postDynamicCut();
//        aSolver.getEngine().propagate();
    }
}
