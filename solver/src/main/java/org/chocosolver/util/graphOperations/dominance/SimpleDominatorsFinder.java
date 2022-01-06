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
 * Uses the simple LT algorithm which runs in O(m.log(n))
 * Fast in practice
 */
public class SimpleDominatorsFinder extends AbstractLengauerTarjanDominatorsFinder {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Object that finds dominators of the given flow graph g(s)
     * It uses the simple LT algorithm which runs in O(m.log(n))
     */
    public SimpleDominatorsFinder(int s, DirectedGraph g) {
        super(s, g);
    }

    //***********************************************************************************
    // link-eval
    //***********************************************************************************

    protected void link(int v, int w) {
        ancestor[w] = v;
    }

    protected int eval(int v) {
        if (ancestor[v] == -1) {
            return v;
        } else {
            compress(v);
            return label[v];
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
