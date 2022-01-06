/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import org.chocosolver.solver.Solver;



/**
 * The "Learn" component
 * (Inspired from "Unifying search algorithms for CSP" N. Jussien and O. Lhomme, Technical report 02-3-INFO, EMN).
 *
 * The aim of the component is to make sure that the search mechanism will avoid (as much as possible) to get back to states that have been explored and proved to be solution-less.
 *
 * Created by cprudhom on 01/09/15.
 * Project: choco.
 */
public interface Learn {

    /**
     * Validate and record a new piece of knowledge, that is, the current position is a dead-end.
     * @return <i>true</i> if something is learned (one or more clauses)
     */
    boolean record(Solver solver);

    /**
     * Forget some pieces of knowledge.
     */
    void forget(Solver solver);

}
