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

package solver.explanations.antidom;

import common.util.iterators.DisposableValueIterator;
import memory.IStateInt;
import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 20/10/11
 * Time: 16:52
 *
 * @Deprecated
 */
@Deprecated
public class AntiDomBipartiteSet implements AntiDomain {
    int offset;
    IStateInt firstIn;
    int[] values;
    int[] pos;
//    AntiDomBitset bu;

    private DisposableValueIterator _viterator;

    public AntiDomBipartiteSet(IntVar A) {
        offset = A.getLB();
        firstIn = A.getSolver().getEnvironment().makeInt();
        values = new int[A.getDomainSize()];
        pos = new int[A.getDomainSize()];
        for (int i = 0; i < values.length; i++) {
            values[i] = i + offset;
            pos[i] = i;
        }
//        bu = new AntiDomBitset(A);
    }


    public void set(int outsideval) {
//        assert compare(outsideval);
//        System.out.println("------------------------");
//        System.out.println(this.toString());
//        System.out.println("val:" + outsideval);
//        System.out.println(Arrays.toString(values));
//        System.out.println(Arrays.toString(pos));
//        System.out.println("in:" + firstIn.get());

        int idxVal = outsideval - offset;
        int in = firstIn.add(1) - 1;

        int p = pos[in];
        pos[in] = pos[idxVal];
        pos[idxVal] = p;


        int i = values[pos[in]];
        int j = values[pos[idxVal]];
        values[pos[in]] = j;
        values[pos[idxVal]] = i;

//        bu.set(outsideval);

//        System.out.println(this.toString());
//        System.out.println("val:" + outsideval);
//        System.out.println(Arrays.toString(values));
//        System.out.println(Arrays.toString(pos));
//        System.out.println("in:" + firstIn.get());

//        assert compare(outsideval);
    }


    /*public boolean compare(int val) {
        TIntHashSet vlaues = new TIntHashSet();
        DisposableValueIterator it1 = this.getValueIterator();
        while (it1.hasNext()) {
            vlaues.add(it1.next());
        }
        DisposableValueIterator it2 = bu.getValueIterator();
        while (it2.hasNext()) {
            assert vlaues.contains(it2.next()) : val + ":" + this.toString() + "\n" + bu.toString();
        }
        it1.dispose();
        it2.dispose();
        return true;
    }  */


    public boolean get(int outsideval) {
        int inside = outsideval - offset;
        return pos[inside] < firstIn.get();
    }

    public DisposableValueIterator getValueIterator() {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                int from;
                int to;


                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    this.from = 0;
                    this.to = firstIn.get();
                }

                @Override
                public void topDownInit() {
                    this.from = 0;
                    this.to = firstIn.get();
                }

                @Override
                public boolean hasNext() {
                    return from < to;
                }

                @Override
                public boolean hasPrevious() {
                    return from <= to;
                }

                @Override
                public int next() {
                    return values[from++];
                }

                @Override
                public int previous() {
                    return values[--to];
                }
            };
        }
        _viterator.bottomUpInit();
        return _viterator;
    }

    @Override
    public String toString() {

        StringBuffer bf = new StringBuffer();
        bf.append("[");
        DisposableValueIterator it = getValueIterator();
        while (it.hasNext()) {
            bf.append(" " + it.next());
        }
        it.dispose();
        bf.append("]");
        return bf.toString();
    }
}
