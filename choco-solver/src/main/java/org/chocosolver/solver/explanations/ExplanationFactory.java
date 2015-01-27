/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.strategies.ConflictBackJumping;
import org.chocosolver.solver.explanations.strategies.DynamicBackTracking;

/**
 * A non exhaustive list of ways to plug and exploit explanations.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 19/10/11
 * Time: 19:22
 */
public enum ExplanationFactory {


    NONE{
        @Override
        public void plugin(Solver solver, boolean nogoodsOn, boolean userFeedbackOn) {
            //DO NOTHING
        }
    },
    /**
     * add a Conflict-based jumping policy on contradiction to an explained solver.
     * It backtracks up to most recent decision involved in the explanation, and forget younger decisions.
     */
    CBJ {
        @Override
        public void plugin(Solver solver, boolean nogoodsOn, boolean userFeedbackOn) {
            ExplanationEngine ee = new ExplanationEngine(solver, userFeedbackOn);
            ConflictBackJumping cbj = new ConflictBackJumping(ee, solver, nogoodsOn);
            solver.plugMonitor(cbj);
        }
    },
    /**
     * add a Dynamic-Backtracking policy on contradiction to an explained solver.
     * It backtracks up to most recent decision involved in the explanation.
     */
    DBT {
        @Override
        public void plugin(Solver solver, boolean nogoodsOn, boolean userFeedbackOn) {
            ExplanationEngine ee = new ExplanationEngine(solver, userFeedbackOn);
            DynamicBackTracking dbt = new DynamicBackTracking(ee, solver, nogoodsOn);
            solver.plugMonitor(dbt);
        }
    };

    /**
     * Plug explanations into coe<code>solver</code>.
     *
     * @param solver         the solver to observe
     * @param nogoodsOn      extract nogoods from conflict
     * @param userFeedbackOn user feedback on: propagators in conflict are available for consultation
     */
    public abstract void plugin(Solver solver, boolean nogoodsOn, boolean userFeedbackOn);

}
