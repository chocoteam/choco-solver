/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.penalty;

import org.chocosolver.solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 27, 2010
 * Time: 11:30:01 AM
 */
public interface IPenaltyFunction {

    int penalty(int value);

    double minGHat(double lambda, IntVar var);

    double maxGHat(double lambda, IntVar var);


}
