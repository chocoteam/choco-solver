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
 * The "Propagate" component
 * (Inspired from "Unifying search algorithms for CSP" N. Jussien and O. Lhomme, Technical report 02-3-INFO, EMN).
 * <p>
 * The aim of the component is to propagate information throughout the constraint network when a decision is made.
 * <p>
 * Created by cprudhom on 01/09/15.
 * Project: choco.
 */
public interface Propagate {

    /**
     * Propagate information throughout the constraint network, that is, apply decision and post dynamic cut (if any).
     *
     * @throws ContradictionException if a dead-end is encountered
     */
    void execute(Solver solver) throws ContradictionException;
}
