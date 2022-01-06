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

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;



/**
 * Implementation based on "Maintaining GAC on adhoc r-ary constraints", Cheng and Yap, CP12.
 * <p/>
 * <p/>
 * Created by cprudhom on 04/11/14.
 * Project: choco.
 */
public class StoredSparseSet {

    private int[] sparse;
    private int[] dense;
    private final IStateInt members;


    public StoredSparseSet(IEnvironment environment) {
        members = environment.makeInt(0);
        sparse = new int[16];
        dense = new int[16];
    }

    /**
     * Return if a value is contained.
     *
     * @param k the value to test
     * @return true if `k` is contained, false otherwise
     */
    public boolean contains(int k) {
        if (k < sparse.length) {
            int a = sparse[k];
            return a < members.get() && dense[a] == k;
        } else return false;
    }

    /**
     * Add the value `k` to the set.
     *
     * @param k value to add
     */
    public void add(int k) {
        ensureCapacity(k+1);
        int a = sparse[k];
        int b = members.get();
        if (a >= b || dense[a] != k) {
            sparse[k] = b;
            dense[b] = k;
            members.set(b + 1);
        }
    }

    private void ensureCapacity(int k) {
        if (k > sparse.length) {
            int[] tmp = sparse;
            int nsize = Math.max(k+1, tmp.length * 3 / 2 + 1);
            sparse = new int[nsize];
            sparse = new int[nsize];
            System.arraycopy(tmp, 0, sparse, 0, tmp.length);
            tmp = dense;
            dense = new int[nsize];
            System.arraycopy(tmp, 0, dense, 0, tmp.length);
        }

    }

}
