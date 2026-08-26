/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

import java.util.List;

/**
 * Finger tree specialized for finding items with sufficient weight.
 * <p>
 * This class extends {@link BinarySearchFingerTree} to provide efficient search
 * for items that meet a minimum weight requirement. It uses {@link InnerNodeMaxWeight}
 * nodes which store the maximum weight in each subtree, enabling efficient pruning
 * of search branches that cannot contain items meeting the weight criterion.
 * <p>
 * This tree is used in the Katriel algorithm to find the first leaf to the right of a
 * starting position that has weight greater than a given value.
 *
 * @author Nicolas PIERRE
 */
public class ItemFindingSearchTree extends BinarySearchFingerTree {

    /**
     * Constructs an item finding search tree from a list of sorted knapsack items.
     *
     * @param sortedItems the knapsack items sorted by decreasing efficiency
     */
    public ItemFindingSearchTree(List<KPItem> sortedItems) {
        super(sortedItems, InnerNodeMaxWeight::new);
    }

    /**
     * Finds the next leaf to the right of startingIndex (up to criticalIndex) with weight greater than the given value.
     *
     * @param startingIndex the global index to start searching from (exclusive)
     * @param criticalIndex the global index that bounds the search (inclusive)
     * @param weight        the minimum weight that the item must have
     * @return the global index of the first leaf to the right with weight > given weight, or -1 if none exists
     */
    public int findNextRightItem(int startingIndex, int criticalIndex, int weight) {
        return binarySearch(startingIndex, criticalIndex, i -> weight < getNodeWeight(i), true);
    }

}

