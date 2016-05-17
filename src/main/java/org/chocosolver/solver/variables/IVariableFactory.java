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
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.impl.*;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.StringUtils;

/**
 * Interface to make variables (BoolVar, IntVar, RealVar and SetVar)
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 * @author Charles Prud'homme
 * @since 4.0.0
 */
@SuppressWarnings("unused")
public interface IVariableFactory extends ISelf<Model> {

    /**
     * Default prefix for constants
     */
    String CSTE_NAME = "cste -- ";

    //*************************************************************************************
    // BOOLEAN VARIABLES
    //*************************************************************************************

    /**
     * Create a constant boolean variable equal to 1 if <i>value</i> is true and 0 otherwise
     * @param value constant value of the boolean variable (true or false)
     * @return a constant of type BoolVar
     */
    default BoolVar boolVar(boolean value) {
        return boolVar(CSTE_NAME + (value?1:0),value);
    }

    /**
     * Create a constant boolean variable equal to 1 if <i>value</i> is true and 0 otherwise
     * @param name name of the variable
     * @param value constant value of the boolean variable (true or false)
     * @return a constant of type BoolVar
     */
    default BoolVar boolVar(String name, boolean value) {
        int intVal = value?1:0;
        if (name.equals(CSTE_NAME + intVal) && _me().getCachedConstants().containsKey(intVal)) {
            return (BoolVar)_me().getCachedConstants().get(intVal);
        }
        BoolVar cste = new FixedBoolVarImpl(name, intVal, _me());
        if (name.equals(CSTE_NAME + intVal)) {
            _me().getCachedConstants().put(intVal, cste);
        }
        return cste;
    }

    /**
     * Create a boolean variable, i.e. a particular integer variable of domain {0, 1}
     * @return a BoolVar of domain {0, 1}
     */
    default BoolVar boolVar() {
        return boolVar(StringUtils.randomName());
    }

    /**
     * Create a boolean variable
     * @param name name of the variable
     * @return a BoolVar of domain {0, 1}
     */
    default BoolVar boolVar(String name) {
        return new BoolVarImpl(name, _me());
    }

    // ARRAY

    /**
     * Create an array of <i>size</i> boolean variables
     * @param size number of variable to create
     * @return an array of <i>size</i> BoolVar of domain {0, 1}
     */
    default BoolVar[] boolVarArray(int size) {
        return boolVarArray(StringUtils.randomName(),size);
    }

    /**
     * Create an array of <i>size</i> boolean variables
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variable to create
     * @return an array of <i>size</i> BoolVar of domain {0, 1}
     */
    default BoolVar[] boolVarArray(String name, int size) {
        BoolVar[] vars = new BoolVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = boolVar(name + "[" + i + "]");
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
    default BoolVar[][] boolVarMatrix(int dim1, int dim2) {
        return boolVarMatrix(StringUtils.randomName(),dim1,dim2);
    }

    /**
     * Create a <i>dim1*dim2</i>-sized matrix of boolean variables
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @return a matrix of <i>dim1*dim2</i> BoolVar of domain {0, 1}
     */
    default BoolVar[][] boolVarMatrix(String name, int dim1, int dim2) {
        BoolVar[][] vars = new BoolVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = boolVarArray(name + "[" + i + "]", dim2);
        }
        return vars;
    }

    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    // SINGLE

    /**
     * Create a constant integer variable equal to <i>value</i>
     * @param value constant value of the variable
     * @return a constant IntVar of domain {<i>value</i>}
     */
    default IntVar intVar(int value) {
        return intVar(CSTE_NAME + value,value);
    }

    /**
     * Create an integer variable of initial domain <i>values</i>.
     * Uses an enumerated domain that supports holes
     * @param values initial domain of the variable
     * @return an IntVar of domain <i>values</i>
     */
    default IntVar intVar(int[] values) {
        return intVar(StringUtils.randomName(),values);
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(int lb, int ub) {
        return intVar(StringUtils.randomName(),lb, ub);
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(int lb, int ub, boolean boundedDomain) {
        return intVar(StringUtils.randomName(), lb, ub, boundedDomain);
    }

    /**
     * Create a constant integer variable equal to <i>value</i>
     * @param name name of the variable
     * @param value value of the variable
     * @return a constant IntVar of domain {<i>value</i>}
     */
    default IntVar intVar(String name, int value) {
        checkIntDomainRange(name, value, value);
        if (value == 0 || value == 1) {
            return boolVar(name,value==1);
        }
        if (name.equals(CSTE_NAME + value) && _me().getCachedConstants().containsKey(value)) {
            return _me().getCachedConstants().get(value);
        }
        IntVar cste = new FixedIntVarImpl(name, value, _me());
        if (name.equals(CSTE_NAME + value)) {
            _me().getCachedConstants().put(value, cste);
        }
        return cste;
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * @param name name of the variable
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(String name, int lb, int ub, boolean boundedDomain) {
        checkIntDomainRange(name, lb, ub);
        if (lb == ub) {
            return intVar(name, lb);
        } else if (lb == 0 && ub == 1) {
            return boolVar(name);
        } else  if(boundedDomain) {
            return new IntervalIntVarImpl(name, lb, ub, _me());
        } else {
            return new BitsetIntVarImpl(name, lb, ub, _me());
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
    default IntVar intVar(String name, int lb, int ub) {
        boolean bounded = ub - lb + 1 >= _me().getSettings().getMaxDomSizeForEnumerated();
        return intVar(name, lb, ub, bounded);
    }

    /**
     * Create an integer variable of initial domain <i>values</i>
     * Uses an enumerated domain that supports holes
     * @param name name of the variable
     * @param values initial domain
     * @return an IntVar of domain <i>values</i>
     */
    default IntVar intVar(String name, int[] values) {
        values = ArrayUtils.mergeAndSortIfNot(values.clone());
        checkIntDomainRange(name, values[0], values[values.length - 1]);
        if (values.length == 1) {
            return intVar(name, values[0]);
        } else if (values.length == 2 && values[0] == 0 && values[1] == 1) {
            return boolVar(name);
        } else {
            int gap = values[values.length - 1] - values[0];
            if (gap > 30 && gap / values.length > 5) {
                return new BitsetArrayIntVarImpl(name, values, _me());
            } else {
                return new BitsetIntVarImpl(name, values, _me());
            }
        }
    }

    // ARRAY

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in <i>values</i>
     * @param size number of variables
     * @param values initial domain of each variable
     * @return an array of <i>size</i> IntVar of domain <i>values</i>
     */
    default IntVar[] intVarArray(int size, int[] values) {
        return intVarArray(StringUtils.randomName(), size, values);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] intVarArray(int size, int lb, int ub) {
        return intVarArray(StringUtils.randomName(), size, lb, ub);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] intVarArray(int size, int lb, int ub, boolean boundedDomain) {
        return intVarArray(StringUtils.randomName(), size, lb, ub, boundedDomain);
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
    default IntVar[] intVarArray(String name, int size, int lb, int ub, boolean boundedDomain) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = intVar(name + "[" + i + "]", lb, ub, boundedDomain);
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
    default IntVar[] intVarArray(String name, int size, int lb, int ub) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = intVar(name + "[" + i + "]", lb, ub);
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
    default IntVar[] intVarArray(String name, int size, int[] values) {
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = intVar(name + "[" + i + "]", values);
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
    default IntVar[][] intVarMatrix(int dim1, int dim2, int[] values) {
        return intVarMatrix(StringUtils.randomName(), dim1, dim2, values);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] intVarMatrix(int dim1, int dim2, int lb, int ub) {
        return intVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub);
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
    default IntVar[][] intVarMatrix(int dim1, int dim2, int lb, int ub, boolean boundedDomain) {
        return intVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub, boundedDomain);
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
    default IntVar[][] intVarMatrix(String name, int dim1, int dim2, int lb, int ub, boolean boundedDomain) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = intVarArray(name + "[" + i + "]", dim2, lb, ub, boundedDomain);
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
    default IntVar[][] intVarMatrix(String name, int dim1, int dim2, int lb, int ub) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = intVarArray(name + "[" + i + "]", dim2, lb, ub);
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
    default IntVar[][] intVarMatrix(String name, int dim1, int dim2, int[] values) {
        IntVar[][] vars = new IntVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = intVarArray(name + "[" + i + "]", dim2, values);
        }
        return vars;
    }


    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    /**
     * Create a constant real variable equal to <i>value</i>
     * @param value constant value of the variable
     * @return a constant RealVar of domain [<i>value</i>,<i>value</i>]
     */
    default RealVar realVar(double value) {
        return realVar(CSTE_NAME + value,value);
    }

    /**
     * Create a constant real variable equal to <i>value</i>
     * @param name name of the variable
     * @param value value of the variable
     * @return a constant RealVar of domain [<i>value</i>,<i>value</i>]
     */
    default RealVar realVar(String name, double value) {
        RealVar cste = new FixedRealVarImpl(name, value, _me());
        return cste;
    }

    /**
     * Creates a constant real variable equal to <i>value</i>
     * @param value constant value of the variable
     * @param precision double precision (e.g., 0.00001d)
     * @return a constant RealVar of domain {<i>value</i>}
     */
    default RealVar realVar(double value, double precision) {
        return realVar(CSTE_NAME+value, value, value, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>lb</i>, <i>ub</i>]
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar realVar(double lb, double ub, double precision) {
        return realVar(StringUtils.randomName(), lb, ub, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>lb</i>, <i>ub</i>]
     * @param name variable name
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar realVar(String name, double lb, double ub, double precision) {
        checkRealDomainRange(name, lb, ub);
        return new RealVarImpl(name, lb, ub, precision, _me());
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
    default RealVar[] realVarArray(int size, double lb, double ub, double precision) {
        return realVarArray(StringUtils.randomName(), size, lb, ub, precision);
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
    default RealVar[] realVarArray(String name, int size, double lb, double ub, double precision) {
        RealVar[] vars = new RealVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = realVar(name + "[" + i + "]", lb, ub, precision);
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
    default RealVar[][] realVarMatrix(int dim1, int dim2, double lb, double ub, double precision) {
        return realVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub, precision);
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
    default RealVar[][] realVarMatrix(String name, int dim1, int dim2, double lb, double ub, double precision) {
        RealVar[][] vars = new RealVar[dim1][];
        for (int i = 0; i < dim1; i++) {
            vars[i] = realVarArray(name + "[" + i + "]", dim2, lb, ub, precision);
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
    default SetVar setVar(int[] lb, int[] ub) {
        return setVar(StringUtils.randomName(), lb, ub);
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar setVar(int... value) {
        String name = CSTE_NAME+"{";
        for(int i=0;i<value.length;i++){
            name+=value[i]+(i<value.length-1?", ":"");
        }name += "}";
        return setVar(name, value);
    }

    /**
     * Creates a set variable taking its domain in [<i>lb</i>, <i>ub</i>],
     * For instance [{0,3},{-2,0,2,3}] means the variable must include both 0 and 3 and can additionnaly include -2, and 2
     * @param name name of the variable
     * @param lb initial domain lower bound (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound (contains potential elements)
     * @return a SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar setVar(String name, int[] lb, int[] ub) {
        return new SetVarImpl(name, lb, SetType.BITSET, ub, SetType.BITSET, _me());
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param name name of the variable
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar setVar(String name, int... value) {
    	if(value==null) value = new int[]{};
      return new SetVarImpl(name, value, _me());
    }

    // ARRAY

    /**
     * Creates an array of <i>size</i> set variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound of every variable (contains potential elements)
     * @return an array of <i>size</i> SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar[] setVarArray(int size, int[] lb, int[] ub) {
        return setVarArray(StringUtils.randomName(), size, lb, ub);
    }

    /**
     * Creates an array of <i>size</i> set variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param lb initial domain lower bound of every variable (contains mandatory elements that should be present in every solution)
     * @param ub initial domain upper bound of every variable (contains potential elements)
     * @return an array of <i>size</i> SetVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default SetVar[] setVarArray(String name, int size, int[] lb, int[] ub) {
        SetVar[] vars = new SetVar[size];
        for (int i = 0; i < size; i++) {
            vars[i] = setVar(name + "[" + i + "]", lb, ub);
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
    default SetVar[][] setVarMatrix(int dim1, int dim2, int[] lb, int[] ub) {
        return setVarMatrix(StringUtils.randomName(), dim1, dim2, lb, ub);
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
    default SetVar[][] setVarMatrix(String name, int dim1, int dim2, int[] lb, int[] ub) {
        SetVar[][] vars = new SetVar[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            vars[i] = setVarArray(name + "[" + i + "]", dim2, lb, ub);
        }
        return vars;
    }





    //*************************************************************************************
    // UTILS
    //*************************************************************************************

    /**
     * Checks domain range.
     * Throws an exception if wrong range definition
     *
     * @param NAME name of the variable
     * @param MIN  lower bound of the domain
     * @param MAX  upper bound of the domain
     */
    default void checkIntDomainRange(String NAME, int MIN, int MAX) {
        if (MIN - Integer.MIN_VALUE == 0 || MAX - Integer.MAX_VALUE == 0) {
            throw new SolverException(NAME + ": consider reducing the bounds to avoid unexpected results");
        }
        if (MAX < MIN) {
            throw new SolverException(NAME + ": wrong domain definition, lower bound > upper bound");
        }
    }

    /**
     * Checks domain range.
     * Throws an exception if wrong range definition
     *
     * @param NAME name of the variable
     * @param MIN  lower bound of the domain
     * @param MAX  upper bound of the domain
     */
    default void checkRealDomainRange(String NAME, double MIN, double MAX) {
        if (MIN - Double.MIN_VALUE == 0 || MAX - Double.MAX_VALUE == 0) {
            throw new SolverException(NAME + ": consider reducing the bounds to avoid unexpected results");
        }
        if (MAX < MIN) {
            throw new SolverException(NAME + ": wrong domain definition, lower bound > upper bound");
        }
    }

    /**
     * Converts <i>ivars</i> into an array of boolean variables
     *
     * @param ivars an IntVar array containing only boolean variables
     * @return an array of BoolVar
     * @throws java.lang.ClassCastException if one variable is not a BoolVar
     */
    default BoolVar[] toBoolVar(IntVar... ivars) {
        BoolVar[] bvars = new BoolVar[ivars.length];
        for (int i = ivars.length - 1; i >= 0; i--) {
            bvars[i] = (BoolVar) ivars[i];
        }
        return bvars;
    }
}