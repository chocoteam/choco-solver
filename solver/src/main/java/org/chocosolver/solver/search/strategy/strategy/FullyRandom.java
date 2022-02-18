/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.IntVar;


/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 26/04/2016.
 */
public class FullyRandom extends IntStrategy {

    @SuppressWarnings("unchecked")
    private final DecisionOperator<IntVar>[] dops = new DecisionOperator[]{
            DecisionOperatorFactory.makeIntEq(),
            DecisionOperatorFactory.makeIntNeq(),
            DecisionOperatorFactory.makeIntSplit(),
            DecisionOperatorFactory.makeIntReverseSplit()};
    /**
     * Random
     */
    private final java.util.Random rnd;

    public FullyRandom(IntVar[] scope, long seed) {
        super(scope, new Random<>(seed), new IntDomainRandom(seed));
        this.rnd = new java.util.Random(seed);
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        int value = this.valueSelector.selectValue(variable);
        DecisionOperator<IntVar> dop;
        dop = dops[rnd.nextInt(4)];
        if(dop == dops[2] && value == variable.getUB()){
            dop = dops[3];
        }else if (dop == dops[3] && value == variable.getLB()){
            dop = dops[2];
        }

        if (!variable.hasEnumeratedDomain()) {
            if(value != variable.getLB() && value != variable.getUB()){
                dop = dops[2+ rnd.nextInt(2)];
            }
        }
        return variable.getModel().getSolver().getDecisionPath().makeIntDecision(variable, dop, value);
    }

}
