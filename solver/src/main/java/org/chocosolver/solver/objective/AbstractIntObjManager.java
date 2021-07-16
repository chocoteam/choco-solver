/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.function.Consumer;

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
    public synchronized boolean updateBestLB(Number lb) {
        if (bestProvedLB.intValue() < lb.intValue()) {
            bestProvedLB = lb;
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updateBestUB(Number ub) {
        if (bestProvedUB.intValue() > ub.intValue()) {
            bestProvedUB = ub;
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBestSolution() {
        if(!objective.isInstantiated()) {
            throw new SolverException(
                "objective variable (" + objective + ") is not instantiated on solution. Check constraints and/or decision variables.");
        }
        return updateBestSolution(objective.getValue());
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
    public String toString() {
        return String.format("%s %s = %d", policy, objective == null ? "?" : this.objective.getName(), getBestSolutionValue().intValue());
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        action.accept(objective);
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
    public boolean updateBestSolution(Number n) {
        return updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(bestProvedLB.intValue(), cutComputer.apply(bestProvedUB).intValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedUB;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        objective.intersectLit(IntIterableRangeSet.MIN, bestProvedUB.intValue() - 1, explanation);
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
    public boolean updateBestSolution(Number n) {
        return updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(cutComputer.apply(bestProvedLB).intValue(), bestProvedUB.intValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedLB;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        objective.intersectLit(bestProvedLB.intValue() + 1, IntIterableRangeSet.MAX, explanation);
    }
}