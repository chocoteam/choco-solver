/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail.chunck;

/**
 * Specify a stack in a world.
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 29/05/2016
 */
public interface World {

    /**
     * The amount of values in the stack
     *
     * @return a positive integer
     */
    int used();

    /**
     * Pop all the stack.
     */
    void revert();

    /**
     * Clean the stack
     */
    void clear();

    /**
     * The allocated trail for this world.
     *
     * @return a positive number
     */
    int allocated();
}