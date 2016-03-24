/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
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
	@SuppressWarnings("PointlessBooleanExpression")
	public final static boolean CEIL = !FLOOR;
	protected final boolean roundingPolicy;

	/**Selects the middle value
	 * @param roundingPolicy should be either FLOOR or CEIL
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
