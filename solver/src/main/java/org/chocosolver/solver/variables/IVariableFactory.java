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
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.impl.*;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.VariableUtils;

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
        return boolVar(CSTE_NAME + (value ? 1 : 0), value);
    }

    /**
     * Create a constant boolean variable equal to 1 if <i>value</i> is true and 0 otherwise
     * @param name name of the variable
     * @param value constant value of the boolean variable (true or false)
     * @return a constant of type BoolVar
     */
    default BoolVar boolVar(String name, boolean value) {
        int intVal = value ? 1 : 0;
        if (name.equals(CSTE_NAME + intVal) && ref().getCachedConstants().containsKey(intVal)) {
            return (BoolVar) ref().getCachedConstants().get(intVal);
        }
        BoolVar cste = new FixedBoolVarImpl(name, intVal, ref());
        if (name.equals(CSTE_NAME + intVal)) {
            ref().getCachedConstants().put(intVal, cste);
        }
        return cste;
    }

    /**
     * Create a boolean variable, i.e. a particular integer variable of domain {0, 1}
     * @return a BoolVar of domain {0, 1}
     */
    default BoolVar boolVar() {
        return boolVar(generateName("BV_"));
    }

    /**
     * Create a boolean variable
     * @param name name of the variable
     * @return a BoolVar of domain {0, 1}
     */
    default BoolVar boolVar(String name) {
        return new BoolVarImpl(name, ref());
    }

    // ARRAY

    /**
     * Create an array of <i>size</i> boolean variables
     * @param size number of variable to create
     * @return an array of <i>size</i> BoolVar of domain {0, 1}
     */
    default BoolVar[] boolVarArray(int size) {
        return boolVarArray(generateName("BV_"), size);
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
        return boolVarMatrix(generateName("BV_"), dim1, dim2);
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
        return intVar(CSTE_NAME + value, value);
    }

    /**
     * Create an integer variable of initial domain <i>values</i>.
     * Uses an enumerated domain that supports holes
     * @param values initial domain of the variable
     * @return an IntVar of domain <i>values</i>
     */
    default IntVar intVar(int[] values) {
        return intVar(generateName("IV_"), values);
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @implNote When boundedDomain is selected only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(int lb, int ub) {
        if (lb == ub) return intVar(lb);
        return intVar(generateName("IV_"), lb, ub);
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain.
     *                      When 'boundedDomain' only bounds modifications are handled
     *                      (any value removals in the middle of the domain will be ignored).
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(int lb, int ub, boolean boundedDomain) {
		if (lb == ub) return intVar(lb);
        return intVar(generateName("IV_"), lb, ub, boundedDomain);
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
            return boolVar(name, value == 1);
        }
        if (name.equals(CSTE_NAME + value) && ref().getCachedConstants().containsKey(value)) {
            return ref().getCachedConstants().get(value);
        }
        IntVar cste = new FixedIntVarImpl(name, value, ref());
        if (name.equals(CSTE_NAME + value)) {
            ref().getCachedConstants().put(value, cste);
        }
        return cste;
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * @param name name of the variable
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param boundedDomain specifies whether to use a bounded domain or an enumerated domain.
     *                      When 'boundedDomain' only bounds modifications are handled
     *                      (any value removals in the middle of the domain will be ignored).
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(String name, int lb, int ub, boolean boundedDomain) {
        checkIntDomainRange(name, lb, ub);
        if (lb == ub) {
            return intVar(name, lb);
        } else if (lb == 0 && ub == 1) {
            return boolVar(name);
        } else if (boundedDomain) {
            return new IntervalIntVarImpl(name, lb, ub, ref());
        } else {
            return new BitsetIntVarImpl(name, lb, ub, ref());
        }
    }

    /**
     * Create an integer variable of initial domain [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @implNote When boundedDomain is selected only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
     * @param name name of the variable
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @return an IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar intVar(String name, int lb, int ub) {
        boolean bounded = ub - lb + 1 >= ref().getSettings().getMaxDomSizeForEnumerated();
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
                return new BitsetArrayIntVarImpl(name, values, ref());
            } else {
                return new BitsetIntVarImpl(name, values, ref());
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
        return intVarArray(generateName("IV_"), size, values);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @implNote When boundedDomain is selected only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] intVarArray(int size, int lb, int ub) {
        return intVarArray(generateName("IV_"), size, lb, ub);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable.
     *                      When 'boundedDomain' only bounds modifications are handled
     *                     (any value removals in the middle of the domain will be ignored).
     * @return an array of <i>size</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[] intVarArray(int size, int lb, int ub, boolean boundedDomain) {
        return intVarArray(generateName("IV_"), size, lb, ub, boundedDomain);
    }

    /**
     * Creates an array of <i>size</i> integer variables, taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The ith variable will be named <i>name</i>[i]
     * @param size number of variables
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable.
     *                      When 'boundedDomain' only bounds modifications are handled
     *                      (any value removals in the middle of the domain will be ignored).
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
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @implNote When boundedDomain is selected only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
     *
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
        return intVarMatrix(generateName("IV_"), dim1, dim2, values);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @implNote When boundedDomain is selected only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
     *
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] intVarMatrix(int dim1, int dim2, int lb, int ub) {
        return intVarMatrix(generateName("IV_"), dim1, dim2, lb, ub);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable.
     *                      When 'boundedDomain' only bounds modifications are handled
     *                      (any value removals in the middle of the domain will be ignored).
     * @return a matrix of <i>dim1*dim2</i> IntVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default IntVar[][] intVarMatrix(int dim1, int dim2, int lb, int ub, boolean boundedDomain) {
        return intVarMatrix(generateName("IV_"), dim1, dim2, lb, ub, boundedDomain);
    }

    /**
     * Creates a matrix of <i>dim1*dim2</i> integer variables taking their domain in [<i>lb</i>, <i>ub</i>]
     * @param name prefix name of the variables to create. The variable in row i and col j will be named <i>name</i>[i][j]
     * @param dim1 number of rows in the matrix
     * @param dim2 number of columns in the matrix
     * @param lb initial domain lower bound of each variable
     * @param ub initial domain upper bound of each variable
     * @param boundedDomain specifies whether to use bounded domains or enumerated domains for each variable.
     *                      When 'boundedDomain' only bounds modifications are handled
     *                      (any value removals in the middle of the domain will be ignored).
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
     * Uses an enumerated domain if <i>ub</i>-<i>lb</i> is small, and a bounded domain otherwise
     * @implNote When boundedDomain is selected only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
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
    // TASK VARIABLES
    //*************************************************************************************

    /**
     * Creates a task variable, based on a starting time <i>s</i> and a duration <i>d</i>
     * such that: s + d = e, where <i>e</i> is the ending time.
     *
     * A call to {@link Task#ensureBoundConsistency()} is required before launching the resolution,
     * this will not be done automatically.
     *
     * @param s integer variable, starting time
     * @param d fixed duration
     * @return a task variable.
     */
    default Task taskVar(IntVar s, int d) {
        return new Task(s, d);
    }

    /**
     * Creates a task variable, based on a starting time <i>s</i> and a duration <i>d</i>
     * such that: s + d = e, where <i>e</i> is the ending time.
     *
     * A call to {@link Task#ensureBoundConsistency()} is required before launching the resolution,
     * this will not be done automatically.
     *
     * @param s integer variable, starting time
     * @param d integer variable, duration
     * @return a task variable.
     */
    default Task taskVar(IntVar s, IntVar d) {
        if(d.isInstantiated()) {
            return new Task(s, d, ref().intOffsetView(s, d.getValue()));
        } else {
            int[] bounds = VariableUtils.boundsForAddition(s, d);
            IntVar end = ref().intVar(bounds[0], bounds[1]);
            return new Task(s, d, end);
        }
    }

    /**
     * Creates a task variable, based on a starting time <i>s</i> and a duration <i>d</i>
     * such that: s + d = e, where <i>e</i> is the ending time.
     *
     * A call to {@link Task#ensureBoundConsistency()} is required before launching the resolution,
     * this will not be done automatically.
     *
     * @param s integer variable, starting time
     * @param d fixed duration
     * @param e integer variable, ending time
     * @return a task variable.
     */
    default Task taskVar(IntVar s, int d, IntVar e) {
        return new Task(s, d, e);
    }

    /**
     * Creates a task variable, made of a starting time <i>s</i>,
     * a duration <i>d</i> and an ending time <i>e</i> such that: s + d = e.
     *
     * A call to {@link Task#ensureBoundConsistency()} is required before launching the resolution,
     * this will not be done automatically.
     *
     * @param s integer variable, starting time
     * @param d integer variable, duration
     * @param e integer variable, ending time
     * @return a task variable.
     */
    default Task taskVar(IntVar s, IntVar d, IntVar e) {
        return new Task(s, d, e);
    }

    /**
     * Creates an array of <i>s.length</i> task variables,
     * where task <i>i</i> is made of a starting time <i>s_i</i>,
     * a processing time <i>p_i</i> and an ending time <i>e_i</i> such that: s_i + p_i = e_i.
     *
     * A call to {@link Task#ensureBoundConsistency()} is required before launching the resolution,
     * this will not be done automatically.
     *
     * @param s integer variables, starting times
     * @param p integer variables, processing times
     * @param e integer variables, ending times
     * @return an array of task variables.
     */
    default Task[] taskVarArray(IntVar[] s, IntVar[] p, IntVar[] e) {
        if (s.length != p.length || s.length != e.length) {
            throw new SolverException("Wrong arrays size");
        }
        Task[] tasks = new Task[s.length];
        for (int i = 0; i < s.length; i++) {
            tasks[i] = taskVar(s[i], p[i], e[i]);
        }
        return tasks;
    }

    /**
     * Creates a matrix of <i>s.length * s_0.length</i> task variables,
     * where task <i>i,j</i> is made of a starting time <i>s_(i,j)</i>,
     * a processing time <i>p_(i,j)</i> and an ending time <i>e_(i,j)</i> such that:
     * s_(i,j) + p_(i,j) = e_(i,j).
     *
     * A call to {@link Task#ensureBoundConsistency()} is required before launching the resolution,
     * this will not be done automatically.
     *
     * @param s integer variables, starting times
     * @param p integer variables, processing times
     * @param e integer variables, ending times
     * @return a matrix task variable.
     */
    default Task[][] taskVarMatrix(IntVar[][] s, IntVar[][] p, IntVar[][] e) {
        if (s.length != p.length || s.length != e.length) {
            throw new SolverException("Wrong arrays size");
        }
        Task[][] tasks = new Task[s.length][];
        for (int i = 0; i < s.length; i++) {
            tasks[i] = taskVarArray(s[i], p[i], e[i]);
        }
        return tasks;
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
        return realVar(CSTE_NAME + value, value);
    }

    /**
     * Create a constant real variable equal to <i>value</i>
     * @param name name of the variable
     * @param value value of the variable
     * @return a constant RealVar of domain [<i>value</i>,<i>value</i>]
     */
    default RealVar realVar(String name, double value) {
        return new FixedRealVarImpl(name, value, ref());
    }

    /**
     * Creates a constant real variable equal to <i>value</i>
     * @param value constant value of the variable
     * @param precision double precision (e.g., 0.00001d)
     * @return a constant RealVar of domain {<i>value</i>}
     */
    default RealVar realVar(double value, double precision) {
        return realVar(CSTE_NAME + value, value, value, precision);
    }

    /**
     * Creates a real variable, taking its domain in [<i>lb</i>, <i>ub</i>]
     * @param lb initial domain lower bound
     * @param ub initial domain upper bound
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain [<i>lb</i>, <i>ub</i>]
     */
    default RealVar realVar(double lb, double ub, double precision) {
        return realVar(generateName("RV_"), lb, ub, precision);
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
        return new RealVarImpl(name, lb, ub, precision, ref());
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
        return realVarArray(generateName("RV_"), size, lb, ub, precision);
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
        return realVarMatrix(generateName("RV_"), dim1, dim2, lb, ub, precision);
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
        return setVar(generateName("RV_"), lb, ub);
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar setVar(int... value) {
        StringBuilder name = new StringBuilder(CSTE_NAME + "{");
        for (int i = 0; i < value.length; i++) {
            name.append(value[i]).append(i < value.length - 1 ? ", " : "");
        }
        name.append("}");
        return setVar(name.toString(), value);
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
        return new SetVarImpl(name, lb, SetType.BITSET, ub, SetType.BITSET, ref());
    }

    /**
     * Creates a constant set variable, equal to <i>value</i>
     * @param name name of the variable
     * @param value value of the set variable, e.g. {0,4,9}
     * @return a constant SetVar of domain {<i>value</i>}
     */
    default SetVar setVar(String name, int... value) {
        if (value == null) value = new int[]{};
        return new SetVarImpl(name, value, ref());
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
        return setVarArray(generateName("SV_"), size, lb, ub);
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
        return setVarMatrix(generateName("SV_"), dim1, dim2, lb, ub);
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
        if (MIN <= Integer.MIN_VALUE || MAX >= Integer.MAX_VALUE) {
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

    /**
     * Return a generated short string, prefixed with {@link Settings#defaultPrefix()}
     * and followed with a single-use number.
     * @return generated String to name internally created variables
     */
    default String generateName() {
        return "TMP_" + ref().nextNameId();
    }

    /**
     * Return a generated short string prefixed with <i>prefix</i>
     * and followed with a single-use number.
     *
     * @param prefix the prefix name
     * @return String
     */
    default String generateName(String prefix) {
        return prefix + ref().nextNameId();
    }

}