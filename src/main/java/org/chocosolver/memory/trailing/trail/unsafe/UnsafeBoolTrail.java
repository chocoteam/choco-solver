/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.memory.trailing.trail.unsafe;

import org.chocosolver.memory.trailing.StoredBool;
import org.chocosolver.memory.trailing.trail.IStoredBoolTrail;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/13
 */
public class UnsafeBoolTrail implements IStoredBoolTrail {

    private final Unsafe unsafe;

    public static final int SIZEOF_DATA = Unsafe.ARRAY_BYTE_INDEX_SCALE;

    public static final int SIZEOF_INT = Unsafe.ARRAY_INT_INDEX_SCALE;

    /**
     * load factor
     */
    private double loadfactor;

    /**
     * Stack of backtrackable search variables.
     */
    private StoredBool[] variableStack;


    /**
     * Stack of values (former values that need be restored upon backtracking).
     */
    private long valueStack;


    /**
     * Stack of timestamps indicating the world where the former value
     * had been written.
     */
    private long stampStack;


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

    public UnsafeBoolTrail(int nUpdates, int nWorlds, double loadfactor) {
        unsafe = getTheUnsafe();
        currentLevel = 0;
        variableStack = new StoredBool[nUpdates];
        valueStack = unsafe.allocateMemory(nUpdates * SIZEOF_DATA);
        stampStack = unsafe.allocateMemory(nUpdates * SIZEOF_INT);
        worldStartLevels = new int[nWorlds];
        this.loadfactor = loadfactor;
    }

    public static Unsafe getTheUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            return null;
        }
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
            resizeWorldCapacity((int)(worldStartLevels.length * loadfactor));
        }
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */
    @Override
    public void worldPop(int worldIndex) {
        StoredBool v;
        byte value;
        int stamp;
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            v = variableStack[currentLevel];
            value = unsafe.getByte(valueStack + (currentLevel * SIZEOF_DATA));
            stamp = unsafe.getInt(stampStack + (currentLevel * SIZEOF_INT));
            v._set(value == 1, stamp);
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
    @Override
    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }


    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    @Override
    public void savePreviousState(StoredBool v, boolean oldValue, int oldStamp) {
        unsafe.putByte(valueStack + currentLevel * SIZEOF_DATA, (byte) (oldValue ? 1 : 0));
        variableStack[currentLevel] = v;
        unsafe.putInt(stampStack + currentLevel * SIZEOF_INT, oldStamp);
        currentLevel++;
        if (variableStack.length == currentLevel) {
            resizeUpdateCapacity();
        }
    }

    @Override
    public void buildFakeHistory(StoredBool v, boolean initValue, int olderStamp) {
        // from world 0 to fromStamp (excluded), create a fake history based on initValue
        // kind a copy of the current elements
        // first save the current state on the top of the stack
        savePreviousState(v, initValue, olderStamp - 1);
        // second: ensures capacities
        while (currentLevel + olderStamp > variableStack.length) {
            resizeUpdateCapacity();
        }
        int i1, f, s = currentLevel;
        for (int w = olderStamp; w > 1; w--) {
            f = worldStartLevels[w];
            i1 = f + w - 1;
            s -= f;
            System.arraycopy(variableStack, f, variableStack, i1, s);
            unsafe.copyMemory(valueStack + f * SIZEOF_DATA, valueStack + i1 * SIZEOF_DATA, SIZEOF_DATA * s);
            unsafe.copyMemory(stampStack + f * SIZEOF_INT, stampStack + i1 * SIZEOF_INT, SIZEOF_INT * s);
            variableStack[--i1] = v;
            unsafe.putByte(valueStack + i1 * SIZEOF_DATA, (byte) (initValue ? 1 : 0));
            unsafe.putInt(stampStack + i1 * SIZEOF_INT, w - 2);
            worldStartLevels[w] += w - 1;
            currentLevel++;
            s = f;
        }
    }


    private void resizeUpdateCapacity() {
        int oldCapacity = variableStack.length;
        int newCapacity = (int)(oldCapacity * loadfactor);
        // first, copy the stack of variables
        StoredBool[] tmp1 = new StoredBool[newCapacity];
        System.arraycopy(variableStack, 0, tmp1, 0, oldCapacity);
        variableStack = tmp1;
        // then, copy the stack of former values
        long ad1 = unsafe.allocateMemory(newCapacity * SIZEOF_DATA);
        unsafe.copyMemory(valueStack, ad1, oldCapacity * SIZEOF_DATA);
        unsafe.freeMemory(valueStack);
        valueStack = ad1;
        // then, copy the stack of world stamps
        long ad2 = unsafe.allocateMemory(newCapacity * SIZEOF_INT);
        unsafe.copyMemory(stampStack, ad2, oldCapacity * SIZEOF_INT);
        unsafe.freeMemory(stampStack);
        stampStack = ad2;
    }

    private void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        unsafe.freeMemory(valueStack);
        unsafe.freeMemory(stampStack);
    }
}
