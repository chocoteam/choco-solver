/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing;

import org.chocosolver.memory.IStateDouble;
import org.chocosolver.memory.trailing.trail.IStoredDoubleTrail;


/**
 * A class implementing backtrackable double.
 */
public class StoredDouble extends IStateDouble {

    protected final IStoredDoubleTrail myTrail;

    /**
     * Constructs a stored search with an initial value.
     * Note: this constructor should not be used directly: one should instead
     * use the IEnvironment factory
     */
    public StoredDouble(final EnvironmentTrailing env, final double i) {
        super(env, i);
        myTrail = env.getDoubleTrail();
    }

    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */
    @Override
    public final void set(final double y) {
        if (y != currentValue) {
            final int wi = environment.getWorldIndex();
            if (this.timeStamp < wi) {
                myTrail.savePreviousState(this, currentValue, timeStamp);
                timeStamp = wi;
            }
            currentValue = y;
        }
    }
}

