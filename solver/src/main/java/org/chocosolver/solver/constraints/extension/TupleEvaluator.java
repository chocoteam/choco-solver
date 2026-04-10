/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension;

/**
 * An interface to implement to evaluate tuples automatically generated through TuplesFactory
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/03/2026
 */
public interface TupleEvaluator {

    /**
     * Compute the value of a tuple
     * @param values tuple to evaluate
     * @return an int
     */
    int compute(int... values);
}
