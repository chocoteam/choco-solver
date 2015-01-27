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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.BitSet;

/**
 * A dynamic backtracking algorithm.
 * <p>
 * Created by cprudhom on 11/12/14.
 * Project: choco.
 */
public class DynamicBackTracking extends ConflictBackJumping {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicBackTracking.class);

    final DBTstrategy dbTstrategy;

    public DynamicBackTracking(ExplanationEngine mExplainer, Solver mSolver, boolean nogoodFromConflict) {
        super(mExplainer, mSolver, nogoodFromConflict);
        dbTstrategy = new DBTstrategy(mSolver, mExplainer);
        mSolver.set(dbTstrategy);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("::EXPL:: WILL BACKTRACK on " + dec /*+ " (up to " + nworld + " level(s))"*/);
        }

        // now we can explicitly enforce the jump
        dec = mSolver.getSearchLoop().getLastDecision(); // the current decision to undo
        while (dec != RootDecision.ROOT && nworld > 1) {

            if (dec.hasNext()) {
                // on a left branch, we need to keep things as is (a left branch can not depend from anything, it is always a willful decision
                dup = dec.duplicate();
                dup.setWorldIndex(dec.getWorldIndex()); // BEWARE we need to keep the wi for to maintain explanations
                dup.rewind();
                dbTstrategy.add(dup);
            } else {
                // on a right branch, necessarily have an explanation (it is a refutation)
                Explanation r = mExplainer.getDecisionRefutationExplanation(dec);
                if (!r.getDecisions().get(jmpBck.getWorldIndex())) {
                    // everything is fine ... this refutation does not depend on what we are reconsidering
                    // set it as non activated and
                    dup = dec.duplicate();
                    dup.setWorldIndex(dec.getWorldIndex()); // BEWARE we need to keep the wi for to maintain explanations
                    dup.rewind();
                    dup.buildNext();
                    // add it to the decisions to force
                    dbTstrategy.add(dup);
//                    System.out.printf("ADD: %s (%d)\n", dup, dup.getWorldIndex());
                }
                // else  we need to forget everything and start from scratch on this decision
                // so nothing to be done
            }
            // get the previous
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != RootDecision.ROOT) {
            if (!dec.hasNext()) {
                throw new UnsupportedOperationException("DynamicBackTracking.identifyRefutedDecision should get to a POSITIVE decision "+ dec);
            }
            Explanation why = lastExplanation.duplicate();
            why.remove(dec);

            mExplainer.storeDecisionExplanation(dec, why);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DynamicBackTracking>> BACKTRACK on " + dec /*+ " (up to " + nworld + " level(s))"*/);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                          Decision<IntVar> services                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private static class DBTstrategy extends AbstractStrategy {

        private final ArrayDeque<Decision<IntVar>> decision_path;
        private final Solver mSolver;
        private final ExplanationEngine mExplainer;
        private AbstractStrategy mainStrategy;

        protected DBTstrategy(Solver solver, ExplanationEngine explainer) {
            super(solver.getStrategy().getVariables());
            this.decision_path = new ArrayDeque<>();
            this.mSolver = solver;
            this.mainStrategy = mSolver.getStrategy();
            this.mExplainer = explainer;
        }

        protected void clear() {
            decision_path.clear();
        }

        protected void add(Decision<IntVar> dec) {
            decision_path.addLast(dec);
        }

        @Override
        public void init() throws ContradictionException {
            mainStrategy.init();
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
                    // then iterate over future and kept refuted decisions in the decision path and update the explanation
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
