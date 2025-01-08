/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail.chunck;


import org.chocosolver.memory.trailing.StoredLong;

import java.util.Arrays;

/**
 * A world devoted to integers.
 *
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 29/05/2016
 */
public class LongWorld implements World {


    /**
     * Stack of backtrackable search variables.
     */
    private StoredLong[] variableStack;

    /**
     * Stack of values (former values that need be restored upon backtracking).
     */
    private long[] valueStack;


    /**
     * Stack of timestamps indicating the world where the former value
     * had been written.
     */
    private int[] stampStack;

    private int now;

    private final int defaultSize;

    private final double loadfactor;

    /**
     * Make a new world.
     *
     * @param defaultSize the default world size
     */
    public LongWorld(int defaultSize, double loadfactor) {
        this.now = 0;
        this.loadfactor = loadfactor;
        this.defaultSize = defaultSize;
    }

    /**
     * Reacts when a StoredLong is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(StoredLong v, long oldValue, int oldStamp) {
        if (stampStack == null) {
            valueStack = new long[defaultSize];
            stampStack = new int[defaultSize];
            variableStack = new StoredLong[defaultSize];
        }
        valueStack[now] = oldValue;
        variableStack[now] = v;
        stampStack[now] = oldStamp;
        now++;
        if (now == valueStack.length) {
            int newCapacity = (int) (variableStack.length * loadfactor);
            valueStack = Arrays.copyOf(valueStack, newCapacity);
            variableStack = Arrays.copyOf(variableStack, newCapacity);
            stampStack = Arrays.copyOf(stampStack, newCapacity);
        }
    }

    @Override
    public void revert() {
        for (int i = now - 1; i >= 0; i--) {
            variableStack[i]._set(valueStack[i], stampStack[i]);
        }
    }

    @Override
    public void clear() {
        now = 0;
    }

    @Override
    public int used() {
        return now;
    }

    @Override
    public int allocated() {
        return stampStack == null ? 0 : stampStack.length;
    }
}
