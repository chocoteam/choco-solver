/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;

import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.memory.structure.OneWordS32BitSet;
import org.chocosolver.memory.structure.OneWordS64BitSet;
import org.chocosolver.memory.structure.S64BitSet;

/**
 * Super class of all environments !
 */
public abstract class AbstractEnvironment implements IEnvironment {

    protected int currentWorld = 0;

    private static final int SIZE = 128;

    private ICondition condition = ICondition.FALSE;

    protected int timestamp;

    /**
     * Shared BitSet
     */
    private BasicIndexedBipartiteSet booleanSet;

    protected AbstractEnvironment() {
        this.timestamp = 0;
    }

    @Override
    public final int getWorldIndex() {
        return currentWorld;
    }

    @Override
    public final int getTimeStamp() {
        return timestamp;
    }

    /**
     * Factory pattern: new IStateBitSet objects are created by the environment
     *
     * @param size initial size of the IStateBitSet
     * @return IStateBitSet
     */
    @Override
    public IStateBitSet makeBitSet(int size) {
        if (size < 32) {
            return new OneWordS32BitSet(this, size);
        } else if (size < 64) {
            return new OneWordS64BitSet(this, size);
        } else {
            return new S64BitSet(this, size);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPopUntil(int w) {
        while (currentWorld > w) {
            worldPop();
        }
    }

    private void createSharedBipartiteSet(int size) {
        booleanSet = new BasicIndexedBipartiteSet(this, size);
    }

    /**
     * Factory pattern : shared StoredBitSetVector objects is return by the environment
     *
     * @return a shared bipartite set
     */
    @Override
    public final BasicIndexedBipartiteSet getSharedBipartiteSetForBooleanVars() {
        if (booleanSet == null) {
            createSharedBipartiteSet(SIZE);
        }
        return booleanSet;
    }

    @Override
    public boolean fakeHistoryNeeded() {
        return condition.satisfied();
    }

    @Override
    public void buildFakeHistoryOn(ICondition condition) {
        this.condition = condition;
        this.condition.set(this);
    }
}
