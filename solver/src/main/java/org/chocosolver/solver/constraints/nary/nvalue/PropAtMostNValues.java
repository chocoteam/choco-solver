/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.chocosolver.solver.constraints.PropagatorPriority.QUADRATIC;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * No level of consistency but better than BC in general (for enumerated domains with holes)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNValues extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int[] concernedValues;
    private final int n;
    private final int[] unusedValues;
    private final int[] mate;
    private boolean allEnum; // all variables are enumerated
    private final int[] instVals; // for K1
    private final TIntArrayList dVar;
    private int minVal, nbInst;
    private final BitSet valSet;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for the NValues constraint
     * The number of distinct values among concerned values in the set of variables vars is exactly equal to nValues
     * No level of consistency for the filtering
     *
     * @param variables array of variables
     * @param concernedValues will be sorted!
     * @param nValues integer variable
     */
    public PropAtMostNValues(IntVar[] variables, int[] concernedValues, IntVar nValues) {
        super(concat(variables, nValues), QUADRATIC, false);
        n = variables.length;
        this.concernedValues = concernedValues;
        unusedValues = new int[concernedValues.length];
        mate = new int[concernedValues.length];
        allEnum = true;
        minVal = Integer.MAX_VALUE / 10;
        int maxVal = -minVal;
        for (int i = 0; i < n; i++) {
            allEnum &= vars[i].hasEnumeratedDomain();
            minVal = Math.min(minVal, vars[i].getLB());
            maxVal = Math.max(maxVal, vars[i].getUB());
        }
        valSet = new BitSet(maxVal - minVal + 1);
        instVals = new int[n];
        dVar = new TIntArrayList();
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[n].updateLowerBound(1, this);
        int count = 0;
        int countMax = 0;
        int idx = 0;
        nbInst = 0;
        for (int i = concernedValues.length - 1; i >= 0; i--) {
            boolean possible = false;
            boolean mandatory = false;
            mate[i] = -1;
            int value = concernedValues[i];
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(value)) {
                    possible = true;
                    if (mate[i] == -1) {
                        mate[i] = v;
                    } else {
                        mate[i] = -2;
                        if (mandatory) {
                            break;
                        }
                    }
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        if (mate[i] == -2) {
                            break;
                        }
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                instVals[count] = value;
                count++;
            } else {
                unusedValues[idx++] = value;
            }
        }
        nbInst = count;
        // filtering cardinality variable
        vars[n].updateLowerBound(count, this);
        // filtering decision variables
        if (count != countMax && vars[n].isInstantiated())
            if (count == vars[n].getUB()) {
                int val;
                for (int i = 0; i < idx; i++) {
                    val = unusedValues[i];
                    for (int v = 0; v < n; v++) {
                        vars[v].removeValue(val, this);
                    }
                }
                for (int i = idx - 1; i >= 0; i--) {
                    val = unusedValues[i];
                    for (int v = 0; v < n; v++) {
                        vars[v].removeValue(val, this);
                    }
                }
                if (allEnum) setPassive();
            } else if (count == vars[n].getUB() - 1) {
                filterK1Rule();
            }
    }

    //***********************************************************************************
    // K1 (from Hadrien Cambazard)
    //***********************************************************************************

    private void filterK1Rule() throws ContradictionException {
        dVar.clear();
        for (int i = 0; i < n; i++) {
            if (!vars[i].isInstantiated()) {
                if (emptyIntersectionWith(vars[i], instVals)) {
                    dVar.add(i);
                }
            }
        }
        if (!dVar.isEmpty()) {
            intersectionDomains();
            for (int i = 0; i < nbInst; i++) {
                valSet.set(instVals[i] - minVal);
            }
            for (int i = 0; i < n; i++) {
                if (!vars[i].isInstantiated()) {
                    restrict(vars[i]);
                }
            }
        }
    }

    private boolean emptyIntersectionWith(final IntVar v, int[] valueSet) {
        for (int val : valueSet) {
            if (v.contains(val)) {
                return false;
            }
        }
        return true;
    }

    private void intersectionDomains() {
        final List<Integer> inter = new LinkedList<>();
        IntVar v = vars[dVar.get(0)];
        for (int val = v.getLB(); val <= v.getUB(); val = v.nextValue(val)) {
            inter.add(val);
        }

        for (int i = 0; i < dVar.size(); i++) {
            final int next = dVar.get(i);
            v = vars[next];
            for (final Iterator it = inter.iterator(); it.hasNext(); ) {
                if (!v.contains((Integer) it.next())) {
                    it.remove();
                }
            }
        }
        valSet.clear();
        for (final Integer i : inter) {
            valSet.set(i - minVal);
        }
    }

    private void restrict(final IntVar v) throws ContradictionException {
        if (v.hasEnumeratedDomain()) {
            for (int val = v.getLB(); val <= v.getUB(); val = v.nextValue(val)) {
                if (!valSet.get(val - minVal)) {
                    v.removeValue(val, this);
                }
            }
        } else {
            int lb = v.getLB();
            int ub = v.getLB();
            for (int val = v.getLB(); val <= ub; val = v.nextValue(val)) {
                if (!valSet.get(val - minVal)) {
                    lb = val + 1;
                } else {
                    break;
                }
            }
            for (int val = v.getUB(); val >= lb; val = v.previousValue(val)) {
                if (!valSet.get(val - minVal)) {
                    ub = val - 1;
                } else {
                    break;
                }
            }
            v.updateBounds(lb, ub, this);
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        int countMin = 0;
        int countMax = 0;
        for (int i = 0; i < concernedValues.length; i++) {
            boolean possible = false;
            boolean mandatory = false;
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(concernedValues[i])) {
                    possible = true;
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        break;
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                countMin++;
            }
        }
        if (countMin > vars[n].getUB()) {
            return ESat.FALSE;
        }
        if (countMax <= vars[n].getLB()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
