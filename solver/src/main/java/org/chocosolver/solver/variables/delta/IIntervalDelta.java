/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.ICause;

/**
 * Interface for delta bounded dedicated to integer variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/01/13
 */
public interface IIntervalDelta extends IntDelta {
    /**
     * Adds a new value interval to the delta
     *
     * @param lb    lower bound of removed interval
     * @param ub    upper bound of removed interval
     * @param cause of the removal
     */
    void add(int lb, int ub, ICause cause);

    /**
     * Return the lower bound of idx^th interval stored in the delta, if any
     *
     * @param idx rank of the interval
     * @return idx^th interval
     * @throws IndexOutOfBoundsException if idx is out of the bounds
     */
    int getLB(int idx) throws IndexOutOfBoundsException;

    /**
     * Return the upper bound idx^th interval stored in the delta, if any
     *
     * @param idx rank of the interval
     * @return idx^th interval
     * @throws IndexOutOfBoundsException if idx is out of the bounds
     */
    int getUB(int idx) throws IndexOutOfBoundsException;
}
