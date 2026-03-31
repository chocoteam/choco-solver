/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.priority;

import org.chocosolver.solver.variables.GraphVar;

/**
 * Choose decisions on nodes first, until the node set is instantiated. Then choose decisions on edges.
 *
 * @author Dimitri Justeau-Allaire
 * @since 19/04/2021
 */
public class GraphNodeThenEdges implements GraphNodeOrEdgeSelector {

    @Override
    public boolean nextIsNode(GraphVar g) {
        return g.getPotentialNodes().size() != g.getMandatoryNodes().size();
    }
}
