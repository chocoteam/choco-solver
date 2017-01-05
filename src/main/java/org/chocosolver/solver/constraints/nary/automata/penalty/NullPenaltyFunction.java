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
 * Date: May 3, 2010
 * Time: 6:50:02 PM
 */
public class NullPenaltyFunction extends AbstractPenaltyFunction {
    @Override
    public int penalty(int value) {
        return 0;
    }

    @Override
    public double minGHat(double lambda, IntVar var) {
        return -lambda * ((lambda > 0) ? var.getUB() : var.getLB());
    }

    @Override
    public double maxGHat(double lambda, IntVar var) {
        return -lambda * ((lambda < 0) ? var.getUB() : var.getLB());
    }

}
