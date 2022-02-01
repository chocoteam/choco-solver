/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.SetVar;

/**
 * Heuristic for branching on a given SetVar
 * @author Jean-Guillaume Fages
 * @since 6/10/13
 */
public interface SetValueSelector {

	/**
	 * Value selection heuristic
	 * @param v a non-instantiated SetVar
	 * @return an integer i of v's envelope, which is not included in v's kernel
	 * so that a decision (forcing/removing i) can be applied on v
	 */
	int selectValue(SetVar v);

}
