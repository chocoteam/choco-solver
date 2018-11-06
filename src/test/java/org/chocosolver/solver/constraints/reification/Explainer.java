/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.search.loop.learn.LearnSignedClauses;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.procedure.IntProcedure;

import java.util.HashMap;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 05/11/2018.
 */
class Explainer {

    static HashMap<IntVar, IntIterableRangeSet> execute(Solver solver, IntProcedure proc, Propagator prop, IntVar v) throws ContradictionException {
        solver.setLearningSignedClauses();
        solver.propagate();
        proc.execute(0);
        solver.propagate();
        LearnSignedClauses<ExplanationForSignedClause> learner
                = (LearnSignedClauses<ExplanationForSignedClause>) solver.getLearner();
        ExplanationForSignedClause expl = learner.getExplanation();
        ContradictionException cex = new ContradictionException();
        cex.set(prop, v, "");
        expl.learnSignedClause(cex);
        return expl.getLiterals();
    }

}
