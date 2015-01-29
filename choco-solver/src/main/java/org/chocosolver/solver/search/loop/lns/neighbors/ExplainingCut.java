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
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.strategies.ConflictBackJumping;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.util.tools.StatisticUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

/**
 * a specific neighborhood for LNS based on the explanation of the cut imposed by a new solution.
 * <p>
 * This neighborhood is specific in the sense that it needs to compute explanation after a new solution has been found.
 * Furthermore, the fixSomeVariables method creates and applies decisions, so that the explanation recorder can infer.
 * <br/>
 * It works as follow:
 * - on a solution: force the application of the cut together with the decision path which leads to the solution, explain the failure
 * - then, on a call to fixSomeVariables, it selects randomly K decisions explaining the cut, and relax them from the decision path.
 * <p>
 * Unrelated decisions are never relaxed, the idea here is to work only on the decisions which lead to a failure.
 *
 * @author Charles Prud'homme
 * @since 03/07/13
 */
public class ExplainingCut extends ANeighbor implements IMonitorUpBranch {

    protected ExplanationEngine mExplanationEngine; // the explanation engine -- it works faster when it's a lazy one
    protected final Random random;

    ArrayList<Decision> path; // decision path that leads to a solution

    BitSet related; // a bitset indicating which decisions of the path are related to the cut
    BitSet unrelated; // a bitset to indicate which decisions of the path are NOT related to the cut
    BitSet notFrozen;
    boolean forceCft; // does the cut has already been explained?
    boolean isTerminated; // if explanations do not contain decisions, then the optimality has been proven

    double nbFixedVariables = 0d; // number of decision to fix in the set of decisions explaining the cut
    int nbCall, limit;
    final int level; // relaxing factor

    Decision last; // needed to catch up the case when a subtree is closed, and this imposes the fgmt


    public ExplainingCut(Solver aSolver, int level, long seed) {
        super(aSolver);
        this.level = level;
        this.random = new Random(seed);
        path = new ArrayList<>(16);
        related = new BitSet(16);
        notFrozen = new BitSet(16);
        unrelated = new BitSet(16);
        mSolver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public void recordSolution() {
        if (mExplanationEngine == null) {
            if (mSolver.getExplainer() == null) {
                mSolver.set(new ExplanationEngine(mSolver, false));
            }
            this.mExplanationEngine = mSolver.getExplainer();
        }
        if (mExplanationEngine.getCstrat() == null) {
            ConflictBackJumping cbj = new ConflictBackJumping(mExplanationEngine, mSolver, false);
            mSolver.plugMonitor(cbj);
        }
        clonePath();
        forceCft = true;
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        // this is called after restart
        // if required, force the cut and explain the cut
        if (forceCft) {
            explain();
        }
        // then fix variables
        _fixVar();
        assert mSolver.getSearchLoop().getLastDecision() == RootDecision.ROOT;
        // add unrelated
        notFrozen.or(unrelated);
        // then build the fake decision path
        last = null;
        int wi = path.get(0).getWorldIndex();
//        LOGGER.debug("relax cut {}", notFrozen.cardinality());
        for (int id = notFrozen.nextSetBit(0); id >= 0; id = notFrozen.nextSetBit(id + 1)) {
//            last = ExplanationToolbox.mimic(path.get(id)); // required because some unrelated decisions can be refuted
            assert path.get(id - wi).hasNext();
            last = path.get(id - wi).duplicate();
            imposeDecisionPath(mSolver, last);
        }
    }

    protected void _fixVar() {
        // this part is specific: a fake decision path has to be created
        nbCall++;
        restrictLess();
        notFrozen.clear();
        notFrozen.or(related);
        for (; !notFrozen.isEmpty() && notFrozen.cardinality() > nbFixedVariables; ) {
            int idx = selectVariable();
            notFrozen.clear(idx);
        }
    }

    @Override
    public void restrictLess() {
        if (nbCall > limit) {
            nbFixedVariables = random.nextDouble() * related.cardinality();
            increaseLimit();
        }
        last = null;
    }

    @Override
    public boolean isSearchComplete() {
        return isTerminated;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    void increaseLimit() {
        long ank = (long) (1.2 * StatisticUtils.binomialCoefficients(related.cardinality(), (int) nbFixedVariables - 1));
        int step = (int) Math.min(ank, level);
        limit = nbCall + step;
    }

    private int selectVariable() {
        int id;
        int cc = random.nextInt(notFrozen.cardinality());
        for (id = notFrozen.nextSetBit(0); id >= 0 && cc > 0; id = notFrozen.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }


    /**
     * Compute the initial fragment, ie set of decisions to keep.
     */
    void clonePath() {
        Decision dec = mSolver.getSearchLoop().getLastDecision();
        while ((dec != RootDecision.ROOT)) {
            addToPath(dec);
            dec = dec.getPrevious();
        }
        Collections.reverse(path);
    }


    /**
     * Add a copy of the current decision to path
     *
     * @param dec a decision of the current decision path
     */
    private void addToPath(Decision dec) {
        Decision clone = dec.duplicate();
        clone.setWorldIndex(dec.getWorldIndex());
        path.add(clone);
        if (!dec.hasNext()) {
            clone.reverse();
        }
    }

    /**
     * Force the failure, apply decisions to the last solution + cut => failure!
     */
    protected void explain() {
        // Goal: force the failure to get the set of decisions related to the cut
        forceCft = false;
        // 1. make a backup
        mSolver.getEnvironment().worldPush();
        Decision d;
        int i = 0;
        try {

            Decision previous = mSolver.getSearchLoop().getLastDecision();
            assert previous == RootDecision.ROOT;
            // 2. apply the decisions
            mExplanationEngine.getSolver().getObjectiveManager().postDynamicCut();
            for (i = 0; i < path.size(); i++) {
                d = path.get(i);
                d.setPrevious(previous);
                d.buildNext();
                d.apply();
                mSolver.propagate();
                previous = d;
            }
            //mSolver.propagate();
            assert false : "SHOULD FAIL!";
        } catch (ContradictionException cex) {
            if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out

                // 3. explain the failure
                Explanation explanation = mExplanationEngine.explain(cex);
                if (explanation.getDecisions().isEmpty()) {
                    isTerminated = true;
                    mSolver.getEnvironment().worldPop();
                    mSolver.getEngine().flush();
                    return;
                }

                related.clear();
                related.or(explanation.getDecisions());

                unrelated.clear();
                unrelated.or(related);
                unrelated.flip(path.get(0).getWorldIndex(), unrelated.length());

                // 4. remove all decisions above i in path
                int j = path.size() - 1;
                while (j > i) {
                    path.remove(j);
                    j--;
                }
                // 5. rewind all other decisions
                while (j >= 0) {
                    Decision dec = path.get(j);
                    dec.setPrevious(null); // useless .. but ... you know
                    dec.rewind();
                    j--;
                }

            } else {
                throw new UnsupportedOperationException(this.getClass().getName() + ".onContradiction incoherent state");
            }
        }
        mSolver.getEnvironment().worldPop();
        mSolver.getEngine().flush();

        nbFixedVariables = related.cardinality() - 1;
        nbCall = 0;
        increaseLimit();

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
