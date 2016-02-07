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

import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.search.strategy.selectors.SetValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxDelta;
import org.chocosolver.solver.search.strategy.selectors.variables.MinDelta;
import org.chocosolver.solver.search.strategy.strategy.SetStrategy;
import org.chocosolver.solver.variables.SetVar;

/**
 * @deprecated : search strategies for sets should be done through {@link Resolver}
 * which implements {@link ISetStrategyFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
public class SetStrategyFactory {

    SetStrategyFactory() {
    }

    /**
     * @deprecated : use {@link Resolver#setVarSearch(VariableSelector, SetValueSelector, boolean, SetVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetStrategy custom(VariableSelector<SetVar> varS, SetValueSelector valS, boolean enforceFirst, SetVar... sets) {
        return new SetStrategy(sets, varS, valS, enforceFirst);
    }

    /**
     * @deprecated : use {@link Resolver#setVarSearch(VariableSelector, SetValueSelector, boolean, SetVar...)}
     * and create variable/value selectors directly instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetStrategy force_first(SetVar... sets) {
        return custom(new InputOrder<>(), new SetDomainMin(), true, sets);
    }

    /**
     * @deprecated : use {@link Resolver#setVarSearch(VariableSelector, SetValueSelector, boolean, SetVar...)}
     * and create variable/value selectors directly instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetStrategy remove_first(SetVar... sets) {
        return custom(new InputOrder<>(), new SetDomainMin(), false, sets);
    }

    /**
     * @deprecated : use {@link Resolver#setVarSearch(SetVar...)}  instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetStrategy force_minDelta_first(SetVar... sets) {
        return custom(new MinDelta(), new SetDomainMin(), true, sets);
    }

    /**
     * @deprecated : use {@link Resolver#setVarSearch(VariableSelector, SetValueSelector, boolean, SetVar...)}
     * and create variable/value selectors directly instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetStrategy force_maxDelta_first(SetVar... sets) {
        return custom(new MaxDelta(), new SetDomainMin(), true, sets);
    }
}
