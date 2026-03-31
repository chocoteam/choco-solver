/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 2001, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.node;

import org.chocosolver.solver.variables.GraphVar;

public class GraphLexNode implements GraphNodeSelector {

    public int selectNode(GraphVar g) {
        for (int i : g.getPotentialNodes()) {
            if (!g.getMandatoryNodes().contains(i)) {
                return i;
            }
        }
        return -1;
    }
}
