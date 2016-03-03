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
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.decision.IntMetaDecision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.util.PoolManager;
import org.chocosolver.util.tools.StatisticUtils;

import java.util.BitSet;
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
 * <p>
 * TODO: fix some variables
 * TODO: catch up case when the sub tree is closed and this imposes a fragment
 */
public class ExplainingCut implements INeighbor {

    protected ExplanationEngine mExplanationEngine; // the explanation engine -- it works faster when it's a lazy one
    protected final Random random;

    IntMetaDecision path; // decision path that leads to a solution

    BitSet related; // a bitset indicating which decisions of the path are related to the cut
    BitSet unrelated; // a bitset to indicate which decisions of the path are NOT related to the cut
    BitSet notFrozen;
    boolean forceCft; // does the cut has already been explained?
    boolean isTerminated; // if explanations do not contain decisions, then the optimality has been proven

    double nbFixedVariables = 0d; // number of decision to fix in the set of decisions explaining the cut
    int nbCall, limit;
    final int level; // relaxing factor

    Model mModel;
    IntMetaDecision decision;
    PoolManager<IntDecision> decisionPool;

    public ExplainingCut(Model aModel, int level, long seed) {
        this.mModel = aModel;
        this.level = level;
        this.random = new Random(seed);
        path = new IntMetaDecision();
        related = new BitSet(16);
        notFrozen = new BitSet(16);
        unrelated = new BitSet(16);
        this.decision = new IntMetaDecision();
        this.decisionPool = new PoolManager<>();
    }

    @Override
    public void init() {

    }

    @Override
    public void recordSolution() {
        if (mExplanationEngine == null) {
            if (mModel.getSolver().getExplainer() == null) {
                mModel.getSolver().set(new ExplanationEngine(mModel, false, false));
            }
            this.mExplanationEngine = mModel.getSolver().getExplainer();
        }
        clonePath();
        forceCft = true;
    }

    @Override
    public Decision fixSomeVariables() {
        // this is called after restart
        // if required, force the cut and explain the cut
        if (forceCft) {
            explain();
        }
        decision.free();
        // then fix variables
        _fixVar();
        assert mModel.getSolver().getLastDecision() == RootDecision.ROOT;
        // add unrelated
        notFrozen.or(unrelated);
        for (int id = notFrozen.nextSetBit(0); id >= 0; id = notFrozen.nextSetBit(id + 1)) {
            decision.add(path.getVar(id), path.getVal(id));
        }
        return decision;
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
    }

    @Override
    public boolean isSearchComplete() {
        return isTerminated;
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
        path.free();
        Decision dec = mModel.getSolver().getLastDecision();
        while ((dec != RootDecision.ROOT)) {
            addToPath(dec);
            dec = dec.getPrevious();
        }
//        Collections.reverse(path);
    }


    /**
     * Add a copy of the current decision to path
     *
     * @param dec a decision of the current decision path
     */
    private void addToPath(Decision dec) {
        if (dec instanceof IntMetaDecision) {
            IntMetaDecision imd = (IntMetaDecision) dec;
            for (int j = 0; j < imd.size(); j++) {
                path.add(imd.getVar(j), imd.getVal(j), imd.getDop(j));
            }
        } else {
            IntDecision id = (IntDecision) dec;
            boolean tofree = false;
            if(!id.hasNext()){
                id = id.flip();
                tofree = true;
            }
            path.add(id.getDecisionVariables(), id.getDecisionValue(), id.getDecOp());
            if(tofree){
                id.free();
            }
        }
    }

    /**
     * Force the failure, apply decisions to the last solution + cut => failure!
     */
    protected void explain() {
        // Goal: force the failure to get the set of decisions related to the cut
        forceCft = false;
        // 1. make a backup
        mModel.getEnvironment().worldPush();
        IntDecision d;
        int i = 0;
        try {

            Decision previous = mModel.getSolver().getLastDecision();
            assert previous == RootDecision.ROOT;
            // 2. apply the decisions
            mExplanationEngine.getSolver().getSolver().getObjectiveManager().postDynamicCut();
            for (i = path.size() - 1; i >= 0; i--) {
                if ((d = decisionPool.getE()) == null) {
                    d = new IntDecision(decisionPool);
                }
                d.set(path.getVar(i), path.getVal(i), path.getDop(i));
                d.setPrevious(previous);
                d.buildNext();
                d.apply();
                mModel.getSolver().propagate();
                previous = d;
            }
            //mModel.propagate();
            assert false : "SHOULD FAIL!";
        } catch (ContradictionException cex) {
            if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out

                // 3. explain the failure
                Explanation explanation = mExplanationEngine.explain(cex);
                if (explanation.getDecisions().isEmpty()) {
                    isTerminated = true;
                    mModel.getEnvironment().worldPop();
                    mModel.getSolver().getEngine().flush();
                    return;
                }

                related.clear();
                related.or(explanation.getDecisions());
                explanation.recycle();

                unrelated.clear();
                unrelated.or(related);
                unrelated.flip(0, i);

                // 4. remove all decisions above i in path
                path.remove(0, i);

            } else {
                throw new UnsupportedOperationException(this.getClass().getName() + ".onContradiction incoherent state");
            }
        }
        mModel.getEnvironment().worldPop();
        mModel.getSolver().getEngine().flush();

        nbFixedVariables = related.cardinality() - 1;
        nbCall = 0;
        increaseLimit();

    }
}
