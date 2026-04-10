/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.edge;

import org.chocosolver.solver.variables.GraphVar;

public interface GraphEdgeSelector<G extends GraphVar> {

    int[] selectEdge(G g);

}
