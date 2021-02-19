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
import org.chocosolver.solver.search.strategy.selectors.values.SetValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.SetVar;

/**
 * Strategy for branching on set variables
 *
 * @author Jean-Guillaume Fages
 * @since 6/10/13
 */
public class SetStrategy extends AbstractStrategy<SetVar> {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * How a variable is selected
     */
    protected VariableSelector<SetVar> varSelector;
    /**
     * How a value is selected
     */
    protected SetValueSelector valSelector;
    /**
     * A decision operator
     */
    protected DecisionOperator<SetVar> operator;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Generic strategy to branch on set variables
     *
     * @param scope        SetVar array to branch on
     * @param varS         variable selection strategy
     * @param valS         integer  selection strategy
     * @param enforceFirst branching order true = enforce first; false = remove first
     */
    public SetStrategy(SetVar[] scope, VariableSelector<SetVar> varS, SetValueSelector valS, boolean enforceFirst) {
        super(scope);
        varSelector = varS;
        valSelector = valS;
        operator = enforceFirst ? DecisionOperatorFactory.makeSetForce(): DecisionOperatorFactory.makeSetRemove();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean init(){
        return true;
    }

    @Override
    public Decision<SetVar> getDecision() {
        SetVar variable = varSelector.getVariable(vars);
        return computeDecision(variable);
    }

    @Override
    public Decision<SetVar> computeDecision(SetVar s) {
        if (s == null) {
            return null;
        }
        assert !s.isInstantiated();
        return s.getModel().getSolver().getDecisionPath().makeSetDecision(s, operator, valSelector.selectValue(s));
    }
}
