/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;


/**
 * Utility class to store constraint's name. Each name should be consistent with the semantic. Two
 * constraint with the same semantic should have the same name. By convention, names are in capital
 * letters, '_' separates words in a name.
 *
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 21/09/2017.
 */
@SuppressWarnings("ALL")
public class ConstraintsName {

    public static final String TRUE = "TRUE";

    public static final String FALSE = "FALSE";

    public static final String ARITHM = "ARITHM";

    public static final String ABSOLUTE = "ABSOLUTE";

    public static final String DISTANCE = "DISTANCE";

    public static final String SQUARE = "SQUARE";

    public static final String TABLE = "TABLE";

    public static final String TIMES = "TIMES";

    public static final String DIVISION = "DIVISION";

    public static final String MAX = "MAX";

    public static final String MIN = "MIN";

    public static final String ALLDIFFERENT = "ALLDIFFERENT";

    public static final String AMONG = "AMONG";

    public static final String ATLEASTNVALUES = "ATLEASTNVALUES";

    public static final String ATMOSTNVALUES = "ATMOSTNVALUES";

    public static final String BINPACKING = "BINPACKING";

    public static final String BOOLCHANNELING = "BOOLCHANNELING";

    public static final String BITSINTCHANNELING = "BITSINTCHANNELING";

    public static final String CLAUSESINTCHANNELING = "CLAUSESINTCHANNELING";

    public static final String CIRCUIT = "CIRCUIT";

    public static final String CLAUSECONSTRAINT = "CLAUSECONSTRAINT";

    public static final String COUNT = "COUNT";

    public static final String COSTREGULAR = "COSTREGULAR";

    public static final String DIFFN = "DIFFN";

    public static final String DIFFNWITHCUMULATIVE = "DIFFNWITHCUMULATIVE";

    public static final String ELEMENT = "ELEMENT";

    public static final String INVERSECHANNELING = "INVERSECHANNELING";

    public static final String INT_VALUE_PRECEDE = "INT_VALUE_PRECEDE";

    public static final String KNAPSACK = "KNAPSACK";

    public static final String KEYSORT = "KEYSORT";

    public static final String LEXCHAIN = "LEXCHAIN";

    public static final String LEX = "LEX";

    public static final String MDDC = "MDDC";

    public static final String MULTICOSTREGULAR = "MULTICOSTREGULAR";

    public static final String NVALUES = "NVALUES";

    public static final String REGULAR = "REGULAR";

    public static final String SUBCIRCUIT = "SUBCIRCUIT";

    public static final String SUBPATH = "SUBPATH";

    public static final String PATH = "PATH";

    public static final String TREE = "TREE";

    public static final String BASIC_REI = "BASIC_REIF";

    public static final String SETINTVALUESUNION = "SETINTVALUESUNION";

    public static final String SETUNION = "SETUNION";

    public static final String SETINTERSECTION = "SETINTERSECTION";

    public static final String SETSUBSETEQ = "SETSUBSETEQ";

    public static final String SETNBEMPTY = "SETNBEMPTY";

    public static final String SETOFFSET = "SETOFFSET";

    public static final String SETNOTEMPTY = "SETNOTEMPTY";

    public static final String SETSUM = "SETSUM";

    public static final String SETMAX = "SETMAX";

    public static final String SETMIN = "SETMIN";

    public static final String SETBOOLCHANNELING = "SETBOOLCHANNELING";

    public static final String SETINTCHANNELING = "SETINTCHANNELING";

    public static final String SETALLDISJOINT = "SETALLDISJOINT";

    public static final String SETALLDIFFERENT = "SETALLDIFFERENT";

    public static final String SETALLEQUAL = "SETALLEQUAL";

    public static final String SETPARTITION = "SETPARTITION";

    public static final String SETINVERSE = "SETINVERSE";

    public static final String SETSYMMETRIC = "SETSYMMETRIC";

    public static final String SETELEMENT = "SETELEMENT";

    public static final String SETMEMBER = "SETMEMBER";

    public static final String SETNOTMEMBER = "SETNOTMEMBER";

    public static final String REIFICATIONCONSTRAINT = "REIFICATIONCONSTRAINT";

    public static final String SATCONSTRAINT = "SATCONSTRAINT";

    public static final String CUMULATIVE = "CUMULATIVE";

    public static final String GCC = "GCC";

    public static final String NOGOODCONSTRAINT = "NOGOODCONSTRAINT";

    public static final String SUM = "SUM";

    public static final String MIXEDSCALAR = "MIXEDSCALAR";

    public static final String INTEQREAL = "INTEQREAL";

    public static final String REALCONSTRAINT = "REALCONSTRAINT";

    public static final String LOCALCONSTRUCTIVEDISJUNCTION = "LOCALCONSTRUCTIVEDISJUNCTION";

    public static final String OPPOSITE = "OPPOSITE";

    public static final String MEMBER = "MEMBER";

    public static final String NOTMEMBER = "NOTMEMBER";

    public static final String SETCARD = "SETCARD";

    public static final String CONDITION = "CONDITION";

}
