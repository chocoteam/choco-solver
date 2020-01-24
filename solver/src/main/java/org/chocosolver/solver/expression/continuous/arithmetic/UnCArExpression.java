/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
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

import java.util.List;
import java.util.TreeSet;

/**
 * Unary arithmetic continuous expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class UnCArExpression implements CArExpression {

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
     * The expression this expression relies on
     */
    private CArExpression e;

    IStateDouble l;
    IStateDouble u;

    /**
     * Builds a unary expression
     *
     * @param op  operator
     * @param exp an continuous arithmetic expression
     */
    public UnCArExpression(Operator op, CArExpression exp) {
        this.op = op;
        this.e = exp;
        this.model = e.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public RealVar realVar(double p) {
        if (me == null) {
            RealVar v = e.realVar(p);
            switch (op) {
                case NEG:
                    me = model.realVar(-v.getUB(), -v.getLB(), p);
                    model.realIbexGenericConstraint("{0}=-{1}", me, v).post();
                    break;
                case ABS:
                    me = model.realVar(0.0, Math.max(Math.abs(v.getLB()), Math.abs(v.getUB())) , p);
                    model.realIbexGenericConstraint("{0}=abs({1})", me, v).post();
                    break;
                case EXP:
                    me = model.realVar(Math.min(Math.exp(v.getLB()), Math.exp(v.getUB())),
                            Math.max(Math.exp(v.getLB()), Math.exp(v.getUB())), p);
                    model.realIbexGenericConstraint("{0}=exp({1})", me, v).post();
                    break;
                case LN:
                    me = model.realVar(Math.min(Math.log(v.getLB()), Math.log(v.getUB())),
                            Math.max(Math.log(v.getLB()), Math.log(v.getUB())), p);
                    model.realIbexGenericConstraint("{0}=ln({1})", me, v).post();
                    break;
                case SQRT:
                    me = model.realVar(Math.min(Math.sqrt(v.getLB()), Math.sqrt(v.getUB())),
                            Math.max(Math.sqrt(v.getLB()), Math.sqrt(v.getUB())), p);
                    model.realIbexGenericConstraint("{0}=sqrt({1})", me, v).post();
                    break;
                case COS:
                    me = model.realVar(-1.0, 1.0, p);
                    model.realIbexGenericConstraint("{0}=cos({1})", me, v).post();
                    break;
                case SIN:
                    me = model.realVar(0.0, 1.0, p);
                    model.realIbexGenericConstraint("{0}=sin({1})", me, v).post();
                    break;
                case TAN:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=tan({1})", me, v).post();
                    break;
                case ACOS:
                    me = model.realVar(0.0, Math.PI, p);
                    model.realIbexGenericConstraint("{0}=acos({1})", me, v).post();
                    break;
                case ASIN:
                    me = model.realVar(-Math.PI / 2, Math.PI / 2, p);
                    model.realIbexGenericConstraint("{0}=asin({1})", me, v).post();
                    break;
                case ATAN:
                    me = model.realVar(-Math.PI / 2, Math.PI / 2, p);
                    model.realIbexGenericConstraint("{0}=atan({1})", me, v).post();
                    break;
                case COSH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=cosh({1})", me, v).post();
                    break;
                case SINH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=sinh({1})", me, v).post();
                    break;
                case TANH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=tanh({1})", me, v).post();
                    break;
                case ACOSH:
                    me = model.realVar(0.0, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=acosh({1})", me, v).post();
                    break;
                case ASINH:
                    me = model.realVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=asinh({1})", me, v).post();
                    break;
                case ATANH:
                    me = model.realVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, p);
                    model.realIbexGenericConstraint("{0}=atanh({1})", me, v).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Unary arithmetic expressions does not support "+op.name());
            }
        }
        return me;
    }

    @Override
    public void tighten() {
        RealInterval res;
        switch (op) {
            case NEG:
            case ABS:
            case EXP:
            case LN:
            case SQRT:
            case COS:
                res = RealUtils.cos(e);
                break;
            case SIN:
                res = RealUtils.sin(e);
                break;
            case TAN:
            case ACOS:
            case ASIN:
            case ATAN:
            case COSH:
            case SINH:
            case TANH:
            case ACOSH:
            case ASINH:
            case ATANH:
            default:
                throw new UnsupportedOperationException("Equation does not support " + op.name());
        }
        l.set(res.getLB());
        u.set(res.getUB());
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
        RealInterval res;
        switch (op) {
            case NEG:
            case ABS:
            case EXP:
            case LN:
            case SQRT:
            case COS:
                res = RealUtils.acos_wrt(this, e);
                if (res.getLB() > res.getUB()) {
                    throw model.getSolver().getContradictionException().set(cause, null, "");
                }
                break;
            case SIN:
                res = RealUtils.asin_wrt(this, e);
                if (res.getLB() > res.getUB()) {
                    throw model.getSolver().getContradictionException().set(cause, null, "");
                }
                break;
            case TAN:
            case ACOS:
            case ASIN:
            case ATAN:
            case COSH:
            case SINH:
            case TANH:
            case ACOSH:
            case ASINH:
            case ATANH:
            default:
                throw new UnsupportedOperationException("Equation does not support " + op.name());
        }
        e.intersect(res, cause);
    }

    @Override
    public void collectVariables(TreeSet<RealVar> set) {
        e.collectVariables(set);
    }

    @Override
    public void subExps(List<CArExpression> list) {
        e.subExps(list);
        list.add(this);
    }

    @Override
    public boolean isolate(RealVar var, List<CArExpression> wx, List<CArExpression> wox) {
        boolean dependsOnX = e.isolate(var, wx, wox);
        if (dependsOnX){
            wx.add(this);
        }
        else{
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
        e.init();
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
    public void intersect(RealInterval interval, ICause cause) throws ContradictionException {
        intersect(interval.getLB(), interval.getUB(), cause);
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
        return op.name() + "(" + e.toString() + ")";
    }
}
