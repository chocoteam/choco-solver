/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.BitSet;

import static org.chocosolver.solver.constraints.PropagatorPriority.QUADRATIC;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * Performs Bound Consistency in O(n+d) with
 * n = |vars|
 * d = maxValue - minValue (from initial domains)
 * <p/>
 * => very appropriate when d <= n It is indeed much better than the usual time complexity of O(n.log(n))
 * =>  not appropriate when d >> n (you should encode another data structure and a quick sort algorithm)
 * <p/>
 * <p/>
 * !redundant propagator!
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNValues_BC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int nbMaxValues;
    private int minValue;
    private int minIndex, maxIndex;
    private TIntArrayList[] bound;
    private TIntArrayList stamp;
    private int[] minVal, maxVal;
    private BitSet kerRepresentant;
    private int[] orderedNodes;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The number of distinct values in vars is at most nValues
     * Performs Bound Consistency in O(n+d) with
     * n = |vars|
     * d = maxValue - minValue (from initial domains)
     * <p/>
     * => very appropriate when d <= n It is indeed much better than the usual time complexity of O(n.log(n))
     * =>  not appropriate when d >> n (you should encode another data structure and a quick sort algorithm)
     *
     * @param variables array of integer variables
     * @param nValues integer variable
     */
    public PropAtMostNValues_BC(IntVar[] variables, IntVar nValues) {
        super(concat(variables, nValues), QUADRATIC, false);
        n = variables.length;
        minValue = vars[0].getLB();
        int maxValue = vars[0].getUB();
        for (int i = 1; i < n; i++) {
            minValue = Math.min(minValue, vars[i].getLB());
            maxValue = Math.max(maxValue, vars[i].getUB());
        }
        nbMaxValues = maxValue - minValue + 1;
        bound = new TIntArrayList[nbMaxValues];
        for (int i = 0; i < nbMaxValues; i++) {
            bound[i] = new TIntArrayList();
        }
        minVal = new int[n];
        maxVal = new int[n];
        stamp = new TIntArrayList();
        kerRepresentant = new BitSet(n);
        orderedNodes = new int[n];
    }

    //***********************************************************************************
    // Initialization and sort
    //***********************************************************************************

    private void computeBounds() throws ContradictionException {
        minIndex = vars[0].getLB();
        maxIndex = vars[0].getUB();
        for (int i = 0; i < n; i++) {
            minVal[i] = vars[i].getLB();
            maxVal[i] = vars[i].getUB();
            minIndex = Math.min(minIndex, minVal[i]);
            maxIndex = Math.max(maxIndex, maxVal[i]);
        }
        minIndex -= minValue;
        maxIndex -= minValue;
    }

    private void sortLB() {
        for (int i = 0; i < nbMaxValues; i++) {
            bound[i].clear();
        }
        for (int i = 0; i < n; i++) {
            bound[minVal[i] - minValue].add(i);
        }
    }

    private void sortUB() {
        for (int i = 0; i < nbMaxValues; i++) {
            bound[i].clear();
        }
        for (int i = 0; i < n; i++) {
            bound[maxVal[i] - minValue].add(i);
        }
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    private boolean pruneLB() throws ContradictionException {
        int node;
        int min = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        int nbKer = 0;
        int index = 0;
        kerRepresentant.clear();
        for (int i = minIndex; i <= maxIndex; i++) {
            for (int k = bound[i].size() - 1; k >= 0; k--) {
                node = bound[i].get(k);
                orderedNodes[index++] = node;
                if (min == Integer.MIN_VALUE) {
                    min = minVal[node];
                    max = maxVal[node];
                    nbKer++;
                } else if (minVal[node] <= max) {
                    min = Math.max(min, minVal[node]);
                    max = Math.min(max, maxVal[node]);
                } else {
                    min = minVal[node];
                    max = maxVal[node];
                    kerRepresentant.set(node);
                    nbKer++;
                }
            }
        }
        boolean hasChanged = vars[n].updateLowerBound(nbKer, this);
        if (nbKer == vars[n].getUB()) {
            stamp.clear();
            for (int i = 0; i < n; i++) {
                node = orderedNodes[i];
                if (kerRepresentant.get(node)) {
                    hasChanged |= updateKer(minVal[node], true);
                    stamp.clear();
                }
                stamp.add(node);
            }
            hasChanged |= updateKer(Integer.MAX_VALUE, true);
        }
        return hasChanged;
    }

    private boolean pruneUB() throws ContradictionException {
        int node;
        int min = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        int nbKer = 0;
        kerRepresentant.clear();
        int index = 0;
        for (int i = maxIndex; i >= minIndex; i--) {
            for (int k = bound[i].size() - 1; k >= 0; k--) {
                node = bound[i].get(k);
                orderedNodes[index++] = node;
                if (min == Integer.MIN_VALUE) {
                    min = minVal[node];
                    max = maxVal[node];
                    nbKer++;
                } else if (maxVal[node] >= min) {
                    max = Math.min(max, maxVal[node]);
                    min = Math.max(min, minVal[node]);
                } else {
                    min = minVal[node];
                    max = maxVal[node];
                    kerRepresentant.set(node);
                    nbKer++;
                }
            }
        }
        boolean hasChanged = vars[n].updateLowerBound(nbKer, this);
        if (nbKer == vars[n].getUB()) {
            stamp.clear();
            for (int i = 0; i < n; i++) {
                node = orderedNodes[i];
                if (kerRepresentant.get(node)) {
                    hasChanged |= updateKer(maxVal[node], false);
                    stamp.clear();
                }
                stamp.add(node);
            }
            hasChanged |= updateKer(Integer.MIN_VALUE, false);
        }
        return hasChanged;
    }

    private boolean updateKer(int newVal, boolean LB) throws ContradictionException {
        boolean hasChanged = false;
        if (LB) {
            int min = Integer.MIN_VALUE;
            for (int i = stamp.size() - 1; i >= 0; i--) {
                if (vars[stamp.get(i)].getUB() < newVal)
                    min = Math.max(min, vars[stamp.get(i)].getLB());
            }
            for (int i = stamp.size() - 1; i >= 0; i--) {
                if (vars[stamp.get(i)].getUB() < newVal)
                    hasChanged |= vars[stamp.get(i)].updateLowerBound(min, this);
            }
        } else {
            int max = Integer.MAX_VALUE;
            for (int i = stamp.size() - 1; i >= 0; i--) {
                if (vars[stamp.get(i)].getLB() > newVal)
                    max = Math.min(max, vars[stamp.get(i)].getUB());
            }
            for (int i = stamp.size() - 1; i >= 0; i--) {
                if (vars[stamp.get(i)].getLB() > newVal)
                    hasChanged |= vars[stamp.get(i)].updateUpperBound(max, this);
            }
        }
        return hasChanged;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged;
        do {
            computeBounds();
            sortLB();
            hasChanged = pruneLB();
            sortUB();
            hasChanged |= pruneUB();
        } while (hasChanged);

    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public ESat isEntailed() {
        BitSet values = new BitSet(nbMaxValues);
        BitSet mandatoryValues = new BitSet(nbMaxValues);
        IntVar v;
        int ub;
        int minVal = 0;
        for (int i = 0; i < n; i++) {
            if (minVal > vars[i].getLB()) {
                minVal = vars[i].getLB();
            }
        }
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.isInstantiated()) {
                mandatoryValues.set(ub - minVal);
            }
            for (int j = v.getLB(); j <= ub; j++) {
                values.set(j - minVal);
            }
        }
        if (values.cardinality() <= vars[n].getLB()) {
            return ESat.TRUE;
        }
        if (mandatoryValues.cardinality() > vars[n].getUB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

}
