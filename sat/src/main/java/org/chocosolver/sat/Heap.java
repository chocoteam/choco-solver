/*
 * This file is part of choco-sat, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * A heap implementation with support for decrease/increase key.
 * Based on <a href="http://minisat.se/MiniSat.html">MiniSat</a> implementation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/03/2021
 */
public class Heap {

    public interface Comp {
        boolean lt(int a, int b);
    }

    // The heap is a minimum-heap with respect to this comparator
    Comp comp;
    // Heap of elements
    TIntArrayList heap = new TIntArrayList();
    // Each integers position (index) in the Heap
    TIntArrayList indices = new TIntArrayList();

    public Heap(Comp comp) {
        this.comp = comp;
    }

    public int size() {
        return heap.size();
    }

    public boolean empty() {
        return heap.size() == 0;
    }

    public boolean inHeap(int n) {
        return n < indices.size() && indices.get(n) >= 0;
    }

    public int get(int pos) {
        return heap.get(pos);
    }

    public void decrease(int n) {
        assert (inHeap(n));
        percolateUp(indices.get(n));
    }

    void increase(int n) {
        assert (inHeap(n));
        percolateDown(indices.get(n));
    }


    // Safe variant of insert/decrease/increase:
    void update(int n) {
        if (!inHeap(n))
            insert(n);
        else {
            percolateUp(indices.get(n));
            percolateDown(indices.get(n));
        }
    }

    public void insert(int n) {
        int k = indices.size();
        //indices.ensureCapacity(n + 1);
        if(k <= n){
            indices.fill(k, n + 1, -1);
        }
        assert (!inHeap(n));

        indices.set(n, heap.size());
        heap.add(n);
        percolateUp(indices.get(n));
    }


    public int removeMin() {
        int x = heap.get(0);
        heap.set(0, heap.get(heap.size() - 1));
        indices.set(heap.get(0), 0);
        indices.set(x, -1);
        heap.removeAt(heap.size() - 1);
        if (heap.size() > 1) percolateDown(0);
        return x;
    }


    // Rebuild the heap from scratch, using the elements in 'ns':
    public void build(TIntList ns) {
        clear();
        for (int i = 0; i < ns.size(); i++) {
            indices.set(ns.get(i), i);
            heap.add(ns.get(i));
        }

        for (int i = heap.size() / 2 - 1; i >= 0; i--)
            percolateDown(i);
    }

    void clear() {
        for (int i = 0; i < heap.size(); i++)
            indices.set(heap.get(i), -1);
        heap.clear();
    }


    //////////////////////////////
    // Index "traversal" functions
    //////////////////////////////
    static int left(int i) {
        return i * 2 + 1;
    }

    static int right(int i) {
        return (i + 1) * 2;
    }

    static int parent(int i) {
        return (i - 1) >> 1;
    }

    void percolateUp(int i) {
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


    void percolateDown(int i) {
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
