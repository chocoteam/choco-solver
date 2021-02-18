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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * @author Arnaud Malapert
 * @since 10/30/2017
 */
public final class PropGEDistanceXYZ extends AbstractPropDistanceXYZ {

	
	public PropGEDistanceXYZ(IntVar[] vars) {
		super(vars);
	}

	@Override
	protected boolean filterFromYZToX() throws ContradictionException {
		return filterGreaterFromIZToJ(Y, X, 1);
	}

	@Override
	protected boolean filterFromXZToY() throws ContradictionException {
		return filterGreaterFromIZToJ(X, Y, 1);
	}

	@Override
	protected boolean filterFromXYtoZ() throws ContradictionException {
		return filterFromXYtoUBZ(0);
	}

	@Override
	protected boolean isEntailed(int distance, int value) {
		return distance >= value;
	}

	@Override
	protected String getOperator() {
		return ">=";
	}
}
