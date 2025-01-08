/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 */
abstract class AbstractRealObjManager implements IObjectiveManager<RealVar> {

    private static final long serialVersionUID = 8038511375883592639L;

    /**
     * The variable to optimize
     **/
    transient protected final RealVar objective;

    /**
     * Define how should the objective be optimize
     */
    protected final ResolutionPolicy policy;

    /**
     * define the precision to consider a variable as instantiated
     **/
    protected final double precision;

    /**
     * best lower bound found so far
     **/
    protected double bestProvedLB;

    /**
     * best upper bound found so far
     **/
    protected double bestProvedUB;

    /**
     * Define how the cut should be updated when posting the cut
     **/
    transient protected DoubleUnaryOperator cutComputer = n -> n; // walking cut by default


    public AbstractRealObjManager(AbstractRealObjManager objman) {
        objective = objman.objective;
        policy = objman.policy;
        precision = objman.precision;
        bestProvedLB = objman.bestProvedLB;
        bestProvedUB = objman.bestProvedUB;
        cutComputer = objman.cutComputer;

    }

    public AbstractRealObjManager(RealVar objective, ResolutionPolicy policy, double precision) {
        assert Objects.nonNull(objective);
        this.objective = objective;
        assert Objects.nonNull(policy);
        this.policy = policy;
        this.precision = precision;
        double prec = Math.abs(precision);
        this.bestProvedLB = objective.getLB() - prec;
        this.bestProvedUB = objective.getUB() + prec;
    }


    @Override
    public final RealVar getObjective() {
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

    public final void setCutComputer(DoubleUnaryOperator cutComputer) {
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


    public synchronized boolean updateBestLB(double lb) {
        if (bestProvedLB < lb) {
            bestProvedLB = lb;
            return true;
        }
        return false;
    }

    public synchronized boolean updateBestUB(double ub) {
        if (bestProvedUB > ub) {
            bestProvedUB = ub;
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBestSolution(Number n) {
        return updateBestSolution(n.doubleValue());
    }

    public abstract boolean updateBestSolution(double n);

    @Override
    public boolean updateBestSolution() {
        assert objective.isInstantiated();
        return updateBestSolution(objective.getUB());
    }


    private int getNbDecimals() {
        int dec = 0;
        double p = precision;
        while ((int) p <= 0 && dec <= 12) {
            dec++;
            p *= 10;
        }
        return dec;
    }

    @Override
    public void resetBestBounds() {
        double prec = Math.abs(precision);
        bestProvedLB = objective.getLB() - prec;
        bestProvedUB = objective.getUB() + prec;
    }

    @Override
    public String toString() {
        return String.format("%s %s = %." + getNbDecimals() + "f", policy, objective == null ? "?" : this.objective.getName(), getBestSolutionValue().doubleValue());
    }
}

class MinRealObjManager extends AbstractRealObjManager {

    private static final long serialVersionUID = 2409478704121834610L;

    @SuppressWarnings("unused") // use for copy by introspection
    public MinRealObjManager(MinRealObjManager objman) {
        super(objman);
    }

    public MinRealObjManager(RealVar objective, double precision) {
        super(objective, ResolutionPolicy.MINIMIZE, -precision);
    }

    @Override
    public boolean updateBestSolution(double n) {
        return updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(bestProvedLB, cutComputer.applyAsDouble(bestProvedUB), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedUB;
    }

}

class MaxRealObjManager extends AbstractRealObjManager {

    private static final long serialVersionUID = 3584094931280638616L;

    @SuppressWarnings("unused") // use for copy by introspection
    public MaxRealObjManager(MaxRealObjManager objman) {
        super(objman);
    }

    public MaxRealObjManager(RealVar objective, double precision) {
        super(objective, ResolutionPolicy.MAXIMIZE, precision);
    }

    @Override
    public boolean updateBestSolution(double n) {
        return updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(cutComputer.applyAsDouble(bestProvedLB), bestProvedUB, this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedLB;
    }
}