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
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/2014
 */
public class PropBinAC3 extends PropBinCSP {

    private final IntIterableBitSet vrms;

    public PropBinAC3(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
    }

    private PropBinAC3(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
        vrms = new IntIterableBitSet();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        reviseV0();
        reviseV1();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            reviseV1();
        } else
            reviseV0();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * updates the support for all values in the domain of v1, and remove unsupported values for v1
     */
    private void reviseV1() throws ContradictionException {
        int nbs = 0;
        vrms.clear();
        vrms.setOffset(v1.getLB());
        DisposableValueIterator itv1 = v1.getValueIterator(true);
        while (itv1.hasNext()) {
            int val1 = itv1.next();
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            while (itv0.hasNext()) {
                int val0 = itv0.next();
                if (relation.isConsistent(val0, val1)) {
                    nbs += 1;
                    break;
                }
            }
            itv0.dispose();
            if (nbs == 0) {
                vrms.add(val1);
            }
            nbs = 0;
        }
        v1.removeValues(vrms, this);
        itv1.dispose();
    }

    /**
     * updates the support for all values in the domain of v0, and remove unsupported values for v0
     */
    private void reviseV0() throws ContradictionException {
        int nbs = 0;
        vrms.clear();
        vrms.setOffset(v0.getLB());
        DisposableValueIterator itv0 = v0.getValueIterator(true);
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    nbs += 1;
                    break;
                }
            }
            itv1.dispose();
            if (nbs == 0) {
                vrms.add(val0);
            }
            nbs = 0;
        }
        v0.removeValues(vrms, this);
        itv0.dispose();
    }
}
