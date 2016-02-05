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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayDeque;
import java.util.BitSet;

/**
 * Dynamic Backtracking[1] (DBT) explanation stategy.
 * It backtracks up to most recent decision involved in the explanation, keep unrelated ones.
 * <p>
 * [1]: M.L. Ginsberg, Dynamic Backtracking, JAIR (1993).
 * <p>
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class LearnDBT extends LearnCBJ {

    /**
     * The strategy which provides already computed decisions unrelated to the last conflict.
     */
    final DBTstrategy dbTstrategy;

    /**
     * Because computing explanation can be lazy, a {@link RuleStore} is needed to continue computing partial explanations.
     * A reference to the one used by the explanation engine is thus needed.
     */
    final RuleStore mRuleStore;

    /**
     * Because computing explanation can be lazy, a {@link IEventStore} is needed to continue computing partial explanations.
     * A reference to the one used by the explanation engine is thus needed.
     */
    final IEventStore mEventStore;

    /**
     * Create a Dynamic Backtracking strategy.
     * @param mModel the solver to instrument
     * @param nogoodFromConflict set to <tt>true</tt> to extract nogoods from explanations.
     * @param userFeedbackOn set to <tt>true</tt> to record causes in explanations (required for user feedback mainly).
     */
    public LearnDBT(Model mModel, boolean nogoodFromConflict, boolean userFeedbackOn) {
        super(mModel, nogoodFromConflict, userFeedbackOn);
        dbTstrategy = new DBTstrategy(mModel, mExplainer);
        mRuleStore = mExplainer.getRuleStore();
        mEventStore = mExplainer.getEventStore();
    }


    /**
     * Main reason of the class
     *
     * @param nworld index of the world to backtrack to
     */
    @SuppressWarnings("unchecked")
    @Override
    void identifyRefutedDecision(int nworld) {
        dbTstrategy.clear();
        if (nworld == 1 || mModel.getEngine().getContradictionException().c == mModel.getObjectiveManager()) {
            super.identifyRefutedDecision(nworld);
            return;
        }
        // preliminary : compute where to jump back
        Decision dup, dec = mModel.getResolver().getLastDecision(); // the current decision to undo
        int myworld = nworld;
        while (dec != RootDecision.ROOT && myworld > 1) {
            dec = dec.getPrevious();
            myworld--;
        }
        Decision jmpBck = dec;

        // now we can explicitly enforce the jump
        dec = mModel.getResolver().getLastDecision(); // the current decision to undo
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
                throw new UnsupportedOperationException("LearnDBT.identifyRefutedDecision should get to a POSITIVE decision " + dec);
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
        int i = anExplanation.getEvtstrIdx() - 1; // skip the last known one
        mRuleStore.init(anExplanation);
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                          Decision<IntVar> services                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private static class DBTstrategy extends AbstractStrategy implements IMonitorInitialize {

        private final ArrayDeque<Decision<IntVar>> decision_path;
        private final Model mModel;
        private final ExplanationEngine mExplainer;
        private AbstractStrategy mainStrategy;

        protected DBTstrategy(Model model, ExplanationEngine mExplainer) {
            super(new Variable[0]);
            this.decision_path = new ArrayDeque<>();
            this.mModel = model;
            this.mExplainer = mExplainer;
            this.mModel.plugMonitor(this);
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
        public void afterInitialize() {
            this.mainStrategy = mModel.getStrategy();
            // put this strategy before any other ones.
            mModel.set(this);
        }

        @Override
        public Decision getDecision() {
            if (decision_path.size() > 0) {
                int wi = mModel.getEnvironment().getWorldIndex();
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
    }
}
