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

import java.util.BitSet;

/**
 * Fast heap which stores in a priority stack elements
 * of value Integer.MIN_VALUE (lower values are forbidden)
 * Created by IntelliJ IDEA.
 *
 * @author Jean-Guillaume Fages
 * @since 18/11/12
 */
public class FastSimpleHeap implements ISimpleHeap {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final BitSet inBest;
    private final BitSet obsolete;
    private final int[] stack;
    private int k;
    private final ISimpleHeap heap_rest;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public FastSimpleHeap(int n) {
        this(n, new BinarySimpleHeap(n));
    }

    public FastSimpleHeap(int n, ISimpleHeap heap) {
        inBest = new BitSet(n);
        obsolete = new BitSet(n);
        stack = new int[n];
        k = 0;
        heap_rest = heap;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    public boolean isEmpty() {
        return k == 0 && heap_rest.isEmpty();
    }

    public int removeFirstElement() {
        if (k == 0) {
            int f;
            do {
                f = heap_rest.removeFirstElement();
            } while (f != -1 && obsolete.get(f));
            return f;
        }
        int first = stack[--k];
        inBest.clear(first);
        return first;
    }

    public boolean addOrUpdateElement(int element, double value) {
        if (value < Integer.MIN_VALUE)
            throw new UnsupportedOperationException("cannot use a FastSimpleHeap on such data");
        if (inBest.get(element)) {
            return false;
        }
        if (value == Double.MIN_VALUE) {
            inBest.set(element);
            stack[k++] = element;
            obsolete.set(element);
            return true;
        }
        return heap_rest.addOrUpdateElement(element, value);
    }

    public void clear() {
        k = 0;
        inBest.clear();
        obsolete.clear();
        heap_rest.clear();
    }
}
