/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.TimeStampedObject;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class GraphDeltaMonitor extends TimeStampedObject {

	private final GraphDelta delta;
	private int[] first; // references, in variable delta value to propagate, to un propagated values
	private int[] frozenFirst, frozenLast; // same as previous while the recorder is frozen, to allow "concurrent modifications"
	private ICause propagator;

	public GraphDeltaMonitor(GraphDelta delta, ICause propagator) {
		super(delta.getEnvironment());
		this.delta = delta;
		this.first = new int[4];
		this.frozenFirst = new int[4];
		this.frozenLast = new int[4];
		this.propagator = propagator;
	}

	public void freeze() {
		if (needReset()) {
			for (int i = 0; i < 4; i++) {
				first[i] = 0;
			}
			resetStamp();
		}
		for (int i = 0; i < 3; i++) {
			frozenFirst[i] = first[i]; // freeze indices
			first[i] = frozenLast[i] = delta.getSize(i);
		}
		frozenFirst[3] = first[3]; // freeze indices
		first[3] = frozenLast[3] = delta.getSize(GraphDelta.AE_TAIL);
	}

	public void unfreeze() {
		delta.lazyClear();    // fix 27/07/12
		resetStamp();
		for (int i = 0; i < 3; i++) {
			first[i] = delta.getSize(i);
		}
		first[3] = delta.getSize(GraphDelta.AE_TAIL);
	}

	/**
	 * Applies proc to every vertex which has just been removed or enforced, depending on evt.
	 * @param proc    an incremental procedure over vertices
	 * @param evt    either ENFORCENODE or REMOVENODE
	 * @throws ContradictionException if a failure occurs
	 */
	public void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException {
		int type;
		if (evt == GraphEventType.REMOVE_NODE) {
			type = GraphDelta.NR;
			for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
				if (delta.getCause(i, type) != propagator) {
					proc.execute(delta.get(i, type));
				}
			}
		} else if (evt == GraphEventType.ADD_NODE) {
			type = GraphDelta.NE;
			for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
				if (delta.getCause(i, type) != propagator) {
					proc.execute(delta.get(i, type));
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Applies proc to every arc which has just been removed or enforced, depending on evt.
	 * @param proc    an incremental procedure over arcs
	 * @param evt    either ENFORCEARC or REMOVEARC
	 * @throws ContradictionException if a failure occurs
	 */
	public void forEachArc(PairProcedure proc, GraphEventType evt) throws ContradictionException {
		if (evt == GraphEventType.REMOVE_ARC) {
			for (int i = frozenFirst[2]; i < frozenLast[2]; i++) {
				if (delta.getCause(i, GraphDelta.AR_TAIL) != propagator) {
					proc.execute(delta.get(i, GraphDelta.AR_TAIL), delta.get(i, GraphDelta.AR_HEAD));
				}
			}
		} else if (evt == GraphEventType.ADD_ARC) {
			for (int i = frozenFirst[3]; i < frozenLast[3]; i++) {
				if (delta.getCause(i, GraphDelta.AE_TAIL) != propagator) {
					proc.execute(delta.get(i, GraphDelta.AE_TAIL), delta.get(i, GraphDelta.AE_HEAD));
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
