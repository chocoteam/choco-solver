/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.objects.RealInterval;

import java.util.List;
import java.util.TreeSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/01/2020
 */
public class RealIntervalConstant implements CArExpression {

    protected final double lb;
    protected final double ub;

    public RealIntervalConstant(RealInterval interval) {
        this.lb = interval.getLB();
        this.ub = interval.getUB();
    }

    public RealIntervalConstant(double l, double u) {
        this.lb = l;
        this.ub = u;
    }

    public String toString() {
        return "[" + lb + "," + ub + "]";
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
    public void intersect(double l, double u, ICause cause) {

    }

    @Override
    public Model getModel() {
        return null;
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
        return false;
    }

    @Override
    public void init() {
        // void
    }

    @Override
    public RealVar realVar(double precision) {
        return null;
    }
}
