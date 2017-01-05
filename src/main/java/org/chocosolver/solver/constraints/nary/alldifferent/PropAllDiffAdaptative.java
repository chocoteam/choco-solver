/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * <p>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation
 * but has a good average behavior in practice
 * <p>
 * Runs incrementally for maintaining a matching
 * <p>
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDiffAdaptative extends PropAllDiffAC {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private Random rd;
    private int calls, success;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param variables array of integer variables
     */
    public PropAllDiffAdaptative(IntVar[] variables) {
        super(variables);
        rd = new Random(0);
        calls = success = 1;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        double p = (success * 1.d) / (calls * 1.d);
        if (rd.nextFloat() < p) {
            boolean rem = true;
            try {
                rem = filter.propagate();
            } finally {
                calls++;
                if (rem) {
                    success++;
                }
            }
        }
    }

}
