/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.IResolutionHelper;
import org.chocosolver.solver.search.loop.learn.ILearnFactory;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitorFactory;
import org.chocosolver.solver.search.loop.move.IMoveFactory;

/**
 * Interface to ease modeling
 * Enables to make variables, views and constraints
 *
 * @author Jean-Guillaume FAGES
 * @since 4.0.0
 */
public interface ISolver extends ILearnFactory, IMoveFactory, ISearchMonitorFactory,
		IResolutionHelper {

}
