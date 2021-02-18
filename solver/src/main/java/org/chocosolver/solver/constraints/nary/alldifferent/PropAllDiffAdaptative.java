/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * Probabilistic Propagator for AllDifferent AC constraint for integer variables introduced
 * to avoid loosing too much time in AC propagation when BC is sufficientss
 * The more this propagator triggers filtering and failure, the more likely it will be called
 * If it does not bring improvement, this propagator will be called less during search
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
        super(variables, true);
        rd = new Random(vars[0].getModel().getSeed());
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
