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

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.RealVar;

import java.util.Arrays;

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
    /**
     * Gap when refuting a decision
     */
    private final double epsilon;
    /**
     * Select left range first
     */
    private final boolean leftFirst;

    /**
     * Create a real strategy which generates decision over real variables.
     * <p>
     * A real decision is like:
     * <ul>
     *     <li>left branch: X &le; v</li>
     *     <li>right branch: X &ge; v + e</li>
     * </ul>
     * where e is 'epsilon'
     * </p>
     * @param scope variables to be managed with this strategy
     * @param varselector how to select the next variable to branch on
     * @param valueIterator on to select the value
     * @param epsilon gap value for refutation
     * @param leftFirst select left range first
     */
    public RealStrategy(RealVar[] scope, VariableSelector<RealVar> varselector,
                        RealValueSelector valueIterator,
                        double epsilon, boolean leftFirst) {
        super(scope);
        this.varselector = varselector;
        this.valueIterator = valueIterator;
        if(Double.isNaN(epsilon)){
            double min = Arrays.stream(scope).mapToDouble(RealVar::getPrecision).min().getAsDouble();
            this.epsilon = min / 10;
        }else {
            this.epsilon = epsilon;
        }
        this.leftFirst = leftFirst;
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
        return variable.getModel().getSolver().getDecisionPath().makeRealDecision(variable, value, epsilon, leftFirst);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Decision getDecision() {
        RealVar variable = varselector.getVariable(vars);
        return computeDecision(variable);
    }
}
