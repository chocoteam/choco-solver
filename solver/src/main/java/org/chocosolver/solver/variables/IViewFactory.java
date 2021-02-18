/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.view.*;

import static java.lang.Math.max;

/**
 * Interface to make views (BoolVar, IntVar, RealVar and SetVar)
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 */
public interface IViewFactory extends ISelf<Model> {

    //*************************************************************************************
    // BOOLEAN VARIABLES
    //*************************************************************************************

    /**
     * Creates a view over <i>bool</i> holding the logical negation of <i>bool</i> (ie, &not;BOOL).
     * @param bool a boolean variable.
     * @return a BoolVar equal to <i>not(bool)</i> (or 1-bool)
     */
    default BoolVar boolNotView(BoolVar bool) {
        if (bool.hasNot()) {
            return bool.not();
        } else {
            BoolVar not;
            if (bool.isInstantiated()) {
                not = bool.getValue() == 1 ? ref().boolVar(false) : ref().boolVar(true);
            } else {
                if (ref().getSettings().enableViews()) {
                    not = new BoolNotView(bool);
                } else {
                    not = ref().boolVar("not(" + bool.getName() + ")");
                    ref().arithm(not, "!=", bool).post();
                }
                not.setNot(true);
            }
            bool._setNot(not);
            not._setNot(bool);
            return not;
        }
    }

    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    /**
     * Creates a view based on <i>var</i>, equal to <i>var+cste</i>.
     * @param var  an integer variable
     * @param cste a constant (can be either negative or positive)
     * @return an IntVar equal to <i>var+cste</i>
     */
    default IntVar intOffsetView(IntVar var, int cste) {
        if (cste == 0) {
            return var;
        }
        String name = "(" + var.getName() + (cste >= 0 ? "+" : "-") + Math.abs(cste) + ")";
        if (var.isInstantiated()) {
            return ref().intVar(name, var.getValue() + cste);
        }
        if (ref().getSettings().enableViews()) {
            int p = checkDeclaredView(var, cste, OffsetView.class);
            if(p>-1){
                return var.getView(p).asIntVar();
            }else {
                return new OffsetView(var, cste);
            }
        } else {
            int lb = var.getLB() + cste;
            int ub = var.getUB() + cste;
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = ref().intVar(name, lb, ub, false);
            } else {
                ov = ref().intVar(name, lb, ub, true);
            }
            ref().arithm(ov, "-", var, "=", cste).post();
            return ov;
        }
    }

    /**
     * Creates a view over <i>var</i> equal to -<i>var</i>.
     * That is if <i>var</i> = [a,b], then this = [-b,-a].
     *
     * @param var an integer variable
     * @return an IntVar equal to <i>-var</i>
     */
    default IntVar intMinusView(IntVar var) {
        if (var.isInstantiated()) {
            return ref().intVar(-var.getValue());
        }
        if (ref().getSettings().enableViews()) {
            if (var instanceof MinusView) {
                return ((MinusView) var).getVariable();
            } else {
                int p = checkDeclaredView(var, -1, MinusView.class);
                if(p>-1){
                    return var.getView(p).asIntVar();
                }else {
                    return new MinusView(var);
                }
            }
        } else {
            int ub = -var.getLB();
            int lb = -var.getUB();
            String name = "-(" + var.getName() + ")";
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = ref().intVar(name, lb, ub, false);
            } else {
                ov = ref().intVar(name, lb, ub, true);
            }
            ref().arithm(ov, "+", var, "=", 0).post();
            return ov;
        }
    }

    /**
     * Creates a view over <i>var</i> equal to <i>var*cste</i>.
     * Requires <i>cste</i> > -2
     * <p>
     * <br/>- if <i>cste</i> &lt; -1, throws an exception;
     * <br/>- if <i>cste</i> = -1, returns a minus view;
     * <br/>- if <i>cste</i> = 0, returns a fixed variable;
     * <br/>- if <i>cste</i> = 1, returns <i>var</i>;
     * <br/>- otherwise, returns a scale view;
     * <p>
     * @param var  an integer variable
     * @param cste a constant.
     * @return an IntVar equal to <i>var*cste</i>
     */
    default IntVar intScaleView(IntVar var, int cste) {
        if (cste == -1) {
            return intMinusView(var);
        }
        IntVar v2;
        if (cste == 0) {
            v2 = ref().intVar(0);
        } else if (cste == 1) {
            v2 = var;
        } else {
            if (var.isInstantiated()) {
                return ref().intVar(var.getValue() * cste);
            }
            if (ref().getSettings().enableViews()) {
                boolean rev = cste < 0;
                cste = Math.abs(cste);
                int p = checkDeclaredView(var, cste, ScaleView.class);
                if(p>-1){
                    return var.getView(p).asIntVar();
                }else {
                    v2 = new ScaleView(var, cste);
                }
                if(rev){
                    v2 = intMinusView(v2);
                }
        } else {
                int lb, ub;
                if (cste > 0) {
                    lb = var.getLB() * cste;
                    ub = var.getUB() * cste;
                } else {
                    lb = var.getUB() * cste;
                    ub = var.getLB() * cste;
                }
                String name = "(" + var.getName() + "*" + cste + ")";
                IntVar ov;
                if (var.hasEnumeratedDomain()) {
                    ov = ref().intVar(name, lb, ub, false);
                } else {
                    ov = ref().intVar(name, lb, ub, true);
                }
                ref().times(var, cste, ov).post();
                return ov;
            }
        }
        return v2;
    }

    /**
     * Creates a view over <i>var</i> such that: |<i>var</i>|.
     * <p>
     * <br/>- if <i>var</i> is already instantiated, returns a fixed variable;
     * <br/>- if the lower bound of <i>var</i> is greater or equal to 0, returns <i>var</i>;
     * <br/>- if the upper bound of <i>var</i> is less or equal to 0, return a minus view;
     * <br/>- otherwise, returns an absolute view;
     * <p>
     * @param var an integer variable.
     * @return an IntVar equal to the absolute value of <i>var</i>
     */
    default IntVar intAbsView(IntVar var) {
        if (var.isInstantiated()) {
            return ref().intVar(Math.abs(var.getValue()));
        } else if (var.getLB() >= 0) {
            return var;
        } else if (var.getUB() <= 0) {
            return intMinusView(var);
        } else {
            int ub = max(-var.getLB(), var.getUB());
            String name = "|" + var.getName() + "|";
            IntVar abs;
            if (var.hasEnumeratedDomain()) {
                abs = ref().intVar(name, 0, ub, false);
            } else {
                abs = ref().intVar(name, 0, ub, true);
            }
            ref().absolute(abs, var).post();
            return abs;
        }
    }

    /**
     * Creates an affine view over <i>x</i> such that: <i>a.x + b</i>.
     * <p>
     *
     * @param a a coefficient
     * @param x an integer variable.
     * @param b a constant
     * @return an IntVar equal to the absolute value of <i>var</i>
     */
    default IntVar intAffineView(int a, IntVar x, int b) {
        if (x.isInstantiated()) {
            return ref().intVar(a * x.getValue() + b);
        } else {
            return intOffsetView(intScaleView(x, a), b);
        }
    }


    /**
     * Creates an view over <i>x</i> such that: <i>(x = c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x = c</i>
     */
    default BoolVar intEqView(IntVar x, int c) {
        if (x.isInstantiatedTo(c)) {
            return ref().boolVar(true);
        } else if (!x.contains(c)) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c, EqView.class);
                if (p >= 0) {
                    return x.getView(p).asBoolVar();
                } else {
                    return new EqView(x, c);
                }
            }else{
                BoolVar b = ref().boolVar();
                ref().reifyXeqC(x, c, b);
                return b;
            }
        }
    }

    /**
     * Creates an view over <i>x</i> such that: <i>(x != c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x != c</i>
     */
    default BoolVar intNeView(IntVar x, int c) {
        if (x.isInstantiatedTo(c)) {
            return ref().boolVar(false);
        } else if (!x.contains(c)) {
            return ref().boolVar(true);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c, EqView.class);
                if (p >= 0) {
                    return x.getView(p).asBoolVar().not();
                } else {
                    return new EqView(x, c).not();
                }
            } else {
                BoolVar b = ref().boolVar();
                ref().reifyXneC(x, c, b);
                return b;
            }
        }
    }

    /**
     * Creates an view over <i>x</i> such that: <i>(x &le; c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x &le; c</i>
     */
    default BoolVar intLeView(IntVar x, int c) {
        if (x.getUB() <= c) {
            return ref().boolVar(true);
        } else if (x.getLB() > c) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c, LeqView.class);
                if (p >= 0) {
                    return x.getView(p).asBoolVar();
                } else {
                    return new LeqView(x, c);
                }
            }else {
                BoolVar b = ref().boolVar();
                ref().reifyXltC(x, c +1, b);
                return b;
            }
        }
    }

    /**
     * Creates an view over <i>x</i> such that: <i>(x &ge; c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x &ge; c</i>
     */
    default BoolVar intGeView(IntVar x, int c) {
        if (x.getLB() >= c) {
            return ref().boolVar(true);
        } else if (x.getUB() < c) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c - 1, LeqView.class);
                if (p >= 0) {
                    return x.getView(p).asBoolVar().not();
                } else {
                    return new LeqView(x, c - 1).not();
                }
            }else {
                BoolVar b = ref().boolVar();
                ref().reifyXgtC(x, c - 1, b);
                return b;
            }
        }
    }

    static int checkDeclaredView(IntVar x, int c, Class clazz){
        for(int i = 0; i < x.getNbViews(); i++)
            if (clazz.isInstance(x.getView(i))) {
                if(clazz  == EqView.class){
                    EqView v = (EqView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }else if(clazz  == LeqView.class){
                    LeqView v = (LeqView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }else if(clazz == MinusView.class){
                    return i;
                }else if(clazz == OffsetView.class){
                    OffsetView v = (OffsetView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }else if(clazz == ScaleView.class){
                    ScaleView v = (ScaleView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }
            }
        return -1;
    }


    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    /**
     * Creates a real view of <i>var</i>, i.e. a RealVar of domain equal to the domain of <i>var</i>.
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the integer variable to be viewed as a RealVar
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain equal to the domain of <i>var</i>
     */
    default RealVar realIntView(IntVar var, double precision) {
        if (ref().getSettings().enableViews()) {
            return new RealView(var, precision);
        } else {
            double lb = var.getLB();
            double ub = var.getUB();
            RealVar rv = ref().realVar("(real)" + var.getName(), lb, ub, precision);
            ref().realIbexGenericConstraint("{0} = {1}", rv, var).post();
            return rv;
        }
    }

    /**
     * Creates an array of real views for a set of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param ints the array of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>ints</i>
     */
    default RealVar[] realIntViewArray(IntVar[] ints, double precision) {
        RealVar[] reals = new RealVar[ints.length];
        if (ref().getSettings().enableViews()) {
            for (int i = 0; i < ints.length; i++) {
                reals[i] = realIntView(ints[i], precision);
            }
        } else {
            for (int i = 0; i < ints.length; i++) {
                double lb = ints[i].getLB();
                double ub = ints[i].getUB();
                reals[i] = ref().realVar("(real)" + ints[i].getName(), lb, ub, precision);
                ref().realIbexGenericConstraint("{0} = {1}", reals[i], ints[i]).post();
            }
        }
        return reals;
    }

    // MATRIX

    /**
     * Creates a matrix of real views for a matrix of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param ints the matrix of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>ints</i>
     */
    default RealVar[][] realIntViewMatrix(IntVar[][] ints, double precision) {
        RealVar[][] vars = new RealVar[ints.length][ints[0].length];
        for (int i = 0; i < ints.length; i++) {
            vars[i] = realIntViewArray(ints[i], precision);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

}