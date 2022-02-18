/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.differences;

/**
 * Interface to represent a set of difference constraints
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public interface D {

	/**
	 * @return true iff var[i1] and var[i2] must be different
	 */
	boolean mustBeDifferent(int i1, int i2);
}
