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

import org.chocosolver.memory.copy.RcLong;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/05/13
 */
public class StoredLongCopy implements IStoredLongCopy {

    RcLong[] objects;
    long[][] values;
    int position;

    public StoredLongCopy() {
        objects = new RcLong[64];
        values = new long[64][];
    }


    public void add(RcLong rc) {
        if (position == objects.length) {
            int newSize = objects.length * 3 / 2 + 1;
            RcLong[] oldElements = objects;
            objects = new RcLong[newSize];
            System.arraycopy(oldElements, 0, objects, 0, oldElements.length);
        }
        objects[position++] = rc;
    }

    public void worldPush(int worldIndex) {
        if (values.length <= worldIndex) {
            long[][] tmp = values;
            values = new long[tmp.length * 3 / 2 + 1][];
            System.arraycopy(tmp, 0, values, 0, tmp.length);
        }
        long[] tmplong = new long[position];
        for (int i = position; --i >= 0; ) {
            tmplong[i] = objects[i].deepCopy();
        }
        values[worldIndex] = tmplong;
    }

    public void worldPop(int worldIndex) {
        long[] tmplong = values[worldIndex];
        for (int i = tmplong.length; --i >= 0; )
            objects[i]._set(tmplong[i], worldIndex);
    }

    @Override
    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void buildFakeHistory(RcLong v, long initValue, int olderStamp) {
        for (int i = 1; i <= olderStamp; i++) {
            long[] _values = values[i];
            int size = _values.length;
            values[i] = new long[position];
            System.arraycopy(_values, 0, values[i], 0, size);
            values[i][size] = initValue;
        }
    }
}
