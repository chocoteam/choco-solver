/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package memory.trailing.trail.flatten;

import memory.trailing.StoredLong;
import memory.trailing.trail.IStoredLongTrail;


public class StoredLongTrail implements IStoredLongTrail {


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
     * capacity of the trailing stack (in terms of number of updates that can be stored)
     */
    private int maxUpdates = 0;


    /**
     * Constructs a trail with predefined size.
     *
     * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     */

    public StoredLongTrail(int nUpdates, int nWorlds) {
        currentLevel = 0;
        maxUpdates = nUpdates;
        variableStack = new StoredLong[maxUpdates];
        valueStack = new long[maxUpdates];
        stampStack = new int[maxUpdates];
        worldStartLevels = new int[nWorlds];
    }


    /**
     * Moving up to the next world.
     *
     * @param worldIndex
     */

    public void worldPush(int worldIndex) {
        worldStartLevels[worldIndex] = currentLevel;
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex
     */

    public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            final StoredLong v = variableStack[currentLevel];
            v._set(valueStack[currentLevel], stampStack[currentLevel]);
        }
    }


    /**
     * Returns the current size of the stack.
     */

    public int getSize() {
        return currentLevel;
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

    public void savePreviousState(StoredLong v, long oldValue, int oldStamp) {
        valueStack[currentLevel] = oldValue;
        variableStack[currentLevel] = v;
        stampStack[currentLevel] = oldStamp;
        currentLevel++;
        if (currentLevel == maxUpdates) {
            resizeUpdateCapacity();
        }
    }

    @Override
    public void buildFakeHistory(StoredLong v, long initValue, int olderStamp) {
        // from world 0 to fromStamp (excluded), create a fake history based on initValue
        // kind a copy of the current elements
        // 1. make a copy of variableStack
        StoredLong[] _variableStack = variableStack;
        long[] _valueStack = valueStack;
        int[] _stampStack = stampStack;
        int[] _worldStartLevels = worldStartLevels;
        int _maxUpdates = variableStack.length + olderStamp;

        variableStack = new StoredLong[_maxUpdates];
        valueStack = new long[_maxUpdates];
        stampStack = new int[_maxUpdates];
        worldStartLevels = new int[worldStartLevels.length];
        currentLevel = 0;


        // then replay the history
        for (int w = 1; w < olderStamp; w++) {
            int f = _worldStartLevels[w];
            int t = _worldStartLevels[w + 1];
            // copy the true history
            System.arraycopy(_variableStack, f, variableStack, currentLevel, t - f);
            System.arraycopy(_valueStack, f, valueStack, currentLevel, t - f);
            System.arraycopy(_stampStack, f, stampStack, currentLevel, t - f);
            currentLevel += (t - f);


            // add the fake one
            savePreviousState(v, initValue, w - 1);
            worldPush(w + 1);
        }
        savePreviousState(v, initValue, olderStamp - 1);
    }


    private void resizeUpdateCapacity() {
        final int newCapacity = ((maxUpdates * 3) / 2);
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
        // last update the capacity
        maxUpdates = newCapacity;
    }

    public void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }

}
