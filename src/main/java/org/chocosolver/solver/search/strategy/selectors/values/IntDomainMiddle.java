/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

/**
 * Selects the value in the variable domain closest to the mean of its current bounds.
 * <br/>
 * It computes the middle value of the domain. Then it checks if the mean is contained in the domain.
 * If not, the closest value to the middle is chosen. It uses a policy to define whether the mean value should
 * be floored or ceiled
 * <br/>
 *
 * BEWARE: should not be used with assignment decisions over bounded variables (because the decision negation
 * would result in no inference)
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 2 juil. 2010
 */
public class IntDomainMiddle implements IntValueSelector {

	// VARIABLES
	public final static boolean FLOOR = true;
	private final boolean roundingPolicy;

	/**Selects the middle value
	 * @param roundingPolicy should be either FLOOR or !FLOOR (ceil)
	 */
	public IntDomainMiddle(boolean roundingPolicy){
		this.roundingPolicy = roundingPolicy;
	}

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("PointlessBooleanExpression")
	@Override
    public int selectValue(IntVar var) {
		int low = var.getLB();
		int upp = var.getUB();
		double mean = (double)(low + upp) / 2;
		int value;
		if(roundingPolicy==FLOOR){
			value = (int) Math.floor(mean);
		}else{
			value = (int) Math.ceil(mean);
		}
		if (var.hasEnumeratedDomain()) {
            if (!var.contains(value)) {
				double a = var.previousValue(value);
				double b = var.nextValue(value);
				if(mean-a < b-mean){
					return (int) a;
				}else if(mean-a > b-mean){
					return (int) b;
				}else{ //tie break
					if(roundingPolicy==FLOOR){
						return (int) a;
					}else{
						return (int) b;
					}
				}
            }
        }
		return value;
    }
}
