/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations.strategies;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitPropagation;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayDeque;
import java.util.BitSet;

/**
 * A dynamic backtracking algorithm.
 * <p>
 * Created by cprudhom on 11/12/14.
 * Project: choco.
 */
public class DynamicBackTracking extends ConflictBackJumping {


    final DBTstrategy dbTstrategy;
    final RuleStore mRuleStore;  // required to continue the computation of the explanations
    final IEventStore mEventStore; // required to continue the computation of the explanations

    public DynamicBackTracking(ExplanationEngine mExplainer, Solver mSolver, boolean nogoodFromConflict) {
        super(mExplainer, mSolver, nogoodFromConflict);
        dbTstrategy = new DBTstrategy(mSolver, mExplainer);
        mRuleStore = mExplainer.getRuleStore();
        mEventStore = mExplainer.getEventStore();
    }

    /**
     * Main reason of the class
     *
     * @param nworld index of the world to backtrack to
     */
    @SuppressWarnings("unchecked")
    void identifyRefutedDecision(int nworld, ICause cause) {
        dbTstrategy.clear();
        if (nworld == 1 || cause == mSolver.getObjectiveManager()) {
            super.identifyRefutedDecision(nworld, cause);
            return;
        }
        // preliminary : compute where to jump back
        Decision dup, dec = mSolver.getSearchLoop().getLastDecision(); // the current decision to undo
        int myworld = nworld;
        while (dec != RootDecision.ROOT && myworld > 1) {
            dec = dec.getPrevious();
            myworld--;
        }
        Decision jmpBck = dec;

        // now we can explicitly enforce the jump
        dec = mSolver.getSearchLoop().getLastDecision(); // the current decision to undo
        int decIdx = lastExplanation.getEvtstrIdx(); // index of the decision to refute in the event store
        while (dec != RootDecision.ROOT && nworld > 1) {

            if (dec.hasNext()) {
                // on a left branch, we need to keep things as is (a left branch can not depend from anything, it is always a unrelated decision
                dup = dec.duplicate();
                dup.setWorldIndex(dec.getWorldIndex()); // BEWARE we need to keep the wi for to maintain explanations
                dup.rewind();
                dbTstrategy.add(dup);
            } else {
                // on a right branch, necessarily have an explanation (it is a refutation)
                Explanation anExplanation = mExplainer.getDecisionRefutationExplanation(dec);
                // if the explication of the refutation
                if (anExplanation.getEvtstrIdx() > 0) {
                    keepUp(anExplanation, decIdx);
                }

                if (!anExplanation.getDecisions().get(jmpBck.getWorldIndex())) {
                    // everything is fine ... this refutation does not depend on what we are reconsidering
                    // set it as non activated and
                    dup = dec.duplicate();
                    dup.setWorldIndex(dec.getWorldIndex()); // BEWARE we need to keep the wi for to maintain explanations
                    dup.rewind();
                    dup.buildNext();
                    // add it to the decisions to force
                    dbTstrategy.add(dup);
                } else {
                    // else  we need to forget everything and start from scratch on this decision
                    mExplainer.freeDecisionExplanation(dec); // not mandatory, for efficiency purpose only
                }
            }
            // get the previous
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != RootDecision.ROOT) {
            if (!dec.hasNext()) {
                throw new UnsupportedOperationException("DynamicBackTracking.identifyRefutedDecision should get to a POSITIVE decision " + dec);
            }
            lastExplanation.remove(dec);
            mExplainer.storeDecisionExplanation(dec, lastExplanation);
        }
    }

    /**
     * Keep up the calculation of the explanation, until it reaches 'decIdx' index of the decision to refute in the event store
     *
     * @param anExplanation explanation to go on computing
     * @param decIdx        index, in the event store, of the decision to refute
     */
    private void keepUp(Explanation anExplanation, int decIdx) {
        //Rules oRules = mRuleStore.getRules(); // temporary store the set of rules of the rule store
        int i = anExplanation.getEvtstrIdx() - 1; // skip the last known one
//        mRuleStore.setRules(anExplanation.getRules()); // replace the rules by the one related to the explanation
        mRuleStore.init(anExplanation);
        // while (i > -1) { // force to compute entirely the explanation, but inefficient in practice
        while (i >= decIdx) { // we continue while we did not reach at least 'decIdx'
            if (mRuleStore.match(i, mEventStore)) {
                mRuleStore.update(i, mEventStore, anExplanation);
            }
            i--;
        }
        anExplanation.setEvtstrIdx(i + 1); // we store where the search ends, for future research
        if (i == 0) {
            anExplanation.getRules().clear(); // only if we're sure the explanation is complete
        }
//        mRuleStore.setRules(oRules); // store the set of rules of the rule store
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                          Decision<IntVar> services                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private static class DBTstrategy extends AbstractStrategy implements IMonitorInitPropagation {

        private final ArrayDeque<Decision<IntVar>> decision_path;
        private final Solver mSolver;
        private final ExplanationEngine mExplainer;
        private AbstractStrategy mainStrategy;

        protected DBTstrategy(Solver solver, ExplanationEngine mExplainer) {
            super(new Variable[0]);
            this.decision_path = new ArrayDeque<>();
            this.mSolver = solver;
            this.mExplainer = mExplainer;
            this.mSolver.plugMonitor(this);
        }

        protected void clear() {
            decision_path.clear();
        }

        protected void add(Decision<IntVar> dec) {
            decision_path.addLast(dec);
        }

        @Override
        public boolean init(){
            return mainStrategy.init();
        }

        @Override
        public Decision getDecision() {
            if (decision_path.size() > 0) {
                int wi = mSolver.getEnvironment().getWorldIndex();
                Decision d = decision_path.pollLast();
                int old = d.getWorldIndex();
//                System.out.printf("MOVE %s (%d -> %d)\n", d, old, wi);
                if (old != wi) {
                    if (d.triesLeft() == 1) { // previously explained refuted decision needs to be kept
                        mExplainer.moveDecisionRefutation(d, wi);
                    }
                    // then iterate over future and kept refuted decisions in the decision path and update the reason
                    for (Decision n : decision_path) {
                        if (n.triesLeft() == 1) {
                            BitSet bt = mExplainer.getDecisionRefutationExplanation(n).getDecisions();
                            if (bt.get(old)) {
//                                System.out.printf("UPDATE %s (%d -> %d)\n", n, old, wi);
                                bt.clear(old);
                                bt.set(wi);
                            }
                        }
                    }
                }
                d.setWorldIndex(wi);
                return d;
            }
            return mainStrategy.getDecision();
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            if (decision_path.size() > 0) str.append(decision_path.toString());
            str.append(mainStrategy.toString());
            return str.toString();
        }

        @Override
        public void beforeInitialPropagation() {

        }

        @Override
        public void afterInitialPropagation() {
            this.mainStrategy = mSolver.getStrategy();
            mSolver.set(this);
        }
    }
}
