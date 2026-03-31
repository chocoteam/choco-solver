/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

/**
 * Selects the variable lower bound
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 sept. 2010
 */
public final class IntDomainMin implements IntValueSelector {

    @Override
    public int selectValue(IntVar var) {
        return var.getLB();
    }

}
