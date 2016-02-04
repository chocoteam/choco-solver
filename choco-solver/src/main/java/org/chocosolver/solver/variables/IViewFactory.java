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

import org.chocosolver.solver.Solver;

/**
 * Interface to make views (BoolVar, IntVar, RealVar and SetVar)
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES (www.cosling.com)
 */
public interface IViewFactory {

    /**
     * Simple method to get a solver object
     * Should not be called by the user
     * @return a solver object
     */
    Solver _me();

    //*************************************************************************************
    // BOOLEAN VARIABLES
    //*************************************************************************************

    /**
     * Creates a view over <i>bool</i> holding the logical negation of <i>bool</i> (ie, &not;BOOL).
     * @param bool a boolean variable.
     * @return a BoolVar equal to <i>not(bool)</i> (or 1-bool)
     */
    default BoolVar boolNotView(BoolVar bool) {
        return VariableFactory.not(bool);
    }

    /**
     * Creates a view over <i>var</i>, equal to <i>var</i>.
     * @param var a boolean variable
     * @return a BoolVar equal to <i>var</i>
     */
    default BoolVar boolEqView(BoolVar var) {
        return VariableFactory.eq(var);
    }


    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    /**
     * Creates a view over <i>var</i>, equal to <i>var</i>.
     * @param var an integer variable
     * @return an IntVar equal to <i>var</i>
     */
    default IntVar intEqView(IntVar var) {
        return VariableFactory.eq(var);
    }

    /**
     * Creates a view based on <i>var</i>, equal to <i>var+cste</i>.
     * @param var  an integer variable
     * @param cste a constant (can be either negative or positive)
     * @return an IntVar equal to <i>var+cste</i>
     */
    default IntVar intOffsetView(IntVar var, int cste) {
        return VariableFactory.offset(var, cste);
    }

    /**
     * Creates a view over <i>var</i> equal to -<i>var</i>.
     * That is if <i>var</i> = [a,b], then this = [-b,-a].
     *
     * @param var an integer variable
     * @return an IntVar equal to <i>-var</i>
     */
    default IntVar intMinusView(IntVar var) {
        return VariableFactory.minus(var);
    }

    /**
     * Creates a view over VAR equal to <i>var*cste</i>.
     * Requires <i>cste</i> > -2
     * <p>
     * <br/>- if <i>cste</i> &lt; -1, throws an exception;
     * <br/>- if <i>cste</i> = -1, returns a minus view;
     * <br/>- if <i>cste</i> = 0, returns a fixed variable;
     * <br/>- if <i>cste</i> = 1, returns VAR;
     * <br/>- otherwise, returns a scale view;
     * <p>
     * @param VAR  an integer variable
     * @param cste a constant.
     * @return an IntVar equal to <i>var*cste</i>
     */
    default IntVar intScaleView(IntVar VAR, int cste) {
        return VariableFactory.scale(VAR, cste);
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
        return VariableFactory.abs(var);
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
        return VariableFactory.real(var, precision);
    }

    /**
     * Creates an array of real views for a set of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the array of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>var</i>
     */
    default RealVar[] realIntViewArray(IntVar[] var, double precision) {
        return VariableFactory.real(var, precision);
    }

    // MATRIX

    /**
     * Creates a matrix of real views for a matrix of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the matrix of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>var</i>
     */
    default RealVar[][] realIntViewMatrix(IntVar[][] var, double precision) {
        RealVar[][] vars = new RealVar[var.length][var[0].length];
        for (int i = 0; i < var.length; i++) {
            vars[i] = realIntViewArray(var[i], precision);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

}