/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail.flatten;

import org.chocosolver.memory.trailing.StoredLong;
import org.chocosolver.memory.trailing.trail.IStoredLongTrail;


public class StoredLongTrail implements IStoredLongTrail {

    /**
     * Load factor
     */
    private final double loadfactor;

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


    /**
     * Points the level of the last entry.
     */
    private int currentLevel;


    /**
     * A stack of pointers (for each start of a world).
     */
    private int[] worldStartLevels;


    /**
     * Constructs a trail with predefined size.
     *
     * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     * @param loadfactor load factor for structures
     */
    public StoredLongTrail(int nUpdates, int nWorlds, double loadfactor) {
        currentLevel = 0;
        variableStack = new StoredLong[nUpdates];
        valueStack = new long[nUpdates];
        stampStack = new int[nUpdates];
        worldStartLevels = new int[nWorlds];
        this.loadfactor = loadfactor;
    }


    /**
     * Moving up to the next world.
     *
     * @param worldIndex current world index
     */
    @Override
    public void worldPush(int worldIndex) {
        worldStartLevels[worldIndex] = currentLevel;
        if (worldIndex == worldStartLevels.length - 1) {
            resizeWorldCapacity((int) (worldStartLevels.length * loadfactor));
        }
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */
    @Override
    public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            final StoredLong v = variableStack[currentLevel];
            v._set(valueStack[currentLevel], stampStack[currentLevel]);
        }
    }

    /**
     * Comits a world: merging it with the previous one.
     */
    @Override
    public void worldCommit(int worldIndex) {
        // principle:
        //   currentLevel decreases to end of previous world
        //   updates of the committed world are scanned:
        //     if their stamp is the previous one (merged with the current one) -> remove the update (garbage collecting this position for the next update)
        //     otherwise update the worldStamp
        final int startLevel = worldStartLevels[worldIndex];
        final int prevWorld = worldIndex - 1;
        int writeIdx = startLevel;
        for (int level = startLevel; level < currentLevel; level++) {
            final StoredLong var = variableStack[level];
            final long val = valueStack[level];
            final int stamp = stampStack[level];
            var.overrideTimeStamp(prevWorld);// update the stamp of the variable (current stamp refers to a world that no longer exists)
            if (stamp != prevWorld) {
                // shift the update if needed
                if (writeIdx != level) {
                    valueStack[writeIdx] = val;
                    variableStack[writeIdx] = var;
                    stampStack[writeIdx] = stamp;
                }
                writeIdx++;
            }  //else:writeIdx is not incremented and the update will be discarded (since a good one is in prevWorld)
        }
        currentLevel = writeIdx;
    }


    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    @Override
    public void savePreviousState(StoredLong v, long oldValue, int oldStamp) {
        valueStack[currentLevel] = oldValue;
        variableStack[currentLevel] = v;
        stampStack[currentLevel] = oldStamp;
        currentLevel++;
        if (currentLevel == variableStack.length) {
            resizeUpdateCapacity();
        }
    }


    private void resizeUpdateCapacity() {
        final int newCapacity = (int) (variableStack.length * loadfactor);
        // first, copy the stack of variables
        final StoredLong[] tmp1 = new StoredLong[newCapacity];
        System.arraycopy(variableStack, 0, tmp1, 0, variableStack.length);
        variableStack = tmp1;
        // then, copy the stack of former values
        final long[] tmp2 = new long[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
        // then, copy the stack of world stamps
        final int[] tmp3 = new int[newCapacity];
        System.arraycopy(stampStack, 0, tmp3, 0, stampStack.length);
        stampStack = tmp3;
    }

    private void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }

}
