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

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.RealVar;

/**
 * Selects a real value at the middle between the lower and the upper bound of the variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealDomainMiddle implements RealValueSelector {

    @Override
    public double selectValue(RealVar var) {
        double low = var.getLB();
        if (low == Double.NEGATIVE_INFINITY) low = -Double.MAX_VALUE;
        double upp = var.getUB();
        if (upp == Double.POSITIVE_INFINITY) upp = Double.MAX_VALUE;
        double r = (low + upp) / 2.0;
        if (r <= low || r >= upp) {
            throw new SolverException("RealDomainMiddle: find a value outside current domain!");
        }
        return r;
    }
}
