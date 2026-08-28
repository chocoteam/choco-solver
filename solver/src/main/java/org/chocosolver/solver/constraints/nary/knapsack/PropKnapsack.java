/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
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
 * Propagator for the 0/1-Knapsack constraint using Dantzig-Wolfe relaxation.
 * <p>
 * This propagator enforces the knapsack constraint by:
 * <ol>
 *   <li>Computing the minimum possible weight from the lower bounds of item occurrences.</li>
 *   <li>Updating the total profit lower bound based on the minimum weight configuration.</li>
 *   <li>Using efficiency-based filtering (profit/weight ratio) to prune the search space:</n *       <ul>
 *         <li>If adding all remaining items (sorted by decreasing efficiency) exceeds capacity,
 *             it computes the maximum achievable profit and updates the total profit upper bound.</li>
 *         <li>If capacity is exhausted, it fails if the profit constraint cannot be satisfied.</li>
 *       </ul>
 *   </li>
 * </ol>
 * <p>
 * <b>Note:</b> This propagator assumes that linear constraints maintaining the consistency between
 * item occurrences, total weight, and total profit are also posted. Specifically, the following must hold:
 * <ul>
 *   <li>sum(weight[i] * itemOccurrence[i]) = totalWeight</li>
 *   <li>sum(profit[i] * itemOccurrence[i]) = totalProfit</li>
 * </ul>
 * Without these constraints, the propagator may produce incorrect filtering results.
 * <p>
 * This propagator has linear time complexity per propagation call.
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

    /**
     * Propagates the knapsack constraint using Dantzig-Wolfe relaxation-based filtering.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li><b>Initial computation:</b> Computes the remaining capacity after accounting for
     *       items at their lower bounds, and calculates the minimum achievable power (profit).</li>
     *   <li><b>Lower bound update:</b> Updates the total profit lower bound if the computed
     *       minimum power from lower bounds exceeds the current lower bound.</li>
     *   <li><b>Feasibility check:</b> Fails if the remaining capacity is negative (i.e., the
     *       minimum weight configuration exceeds the capacity).</li>
     *   <li><b>Efficiency-based filtering:</b> Iterates items by decreasing efficiency ratio
     *       (profit/weight) to compute the maximum achievable profit:
     *       <ul>
     *         <li>If all of an item can fit in the remaining capacity, add its full profit contribution.</li>
     *         <li>If capacity is exhausted, update the total profit upper bound and return.</li>
     *         <li>If only part of an item can fit, compute the maximum profit achievable with
     *             the remaining capacity using the efficiency ratio, update the upper bound, and return.</li>
     *       </ul>
     *   </li>
     * </ol>
     * <p>
     * The filtering is based on the observation that items sorted by decreasing efficiency
     * ratio provide an optimal way to maximize profit within a given capacity.
     *
     * @param evtmask the event mask that triggered the propagation
     * @throws ContradictionException if a contradiction is detected (capacity exceeded or profit bounds inconsistent)
     */
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Step 1: Initial computation
        // Compute remaining capacity after placing all items at their lower bounds
        // and calculate the minimum power (profit) achievable from the lower bound configuration
        int remainingCapacity = capacity.getUB();
        int maxPower = 0;
        for (int i = 0; i < n; i++) {
            int lb = vars[i].getLB();
            remainingCapacity -= weight[i] * lb;
            maxPower += energy[i] * lb;
        }
        
        // Step 2: Lower bound update
        // Update the total profit lower bound if the minimum power from lower bounds is greater
        if (power.getLB() < maxPower) {
            power.updateLowerBound(maxPower, this, lcg() ? this.lbounds(power, vars) : Reason.undef());
        }
        
        // Step 3: Feasibility check
        // Fail if the remaining capacity is negative (minimum weight exceeds capacity)
        if (remainingCapacity < 0) {
            this.fails(lcg() ? this.lbounds(power, vars) : Reason.undef());
        } else {
            // Step 4: Efficiency-based filtering
            // Iterate items by decreasing efficiency ratio (profit/weight) to maximize profit
            int idx;
            for (int i = 0; i < n; i++) {
                assert remainingCapacity >= 0;
                idx = order[i];
                
                // Get the range of possible additional occurrences for this item
                int range = vars[idx].getUB() - vars[idx].getLB();
                if (range > 0) {
                    // Compute the weight delta if we add all remaining occurrences of this item
                    int delta = weight[idx] * (range);
                    
                    // Case 1: All of this item can fit in the remaining capacity
                    if (delta <= remainingCapacity) {
                        // Add full profit contribution from this item
                        maxPower += energy[idx] * (range);
                        remainingCapacity -= delta;
                        
                        // Special case: capacity is now exhausted
                        // Update upper bound since no more items can be added
                        if (weight[idx] > 0 && remainingCapacity == 0) {
                            if (power.getUB() > maxPower) {
                                power.updateUpperBound(maxPower, this, explain(i));
                            }
                            return;
                        }
                    } else {
                        // Case 2: Only part of this item can fit
                        // Compute maximum profit achievable with remaining capacity
                        // using the efficiency ratio (profit per unit weight)
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

    /**
     * Generates an explanation for the reason of a bound update or failure.
     * <p>
     * This method constructs a reason object that explains why the profit upper bound was updated
     * or why the propagator failed. The explanation is based on the literals representing:
     * <ul>
     *   <li>The minimum occurrence of each item (vars[j].getMinLit())</li>
     *   <li>The maximum occurrence of items up to index i in the efficiency order (vars[order[j]].getMaxLit())</li>
     * </ul>
     * This captures the constraint that the current filtering decision is valid given the
     * lower bounds of all items and the upper bounds of the items considered so far.
     *
     * @param i the index up to which items have been considered in the efficiency-based filtering
     * @return a Reason object explaining the bound update or failure, or Reason.undef() if
     *         learning clause generation (lcg) is not enabled
     */
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
            r = this.r(lits);
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
