/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
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

    private int offset0;
    private int offset1;

    private int minS0;    //value with minimum number of supports for v0
    private int minS1;    //value with minimum number of supports for v1

    private int initDomSize0;
    private int initDomSize1;

    public PropBinAC3bitrm(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
        if(!x.hasEnumeratedDomain() || !y.hasEnumeratedDomain()) {
            throw new SolverException("PropBinAC3bitrm (\"AC3bit+rm\")may produce incorrect filtering with bounded variables");
        }
        offset0 = v0.getLB();
        offset1 = v1.getLB();

        initDomSize0 = v0.getUB() - offset0 + 1;
        initDomSize1 = v1.getUB() - offset1 + 1;
    }

    private PropBinAC3bitrm(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
        offset0 = v0.getLB();
        offset1 = v1.getLB();

        initDomSize0 = v0.getUB() - offset0 + 1;
        initDomSize1 = v1.getUB() - offset1 + 1;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            fastInitNbSupports();
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val0 = itv0.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(0, val0, v1)) {
                        v0.removeValue(val0, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
            itv0 = v1.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val1 = itv0.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(1, val1, v0)) {
                        v1.removeValue(val1, this);
                    }
                }
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void fastInitNbSupports() {
        int[] initS1 = new int[initDomSize1];
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
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            try {
                while (itv1.hasNext()) {
                    int y = itv1.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(1, y, v0)) {
                        v1.removeValue(y, this);
                    }
                }
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
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int x = itv0.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(0, x, v1)) {
                        v0.removeValue(x, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
        }
    }


    private void onInstantiationOf(int idx) throws ContradictionException {
        if (idx == 0) {
            int value = v0.getValue();
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            try {
                while (itv1.hasNext()) {
                    int val = itv1.next();
                    if (!relation.isConsistent(value, val)) {
                        v1.removeValue(val, this);
                    }
                }
            } finally {
                itv1.dispose();
            }
        } else {
            int value = v1.getValue();
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val = itv0.next();
                    if (!relation.isConsistent(val, value)) {
                        v0.removeValue(val, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
        }
    }

}
