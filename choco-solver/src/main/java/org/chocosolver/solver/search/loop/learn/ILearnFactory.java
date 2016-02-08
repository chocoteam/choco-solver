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

import org.chocosolver.solver.Resolver;

/**
 * Interface to define how to learn during the solving process (e.g. CBJ, DBT...)
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface ILearnFactory {

    Resolver _me();

	/**
     * @return an object learning nothing during search (default configuration)
     */
    default void doNotLearn(){
        _me().set(new LearnNothing());
    }

    /**
     * Creates a learning object based on Conflict-based Backjumping (CBJ) explanation strategy.
     * It backtracks up to the most recent decision involved in the explanation, and forget younger decisions.
     * @param nogoodsOn set to true to extract nogoods from failures
     * @param userFeedbackOn set to true to record the propagation in conflict
     *                       (only relevant when one wants to interpret the explanation of a failure).
     * @see org.chocosolver.solver.explanations.ExplanationFactory#CBJ
     */
    default void learnCBJ(boolean nogoodsOn, boolean userFeedbackOn) {
        _me().set(new LearnCBJ(_me().getModel(),nogoodsOn, userFeedbackOn));
    }

    /**
     * Creates a learning object based on Dynamic Backjumping (DBT) explanation strategy.
     * It backtracks up to most recent decision involved in the explanation, keep unrelated ones.
     * @param nogoodsOn set to true to extract nogoods from failures
     * @param userFeedbackOn set to true to record the propagation in conflict
     *                       (only relevant when one wants to interpret the explanation of a failure).
     * @see org.chocosolver.solver.explanations.ExplanationFactory#DBT
     */
    default void learnDBT(boolean nogoodsOn, boolean userFeedbackOn) {
        _me().set(new LearnDBT(_me().getModel(), nogoodsOn, userFeedbackOn));
    }
}
