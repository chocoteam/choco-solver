/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.memory.IStateDouble;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.objects.RealInterval;
import org.chocosolver.util.tools.RealUtils;
import org.chocosolver.util.tools.VariableUtils;

import java.util.List;
import java.util.TreeSet;

/**
 * Binary continuous arithmetic expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class BiCArExpression implements CArExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;

    /**
     * Lazy creation of the underlying variable
     */
    RealVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    Operator op;

    /**
     * The first expression this expression relies on
     */
    private final CArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private final CArExpression e2;

    IStateDouble l;
    IStateDouble u;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiCArExpression(Operator op, CArExpression e1, CArExpression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
        this.model = e1.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public RealVar realVar(double p) {
        if (me == null) {
            RealVar v1 = e1.realVar(p);
            RealVar v2 = e2.realVar(p);
            double[] bounds;
            switch (op) {

                case ADD:
                    bounds = VariableUtils.boundsForAddition(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}+{2}", me, v1, v2).post();
                    break;
                case SUB:
                    bounds = VariableUtils.boundsForSubstraction(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}-{2}", me, v1, v2).post();
                    break;
                case MUL:
                    bounds = VariableUtils.boundsForMultiplication(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}*{2}", me, v1, v2).post();
                    break;
                case DIV:
                    bounds = VariableUtils.boundsForDivision(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}={1}/{2}", me, v1, v2).post();
                    break;
                case POW:
                    if (isIntegerConstant(v2)) {
                        // See issue: #702
                        int exponent = (int) v2.getLB();
                        bounds = VariableUtils.boundsForPow(v1, exponent);
                        me = model.realVar(bounds[0], bounds[1], p);
                        model.realIbexGenericConstraint("{0}={1}^" + exponent, me, v1).post();
                    } else {
                        bounds = VariableUtils.boundsForPow(v1, v2);
                        me = model.realVar(bounds[0], bounds[1], p);
                        model.realIbexGenericConstraint("{0}={1}^{2}", me, v1, v2).post();
                    }
                    break;
                case MIN:
                    bounds = VariableUtils.boundsForMinimum(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}=min({1},{2})", me, v1, v2).post();
                    break;
                case MAX:
                    bounds = VariableUtils.boundsForMaximum(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}=max({1},{2})", me, v1, v2).post();
                    break;
                case ATAN2:
                    bounds = VariableUtils.boundsForAtan2(v1, v2);
                    me = model.realVar(bounds[0], bounds[1], p);
                    model.realIbexGenericConstraint("{0}=atan2({1},{2})", me, v1, v2).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public void tighten() {
        RealInterval res;
        switch (op) {
            case ADD:
                res = RealUtils.add(e1, e2);
                break;
            case SUB:
                res = RealUtils.sub(e1, e2);
                break;
            case MUL:
                res = RealUtils.mul(e1, e2);
                break;
            case DIV:
                res = RealUtils.odiv(e1, e2);
                break;
            case MIN:
                res = new RealIntervalConstant(
                        Math.min(e1.getLB(), e2.getLB()),
                        Math.min(e1.getUB(), e2.getUB())
                );
                break;
            case MAX:
                res = new RealIntervalConstant(
                        Math.max(e1.getLB(), e2.getLB()),
                        Math.max(e1.getUB(), e2.getUB())
                );
                break;
            case POW:
            case ATAN2:
            default:
                throw new UnsupportedOperationException("Equation does not support " + op.name()+". Consider using Ibex instead.");
        }
        l.set(res.getLB());
        u.set(res.getUB());
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
        switch (op) {
            case ADD:
                e1.intersect(RealUtils.sub(this, e2), cause);
                e2.intersect(RealUtils.sub(this, e1), cause);
                break;
            case SUB:
                e1.intersect(RealUtils.add(this, e2), cause);
                e2.intersect(RealUtils.sub(e1, this), cause);
                break;
            case MUL:
                e1.intersect(RealUtils.odiv_wrt(this, e2, e1), cause);
                e2.intersect(RealUtils.odiv_wrt(this, e1, e2), cause);
                break;
            case DIV:
                e1.intersect(RealUtils.mul(this, e2), cause);
                e2.intersect(RealUtils.odiv_wrt(e1, this, e2), cause);
                break;
            case MIN:
                e1.intersect(this.getLB(), RealUtils.nextFloat(e1.getUB()), cause);
                e2.intersect(this.getLB(), RealUtils.nextFloat(e2.getUB()), cause);
                if (e2.getLB() > this.getUB()) {
                    e1.intersect(RealUtils.prevFloat(e1.getLB()), this.getUB(), cause);
                }
                if (e1.getLB() > this.getUB()) {
                    e2.intersect(RealUtils.prevFloat(e2.getLB()), this.getUB(), cause);
                }
                break;
            case MAX:
                e1.intersect(RealUtils.prevFloat(e1.getLB()), this.getUB(), cause);
                e2.intersect(RealUtils.prevFloat(e2.getLB()), this.getUB(), cause);
                if (e2.getUB() < this.getLB()) {
                    e1.intersect(this.getLB(), RealUtils.nextFloat(e1.getUB()), cause);
                }
                if (e1.getUB() < this.getLB()) {
                    e2.intersect(this.getLB(), RealUtils.nextFloat(e2.getUB()), cause);
                }
                break;
            case POW:
            case ATAN2:
            default:
                throw new UnsupportedOperationException("Equation does not support " + op.name()+". Consider using Ibex instead.");
        }
    }

    @Override
    public void collectVariables(TreeSet<RealVar> set) {
        e1.collectVariables(set);
        e2.collectVariables(set);
    }

    @Override
    public void subExps(List<CArExpression> list) {
        e1.subExps(list);
        e2.subExps(list);
        list.add(this);
    }

    @Override
    public boolean isolate(RealVar var, List<CArExpression> wx, List<CArExpression> wox) {
        boolean dependsOnX = e1.isolate(var, wx, wox) | e2.isolate(var, wx, wox);
        if (dependsOnX){
            wx.add(this);
        } else{
            wox.add(this);
        }
        return dependsOnX;
    }

    @Override
    public void init() {
        if(l == null && u == null) {
            l = model.getEnvironment().makeFloat(Double.NEGATIVE_INFINITY);
            u = model.getEnvironment().makeFloat(Double.POSITIVE_INFINITY);
        }
        e1.init();
        e2.init();
    }

    @Override
    public double getLB() {
        return l.get();
    }

    @Override
    public double getUB() {
        return u.get();
    }

    @Override
    public void intersect(double lb, double ub, ICause cause) throws ContradictionException {
        if (lb > getLB()) {
            l.set(lb);
        }
        if (ub < getUB()) {
            u.set(ub);
        }
        if (getLB() > getUB()) {
            model.getSolver().throwsException(cause, null, "");
        }
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }

    private boolean isIntegerConstant(RealVar realVar) {
        return realVar.isAConstant() && Math.rint(realVar.getLB()) == realVar.getLB();
    }
}
