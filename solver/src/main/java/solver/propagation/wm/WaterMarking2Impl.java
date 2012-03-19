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
package solver.propagation.wm;

import gnu.trove.set.hash.TIntHashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/12
 */
public class WaterMarking2Impl implements IWaterMarking {

    protected TIntHashSet[] elements;

    private int OFFSET = 1;

    protected int pivot;


    public WaterMarking2Impl(int pivot) {
        long nbe2 = pivot * pivot;
        if (nbe2 > Integer.MAX_VALUE) {
            throw new RuntimeException("WaterMarkingImpl cannot handle so much elements!");
        }
        elements = new TIntHashSet[8];
        this.pivot = pivot;
    }

    private int _id(int id1, int id2) {
        if (id1 < id2) {
            return id1 * pivot + id2;
        } else {
            return id2 * pivot + id1;
        }
    }

    @Override
    public int size() {
        int s = 0;
        for (int i = 0; i < elements.length; i++) {
            s += (elements[i]==null?0:elements[i].size());
        }
        return s;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void putMark(int id) {
        putMark(0, id, -1);
    }

    @Override
    public void putMark(int id1, int id2, int id3) {
        int id = _id(id1, id2);
        if (id3 + OFFSET >= elements.length) {
            TIntHashSet[] tmp = elements;
            elements = new TIntHashSet[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, elements, 0, tmp.length);
        }
        if (elements[id3 + OFFSET] == null) {
            elements[id3 + OFFSET] = new TIntHashSet();
        }
        elements[id3 + OFFSET].add(id);
    }

    @Override
    public void clearMark(int id) {
        clearMark(0, id, -1);
    }

    @Override
    public void clearMark(int id1, int id2, int id3) {
        int id = _id(id1, id2);
        // we assume it alredy exists
        elements[id3 + OFFSET].remove(id);
    }

    @Override
    public boolean isMarked(int id) {
        return isMarked(0, id, -1);
    }

    @Override
    public boolean isMarked(int id1, int id2, int id3) {
        return id3 + OFFSET < elements.length
                && elements[id3 + OFFSET] != null
                && elements[id3 + OFFSET].contains(_id(id1, id2));
    }
}
