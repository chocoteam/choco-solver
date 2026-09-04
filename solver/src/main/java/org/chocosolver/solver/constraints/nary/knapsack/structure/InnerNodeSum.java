/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Inner node implementation that stores the sum of weights and profits of its subtree.
 * <p>
 * This class is used in the {@link ComputingLossWeightTree} to maintain cumulative
 * weight and profit information for subsets of items, enabling efficient computation
 * of knapsack bounds.
 * <p>
 * The node accumulates values from its children, allowing O(1) access to the total
 * weight and profit of any subtree.
 *
 * @author Nicolas PIERRE
 */
public class InnerNodeSum implements InnerNode, ProfitInterface {
    
    /**
     * The sum of weights of all items in this subtree.
     */
    private int sumWeight;
    
    /**
     * The sum of profits of all items in this subtree.
     */
    private int sumProfit;

    /**
     * Constructs a new inner node with zero initial values.
     */
    public InnerNodeSum() {
        setup();
    }

    /**
     * Resets this node's values to zero.
     */
    public void setup() {
        sumWeight = 0;
        sumProfit = 0;
    }

    /**
     * Updates this node's values by adding the weight and profit of an item.
     * Only active items contribute to the sums.
     *
     * @param item the item whose values should be added
     */
    public void updateValue(KPItem item) {
        if (item.isActive()) {
            sumWeight += item.getWeight();
            sumProfit += item.getProfit();
        }
    }

    /**
     * Returns the total weight of all items in this subtree.
     *
     * @return the sum of weights
     */
    public int getWeight() {
        return sumWeight;
    }

    /**
     * Returns the total profit of all items in this subtree.
     *
     * @return the sum of profits
     */
    public int getProfit() {
        return sumProfit;
    }

    /**
     * Updates this node's values by adding the weight and profit of another node.
     * This method only works with other InnerNodeSum instances.
     *
     * @param node the node whose values should be added
     * @throws RuntimeException if the node is not an InnerNodeSum
     */
    public void updateValue(InnerNode node) {
        try {
            InnerNodeSum nodeSum = (InnerNodeSum) node;
            sumWeight += nodeSum.getWeight();
            sumProfit += nodeSum.getProfit();
        } catch (Exception e) {
            throw new RuntimeException("updateValue of InnerNode used with another type ");
        }
    }

    /**
     * Checks if this node is currently active (has non-zero weight and profit).
     *
     * @return true if this node has non-zero values, false otherwise
     */
    public boolean isActive() {
        return !(sumProfit == 0 && sumWeight == 0);
    }

    /**
     * Returns a string representation of this node.
     *
     * @return a string in the format "w=weight,p=profit"
     */
    public String toString() {
        return "w=" + sumWeight + ",p=" + sumProfit;
    }

}
