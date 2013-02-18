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
package solver.explanations.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.*;
import solver.explanations.strategies.jumper.MostRecentWorldJumper;
import solver.search.loop.monitors.IMonitorContradiction;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;

/**
 * This class describes operations to execute to perform Conflict-based back jumping.
 * I reacts on contradictions, by computing the decision to bracktrack to, and on solutions to by explaining
 * the refutation of the decision that leads to the solution.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class ConflictBasedBackjumping implements IDynamicBacktrackingAlgorithm, IMonitorContradiction, IMonitorSolution {

    static Logger LOGGER = LoggerFactory.getLogger("explainer");
    protected ExplanationEngine mExplanationEngine;
    protected Solver mSolver;

    protected IDecisionJumper decisionJumper;

    public ConflictBasedBackjumping(ExplanationEngine mExplanationEngine) {
        this(mExplanationEngine, new MostRecentWorldJumper());
    }

    protected ConflictBasedBackjumping(ExplanationEngine mExplanationEngine, IDecisionJumper ws) {
        this.mExplanationEngine = mExplanationEngine;
        this.mSolver = mExplanationEngine.getSolver();
        this.decisionJumper = ws;
        mSolver.getSearchLoop().plugSearchMonitor(this);
    }

    public Solver getSolver() {
        return mSolver;
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
            if (Configuration.PRINT_EXPLANATION && LOGGER.isInfoEnabled()) {
                mExplanationEngine.onContradiction(cex, complete);
            }
            int upto = decisionJumper.compute(complete, mSolver.getEnvironment().getWorldIndex());
            mSolver.getSearchLoop().overridePreviousWorld(upto);
            updateVRExplainUponbacktracking(upto, complete, cex.c);
        } else {
            throw new UnsupportedOperationException(this.getClass().getName() + ".onContradiction incoherent state");
        }
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
            mExplanationEngine.store(dec.getNegativeDeduction(), explanation);
        }
        mSolver.getSearchLoop().overridePreviousWorld(1);
    }

    /**
     * Identify the decision to reconsider, and explain its refutation in the explanation data base
     *
     * @param nworld index of the world to backtrack to
     * @param expl   explanation of the backtrack
     * @param cause  cause of the failure
     */
    protected void updateVRExplainUponbacktracking(int nworld, Explanation expl, ICause cause) {
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
            assert left.getmType() == Deduction.Type.DecLeft;
            BranchingDecision va = (BranchingDecision) left;
            mExplanationEngine.removeLeftDecisionFrom(va.getDecision(), va.getVar());

            Deduction right = dec.getNegativeDeduction();
            mExplanationEngine.store(right, mExplanationEngine.flatten(expl));
        }
        if (Configuration.PRINT_EXPLANATION && LOGGER.isInfoEnabled()) {
            LOGGER.info("::EXPL:: BACKTRACK on " + dec /*+ " (up to " + nworld + " level(s))"*/);
        }
    }
}
