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
import org.chocosolver.util.objects.setDataStructures.ISet;

public abstract class GraphArcSelector<G extends GraphVar> {

	protected G g;
	protected ISet envNodes;

	protected int from, to;


	public GraphArcSelector(G g) {
		this.g = g;
		this.envNodes = g.getPotentialNodes();
	}

	public abstract boolean computeNextArc();

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
}
