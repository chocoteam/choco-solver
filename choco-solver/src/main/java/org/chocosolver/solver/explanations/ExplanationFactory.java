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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Model;

/**
 * A non exhaustive list of ways to plug and exploit explanations.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 19/10/11
 * Time: 19:22
 */
public enum ExplanationFactory {


    NONE {
        @Override
        public void plugin(Model model, boolean nogoodsOn, boolean userFeedbackOn) {
            //DO NOTHING
        }
    },
    /**
     * add a Conflict-based Backjumping policy on contradiction to an explained solver.
     * It backtracks up to most recent decision involved in the explanation, and forget younger decisions.
     * @see org.chocosolver.solver.Resolver#setCBJLearning(boolean, boolean)
     */
    CBJ {
        @Override
        public void plugin(Model model, boolean nogoodsOn, boolean userFeedbackOn) {
            model.getResolver().setCBJLearning(nogoodsOn, userFeedbackOn);
        }
    },
    /**
     * add a Dynamic-Backtracking policy on contradiction to an explained solver.
     * It backtracks up to most recent decision involved in the explanation, keep unrelated ones.
     * @see org.chocosolver.solver.Resolver#setDBTLearning(boolean, boolean)
     */
    DBT {
        @Override
        public void plugin(Model model, boolean nogoodsOn, boolean userFeedbackOn) {
            model.getResolver().setDBTLearning(nogoodsOn, userFeedbackOn);
        }
    };

    /**
     * Plug explanations into coe<code>solver</code>.
     *
     * @param model         the solver to observe
     * @param nogoodsOn      extract nogoods from conflict
     * @param userFeedbackOn user feedback on: propagators in conflict are available for consultation
     */
    public abstract void plugin(Model model, boolean nogoodsOn, boolean userFeedbackOn);

}
