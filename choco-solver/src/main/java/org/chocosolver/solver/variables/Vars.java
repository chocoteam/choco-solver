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
import org.chocosolver.util.tools.StringUtils;

/**
 * Interface to make variables (BoolVar, IntVar, RealVar and SetVar)
 *
 * @author Jean-Guillaume FAGES (www.cosling.com)
 */
public interface Vars {

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
     * Create a constant boolean variable equal to 1 if value and 0 otherwise
     * @param value constant value of the boolean variable (true or false)
     * @return a constant of type BoolVar
     */
    default BoolVar makeBoolVar(boolean value) {
        if(value){
            return _me().ONE();
        }else{
            return _me().ZERO();
        }
    }

    /**
     * Create a boolean variable, i.e. a particular integer variable of domain {0, 1}
     * @return a BoolVar of domain {0, 1}
     */
    default BoolVar makeBoolVar() {
        return makeBoolVar(StringUtils.randomName());
    }

    /**
     * Create a boolean variable
     * @param name name of the variable
     * @return a BoolVar of domain {0, 1}
     */
    default BoolVar makeBoolVar(String name) {
        return VariableFactory.bool(name, _me());
    }

    // ARRAY

    /**
     * Create an array of <i>size</i> boolean variables
     * @param size number of variable to create
     * @return an array of <i>size</i> BoolVar of domain {0, 1}
     */
    default BoolVar[] makeBoolVarArray(int size) {
        return makeBoolVarArray(StringUtils.randomName(),size);
    }

    /**
     * Create an array of <i>size</i> boolean variables
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variable to create
     * @return an array of <i>size</i> BoolVar of domain {0, 1}
     */
    default BoolVar[] makeBoolVarArray(String name, int size) {
        BoolVar[] vars = new BoolVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeBoolVar(name + "[" + i + "]");
        }
        return vars;
    }

    // MATRIX

    /**
     * Create a <i>dim1*dim2</i>-sized matrix of boolean variables
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @return a matrix of <i>dim1*dim2</i> BoolVar of domain {0, 1}
     */
    default BoolVar[][] makeBoolVarMatrix(int dim1, int dim2) {
        return makeBoolVarMatrix(StringUtils.randomName(),dim1,dim2);
    }

    /**
     * Create a <i>dim1*dim2</i>-sized matrix of boolean variables
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @return a matrix of <i>dim1*dim2</i> BoolVar of domain {0, 1}
     */
    default BoolVar[][] makeBoolVarMatrix(String name, int dim1, int dim2) {
        BoolVar[][] vars = new BoolVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeBoolVarArray(name + "[" + i + "]", dim2);
        }
        return vars;
    }

    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    // SINGLE

    /**
     * Create a constant integer variable
     * @param value constant value of the variable
     * @return a constant IntVar of domain {<i>value</i>}
     */
    default IntVar makeIntVar(int value) {
        return makeIntVar(StringUtils.randomName(),value);
    }

    /**
     * Create an integer variable of initial domain <i>values</i>.
     * Uses an enumerated domain that supports holes
     * @param values initial domain of the variable
     * @return an IntVar of domain <i>values</i>
     */
    default IntVar makeIntVar(int[] values) {
        return makeIntVar(StringUtils.randomName(),values);
    }

    /**
     * Create an integer variable of initial domain [<i>min</i>, <i>max</i>]
     * Uses an enumerated domain if <i>max</i>-<i>min</i> is small, and a bounded domain otherwise
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @return an IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar makeIntVar(int min, int max) {
        return makeIntVar(StringUtils.randomName(),min, max);
    }

    /**
     * Create an integer variable of initial domain [<i>min</i>, <i>max</i>]
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain
     * @return an IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar makeIntVar(int min, int max, boolean boundedDomain) {
        return makeIntVar(StringUtils.randomName(), min, max, boundedDomain);
    }

    /**
     * Create a constant integer variable
     * @param name name of the variable
     * @param value value of the variable
     * @return a constant IntVar of domain {<i>value</i>}
     */
    default IntVar makeIntVar(String name, int value) {
        return VariableFactory.fixed(name, value, _me());
    }

    /**
     * Create an integer variable of initial domain [<i>min</i>, <i>max</i>]
     * @param name name of the variable
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain
     * @return an IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar makeIntVar(String name, int min, int max, boolean boundedDomain) {
        if (boundedDomain) {
            return VariableFactory.bounded(name, min, max, _me());
        } else {
            return VariableFactory.enumerated(name, min, max, _me());
        }
    }

    /**
     * Create an integer variable of initial domain [<i>min</i>, <i>max</i>]
     * Uses an enumerated domain if <i>max</i>-<i>min</i> is small, and a bounded domain otherwise
     * @param name name of the variable
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @return an IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar makeIntVar(String name, int min, int max) {
        boolean bounded = max - min + 1 >= _me().getSettings().getMaxDomSizeForEnumerated();
        return makeIntVar(name, min, max, bounded);
    }

    /**
     * Create an integer variable of initial domain <i>values</i>
     * Uses an enumerated domain that supports holes
     * @param name name of the variable
     * @param values initial domain
     * @return an IntVar of domain <i>values</i>
     */
    default IntVar makeIntVar(String name, int[] values) {
        return VariableFactory.enumerated(name, values, _me());
    }

    // ARRAY

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in <i>values</i>
     * @param size number of variables
     * @param values initial domain of each variable
     * @return an array of <i>size</i> IntVar of domain <i>values</i>
     */
    default IntVar[] makeIntVarArray(int size, int[] values) {
        return makeIntVarArray(StringUtils.randomName(), size, values);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param size number of variables
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @return an array of <i>size</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[] makeIntVarArray(int size, int min, int max) {
        return makeIntVarArray(StringUtils.randomName(), size, min, max);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param size number of variables
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return an array of <i>size</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[] makeIntVarArray(int size, int min, int max, boolean boundedDomain) {
        return makeIntVarArray(StringUtils.randomName(), size, min, max, boundedDomain);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return an array of <i>size</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[] makeIntVarArray(String name, int size, int min, int max, boolean boundedDomain) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeIntVar(name + "[" + i + "]", min, max, boundedDomain);
        }
        return vars;
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @return an array of <i>size</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[] makeIntVarArray(String name, int size, int min, int max) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeIntVar(name + "[" + i + "]", min, max);
        }
        return vars;
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in <i>values</i>
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param values initial domain of each variable
     * @return an array of <i>size</i> IntVar of domain <i>values</i>
     */
    default IntVar[] makeIntVarArray(String name, int size, int[] values) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeIntVar(name + "[" + i + "]", values);
        }
        return vars;
    }

    // MATRIX

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in <i>values</i>
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param values initial domain of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain <i>values</i>
     */
    default IntVar[][] makeIntVarMatrix(int dim1, int dim2, int[] values) {
        return makeIntVarMatrix(StringUtils.randomName(), dim1, dim2, values);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>min</i>, <i>max</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[][] makeIntVarMatrix(int dim1, int dim2, int min, int max) {
        return makeIntVarMatrix(StringUtils.randomName(), dim1, dim2, min, max);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>min</i>, <i>max</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[][] makeIntVarMatrix(int dim1, int dim2, int min, int max, boolean boundedDomain) {
        return makeIntVarMatrix(StringUtils.randomName(), dim1, dim2, min, max, boundedDomain);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[][] makeIntVarMatrix(String name, int dim1, int dim2, int min, int max, boolean boundedDomain) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeIntVarArray(name + "[" + i + "]", dim2, min, max, boundedDomain);
        }
        return vars;
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param min initial domain lower bound of each variable
     * @param max initial domain upper bound of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>min</i>, <i>max</i>]
     */
    default IntVar[][] makeIntVarMatrix(String name, int dim1, int dim2, int min, int max) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeIntVarArray(name + "[" + i + "]", dim2, min, max);
        }
        return vars;
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in <i>values</i>
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param values initial domain of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain <i>values</i>
     */
    default IntVar[][] makeIntVarMatrix(String name, int dim1, int dim2, int[] values) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeIntVarArray(name + "[" + i + "]", dim2, values);
        }
        return vars;
    }


    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    /**
     * Creates a constant real variable equal to VALUE
     * @param VALUE value of the variable
     * @param precision double precision (e.g., 0.00001d)
     * @return a constant RealVar of domain {VALUE}
     */
    default RealVar makeRealVar(double VALUE, double precision) {
        return makeRealVar(VALUE, VALUE, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>min</i>, <i>max</i>]
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>min</i>, <i>max</i>]
     */
    default RealVar makeRealVar(double min, double max, double precision) {
        return makeRealVar(StringUtils.randomName(), min, max, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>min</i>, <i>max</i>]
     * @param name variable name
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>min</i>, <i>max</i>]
     */
    default RealVar makeRealVar(String name, double min, double max, double precision) {
        return VariableFactory.real(name, min, max, precision, _me());
    }

    /**
     * Creates a real view of <i>var</i>, i.e. a RealVar of domain equal to the domain of <i>var</i>.
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the integer variable to be viewed as a RealVar
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain equal to the domain of <i>var</i>
     */
    default RealVar makeRealView(IntVar var, double precision) {
        return VariableFactory.real(var, precision);
    }

    // ARRAY

    /**
     * Creates an array of <i>size</i> real variables, each of domain [<i>min</i>, <i>max</i>]
     * @param size number of variables
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return an array of <i>size</i> RealVar of domain [<i>min</i>, <i>max</i>]
     */
    default RealVar[] makeRealVarArray(int size, double min, double max, double precision) {
        return makeRealVarArray(StringUtils.randomName(), size, min, max, precision);
    }

    /**
     * Creates an array of <i>size</i> real variables, each of domain [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return an array of <i>size</i> RealVar of domain [<i>min</i>, <i>max</i>]
     */
    default RealVar[] makeRealVarArray(String name, int size, double min, double max, double precision) {
        RealVar[] vars = new RealVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeRealVar(name + "[" + i + "]", min, max, precision);
        }
        return vars;
    }

    /**
     * Creates an array of real views for a set of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the array of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>var</i>
     */
    default RealVar[] makeRealViewArray(IntVar[] var, double precision) {
        return VariableFactory.real(var, precision);
    }

    // MATRIX

    /**
     * Creates a matrix of <i>dim1*dim2</i> real variables, each of domain [<i>min</i>, <i>max</i>]
     * @param dim1 number of matrix rows
     * @param dim2 number of matrix columns
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a matrix of <i>dim1*dim2</i> RealVar of domain [<i>min</i>, <i>max</i>]
     */
    default RealVar[][] makeRealVarMatrix(int dim1, int dim2, double min, double max, double precision) {
        return makeRealVarMatrix(StringUtils.randomName(), dim1, dim2, min, max, precision);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> real variables, each of domain [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of matrix rows
     * @param dim2 number of matrix columns
     * @param min initial domain lower bound
     * @param max initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a matrix of <i>dim1*dim2</i> RealVar of domain [<i>min</i>, <i>max</i>]
     */
    default RealVar[][] makeRealVarMatrix(String name, int dim1, int dim2, double min, double max, double precision) {
        RealVar[][] vars = new RealVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeRealVarArray(name + "[" + i + "]", dim2, min, max, precision);
        }
        return vars;
    }

    /**
     * Creates a matrix of real views for a matrix of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the matrix of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>var</i>
     */
    default RealVar[][] makeRealViewMatrix(IntVar[][] var, double precision) {
        RealVar[][] vars = new RealVar[var.length][var[0].length];
        for (int i = 0; i < var.length; i++) {
            vars[i] = makeRealViewArray(var[i], precision);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

    /**
     * Creates a set variable taking its domain in [<i>min</i>, <i>max</i>],
     * For instance [{0,3},{-2,0,2,3}] means the variable must include both 0 and 3 and can additionnaly include -2, and 2
     * @param min initial domain lower bound (contains mandatory elements that should be present in every solution)
     * @param max initial domain upper bound (contains potential elements)
     * @return a SetVar of domain [<i>min</i>, <i>max</i>]
     */
    default SetVar makeSetVar(int[] min, int[] max) {
        return makeSetVar(StringUtils.randomName(), min, max);
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar makeSetVar(int[] value) {
        return makeSetVar(StringUtils.randomName(), value);
    }

    /**
     * Creates a set variable taking its domain in [<i>min</i>, <i>max</i>],
     * For instance [{0,3},{-2,0,2,3}] means the variable must include both 0 and 3 and can additionnaly include -2, and 2
     * @param name name of the variable
     * @param min initial domain lower bound (contains mandatory elements that should be present in every solution)
     * @param max initial domain upper bound (contains potential elements)
     * @return a SetVar of domain [<i>min</i>, <i>max</i>]
     */
    default SetVar makeSetVar(String name, int[] min, int[] max) {
        return VariableFactory.set(name, max, min, _me());
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param name name of the variable
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar makeSetVar(String name, int[] value) {
        return VariableFactory.fixed(name, value, _me());
    }

    // ARRAY

    /**
     * Creates an array of <i>size</i> set variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param size number of variables
     * @param min initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param max initial domain upper bound of every variable (contains potential elements)
     * @return an array of <i>size</i> SetVar of domain [<i>min</i>, <i>max</i>]
     */
    default SetVar[] makeSetVarArray(int size, int[] min, int[] max) {
        return makeSetVarArray(StringUtils.randomName(), size, min, max);
    }

    /**
     * Creates an array of <i>size</i> set variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param min initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param max initial domain upper bound of every variable (contains potential elements)
     * @return an array of <i>size</i> SetVar of domain [<i>min</i>, <i>max</i>]
     */
    default SetVar[] makeSetVarArray(String name, int size, int[] min, int[] max) {
        SetVar[] vars = new SetVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeSetVar(name + "[" + i + "]", min, max);
        }
        return vars;
    }

    // MATRIX

    /**
     * Creates a matrix of <i>dim1*dim2</i> set variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param min initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param max initial domain upper bound of every variable (contains potential elements)
     * @return a matrix of <i>dim1*dim2</i> SetVar of domain [<i>min</i>, <i>max</i>]
     */
    default SetVar[][] makeSetVarMatrix(int dim1, int dim2, int[] min, int[] max) {
        return makeSetVarMatrix(StringUtils.randomName(), dim1, dim2, min, max);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> set variables, taking their domain in [<i>min</i>, <i>max</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param min initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param max initial domain upper bound of every variable (contains potential elements)
     * @return a matrix of <i>dim1*dim2</i> SetVar of domain [<i>min</i>, <i>max</i>]
     */
    default SetVar[][] makeSetVarMatrix(String name, int dim1, int dim2, int[] min, int[] max) {
        SetVar[][] vars = new SetVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeSetVarArray(name + "[" + i + "]", dim2, min, max);
        }
        return vars;
    }
}
