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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.decision.Decision;

import static org.chocosolver.solver.search.strategy.decision.RootDecision.ROOT;

/**
 * A conflict-based Back-jumping strategy that relies on an explanation engine.
 * <p>
 * Created by cprudhom on 11/12/14.
 * Project: choco.
 */
public class ConflictBackJumping implements IMonitorContradiction, IMonitorSolution, ConflictStrategy {

    // The explanation engine
    final ExplanationEngine mExplainer;
    final Solver mSolver;
    private final boolean saveCauses, nogoodFromConflict;
    private final PropNogoods ngstore;
    private TIntList ps;

    // The last explanation computed, for user only
    Explanation lastExplanation;

    public ConflictBackJumping(ExplanationEngine mExplainer, Solver mSolver, boolean nogoodFromConflict) {
        this.mExplainer = mExplainer;
        this.mSolver = mSolver;
        this.saveCauses = mExplainer.isSaveCauses();
        this.nogoodFromConflict = nogoodFromConflict;
        this.ngstore = mSolver.getNogoodStore().getPropNogoods();
        this.ps = new TIntArrayList();
        this.mExplainer.setCstrat(this);
        this.mSolver.plugMonitor(this);
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        assert (cex.v != null) || (cex.c != null) : this.getClass().getName() + ".onContradiction incoherent state";
        lastExplanation = mExplainer.explain(cex);

        if (this.nogoodFromConflict) {
            lastExplanation.postNogood(ngstore, ps);
        }

        int upto = compute(mSolver.getEnvironment().getWorldIndex());
        assert upto > 0;
        mSolver.getSearchLoop().overridePreviousWorld(upto);

        identifyRefutedDecision(upto, cex.c);
    }

    @Override
    public void onSolution() {
        // we need to prepare a "false" backtrack on this decision
        Decision dec = mSolver.getSearchLoop().getLastDecision();
        while ((dec != ROOT) && (!dec.hasNext())) {
            dec = dec.getPrevious();
        }
        if (dec != ROOT) {
            Explanation explanation = mExplainer.makeExplanation(saveCauses);
            // 1. skip the current one which is refuted...
            Decision d = dec.getPrevious();
            while ((d != ROOT)) {
                if (d.hasNext()) {
                    explanation.addDecicion(d);
                }
                d = d.getPrevious();
            }
            mExplainer.storeDecisionExplanation(dec, explanation);
        }
        mSolver.getSearchLoop().overridePreviousWorld(1);
    }

//    /**
//     * Compute the jump naturally made by standard backtrack algorithm
//     * @param currentWorldIndex current world index
//     */
//    int easyNegDec(int currentWorldIndex) {
//        Decision dec = mSolver.getSearchLoop().getLastDecision(); // the current decision to undo
//        int dworld = dec.getWorldIndex() + 1;
//        while (dec != ROOT && !dec.hasNext()) {
//            dec = dec.getPrevious();
//            dworld = dec.getWorldIndex() + 1;
//        }
//        return 1 + (currentWorldIndex - dworld);
//    }

    /**
     * Identify the decision to reconsider, and explain its refutation in the explanation data base
     *
     * @param nworld index of the world to backtrack to
     */
    void identifyRefutedDecision(int nworld, ICause cause) {
        Decision dec = mSolver.getSearchLoop().getLastDecision(); // the current decision to undo
        while (dec != ROOT && nworld > 1) {
            mExplainer.freeDecisionExplanation(dec); // not mandatory, for efficiency purpose only
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != ROOT) {
            if (!dec.hasNext()) {
                throw new UnsupportedOperationException("ConflictBackJumping.identifyRefutedDecision should get to a LEFT decision:" + dec);
            }
            lastExplanation.remove(dec);
            mExplainer.storeDecisionExplanation(dec, lastExplanation);
        }
    }


    /**
     * Compute the world to backtrack to
     *
     * @param currentWorldIndex current world index
     * @return the number of world to backtrack to.
     */
    int compute(int currentWorldIndex) {
        assert currentWorldIndex >= lastExplanation.getDecisions().length();
        return currentWorldIndex - lastExplanation.getDecisions().previousSetBit(lastExplanation.getDecisions().length());
    }

    /**
     * Return the explanation of the last conflict
     *
     * @return an explanation
     */
    public Explanation getLastExplanation() {
        return lastExplanation;
    }

}
