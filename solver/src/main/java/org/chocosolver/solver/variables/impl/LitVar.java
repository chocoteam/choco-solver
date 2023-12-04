/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.ICause;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/11/2023
 */
public interface LitVar {

    /**
     * Channeling between an instantiation of a SAT variable and the modification of the related variable in CP.
     *
     * @param val      the corresponding value
     * @param val_type the corresponding type (EQ-NE or GE-LE)
     * @param sign     the sign of the literal (0 or 1)
     */
    default void channel(int val, int val_type, int sign) {
        throw new UnsupportedOperationException();
    }

    /**
     * Notify the modification of a variable to the SAT solver.
     * @param reason the reason of the modification
     * @param cause the cause of the modification
     * @param sat the SAT solver
     * @param lit the literal associated with the modification
     */
    default void notify(Reason reason, ICause cause, MiniSat sat, int lit) {
        sat.cEnqueue(lit, cause.manageReification().apply(reason));
    }

}
