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
 * Selector for graph strategies which indicates whether the next decision should apply
 * on a node or and edge.
 *
 * @author Dimitri Justeau-Allaire
 * @since 19/04/2021
 */
public interface GraphNodeOrEdgeSelector<G extends GraphVar> {

    /**
     * @return True if the next decision must be on nodes, else if it must be on edges.
     */
    boolean nextIsNode(G g);
}
