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
        return makeIntVar(VariableFactory.CSTE_NAME + value,value);
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
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar makeIntVar(int lb, int ub) {
        return makeIntVar(StringUtils.randomName(),lb, ub);
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar makeIntVar(int lb, int ub, boolean boundedDomain) {
        return makeIntVar(StringUtils.randomName(), lb, ub, boundedDomain);
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
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * @param name name of the variable
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar makeIntVar(String name, int lb, int ub, boolean boundedDomain) {
        if (boundedDomain) {
            return VariableFactory.bounded(name, lb, ub, _me());
        } else {
            return VariableFactory.enumerated(name, lb, ub, _me());
        }
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @param name name of the variable
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar makeIntVar(String name, int lb, int ub) {
        boolean bounded = ub - lb + 1 >= _me().getSettings().getMaxDomSizeForEnumerated();
        return makeIntVar(name, lb, ub, bounded);
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
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] makeIntVarArray(int size, int lb, int ub) {
        return makeIntVarArray(StringUtils.randomName(), size, lb, ub);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] makeIntVarArray(int size, int lb, int ub, boolean boundedDomain) {
        return makeIntVarArray(StringUtils.randomName(), size, lb, ub, boundedDomain);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] makeIntVarArray(String name, int size, int lb, int ub, boolean boundedDomain) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeIntVar(name + "[" + i + "]", lb, ub, boundedDomain);
        }
        return vars;
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] makeIntVarArray(String name, int size, int lb, int ub) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeIntVar(name + "[" + i + "]", lb, ub);
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
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] makeIntVarMatrix(int dim1, int dim2, int lb, int ub) {
        return makeIntVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] makeIntVarMatrix(int dim1, int dim2, int lb, int ub, boolean boundedDomain) {
        return makeIntVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub, boundedDomain);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] makeIntVarMatrix(String name, int dim1, int dim2, int lb, int ub, boolean boundedDomain) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeIntVarArray(name + "[" + i + "]", dim2, lb, ub, boundedDomain);
        }
        return vars;
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] makeIntVarMatrix(String name, int dim1, int dim2, int lb, int ub) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeIntVarArray(name + "[" + i + "]", dim2, lb, ub);
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
     * Creates a constant real variable equal to <i>value</i>
     * @param value constant value of the variable
     * @param precision double precision (e.g., 0.00001d)
     * @return a constant RealVar of domain {<i>value</i>}
     */
    default RealVar makeRealVar(double value, double precision) {
        return makeRealVar(VariableFactory.CSTE_NAME+value, value, value, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>lb</i>, <i>ub</i>]
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar makeRealVar(double lb, double ub, double precision) {
        return makeRealVar(StringUtils.randomName(), lb, ub, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>lb</i>, <i>ub</i>]
     * @param name variable name
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar makeRealVar(String name, double lb, double ub, double precision) {
        return VariableFactory.real(name, lb, ub, precision, _me());
    }

    // ARRAY

    /**
     * Creates an array of <i>size</i> real variables, each of domain [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return an array of <i>size</i> RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar[] makeRealVarArray(int size, double lb, double ub, double precision) {
        return makeRealVarArray(StringUtils.randomName(), size, lb, ub, precision);
    }

    /**
     * Creates an array of <i>size</i> real variables, each of domain [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return an array of <i>size</i> RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar[] makeRealVarArray(String name, int size, double lb, double ub, double precision) {
        RealVar[] vars = new RealVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeRealVar(name + "[" + i + "]", lb, ub, precision);
        }
        return vars;
    }

    // MATRIX

    /**
     * Creates a matrix of <i>dim1*dim2</i> real variables, each of domain [<i>lb</i>, <i>ub</i>]
     * @param dim1 number of matrix rows
     * @param dim2 number of matrix columns
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a matrix of <i>dim1*dim2</i> RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar[][] makeRealVarMatrix(int dim1, int dim2, double lb, double ub, double precision) {
        return makeRealVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub, precision);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> real variables, each of domain [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of matrix rows
     * @param dim2 number of matrix columns
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a matrix of <i>dim1*dim2</i> RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar[][] makeRealVarMatrix(String name, int dim1, int dim2, double lb, double ub, double precision) {
        RealVar[][] vars = new RealVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeRealVarArray(name + "[" + i + "]", dim2, lb, ub, precision);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

    /**
     * Creates a set variable taking its domain in [<i>lb</i>, <i>ub</i>],
     * For instance [{0,3},{-2,0,2,3}] means the variable must include both 0 and 3 and can additionnaly include -2, and 2
     * @param lb initial domain lower bound (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound (contains potential elements)
     * @return a SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar makeSetVar(int[] lb, int[] ub) {
        return makeSetVar(StringUtils.randomName(), lb, ub);
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar makeSetVar(int[] value) {
        String name = VariableFactory.CSTE_NAME+"{";
        for(int i=0;i<value.length;i++){
            name+=value[i]+i<value.length-1?", ":"";
        }name += "}";
        return makeSetVar(name, value);
    }

    /**
     * Creates a set variable taking its domain in [<i>lb</i>, <i>ub</i>],
     * For instance [{0,3},{-2,0,2,3}] means the variable must include both 0 and 3 and can additionnaly include -2, and 2
     * @param name name of the variable
     * @param lb initial domain lower bound (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound (contains potential elements)
     * @return a SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar makeSetVar(String name, int[] lb, int[] ub) {
        return VariableFactory.set(name, ub, lb, _me());
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
     * Creates an array of <i>size</i> set variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound of every variable (contains potential elements)
     * @return an array of <i>size</i> SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar[] makeSetVarArray(int size, int[] lb, int[] ub) {
        return makeSetVarArray(StringUtils.randomName(), size, lb, ub);
    }

    /**
     * Creates an array of <i>size</i> set variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param lb initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound of every variable (contains potential elements)
     * @return an array of <i>size</i> SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar[] makeSetVarArray(String name, int size, int[] lb, int[] ub) {
        SetVar[] vars = new SetVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = makeSetVar(name + "[" + i + "]", lb, ub);
        }
        return vars;
    }

    // MATRIX

    /**
     * Creates a matrix of <i>dim1*dim2</i> set variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound of every variable (contains potential elements)
     * @return a matrix of <i>dim1*dim2</i> SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar[][] makeSetVarMatrix(int dim1, int dim2, int[] lb, int[] ub) {
        return makeSetVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> set variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound of every variable (contains potential elements)
     * @return a matrix of <i>dim1*dim2</i> SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar[][] makeSetVarMatrix(String name, int dim1, int dim2, int[] lb, int[] ub) {
        SetVar[][] vars = new SetVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = makeSetVarArray(name + "[" + i + "]", dim2, lb, ub);
        }
        return vars;
    }
}
