/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

public class Info {
    public final int index;
    public final double profit;
    public final int weight;
    public final int weightWithoutCriticalItem;

    public Info(int index, double profit, int profitWithoutCriticalItem, int weight) {
        this.index = index;
        this.profit = profit;
        this.weight = weight;
        this.weightWithoutCriticalItem = profitWithoutCriticalItem;
    }
}
