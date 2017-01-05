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
 * Date: Apr 30, 2010
 * Time: 1:57:07 PM
 */
public abstract class AbstractPenaltyFunction implements IPenaltyFunction {
    @Override
    public abstract int penalty(int value);

    @Override
    public double minGHat(double lambda, IntVar var) {

        double ghat = Double.POSITIVE_INFINITY;
        int ub = var.getUB();
        for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
            ghat = Math.min(ghat, penalty(i) - lambda * i);
        }
        return ghat;
    }

    @Override
    public double maxGHat(double lambda, IntVar var) {
        double ghat = Double.NEGATIVE_INFINITY;
        int ub = var.getUB();
        for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
            ghat = Math.max(ghat, penalty(i) - lambda * i);
        }
        return ghat;
    }
}
