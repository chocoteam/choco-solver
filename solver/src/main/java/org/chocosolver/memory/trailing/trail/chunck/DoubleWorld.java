/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail.chunck;


import org.chocosolver.memory.trailing.StoredDouble;

/**
 * A world devoted to integers.
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 29/05/2016
 */
public class DoubleWorld implements World{


    /**
     * Stack of backtrackable search variables.
     */
    private StoredDouble[] variableStack;

    /**
     * Stack of values (former values that need be restored upon backtracking).
     */
    private double[] valueStack;


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
    public DoubleWorld(int defaultSize, double loadfactor) {
        this.now = 0;
        this.loadfactor = loadfactor;
        this.defaultSize = defaultSize;
    }

    /**
     * Reacts when a StoredDouble is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(StoredDouble v, double oldValue, int oldStamp) {
        if (stampStack == null) {
            valueStack = new double[defaultSize];
            stampStack = new int[defaultSize];
            variableStack = new StoredDouble[defaultSize];
        }
        valueStack[now] = oldValue;
        variableStack[now] = v;
        stampStack[now] = oldStamp;
        now++;
        if (now == valueStack.length) {
            resizeUpdateCapacity();
        }
    }

    @Override
    public void revert() {
        for (int i = now - 1; i >= 0; i--) {
            variableStack[i]._set(valueStack[i], stampStack[i]);
        }
    }

    private void resizeUpdateCapacity() {
        int newCapacity = (int)(variableStack.length * loadfactor);
        final StoredDouble[] tmp1 = new StoredDouble[newCapacity];
        System.arraycopy(variableStack, 0, tmp1, 0, variableStack.length);
        variableStack = tmp1;
        final double[] tmp2 = new double[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
        final int[] tmp3 = new int[newCapacity];
        System.arraycopy(stampStack, 0, tmp3, 0, stampStack.length);
        stampStack = tmp3;
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
