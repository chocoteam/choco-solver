/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * class to transmit informations about the last search, useful for
 * {@code computeLimitWeightForbidden} and {@code computeLimitWeightMandatory}
 */
public class SearchInfos {
    public final boolean decision;
    public final int endItem;
    public final double profitAccumulated;
    public final double weightAccumulated;
    public final double remainingWeightEndItem;

    public SearchInfos(boolean decision, int endItem, double profitAccumulated, double weightAccumulated,
                       double remainingWeightEndItem) {
        this.decision = decision;
        this.endItem = endItem;
        this.profitAccumulated = profitAccumulated;
        this.weightAccumulated = weightAccumulated;
        this.remainingWeightEndItem = remainingWeightEndItem;
    }

}
