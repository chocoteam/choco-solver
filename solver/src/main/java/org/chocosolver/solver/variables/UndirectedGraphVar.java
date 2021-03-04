/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Interface defining an undirected graph variable
 */
public interface UndirectedGraphVar<E extends UndirectedGraph> extends GraphVar<E> {

    /**
     * Get the set of neighbors of vertex 'idx' in the lower bound graph
     * (mandatory incident edges)
     *
     * @param idx a vertex
     * @return The set of neighbors of 'idx' in LB
     */
    default ISet getMandNeighOf(int idx) {
        return getMandSuccOrNeighOf(idx);
    }

    /**
     * Get the set of neighbors of vertex 'idx' in the upper bound graph
     * (potential incident edges)
     *
     * @param idx a vertex
     * @return The set of neighbors of 'idx' in UB
     */
    default ISet getPotNeighOf(int idx) {
        return getPotSuccOrNeighOf(idx);
    }

    /**
     * Retrieves the current value of the variable if instantiated, otherwise the lower bound (kernel).
     *
     * @return the current value (or kernel if not yet instantiated).
     */
    default E getValue(){
        assert isInstantiated() : getName() + " not instantiated";
        return getLB();
    }

    @Override
    default boolean isDirected() {
        return false;
    }
}
