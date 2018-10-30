/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
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
        ref().setLearner(new LearnNothing());
    }

    /**
     * @deprecated does nothing, will be removed in next version
     */
    @Deprecated
    default void setCBJLearning(boolean nogoodsOn, boolean userFeedbackOn) {
    }

    /**
     * @deprecated does nothing, will be removed in next version
     */
    @Deprecated
    default void setDBTLearning(boolean nogoodsOn, boolean userFeedbackOn) {

    }
}
