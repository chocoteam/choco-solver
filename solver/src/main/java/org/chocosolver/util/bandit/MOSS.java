/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.bandit;

/**
 * Minimax Optimal Strategy in the Stochastic case.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/2020
 */
public class MOSS extends UCB1 {

    public MOSS(int numActions) {
        super(numActions);
    }

    /**
     * @param t step
     * @param n number of times an action was played so far
     * @return &radic;(4 / n * log(max(1, t / 2n))
     * @implNote The main reason this class exists
     */
    protected double upperBound(int t, int n) {
        return Math.sqrt(4. / n * Math.log(Math.max(1, t / 2 * n)));
    }
}
