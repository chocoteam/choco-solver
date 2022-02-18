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

import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * Selects randomly between the lower and the upper bound of the variable
 * <br/>
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 2 april 2014
 */
public class IntDomainRandomBound implements IntValueSelector {

    private final Random rand;

    public IntDomainRandomBound(long seed) {
        this.rand = new Random(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
		return rand.nextBoolean() ? var.getLB() : var.getUB();
    }
}
