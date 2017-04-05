/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.IExplanationEngine;
import org.chocosolver.solver.explanations.NoExplanationEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;

/**
 * An abstract class to deal with explanation-based learning.
 * Created by cprudhom on 05/11/2015.
 * Project: choco.
 */
public class LearnExplained implements Learn {

    /**
     * The solver to explain.
     */
    protected final Model mModel;

    /**
     * The explanation engine, which computes and returns explanation of a state.
     */
    protected final IExplanationEngine mExplainer;

    /**
     * Indicates if the causes need to be stored.
     * This is required, for instance, when a user explanation is required.
     */
    private final boolean saveCauses;

    /**
     * Maintains the number of solutions found, required for {@link #record(Solver)}.
     */
    private long nbsol = 0;

    /**
     * The last explanation computed.
     */
    protected Explanation lastExplanation;


    /**
     * Equips the solver with an explanation engine, which is able to explain failures and solutions.
     * @param mModel the solver to equip
     * @param partialExplanationsOn set to <tt>true</tt> to enable partial explanations, <tt>false</tt> otherwise
     * @param recordCauses set to <tt>true</tt> to record causes in explanations, <tt>false</tt> otherwise
     * @throws SolverException if views are enabled. Views provide incomplete explanation.
     */
    public LearnExplained(Model mModel, boolean partialExplanationsOn, boolean recordCauses) {
        this.mModel = mModel;
        if (mModel.getSolver().getExplainer() == NoExplanationEngine.SINGLETON) {
            mModel.getSolver().setExplainer(new ExplanationEngine(mModel, partialExplanationsOn, recordCauses));
        }
        this.mExplainer = mModel.getSolver().getExplainer();
        this.saveCauses = recordCauses;
    }

    @Override
    public void record(Solver solver) {
        if (nbsol == solver.getSolutionCount()) {
            onFailure(solver);
        } else {
            nbsol++;
            onSolution(solver);
        }
    }

    @Override
    public void forget(Solver solver) {
        // nothing by default but forget some learnt nogoods should be done here
    }

    /**
     * Actions to do when a solution is found.
     * By default, it records the basic explanation related to the refutation of the last decision.
     */
    public void onSolution(Solver solver){
        // we need to prepare a "false" backtrack on this decision
        DecisionPath path = mModel.getSolver().getDecisionPath();
        int i = path.size() -1;
        Decision dec = path.getDecision(i);
        while (i > 0 && !dec.hasNext()) {
            dec = path.getDecision(--i);
        }
        if (i > 0) {
            Explanation explanation = mExplainer.makeExplanation(saveCauses);
            // 1. skip the current one which is refuted...
            Decision d = path.getDecision(--i);
            while (i > 0) {
                if (d.hasNext()) {
                    explanation.addDecision(d);
                }
                d = path.getDecision(--i);
            }
            mExplainer.storeDecisionExplanation(dec, explanation);
        }
        solver.setJumpTo(1);
    }

    /**
     * Actions to do when a failure is met.
     */
    public void onFailure(Solver solver){
        ContradictionException cex = mModel.getSolver().getContradictionException();
        assert (cex.v != null) || (cex.c != null) : this.getClass().getName() + ".onContradiction incoherent state";
        lastExplanation = mExplainer.explain(cex);
    }



    /**
     * Return the explanation of the last conflict
     *
     * @return an explanation
     */
    public final Explanation getLastExplanation() {
        return lastExplanation;
    }

}
