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

import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Interface defining a directed graph variable
 */
public interface DirectedGraphVar<E extends DirectedGraph> extends GraphVar<E> {

    /**
     * Get the set of successors of vertex 'idx' in the lower bound graph
     * (mandatory outgoing arcs)
     *
     * @param idx a vertex
     * @return The set of successors of 'idx' in LB
     */
    default ISet getMandSuccOf(int idx) {
        return getMandSuccOrNeighOf(idx);
    }

    /**
     * Get the set of predecessors of vertex 'idx' in the lower bound graph
     * (mandatory ingoing arcs)
     *
     * @param idx a vertex
     * @return The set of predecessors of 'idx' in LB
     */
    default ISet getMandPredOf(int idx) {
        return getMandPredOrNeighOf(idx);
    }

    /**
     * Get the set of predecessors of vertex 'idx'
     * in the upper bound graph (potential ingoing arcs)
     *
     * @param idx a vertex
     * @return The set of predecessors of 'idx' in UB
     */
    default ISet getPotPredOf(int idx) {
        return getPotPredOrNeighOf(idx);
    }

    /**
     * Get the set of successors of vertex 'idx'
     * in the upper bound graph (potential outgoing arcs)
     *
     * @param idx a vertex
     * @return The set of successors of 'idx' in UB
     */
    default ISet getPotSuccOf(int idx) {
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
        return true;
    }
}
