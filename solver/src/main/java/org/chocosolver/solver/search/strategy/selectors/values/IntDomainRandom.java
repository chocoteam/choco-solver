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
 * Selects randomly a value in the variable domain.
 *
 * BEWARE: this should not be used within assignments and/or value removals if variables
 * have a bounded domain.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class IntDomainRandom implements IntValueSelector {

	private final Random rand;

    public IntDomainRandom(long seed) {
        this.rand = new Random(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
		int i = rand.nextInt(var.getDomainSize());
		int value = var.getLB();
		while (i > 0) {
			value = var.nextValue(value);
			i--;
		}
		return value;
    }
}
