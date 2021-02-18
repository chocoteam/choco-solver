/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
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
    public Decision<IntVar> getDecision() {
        IntVar variable = variableSelector.getVariable(vars);
        return computeDecision(variable);
    }
}
