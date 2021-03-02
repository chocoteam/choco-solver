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

public class GraphLexArc extends GraphArcSelector<GraphVar> {

	public GraphLexArc(GraphVar g) {
		super(g);
	}

	@Override
	public boolean computeNextArc() {
		ISet envSuc, kerSuc;
		for (int i : envNodes) {
			envSuc = g.getPotSuccOrNeighOf(i);
			kerSuc = g.getMandSuccOrNeighOf(i);
			if (envSuc.size() != kerSuc.size()) {
				for (int j : envSuc) {
					if (!kerSuc.contains(j)) {
						this.from = i;
						this.to = j;
						return true;
					}
				}
			}
		}
		this.from = this.to = -1;
		return false;
	}
}
