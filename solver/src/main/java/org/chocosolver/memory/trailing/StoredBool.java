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

import org.chocosolver.memory.IStateBool;
import org.chocosolver.memory.trailing.trail.IStoredBoolTrail;


/**
 * A class implementing backtrackable boolean.
 */
public class StoredBool extends IStateBool {

    protected final IStoredBoolTrail myTrail;

    /**
     * Constructs a stored search with an initial value.
     * Note: this constructor should not be used directly: one should instead
     * use the IEnvironment factory
     */
    public StoredBool(final EnvironmentTrailing env, final boolean i) {
        super(env, i);
        myTrail = env.getBoolTrail();
    }

    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */
    @Override
    public final void set(final boolean y) {
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

