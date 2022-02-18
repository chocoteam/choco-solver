/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

/**
 * A specific interface propagator to enable, under certain conditions,
 * to update some of their internal structure.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/12/2020
 */
public interface UpdatablePropagator<W>{

    /**
     * Update class with the new value {@code v}.
     * @param value a new value
     * @param thenForcePropagate force propagation after updating the propagator
     * @implNote setting {@code thenForcePropagate} to {@code true}
     */
    void update(W value, boolean thenForcePropagate);

    /**
     * @return the current value of the updatable field.
     */
    W getUpdatedValue();

}
