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
package org.chocosolver.solver.explanations.arlil;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.constraints.nary.cnf.SatSolver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.variables.IntVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.chocosolver.solver.search.strategy.decision.RootDecision.ROOT;

/**
 * A conflict-based Back-jumping strategy that relies on ARLIL explanation engine.
 * Basically, it acts exactly like {@link org.chocosolver.solver.explanations.strategies.ConflictBasedBackjumping}.
 * <p>
 * Created by cprudhom on 11/12/14.
 * Project: choco.
 */
public class CBJ4ARLIL implements IMonitorContradiction, IMonitorSolution {

    private static final Logger LOGGER = LoggerFactory.getLogger(CBJ4ARLIL.class);

    // The ARLIL explanation engine
    final ARLILExplanationEngine mArlile;
    final Solver mSolver;
    private final boolean saveCauses, nogoodFromConflict;
    private final PropNogoods ngstore;
    private TIntList ps;

    // The last reason computed, for user only
    Reason lastReason;

    public CBJ4ARLIL(ARLILExplanationEngine mArlile, Solver mSolver, boolean nogoodFromConflict) {
        this.mArlile = mArlile;
        this.mSolver = mSolver;
        this.saveCauses = mArlile.isSaveCauses();
        this.nogoodFromConflict = nogoodFromConflict;
        this.ngstore = mSolver.getNogoodStore().getPropNogoods();
        this.ps = new TIntArrayList();
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        assert (cex.v != null) || (cex.c != null) : this.getClass().getName() + ".onContradiction incoherent state";
        lastReason = mArlile.explain(cex);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CBJ4ARLIL>> Reason of " + cex.toString() + " is " + lastReason);
        }
        if (this.nogoodFromConflict) {
            extractNogoodFromReason(lastReason);
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
            Reason reason = new Reason(saveCauses);
            // 1. skip the current one which is refuted...
            Decision d = dec.getPrevious();
            while ((d != ROOT)) {
                if (d.hasNext()) {
                    reason.addDecicion(d);
                }
                d = d.getPrevious();
            }
            mArlile.storeDecisionRefutation(dec, reason);
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
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != ROOT) {
            if (!dec.hasNext()) {
                throw new UnsupportedOperationException("CBJ4ARLIL.identifyRefutedDecision should get to a LEFT decision:" + dec);
            }
            Reason why = lastReason.duplicate();
            why.remove(dec);

            mArlile.storeDecisionRefutation(dec, why);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CBJ4ARLIL>> BACKTRACK on " + dec /*+ " (up to " + nworld + " level(s))"*/);
        }
    }


    /**
     * Compute the world to backtrack to
     *
     * @param currentWorldIndex current world index
     * @return the number of world to backtrack to.
     */
    int compute(int currentWorldIndex) {
        assert currentWorldIndex >= lastReason.getDecisions().length();
        return currentWorldIndex - lastReason.getDecisions().previousSetBit(lastReason.getDecisions().length());
    }

    /**
     * Return the Reason of the last conflict
     *
     * @return a Reason
     */
    public Reason getLastReason() {
        return lastReason;
    }

    private void extractNogoodFromReason(Reason reason) {
        Decision<IntVar> decision = mSolver.getSearchLoop().getLastDecision();
        ps.clear();
        while (decision != RootDecision.ROOT) {
            if (reason.getDecisions().get(decision.getWorldIndex())) {
                assert decision.hasNext();
//                System.out.printf("%s = %d,", decision.getDecisionVariable(), (Integer) decision.getDecisionValue());
                ps.add(SatSolver.negated(ngstore.Literal(decision.getDecisionVariable(), (Integer) decision.getDecisionValue())));
            }
            decision = decision.getPrevious();
        }
        this.ngstore.addLearnt(ps.toArray());
    }
}
