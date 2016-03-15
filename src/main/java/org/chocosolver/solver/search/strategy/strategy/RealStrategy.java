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
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.variables.RealVar;

/**
 * Define a strategy based on {@link RealVar}.
 * It defines how a variable is selected to be part of the next decision, and which value from its domain is selected too.
 * Then, the decision will be {@code var} &le; {value}.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class RealStrategy extends AbstractStrategy<RealVar> {

    /**
     * How a variable is selected
     */
    VariableSelector<RealVar> varselector;
    /**
     * How a value is selected
     */
    RealValueSelector valueIterator;

    public RealStrategy(RealVar[] scope, VariableSelector<RealVar> varselector, RealValueSelector valueIterator) {
        super(scope);
        this.varselector = varselector;
        this.valueIterator = valueIterator;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public Decision<RealVar> computeDecision(RealVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        double value = valueIterator.selectValue(variable);
        return variable.getModel().getSolver().getDecisionPath().makeRealDecision(variable, value);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Decision getDecision() {
        RealVar variable = varselector.getVariable(vars);
        return computeDecision(variable);
    }
}
