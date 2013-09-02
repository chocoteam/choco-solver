/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.explanations.strategies;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.lns.neighbors.RandomNeighborhood;
import solver.search.loop.monitors.IMonitorUpBranch;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.variables.IntVar;

/**
 * This class extends {@link solver.search.loop.lns.neighbors.RandomNeighborhood}, but, instead of instantiating variables
 * to values, it builds a fake decision path, this enables plugging explanation in.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/07/13
 */
public class RandomNeighborhood4Explanation extends RandomNeighborhood implements IMonitorUpBranch {

    private Decision duplicator;
    private Decision last; // needed to catch up the case when a subtree is closed, and this imposes the fgmt

    public RandomNeighborhood4Explanation(Solver aSolver, IntVar[] vars, int level, long seed) {
        super(aSolver, vars, level, seed);
        mSolver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public void recordSolution() {
        super.recordSolution();
        if (duplicator == null) {
            duplicator = mSolver.getSearchLoop().decision.duplicate();
        }
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        last = null;
        super.fixSomeVariables(cause);
    }

    @Override
    protected void impose(int id, ICause cause) throws ContradictionException {
        FastDecision d = (FastDecision) duplicator.duplicate();
        d.set(vars[id], bestSolution[id], DecisionOperator.int_eq);
        last = d;
        ExplanationToolbox.imposeDecisionPath(mSolver, d);
    }

    @Override
    public void restrictLess() {
        last = null;
        super.restrictLess();
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        // we need to catch up that case when the sub tree is closed and this imposes a fragment
        if (last != null && mSolver.getSearchLoop().decision.getId() == last.getId()) {
            mSolver.getSearchLoop().restart();
        }
    }
}
