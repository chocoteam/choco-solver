/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.search.loop.learn.LearnSignedClauses;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.procedure.IntProcedure;

import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 05/11/2018.
 */
public class Explainer {

    public static HashMap<IntVar, IntIterableRangeSet> execute(Solver solver, IntProcedure proc, ICause cause, IntVar v) throws ContradictionException {
        solver.setLearningSignedClauses();
        solver.propagate();
        proc.execute(0);
        solver.propagate();
        LearnSignedClauses<ExplanationForSignedClause> learner
                = (LearnSignedClauses<ExplanationForSignedClause>) solver.getLearner();
        ExplanationForSignedClause expl = learner.getExplanation();
        ContradictionException cex = new ContradictionException();
        cex.set(cause, v, "");
        expl.learnSignedClause(cex);
        return expl.getLiterals()
                .stream()
                .collect(Collectors.toMap(
                        var -> var,
                        var -> var.getLit().export(),
                        (a, b) -> a,
                        HashMap::new
                )
        );
    }

    public static HashMap<IntVar, IntIterableRangeSet> fail(Solver solver, IntProcedure proc) throws ContradictionException {
        solver.setLearningSignedClauses();
        solver.propagate();
        try {
            proc.execute(0);
            solver.propagate();
        } catch (ContradictionException cex0) {
        }
        LearnSignedClauses<ExplanationForSignedClause> learner
                = (LearnSignedClauses<ExplanationForSignedClause>) solver.getLearner();
        ExplanationForSignedClause expl = learner.getExplanation();
        expl.learnSignedClause(solver.getContradictionException());
        return expl.getLiterals()
                .stream()
                .collect(Collectors.toMap(
                        var -> var,
                        var -> var.getLit().export(),
                        (a, b) -> a,
                        HashMap::new
                        )
                );
    }

}
