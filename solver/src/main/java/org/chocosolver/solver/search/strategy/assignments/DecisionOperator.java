/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.assignments;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;

import java.io.Serializable;



/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/02/11
 */
public interface DecisionOperator<V extends Variable> extends Serializable {

    boolean apply(V var, int value, ICause cause) throws ContradictionException;

    boolean unapply(V var, int value, ICause cause) throws ContradictionException;

    DecisionOperator<V> opposite();

    String toString();

}