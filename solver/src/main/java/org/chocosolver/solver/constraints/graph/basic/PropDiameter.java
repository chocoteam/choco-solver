/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator for the diameter constraint
 *
 * @author Jean-Guillaume Fages
 */
public class PropDiameter extends Propagator<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntVar diameter;
	private BitSet visited;
	private TIntArrayList set, nextSet;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropDiameter(GraphVar graph, IntVar maxDiam) {
		super(new GraphVar[]{graph}, PropagatorPriority.LINEAR, false);
		this.g = graph;
		this.diameter = maxDiam;
		visited = new BitSet(g.getNbMaxNodes());
		set = new TIntArrayList();
		nextSet = new TIntArrayList();
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int max = -1;
		for (int i : g.getPotentialNodes()) {
			if (g.getMandatoryNodes().contains(i)) {
				diameter.updateLowerBound(depthBFS(i, true), this);
			}
			max = Math.max(max, depthBFS(i, false));
		}
		diameter.updateUpperBound(max, this);
	}

	private int depthBFS(int root, boolean min) {
		nextSet.clear();
		set.clear();
		visited.clear();
		int i = root;
		set.add(i);
		visited.set(i);
		ISet nei;
		int depth = 0;
		int nbMand = g.getMandatoryNodes().size();
		int count = 1;
		while (!set.isEmpty()) {
			for (i = set.size() - 1; i >= 0; i--) {
				nei = g.getPotentialSuccessorsOf(set.get(i));
				for (int j : nei) {
					if (!visited.get(j)) {
						visited.set(j);
						nextSet.add(j);
						if (min && g.getMandatoryNodes().contains(j)) {
							count++;
							if (count == nbMand) {
								return depth + 1;
							}
						}
					}
				}
			}
			depth++;
			TIntArrayList tmp = nextSet;
			nextSet = set;
			set = tmp;
			nextSet.clear();
		}
		return depth - 1;
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public ESat isEntailed() {
		int max = -1;
		int min = -1;
		for (int i : g.getPotentialNodes()) {
			if (g.getMandatoryNodes().contains(i)) {
				min = Math.max(min, depthBFS(i, true));
			}
			max = Math.max(max, depthBFS(i, false));
		}
		if (min > diameter.getUB() || max < diameter.getLB()) {
			return ESat.FALSE;
		}
		if (isCompletelyInstantiated() && min == diameter.getValue()) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
