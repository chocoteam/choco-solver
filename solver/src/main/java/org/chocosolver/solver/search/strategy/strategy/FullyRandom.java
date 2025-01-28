/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;


/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 26/04/2016.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class FullyRandom extends AbstractStrategy<Variable> {

    @SuppressWarnings("unchecked")
    private final DecisionOperator<IntVar>[] intdops = new DecisionOperator[]{
            DecisionOperatorFactory.makeIntEq(),
            DecisionOperatorFactory.makeIntNeq(),
            DecisionOperatorFactory.makeIntSplit(),
            DecisionOperatorFactory.makeIntReverseSplit()};

    private final DecisionOperator<SetVar>[] setdops = new DecisionOperator[]{
            DecisionOperatorFactory.makeSetRemove(),
            DecisionOperatorFactory.makeSetForce()};
    /**
     * Random
     */
    private final java.util.Random rnd;

    private final VariableSelector<Variable> variableSelector;


    public FullyRandom(Variable[] scope, long seed) {
        super(scope);
        this.rnd = new java.util.Random(seed);
        variableSelector = new Random<>(seed);
    }

    @Override
    public Decision getDecision() {
        return computeDecision(variableSelector.getVariable(vars));
    }

    @Override
    public Decision computeDecision(Variable variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        switch (variable.getTypeAndKind() & Variable.KIND) {
            case Variable.INT:
            case Variable.BOOL:
                IntVar ivar = (IntVar) variable;
                int value;
                if (ivar.hasEnumeratedDomain()) {
                    int i = rnd.nextInt(ivar.getDomainSize());
                    value = ivar.getLB();
                    while (i > 0) {
                        value = ivar.nextValue(value);
                        i--;
                    }
                } else {
                    value = rnd.nextBoolean() ? ivar.getLB() : ivar.getUB();
                }
                DecisionOperator<IntVar> dop;
                dop = intdops[rnd.nextInt(4)];
                if (dop == intdops[2] && value == ivar.getUB()) {
                    dop = intdops[3];
                } else if (dop == intdops[3] && value == ivar.getLB()) {
                    dop = intdops[2];
                }

                if (!ivar.hasEnumeratedDomain()) {
                    if (value != ivar.getLB() && value != ivar.getUB()) {
                        dop = intdops[2 + rnd.nextInt(2)];
                    }
                }
                return variable.getModel().getSolver().getDecisionPath().makeIntDecision(ivar, dop, value);
            case Variable.SET:
                SetVar svar = (SetVar) variable;
                ISetIterator iter = svar.getUB().newIterator();
                int i = rnd.nextInt(svar.getUB().size() - svar.getLB().size());
                // randomly select an element from the iterator that is not in svar.getLB()
                int elt = iter.nextInt();
                while (svar.getLB().contains(elt) && iter.hasNext()) {
                    elt = iter.nextInt();
                }
                assert !svar.getLB().contains(elt);
                while (i > 0 && iter.hasNext()) {
                    elt = iter.nextInt();
                    while (svar.getLB().contains(elt) && iter.hasNext()) {
                        elt = iter.nextInt();
                    }
                    i--;
                }
                return variable.getModel().getSolver().getDecisionPath().makeSetDecision(svar, setdops[rnd.nextInt(2)], elt);
            default:
                throw new UnsupportedOperationException("Unknown variable type: " + variable.getTypeAndKind());
        }
    }

}
