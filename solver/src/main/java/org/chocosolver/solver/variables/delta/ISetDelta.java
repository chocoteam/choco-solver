/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.ICause;

/**
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public interface ISetDelta extends IDelta {

	int LB = 0;
	int UB = 1;

	int getSize(int lbOrUb);

	void add(int element, int lbOrUb, ICause cause);

	int get(int index, int lbOrUb);

	ICause getCause(int index, int lbOrUb);
}
