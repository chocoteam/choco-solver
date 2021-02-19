/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.IntMap;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 26/02/2020.
 */
public abstract class AbstractCriterionBasedStrategy extends AbstractStrategy<IntVar> {

    /**
     * Randomness to break ties
     */
    private java.util.Random random;
    /***
     * Pointer to the last uninstantiated variable
     */
    private IStateInt last;
    /**
     * The way value is selected for a given variable
     */
    private IntValueSelector valueSelector;
    /**
     * Temporary. Stores index of variables with the same (best) score
     */
    private TIntArrayList bests = new TIntArrayList();

    /**
     * Kind of duplicate of pid2ari to limit calls of backtrackable objects
     */
    IntMap pid2arity;

    public AbstractCriterionBasedStrategy(IntVar[] vars, long seed,
                                          IntValueSelector valueSelector) {
        super(vars);
        this.random = new java.util.Random(seed);
        this.valueSelector = valueSelector;
        this.last = vars[0].getModel().getEnvironment().makeInt(vars.length - 1);
        pid2arity = new IntMap(vars[0].getModel().getCstrs().length * 3 / 2 + 1, -1);
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar best = null;
        bests.resetQuick();
        pid2arity.clear();
        double w = 0.;
        int to = last.get();
        for (int idx = 0; idx <= to; idx++) {
            int dsize = vars[idx].getDomainSize();
            if (dsize > 1) {
                double weight = weight(vars[idx]);
                if (w < weight) {
                    bests.resetQuick();
                    bests.add(idx);
                    w = weight;
                } else if (w == weight) {
                    bests.add(idx);
                }
            } else {
                // swap
                IntVar tmp = vars[to];
                vars[to] = vars[idx];
                vars[idx] = tmp;
                idx--;
                to--;
            }
        }
        last.set(to);
        if (bests.size() > 0) {
            int currentVar = bests.get(random.nextInt(bests.size()));
            best = vars[currentVar];
        }
        return computeDecision(best);
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        int currentVal = valueSelector.selectValue(variable);
        return variable.getModel().getSolver().getDecisionPath()
                .makeIntDecision(variable, DecisionOperatorFactory
                        .makeIntEq(), currentVal);
    }

    protected abstract double weight(IntVar v);

    protected final int futVars(Propagator prop) {
        int pid = prop.getId();
        int futVars = pid2arity.get(pid);
        if (futVars == -1) {
            futVars = computeFutvars(prop, pid);
        }
        return futVars;
    }

    private int computeFutvars(Propagator prop, int pid) {
        int futVars = 0;
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (!prop.getVar(i).isInstantiated()) {
                if (++futVars > 1) {
                    break;
                }
            }
        }
        pid2arity.put(pid, futVars);
        return futVars;
    }
}
