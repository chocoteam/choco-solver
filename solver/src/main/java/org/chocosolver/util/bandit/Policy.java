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
 * Interface for bandit strategy selection.
 * <br/>
 * @see UCB1
 * @see MOSS
 * @see Static
 *
 * @author Charles Prud'homme
 * @since 29/06/2020
 */
public interface Policy {

    /**
     * Initialize the policy
     */
    void init();

    /**
     * Select the next action, at step {@code step}
     * @param step current step
     * @return the next action to play
     */
    int nextAction(int step);

    /**
     * After {@code action} has been played and
     * update the policy based on the {@code reward}
     * @param action action played
     * @param reward reward obtained playing {@code reward}
     */
    void update(int action, double reward);

}
