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
package org.chocosolver.solver.search.loop.learn;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

/**
 * An abstract class to deal with explanation-based learning.
 * Created by cprudhom on 05/11/2015.
 * Project: choco.
 */
public class LearnExplained implements Learn {

    /**
     * The solver to explain.
     */
    final Model mModel;

    /**
     * The explanation engine, which computes and returns explanation of a state.
     */
    final ExplanationEngine mExplainer;

    /**
     * Indicates if the causes need to be stored.
     * This is required, for instance, when a user explanation is required.
     */
    final boolean saveCauses;

    /**
     * Maintains the number of solutions found, required for {@link #record(Solver)}.
     */
    private long nbsol = 0;

    /**
     * The last explanation computed.
     */
    Explanation lastExplanation;


    /**
     * Equips the solver with an explanation engine, which is able to explain failures and solutions.
     * @param mModel the solver to equip
     * @param partialExplanationsOn set to <tt>true</tt> to enable partial explanations, <tt>false</tt> otherwise
     * @param recordCauses set to <tt>true</tt> to record causes in explanations, <tt>false</tt> otherwise
     * @throws SolverException if views are enabled. Views provide incomplete explanation.
     */
    public LearnExplained(Model mModel, boolean partialExplanationsOn, boolean recordCauses) {
        for(IntVar iv : mModel.retrieveIntVars(true)){
            if((iv.getTypeAndKind() & Variable.VIEW) !=0){
                throw new SolverException("Using explanations with views is not possible yet.\n" +
                        "Consider disabling views:\n" +
                        "mModel.set(new Settings() {\n" +
                        "            @Override\n" +
                        "            public boolean enableViews() {\n" +
                        "                return false;\n" +
                        "            }\n" +
                        "        });" +
                        "");
            }
        }
        this.mModel = mModel;
        if (mModel.getSolver().getExplainer() == null) {
            mModel.getSolver().set(new ExplanationEngine(mModel, partialExplanationsOn, recordCauses));
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
        ContradictionException cex = mModel.getSolver().getEngine().getContradictionException();
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
