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

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/12
 */
public class WaterMarkingLongImpl implements IWaterMarking {

    protected TLongObjectHashMap<TIntHashSet> elements;

    protected long pivot;


    public WaterMarkingLongImpl(long pivot) {
        elements = new TLongObjectHashMap<TIntHashSet>();
        this.pivot = pivot;
    }

    private long _id(long id1, long id2) {
        if (id1 < id2) {
            return id1 * pivot + id2;
        } else {
            return id2 * pivot + id1;
        }
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public void putMark(int id) {
        elements.put(id, null);
    }

    @Override
    public void putMark(int id1, int id2, int id3) {
        long id = _id(id1, id2);
        if (elements.contains(id)) {
            elements.get(id).add(id3);
        } else {
            TIntHashSet tmp = new TIntHashSet();
            tmp.add(id3);
            elements.put(id, tmp);
        }
    }

    @Override
    public void clearMark(int id) {
        elements.remove(id);
    }

    @Override
    public void clearMark(int id1, int id2, int id3) {
        long id = _id(id1, id2);
        TIntHashSet tmp = elements.get(id);
        if (tmp != null) {
            tmp.remove(id3);
            if (tmp.isEmpty()) {
                elements.remove(id);
            }
        }
    }

    @Override
    public boolean isMarked(int id) {
        return elements.containsKey(id);
    }

    @Override
    public boolean isMarked(int id1, int id2, int id3) {
        TIntHashSet tmp = elements.get(_id(id1, id2));
        return tmp != null && tmp.contains(id3);
    }
}
