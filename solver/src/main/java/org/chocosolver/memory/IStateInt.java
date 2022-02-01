/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;



/**
 * An abstract class for backtrackable int.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public abstract class IStateInt  {

    protected final IEnvironment environment;
    protected int currentValue;
    protected int timeStamp;


    public IStateInt(IEnvironment env, int i) {
        environment = env;
        currentValue = i;
        timeStamp = -1;
    }

    /**
     * Returns the current value.
     */
    public final int get() {
        return currentValue;
    }


    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */
    public abstract void set(int y);

    /**
     * modifying a StoredInt by an increment
     *
     * @param delta increment value
     * @return the new value
     */
    public final int add(int delta) {
        int res = currentValue + delta;
        set(res);
        return res;
    }

    /**
     * Modifies the value without storing the former value on the trailing stack.
     *
     * @param y      the new value
     * @param wstamp the stamp of the world in which the update is performed
     */
    public void _set(final int y, final int wstamp) {
        currentValue = y;
        timeStamp = wstamp;
    }

    public void overrideTimeStamp(int aTimeStamp) {
        this.timeStamp = aTimeStamp;
    }

    @Override
    public String toString() {
        return String.valueOf(currentValue);
    }
}
