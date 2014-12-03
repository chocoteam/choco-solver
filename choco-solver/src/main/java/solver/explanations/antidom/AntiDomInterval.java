/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.explanations.antidom;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 20/10/11
 * Time: 16:52
 */
public class AntiDomInterval implements AntiDomain {
    int initLB, initUB;
    IStateInt lbidx;
    int[] lbs;
    IStateInt ubidx;
    int[] ubs;

    private DisposableValueIterator _viterator;

    public AntiDomInterval(IntVar A) {
        IEnvironment env = A.getSolver().getEnvironment();

        lbidx = env.makeInt(-1);
        ubidx = env.makeInt(-1);
        initLB = A.getLB();
        initUB = A.getUB();
        lbs = new int[16];
        ubs = new int[16];
    }


    @Override
    public void add(int outsideval) {
        int lbi = lbidx.get();
        int ubi = ubidx.get();
        if ((lbi >= 0 && outsideval == lbs[lbi] + 1)
                || (lbi < 0 && outsideval == initLB)) {
            int id = lbidx.add(1);
            resizelbs(id + 1);
            lbs[id] = outsideval;
        } else if ((ubi >= 0 && outsideval == ubs[ubi] - 1)
                || (ubi < 0 && outsideval == initUB)) {
            int id = ubidx.add(1);
            resizeubs(id + 1);
            ubs[id] = outsideval;
        } else {
            throw new SolverException("Unknown value");
        }
    }

    @Override
    public void updateLowerBound(int oldLB, int newLB) {
        int lbi = lbidx.get();
        if ((lbi >= 0)
                || (lbi < 0 && oldLB == initLB)) {
            int id = lbidx.add(1);
            resizelbs(id + 1);
            lbs[id] = newLB - 1;
        } else {
            throw new SolverException("Unknown value");
        }
    }

    @Override
    public void updateUpperBound(int oldUB, int newUB) {
        int ubi = ubidx.get();
        if ((ubi >= 0)
                || (ubi < 0 && oldUB == initUB)) {
            int id = ubidx.add(1);
            resizeubs(id + 1);
            ubs[id] = newUB + 1;
        } else {
            throw new SolverException("Unknown value");
        }
    }

    private void resizelbs(int capacity) {
        if (lbs.length < capacity) {
            int[] tmp = lbs;
            lbs = new int[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, lbs, 0, tmp.length);
        }
    }

    private void resizeubs(int capacity) {
        if (ubs.length < capacity) {
            int[] tmp = ubs;
            ubs = new int[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, ubs, 0, tmp.length);
        }
    }

    @Override
    public boolean get(int outsideval) {
//        return false;
        int lbi = lbidx.get();
        if (lbi >= 0 && initLB <= outsideval && outsideval <= lbs[lbi]) {
            return true;
        }
        int ubi = ubidx.get();
        return ubi >= 0 && ubs[ubi] <= outsideval && outsideval <= initUB;
    }

    private int binarySearchLU(int fromIndex, int toIndex, int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int UB = lbs[mid];
            int LB = lbs[mid - 1];

            if (key > UB) {
                low = mid + 1;
            } else if (key < LB) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        throw new SolverException("Cannot found correct interval");
    }


    private int binarySearchUL(int fromIndex, int toIndex, int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int UB = ubs[mid - 1];
            int LB = ubs[mid];

            if (key > UB) {
                high = mid - 1;
            } else if (key < LB) {
                low = mid + 1;
            } else {
                return mid; // key found
            }
        }
        throw new SolverException("Cannot found correct interval");
    }

    @Override
    public int getKeyValue(int val) {
        int lbi = lbidx.get();
        if (lbi >= 0 && initLB <= val && val <= lbs[lbi]) {
            if (val <= lbs[0]) {
                return lbs[0];
            } else {
                int ii = binarySearchLU(1, lbi + 1, val);
                assert val <= lbs[ii];
                return lbs[ii];
            }
        }
        int ubi = ubidx.get();
        if (ubi >= 0 && ubs[ubi] <= val && val <= initUB) {
            if (val >= ubs[0]) {
                return ubs[0];
            } else {
                int ii = binarySearchUL(1, ubi + 1, val);
                assert val >= ubs[ii];
                return ubs[ii];
            }
        }
        return val;
    }

    @Override
    public int size() {
        return lbidx.get() + ubidx.get() + 2;
    }

    public DisposableValueIterator getValueIterator() {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                int lbi;
                int clbi;
                int ubi;
                int cubi;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    lbi = lbidx.get();
                    clbi = 0;
                    ubi = ubidx.get();
                    cubi = 0;
                }

                @Override
                public void topDownInit() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean hasNext() {
                    return clbi <= lbi || cubi <= ubi;
                }

                @Override
                public boolean hasPrevious() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int next() {
                    if (clbi <= lbi) {
                        return lbs[clbi++];
                    } else {
                        return ubs[cubi++];
                    }
                }

                @Override
                public int previous() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        _viterator.bottomUpInit();
        return _viterator;
    }

    @Override
    public String toString() {

        StringBuilder bf = new StringBuilder();
        bf.append("[");
        DisposableValueIterator it = getValueIterator();
        while (it.hasNext()) {
            bf.append(" ").append(it.next());
        }
        it.dispose();
        bf.append("]");
        return bf.toString();
    }

    @Override
    public boolean isEnumerated() {
        return false;
    }
}
