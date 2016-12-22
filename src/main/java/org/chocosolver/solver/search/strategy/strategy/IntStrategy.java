/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

/**
 * Class to perform branching decisions over integer variables
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 2 juil. 2010
 */
public class IntStrategy extends AbstractStrategy<IntVar> {

	// Search strategy parameters
	/**
	 * How a variable is selected
	 */
	private VariableSelector<IntVar> variableSelector;
    /**
     * How a value is selected
     */
	protected IntValueSelector valueSelector;
    /**
     * The decision operator
     */
	private DecisionOperator<IntVar> decisionOperator;

	/**
	 * Creates a search strategy which selects a variable X and a value V to perform
	 * the decision X = V
	 *
	 * BEWARE: if the variable domain is not enumerated, the value V should be a domain bound.
	 * Otherwise, the decision cannot be negated, and the search may loop infinitely.
	 *
	 * @param scope			defines which variables to branch on
	 * @param varSelector	defines how to select the next variable to branch on
	 * @param valSelector	defines how to select the value involved in the branching decision
	 */
    public IntStrategy(IntVar[] scope, VariableSelector<IntVar> varSelector, IntValueSelector valSelector) {
        this(scope, varSelector, valSelector,DecisionOperatorFactory.makeIntEq());
    }

	/**
	 * Creates a search strategy which selects a variable X and a value V to perform
	 * the decision X decOperator V
	 *
	 * This can be X <= V for instance.
	 *
	 * BEWARE: if the variable domain is not enumerated, and if the operator is either "=" or "!=",
	 * then the value V should be a domain bound. Otherwise, the decision cannot be negated,
	 * and the search may loop infinitely.
	 *
	 * @param scope			defines which variables to branch on
	 * @param varSelector	defines how to select the next variable to branch on
	 * @param valSelector	defines how to select the value involved in the branching decision
	 * @param decOperator	defines what to do in a branching decision
	 */
    public IntStrategy(IntVar[] scope, VariableSelector<IntVar> varSelector, IntValueSelector valSelector,
					   DecisionOperator<IntVar> decOperator) {
        super(scope);
        this.variableSelector = varSelector;
        this.valueSelector = valSelector;
		this.decisionOperator = decOperator;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        int value = valueSelector.selectValue(variable);
        return variable.getModel().getSolver().getDecisionPath().makeIntDecision(variable, decisionOperator, value);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Decision getDecision() {
        IntVar variable = variableSelector.getVariable(vars);
        return computeDecision(variable);
    }

    // ACCESSORS

	public VariableSelector<IntVar> getVarSelector(){
		return variableSelector;
	}

	public IntValueSelector getValSelector(){
		return valueSelector;
	}

	public DecisionOperator<IntVar> getDecisionOperator(){
		return decisionOperator;
	}
}
