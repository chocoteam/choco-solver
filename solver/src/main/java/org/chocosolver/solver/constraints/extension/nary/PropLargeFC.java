/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class PropLargeFC extends PropLargeCSP<LargeRelation> {

    private final int[] currentTuple;
    private final IntIterableBitSet vrms;

    private PropLargeFC(IntVar[] vars, LargeRelation relation) {
        super(vars, relation);
        this.currentTuple = new int[vars.length];
        vrms = new IntIterableBitSet();
    }

    public PropLargeFC(IntVar[] vars, Tuples tuples) {
        this(vars, RelationFactory.makeLargeRelation(tuples, vars));
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(PropagatorEventType.FULL_PROPAGATION);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CSPLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i]).append(", ");
        }
        sb.append("})");
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void filter() throws ContradictionException {
        boolean stop = false;
        int nbUnassigned = 0;
        int index = -1, i = 0;
        while (!stop && i < vars.length) {
            if (!vars[i].isInstantiated()) {
                nbUnassigned++;
                index = i;
            } else {
                currentTuple[i] = vars[i].getValue();
            }
            if (nbUnassigned > 1) {
                stop = true;
            }
            i++;
        }
        if (!stop) {
            if (nbUnassigned == 1) {
                vrms.clear();
                vrms.setOffset(vars[index].getLB());

                int ub = vars[index].getUB();
                for (int val = vars[index].getLB(); val <= ub; val = vars[index].nextValue(val)) {
                    currentTuple[index] = val;
                    if (!relation.isConsistent(currentTuple)) {
                        vrms.add(val);
                    }
                }
                vars[index].removeValues(vrms, this);
            } else {
                if (!relation.isConsistent(currentTuple)) {
                    fails();
                }
            }
        }
    }
}
