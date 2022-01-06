/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.ICause;

/**
 * An empty interface for delta dedicated to integer variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/03/11
 */
public interface IntDelta extends IDelta {

	/**
	 * Returns the number of element
	 *
	 * @return number of element
	 */
	int size();

	/**
	 * Return the cause of the idx^th cause stored in the delta, if any
	 *
	 * @param idx rank of the interval
	 * @return cause of the removal
	 * @throws IndexOutOfBoundsException if idx is out of the bounds
	 */
	ICause getCause(int idx) throws IndexOutOfBoundsException;
}
