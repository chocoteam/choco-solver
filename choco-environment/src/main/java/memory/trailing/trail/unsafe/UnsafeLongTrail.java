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
package memory.trailing.trail.unsafe;

import memory.trailing.StoredLong;
import memory.trailing.trail.IStoredLongTrail;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/13
 */
public class UnsafeLongTrail implements IStoredLongTrail {

    private final boolean isUnsafe;

    private final Unsafe unsafe;

    public static final int SIZEOF_DATA = 8;

    public static final int SIZEOF_INT = 4;


    private static final int DEFAULT_CHUNK_SIZE = 20000;


    /**
     * Stack of backtrackable search variables.
     */

    private StoredLong[][] variableStack;


    /**
     * Stack of values (former values that need be restored upon backtracking).
     */

    private long[] valueStack;


    /**
     * Stack of timestamps indicating the world where the former value
     * had been written.
     */

    private long[] stampStack;


    /**
     * Points the level of the last entry.
     */

    private int curChunk;

    private int nextTop;


    /**
     * A stack of pointers (for each start of a world).
     */

    private int[] chunks;
    private int[] tops;

    public UnsafeLongTrail(int nUpdates, int nWorlds) {
        unsafe = getTheUnsafe();
        isUnsafe = unsafe != null;
        curChunk = nextTop = 0;

        variableStack = new StoredLong[1][];
        variableStack[0] = new StoredLong[DEFAULT_CHUNK_SIZE];

        valueStack = new long[1];
        assert unsafe != null;
        valueStack[0] = unsafe.allocateMemory(DEFAULT_CHUNK_SIZE * SIZEOF_DATA);

        stampStack = new long[1];
        stampStack[0] = unsafe.allocateMemory(DEFAULT_CHUNK_SIZE * SIZEOF_INT);

        chunks = new int[nWorlds];
        tops = new int[nWorlds];

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
     * @param worldIndex
     */

    public void worldPush(int worldIndex) {
        chunks[worldIndex] = curChunk;
        tops[worldIndex] = nextTop;
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex
     */

    public void worldPop(int worldIndex) {
        final int c = chunks[worldIndex];
        final int t = tops[worldIndex];
        StoredLong[] cvar;
        long cval;
        int cstmp;
        for (int cc = curChunk; cc >= c; cc--) {
            cvar = variableStack[cc];
            int tt = (cc == curChunk ? nextTop : DEFAULT_CHUNK_SIZE) - 1;
            int to = (cc == c ? t : 0);
            for (; tt >= to; tt--) {
                cval = unsafe.getLong(valueStack[cc] + (tt * SIZEOF_DATA));
                cstmp = unsafe.getInt(stampStack[cc] + (tt * SIZEOF_INT));
                cvar[tt]._set(cval, cstmp);
            }
        }
        curChunk = c;
        nextTop = t;
    }


    /**
     * Returns the current size of the stack.
     */

    public int getSize() {
        return curChunk * DEFAULT_CHUNK_SIZE + nextTop;
    }


    /**
     * Comits a world: merging it with the previous one.
     */

    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(StoredLong v, long oldValue, int oldStamp) {
        unsafe.putLong(valueStack[curChunk] + (nextTop * SIZEOF_DATA), oldValue);
        variableStack[curChunk][nextTop] = v;
        unsafe.putInt(stampStack[curChunk] + (nextTop * SIZEOF_INT), oldStamp);
        nextTop++;
        if (nextTop == DEFAULT_CHUNK_SIZE) {
            curChunk++;
            int l = variableStack.length;
            if (curChunk == l) {
                increase(l);
            }
            nextTop = 0;
        }
    }

    private void increase(int l) {
        StoredLong[][] varBigger = new StoredLong[l + 1][];
        System.arraycopy(variableStack, 0, varBigger, 0, l);
        varBigger[l] = new StoredLong[DEFAULT_CHUNK_SIZE];
        variableStack = varBigger;

        long[] valBigger = new long[l + 1];
        System.arraycopy(valueStack, 0, valBigger, 0, l);
        valBigger[l] = unsafe.allocateMemory(DEFAULT_CHUNK_SIZE * SIZEOF_DATA);
        valueStack = valBigger;

        long[] staBigger = new long[l + 1];
        System.arraycopy(stampStack, 0, staBigger, 0, l);
        staBigger[l] = unsafe.allocateMemory(DEFAULT_CHUNK_SIZE * SIZEOF_INT);
        stampStack = staBigger;
    }

    public void resizeWorldCapacity(int newWorldCapacity) {
        int[] tmp = new int[newWorldCapacity];
        System.arraycopy(chunks, 0, tmp, 0, chunks.length);
        chunks = tmp;

        tmp = new int[newWorldCapacity];
        System.arraycopy(tops, 0, tmp, 0, tops.length);
        tops = tmp;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        final int c = chunks[0];
        for (int cc = valueStack.length; cc >= c; cc--) {
            unsafe.freeMemory(valueStack[cc]);
            unsafe.freeMemory(stampStack[cc]);
        }
    }
}
