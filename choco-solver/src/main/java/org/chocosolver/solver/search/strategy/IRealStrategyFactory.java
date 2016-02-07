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

import org.chocosolver.solver.search.strategy.selectors.IValSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.IVarSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;

/**
 * Strategies over real variables
 * Just there to simplify strategies creation.
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public interface IRealStrategyFactory extends IVarSelectorFactory, IValSelectorFactory{

    /**
     * Generic strategy to branch on real variables, based on domain splitting
     * @param varS  variable selection strategy
     * @param valS  strategy to select where to split domains
     * @param rvars RealVar array to branch on
     * @return a strategy to instantiate reals
     */
    default RealStrategy customRealSearch(VariableSelector<RealVar> varS, RealValueSelector valS, RealVar... rvars) {
        return new RealStrategy(rvars, varS, valS);
    }

    /**
     * strategy to branch on real variables by choosing sequentially the next variable domain
     * to split in two, wrt the middle value
     * @param reals variables to branch on
     * @return a strategy to instantiate real variables
     */
    default RealStrategy realSearch(RealVar... reals) {
        return customRealSearch(nextVarSelector(), midRValSelector(), reals);
    }
}
