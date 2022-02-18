/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.edges;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Jean-Guillaume Fages
 */
public class PropNeighSetsChannel2 extends Propagator<SetVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final int n;
	private int currentSet;
	private final ISetDeltaMonitor[] sdm;
	private final SetVar[] sets;
	private final GraphVar<?> g;
	private final IntProcedure elementForced;
	private final IntProcedure elementRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Channeling between a graph variable and set variables
	 * representing either node neighbors or node successors
	 */
	public PropNeighSetsChannel2(SetVar[] setsV, GraphVar<?> gV) {
		super(setsV, PropagatorPriority.LINEAR, true);
		this.sets = new SetVar[setsV.length];
		System.arraycopy(vars, 0, this.sets, 0, setsV.length);
		n = sets.length;
		this.g = gV;
		assert (n == g.getNbMaxNodes());
		sdm = new ISetDeltaMonitor[n];
		for (int i = 0; i < n; i++) {
			sdm[i] = sets[i].monitorDelta(this);
		}
		elementForced = element -> g.enforceEdge(currentSet, element, this);
		elementRemoved = element -> g.removeEdge(currentSet, element, this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for (int i = 0; i < n; i++) {
			for (int j : sets[i].getLB()) {
				g.enforceEdge(i, j, this);
			}
			ISet tmp = g.getPotentialSuccessorsOf(i);
			for (int j : tmp) {
				if (!sets[i].getUB().contains(j)) {
					g.removeEdge(i, j, this);
				}
			}
		}
		for (int i = 0; i < n; i++) {
			sdm[i].startMonitoring();
		}
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		currentSet = idxVarInProp;
		sdm[currentSet].forEach(elementForced, SetEventType.ADD_TO_KER);
		sdm[currentSet].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
	}

	@Override
	public ESat isEntailed() {
		for (int i = 0; i < n; i++) {
			for (int j : sets[i].getLB()) {
				if (!g.getPotentialSuccessorsOf(i).contains(j)) {
					return ESat.FALSE;
				}
			}
			ISet tmp = g.getMandatorySuccessorsOf(i);
			for (int j : tmp) {
				if (!sets[i].getUB().contains(j)) {
					return ESat.FALSE;
				}
			}
		}
		if (isCompletelyInstantiated()) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
