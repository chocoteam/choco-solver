/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing;

import org.chocosolver.memory.IStateLong;
import org.chocosolver.memory.trailing.trail.IStoredLongTrail;


/**
 * A class implementing backtrackable long.
 */
public class StoredLong extends IStateLong {

    protected final IStoredLongTrail myTrail;

    /**
     * Constructs a stored search with an initial value.
     * Note: this constructor should not be used directly: one should instead
     * use the IEnvironment factory
     */
    public StoredLong(final EnvironmentTrailing env, final long i) {
        super(env, i);
        myTrail = env.getLongTrail();
        if(env.fakeHistoryNeeded()){
            myTrail.buildFakeHistory(this, i, timeStamp);
        }
    }

    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */
    @Override
    public final void set(final long y) {
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

