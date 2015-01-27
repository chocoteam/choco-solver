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
package org.chocosolver.solver.explanations.strategies;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.strategy.decision.Decision;

/**
 * A toolbox dedicated to explained neighbors
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/07/13
 */
enum ExplanationToolbox {
    ;

    /**
     * Simutate a decision path, with backup
     *
     * @param aSolver  the concerned solver
     * @param decision the decision to apply
     * @throws ContradictionException
     */
    protected static void imposeDecisionPath(Solver aSolver, Decision decision) throws ContradictionException {
        IEnvironment environment = aSolver.getEnvironment();
        ObjectiveManager objectiveManager = aSolver.getObjectiveManager();
        // 1. simulates open node
        Decision current = aSolver.getSearchLoop().getLastDecision();
        decision.setPrevious(current);
        aSolver.getSearchLoop().setLastDecision(decision);
        // 2. simulates down branch
        environment.worldPush();
        decision.setWorldIndex(environment.getWorldIndex());
        decision.buildNext();
        objectiveManager.apply(decision);
        objectiveManager.postDynamicCut();
//        aSolver.getEngine().propagate();
    }

    protected static Decision mimic(Decision dec) {
        Decision clone = dec.duplicate();
        boolean forceNext = !dec.hasNext();
        if (forceNext) {
            clone.buildNext();
        }
        return clone;
    }
}
