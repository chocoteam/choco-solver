/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.RealVar;

/**
 * Selects the lower bound of a real variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealDomainMin implements RealValueSelector {

    @Override
    public double selectValue(RealVar var) {
        double low = var.getLB();
        if (low == Double.NEGATIVE_INFINITY) low = -Double.MAX_VALUE;
        return low;
    }
}
