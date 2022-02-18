/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
public interface UndirectedGraphVar extends GraphVar<UndirectedGraph> {

    /**
     * Get the set of neighbors of vertex 'idx' in the lower bound graph
     * (mandatory incident edges)
     *
     * @param idx a vertex
     * @return The set of neighbors of 'idx' in LB
     */
    default ISet getMandatoryNeighborsOf(int idx) {
        return getLB().getNeighborsOf(idx);
    }

    /**
     * Get the set of neighbors of vertex 'idx' in the upper bound graph
     * (potential incident edges)
     *
     * @param idx a vertex
     * @return The set of neighbors of 'idx' in UB
     */
    default ISet getPotentialNeighborsOf(int idx) {
        return getUB().getNeighborsOf(idx);
    }

    /**
     * @deprecated For an undirected graph, this method is equivalent to getMandatoryNeighborsOf.
     */
    @Deprecated
    @Override
    default ISet getMandatorySuccessorsOf(int node) {
        return GraphVar.super.getMandatorySuccessorsOf(node);
    }

    /**
     * @deprecated For an undirected graph, this method is equivalent to getPotentialNeighborsOf.
     */
    @Deprecated
    @Override
    default ISet getPotentialSuccessorsOf(int node) {
        return GraphVar.super.getPotentialSuccessorsOf(node);
    }

    /**
     * @deprecated For an undirected graph, this method is equivalent to getMandatoryNeighborsOf.
     */
    @Deprecated
    @Override
    default ISet getMandatoryPredecessorsOf(int node) {
        return GraphVar.super.getMandatoryPredecessorsOf(node);
    }

    /**
     * @deprecated For an undirected graph, this method is equivalent to getPotentialNeighborsOf.
     */
    @Deprecated
    @Override
    default ISet getPotentialPredecessorOf(int node) {
        return GraphVar.super.getPotentialPredecessorOf(node);
    }

    /**
     * Retrieves the current value of the variable if instantiated, otherwise the lower bound (kernel).
     *
     * @return the current value (or kernel if not yet instantiated).
     */
    default UndirectedGraph getValue(){
        assert isInstantiated() : getName() + " not instantiated";
        return getLB();
    }

    @Override
    default boolean isDirected() {
        return false;
    }
}
