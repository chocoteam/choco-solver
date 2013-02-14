/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package solver.explanations;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorContradiction;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class ConflictBasedBackjumping implements IMonitorContradiction, IMonitorSolution {

    protected RecorderExplanationEngine mExplanationEngine;
    protected Solver mSolver;

    public ConflictBasedBackjumping(RecorderExplanationEngine mExplanationEngine) {
        this.mExplanationEngine = mExplanationEngine;
        this.mSolver = mExplanationEngine.solver;
        mExplanationEngine.solver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out
            Explanation expl = new Explanation();
            if (cex.v != null) {
                cex.v.explain(VariableState.DOM, expl);
            } else {
                cex.c.explain(null, expl);
            }
            Explanation complete = mExplanationEngine.flatten(expl);
            int upto = complete.getMostRecentWorldToBacktrack(mExplanationEngine);
            mSolver.getSearchLoop().overridePreviousWorld(upto);
            Decision dec = updateVRExplainUponbacktracking(upto, complete, cex.c);
            mExplanationEngine.emList.onContradiction(cex, complete, upto, dec);
        } else {
            throw new UnsupportedOperationException(this.getClass().getName() + ".onContradiction incoherent state");
        }
    }

    /**
     * Identifie la decision a remettre en cause
     * Met l'explication de la refutation dans la base d'explications
     * parce que comme c'est implicite au niveau de la search ca bren'apparaitrait pas sinon
     *
     * @param nworld
     * @param expl
     * @param cause
     * @return
     */
    protected Decision updateVRExplainUponbacktracking(int nworld, Explanation expl, ICause cause) {
        Decision dec = mSolver.getSearchLoop().decision; // the current decision to undo
        while (dec != RootDecision.ROOT && nworld > 1) {
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != RootDecision.ROOT) {
            if (!dec.hasNext())
                throw new UnsupportedOperationException("RecorderExplanationEngine.updatVRExplain should get to a POSITIVE decision");
            Deduction left = dec.getPositiveDeduction();
            expl.remove(left);
            assert left.mType == Deduction.Type.DecLeft;
            BranchingDecision va = (BranchingDecision) left;
            mExplanationEngine.leftbranchdecisions.get(va.getVar().getId()).remove(va.getDecision().getId());

            Deduction right = dec.getNegativeDeduction();
            mExplanationEngine.database.put(right.id, mExplanationEngine.flatten(expl));
        }
        return dec;
    }

    @Override
    public void onSolution() {
        // we need to prepare a "false" backtrack on this decision
        Decision dec = mSolver.getSearchLoop().decision;
        while ((dec != RootDecision.ROOT) && (!dec.hasNext())) {
            dec = dec.getPrevious();
        }
        if (dec != RootDecision.ROOT) {
            Explanation explanation = new Explanation();
            Decision d = dec.getPrevious();
            while ((d != RootDecision.ROOT)) {
                if (d.hasNext()) {
                    explanation.add(d.getPositiveDeduction());
                }
                d = d.getPrevious();
            }
            mExplanationEngine.database.put(dec.getNegativeDeduction().id, explanation);
        }
        mSolver.getSearchLoop().overridePreviousWorld(1);
    }

}
