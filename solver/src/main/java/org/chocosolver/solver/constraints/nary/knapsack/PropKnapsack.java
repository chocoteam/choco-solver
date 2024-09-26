/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Propagator for the Knapsack constraint
 * based on Dantzig-Wolfe relaxation
 *
 * @author Jean-Guillaume Fages
 */
@Explained
public class PropKnapsack extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int[] weight;
    private final int[] energy;
    private final int[] order;
    private final double[] ratio;
    private final int n;
    private final IntVar capacity;
    private final IntVar power;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKnapsack(IntVar[] itemOccurence, IntVar capacity, IntVar power,
                        int[] weight, int[] energy) {
        super(ArrayUtils.append(itemOccurence, new IntVar[]{capacity, power}), PropagatorPriority.LINEAR, false);
        this.weight = weight;
        this.energy = energy;
        this.n = itemOccurence.length;
        this.capacity = vars[n];
        this.power = vars[n + 1];
        this.ratio = new double[n];
        for (int i = 0; i < n; i++) {
            ratio[i] = weight[i] == 0 ? Double.MAX_VALUE : ((double) (energy[i]) / (double) (weight[i]));
        }
        this.order = ArrayUtils.array(0, n - 1);
        ArraySort<?> sorter = new ArraySort<>(n, false, true);
        sorter.sort(order, n, (i1, i2) -> Double.compare(ratio[i2], ratio[i1]));
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int remainingCapacity = capacity.getUB();
        int maxPower = 0;
        for (int i = 0; i < n; i++) {
            int lb = vars[i].getLB();
            remainingCapacity -= weight[i] * lb;
            maxPower += energy[i] * lb;
        }
        if (power.getLB() < maxPower) {
            power.updateLowerBound(maxPower, this, lcg() ? Propagator.lbounds(power, vars) : Reason.undef());
        }
        if (remainingCapacity < 0) {
            this.fails(lcg() ? Propagator.lbounds(power, vars) : Reason.undef());
        } else {
            int idx;
            for (int i = 0; i < n; i++) {
                assert remainingCapacity >= 0;
                idx = order[i];
                int range = vars[idx].getUB() - vars[idx].getLB();
                if (range > 0) {
                    int delta = weight[idx] * (range);
                    if (delta <= remainingCapacity) {
                        maxPower += energy[idx] * (range);
                        remainingCapacity -= delta;
                        if (weight[idx] > 0 && remainingCapacity == 0) {
                            if (power.getUB() > maxPower) {
                                power.updateUpperBound(maxPower, this, explain(i));
                            }
                            return;
                        }
                    } else {
                        int deltaPow = (int) Math.ceil((double) remainingCapacity * ratio[idx]);
                        if (power.getUB() > maxPower + deltaPow) {
                            power.updateUpperBound(maxPower + deltaPow, this, explain(i));
                        }
                        return;
                    }
                }
            }
        }
    }

    private Reason explain(int i) {
        Reason r = Reason.undef();
        if (lcg()) {
            int[] lits = new int[n + i + 2];
            int m = 1;
            for (int j = 0; j < n; j++) {
                lits[m++] = vars[j].getMinLit();
            }
            for(int j = 0; j <= i; j++){
                lits[m++] = vars[order[j]].getMaxLit();
            }
            r = Reason.r(lits);
        }
        return r;
    }

    @Override
    public ESat isEntailed() {
        double camax = capacity.getUB();
        double pomin = 0;
        for (int i = 0; i < n; i++) {
            camax -= (long) weight[i] * vars[i].getLB(); // potential overflow
            pomin += (long) energy[i] * vars[i].getLB(); // potential overflow
        }
        if (camax < 0 || pomin > power.getUB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            if (pomin == power.getValue()) {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

}
