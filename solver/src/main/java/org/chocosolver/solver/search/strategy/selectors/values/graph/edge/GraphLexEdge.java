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

import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class GraphLexEdge implements GraphEdgeSelector {

    public int[] selectEdge(GraphVar g) {
        ISet envSuc, kerSuc;
        for (int i : g.getPotentialNodes()) {
            envSuc = g.getPotentialSuccessorsOf(i);
            kerSuc = g.getMandatorySuccessorsOf(i);
            if (envSuc.size() != kerSuc.size()) {
                for (int j : envSuc) {
                    if (!kerSuc.contains(j)) {
                        return new int[] {i, j};
                    }
                }
            }
        }
        return new int[] {-1, -1};
    }
}
