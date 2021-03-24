/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 30/01/12
 * Time: 17:10
 */

package org.chocosolver.solver.constraints.graph.cost.tsp.heap;

/**
 * Same worst case complexity but much better in practice
 * Especially when several nodes have same -infinity value
 */
public class FastArrayHeap extends ArrayHeap {

    private int[] best;
    private int bestSize;
    private double bestVal;

    public FastArrayHeap(int n) {
        super(n);
        best = new int[n];
    }

    @Override
    public boolean addOrUpdateElement(int element, double elementKey) {
        if (isEmpty() || elementKey < bestVal) {
            bestVal = elementKey;
            bestSize = 0;
            best[bestSize++] = element;
        } else if (elementKey == bestVal && elementKey < value[element]) {
            best[bestSize++] = element;
        }
        return super.addOrUpdateElement(element, elementKey);
    }

    @Override
    public int removeFirstElement() {
        if (bestSize > 0) {
            int min = best[--bestSize];
            in.clear(min);
            size--;
            return min;
        }
        return super.removeFirstElement();
    }

    @Override
    public void clear() {
        super.clear();
        bestSize = 0;
    }
}
