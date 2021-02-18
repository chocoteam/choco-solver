/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.learn.AbstractEventObserver;
import org.chocosolver.solver.learn.EventRecorder;
import org.chocosolver.solver.learn.ExplanationForSignedClause;

/**
 * Interface to define how to learn during the solving process (e.g. CBJ, DBT...)
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface ILearnFactory extends ISelf<Solver> {

	/**
     * Indicate that no learning should be achieved during search (default configuration)
     */
    default void setNoLearning(){
        ref().setEventObserver(AbstractEventObserver.SILENT_OBSERVER);
        ref().setLearner(new LearnNothing());
    }

    /**
     * Creates a learning object based on Conflict-Driven Clause-Learning (CD-CL) strategy.
     * It backtracks up to the most recent decision involved in the explanation, and forget younger decisions.
     * It also posts signed clauses learnt on failures.
     * Some settings related to explanation can be define thanks to {@link org.chocosolver.solver.Settings}:
     * <ul>
     *     <il>{@link Settings#setNbMaxLearntClauses(int)}</il>
     *     <il>{@link Settings#setRatioForClauseStoreReduction(float)}</il>
     *     <il>{@link Settings#setMaxLearntClauseCardinality(int)}</il>
     *     <il>{@link Settings#setLearntClausesDominancePerimeter(int)}</il>
     * </ul>
     */
    default void setLearningSignedClauses() {
        AbstractEventObserver evtObs = ref().getEventObserver();
        if (evtObs == AbstractEventObserver.SILENT_OBSERVER) {
            evtObs = new EventRecorder(ref());
        }
        LearnSignedClauses<ExplanationForSignedClause> learner = new LearnSignedClauses<>(ref());
        assert evtObs.getGI().isPresent();
        learner.setExplanation(new ExplanationForSignedClause(evtObs.getGI().get()));
        ref().setLearner(learner);
    }
}
