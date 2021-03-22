/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.GraphVar;

public class GraphLexNode extends GraphNodeSelector<GraphVar> {

    public GraphLexNode(GraphVar g) {
        super(g);
    }

    @Override
    public int nextNode() {
        for (int i : envNodes) {
            if (!kerNodes.contains(i)) {
                return i;
            }
        }
        return -1;
    }
}
