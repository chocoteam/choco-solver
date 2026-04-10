/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

import java.util.List;

public class ItemFindingSearchTree extends BinarySearchFingerTree {

    public ItemFindingSearchTree(List<KPItem> sortedItems) {
        super(sortedItems, InnerNodeMaxWeight::new);
    }

    public int findNextRightItem(int startingIndex, int criticalIndex, int weight) {
        return binarySearch(startingIndex, criticalIndex, i -> weight < getNodeWeight(i), true);
    }

}
