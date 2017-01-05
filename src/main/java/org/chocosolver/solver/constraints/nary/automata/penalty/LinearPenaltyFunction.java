/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.penalty;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: May 3, 2010
 * Time: 5:49:24 PM
 */
public class LinearPenaltyFunction extends AbstractPenaltyFunction {

    /**
     * minimum bound
     */
    private int min;

    /**
     * soft minimum bound (= min if not soft).
     */
    private int minPref;

    /**
     * unit violation cost of the soft minimum bound (= 0 if not soft).
     */
    private int minPenalty;

    /**
     * maximum value
     */
    private int max;

    /**
     * soft maximum value (= max if not soft).
     */
    private int maxPref;

    /**
     * unit violation cost of the soft maximum value (= 0 if not soft).
     */
    private int maxPenalty;


    public LinearPenaltyFunction(int min, int minPref, int minPenalty, int max, int maxPref, int maxPenalty) {
        this.min = min;
        this.max = max;
        this.minPref = minPref;
        this.maxPref = maxPref;
        this.minPenalty = minPenalty;
        this.maxPenalty = maxPenalty;

    }


    @Override
    public int penalty(int value) {
        if (value < minPref) {
            if (value >= min) {
                return (minPref - value) * minPenalty;
            } else {
                return Integer.MAX_VALUE;
            }
        } else if (value > maxPref) {
            if (value <= max) {
                return (value - maxPref) * maxPenalty;
            } else {
                return Integer.MAX_VALUE;
            }

        } else {
            return 0;
        }
    }
}
