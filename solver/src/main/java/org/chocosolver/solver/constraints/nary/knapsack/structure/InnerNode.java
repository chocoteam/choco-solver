/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Interface for inner nodes in the knapsack search trees.
 * <p>
 * Inner nodes are used to build binary trees that store aggregate information
 * (such as total weight and profit) for subsets of items. This allows efficient
 * computation of knapsack bounds and critical items.
 * <p>
 * This interface extends {@link WeightInterface} to provide weight information,
 * and adds methods for setting up and updating node values from child nodes or items.
 *
 * @author Nicolas PIERRE
 */
public interface InnerNode extends WeightInterface {

    /**
     * Initializes this node's value to zero (or appropriate initial state).
     */
    void setup();

    /**
     * Updates this node's value by incorporating the values from a child item.
     *
     * @param item the child item whose values should be incorporated
     */
    void updateValue(KPItem item);

    /**
     * Updates this node's value by incorporating the values from a child node.
     *
     * @param item the child node whose values should be incorporated
     */
    void updateValue(InnerNode item);

    /**
     * Sets this node's value from two child items.
     * This is a convenience method that calls setup() and then updates with both items.
     *
     * @param item1 the first child item
     * @param item2 the second child item
     */
    default void setValue(KPItem item1, KPItem item2) {
        setup();
        updateValue(item1);
        updateValue(item2);
    }

    /**
     * Sets this node's value from two child nodes.
     * This is a convenience method that calls setup() and then updates with both nodes.
     *
     * @param item1 the first child node
     * @param item2 the second child node
     */
    default void setValue(InnerNode item1, InnerNode item2) {
        setup();
        updateValue(item1);
        updateValue(item2);
    }

    /**
     * Checks if this node is currently active in the tree.
     *
     * @return true if active, false otherwise
     * @deprecated This method should be removed as it's not consistently used
     */
    // todo remove this method
    boolean isActive();
}
