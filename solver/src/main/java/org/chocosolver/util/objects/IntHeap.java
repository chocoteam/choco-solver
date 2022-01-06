/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * A heap implementation with support for decrease/increase key.
 * Based on <a href="http://minisat.se/MiniSat.html">MiniSat</a> implementation.
 * <br/>
 *
 * <p>
 * It is a minimum-heap with respect the {@link Comp} a comparator to define.
 * </p>
 *
 * @author Charles Prud'homme
 * @since 19/03/2021
 */
public class IntHeap {

    @FunctionalInterface
    public interface Comp {
        boolean lt(int a, int b);
    }

    // The heap is a minimum-heap with respect to this comparator
    Comp comp;
    // Heap of elements
    TIntArrayList heap = new TIntArrayList();
    // Each integers position (index) in the Heap
    TIntArrayList indices = new TIntArrayList();

    /**
     * Create a minimum heap wrt to {@code comp} a specific comparator.
     *
     * @param comp a comparator that compare 2 elements in the heap (a,b) and returns true
     *             iff {@code f(a) < f(b)} where {@code f} is designed on purpose.
     */
    public IntHeap(Comp comp) {
        this.comp = comp;
    }

    /**
     * @return number of element in this heap.
     */
    public int size() {
        return heap.size();
    }

    /**
     * @return {@code true} if this heap is empty.
     */
    public boolean isEmpty() {
        return heap.size() == 0;
    }

    /**
     * @param n an element
     * @return {@code true} if this heap contains the element {@code n}.
     */
    public boolean contains(int n) {
        return n < indices.size() && indices.get(n) >= 0;
    }

    /**
     * @param pos a position
     * @return the element at position {@code pos} in this.
     * @throws ArrayIndexOutOfBoundsException if {@code pos} is not in the bounds of the heap.
     */
    public int get(int pos) {
        return heap.get(pos);
    }

    /**
     * Move an element up in the heap considering its weight has decreased.
     *
     * @param n an element
     */
    public void decrease(int n) {
        assert (contains(n));
        percolateUp(indices.get(n));
    }

    /**
     * Move an element up in the heap considering its weight has increased.
     *
     * @param n an element
     */
    public void increase(int n) {
        assert (contains(n));
        percolateDown(indices.get(n));
    }


    /**
     * Move an element in the heap considering its weight has changed.
     *
     * @param n an element
     */
    public void update(int n) {
        if (!contains(n))
            insert(n);
        else {
            percolateUp(indices.get(n));
            percolateDown(indices.get(n));
        }
    }

    /**
     * Insert an element in this heap
     *
     * @param n element to insert
     */
    public void insert(int n) {
        int k = indices.size();
        //indices.ensureCapacity(n + 1);
        if (k <= n) {
            indices.fill(k, n + 1, -1);
        }
        assert (!contains(n));

        indices.set(n, heap.size());
        heap.add(n);
        percolateUp(indices.get(n));
    }

    /**
     * Remove the element at root node, ie, the one with the minimum weight.
     *
     * @return the smallest element and update this.
     */
    public int removeMin() {
        int x = heap.get(0);
        heap.set(0, heap.get(heap.size() - 1));
        indices.set(heap.get(0), 0);
        indices.set(x, -1);
        heap.removeAt(heap.size() - 1);
        if (heap.size() > 1) percolateDown(0);
        return x;
    }


    /**
     * Empty this and add elements from {@code elements}.
     *
     * @param elements new list of element to store in the heap (the comparator has not changed though).
     */
    public void build(TIntList elements) {
        clear();
        for (int i = 0; i < elements.size(); i++) {
            indices.set(elements.get(i), i);
            heap.add(elements.get(i));
        }

        for (int i = heap.size() / 2 - 1; i >= 0; i--)
            percolateDown(i);
    }

    /**
     * Remove all elements from this.
     */
    public void clear() {
        for (int i = 0; i < heap.size(); i++)
            indices.set(heap.get(i), -1);
        heap.clear();
    }


    //////////////////////////////
    // Index "traversal" functions
    //////////////////////////////
    private static int left(int i) {
        return (i << 1) + 1;
    }

    private static int right(int i) {
        return (i + 1) << 1;
    }

    private static int parent(int i) {
        return (i - 1) >> 1;
    }

    private void percolateUp(int i) {
        int x = heap.get(i);
        int p = parent(i);

        while (i != 0 && comp.lt(x, heap.get(p))) {
            heap.set(i, heap.get(p));
            indices.set(heap.get(p), i);
            i = p;
            p = parent(p);
        }
        heap.set(i, x);
        indices.set(x, i);
    }


    private void percolateDown(int i) {
        int x = heap.get(i);
        while (left(i) < heap.size()) {
            int child = right(i) < heap.size() && comp.lt(heap.get(right(i)), heap.get(left(i))) ? right(i) : left(i);
            if (!comp.lt(heap.get(child), x)) break;
            heap.set(i, heap.get(child));
            indices.set(heap.get(i), i);
            i = child;
        }
        heap.set(i, x);
        indices.set(x, i);
    }

}
