/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.mis;

import java.util.BitSet;

/**
 * Interface to represent an heuristic which computes independent sets
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public interface F {

	/** Potentially performs some calculation before computing independent sets */
	void prepare();

	/** Computes an Independent Set as large as possible, although it is not necessarily maximum */
	void computeMIS();

	/**
	 * @return true iff the heuristic can compute another independent set
	 */
	boolean hasNextMIS();

	/**
	 * @return a BitSet representing vertices that belong to the independent set
	 */
	BitSet getMIS();
}
