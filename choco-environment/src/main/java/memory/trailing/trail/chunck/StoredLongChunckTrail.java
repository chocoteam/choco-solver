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

package memory.trailing.trail.chunck;

import memory.trailing.StoredLong;
import memory.trailing.trail.IStoredLongTrail;


/**
 * Implementing storage of historical values for backtrackable integers.
 *
 * @see memory.IStorage
 */
public class StoredLongChunckTrail implements IStoredLongTrail {

    private static final int CHUNK_SIZE = 20000;

    /**
     * Stack of backtrackable search variables.
     */

    private StoredLong[][] variableStack;


    /**
     * Stack of values (former values that need be restored upon backtracking).
     */

    private long[][] valueStack;


    /**
     * Stack of timestamps indicating the world where the former value
     * had been written.
     */

    private int[][] stampStack;


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


    /**
     * Constructs a trail with predefined size.
     *
     * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     */

    public StoredLongChunckTrail(int nUpdates, int nWorlds) {
        curChunk = nextTop = 0;

        variableStack = new StoredLong[1][];
        variableStack[0] = new StoredLong[CHUNK_SIZE];

        valueStack = new long[1][];
        valueStack[0] = new long[CHUNK_SIZE];

        stampStack = new int[1][];
        stampStack[0] = new int[CHUNK_SIZE];

        chunks = new int[nWorlds];
        tops = new int[nWorlds];
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
        long[] cval;
        int[] cstmp;
        for (int cc = curChunk; cc >= c; cc--) {
            cvar = variableStack[cc];
            cval = valueStack[cc];
            cstmp = stampStack[cc];
            int tt = (cc == curChunk ? nextTop : CHUNK_SIZE) - 1;
            int to = (cc == c ? t : 0);
            for (; tt >= to; tt--) {
                cvar[tt]._set(cval[tt], cstmp[tt]);
            }
        }
        curChunk = c;
        nextTop = t;
    }


    /**
     * Returns the current size of the stack.
     */

    public int getSize() {
        return curChunk * CHUNK_SIZE + nextTop;
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
        valueStack[curChunk][nextTop] = oldValue;
        variableStack[curChunk][nextTop] = v;
        stampStack[curChunk][nextTop] = oldStamp;
        nextTop++;
        if (nextTop == CHUNK_SIZE) {
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
        varBigger[l] = new StoredLong[CHUNK_SIZE];
        variableStack = varBigger;

        long[][] valBigger = new long[l + 1][];
        System.arraycopy(valueStack, 0, valBigger, 0, l);
        valBigger[l] = new long[CHUNK_SIZE];
        valueStack = valBigger;

        int[][] staBigger = new int[l + 1][];
        System.arraycopy(stampStack, 0, staBigger, 0, l);
        staBigger[l] = new int[CHUNK_SIZE];
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
}

