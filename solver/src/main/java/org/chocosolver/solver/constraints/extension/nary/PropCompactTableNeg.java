/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.tools.VariableUtils;

/**
 * Propagator for table constraint based on
 * "Extending Compact-Table to Negative and Short Tables",
 * H. Verhaeghe and C. Lecoutre and P. Schauss, AAAI-17.
 * It deals with negative tuples.
 *
 * @author Charles Prud'homme
 * @since 05/05/2025
 */
@Explained(ignored = true, comment = "Turned into clauses")
public final class PropCompactTableNeg extends PropCompactTable {

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create a propagator for table constraint Only for infeasible Tuples
     *
     * @param vars   scope
     * @param tuples list of feasible tuples
     */
    public PropCompactTableNeg(IntVar[] vars, Tuples tuples) {
        super(vars, tuples);
        assert !tuples.isFeasible();
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int vIdx, int mask) throws ContradictionException {
        currTable.clearMask();
        if (vars[vIdx].isInstantiated()) {
            currTable.addToMask(supports[vIdx].get(vars[vIdx].getValue() - offset[vIdx]));
        } else if (vars[vIdx].getDomainSize() > monitors[vIdx].sizeApproximation()) {
            monitors[vIdx].forEachRemVal(onValRem.set(vIdx));
            currTable.reverseMask();
        } else {
            int ub = vars[vIdx].getUB();
            for (int v = vars[vIdx].getLB(); v <= ub; v = vars[vIdx].nextValue(v)) {
                currTable.addToMask(supports[vIdx].get(v - offset[vIdx]));
            }
        }
        currTable.intersectWithMask();
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }


    @Override
    protected void filterDomains() throws ContradictionException {
        long count = currTable.nb1s();
        if (count == 0) { // Invariant 8.4
            setPassive();
        } else {
            // check for views
            long initThreshold = VariableUtils.domainCardinality(vars);
            if (invariant && count == initThreshold) { // Invariant 8.4
                fails();
            }
            currTable.clearMask();
            for (int i = 0; i < vars.length; i++) {
                if (uniqueness && vars[i].isInstantiated()) continue;
                if (vars[i].hasEnumeratedDomain()) {
                    enumFilter(i, initThreshold);
                } else {
                    boundFilter(i, initThreshold);
                }
            }
            currTable.intersectWithNotMask();
        }
    }

    private void enumFilter(int i, long initThreshold) throws ContradictionException {
        int ub = vars[i].getUB();
        int domSize = vars[i].getDomainSize();
        long threshold = initThreshold / domSize;
        for (int v = vars[i].getLB(); v <= ub; v = vars[i].nextValue(v)) {
            long nb1s = currTable.nb1s(supports[i].get(v - offset[i]));
            if (nb1s == threshold) {
                vars[i].removeValue(v, this);
                currTable.addToMask(supports[i].get(v - offset[i]));
                domSize--;
                threshold = initThreshold / domSize;
            }
        }
    }

    private void boundFilter(int i, long initThreshold) throws ContradictionException {
        int lb = vars[i].getLB();
        int ub = vars[i].getUB();
        int domSize = vars[i].getDomainSize();
        long threshold = initThreshold / domSize;
        for (int v = lb; v <= ub; v++) {
            long nb1s = currTable.nb1s(supports[i].get(v - offset[i]));
            if (nb1s == threshold) {
                lb++;
                currTable.addToMask(supports[i].get(v - offset[i]));
                domSize--;
                threshold = initThreshold / domSize;
            } else {
                break;
            }
        }
        vars[i].updateLowerBound(lb, this);
        for (int v = ub; v >= lb; v--) {
            long nb1s = currTable.nb1s(supports[i].get(v - offset[i]));
            if (nb1s == threshold) {
                ub--;
                currTable.addToMask(supports[i].get(v - offset[i]));
                domSize--;
                threshold = initThreshold / domSize;
            } else {
                break;
            }
        }
        vars[i].updateUpperBound(ub, this);
    }

}