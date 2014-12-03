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
package org.chocosolver.solver.constraints.extension.binary;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * AC3 bit rm algorithm for binary table constraint
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 22/04/2014
 */
public class PropBinAC3bitrm extends PropBinCSP {

    protected int offset0;
    protected int offset1;

    protected int minS0;    //value with minimum number of supports for v0
    protected int minS1;    //value with minimum number of supports for v1

    protected int initDomSize0;
    protected int initDomSize1;

    public PropBinAC3bitrm(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
    }

    private PropBinAC3bitrm(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            offset0 = v0.getLB();
            offset1 = v1.getLB();

            initDomSize0 = v0.getDomainSize();
            initDomSize1 = v1.getDomainSize();

            fastInitNbSupports();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val0 = itv0.next();
                    if (!((CouplesBitSetTable) relation).checkValue(0, val0, v1)) {
                        if (val0 == right + 1) {
                            right = val0;
                        } else {
                            v0.removeInterval(left, right, this);
                            left = right = val0;
                        }
                    }
                }
                v0.removeInterval(left, right, this);
            } finally {
                itv0.dispose();
            }
            itv0 = v1.getValueIterator(true);
            left = right = Integer.MIN_VALUE;
            try {
                while (itv0.hasNext()) {
                    int val1 = itv0.next();
                    if (!((CouplesBitSetTable) relation).checkValue(1, val1, v0)) {
                        if (val1 == right + 1) {
                            right = val1;
                        } else {
                            v1.removeInterval(left, right, this);
                            left = right = val1;
                        }
                    }
                }
                v1.removeInterval(left, right, this);
            } finally {
                itv0.dispose();
            }
        }
        reviseV0();
        reviseV1();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (IntEventType.isInstantiate(mask)) {
            onInstantiationOf(idxVarInProp);
        } else if (idxVarInProp == 0) {
            reviseV1();
        } else {
            reviseV0();
        }
    }


    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);

            identitymap.put(this, new PropBinAC3bitrm(X, Y, (CouplesBitSetTable) relation.duplicate()));
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void fastInitNbSupports() {
        int[] initS1 = new int[v1.getUB() - v1.getLB() + 1];
        minS0 = Integer.MAX_VALUE;
        minS1 = Integer.MAX_VALUE;
        DisposableValueIterator itv0 = v0.getValueIterator(true);
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            int initS0 = 0;
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    initS0++;
                    initS1[val1 - offset1]++;
                }
            }
            if (initS0 < minS0) minS0 = initS0;
            itv1.dispose();
        }
        itv0.dispose();
        for (int i = 0; i < initS1.length; i++) {
            if (initS1[i] < minS1) minS1 = initS1[i];
        }
    }


    /**
     * updates the support for all values in the domain of v1, and remove unsupported values for v1
     */
    private void reviseV1() throws ContradictionException {
        int v0Size = v0.getDomainSize();
        if (minS1 <= (initDomSize0 - v0Size)) {
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            try {
                while (itv1.hasNext()) {
                    int y = itv1.next();
                    if (!((CouplesBitSetTable) relation).checkValue(1, y, v0)) {
                        if (y == right + 1) {
                            right = y;
                        } else {
                            v1.removeInterval(left, right, this);
                            left = right = y;
                        }
                        //                        v1.removeVal(y, this, false);
                    }
                }
                v1.removeInterval(left, right, this);
            } finally {
                itv1.dispose();
            }
        }
    }

    /**
     * updates the support for all values in the domain of v0, and remove unsupported values for v0
     */
    private void reviseV0() throws ContradictionException {
        int v1Size = v1.getDomainSize();
        if (minS0 <= (initDomSize1 - v1Size)) {
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int x = itv0.next();
                    if (!((CouplesBitSetTable) relation).checkValue(0, x, v1)) {
                        if (x == right + 1) {
                            right = x;
                        } else {
                            v0.removeInterval(left, right, this);
                            left = right = x;
                        }
                        //                        v0.removeVal(x, this, false);
                    }
                }
                v0.removeInterval(left, right, this);
            } finally {
                itv0.dispose();
            }
        }
    }


    private void onInstantiationOf(int idx) throws ContradictionException {
        if (idx == 0) {
            int value = v0.getValue();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            try {
                while (itv1.hasNext()) {
                    int val = itv1.next();
                    if (!relation.isConsistent(value, val)) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            v1.removeInterval(left, right, this);
                            left = right = val;
                        }
                    }
                }
                v1.removeInterval(left, right, this);
            } finally {
                itv1.dispose();
            }
        } else {
            int value = v1.getValue();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val = itv0.next();
                    if (!relation.isConsistent(val, value)) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            v0.removeInterval(left, right, this);
                            left = right = val;
                        }
                    }
                }
                v0.removeInterval(left, right, this);
            } finally {
                itv0.dispose();
            }
        }
    }

}
