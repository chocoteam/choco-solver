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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropBinAC3rm extends PropBinCSP {

    private int[] currentSupport0;
    private int[] currentSupport1;

    private int offset0;
    private int offset1;

    private int[] initS0; //initial number of supports of each value of x0
    private int[] initS1; //initial number of supports of each value of x0
    private int minS0;    //value with minimum number of supports for v0
    private int minS1;    //value with minimum number of supports for v1

    private int initDomSize0;
    private int initDomSize1;

    private final IntIterableBitSet vrms;

    public PropBinAC3rm(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
    }

    private PropBinAC3rm(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
        vrms = new IntIterableBitSet();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void fastInitNbSupports(int a, int b) {
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

    private boolean testDeepakConditionV1(int y, int v0Size) {
        return initS1[y - offset1] <= (initDomSize0 - v0Size);
    }

    private boolean testDeepakConditionV0(int x, int v1Size) {
        return initS0[x - offset0] <= (initDomSize1 - v1Size);
    }

    private int getSupportV1(int y) {
        return currentSupport1[y - offset1];
    }

    private int getSupportV0(int x) {
        return currentSupport0[x - offset0];
    }

    /**
     * updates the support for all values in the domain of v1, and remove unsupported values for v1
     *
     * @throws ContradictionException
     */
    private void reviseV1() throws ContradictionException {
        int v0Size = v0.getDomainSize();
        if (minS1 <= (initDomSize0 - v0Size)) {
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            vrms.clear();
            vrms.setOffset(v1.getLB());
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
                                vrms.add(y);
                            }
                        }
                    }
                }
                v1.removeValues(vrms, this);
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
    private void reviseV0() throws ContradictionException {
        int v1Size = v1.getDomainSize();
        if (minS0 <= (initDomSize1 - v1Size)) {
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            vrms.clear();
            vrms.setOffset(v0.getLB());
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
                                vrms.add(x);
                            }
                        }
                    }
                }
                v0.removeValues(vrms, this);
            } finally {
                itv0.dispose();
            }
        }
    }

    private void storeSupportV0(int support, int x) {
        currentSupport0[x - offset0] = support;
        currentSupport1[support - offset1] = x;
    }

    private void storeSupportV1(int support, int y) {
        currentSupport1[y - offset1] = support;
        currentSupport0[support - offset0] = y;
    }

    private void initProp() throws ContradictionException {
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
        vrms.clear();
        vrms.setOffset(v0.getLB());
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
                    vrms.add(val0);
                } else {
                    storeSupportV0(support, val0);
                }
                found = false;
            }
            v0.removeValues(vrms, this);
        } finally {
            itv0.dispose();
        }
        found = false;
        DisposableValueIterator itv1 = v1.getValueIterator(true);
        vrms.clear();
        vrms.setOffset(v1.getLB());
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
                    vrms.add(val1);
                } else {
                    storeSupportV1(support, val1);
                }
                found = false;
            }
            v1.removeValues(vrms, this);
        } finally {
            itv1.dispose();
        }
        //propagate();
    }

    private void onInstantiationOf(int idx) throws ContradictionException {
        if (idx == 0) {
            int value = v0.getValue();
            DisposableValueIterator iterator = v1.getValueIterator(true);
            vrms.clear();
            vrms.setOffset(v1.getLB());
            try {
                while (iterator.hasNext()) {
                    int val = iterator.next();
                    if (!relation.isConsistent(value, val)) {
                        vrms.add(val);
                    }
                }
                v1.removeValues(vrms, this);
            } finally {
                iterator.dispose();
            }
        } else {
            int value = v1.getValue();
            DisposableValueIterator iterator = v0.getValueIterator(true);
            vrms.clear();
            vrms.setOffset(v0.getLB());
            try {
                while (iterator.hasNext()) {
                    int val = iterator.next();
                    if (!relation.isConsistent(val, value)) {
                        vrms.add(val);
                    }
                }
                v0.removeValues(vrms, this);
            } finally {
                iterator.dispose();
            }
        }
    }
}
