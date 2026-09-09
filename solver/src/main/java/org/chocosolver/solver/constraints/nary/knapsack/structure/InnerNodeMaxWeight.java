/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Inner node implementation that stores the maximum weight in its subtree.
 * <p>
 * This class is used in the {@link ItemFindingSearchTree} to efficiently find
 * items with sufficient weight during the filtering process.
 * The node maintains the maximum weight among all items in its subtree,
 * allowing O(1) access to this information.
 *
 * @author Nicolas PIERRE
 */
public class InnerNodeMaxWeight implements InnerNode {
    
    /**
     * The maximum weight among all items in this subtree.
     * Initialized to -1 to indicate an empty/inactive state.
     */
    private int maxWeight;

    /**
     * Constructs a new inner node with initial maximum weight set to -1.
     */
    public InnerNodeMaxWeight() {
        setup();
    }

    /**
     * Resets this node's maximum weight to -1 (inactive state).
     */
    public void setup() {
        this.maxWeight = -1;
    }

    /**
     * Updates this node's maximum weight by considering an item's weight.
     * Only active items are considered.
     *
     * @param item the item whose weight should be considered
     */
    public void updateValue(KPItem item) {
        if (item.isActive()) {
            this.maxWeight = Math.max(item.getWeight(), maxWeight);
        }
    }

    /**
     * Returns the maximum weight among all items in this subtree.
     *
     * @return the maximum weight, or -1 if no items are present
     */
    public int getWeight() {
        return maxWeight;
    }

    /**
     * Updates this node's maximum weight by considering another node's maximum weight.
     * This method only works with other InnerNodeMaxWeight instances.
     *
     * @param node the node whose maximum weight should be considered
     * @throws RuntimeException if the node is not an InnerNodeMaxWeight
     */
    public void updateValue(InnerNode node) {
        try {
            InnerNodeMaxWeight nodeMaxWeight = (InnerNodeMaxWeight) node;
            this.maxWeight = Math.max(nodeMaxWeight.getWeight(), maxWeight);
        } catch (Exception e) {
            throw new RuntimeException("updateValue of InnerNode used with another type ");
        }
    }

    /**
     * Checks if this node is currently active (has a valid maximum weight).
     *
     * @return true if this node has a non-negative maximum weight, false otherwise
     */
    public boolean isActive() {
        return maxWeight != -1;
    }

    /**
     * Returns a string representation of this node.
     *
     * @return a string in the format "w=maxWeight"
     */
    public String toString() {
        return "w=" + maxWeight;
    }

}
