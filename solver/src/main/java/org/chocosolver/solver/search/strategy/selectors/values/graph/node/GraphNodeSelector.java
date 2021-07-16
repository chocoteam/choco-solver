/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.node;

import org.chocosolver.solver.variables.GraphVar;

public interface GraphNodeSelector<G extends GraphVar> {

    int selectNode(G g);

}
