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
package solver.constraints.extension.binary;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.extension.Tuples;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.events.IntEventType;
import solver.variables.events.PropagatorEventType;
import util.iterators.DisposableValueIterator;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropBinAC3rm extends PropBinCSP {

    protected int[] currentSupport0;
    protected int[] currentSupport1;

    protected int offset0;
    protected int offset1;

    protected int[] initS0; //initial number of supports of each value of x0
    protected int[] initS1; //initial number of supports of each value of x0
    protected int minS0;    //value with minimum number of supports for v0
    protected int minS1;    //value with minimum number of supports for v1

    protected int initDomSize0;
    protected int initDomSize1;


    public PropBinAC3rm(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
    }

    private PropBinAC3rm(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            initProp();
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

            identitymap.put(this, new PropBinAC3rm(X, Y, (CouplesBitSetTable) relation.duplicate()));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void fastInitNbSupports(int a, int b) {
        DisposableValueIterator itv0 = v0.getValueIterator(true);
        int cpt1 = 0;
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            cpt1++;
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            int cpt2 = 0;
            while (itv1.hasNext()) {
                cpt2++;
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    initS0[val0 - offset0]++;
                    initS1[val1 - offset1]++;
                }
                if (cpt2 >= a) break;
            }
            itv1.dispose();
            if (cpt1 >= b) break;
        }
        itv0.dispose();
        minS0 = Integer.MAX_VALUE;
        minS1 = Integer.MAX_VALUE;
        for (int i = 0; i < initS0.length; i++) {
            if (initS0[i] < minS0) minS0 = initS0[i];
        }
        for (int i = 0; i < initS1.length; i++) {
            if (initS1[i] < minS1) minS1 = initS1[i];
        }
    }

    public boolean testDeepakConditionV1(int y, int v0Size) {
        return initS1[y - offset1] <= (initDomSize0 - v0Size);
    }

    public boolean testDeepakConditionV0(int x, int v1Size) {
        return initS0[x - offset0] <= (initDomSize1 - v1Size);
    }

    public int getSupportV1(int y) {
        return currentSupport1[y - offset1];
    }

    public int getSupportV0(int x) {
        return currentSupport0[x - offset0];
    }

    /**
     * updates the support for all values in the domain of v1, and remove unsupported values for v1
     *
     * @throws ContradictionException
     */
    public void reviseV1() throws ContradictionException {
        int v0Size = v0.getDomainSize();
        if (minS1 <= (initDomSize0 - v0Size)) {
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (itv1.hasNext()) {
                    int y = itv1.next();
                    if (testDeepakConditionV1(y, v0Size)) { //initS1[y - offset1] <= (initDomSize0 - v0Size)) {
                        if (!v0.contains(getSupportV1(y))) {
                            boolean found = false;
                            int support = 0;
                            DisposableValueIterator itv0 = v0.getValueIterator(true);
                            while (!found && itv0.hasNext()) {
                                support = itv0.next();
                                if (relation.isConsistent(support, y)) found = true;
                            }
                            itv0.dispose();

                            if (found) {
                                storeSupportV1(support, y);
                            } else {
                                if (y == right + 1) {
                                    right = y;
                                } else {
                                    v1.removeInterval(left, right, this);
                                    left = right = y;
                                }
                            }
                        }
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
     *
     * @throws ContradictionException
     */
    public void reviseV0() throws ContradictionException {
        int v1Size = v1.getDomainSize();
        if (minS0 <= (initDomSize1 - v1Size)) {
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (itv0.hasNext()) {
                    int x = itv0.next();
                    if (testDeepakConditionV0(x, v1Size)) { //initS0[x - offset0] <= (initDomSize1 - v1Size)) {
                        if (!v1.contains(getSupportV0(x))) {
                            boolean found = false;
                            int support = 0;
                            DisposableValueIterator itv1 = v1.getValueIterator(true);
                            while (!found && itv1.hasNext()) {
                                support = itv1.next();
                                if (relation.isConsistent(x, support)) found = true;
                            }
                            itv1.dispose();
                            if (found) {
                                storeSupportV0(support, x);
                            } else {
                                if (x == right + 1) {
                                    right = x;
                                } else {
                                    v0.removeInterval(left, right, this);
                                    left = right = x;
                                }
                            }
                        }
                    }
                }
                v0.removeInterval(left, right, this);
            } finally {
                itv0.dispose();
            }
        }
    }

    public void storeSupportV0(int support, int x) {
        currentSupport0[x - offset0] = support;
        currentSupport1[support - offset1] = x;
    }

    public void storeSupportV1(int support, int y) {
        currentSupport1[y - offset1] = support;
        currentSupport0[support - offset0] = y;
    }


    public void initProp() throws ContradictionException {
        offset1 = v1.getLB();
        offset0 = v0.getLB();
        currentSupport0 = new int[v0.getUB() - v0.getLB() + 1];
        currentSupport1 = new int[v1.getUB() - v1.getLB() + 1];
        initS0 = new int[v0.getUB() - v0.getLB() + 1];
        initS1 = new int[v1.getUB() - v1.getLB() + 1];

        initDomSize0 = v0.getDomainSize();
        initDomSize1 = v1.getDomainSize();

        Arrays.fill(currentSupport0, -1);
        Arrays.fill(currentSupport1, -1);
        //double cardprod = v0.getDomainSize() * v1.getDomainSize();
        //if (cardprod <= 7000)
        fastInitNbSupports(Integer.MAX_VALUE, Integer.MAX_VALUE);
        //else fastInitNbSupports(80,80);
        DisposableValueIterator itv0 = v0.getValueIterator(true);
        int left = Integer.MIN_VALUE;
        int right = left;
        int support = 0;
        boolean found = false;
        try {
            while (itv0.hasNext()) {
                DisposableValueIterator itv1 = v1.getValueIterator(true);
                int val0 = itv0.next();
                while (itv1.hasNext()) {
                    int val1 = itv1.next();
                    if (relation.isConsistent(val0, val1)) {
                        support = val1;
                        found = true;
                        break;
                    }
                }
                itv1.dispose();
                if (!found) {
                    if (val0 == right + 1) {
                        right = val0;
                    } else {
                        v0.removeInterval(left, right, this);
                        left = right = val0;
                    }
                } else {
                    storeSupportV0(support, val0);
                }
                found = false;
            }
            v0.removeInterval(left, right, this);
        } finally {
            itv0.dispose();
        }
        found = false;
        DisposableValueIterator itv1 = v1.getValueIterator(true);
        left = right = Integer.MIN_VALUE;
        try {
            while (itv1.hasNext()) {
                itv0 = v0.getValueIterator(true);
                int val1 = itv1.next();
                while (itv0.hasNext()) {
                    int val0 = itv0.next();
                    if (relation.isConsistent(val0, val1)) {
                        support = val0;
                        found = true;
                        break;
                    }
                }
                itv0.dispose();
                if (!found) {
                    if (val1 == right + 1) {
                        right = val1;
                    } else {
                        v1.removeInterval(left, right, this);
                        left = right = val1;
                    }
                } else {
                    storeSupportV1(support, val1);
                }
                found = false;
            }
            v1.removeInterval(left, right, this);
        } finally {
            itv1.dispose();
        }
        //propagate();
    }

    public void onInstantiationOf(int idx) throws ContradictionException {
        int left, right;
        if (idx == 0) {
            int value = v0.getValue();
            DisposableValueIterator iterator = v1.getValueIterator(true);
            left = right = Integer.MIN_VALUE;
            try {
                while (iterator.hasNext()) {
                    int val = iterator.next();
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
                iterator.dispose();
            }
        } else {
            int value = v1.getValue();
            DisposableValueIterator iterator = v0.getValueIterator(true);
            left = right = Integer.MIN_VALUE;
            try {
                while (iterator.hasNext()) {
                    int val = iterator.next();
                    if (!relation.isConsistent(val, value)) {
                        if (val == right + 1) {
                            right = val;
                        } else if (val > right + 1) {
                            v0.removeInterval(left, right, this);
                            left = right = val;
                        }
                    }
                }
                v0.removeInterval(left, right, this);
            } finally {
                iterator.dispose();
            }
        }
    }

}
