/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/*
@author Arthur Godet <arth.godet@gmail.com>
@since 01/02/2021
*/

package org.chocosolver.solver.constraints.nary.alldifferentprec;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

public class IntUnionFind {
    private final int[] values;
    private final TIntIntHashMap sets;
    private final int[] id;
    private final int[] sizes;
    private final int[] mins;
    private final int[] maxs;

    private final int n;

    public IntUnionFind(int l, int u) {
        this(IntStream.range(l, u + 1).toArray());
    }

    public IntUnionFind(int[] values) {
        this.values = values;
        Arrays.sort(this.values);
        n = values.length;
        sets = new TIntIntHashMap(n, 0.5f, -1, -1);
        mins = new int[n];
        maxs = new int[n];
        sizes = new int[n];
        id = new int[n];
        for(int i = 0; i < n; i++) {
            sets.put(this.values[i], i);
            mins[i] = this.values[i];
            maxs[i] = this.values[i];
            id[i] = i;
            sizes[i] = 1;
        }
    }

    public void init() {
        sets.clear();
        for(int i = 0; i < n; i++) {
            sets.put(this.values[i], i);
            mins[i] = this.values[i];
            maxs[i] = this.values[i];
            sizes[i] = 1;
            id[i] = i;
            sizes[i] = 1;
        }
    }

    public int find(int a) {
        return root(a);
    }

    public int getMin(int idxSet) {
        return mins[idxSet];
    }

    public int getMax(int idxSet) {
        return maxs[idxSet];
    }

    private int root(int a) {
        int i = sets.get(a);
        if(i >= 0) {
            while(id[i] != i) {
                id[i] = id[id[i]];
                i = id[i];
            }
        }
        return i;
    }

    public void union(int a, int b) {
        int idx1 = root(a);
        int idx2 = root(b);
        int min = Math.min(mins[idx1], mins[idx2]);
        int max = Math.max(maxs[idx1], maxs[idx2]);
        if(sizes[idx1] < sizes[idx2]) {
            mins[idx2] = min;
            maxs[idx2] = max;
            sizes[idx2] += sizes[idx1];
            id[idx1] = idx2;
        } else {
            mins[idx1] = min;
            maxs[idx1] = max;
            sizes[idx1] += sizes[idx2];
            id[idx2] = idx1;
        }
    }

    @Override
    public String toString() {
        return sets.toString() + "\n" +
                "mins : " + Arrays.toString(mins) + "\n" +
                "maxs : " + Arrays.toString(maxs) + "\n" +
                "id : " + Arrays.toString(id);
    }
}
