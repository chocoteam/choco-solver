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
 * Time: 17:09
 */

package org.chocosolver.solver.constraints.graph.cost.tsp.heap;

import java.util.BitSet;

/**
 * Trivial Heap (not that bad in practice)
 * worst case running time for O(m) add/decrease key and O(n) pop = O(n*n+m)
 *
 * @author Jean-Guillaume Fages
 */
public class ArrayHeap implements ISimpleHeap {

    protected BitSet in;
    protected double[] value;
    protected int size;

    public ArrayHeap(int n) {
        in = new BitSet(n);
        value = new double[n];
        size = 0;
    }

    @Override
    public boolean addOrUpdateElement(int element, double elementKey) {
        if (!in.get(element)) {
            in.set(element);
            size++;
            value[element] = elementKey;
            return true;
        } else if (elementKey < value[element]) {
            value[element] = elementKey;
            return true;
        }
        return false;
    }

    @Override
    public int removeFirstElement() {
        if (isEmpty()) {
            throw new UnsupportedOperationException();
        }
        int min = in.nextSetBit(0);
        for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
            if (value[i] < value[min]) {
                min = i;
            }
        }
        in.clear(min);
        size--;
        return min;
    }

    @Override
    public void clear() {
        in.clear();
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }
}
