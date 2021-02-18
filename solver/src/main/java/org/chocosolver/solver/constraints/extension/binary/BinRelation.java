/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.extension.Tuples;

public abstract class BinRelation {

    /**
     * return true if couple (x,y) is feasible according
     * to the definition of the relation. e.g if the relation is defined
     * with infeasible tuples, it returns true if (x,y) is one of them.
     *
     * @param x a value for the first element
     * @param y a value for the second element
     * @return boolean
     */
    public abstract boolean checkCouple(int x, int y);

    /**
     * Test whether the couple (x,y) is consistent
     *
     * @param x a value for the first element
     * @param y a value for the second element
     * @return true if (x,y) is a consistent couple
     */
    public abstract boolean isConsistent(int x, int y);

    /**
     * Convert this relation into 'Tuples'
     * @return
     */
    public abstract Tuples convert();
}
