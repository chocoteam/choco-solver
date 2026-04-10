/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * Randomly selects a value in the variable domain.
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
	private final boolean boundOnly;

	/**
	 * Creates a value selector that randomly selects a value in the variable domain.
	 * </p>
	 * By default, if the variable has a bounded domain,
	 * only the lower or upper bound of the variable will be selected randomly.
	 * @param seed the seed for the random number generator
	 */
    public IntDomainRandom(long seed) {
        this(seed, false);
    }

	/**
	 * Creates a value selector that randomly selects a value in the variable domain.
	 * @param seed the seed for the random number generator
	 * @param boundOnly if true, only the lower or upper bound of the variable will be selected randomly
	 */
	public IntDomainRandom(long seed, boolean boundOnly) {
	        this.rand = new Random(seed);
			this.boundOnly = boundOnly;
	    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        if (boundOnly || !var.hasEnumeratedDomain()) {
            return rand.nextBoolean() ? var.getLB() : var.getUB();
        } else {
            int i = rand.nextInt(var.getDomainSize());
            int value = var.getLB();
            while (i > 0) {
                value = var.nextValue(value);
                i--;
            }
            return value;
        }
    }
}
