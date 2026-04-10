/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.assignments;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;

/**
 * @author Dimitri Justeau-Allaire
 * @since 19/04/2021
 */
public interface GraphDecisionOperator extends DecisionOperator<GraphVar>{

    boolean apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException;

    boolean unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException;

}
