/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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
