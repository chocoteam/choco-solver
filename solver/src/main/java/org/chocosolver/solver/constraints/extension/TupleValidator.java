/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension;

/**
 * An interface to implement to filter valid tuples automatically generated through TuplesFactory
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/2014
 */
public interface TupleValidator {

	TupleValidator TRUE = values -> true;

    /**
     * Valid a tuple
     * @param values tuple to valid
     * @return a boolean
     */
    boolean valid(int... values);
}
