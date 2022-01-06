/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * @author Arnaud Malapert
 * @since 10/30/2017
 */
public final class PropLEDistanceXYZ extends AbstractPropDistanceXYZ {

	
	public PropLEDistanceXYZ(IntVar[] vars) {
		super(vars);
	}
	
	@Override
	public void propagate(int evtmask) throws ContradictionException {
		vars[Z].updateLowerBound(0, this);
		propagate();
	}

	@Override
	protected boolean filterFromYZToX() throws ContradictionException {
		return filterLowerFromIZToJ(Y, X, 0);
	}

	@Override
	protected boolean filterFromXZToY() throws ContradictionException {
		return filterLowerFromIZToJ(X, Y, 0);
	}

	@Override
	protected boolean filterFromXYtoZ() throws ContradictionException {
		return filterFromXYtoLBZ(0);
	}

	@Override
	protected boolean isEntailed(int distance, int value) {
		return distance <= value;
	}

	@Override
	protected String getOperator() {
		return "<=";
	}
}
