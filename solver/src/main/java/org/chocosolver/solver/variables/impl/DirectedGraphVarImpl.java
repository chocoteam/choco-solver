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

import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;

public class DirectedGraphVarImpl<E extends DirectedGraph> extends AbstractGraphVar<E> implements DirectedGraphVar<E> {

	////////////////////////////////// GRAPH PART ///////////////////////////////////////

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
	public DirectedGraphVarImpl(String name, Model solver, E LB, E UB) {
		super(name, solver, LB, UB);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
		assert cause != null;
		if (LB.arcExists(x, y)) {
			this.contradiction(cause, "remove mandatory arc " + x + "->" + y);
			return false;
		}
		if (UB.removeArc(x, y)) {
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
		if (UB.arcExists(x, y)) {
			if (LB.addArc(x, y)) {
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
}
