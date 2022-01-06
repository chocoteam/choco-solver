/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.edge;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Random;

public class GraphRandomEdge implements GraphEdgeSelector {

    private final Random rd;
    private final TIntArrayList pFrom;
    private final TIntArrayList pTo;

    public GraphRandomEdge(long seed) {
        rd = new Random(seed);
        pFrom = new TIntArrayList();
        pTo = new TIntArrayList();
    }

    public int[] selectEdge(GraphVar g) {
        pFrom.clear();
        pTo.clear();
        ISet envSuc, kerSuc;
        for (int i : g.getPotentialNodes()) {
            envSuc = g.getPotentialSuccessorsOf(i);
            kerSuc = g.getMandatorySuccessorsOf(i);
            if (envSuc.size() != kerSuc.size()) {
                for (int j : envSuc) {
                    if (!kerSuc.contains(j)) {
                        pFrom.add(i);
                        pTo.add(j);
                    }
                }
            }
        }
        if (pFrom.isEmpty()) {
            return new int[] {-1, -1};
        } else {
            int idx = rd.nextInt(pFrom.size());
            return new int[] {pFrom.get(idx), pTo.get(idx)};
        }
    }
}
