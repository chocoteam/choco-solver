/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy.selectors.variables;

import solver.search.strategy.selectors.VariableSelector;
import solver.variables.IntVar;

/**
 * <b>Most constrained</b> variable selector.
 * It chooses the variable with the smallest value in its domain, breaking ties using the number of propagators
 * (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class MostConstrained implements VariableSelector<IntVar> {

    @Override
    public IntVar getVariable(IntVar[] variables) {
        int small_idx = -1;
        int small_value = Integer.MAX_VALUE;
        for (int idx = 0; idx < variables.length; idx++) {
            int dsize = variables[idx].getDomainSize();
            int lower = variables[idx].getLB();
            if (dsize > 1 &&
                    (lower < small_value
                            || (lower == small_value && variables[idx].getNbProps() < variables[small_idx].getNbProps()))) {
                small_value = lower;
                small_idx = idx;
            }
        }
        return small_idx > -1 ? variables[small_idx] : null;
    }
}