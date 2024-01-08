/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 */
public abstract class AbstractIntObjManager implements IObjectiveManager<IntVar> {

    private static final long serialVersionUID = 5539060355541720114L;

    /**
     * The variable to optimize
     **/
    transient protected final IntVar objective;

    /**
     * Define how should the objective be optimize
     */
    protected final ResolutionPolicy policy;

    /**
     * define the precision to consider a variable as instantiated
     **/
    protected final int precision;

    /**
     * best lower bound found so far
     **/
    protected int bestProvedLB;

    /**
     * best upper bound found so far
     **/
    protected int bestProvedUB;

    /**
     * Define how the cut should be updated when posting the cut
     **/
    transient protected IntUnaryOperator cutComputer = n -> n; // walking cut by default

    public AbstractIntObjManager(AbstractIntObjManager objman) {
        objective = objman.objective;
        policy = objman.policy;
        precision = objman.precision;
        bestProvedLB = objman.bestProvedLB;
        bestProvedUB = objman.bestProvedUB;
        cutComputer = objman.cutComputer;
    }

    public AbstractIntObjManager(IntVar objective, ResolutionPolicy policy, int precision) {
        assert Objects.nonNull(objective);
        this.objective = objective;
        assert Objects.nonNull(policy);
        this.policy = policy;
        this.precision = precision;
        bestProvedLB = objective.getLB() - 1;
        bestProvedUB = objective.getUB() + 1;
    }


    @Override
    public final IntVar getObjective() {
        return objective;
    }

    @Override
    public final ResolutionPolicy getPolicy() {
        return policy;
    }

    @Override
    public final Number getBestLB() {
        return bestProvedLB;
    }

    @Override
    public final Number getBestUB() {
        return bestProvedUB;
    }

    @Override
    public final void setCutComputer(Function<Number, Number> cutComputer) {
        this.cutComputer = operand -> cutComputer.apply(operand).intValue();
    }

    public final void setCutComputer(IntUnaryOperator cutComputer) {
            this.cutComputer = cutComputer;
        }

    @Override
    public void setStrictDynamicCut() {
        cutComputer = n -> n + precision;
    }

    @Override
    public final void setWalkingDynamicCut() {
        cutComputer = n -> n;
    }

    public synchronized boolean updateBestLB(int lb) {
        if (bestProvedLB < lb) {
            bestProvedLB = lb;
            return true;
        }
        return false;
    }

    public synchronized boolean updateBestUB(int ub) {
        if (bestProvedUB > ub) {
            bestProvedUB = ub;
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBestSolution(Number n) {
        return updateBestSolution(n.intValue());
    }

    /**
     * Informs the manager that a new solution has been found
     */
    public abstract boolean updateBestSolution(int n);

    @Override
    public boolean updateBestSolution() {
        if (!objective.isInstantiated()) {
            throw new SolverException(
                    "objective variable (" + objective + ") is not instantiated on solution. Check constraints and/or decision variables.");
        }
        return updateBestSolution(objective.getValue());
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
    public boolean updateBestSolution(int n) {
        return updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(bestProvedLB, cutComputer.applyAsInt(bestProvedUB), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedUB;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        objective.intersectLit(IntIterableRangeSet.MIN, bestProvedUB - 1, explanation);
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
    public boolean updateBestSolution(int n) {
        return updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(cutComputer.applyAsInt(bestProvedLB), bestProvedUB, this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedLB;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        objective.intersectLit(bestProvedLB + 1, IntIterableRangeSet.MAX, explanation);
    }
}