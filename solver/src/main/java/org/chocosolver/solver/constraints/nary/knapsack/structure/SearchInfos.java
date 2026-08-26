/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Information container for search results in the knapsack filtering algorithm.
 * <p>
 * This class is used to transmit information between calls to
 * {@link ComputingLossWeightTree#computeLimitWeightMandatory} and
 * {@link ComputingLossWeightTree#computeLimitWeightForbidden} methods.
 * It contains the results of determining whether an item is mandatory or forbidden,
 * along with accumulated values used for subsequent computations.
 * <p>
 * All fields are immutable to ensure consistency during the filtering process.
 *
 * @param decision          The decision result: true if the item is mandatory/forbidden, false otherwise.
 * @param lastItemIndex     The index of the last item processed in the search.
 *                          This is used as the starting point for the next search iteration.
 * @param accumulatedProfit The total profit accumulated during the search.
 *                          This represents the profit from items that can be used to replace or compensate
 *                          for the item being checked.
 * @param accumulatedWeight The total weight accumulated during the search.
 *                          This represents the weight from items that can be used to replace or compensate
 *                          for the item being checked.
 * @param remainingWeight   The remaining weight of the last item after partial inclusion.
 *                          This is used for precise calculations when an item is only partially included.
 * @author Nicolas PIERRE
 */
public record SearchInfos(boolean decision, int lastItemIndex, double accumulatedProfit, double accumulatedWeight,
                          double remainingWeight) {
}
