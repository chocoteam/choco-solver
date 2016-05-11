/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.real.IntEqRealConstraint;
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
            if(bool.isInstantiated()) {
                not = bool.getValue() == 1 ? _me().ZERO() : _me().ONE();
            }else if (_me().getSettings().enableViews()) {
                not = new BoolNotView(bool);
            }else {
                not = _me().boolVar("not(" + bool.getName() + ")");
                _me().arithm(not, "!=", bool).post();
            }
            bool._setNot(not);
            not._setNot(bool);
            not.setNot(true);
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
        String name = "(" + var.getName() + (cste >= 0 ? "+":"-") + Math.abs(cste) + ")";
        if(var.isInstantiated()) {
            return _me().intVar(name, var.getValue() + cste);
        }
        if (_me().getSettings().enableViews()) {
            return new OffsetView(var, cste);
        } else {
            int lb = var.getLB() + cste;
            int ub = var.getUB() + cste;
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = _me().intVar(name, lb, ub, false);
            } else {
                ov = _me().intVar(name, lb, ub, true);
            }
            _me().arithm(ov, "-", var, "=", cste).post();
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
        if(var.isInstantiated()) {
            return _me().intVar(-var.getValue());
        }
        if (_me().getSettings().enableViews()) {
            if(var instanceof MinusView){
                return ((MinusView)var).getVariable();
            }else {
                return new MinusView(var);
            }
        } else {
            int ub = -var.getLB();
            int lb = -var.getUB();
            String name = "-(" + var.getName() + ")";
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = _me().intVar(name, lb, ub, false);
            } else {
                ov = _me().intVar(name, lb, ub, true);
            }
            _me().arithm(ov, "+", var, "=", 0).post();
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
        if (cste < 0) {
            throw new UnsupportedOperationException("scale requires a coefficient >= -1 (found "+cste+")");
        } else {
            IntVar v2;
            if (cste == 0) {
                v2 = _me().intVar(0);
            } else if (cste == 1) {
                v2 = var;
            } else {
                if(var.isInstantiated()) {
                    return _me().intVar(var.getValue() * cste);
                }
                if (_me().getSettings().enableViews()) {
                    v2 = new ScaleView(var, cste);
                } else {
                    int lb = var.getLB() * cste;
                    int ub = var.getUB() * cste;
                    String name = "(" + var.getName() + "*" + cste + ")";
                    IntVar ov;
                    if (var.hasEnumeratedDomain()) {
                        ov = _me().intVar(name, lb, ub, false);
                    } else {
                        ov = _me().intVar(name, lb, ub, true);
                    }
                    _me().times(var, cste, ov).post();
                    return ov;
                }
            }
            return v2;
        }
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
            return _me().intVar(Math.abs(var.getValue()));
        } else if (var.getLB() >= 0) {
            return var;
        } else if (var.getUB() <= 0) {
            return intMinusView(var);
        } else {
            int ub = max(-var.getLB(), var.getUB());
            String name = "|" + var.getName() + "|";
            IntVar abs;
            if (var.hasEnumeratedDomain()) {
                abs = _me().intVar(name, 0, ub, false);
            } else {
                abs = _me().intVar(name, 0, ub, true);
            }
            _me().absolute(abs, var).post();
            return abs;
        }
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
        if (_me().getSettings().enableViews()) {
            return new RealView(var, precision);
        } else {
            double lb = var.getLB();
            double ub = var.getUB();
            RealVar rv = _me().realVar("(real)" + var.getName(), lb, ub, precision);
            new IntEqRealConstraint(var, rv, precision).post();
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
        if (_me().getSettings().enableViews()) {
            for (int i = 0; i < ints.length; i++) {
                reals[i] = realIntView(ints[i], precision);
            }
        } else {
            for (int i = 0; i < ints.length; i++) {
                double lb = ints[i].getLB();
                double ub = ints[i].getUB();
                reals[i] = _me().realVar("(real)" + ints[i].getName(), lb, ub, precision);
            }
            new IntEqRealConstraint(ints, reals, precision).post();
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