/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.ICause;

/**
 * Interface for delta enumerated dedicated to integer variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/01/13
 */
public interface IEnumDelta extends IntDelta {
    /**
     * Adds a new value to the delta
     *
     * @param value value to add
     * @param cause of the removal
     */
    void add(int value, ICause cause);

    /**
     * Return the idx^th value stored in the delta, if any
     *
     * @param idx rank of the value
     * @return idx^th value
     * @throws IndexOutOfBoundsException if idx is out of the bounds
     */
    int get(int idx) throws IndexOutOfBoundsException;
}
