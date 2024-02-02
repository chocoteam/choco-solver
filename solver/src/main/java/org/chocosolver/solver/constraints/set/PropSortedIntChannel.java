/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Channeling between a set variable and integer variables such that:
 * <p>
 * intvars[i] = v iff the ith element of the sorted set elements is v.
 *
 * @author Dimitri Justeau-Allaire
 */
public class PropSortedIntChannel extends Propagator<Variable> {

    private final SetVar set;
    private final IntVar[] ints;
    private final int nullValue;
    private final int offset;

    public PropSortedIntChannel(SetVar set, IntVar[] ints, final int nullValue, final int offset) {
        super(ArrayUtils.append(new Variable[]{set}, ints), PropagatorPriority.LINEAR, false);
        this.set = set;
        this.ints = ints;
        this.nullValue = nullValue;
        this.offset = offset;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Instantiate ints that are positioned out of the set to nullValue.
        for (int i = set.getUB().size(); i < ints.length; i++) {
            ints[i].instantiateTo(nullValue, this);
        }
        int[] sortedLB = set.getLB().toArray();
        Arrays.sort(sortedLB);
        // Instantiate ints that are positioned within the kernel to the sorted kernel value.
        for (int i = 0; i < sortedLB.length && i < ints.length; i++) {
            ints[i].instantiateTo(sortedLB[i] + offset, this);
        }
        if (sortedLB.length < set.getUB().size()) {
            // Filter possible values of ints positioned out of the kernel and within the envelope.
            for (int i = sortedLB.length; i < set.getUB().size() && i < ints.length; i++) {
                for (int k = 0; k < sortedLB.length; k++) {
                    if (sortedLB[k] != nullValue) {
                        ints[i].removeValue(sortedLB[k], this);
                    }
                }
            }
        }
    }


    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] sortedValues = set.getValue().toArray();
            Arrays.sort(sortedValues);
            for (int i = 0; i < ints.length; i++) {
                if (i < sortedValues.length && ints[i].getValue() != sortedValues[i] + offset) {
                    return ESat.FALSE;
                } else {
                    if (i >= sortedValues.length && ints[i].getValue() != nullValue) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
