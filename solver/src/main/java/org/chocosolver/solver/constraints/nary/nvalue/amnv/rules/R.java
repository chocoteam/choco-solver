/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.rules;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

/**
 * Interface to represent a filtering rule
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public interface R {

	void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException;
}
