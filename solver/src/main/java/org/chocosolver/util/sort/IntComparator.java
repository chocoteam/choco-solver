/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.sort;

/**
 * Comparator to sort primitive integers (presumably indexes)
 * @author Jean-Guillaume Fages
 * @since 07/11/13
 */
public interface IntComparator {

	/**
	 * comparator for primitive integers
	 */
	int compare(int i1, int i2);
}
