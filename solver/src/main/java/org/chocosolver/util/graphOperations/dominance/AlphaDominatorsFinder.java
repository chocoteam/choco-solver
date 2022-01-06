/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.dominance;

import org.chocosolver.util.objects.graphs.DirectedGraph;

/**
 * Class that finds dominators of a given flow graph g(s)
 * Uses the LT algorithm which runs in O(alpha.m)
 */
public class AlphaDominatorsFinder extends AbstractLengauerTarjanDominatorsFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int[] size;
    private final int[] child;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Object that finds dominators of the given flow graph g(s)
     * It uses the LT algorithm which runs in O(alpha.m)
     */
    public AlphaDominatorsFinder(int s, DirectedGraph g) {
        super(s, g);
        size = new int[n];
        child = new int[n];
    }

    //***********************************************************************************
    // INITIALIZATION
    //***********************************************************************************

    @Override
    protected void initParams(boolean inverseGraph) {
        super.initParams(inverseGraph);
        for (int i = 0; i < n; i++) {
            size[i] = 0;
            child[i] = root;
        }
    }

    //***********************************************************************************
    // link-eval
    //***********************************************************************************

    protected void link(int v, int w) {
        int s = w;
        while (semi[label[w]] < semi[label[child[s]]]) {
            if (size[s] + size[child[child[s]]] >= 2 * size[child[s]]) {
                ancestor[child[s]] = s;
                child[s] = child[child[s]];
            } else {
                size[child[s]] = size[s];
                ancestor[s] = child[s];
                s = ancestor[s];
            }
        }
        label[s] = label[w];
        size[v] = size[v] + size[w];
        if (size[v] < 2 * size[w]) {
            int k = s;
            s = child[v];
            child[v] = k;
        }
        while (s != root) {
            ancestor[s] = v;
            s = child[s];
        }
    }

    protected int eval(int v) {
        if (ancestor[v] == -1) {
            return label[v];
        } else {
            compress(v);
            if (semi[label[ancestor[v]]] >= semi[label[v]]) {
                return label[v];
            } else {
                return label[ancestor[v]];
            }
        }

    }

    protected void compress(int v) {
        int k = v;
        list.resetQuick();
        while (ancestor[ancestor[k]] != -1) {
            list.add(k);
            k = ancestor[k];
        }
        for (k = list.size() - 1; k >= 0; k--) {
            v = list.get(k);
            if (semi[label[ancestor[v]]] < semi[label[v]]) {
                label[v] = label[ancestor[v]];
            }
            ancestor[v] = ancestor[ancestor[v]];
        }
    }
}
