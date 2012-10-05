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

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class DynamicBacktracking extends AbstractStrategy<Variable> implements ISearchMonitor {

    RecorderExplanationEngine mExplanationEngine;
    Solver mSolver;

    AbstractStrategy<Variable> dStrategy;

    ArrayList<Decision> decision_path; // list of decisions describing the decision path
    int fix_path; // part of the decision path that should not be modified on restart
    int current;


    public DynamicBacktracking(RecorderExplanationEngine mExplanationEngine) {
        super(new Variable[0]);
        this.mExplanationEngine = mExplanationEngine;
        this.mSolver = mExplanationEngine.solver;
        mSolver.getSearchLoop().plugSearchMonitor(this);
        decision_path = new ArrayList<Decision>(8);
        mSolver.getSearchLoop().restartAfterEachFail(true);  // TODO: remove when a better solution has been found
    }

    @Override
    public void beforeInitialize() {
        this.dStrategy = mSolver.getSearchLoop().getStrategy();
        mSolver.getSearchLoop().set(this);
    }

    @Override
    public void init() throws ContradictionException {
        this.dStrategy.init();
    }

    @Override
    public Decision getDecision() {
        if (current == decision_path.size()) {
            return dStrategy.getDecision();
        } else {
            Decision dec;
            // first apply the fix part
            if (current < fix_path) {
                dec = decision_path.get(current++); // from the last decision to the first one
                dec.setPrevious(null);
            }
            // otherwise, change the way decisions are applied
            else {
                dec = decision_path.get(current++); // from the last decision to the first one

                dec.setPrevious(null);
                // TODO: is it really mandatory ?
                Explanation explanation = Explanation.build();
                Decision d = dec.getPrevious();
                while ((d != null)) {
                    if (d.hasNext()) {
                        explanation.add(d.getPositiveDeduction());
                    }
                    d = d.getPrevious();
                }
                mExplanationEngine.database.put(dec.getNegativeDeduction().id, explanation);
            }
//            AbstractStrategy.LOGGER.info("OVERRIDE DECISION : {}", dec);
            return dec;
        }
    }

    public void onContradiction(ContradictionException cex) {
        if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out
            Explanation expl = (cex.v != null) ? cex.v.explain(VariableState.DOM)
                    : cex.c.explain(null);
            Explanation complete = mExplanationEngine.flatten(expl);

            decision_path.clear();
//            AbstractStrategy.LOGGER.info("START PATH REPAIR");
            int topworld, nworld;
            nworld = topworld = mSolver.getEnvironment().getWorldIndex();
            Decision dec = mSolver.getSearchLoop().decision; // the current decision to undo
            while (dec != RootDecision.ROOT) {
                Deduction ded;
//                System.out.printf("CHECK %s\n", dec);
                // Find the deduction related to the decision
                if (dec.hasNext()) { // variable assignment
                    ded = dec.getPositiveDeduction();
                } else { // variable refutation
                    ded = dec.getNegativeDeduction();
                }
                if (complete.contain(ded))
                // IF the current decision is part of the explanation
                //      IF left branch: let the engine compute the oppsite, and post it
                //      ELSE right branch: no opposite possible, should fail => compute the opposite and post it
                {
                    // NOTHING TO DO except getting the world index for the dbt
                    if (nworld < topworld) { // we look for the "most shallow" index
                        topworld = nworld;
                    }
//                    AbstractStrategy.LOGGER.info("{} in XP", dec);
                    // we make a copy, because decisions are freed on backtracking
                    decision_path.add(dec.copy());
                } else
                // ELSE the current decision is not part of the explanation, move it back to apply it as is.
                {
                    dec.buildPrevious();
                    // we make a copy, because decisions are freed on backtracking
                    decision_path.add(dec.copy());
                }


                dec = dec.getPrevious();
                nworld--;
            }
            // Then remove decisions between ROOT decision and the first one belonging to the explanation
            fix_path = topworld - (nworld + 1);
            current = 0;
            // Compute the world index to jump to
            int upto = 2 + (mSolver.getEnvironment().getWorldIndex() - topworld);
            mSolver.getSearchLoop().overridePreviousWorld(upto);
            // CPRU: what is it for ?
            /*dec = updateVRExplainUponbacktracking(upto, complete);
            mExplanationEngine.emList.onContradiction(cex, complete, upto, dec);*/
//            AbstractStrategy.LOGGER.info("END PATH REPAIR");
        } else {
            throw new UnsupportedOperationException("DynamicBacktracking.onContradiction incoherent state");
        }
    }

    /*protected Decision updateVRExplainUponbacktracking(int nworld, Explanation expl) {
        Decision dec = mSolver.getSearchLoop().decision; // the current decision to undo
        while (dec != RootDecision.ROOT && nworld > 0) {
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != RootDecision.ROOT) {
            if (!dec.hasNext()) {
                //                throw new UnsupportedOperationException("RecorderExplanationEngine.updateVRExplain should get to a POSITIVE decision");
                Deduction vr = dec.getNegativeDeduction();
                mExplanationEngine.database.put(vr.id, mExplanationEngine.flatten(expl));
                return dec;
            }
            Deduction vr = dec.getNegativeDeduction();
            Deduction assign = dec.getPositiveDeduction();
            expl.remove(assign);
            if (assign.mType == Deduction.Type.VarAss) {
                VariableAssignment va = (VariableAssignment) assign;
                mExplanationEngine.variableassignments.get(va.var.getId()).remove(va.val);
            }
            mExplanationEngine.database.put(vr.id, mExplanationEngine.flatten(expl));
        }
        return dec;
    }*/


    @Override
    public void onSolution() {
        // we need to prepare a "false" backtrack on this decision
        Decision dec = mSolver.getSearchLoop().decision;
        while ((dec != RootDecision.ROOT) && (!dec.hasNext())) {
            dec = dec.getPrevious();
        }
        if (dec != RootDecision.ROOT) {
            Explanation explanation = Explanation.build();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void afterInitialize() {
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
    }

    @Override
    public void beforeOpenNode() {
    }

    @Override
    public void afterOpenNode() {
    }

    @Override
    public void beforeDownLeftBranch() {
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
    }

    @Override
    public void afterDownRightBranch() {
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
    }

    @Override
    public void afterInterrupt() {
    }

    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
    }
}
