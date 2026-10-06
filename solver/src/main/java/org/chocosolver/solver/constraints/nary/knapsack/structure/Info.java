/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Information container for the Dantzig relaxation solution of a knapsack problem.
 * <p>
 * This class stores the results of computing the fractional knapsack solution (Dantzig relaxation),
 * which includes:
 * <ul>
 *   <li>The index of the critical item (first item that cannot be fully included)</li>
 *   <li>The total profit of the relaxed solution</li>
 *   <li>The total weight used in the relaxed solution</li>
 *   <li>The weight without the critical item (used for computing bounds)</li>
 * </ul>
 * <p>
 * The Dantzig relaxation is computed by greedily selecting items in order of decreasing
 * efficiency until the capacity is reached, then including a fraction of the next item.
 *
 * @param index                     The index of the critical item in the efficiency-sorted tree.
 *                                  The critical item is the first item that cannot be fully included without exceeding the capacity.
 * @param profit                    The total profit of the Dantzig relaxation solution.
 *                                  This includes the profit from fully included items plus the fractional profit from the critical item.
 * @param weight                    The total weight used in the Dantzig relaxation solution.
 *                                  This equals the knapsack capacity when the solution uses the full capacity.
 * @param weightWithoutCriticalItem The weight without the critical item's fractional part.
 *                                  This represents the weight of all fully included items before the critical item.
 *                                  Note: Despite the parameter name in the constructor, this field stores a weight value, not a profit.
 * @author Nicolas PIERRE
 */
public record Info(int index, double profit, int weightWithoutCriticalItem, int weight) {
}
