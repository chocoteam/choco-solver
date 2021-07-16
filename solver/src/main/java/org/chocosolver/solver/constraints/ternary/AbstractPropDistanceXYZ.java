/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A constraint to state |X - Y| operator Z
 * where operator can be =, <=, <, >=, > and X, Y, and Z are variables
 * Warning: only achieves BoundConsistency for the moment !
 *
 * @author Hadrien Cambazard, Charles Prud'homme, Arnaud Malapert
 * @since 06/04/12
 */
public abstract class AbstractPropDistanceXYZ extends Propagator<IntVar> {

	public final static int X = 0;
	public final static int Y = 1;
	public final static int Z = 2;

	/**
	 * Enforces |X - Y| op Z
	 *
	 * @param vars variable
	 */
	public AbstractPropDistanceXYZ(final IntVar[] vars) {
		super(vars, PropagatorPriority.TERNARY, true);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return IntEventType.boundAndInst();
	}

	public final void propagate() throws ContradictionException {
		boolean change;
		do {
			change = filterFromXYtoZ();
			change |= filterFromYZToX();
			change |= filterFromXZToY();
		} while (change);
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		propagate();
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		propagate();
	}

	protected abstract boolean filterFromYZToX() throws ContradictionException;


	protected abstract boolean filterFromXZToY() throws ContradictionException;


	protected abstract boolean filterFromXYtoZ() throws ContradictionException;


	/**
	 * Update lower bound of Z if X != Y
	 * @return <code>true</code> if the domain of Z has changed
	 * @throws ContradictionException
	 */
	public boolean filterFromXYtoLBZ(final int delta) throws ContradictionException {
		// x < y ?
		final int dYX = vars[Y].getLB() - vars[X].getUB();
		if (dYX > 0) { 
			return vars[2].updateLowerBound(dYX + delta, this);
		}
		// x > y ?
		final int dXY = vars[X].getLB() - vars[Y].getUB();
		return dXY > 0 && vars[Z].updateLowerBound(dXY + delta, this);
	}

	/**
	 * update upper bound of Z as max(|Y.sup - X.inf|, |Y.inf - X.sup|) - delta
	 * @return <code>true</code> if the domain of Z has changed
	 * @throws ContradictionException
	 */
	public boolean filterFromXYtoUBZ(final int delta) throws ContradictionException {
		final int a = Math.abs(vars[Y].getUB() - vars[X].getLB());
		final int b = Math.abs(vars[X].getUB() - vars[Y].getLB());
		return vars[Z].updateUpperBound( ( (a > b) ? a : b) - delta , this);
	}

	/**
	 * update bounds of X (resp. Y) from Z and Y (resp. X) based on | X - Y| <(=) Z
	 * @throws ContradictionException
	 */
	public boolean filterLowerFromIZToJ(final int i, final int j, final int delta) throws ContradictionException {
		final int lI = vars[i].getLB();
		final int uI = vars[i].getUB();
		final int uZ = vars[Z].getUB();
		final int lb = lI - uZ + delta;
		final int ub = uI + uZ - delta;
		return vars[j].updateBounds(lb, ub, this);
	}

	/**
	 * update bounds of X (resp. Y) from Z and Y (resp. X) based on | X - Y| >(=) Z
	 * @throws ContradictionException
	 */
	public boolean filterGreaterFromIZToJ(final int i, final int j, final int delta) throws ContradictionException {
		final int lI = vars[i].getLB();
		final int uI = vars[i].getUB();
		final int lZ = vars[Z].getLB();
		final int lbv = uI - lZ + delta;
		final int ubv = lI + lZ - delta;
		return vars[j].removeInterval(lbv, ubv, this);
	}

	protected abstract boolean isEntailed(final int distance, final int value);
	
	protected abstract String getOperator();

	@Override
	public ESat isEntailed() {
		if (isCompletelyInstantiated()) {
			final int distance = Math.abs(vars[X].getValue() - vars[Y].getValue());
			final int z = vars[Z].getValue();
			return ESat.eval(isEntailed(distance, z));
		}
		return ESat.UNDEFINED;
	}

	@Override
	public String toString() {
		return "|" + vars[X] + " - " + vars[Y] + "| " + getOperator() + " " + vars[Z];
	}

}
