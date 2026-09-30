/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality.algo;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * Common shape of the two consistency algorithms posted for the Global Cardinality Constraint
 * ({@link AlgoGccBC}, {@link AlgoGccAC}), so that {@code PropGcc} can drive either one without
 * knowing which consistency level it enforces.
 *
 * @author Charles Prud'homme
 */
public interface GccFilter {

    /**
     * (Re)initializes the algorithm's internal structures for the current array of decision
     * variables. Called once per propagator construction.
     */
    void reset(IntVar[] variables);

    /**
     * Filters the decision variables to (bound- or arc-) consistency, given the current bounds
     * on the occurrence count of every value in {@code [firstValue, firstValue + minOcc.length)}.
     *
     * @return {@code true} if at least one variable was filtered
     */
    boolean filter(int[] minOcc, int[] maxOcc, int firstValue) throws ContradictionException;
}
