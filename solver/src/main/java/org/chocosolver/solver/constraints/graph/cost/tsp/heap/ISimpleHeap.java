/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp.heap;

/**
 * Simple heap interface with only a few services
 *
 * @author Jean-Guillaume Fages
 */
public interface ISimpleHeap {


    /**
     * Get and remove the lowest element of the heap
     *
     * @return the first (lowest) element of the heap
     */
    int removeFirstElement();

    /**
     * Add element in the heap or update its value in case it is already in
     *
     * @param element key
     * @param value value
     * @return true iff element was not already in the heap or if the new value is strictly lower than the previous one
     */
    boolean addOrUpdateElement(int element, double value);

    /**
     * @return true iff the heap is empty
     */
    boolean isEmpty();

    /**
     * Clear the heap (remove any remaining element)
     */
    void clear();
}
