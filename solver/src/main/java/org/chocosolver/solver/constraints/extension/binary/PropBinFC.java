/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * Forward checking algorithm for table constraint
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropBinFC extends PropBinCSP {

    private final IntIterableBitSet vrms;

    public PropBinFC(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesTable(tuples, x, y));
    }

    private PropBinFC(IntVar x, IntVar y, CouplesTable table) {
        super(x, y, table);
        vrms = new IntIterableBitSet();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (v0.isInstantiated())
            onInstantiation0();
        if (v1.isInstantiated())
            onInstantiation1();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) onInstantiation0();
        else onInstantiation1();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void onInstantiation0() throws ContradictionException {
        int value = v0.getValue();
        DisposableValueIterator values = v1.getValueIterator(true);
        vrms.clear();
        vrms.setOffset(v1.getLB());
        try {
            while (values.hasNext()) {
                int val = values.next();
                if (!relation.isConsistent(value, val)) {
                    vrms.add(val);
                }
            }
            v1.removeValues(vrms, this);
        } finally {
            values.dispose();
        }
    }

    private void onInstantiation1() throws ContradictionException {
        int value = v1.getValue();
        DisposableValueIterator values = v0.getValueIterator(true);
        vrms.clear();
        vrms.setOffset(v0.getLB());
        try {
            while (values.hasNext()) {
                int val = values.next();
                if (!relation.isConsistent(val, value)) {
                    vrms.add(val);
                }
            }
            v0.removeValues(vrms, this);
        } finally {
            values.dispose();
        }
    }
}
