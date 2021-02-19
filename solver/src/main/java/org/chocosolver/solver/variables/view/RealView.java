/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.impl.scheduler.RealEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.RealInterval;

import java.util.List;
import java.util.TreeSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 20/07/12
 */
public class RealView extends AbstractVariable implements IView, RealVar {

    protected final IntVar var;

    protected final double precision;

    public RealView(IntVar var, double precision) {
        super("(real)" + var.getName(), var.getModel());
        this.var = var;
        this.precision = precision;
        this.var.subscribeView(this);
    }

    @Override
    public IntVar getVariable() {
        return var;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new RealEvtScheduler();
    }

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        throw new UnsupportedOperationException("RealView does not support explanation.");
    }

    @Override
    public void explain(int p, ExplanationForSignedClause clause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "(real)" + var.toString();
    }

    ///////////// SERVICES REQUIRED FROM CAUSE ////////////////////////////

    @Override
    public double getLB() {
        return var.getLB();
    }

    @Override
    public double getUB() {
        return var.getUB();
    }

    @Override
    public void intersect(double l, double u, ICause cause) throws ContradictionException {
        var.updateBounds(
                (int) Math.ceil(l),
                (int) Math.floor(u),
                cause
        );
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        if (var.updateLowerBound((int) Math.ceil(value - precision), this)) {
            super.notifyPropagators(RealEventType.INCLOW, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        if (var.updateUpperBound((int) Math.floor(value + precision), this)) {
            super.notifyPropagators(RealEventType.INCLOW, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        int c = 0;
        c += (var.updateLowerBound((int) Math.ceil(lowerbound - precision), this) ? 1 : 0);
        c += (var.updateUpperBound((int) Math.floor(upperbound + precision), this) ? 2 : 0);
        switch (c) {
            case 3:
                super.notifyPropagators(RealEventType.BOUND, cause);
                return true;
            case 2:
                super.notifyPropagators(RealEventType.DECUPP, cause);
                return true;
            case 1:
                super.notifyPropagators(RealEventType.INCLOW, cause);
                return true;
            default: //cas 0;
                return false;
        }
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
    public void silentlyAssign(double l, double u) {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }

    @Override
    public boolean isInstantiated() {
        return var.isInstantiated();
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {
    }

    @Override
    public void notify(IEventType event) throws ContradictionException {
        if (event != IntEventType.REMOVE) { // there is no real event matching remove value
            super.notifyPropagators(transformEvent((IntEventType) event), this);
        }
    }

    public IEventType transformEvent(IntEventType evt) {
        switch (evt){
            case REMOVE:
                throw new UnsupportedOperationException("Cannot transform REMOVE event from int to real");
            case INCLOW:
                return RealEventType.INCLOW;
            case DECUPP:
                return RealEventType.DECUPP;
            default:
            case INSTANTIATE:
            case BOUND:
                return RealEventType.BOUND;
        }
    }

    @Override
    public int getTypeAndKind() {
        return VIEW | REAL;
    }

    @Override
    public void tighten() {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }

    @Override
    public void collectVariables(TreeSet<RealVar> set) {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }

    @Override
    public void subExps(List<CArExpression> list) {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }

    @Override
    public boolean isolate(RealVar var, List<CArExpression> wx, List<CArExpression> wox) {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Consider using a constraint instead of a view. See: model.eq(RealVar, IntVar)");
    }
}
