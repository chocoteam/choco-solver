/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class UndirectedGraphVarImpl extends AbstractGraphVar<UndirectedGraph> {

	//////////////////////////////// GRAPH PART /////////////////////////////////////////

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a graph variable
	 *
	 * @param name
	 * @param solver
	 * @param LB
	 * @param UB
	 */
	public UndirectedGraphVarImpl(String name, Model solver, UndirectedGraph LB, UndirectedGraph UB) {
		super(name, solver, LB, UB);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
		assert cause != null;
		if (LB.edgeExists(x, y)) {
			this.contradiction(cause, "remove mandatory arc");
			return false;
		}
		if (UB.removeEdge(x, y)) {
			if (reactOnModification) {
				delta.add(x, GraphDelta.AR_TAIL, cause);
				delta.add(y, GraphDelta.AR_HEAD, cause);
			}
			GraphEventType e = GraphEventType.REMOVE_ARC;
			notifyPropagators(e, cause);
			return true;
		}
		return false;
	}

	@Override
	public boolean enforceArc(int x, int y, ICause cause) throws ContradictionException {
		assert cause != null;
		enforceNode(x, cause);
		enforceNode(y, cause);
		if (UB.edgeExists(x, y)) {
			if (LB.addEdge(x, y)) {
				if (reactOnModification) {
					delta.add(x, GraphDelta.AE_TAIL, cause);
					delta.add(y, GraphDelta.AE_HEAD, cause);
				}
				GraphEventType e = GraphEventType.ADD_ARC;
				notifyPropagators(e, cause);
				return true;
			}
			return false;
		}
		this.contradiction(cause, "enforce arc which is not in the domain");
		return false;
	}

	/**
	 * Get the set of neighbors of vertex 'idx' in the lower bound graph
	 * (mandatory incident edges)
	 *
	 * @param idx a vertex
	 * @return The set of neighbors of 'idx' in LB
	 */
	public ISet getMandNeighOf(int idx) {
		return getMandSuccOrNeighOf(idx);
	}

	/**
	 * Get the set of neighbors of vertex 'idx' in the upper bound graph
	 * (potential incident edges)
	 *
	 * @param idx a vertex
	 * @return The set of neighbors of 'idx' in UB
	 */
	public ISet getPotNeighOf(int idx) {
		return getPotSuccOrNeighOf(idx);
	}

	@Override
	public boolean isDirected() {
		return false;
	}
}
