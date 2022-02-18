/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

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
public class PropKnapsack extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int[] weigth;
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
        this.weigth = weight;
        this.energy = energy;
        this.n = itemOccurence.length;
        this.capacity = vars[n];
        this.power = vars[n + 1];
        this.ratio = new double[n];
        for (int i = 0; i < n; i++) {
            ratio[i] = weight[i] == 0?Double.MAX_VALUE : ((double) (energy[i]) / (double) (weight[i]));
        }
        this.order = ArrayUtils.array(0,n-1);
        ArraySort sorter = new ArraySort(n,false,true);
        sorter.sort(order, n, (i1, i2) -> Double.compare(ratio[i2],ratio[i1]));
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
            remainingCapacity -= weigth[i] * lb;
            maxPower += energy[i] * lb;
        }
        power.updateLowerBound(maxPower, this);
        if (remainingCapacity < 0) {
            power.updateUpperBound(power.getLB() - 1, this); // fails
        } else {
            int idx;
            for (int i = 0; i < n; i++) {
                assert remainingCapacity >= 0;
                idx = order[i];
                int range = vars[idx].getUB() - vars[idx].getLB();
                if (range > 0) {
					int delta = weigth[idx] * (range);
                    if (delta <= remainingCapacity) {
                        maxPower += energy[idx] * (range);
                        remainingCapacity -= delta;
                        if (weigth[idx] > 0 && remainingCapacity == 0) {
                            power.updateUpperBound(maxPower, this);
                            return;
                        }
                    } else {
                        int deltaPow = (int) Math.ceil((double)remainingCapacity * ratio[idx]);
                        power.updateUpperBound(maxPower + deltaPow, this);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        double camax = capacity.getUB();
        double pomin = 0;
        for (int i = 0; i < n; i++) {
            camax -= (long)weigth[i] * vars[i].getLB(); // potential overflow
            pomin += (long)energy[i] * vars[i].getLB(); // potential overflow
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
