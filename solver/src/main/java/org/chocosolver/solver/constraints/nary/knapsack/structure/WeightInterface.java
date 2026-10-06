/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * Interface for objects that have a weight value.
 * <p>
 * This interface is implemented by both {@link KPItem} and tree node classes
 * to allow uniform access to weight information in the knapsack algorithms.
 *
 * @author Nicolas PIERRE
 */
public interface WeightInterface {
    
    /**
     * Returns the weight of this object.
     * For inactive items, this should return 0.
     *
     * @return the weight value
     */
    int getWeight();
}
