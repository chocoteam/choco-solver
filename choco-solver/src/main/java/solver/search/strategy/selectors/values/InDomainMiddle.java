/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy.selectors.values;

import solver.search.strategy.selectors.InValueIterator;
import solver.variables.IntVar;

/**
 * Assigns the value in the variable's domain closest to the mean of its current bounds.
 * <br/>
 * It computes the mean of the variable's domain. Then it checks if the mean is contained in the domain, otherwise
 * modify its value by adding a term of the infinite serie : 0 + 1 - 2  + 3 - 4 ... in order to find the closest
 * mean of the current domain's bounds.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class InDomainMiddle implements InValueIterator {

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        if (var.hasEnumeratedDomain()) {
            int low = var.getLB();
            int upp = var.getUB();
            int mean = (low + upp) / 2;
            if (!var.contains(mean)) {
                int lb = var.previousValue(mean);
                int ub = var.previousValue(mean);
                if ((mean - lb) < (ub - mean)) {
                    return lb;
                } else {
                    return ub;
                }
            }
            return mean;
        } else {
            return var.getLB();
        }
    }
}