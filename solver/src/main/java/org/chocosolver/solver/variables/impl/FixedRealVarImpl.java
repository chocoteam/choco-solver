/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.scheduler.RealEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.RealInterval;

import java.util.List;
import java.util.TreeSet;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 13/05/2016.
 */
public class FixedRealVarImpl extends AbstractVariable implements RealVar {

    /**
     * The lower bound of this interval
     */
    double lb;
    /**
     * The upper bound of this interval
     */
    double ub;

    /**
     * Create a fixed interval variable.
     *
     * @param name  name of the variable
     * @param value a double value
     * @param model model which declares this variable
     */
    public FixedRealVarImpl(String name, double value, Model model) {
        this(name, value, value, model);
    }

    /**
     * Create a fixed interval variable.
     *
     * @param name  name of the variable
     * @param lb a double value
     * @param ub a double value
     * @param model model which declares this variable
     */
    public FixedRealVarImpl(String name, double lb, double ub, Model model) {
        super(name, model);
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public double getLB() {
        return lb;
    }

    @Override
    public double getUB() {
        return ub;
    }

    @Override
    public void intersect(double l, double u, ICause cause) throws ContradictionException {

    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        if (value > this.lb) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        if (value < this.ub) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        if (lowerbound > lb || upperbound < ub) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public double getPrecision() {
        return Double.MIN_VALUE;
    }

    @Override
    public void silentlyAssign(RealInterval bounds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void silentlyAssign(double lb, double ub) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInstantiated() {
        return true;
    }

    @Override
    public IDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {

    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {

    }

    @Override
    public int getTypeAndKind() {
        return Variable.REAL | Variable.CSTE;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new RealEvtScheduler();
    }

    @Override
    public void tighten() {
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
    }

    @Override
    public void collectVariables(TreeSet<RealVar> set) {
        // void
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
