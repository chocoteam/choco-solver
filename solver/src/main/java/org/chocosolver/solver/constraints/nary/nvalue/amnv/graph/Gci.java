/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.graph;

import org.chocosolver.solver.constraints.nary.nvalue.amnv.differences.AutoDiffDetection;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.differences.D;
import org.chocosolver.solver.variables.IntVar;

/**
 * Constrained intersection graph
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class Gci extends Gi {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private D D;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates the constrained intersection graph of X and D
     *
     * @param X integer variables
     * @param D set of difference constraints
     */
    public Gci(IntVar[] X, D D) {
        super(X);
        this.D = D;
    }

    /**
     * Creates the constrained intersection graph of X and D
     * by automatically detecting disequalities and allDifferent constraints.
     *
     * @param X integer variables
     */
    public Gci(IntVar[] X) {
        this(X, new AutoDiffDetection(X));
    }

    //***********************************************************************************
    // ALGORITHMS
    //***********************************************************************************

    protected boolean intersect(int i, int j) {
        return !D.mustBeDifferent(i, j) && super.intersect(i, j);
    }

}
