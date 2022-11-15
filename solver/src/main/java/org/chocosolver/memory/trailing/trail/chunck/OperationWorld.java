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


import org.chocosolver.memory.structure.IOperation;

import java.util.Arrays;

/**
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 31/05/2016
 */
public class OperationWorld implements World {


    /**
     * Stack of backtrackable search variables.
     */
    private IOperation[] variableStack;

    private int now;

    private final double loadfactor;

    public OperationWorld(int defaultSize, double loadfactor) {
        now = 0;
        this.loadfactor = loadfactor;
        variableStack = new IOperation[defaultSize];
    }

    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(IOperation v) {
        variableStack[now] = v;
        now++;
        if (now == variableStack.length) {
            int newCapacity = (int) (variableStack.length * loadfactor);
            variableStack = Arrays.copyOf(variableStack, newCapacity);
        }
    }

    @Override
    public void revert() {
        IOperation v;
        for (int i = now - 1; i >= 0; i--) {
            v = variableStack[i];
            v.undo();
        }
    }

    public void clear() {
        now = 0;
    }

    @Override
    public int allocated() {
        return variableStack == null ? 0 : variableStack.length;
    }

    @Override
    public int used() {
        return now;
    }
}
