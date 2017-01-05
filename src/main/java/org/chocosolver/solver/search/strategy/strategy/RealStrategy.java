/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
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
    private VariableSelector<RealVar> varselector;
    /**
     * How a value is selected
     */
    private RealValueSelector valueIterator;

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
