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
import org.chocosolver.solver.variables.RealVar;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 */
abstract class AbstractRealObjManager extends AbstractObjManager<RealVar> {

    private static final long serialVersionUID = 8038511375883592639L;

    public AbstractRealObjManager(AbstractObjManager<RealVar> objman) {
        super(objman);
    }

    public AbstractRealObjManager(RealVar objective, ResolutionPolicy policy, Number precision) {
        super(objective, policy, precision);
        double prec = Math.abs(precision.doubleValue());
        bestProvedLB = objective.getLB() - prec;
        bestProvedUB = objective.getUB() + prec;
    }

    @Override
    public synchronized boolean updateBestLB(Number lb) {
        if (bestProvedLB.doubleValue() < lb.doubleValue()) {
            bestProvedLB = lb;
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updateBestUB(Number ub) {
        if (bestProvedUB.doubleValue() > ub.doubleValue()) {
            bestProvedUB = ub;
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBestSolution() {
        assert objective.isInstantiated();
        return updateBestSolution(objective.getUB());
    }

    @Override
    public void setStrictDynamicCut() {
        cutComputer = (Number n) -> n.doubleValue() + precision.doubleValue();
    }

    private int getNbDecimals() {
        int dec = 0;
        double p = precision.doubleValue();
        while ((int) p <= 0 && dec <= 12) {
            dec++;
            p *= 10;
        }
        return dec;
    }

    @Override
    public void resetBestBounds() {
        double prec = Math.abs(precision.doubleValue());
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
    public boolean updateBestSolution(Number n) {
        return updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(bestProvedLB.doubleValue(), cutComputer.apply(bestProvedUB).doubleValue(), this);
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
    public boolean updateBestSolution(Number n) {
        return updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(cutComputer.apply(bestProvedLB).doubleValue(), bestProvedUB.doubleValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedLB;
    }
}