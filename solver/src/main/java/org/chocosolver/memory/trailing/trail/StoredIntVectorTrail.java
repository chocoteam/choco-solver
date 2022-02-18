/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail;

import org.chocosolver.memory.IStorage;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.memory.trailing.StoredIntVector;


/**
 * Implements a trail with the history of all the stored search vectors.
 */
public class StoredIntVectorTrail implements IStorage {

    /**
     * The current environment.
     */

    private final EnvironmentTrailing environment;

    /**
     * Load factor
     */
    private final double loadfactor;

    /**
     * All the stored search vectors.
     */

    private StoredIntVector[] vectorStack;


    /**
     * Indices of the previous values in the stored vectors.
     */

    private int[] indexStack;


    /**
     * Previous values of the stored vector elements.
     */

    private int[] valueStack;


    /**
     * World stamps associated to the previous values
     */

    private int[] stampStack;

    /**
     * The last world an search vector was modified in.
     */

    private int currentLevel;


    /**
     * Starts of levels in all the history arrays.
     */

    private int[] worldStartLevels;

    /**
     * Constructs a trail for the specified environment with the
     * specified numbers of updates and worlds.
     * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     * @param loadfactor load factor for structures
     */

    public StoredIntVectorTrail(EnvironmentTrailing env, int nUpdates, int nWorlds, double loadfactor) {
        this.environment = env;
        this.currentLevel = 0;
        this.vectorStack = new StoredIntVector[nUpdates];
        this.indexStack = new int[nUpdates];
        this.valueStack = new int[nUpdates];
        this.stampStack = new int[nUpdates];
        this.worldStartLevels = new int[nWorlds];
        this.loadfactor = loadfactor;
    }


    /**
     * Reacts on the modification of an element in a stored search vector.
     */

    public void savePreviousState(StoredIntVector vect, int index, int oldValue, int oldStamp) {
        this.vectorStack[currentLevel] = vect;
        this.indexStack[currentLevel] = index;
        this.stampStack[currentLevel] = oldStamp;
        this.valueStack[currentLevel] = oldValue;
        currentLevel++;
        if (currentLevel == vectorStack.length) {
            resizeUpdateCapacity();
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = (int) (vectorStack.length * loadfactor);
        // first, copy the stack of variables
        final StoredIntVector[] tmp1 = new StoredIntVector[newCapacity];
        System.arraycopy(vectorStack, 0, tmp1, 0, vectorStack.length);
        vectorStack = tmp1;
        // then, copy the stack of former values
        final int[] tmp2 = new int[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
        // then, copy the stack of world stamps
        final int[] tmp3 = new int[newCapacity];
        System.arraycopy(stampStack, 0, tmp3, 0, stampStack.length);
        stampStack = tmp3;
        // then, copy the stack of indices
        final int[] tmp4 = new int[newCapacity];
        System.arraycopy(indexStack, 0, tmp4, 0, indexStack.length);
        indexStack = tmp4;
    }

    public void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }

    /**
     * Moving up to the next world.
     *
     * @param worldIndex current world index
     */

    public void worldPush(int worldIndex) {
        this.worldStartLevels[worldIndex] = currentLevel;
        if (worldIndex == worldStartLevels.length - 1) {
            resizeWorldCapacity((int) (worldStartLevels.length * loadfactor));
        }
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */

    public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            StoredIntVector v = vectorStack[currentLevel];
            v._set(indexStack[currentLevel], valueStack[currentLevel], stampStack[currentLevel]);
        }
    }


    /**
     * Comits a world: merging it with the previous one.
     */

    public void worldCommit(int worldIndex) {
        // principle:
        //   currentLevel decreases to end of previous world
        //   updates of the committed world are scanned:
        //     if their stamp is the previous one (merged with the current one) -> remove the update (garbage collecting this position for the next update)
        //     otherwise update the worldStamp
        int startLevel = worldStartLevels[environment.getWorldIndex()];
        int prevWorld = environment.getWorldIndex() - 1;
        int writeIdx = startLevel;
        for (int level = startLevel; level < currentLevel; level++) {
            StoredIntVector var = vectorStack[level];
            int idx = indexStack[level];
            int val = valueStack[level];
            int stamp = stampStack[level];
            var.worldStamps[idx] = prevWorld;// update the stamp of the variable (current stamp refers to a world that no longer exists)
            if (stamp != prevWorld) {
                // shift the update if needed
                if (writeIdx != level) {
                    valueStack[writeIdx] = val;
                    indexStack[writeIdx] = idx;
                    vectorStack[writeIdx] = var;
                    stampStack[writeIdx] = stamp;
                }
                writeIdx++;
            }  //else:writeIdx is not incremented and the update will be discarded (since a good one is in prevWorld)
        }
        currentLevel = writeIdx;
    }
}
