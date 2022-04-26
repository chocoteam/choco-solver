/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.differences;

import org.chocosolver.solver.constraints.binary.PropNotEqualX_Y;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.constraints.set.PropAllDiff;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;

/**
 * automatic detection of binary disequalities and allDifferent constraints
 *
 * @author Jean-Guillaume Fages
 * @since 03/04/14
 * Created by IntelliJ IDEA.
 */
public class AutoDiffDetection implements D {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /**
     * whether or not disequality constraints may be added during search
     **/
    public static boolean dynamicAdditions = false;

    private final Variable[] scope;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AutoDiffDetection(Variable[] scope) {
        this.scope = scope;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean mustBeDifferent(int i1, int i2) {
        // automatic detection of binary disequalities and allDifferent constraints
        if (dynamicAdditions || scope[i1].getEnvironment().getWorldIndex() <= 1) {
            int nbp = scope[i1].getNbProps();
            if (scope[i2].getNbProps() < nbp) {
                int t = i1;
                i1 = i2;
                i2 = t;
            }
            int finalI = i2;
            return scope[i1].streamPropagators()
                    .filter(p -> (p instanceof PropNotEqualX_Y || p instanceof PropAllDiffInst || p instanceof PropAllDiff) && p.isActive())
                    .anyMatch(p -> Arrays.stream(p.getVars()).anyMatch(v -> v == scope[finalI]));
        }
        return false;
    }

}
