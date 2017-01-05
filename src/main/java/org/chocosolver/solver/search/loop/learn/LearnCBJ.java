/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.PropNogoods;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.variables.IntVar;

/**
 * Conflict-based Backjumping[1] (CBJ) explanation strategy.
 * It backtracks up to most recent decision involved in the explanation, and forget younger decisions.
 * <p>
 * [1]: P. Prosser, Hybrid algorithms for the constraint satisfaction problem, Computational Intelligence (93).
 * <p>
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 * @author Charles Prud'homme, Narendra Jussien
 */
public class LearnCBJ extends LearnExplained {

    /**
     * Indicates if nogoods must be extracted from explanations.
     */
    private final boolean nogoodFromConflict;

    /**
     * The nogood store, if needed.
     */
    private PropNogoods ngstore;

    /**
     * A temporary int list to compute the nogood.
     */
    private TIntList ps;


    /**
     * Create a Conflict-based Backjumping strategy.
     * @param mModel the solver to instrument
     * @param nogoodFromConflict set to <tt>true</tt> to extract nogoods from explanations.
     * @param userFeedbackOn set to <tt>true</tt> to record causes in explanations (required for user feedback mainly).
     */
    public LearnCBJ(Model mModel, boolean nogoodFromConflict, boolean userFeedbackOn) {
        super(mModel, !nogoodFromConflict, userFeedbackOn);
        if(this.nogoodFromConflict = nogoodFromConflict) {
            this.ngstore = mModel.getNogoodStore().getPropNogoods();
            this.ps = new TIntArrayList();
        }
    }

    /**
     * Identify the decision to reconsider, and explain its refutation in the explanation data base
     *
     * @param nworld index of the world to backtrack to
     */
    protected void identifyRefutedDecision(int nworld) {
        DecisionPath path = mModel.getSolver().getDecisionPath();
        int last = path.size() -1 ;
        Decision dec = path.getLastDecision(); // the current decision to undo
        while (last > 0 && nworld > 1) {
            mExplainer.freeDecisionExplanation(dec); // not mandatory, for efficiency purpose only
            nworld--;
            dec = path.getDecision(--last);

        }
        if (last > 0) {
            if (dec.getArity()> 1 && !dec.hasNext()) {
                throw new UnsupportedOperationException("LearnCBJ.identifyRefutedDecision should get to a LEFT decision:" + dec);
            }
            lastExplanation.remove(dec);
            mExplainer.storeDecisionExplanation(dec, lastExplanation);
        }
    }

    @Override
    public void onFailure(Solver solver) {
       super.onFailure(solver);
        if (this.nogoodFromConflict) {
            postNogood();
        }
        int upto = compute(mModel.getSolver().getDecisionPath());
        assert upto > 0 && upto <= solver.getDecisionPath().size();
        solver.setJumpTo(upto);
        identifyRefutedDecision(upto);
    }

    /**
     * Extracts a nogod from this explanation (which needs to be complete) and add it to the no-good store.
     * If this explanation is not complete, it does nothing.
     */
    @SuppressWarnings("unchecked")
    private void postNogood() {
        if (lastExplanation.isComplete()) {
            Model mModel = ngstore.getModel();
            DecisionPath dp = mModel.getSolver().getDecisionPath();
            int last = dp.size()-1;
            Decision<IntVar> decision;
            ps.clear();
            while (last > 0) {
                decision = dp.getDecision(last--);
                if (lastExplanation.getDecisions().get(decision.getPosition())) {
                    assert decision.hasNext();
                    ps.add(SatSolver.negated(ngstore.Literal(decision.getDecisionVariable(),
                            (Integer) decision.getDecisionValue(), true)));
                }
            }
            ngstore.addLearnt(ps.toArray());
        }
    }


    /**
     * Compute the world to backtrack to
     *
     * @param decisionPath current decision path
     * @return the number of world to backtrack to.
     */
    private int compute(DecisionPath decisionPath) {
        assert decisionPath.size() >= lastExplanation.getDecisions().length();
        // TODO: should include levels too
        if(lastExplanation.getDecisions().cardinality()>0) {
            return decisionPath.size() - lastExplanation.getDecisions().previousSetBit(lastExplanation.getDecisions().length());
        }else{
            return decisionPath.size();
        }
    }

    @Override
    public void forget(Solver solver) {
        mExplainer.getRuleStore();
    }
}
