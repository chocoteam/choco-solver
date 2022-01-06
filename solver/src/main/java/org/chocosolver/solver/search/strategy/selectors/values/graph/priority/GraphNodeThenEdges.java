/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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
