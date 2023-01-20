/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.propagate;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;



/**
 * @deprecated
 */
@Deprecated
public interface Propagate {

    /**
     * Propagate information throughout the constraint network, that is, apply decision and post dynamic cut (if any).
     *
     * @throws ContradictionException if a dead-end is encountered
     */
    void execute(Solver solver) throws ContradictionException;
}
