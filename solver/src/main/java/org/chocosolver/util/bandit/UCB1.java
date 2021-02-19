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

import java.util.Arrays;

/**
 * Upper Confidence Bound bandit selection policy.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/2020
 */
public class UCB1 implements Policy {

    int numActions;
    double[] payoffSums;
    int[] numPlays;

    /**
     * An Upper Bound Confidence (UCB1) selection strategy.
     * @param numActions number of arms
     */
    public UCB1(int numActions) {
        this.numActions = numActions;
        payoffSums = new double[numActions];
        numPlays = new int[numActions];
    }


    /**
     * Initialize internal vectors.
     * @implSpec
     * Fill {@link #numPlays} with ones
     */
    @Override
    public void init() {
        Arrays.fill(numPlays, 1);
    }

    /**
     * @implSpec
     * Let x<sub>i</sub> empirical mean payoffs of each action <i>i</i>,
     * and n<sub>i</sub> the number of times action <i>i</i> was played so far,
     * select the arm the maximizes:
     * <p>
     *  x<sub>i</sub> + f(t, n<sub>i</sub>)
     * </p>
     * where <i>t</i> is the step and <i>f(t, n<sub>i</sub>)</i>
     * is defined by {@link #upperBound(int, int)}
     * @param step current step
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public int nextAction(int step) {
        if (step < numActions) {
            return step;
        }
        int a = 0;
        double ucb = Integer.MIN_VALUE;
        for (int i = 0; i < numActions; i++) {
            double ucbi = payoffSums[i] / numPlays[i] + upperBound(step, numPlays[i]);
            if (ucb < ucbi) {
                ucb = ucbi;
                a = i;
            }
        }
        return a;
    }

    /**
     * @param t step
     * @param n number of times an action was played so far
     * @return &radic;(2 log t / n)
     */
    protected double upperBound(int t, int n) {
        return Math.sqrt(2 * Math.log(t + 1) / n);
    }

    @Override
    public void update(int action, double reward) {
        numPlays[action] += 1;
        payoffSums[action] += reward;
    }

}
