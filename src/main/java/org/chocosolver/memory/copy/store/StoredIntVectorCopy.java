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
package org.chocosolver.memory.copy.store;

import org.chocosolver.memory.IStorage;
import org.chocosolver.memory.copy.RcIntVector;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/05/13
 */
public class StoredIntVectorCopy implements IStorage {

    RcIntVector[] objects;
    int[][][] values;
    int position;

    int lastSavedWorldIndex;

    public StoredIntVectorCopy(int worldIndex) {
        objects = new RcIntVector[64];
        lastSavedWorldIndex = worldIndex;
    }


    public void add(RcIntVector rc) {
        if (position == objects.length) {
            int newSize = objects.length * 3 / 2 + 1;
            RcIntVector[] oldElements = objects;
            objects = new RcIntVector[newSize];
            System.arraycopy(oldElements, 0, objects, 0, oldElements.length);
        }
        objects[position++] = rc;
    }

    @Override
    public void worldPush(int worldIndex) {
        if (lastSavedWorldIndex >= worldIndex) lastSavedWorldIndex = 0;

        if (values.length >= worldIndex) {
            int[][][] tmp = values;
            values = new int[worldIndex * 3 / 2 + 1][][];
            System.arraycopy(tmp, 0, values, 0, worldIndex - 1);
        }
        int[][] tmpint = new int[objects.length][];
        for (int i = objects.length; --i >= 0; ) {
            if (worldIndex != 0 && lastSavedWorldIndex >= (objects[i]).getTimeStamp()) {
                tmpint[i] = values[lastSavedWorldIndex][i];
            } else {
                tmpint[i] = objects[i].deepCopy();
            }
        }
        values[worldIndex] = tmpint;
    }

    @Override
    public void worldPop(int worldIndex) {
        int[][] tmpobj = values[worldIndex];
        for (int i = tmpobj.length; --i >= 0; )
            objects[i]._set(tmpobj[i], worldIndex);
    }

    @Override
    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

}
