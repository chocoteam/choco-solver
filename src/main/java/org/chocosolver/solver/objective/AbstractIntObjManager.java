/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 *
 */
abstract class AbstractIntObjManager extends AbstractObjManager<IntVar> {

    private static final long serialVersionUID = 5539060355541720114L;

    public AbstractIntObjManager(AbstractIntObjManager objman) {
        super(objman);
    }

    public AbstractIntObjManager(IntVar objective, ResolutionPolicy policy, Number precision) {
        super(objective, policy, precision);
        bestProvedLB = objective.getLB() - 1;
        bestProvedUB = objective.getUB() + 1;
    }

    @Override
    public synchronized void updateBestLB(Number lb) {
        if (bestProvedLB.intValue() < lb.intValue()) {
            bestProvedLB = lb;
        }
    }

    @Override
    public synchronized void updateBestUB(Number ub) {
        if (bestProvedUB.intValue() > ub.intValue()) {
            bestProvedUB = ub;
        }
    }

    @Override
    public void updateBestSolution() {
        assert objective.isInstantiated();
        updateBestSolution(objective.getValue());
    }

    @Override
    public void setStrictDynamicCut() {
        cutComputer = (Number n) -> n.intValue() + precision.intValue();
    }

    @Override
    public void resetBestBounds() {
        bestProvedLB = objective.getLB() - 1;
        bestProvedUB = objective.getUB() + 1;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return isOptimization() && ruleStore.addBoundsRule(objective);
    }

    @Override
    public String toString() {
        return String.format("%s %s = %d", policy, objective == null ? "?" : this.objective.getName(), getBestSolutionValue().intValue());
    }

}

class MinIntObjManager extends AbstractIntObjManager {

    private static final long serialVersionUID = 6963161492115613388L;

    @SuppressWarnings("unused") // use for copy by introspection
    public MinIntObjManager(MinIntObjManager objman) {
        super(objman);
    }

    public MinIntObjManager(IntVar objective) {
        super(objective, ResolutionPolicy.MINIMIZE, -1);
    }

    @Override
    public void updateBestSolution(Number n) {
        updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(bestProvedLB.intValue(), cutComputer.apply(bestProvedUB).intValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedUB;
    }

}

class MaxIntObjManager extends AbstractIntObjManager {

    private static final long serialVersionUID = -245398442954059838L;

    @SuppressWarnings("unused") // use for copy by introspection
    public MaxIntObjManager(MaxIntObjManager objman) {
        super(objman);
    }

    public MaxIntObjManager(IntVar objective) {
        super(objective, ResolutionPolicy.MAXIMIZE, 1);
    }

    @Override
    public void updateBestSolution(Number n) {
        updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(cutComputer.apply(bestProvedLB).intValue(), bestProvedUB.intValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedLB;
    }
}
