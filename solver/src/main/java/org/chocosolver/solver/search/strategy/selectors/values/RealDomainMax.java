/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.RealVar;

/**
 * Selects the upper bound of a real variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealDomainMax implements RealValueSelector {

    @Override
    public double selectValue(RealVar var) {
        double upp = var.getUB();
        if (upp == Double.POSITIVE_INFINITY) upp = Double.MAX_VALUE;
        return upp;
    }
}
