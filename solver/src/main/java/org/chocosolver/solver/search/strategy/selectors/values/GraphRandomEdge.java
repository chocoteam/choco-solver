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

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Random;

public class GraphRandomEdge extends GraphEdgeSelector<GraphVar> {

	private Random rd;
	private TIntArrayList pFrom, pTo;

	public GraphRandomEdge(GraphVar g, long seed) {
		super(g);
		rd = new Random(seed);
		pFrom = new TIntArrayList();
		pTo = new TIntArrayList();
	}

	@Override
	public boolean computeNextEdge() {
		pFrom.clear();
		pTo.clear();
		ISet envSuc, kerSuc;
		for (int i : envNodes) {
			envSuc = g.getPotentialSuccessorsOf(i);
			kerSuc = g.getMandatorySuccessorsOf(i);
			if (envSuc.size() != kerSuc.size()) {
				for (int j : envSuc) {
					if (!kerSuc.contains(j)) {
						pFrom.add(i);
						pTo.add(j);
					}
				}
			}
		}
		if (pFrom.isEmpty()) {
			this.from = this.to = -1;
			return false;
		} else {
			int idx = rd.nextInt(pFrom.size());
			this.from = pFrom.get(idx);
			this.to = pTo.get(idx);
			return true;
		}
	}
}
