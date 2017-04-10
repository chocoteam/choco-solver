/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;



/**
 * An abstract class for backtrackable long.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public abstract class IStateLong  {

    protected final IEnvironment environment;
    protected long currentValue;
    protected int timeStamp;


    public IStateLong(IEnvironment env, long i) {
        environment = env;
        currentValue = i;
        timeStamp = environment.getWorldIndex();
    }

    /**
     * Returns the current value.
     */
    public final long get() {
        return currentValue;
    }


    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */
    public abstract void set(long y);

    /**
     * modifying a StoredInt by an increment
     *
     * @param delta increment value
     * @return the new value
     */
    @Deprecated // never used
    public final long add(long delta) {
        long res = currentValue + delta;
        set(res);
        return res;
    }

    /**
     * Modifies the value without storing the former value on the trailing stack.
     *
     * @param y      the new value
     * @param wstamp the stamp of the world in which the update is performed
     */
    public void _set(final long y, final int wstamp) {
        currentValue = y;
        timeStamp = wstamp;
    }

    /**
     * Make a deep copy of this.
     *
     * @return a long
     */
    @Deprecated // never used
    public final long deepCopy() {
        return currentValue;
    }

    @Deprecated // never used
    public int getTimeStamp() {
        return timeStamp;
    }

    public void overrideTimeStamp(int aTimeStamp) {
        this.timeStamp = aTimeStamp;
    }

    /**
     * Retrieving the environment
     */
    @Deprecated // never used
    public IEnvironment getEnvironment() {
        return environment;
    }


    @Override
    public String toString() {
        return String.valueOf(currentValue);
    }
}
