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
package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.search.strategy.selectors.IVarSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;

/**
 * @deprecated : search strategies for reals should be done through the {@link IRealStrategyFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
public class RealStrategyFactory {

    RealStrategyFactory() {}

    /**
     * @deprecated : use {@link IRealStrategyFactory#customRealSearch(VariableSelector, RealValueSelector, RealVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealStrategy custom(VariableSelector<RealVar> varS, RealValueSelector valS, RealVar... rvars) {
        return new RealStrategy(rvars, varS, valS);
    }

    /**
     * @deprecated : use {@link IRealStrategyFactory#realSearch(RealVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealStrategy cyclic_middle(RealVar... reals) {
        return custom(cyclic(), mid_value_selector(), reals);
    }

    /**
     * @deprecated : use {@link IVarSelectorFactory#nextVarSelector()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static VariableSelector<RealVar> cyclic() {
        return new Cyclic();
    }

    /**
     * @deprecated : use {@link IRealStrategyFactory#midRValSelector()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealValueSelector mid_value_selector() {
        return new RealDomainMiddle();
    }

    /**
     * @deprecated : use {@link IRealStrategyFactory#minRValSelector()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealValueSelector min_value_selector() {
        return new RealDomainMiddle();
    }

    /**
     * @deprecated : use {@link IRealStrategyFactory#maxRValSelector()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealValueSelector max_value_selector() {
        return new RealDomainMax();
    }
}
