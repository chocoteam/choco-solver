/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;



public abstract class LargeRelation  {

    /**
     * return true if tuple is feasible according
     * to the definition of the relation. e.g if the relation is defined
     * with infeasible tuples, it returns true if tuple is one of them.
     */
    public abstract boolean checkTuple(int[] tuple);

    /**
     * Test whether a tuple is consistent
     *
     * @return true if tuple is consistent.
     */
    public abstract boolean isConsistent(int[] tuple);

    protected boolean valid(int[] tuple, IntVar[] vars) {
        for (int i = 0; i < tuple.length; i++) {
            if (!vars[i].contains(tuple[i]))
                return false;
        }
        return true;
    }

    /**
     * Convert this relation into 'Tuples'
     * @return
     */
    public abstract Tuples convert();
}
