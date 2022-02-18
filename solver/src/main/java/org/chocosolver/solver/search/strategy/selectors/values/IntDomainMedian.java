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

/**
 * Selects the median value in the variable domain.
 *
 * BEWARE: this should not be used within assignments and/or value removals if variables
 * have a bounded domain.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class IntDomainMedian implements IntValueSelector {

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
		int dz = var.getDomainSize();
		if(dz % 2 == 0){ // even number of values
			dz--;
		}
		dz  =  dz >> 1;
		int median = var.getLB();
		for (int i = 0; i < dz; i++) {
			median = var.nextValue(median);
		}
		return median;
    }
}
