/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Represents an item in the Knapsack Problem.
 * <p>
 * Each item has a weight and a profit value. Items can be activated or deactivated
 * to reflect whether they are currently part of the problem being solved.
 * When deactivated, an item's weight and profit are treated as zero in calculations.
 * <p>
 * This class implements both {@link WeightInterface} and {@link ProfitInterface} to allow
 * uniform treatment of items and tree nodes in the knapsack algorithms.
 *
 * @author Nicolas PIERRE
 */
public class KPItem implements WeightInterface, ProfitInterface {

    /**
     * The profit value of this item (immutable).
     */
    private final int profit;
    
    /**
     * The weight value of this item (can be modified).
     */
    private int weight;
    
    /**
     * Indicates whether this item is currently active in the problem.
     * When inactive, the item is treated as having zero weight and profit.
     */
    private boolean active;

    /**
     * Constructs a new KPItem with the specified profit and weight.
     *
     * @param profit the profit value of this item
     * @param weight the weight value of this item
     */
    public KPItem(int profit, int weight) {
        this.profit = profit;
        this.weight = weight;
        this.active = true;
    }

    /**
     * Deactivates this item, making it behave as having zero weight and profit.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Activates this item, restoring its original weight and profit values.
     */
    public void activate() {
        active = true;
    }

    /**
     * Returns the profit of this item if active, zero otherwise.
     *
     * @return the profit value if active, 0 otherwise
     */
    public int getProfit() {
        return active ? profit : 0;
    }

    /**
     * Returns the original weight of this item, regardless of its active state.
     * This is used when the item is added to or removed from the solution.
     *
     * @return the weight value of this item
     */
    public int getActivatedWeight() {
        return weight;
    }

    /**
     * Returns the original profit of this item, regardless of its active state.
     * This is used when the item is added to or removed from the solution.
     *
     * @return the profit value of this item
     */
    public int getActivatedProfit() {
        return profit;
    }

    /**
     * Returns the weight of this item if active, zero otherwise.
     *
     * @return the weight value if active, 0 otherwise
     */
    public int getWeight() {
        return active ? weight : 0;
    }

    /**
     * Checks if this item is currently active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the weight of this item.
     *
     * @param weight the new weight value
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Computes and returns the efficiency (profit/weight ratio) of this item.
     *
     * @return the efficiency ratio if active and weight > 0, 0 otherwise
     */
    public double getEfficiency() {
        return active ? (double) getProfit() / getWeight() : 0;
    }

}