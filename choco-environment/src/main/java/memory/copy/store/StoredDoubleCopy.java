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
package memory.copy.store;

import memory.copy.RcDouble;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/05/13
 */
public class StoredDoubleCopy implements IStoredDoubleCopy {

    RcDouble[] objects;
    double[][] values;
    int position;

    public StoredDoubleCopy() {
        objects = new RcDouble[64];
        values = new double[64][];
    }


    public void add(RcDouble rc) {
        if (position == objects.length) {
            int newSize = objects.length * 3 / 2 + 1;
            RcDouble[] oldElements = objects;
            objects = new RcDouble[newSize];
            System.arraycopy(oldElements, 0, objects, 0, oldElements.length);
        }
        objects[position++] = rc;
    }

    public void worldPush(int worldIndex) {
        if (values.length <= worldIndex) {
            double[][] tmp = values;
            values = new double[tmp.length * 3 / 2 + 1][];
            System.arraycopy(tmp, 0, values, 0, tmp.length);
        }
        double[] tmpdouble = new double[position];
        for (int i = position; --i >= 0; ) {
            tmpdouble[i] = objects[i].deepCopy();
        }
        values[worldIndex] = tmpdouble;
    }

    public void worldPop(int worldIndex) {
        double[] tmpdouble = values[worldIndex];
        for (int i = tmpdouble.length; --i >= 0; )
            objects[i]._set(tmpdouble[i], worldIndex);
    }

    @Override
    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void buildFakeHistory(RcDouble v, double initValue, int olderStamp) {
        for (int i = 1; i <= olderStamp; i++) {
            double[] _values = values[i];
            int size = _values.length;
            values[i] = new double[position];
            System.arraycopy(_values, 0, values[i], 0, size);
            values[i][size] = initValue;
        }
    }
}
