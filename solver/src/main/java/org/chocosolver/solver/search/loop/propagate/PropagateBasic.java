/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.propagate;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;

/**
 * A basic {@link Propagate} implementation
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 02/09/15
 */
public class PropagateBasic implements Propagate {

    /**
     * Basic propagation:
     * <ul>
     *     <li>First, prepare the decision (to ensure good behavior of the
     *     {@link org.chocosolver.solver.search.loop.move.Move#repair(Solver)} call)</li>
     *     <li>then, a first propagation ensures that, if learning is on,
     *     the unit nogood learnt on failure filters,
     *     <li>the cut is posted before applying the decision to ensure good nogood,
     *     and a second propagation ensures the cut is taken into account</li>
     *     <li>the decision is applied (if learning is on and the decision is refuted,
     *     it is bypassed by the learnt unit nogood),</li>
     *     <li>finally, a fix point is reached.</li>
     * </ul>
     *
     * @param solver    the underlying solver
     * @throws ContradictionException if a contradiction occurs
     */
    @Override
    public void execute(Solver solver) throws ContradictionException {
        //WARNING: keep the order as is (read javadoc for more details)
        solver.getDecisionPath().buildNext();
        solver.getObjectiveManager().postDynamicCut();
        solver.getEngine().propagate();
        solver.getDecisionPath().apply();
        solver.getEngine().propagate();
    }
}
