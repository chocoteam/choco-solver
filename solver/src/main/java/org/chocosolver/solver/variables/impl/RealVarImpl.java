/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IStateDouble;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.solver.variables.impl.scheduler.RealEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.RealInterval;

import java.util.List;
import java.util.TreeSet;

/**
 * An implementation of RealVar, variable for continuous constraints (solved using IBEX).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealVarImpl extends AbstractVariable implements RealVar {

    private final IStateDouble LB;
    private final IStateDouble UB;
    private final double precision;

    public RealVarImpl(String name, double lb, double ub, double precision, Model model) {
        super(name, model);
        this.LB = model.getEnvironment().makeFloat(lb);
        this.UB = model.getEnvironment().makeFloat(ub);
        this.precision = precision;
    }

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public void silentlyAssign(RealInterval bounds) {
	silentlyAssign(bounds.getLB(), bounds.getUB());
    }

    @Override
    public void silentlyAssign(double lb, double ub) {
        this.LB.set(lb);
        this.UB.set(ub);
    }

    @Override
    public double getLB() {
        return LB.get();
    }

    @Override
    public double getUB() {
        return UB.get();
    }

    @Override
    public void intersect(double l, double u, ICause cause) throws ContradictionException {
        updateBounds(l, u, cause);
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        assert cause != null;
//        TODO ICause antipromo = cause;
        double old = this.getLB();
        if (old < value) {
            if (this.getUB() < value) {
//                TODO solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                this.contradiction(cause, MSG_LOW);
            } else {
                LB.set(value);
                this.notifyPropagators(RealEventType.INCLOW, cause);

//                TODO solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        assert cause != null;
//        TODO ICause antipromo = cause;
        double old = this.getUB();
        if (old > value) {
            if (this.getLB() > value) {
//                TODO solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                this.contradiction(cause, MSG_UPP);
            } else {
                UB.set(value);
                this.notifyPropagators(RealEventType.DECUPP, cause);
//                TODO solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        assert cause != null;
//        TODO ICause antipromo = cause;
        double oldlb = this.getLB();
        double oldub = this.getUB();
        if (oldlb < lowerbound || oldub > upperbound) {
            if (oldub < lowerbound || oldlb > upperbound) {
//                TODO solver.getExplainer()...
                this.contradiction(cause, MSG_BOUND);
            } else {
				RealEventType e = RealEventType.VOID;
                if (oldlb < lowerbound) {
                    LB.set(lowerbound);
                    e = RealEventType.INCLOW;
                }
                if (oldub > upperbound) {
                    UB.set(upperbound);
                    e = (e == RealEventType.INCLOW) ? RealEventType.BOUND : RealEventType.DECUPP;
                }
                this.notifyPropagators(e, cause);
//                TODO solver.getExplainer()...
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInstantiated() {
        double lb = LB.get();
        double ub = UB.get();
        return ub - lb < precision || nextValue(lb) >= ub;
    }

    private double nextValue(double x) {
        if (x < 0) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
        } else if (x == 0) {
            return Double.longBitsToDouble(1L);
        } else if (x < Double.POSITIVE_INFINITY) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
        } else {
            return x; // nextValue(infty) = infty
        }
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {
        throw new SolverException("Unable to create delta for RealVar!");
    }

    @Override
    public int getTypeAndKind() {
        return VAR | REAL;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new RealEvtScheduler();
    }

    @Override
    public String toString() {
        return String.format("%s = [%.16f .. %.16f]", name, getLB(), getUB());
    }

    @Override
    public void tighten() {
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
    }

    @Override
    public void collectVariables(TreeSet<RealVar> set) {
        set.add(this);
    }

    @Override
    public void subExps(List<CArExpression> list) {
        list.add(this);
    }

    @Override
    public boolean isolate(RealVar var, List<CArExpression> wx, List<CArExpression> wox) {
        return var == this;
    }

    @Override
    public void init() {
        // void
    }
}
