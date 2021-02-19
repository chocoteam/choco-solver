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

import java.util.Random;

/**
 * Static bandit selection strategy.
 * Each arm is given a probability to be chosen.
 * A pseudorandom numbers generator is queried
 * to get a double between between {@code 0.0} and {@code 1.0}.
 * This value designates the arm.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/2020
 */
public class Static implements Policy {
    /**
     * Number of arms
     */
    int numActions;
    /**
     * Pseudorandom numbers generator
     */
    Random random;
    /**
     * Probabilities to select each arm
     */
    double[] probabilities;

    /**
     * A static bandit selection policy.
     *
     * @param probabilities probability of each arm
     * @param random        a pseudorandom numbers generator
     */
    public Static(double[] probabilities, Random random) {
        this.numActions = probabilities.length;
        this.probabilities = probabilities;
        this.random = random;
    }

    /**
     * @implNote empty method
     */
    @Override
    public void init() {
    }

    /**
     * @implSpec Select the next action based on {@link #probabilities}
     */
    @Override
    public int nextAction(int step) {
        double r = random.nextDouble();
        int a = 0;
        while (a < numActions && r > probabilities[a]) {
            r -= probabilities[a];
            a++;
        }
        return a;
    }

    /**
     * @implNote empty method
     */
    @Override
    public void update(int action, double reward) {
    }
}
