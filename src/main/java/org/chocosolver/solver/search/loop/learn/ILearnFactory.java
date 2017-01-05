/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Solver;

/**
 * Interface to define how to learn during the solving process (e.g. CBJ, DBT...)
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface ILearnFactory extends ISelf<Solver> {

	/**
     * Indicate that no learning should be achieved during search (default configuration)
     */
    default void setNoLearning(){
        _me().setLearner(new LearnNothing());
    }

    /**
     * Creates a learning object based on Conflict-based Backjumping (CBJ) explanation strategy.
     * It backtracks up to the most recent decision involved in the explanation, and forget younger decisions.
     * @param nogoodsOn set to true to extract nogoods from failures
     * @param userFeedbackOn set to true to record the propagation in conflict
     *                       (only relevant when one wants to interpret the explanation of a failure).
     */
    default void setCBJLearning(boolean nogoodsOn, boolean userFeedbackOn) {
        _me().setLearner(new LearnCBJ(_me().getModel(),nogoodsOn, userFeedbackOn));
    }

    /**
     * Creates a learning object based on Dynamic Backjumping (DBT) explanation strategy.
     * It backtracks up to most recent decision involved in the explanation, keep unrelated ones.
     * @param nogoodsOn set to true to extract nogoods from failures
     * @param userFeedbackOn set to true to record the propagation in conflict
     *                       (only relevant when one wants to interpret the explanation of a failure).
     */
    default void setDBTLearning(boolean nogoodsOn, boolean userFeedbackOn) {
        _me().setLearner(new LearnDBT(_me().getModel(), nogoodsOn, userFeedbackOn));
    }
}
