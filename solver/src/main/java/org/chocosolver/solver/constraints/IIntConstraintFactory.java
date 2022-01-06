/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.binary.*;
import org.chocosolver.solver.constraints.binary.element.ElementFactory;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.extension.binary.*;
import org.chocosolver.solver.constraints.extension.nary.*;
import org.chocosolver.solver.constraints.nary.PropDiffN;
import org.chocosolver.solver.constraints.nary.PropIntValuePrecedeChain;
import org.chocosolver.solver.constraints.nary.PropKLoops;
import org.chocosolver.solver.constraints.nary.PropKnapsack;
import org.chocosolver.solver.constraints.nary.alldifferent.AllDifferent;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.PropCondAllDiffInst;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.PropCondAllDiff_AC;
import org.chocosolver.solver.constraints.nary.alldifferentprec.PropAllDiffPrec;
import org.chocosolver.solver.constraints.nary.among.PropAmongGAC;
import org.chocosolver.solver.constraints.nary.automata.CostRegular;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.PropMultiCostRegular;
import org.chocosolver.solver.constraints.nary.automata.PropRegular;
import org.chocosolver.solver.constraints.nary.binPacking.PropBinPacking;
import org.chocosolver.solver.constraints.nary.channeling.PropClauseChanneling;
import org.chocosolver.solver.constraints.nary.channeling.PropEnumDomainChanneling;
import org.chocosolver.solver.constraints.nary.channeling.PropInverseChannelAC;
import org.chocosolver.solver.constraints.nary.channeling.PropInverseChannelBC;
import org.chocosolver.solver.constraints.nary.circuit.*;
import org.chocosolver.solver.constraints.nary.count.PropCountVar;
import org.chocosolver.solver.constraints.nary.count.PropCount_AC;
import org.chocosolver.solver.constraints.nary.cumulative.CumulFilter;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.constraints.nary.element.PropElementV_fast;
import org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality;
import org.chocosolver.solver.constraints.nary.lex.PropLex;
import org.chocosolver.solver.constraints.nary.lex.PropLexChain;
import org.chocosolver.solver.constraints.nary.min_max.*;
import org.chocosolver.solver.constraints.nary.nvalue.*;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.graph.Gci;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.MDRk;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.rules.R;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.rules.R1;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.rules.R3;
import org.chocosolver.solver.constraints.nary.sort.PropKeysorting;
import org.chocosolver.solver.constraints.nary.sum.IntLinCombFactory;
import org.chocosolver.solver.constraints.nary.tree.PropAntiArborescences;
import org.chocosolver.solver.constraints.ternary.*;
import org.chocosolver.solver.constraints.unary.Member;
import org.chocosolver.solver.constraints.unary.NotMember;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.MathUtils;
import org.chocosolver.util.tools.VariableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Interface to make constraints over BoolVar and IntVar
 * <p>
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 * @author Charles Prud'homme
 * @since 4.0.0
 */
public interface IIntConstraintFactory extends ISelf<Model> {

    //##################################################################################################################
    // UNARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Creates an arithmetic constraint : var op cste,
     * where op in {"=", "!=", ">","<",">=","<="}
     *
     * @param var  a variable
     * @param op   an operator
     * @param cste a constant
     */
    default Constraint arithm(IntVar var, String op, int cste) {
        return new Arithmetic(var, Operator.get(op), cste);
    }

    /**
     * Creates a member constraint.
     * Ensures var takes its values in table
     *
     * @param var   an integer variable
     * @param table an array of values
     */
    default Constraint member(IntVar var, int[] table) {
        return new Member(var, new IntIterableRangeSet(table));
    }

    /**
     * Creates a member constraint.
     * Ensures var takes its values in [LB, UB]
     *
     * @param var an integer variable
     * @param lb  the lower bound of the interval
     * @param ub  the upper bound of the interval
     */
    default Constraint member(IntVar var, int lb, int ub) {
        return new Member(var, lb, ub);
    }

    /**
     * Creates a modulo constraint.
     * Ensures X % a = b
     *
     * @param X   an integer variable
     * @param mod the value of the modulo operand
     * @param res the result of the modulo operation
     */
    default Constraint mod(IntVar X, int mod, int res) {
        if (mod == 0) {
            throw new SolverException("a should not be 0 for " + X.getName() + " MOD a = b");
        }
        TIntArrayList list = new TIntArrayList();
        for (int v = X.getLB(); v <= X.getUB(); v = X.nextValue(v)) {
            if (v % mod == res) {
                list.add(v);
            }
        }
        return member(X, list.toArray());
    }

    /**
     * Gets the opposite of a given constraint
     * Works for any constraint, including globals, but the associated performances might be weak
     *
     * @param cstr a constraint
     * @return the opposite constraint of <i>cstr</i>
     */
    default Constraint not(Constraint cstr) {
        return cstr.getOpposite();
    }

    /**
     * Creates a notMember constraint.
     * Ensures var does not take its values in table
     *
     * @param var   an integer variable
     * @param table an array of values
     */
    default Constraint notMember(IntVar var, int[] table) {
        return new NotMember(var, new IntIterableRangeSet(table));
    }

    /**
     * Creates a member constraint.
     * Ensures var takes its values in set
     *
     * @param var an integer variable
     * @param set a set of values
     */
    default Constraint member(IntVar var, IntIterableRangeSet set) {
        return new Member(var, set);
    }

    /**
     * Creates a notMember constraint.
     * Ensures var does not take its values in [lb, UB]
     *
     * @param var an integer variable
     * @param lb  the lower bound of the interval
     * @param ub  the upper bound of the interval
     */
    default Constraint notMember(IntVar var, int lb, int ub) {
        return new NotMember(var, lb, ub);
    }

    /**
     * Creates a notMember constraint.
     * Ensures var does not take its values in set
     *
     * @param var an integer variable
     * @param set a set of values
     */
    default Constraint notMember(IntVar var, IntIterableRangeSet set) {
        return new NotMember(var, set);
    }

    //##################################################################################################################
    //BINARIES #########################################################################################################
    //##################################################################################################################

    /**
     * Creates an absolute value constraint: var1 = |var2|
     */
    default Constraint absolute(IntVar var1, IntVar var2) {
        assert var1.getModel() == var2.getModel();
        return new Constraint(ConstraintsName.ABSOLUTE, new PropAbsolute(var1, var2));
    }

    /**
     * Creates an arithmetic constraint: var1 op var2,
     * where op in {"=", "!=", ">","<",">=","<="}
     *
     * @param var1 first variable
     * @param op   an operator
     * @param var2 second variable
     */
    default Constraint arithm(IntVar var1, String op, IntVar var2) {
        if (var2.isInstantiated()) {
            return arithm(var1, op, var2.getValue());
        }
        if (var1.isInstantiated()) {
            return arithm(var2, Operator.getFlip(op), var1.getValue());
        }
        return new Arithmetic(var1, Operator.get(op), var2);
    }

    /**
     * Creates an arithmetic constraint : var1 op var2,
     * where op in {"=", "!=", ">","<",">=","<="} or {"+", "-", "*", "/"}
     *
     * @param var1 first variable
     * @param op1  an operator
     * @param var2 second variable
     * @param op2  another operator
     * @param cste an operator
     */
    @SuppressWarnings("Duplicates")
    default Constraint arithm(IntVar var1, String op1, IntVar var2, String op2, int cste) {
        if (op1.equals("*") || op1.equals("/") || op2.equals("*") || op2.equals("/")) {
            switch (op1) {
                case "*": // v1 * v2 OP cste
                    if (Operator.EQ.name().equals(op2)) {
                        return times(var1, var2, cste);
                    } else {
                        int[] bounds = VariableUtils.boundsForMultiplication(var1, var2);
                        IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                        ref().times(var1, var2, var4).post();
                        return arithm(var4, op2, cste);
                    }
                case "/":
                    // v1 / v2 OP cste
                    if (Operator.EQ.name().equals(op2)) {
                        return div(var1, var2, ref().intVar(cste));
                    } else {
                        int[] bounds = VariableUtils.boundsForDivision(var1, var2);
                        IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                        ref().div(var1, var2, var4).post();
                        return arithm(var4, op2, cste);
                    }
                default:
                    switch (op2) {
                        default:
                            throw new SolverException("Unknown operators for arithm constraint");
                        case "*": // v1 OP v2 * cste
                            if (Operator.EQ.name().equals(op1)) {
                                return times(var2, cste, var1);
                            } else {
                                int[] bounds = VariableUtils.boundsForMultiplication(var2, ref().intVar(cste));
                                IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                                ref().times(var2, cste, var4).post();
                                return arithm(var1, op1, var4);
                            }
                        case "/":
                            // v1 OP v2 / cste
                            if (Operator.EQ.name().equals(op1)) {
                                return div(var2, ref().intVar(cste), var1);
                            } else {
                                // v1 OP v2 / v3
                                int[] bounds = VariableUtils.boundsForDivision(var2, ref().intVar(cste));
                                IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                                ref().div(var2, ref().intVar(cste), var4).post();
                                return arithm(var1, op1, var4);
                            }
                    }
            }
        } else {
            if (var2.isInstantiated()) {
                if ("+".equals(op1)) {
                    return arithm(var1, op2, cste - var2.getValue());
                } else if ("-".equals(op1)) {
                    return arithm(var1, op2, cste + var2.getValue());
                }
            }
            if (var1.isInstantiated()) {
                if ("+".equals(op1)) {
                    return arithm(var2, op2, cste - var1.getValue());
                } else if ("-".equals(op1)) {
                    return arithm(var2, Operator.getFlip(op2), var1.getValue() - cste);
                }
            }
            return new Arithmetic(var1, Operator.get(op1), var2, Operator.get(op2), cste);
        }
    }

    /**
     * Creates a distance constraint : |var1-var2| op cste
     * <br/>
     * where op can take its value among {"=", ">", "<", "!="}
     */
    default Constraint distance(IntVar var1, IntVar var2, String op, int cste) {
        assert var1.getModel() == var2.getModel();
        Operator operator = Operator.get(op);
        if (operator != Operator.EQ && operator != Operator.GT && operator != Operator.LT && operator != Operator.NQ) {
            throw new SolverException("Unexpected operator for distance");
        }
        return new Constraint(ConstraintsName.DISTANCE, new PropDistanceXYC(ArrayUtils.toArray(var1, var2), operator, cste));
    }

    /**
     * Creates an element constraint: value = table[index-offset]
     *
     * @param value  an integer variable taking its value in table
     * @param table  an array of integer values
     * @param index  an integer variable representing the value of value in table
     * @param offset offset matching index.lb and table[0] (Generally 0)
     */
    default Constraint element(IntVar value, int[] table, IntVar index, int offset) {
        return ElementFactory.detect(value, table, index, offset);
    }

    /**
     * Creates an element constraint: value = table[index]
     *
     * @param value an integer variable taking its value in table
     * @param table an array of integer values
     * @param index an integer variable representing the value of value in table
     */
    default Constraint element(IntVar value, int[] table, IntVar index) {
        return element(value, table, index, 0);
    }

    /**
     * Creates a modulo constraint: X % a = Y
     *
     * @param X   first integer variable
     * @param mod the value of the modulo operand
     * @param Y   second integer variable (result of the modulo operation)
     */
    default Constraint mod(IntVar X, int mod, IntVar Y) {
        if (mod == 0) {
            throw new SolverException("a should not be 0 for " + X.getName() + " MOD a = " + Y.getName());
        }

        if (Y.isInstantiated()) {
            return mod(X, mod, Y.getValue());
        } else if (TuplesFactory.canBeTupled(X, Y)) {
            return table(X, Y, TuplesFactory.modulo(X, mod, Y));
        } else {
            return new Constraint((X.getName() + " MOD " + mod + " = " + Y.getName()), new PropModXY(X, mod, Y));
        }
    }

    /**
     * Creates a square constraint: var1 = var2^2
     * @param var1 a variable
     * @param var2 a variable
     * @return a square constraint
     */
    default Constraint square(IntVar var1, IntVar var2) {
        assert var2.getModel() == var1.getModel();
        if (var2.isInstantiated()) {
            int v1 = var2.getValue();
            if (var1.isInstantiated()) {
                int v2 = var1.getValue();
                if(v1 * v1 == v2){
                    return ref().trueConstraint();
                }else{
                    return ref().falseConstraint();
                }
            } else {
                return ref().arithm(var1, "=", v1*v1);
            }
        } else {
            if (var1.isInstantiated()) {
                int v2 = var1.getValue();
                if (v2 == 0) {
                    return ref().arithm(var2, "=", 0);
                } else {
                    if (v2 > 0 && MathUtils.isPerfectSquare(v2)) {
                        int sqt = (int)Math.sqrt(v2);
                        return ref().member(var2, new int[]{-sqt, sqt});
                    } else {
                        return ref().falseConstraint();
                    }
                }
            } else {
                return new Constraint(ConstraintsName.SQUARE, new PropSquare(var1, var2));
            }
        }
    }

    /**
     * Create a table constraint over a couple of variables var1 and var2
     * <p>
     * Uses AC3rm algorithm by default, except if one variable has a bounded domain.
     * In that case, "CT+" is chosen.
     *
     * @param var1 first variable
     * @param var2 second variable
     */
    default Constraint table(IntVar var1, IntVar var2, Tuples tuples) {
        if (!var1.hasEnumeratedDomain() || !var2.hasEnumeratedDomain()) {
            return table(var1, var2, tuples, "CT+");
        } else {
            return table(var1, var2, tuples, "AC3bit+rm");
        }
    }

    /**
     * Creates a table constraint over a couple of variables var1 and var2:<br/>
     * - <b>AC2001</b>: table constraint which applies the AC2001 algorithm,<br/>
     * - <b>AC3</b>: table constraint which applies the AC3 algorithm,<br/>
     * - <b>AC3rm</b>: table constraint which applies the AC3 rm algorithm,<br/>
     * - <b>AC3bit+rm</b> (default): table constraint which applies the AC3 bit+rm algorithm,<br/>
     * - <b>FC</b>: table constraint which applies forward checking algorithm.<br/>
     *
     * @param var1   first variable
     * @param var2   second variable
     * @param tuples the relation between the two variables, among {"AC3", "AC3rm", "AC3bit+rm", "AC2001", "CT+", "FC"}
     */
    default Constraint table(IntVar var1, IntVar var2, Tuples tuples, String algo) {
        Propagator<IntVar> p;
        if (tuples.allowUniversalValue()) {
            p = new PropCompactTableStar(new IntVar[]{var1, var2}, tuples);
        } else {
            switch (algo) {
                case "CT+":
                    p = new PropCompactTable(new IntVar[]{var1, var2}, tuples);
                    break;
                case "AC2001":
                    p = new PropBinAC2001(var1, var2, tuples);
                    break;
                case "FC":
                    p = new PropBinFC(var1, var2, tuples);
                    break;
                case "AC3":
                    p = new PropBinAC3(var1, var2, tuples);
                    break;
                case "AC3rm":
                    p = new PropBinAC3rm(var1, var2, tuples);
                    break;
                case "AC3bit+rm":
                    p = new PropBinAC3bitrm(var1, var2, tuples);
                    break;
                default:
                    throw new SolverException("Table algorithm " + algo + " is unkown");
            }
        }
        return new Constraint(ConstraintsName.TABLE, p);
    }

    /**
     * Creates a multiplication constraint: X * Y = Z
     *
     * @param X first variable
     * @param Y a constant
     * @param Z result variable
     */
    default Constraint times(IntVar X, int Y, IntVar Z) {
        if (Y == 0) {
            return arithm(Z, "=", 0);
        } else if (Y == 1) {
            return arithm(X, "=", Z);
        } else if (Y < 0) {
            return times(X.getModel().intMinusView(X), -Y, Z);
        } else {
            return new Constraint(ConstraintsName.TIMES, new PropScale(X, Y, Z));
        }
    }

    /**
     * Creates a multiplication constraint: X * Y = Z
     *
     * @param X first variable
     * @param Y second variable
     * @param Z a constant (result)
     */
    default Constraint times(IntVar X, IntVar Y, int Z) {
        return times(X, Y, X.getModel().intVar(Z));
    }

    //##################################################################################################################
    //TERNARIES ########################################################################################################
    //##################################################################################################################

    /**
     * Creates an arithmetic constraint: var1 op1 var2 op2 var3,
     * where op1 and op2 in {"=", "!=", ">","<",">=","<="} or {"+", "-", "*", "/"}
     *
     * @param var1 first variable
     * @param op1  an operator
     * @param var2 second variable
     * @param op2  another operator
     * @param var3 third variable
     */
    @SuppressWarnings("Duplicates")
    default Constraint arithm(IntVar var1, String op1, IntVar var2, String op2, IntVar var3) {
        switch (op1) {
            case "+":
                return scalar(new IntVar[]{var1, var2}, new int[]{1, 1}, op2, var3);
            case "-":
                return scalar(new IntVar[]{var1, var2}, new int[]{1, -1}, op2, var3);
            case "*":
                // v1 * v2 = v3
                if (Operator.EQ.name().equals(op2)) {
                    return times(var1, var2, var3);
                } else {
                    // v1 * v2 OP v3
                    int[] bounds = VariableUtils.boundsForMultiplication(var1, var2);
                    IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                    ref().times(var1, var2, var4).post();
                    return arithm(var4, op2, var3);
                }
            case "/":
                // v1 / v2 = v3
                if (Operator.EQ.name().equals(op2)) {
                    return div(var1, var2, var3);
                } else {
                    // v1 / v2 OP v3
                    int[] bounds = VariableUtils.boundsForDivision(var1, var2);
                    IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                    ref().div(var1, var2, var4).post();
                    return arithm(var4, op2, var3);
                }
            default:
                switch (op2) {
                    case "*":
                        // v1 = v2 * v3
                        if (Operator.EQ.name().equals(op1)) {
                            return times(var2, var3, var1);
                        } else {
                            // v1 OP v2 * v3
                            int[] bounds = VariableUtils.boundsForMultiplication(var2, var3);
                            IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                            ref().times(var2, var3, var4).post();
                            return arithm(var1, op1, var4);
                        }
                    case "/":
                        // v1 = v2 / v3
                        if (Operator.EQ.name().equals(op1)) {
                            return div(var2, var3, var1);
                        } else {
                            // v1 OP v2 / v3
                            int[] bounds = VariableUtils.boundsForDivision(var2, var3);
                            IntVar var4 = ref().intVar(bounds[0], bounds[1]);
                            ref().div(var2, var3, var4).post();
                            return arithm(var1, op1, var4);
                        }
                    case "+":
                        return scalar(new IntVar[]{var1, var3}, new int[]{1, -1}, op1, var2);
                    case "-":
                        return scalar(new IntVar[]{var1, var3}, new int[]{1, 1}, op1, var2);
                    default:
                        throw new SolverException("Unknown operators for arithm constraint");
                }
        }
    }

    /**
     * Creates a distance constraint: |var1-var2| op var3
     * <br/>
     * where op can take its value among {"=", ">", "<"}
     *
     * @param var1 first variable
     * @param var2 second variable
     * @param op   an operator
     * @param var3 resulting variable
     */
    default Constraint distance(IntVar var1, IntVar var2, String op, IntVar var3) {
        switch (Operator.get(op)) {
            case EQ:
                return new Constraint(ConstraintsName.DISTANCE, new PropEQDistanceXYZ(ArrayUtils.toArray(var1, var2, var3)));
            case LE:
                return new Constraint(ConstraintsName.DISTANCE, new PropLEDistanceXYZ(ArrayUtils.toArray(var1, var2, var3)));
            case GE:
                return new Constraint(ConstraintsName.DISTANCE, new PropGEDistanceXYZ(ArrayUtils.toArray(var1, var2, var3)));
            case LT:
                return new Constraint(ConstraintsName.DISTANCE, new PropLTDistanceXYZ(ArrayUtils.toArray(var1, var2, var3)));
            case GT:
                return new Constraint(ConstraintsName.DISTANCE, new PropGTDistanceXYZ(ArrayUtils.toArray(var1, var2, var3)));
            default:
                throw new SolverException("Unexpected operator for distance: " + op);
        }
    }

    /**
     * Creates an euclidean division constraint.
     * Ensures dividend / divisor = result, rounding towards 0
     * Also ensures divisor != 0
     *
     * @param dividend dividend
     * @param divisor  divisor
     * @param result   result
     */
    default Constraint div(IntVar dividend, IntVar divisor, IntVar result) {
        return new Constraint(ConstraintsName.DIVISION, new PropDivXYZ(dividend, divisor, result));
    }

    /**
     * Creates a maximum constraint : max = max(var1, var2)
     * (Bound Consistency)
     *
     * @param max  a variable
     * @param var1 a variable
     * @param var2 a variable
     */
    default Constraint max(IntVar max, IntVar var1, IntVar var2) {
        return new Constraint(ConstraintsName.MAX, new PropMaxBC(max, var1, var2));
    }

    /**
     * Creates a minimum constraint:  min = min(var1, var2)
     * (Bound Consistency)
     *
     * @param min  a variable
     * @param var1 a variable
     * @param var2 a variable
     */
    default Constraint min(IntVar min, IntVar var1, IntVar var2) {
        return new Constraint(ConstraintsName.MIN, new PropMinBC(min, var1, var2));
    }

    /**
     * <p>
     * Ensures X % Y = Z.
     * </p>
     * <p>
     * Creates a modulo constraint, that uses truncated division:
     * the quotient is defined by truncation q = trunc(a/n)
     * and the remainder would have same sign as the dividend.
     * The quotient is rounded towards zero: equal to the first integer
     * in the direction of zero from the exact rational quotient.
     * </p>
     *
     * @param X first variable
     * @param Y second variable
     * @param Z result
     */
    default Constraint mod(IntVar X, IntVar Y, IntVar Z) {
        if (Y.isInstantiated() && Y.getValue() == 0) {
            throw new SolverException("Y variable should not be instantiated to 0 for constraint "
                    + X.getName() + " MOD " + Y.getName() + " = " + Z.getName());
        }

        if (Y.isInstantiated()) {
            return mod(X, Y.getValue(), Z);
        } else if (TuplesFactory.canBeTupled(X, Y, Z)) {
            return table(new IntVar[]{X, Y, Z}, TuplesFactory.modulo(X, Y, Z));
        } else {
            return new Constraint(X.getName() + " MOD " + Y.getName() + " = " + Z.getName(), new PropModXYZ(X, Y, Z));
        }
    }

    /**
     * Creates a multiplication constraint: X * Y = Z
     *
     * @param X first variable
     * @param Y second variable
     * @param Z result variable
     */
    @SuppressWarnings("SuspiciousNameCombination")
    default Constraint times(IntVar X, IntVar Y, IntVar Z) {
        if (Y.isInstantiated()) {
            return times(X, Y.getValue(), Z);
        } else if (X.isInstantiated()) {
            return times(Y, X.getValue(), Z);
        } else if (TuplesFactory.canBeTupled(X, Y, Z)) {
            return table(new IntVar[]{X, Y, Z}, TuplesFactory.times(X, Y, Z));
        } else {
            return new Constraint(ConstraintsName.TIMES, new PropTimesNaive(X, Y, Z));
        }
    }

    //##################################################################################################################
    //GLOBALS ##########################################################################################################
    //##################################################################################################################

    /**
     * Creates an allDifferent constraint.
     * Ensures that all variables from vars take a different value.
     * Uses BC plus a probabilistic AC propagator to get a compromise between BC and AC
     *
     * @param vars list of variables
     */
    default Constraint allDifferent(IntVar... vars) {
        return allDifferent(vars, "DEFAULT");
    }

    /**
     * Creates an allDifferent constraint.
     * Ensures that all variables from vars take a different value.
     * The consistency level should be chosen among "BC", "AC" and "DEFAULT".
     *
     * @param vars        list of variables
     * @param CONSISTENCY consistency level, among {"BC", "AC_REGIN", "AC", "AC_ZHANG", "DEFAULT"}
     *                    <p>
     *                    <b>BC</b>:
     *                    Based on: "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint"</br>
     *                    A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek
     *                    <br/>
     *                    <b>AC_REGIN</b>:
     *                    Uses Regin algorithm
     *                    Runs in O(m.n) worst case time for the initial propagation and then in O(n+m) on average.
     *                    <p>
     *                    <b>AC, AC_ZHANG</b>:
     *                    Uses Zhang improvement of Regin algorithm
     *                    <p>
     *                    <b>DEFAULT</b>:
     *                    <br/>
     *                    Uses BC plus a probabilistic AC_ZHANG propagator to get a compromise between BC and AC_ZHANG
     */
    default Constraint allDifferent(IntVar[] vars, String CONSISTENCY) {
        if (vars.length <= 1) return ref().trueConstraint();
        return new AllDifferent(vars, CONSISTENCY);
    }

    /**
     * Creates an allDifferent constraint subject to the given condition. More precisely:
     * <p>
     * IF <code>singleCondition</code>
     * for all X,Y in vars, condition(X) => X != Y
     * ELSE
     * for all X,Y in vars, condition(X) AND condition(Y) => X != Y
     *
     * @param vars            collection of variables
     * @param condition       condition defining which variables should be constrained
     * @param singleCondition specifies how to apply filtering
     */
    default Constraint allDifferentUnderCondition(IntVar[] vars, Condition condition, boolean singleCondition) {
        if (singleCondition) {
            return new Constraint(ConstraintsName.ALLDIFFERENT,
                    new PropCondAllDiffInst(vars, condition, singleCondition),
                    new PropCondAllDiff_AC(vars, condition)
            );
        }
        return new Constraint(ConstraintsName.ALLDIFFERENT, new PropCondAllDiffInst(vars, condition, singleCondition));
    }

    /**
     * Creates an allDifferent constraint for variables that are not equal to 0.
     * There can be multiple variables equal to 0.
     *
     * @param vars collection of variables
     */
    default Constraint allDifferentExcept0(IntVar[] vars) {
        return allDifferentUnderCondition(vars, Condition.EXCEPT_0, true);
    }

    /**
     * Creates an AllDiffPrec constraint. The predecessors and successors matrix are built as following:
     * with n = |variables|, for all i in [0,n-1], if there is k such that predecessors[i][k] = j then variables[j] is a predecessor of variables[i].
     * Similarly, with n = |variables|, for all i in [0,n-1], if there is k such that successors[i][k] = j then variables[j] is a successor of variables[i].
     * The matrix should be built such that, if variables[i] is a predecessor of variables[j], then i is in successors[j] and vice versa.
     *
     * filter should be one of the following (depending on the wanted filtering algorithm): BESSIERE, GREEDY, GREEDY_RC, GODET_RC, GODET_BC or DEFAULT (which is GODET_BC).
     *
     * @param variables the variables
     * @param predecessors the predecessors matrix
     * @param successors the successors matrix
     * @param filter the name of the filtering scheme
     */
    default Constraint allDiffPrec(IntVar[] variables, int[][] predecessors, int[][] successors, String filter) {
        return new Constraint(
            ConstraintsName.ALLDIFFPREC,
            new PropAllDiffPrec(variables, predecessors, successors, filter)
        );
    }

    /**
     * Creates an AllDiffPrec constraint. The predecessors and successors matrix are built as following:
     * with n = |variables|, for all i in [0,n-1], if there is k such that predecessors[i][k] = j then variables[j] is a predecessor of variables[i].
     * Similarly, with n = |variables|, for all i in [0,n-1], if there is k such that successors[i][k] = j then variables[j] is a successor of variables[i].
     * The matrix should be built such that, if variables[i] is a predecessor of variables[j], then i is in successors[j] and vice versa.
     *
     * @param variables    the variables
     * @param predecessors the predecessors matrix
     * @param successors   the successors matrix
     */
    default Constraint allDiffPrec(IntVar[] variables, int[][] predecessors, int[][] successors) {
        return allDiffPrec(variables, predecessors, successors, "GODET_BC");
    }

    /**
     * Creates an AllDiffPrec constraint. The precedence matrix is built as following:
     * with n = |variables|, for all i,j in [0,n-1], precedence[i][j] = true iff precedence[j][i] = false iff variables[i] precedes variables[j].
     *
     * filter should be one of the following (depending on the wanted filtering algorithm): BESSIERE, GREEDY, GREEDY_RC, GODET_RC, GODET_BC or DEFAULT (which is GODET_BC).
     *
     * @param variables the variables
     * @param precedence the precedence matrix
     * @param filter the name of the filtering scheme
     */
    default Constraint allDiffPrec(IntVar[] variables, boolean[][] precedence, String filter) {
        return new Constraint(
            ConstraintsName.ALLDIFFPREC,
            new PropAllDiffPrec(variables, precedence, filter)
        );
    }

    /**
     * Creates an AllDiffPrec constraint. The precedence matrix is built as following:
     * with n = |variables|, for all i,j in [0,n-1], precedence[i][j] = true iff precedence[j][i] = false iff variables[i] precedes variables[j].
     *
     * @param variables  the variables
     * @param precedence the precedence matrix
     */
    default Constraint allDiffPrec(IntVar[] variables, boolean[][] precedence) {
        return allDiffPrec(variables, precedence, "GODET_BC");
    }

    /**
     * Creates an allEqual constraint.
     * Ensures that all variables from vars take the same value.
     *
     * @param vars list of variables
     */
    default Constraint allEqual(IntVar... vars) {
        return atMostNValues(vars, ref().intVar(1), false);
    }

    /**
     * Creates a notAllEqual constraint.
     * Ensures that all variables from vars take more than a single value.
     *
     * @param vars list of variables
     */
    default Constraint notAllEqual(IntVar... vars) {
        return atLeastNValues(vars, ref().intVar(2), false);
    }

    /**
     * Creates an among constraint.
     * nbVar is the number of variables of the collection vars that take their value in values.
     * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
     * <br/>
     * Propagator :
     * C. Bessiere, E. Hebrard, B. Hnich, Z. Kiziltan, T. Walsh,
     * Among, common and disjoint Constraints
     * CP-2005
     *
     * @param nbVar  a variable
     * @param vars   vector of variables
     * @param values set of values
     */
    default Constraint among(IntVar nbVar, IntVar[] vars, int[] values) {
        int[] vls = new TIntHashSet(values).toArray(); // remove double occurrences
        Arrays.sort(vls);                              // sort
        return new Constraint(ConstraintsName.AMONG, new PropAmongGAC(ArrayUtils.concat(vars, nbVar), vls));
    }

    /**
     * Creates an and constraint that is satisfied if all boolean variables in <i>bools</i> are true
     *
     * @param bools an array of boolean variable
     * @return a constraint and ensuring that variables in <i>bools</i> are all set to true
     */
    default Constraint and(BoolVar... bools) {
        Model s = bools[0].getModel();
        IntVar sum = s.intVar(0, bools.length, true);
        s.sum(bools, "=", sum).post();
        return s.arithm(sum, "=", bools.length);
    }

    /**
     * Creates an and constraint that is satisfied if all constraints in <i>cstrs</i> are satisfied
     * BEWARE: this should not be used to post several constraints at once but in a reification context
     *
     * @param cstrs an array of constraints
     * @return a constraint and ensuring that all constraints in <i>cstrs</i> are satisfied
     */
    default Constraint and(Constraint... cstrs) {
        BoolVar[] bools = new BoolVar[cstrs.length];
        for (int i = 0; i < cstrs.length; i++) {
            bools[i] = cstrs[i].reify();
        }
        return and(bools);
    }

    /**
     * Creates an atLeastNValue constraint.
     * Let N be the number of distinct values assigned to the variables of the vars collection.
     * Enforce condition N >= nValues to hold.
     * <p>
     * This embeds a light propagator by default.
     * Additional filtering algorithms can be added.
     *
     * @param vars    collection of variables
     * @param nValues limit variable
     * @param AC      additional filtering algorithm, domain filtering algorithm derivated from (Soft)AllDifferent
     */
    default Constraint atLeastNValues(IntVar[] vars, IntVar nValues, boolean AC) {
        int[] vals = getDomainUnion(vars);
        if (AC) {
            return new Constraint(ConstraintsName.ATLEASTNVALUES, new PropAtLeastNValues(vars, vals, nValues),
                    new PropAtLeastNValues_AC(vars, vals, nValues));
        } else {
            return new Constraint(ConstraintsName.ATLEASTNVALUES, new PropAtLeastNValues(vars, vals, nValues));
        }
    }

    /**
     * Creates an atMostNValue constraint.
     * Let N be the number of distinct values assigned to the variables of the vars collection.
     * Enforce condition N <= nValues to hold.
     * <p>
     * This embeds a light propagator by default.
     * Additional filtering algorithms can be added.
     *
     * @param vars    collection of variables
     * @param nValues limit variable
     * @param STRONG  "AMNV<Gci|MDRk|R13>" Filters the conjunction of AtMostNValue and disequalities
     *                (see Fages and Lap&egrave;gue Artificial Intelligence 2014)
     *                automatically detects disequalities and allDifferent constraints.
     *                Presumably useful when nValues must be minimized.
     */
    default Constraint atMostNValues(IntVar[] vars, IntVar nValues, boolean STRONG) {
        int[] vals = getDomainUnion(vars);
        if (STRONG) {
            Gci gci = new Gci(vars);
            R[] rules = new R[]{new R1(), new R3(vars.length, nValues.getModel())};
            return new Constraint(ConstraintsName.ATMOSTNVALUES, new PropAtMostNValues(vars, vals, nValues),
                    new PropAMNV(vars, nValues, gci, new MDRk(gci), rules));
        } else {
            return new Constraint(ConstraintsName.ATMOSTNVALUES, new PropAtMostNValues(vars, vals, nValues));
        }
    }

    /**
     * Creates a BinPacking constraint.
     * Bin Packing formulation:
     * forall b in [0,binLoad.length-1],
     * binLoad[b]=sum(itemSize[i] | i in [0,itemSize.length-1], itemBin[i] = b+offset
     * forall i in [0,itemSize.length-1], itemBin is in [offset,binLoad.length-1+offset],
     *
     * @param itemBin  IntVar representing the bin of each item
     * @param itemSize int representing the size of each item
     * @param binLoad  IntVar representing the load of each bin (i.e. the sum of the size of the items in it)
     * @param offset   0 by default but typically 1 if used within MiniZinc
     *                 (which counts from 1 to n instead of from 0 to n-1)
     */
    default Constraint binPacking(IntVar[] itemBin, int[] itemSize, IntVar[] binLoad, int offset) {
        if (itemBin.length != itemSize.length) {
            throw new SolverException("itemBin and itemSize arrays should have same size");
        }
        Model model = itemBin[0].getModel();
        // redundant filtering
        int sum = 0;
        for (int i = 0; i < itemSize.length; i++) {
            sum += itemSize[i];
        }

        int maxCapa = binLoad[0].getUB();
        for (int j = 1; j < binLoad.length; j++) {
            maxCapa = Math.max(maxCapa, binLoad[j].getUB());
        }
        int thresholdCapa = (int) Math.ceil(1.0 * maxCapa / 2);
        List<IntVar> list = new ArrayList<>(itemBin.length);
        for (int i = 0; i < itemBin.length; i++) {
            if (itemSize[i] > thresholdCapa) {
                list.add(itemBin[i]);
            }
        }

        return Constraint.merge(
                ConstraintsName.BINPACKING,
                new Constraint(ConstraintsName.BINPACKING, new PropBinPacking(itemBin, itemSize, binLoad, offset)),
                model.sum(binLoad, "=", sum),
                model.allDifferent(list.toArray(new IntVar[0]))
        );
    }

    /**
     * Creates an channeling constraint between an integer variable and a set of boolean variables.
     * Maps the boolean assignments variables bVars with the standard assignment variable var. <br>
     * var = i <-> bVars[i-offset] = 1
     *
     * @param bVars  array of boolean variables
     * @param var    observed variable. Should presumably have an enumerated domain
     * @param offset 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     */
    default Constraint boolsIntChanneling(BoolVar[] bVars, IntVar var, int offset) {
        if (var.hasEnumeratedDomain()) {
            return new Constraint(ConstraintsName.BOOLCHANNELING, new PropEnumDomainChanneling(bVars, var, offset));
        } else {
            IntVar enumV = var.getModel().intVar(var.getName() + "_enumImage", var.getLB(), var.getUB(), false);
            enumV.eq(var).post();
            return new Constraint(ConstraintsName.BOOLCHANNELING,
                    new PropEnumDomainChanneling(bVars, enumV, offset)
            );
        }
    }

    /**
     * Creates an channeling constraint between an integer variable and a set of bit variables.
     * Ensures that var = 2<sup>0</sup>*BIT_1 + 2<sup>1</sup>*BIT_2 + ... 2<sup>n-1</sup>*BIT_n.
     * <br/>
     * BIT_1 is related to the first bit of OCTET (2^0),
     * BIT_2 is related to the first bit of OCTET (2^1), etc.
     * <br/>
     * The upper bound of var is given by 2<sup>n</sup>, where n is the size of the array bits.
     *
     * @param bits the array of bits
     * @param var  the numeric value
     * @implNote This constraint is transformed into a table constraint
     * @see #table(IntVar[], Tuples)
     */
    default Constraint bitsIntChanneling(BoolVar[] bits, IntVar var) {
        int s = bits.length;
        int max = (int) Math.pow(2, s) - 1;
        Tuples tuples = new Tuples(true);
        for (int v = 0; v <= max; v++) {
            if (var.contains(v)) {
                int[] t = new int[s + 1];
                for (int b = 0; b < s; b++) {
                    t[b] = ((v >>> b) & 1) != 0 ? 1 : 0;
                }
                t[s] = v;
                tuples.add(t);
            }
        }
        return table(ArrayUtils.append(bits, new IntVar[]{var}), tuples);
    }

    /**
     * Creates an channeling constraint between an integer variable and a set of clauses.
     * Link each value from the domain of var to two boolean variable:
     * one reifies the equality to the i^th value of the variable domain,
     * the other reifies the less-or-equality to the i^th value of the variable domain.
     * Contract: eVars.lenght == lVars.length == var.getUB() - var.getLB() + 1
     * Contract: var is not a boolean variable
     *
     * @param var   an Integer variable
     * @param eVars array of EQ boolean variables
     * @param lVars array of LQ boolean variables
     */
    default Constraint clausesIntChanneling(IntVar var, BoolVar[] eVars, BoolVar[] lVars) {
        return new Constraint(ConstraintsName.CLAUSESINTCHANNELING, new PropClauseChanneling(var, eVars, lVars));
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/> the elements of vars define a covering circuit
     * <p/> where vars[i] = offset+j means that j is the successor of i.
     * <p>
     * Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> Strongly Connected Components based filtering (Cambazar & Bourreau JFPC'06 and Fages and Lorca TechReport'12)
     *
     * @param vars vector of variables which take their value in [offset,offset+|vars|-1]
     * @return a circuit constraint
     */
    default Constraint circuit(IntVar[] vars) {
        return circuit(vars, 0);
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/> the elements of vars define a covering circuit
     * <p/> where vars[i] = offset+j means that j is the successor of i.
     * <p>
     * Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> Strongly Connected Components based filtering (Cambazar & Bourreau JFPC'06 and Fages and Lorca TechReport'12)
     *
     * @param vars   vector of variables which take their value in [offset,offset+|vars|-1]
     * @param offset 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @return a circuit constraint
     */
    default Constraint circuit(IntVar[] vars, int offset) {
        return circuit(vars, offset, CircuitConf.RD);
    }

    /**
     * Creates a circuit constraint which ensures that
     * <p/> the elements of vars define a covering circuit
     * <p/> where vars[i] = offset+j means that j is the successor of i.
     * <p>
     * Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> Strongly Connected Components based filtering (Cambazard & Bourreau JFPC'06 and Fages and Lorca TechReport'12)
     * <p/> See Fages PhD Thesis (2014) for more information
     *
     * @param vars   vector of variables which take their value in [offset,offset+|vars|-1]
     * @param offset 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @param conf   filtering options
     * @return a circuit constraint
     */
    default Constraint circuit(IntVar[] vars, int offset, CircuitConf conf) {
        Propagator<?>[] props;
        if (conf == CircuitConf.LIGHT) {
            props = new Propagator[]{new PropNoSubtour(vars, offset)};
        } else {
            props = new Propagator[]{
                    new PropNoSubtour(vars, offset),
                    new PropCircuit_ArboFiltering(vars, offset, conf),
                    new PropCircuit_AntiArboFiltering(vars, offset, conf),
                    new PropCircuitSCC(vars, offset, conf)
            };
        }
        Constraint alldiff = allDifferent(vars, "AC");
        alldiff.ignore();
        return new Constraint(ConstraintsName.CIRCUIT, ArrayUtils.append(alldiff.propagators, props));
    }

    /**
     * Creates a regular constraint that supports a cost function.
     * Ensures that the assignment of a sequence of variables is recognized by costAutomaton, a deterministic finite automaton,
     * and that the sum of the costs associated to each assignment is bounded by the cost variable.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     *
     * @param vars          sequence of variables
     * @param cost          cost variable
     * @param costAutomaton a deterministic finite automaton defining the regular language and the costs
     *                      Can be built with method CostAutomaton.makeSingleResource(...)
     */
    default Constraint costRegular(IntVar[] vars, IntVar cost, ICostAutomaton costAutomaton) {
        return new CostRegular(vars, cost, costAutomaton);
    }

    /**
     * Creates a count constraint.
     * Let N be the number of variables of the vars collection assigned to value value;
     * Enforce condition N = limit to hold.
     * <p>
     *
     * @param value an int
     * @param vars  a vector of variables
     * @param limit a variable
     */
    default Constraint count(int value, IntVar[] vars, IntVar limit) {
        return new Constraint(ConstraintsName.COUNT, new PropCount_AC(vars, value, limit));
    }

    /**
     * Creates a count constraint.
     * Let N be the number of variables of the vars collection assigned to value value;
     * Enforce condition N = limit to hold.
     * <p>
     *
     * @param value a variable
     * @param vars  a vector of variables
     * @param limit a variable
     */
    default Constraint count(IntVar value, IntVar[] vars, IntVar limit) {
        if (value.isInstantiated()) {
            return count(value.getValue(), vars, limit);
        } else if (value.hasEnumeratedDomain()) {
            return new Constraint(ConstraintsName.COUNT, new PropCountVar(vars, value, limit));
        } else {
            Model model = value.getModel();
            IntVar Evalue = model.intVar(model.generateName("COUNT_"), value.getLB(), value.getUB(), false);
            Evalue.eq(value).post();
            return new Constraint(ConstraintsName.COUNT,
                    new PropCountVar(vars, Evalue, limit));
        }
    }

    /**
     * Creates a cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * Task duration and height should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param tasks    Task objects containing start, duration and end variables
     * @param heights  integer variables representing the resource consumption of each task
     * @param capacity integer variable representing the resource capacity
     * @return a cumulative constraint
     */
    default Constraint cumulative(Task[] tasks, IntVar[] heights, IntVar capacity) {
        return cumulative(tasks, heights, capacity, true);
    }

    /**
     * Creates a cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * Task duration and height should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param tasks       Task objects containing start, duration and end variables
     * @param heights     integer variables representing the resource consumption of each task
     * @param capacity    integer variable representing the resource capacity
     * @param incremental specifies if an incremental propagation should be applied
     * @return a cumulative constraint
     */
    default Constraint cumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean incremental) {
        return cumulative(tasks, heights, capacity, incremental, Cumulative.Filter.DEFAULT.make(tasks.length));
    }

    /**
     * Creates a cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * Task duration and height should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param tasks       Task objects containing start, duration and end variables
     * @param heights     integer variables representing the resource consumption of each task
     * @param capacity    integer variable representing the resource capacity
     * @param incremental specifies if an incremental propagation should be applied
     * @param filters     specifies which filtering algorithms to apply
     * @return a cumulative constraint
     */
    default Constraint cumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean incremental, Cumulative.Filter... filters) {
        return cumulative(tasks, heights, capacity, incremental, Arrays.stream(filters).map(f -> f.make(tasks.length)).toArray(CumulFilter[]::new));
    }

    /**
     * Creates a cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * Task duration and height should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param tasks       Task objects containing start, duration and end variables
     * @param heights     integer variables representing the resource consumption of each task
     * @param capacity    integer variable representing the resource capacity
     * @param incremental specifies if an incremental propagation should be applied
     * @param filters     specifies which filtering algorithms to apply
     * @return a cumulative constraint
     */
    default Constraint cumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean incremental, CumulFilter... filters) {
        if (tasks.length != heights.length) {
            throw new SolverException("Tasks and heights arrays should have same size");
        }
        int nbUseFull = 0;
        for (int h = 0; h < heights.length; h++) {
            if (heights[h].getUB() > 0 && tasks[h].getDuration().getUB() > 0) {
                nbUseFull++;
            }
        }
        // remove tasks that have no impact on resource consumption
        if (nbUseFull < tasks.length) {
            if (nbUseFull == 0) return arithm(capacity, ">=", 0);
            Task[] T2 = new Task[nbUseFull];
            IntVar[] H2 = new IntVar[nbUseFull];
            int idx = 0;
            for (int h = 0; h < heights.length; h++) {
                if (heights[h].getUB() > 0 && tasks[h].getDuration().getUB() > 0) {
                    T2[idx] = tasks[h];
                    H2[idx] = heights[h];
                    idx++;
                }
            }
            tasks = T2;
            heights = H2;
        }
        return new Cumulative(tasks, heights, capacity, incremental, filters);
    }

    /**
     * Creates and <b>posts</b> a decomposition of a cumulative constraint:
     * Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * Task duration and height should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param starts    starting time of each task
     * @param durations processing time of each task
     * @param heights   resource consumption of each task
     * @param capacity  resource capacity
     */
    default void cumulative(IntVar[] starts, int[] durations, int[] heights, int capacity) {
        int n = starts.length;
        final IntVar[] d = new IntVar[n];
        final IntVar[] h = new IntVar[n];
        final IntVar[] e = new IntVar[n];
        Task[] tasks = new Task[n];
        for (int i = 0; i < n; i++) {
            d[i] = ref().intVar(durations[i]);
            h[i] = ref().intVar(heights[i]);
            e[i] = ref().intVar(starts[i].getName() + "_e",
                    starts[i].getLB() + durations[i],
                    starts[i].getUB() + durations[i],
                    true);
            tasks[i] = new Task(starts[i], d[i], e[i]);
        }
        ref().cumulative(tasks, h, ref().intVar(capacity), false, Cumulative.Filter.NAIVETIME).post();
    }

    /**
     * Creates a diffN constraint. Constrains each rectangle<sub>i</sub>, given by their origins X<sub>i</sub>,Y<sub>i</sub>
     * and sizes width<sub>i</sub>,height<sub>i</sub>, to be non-overlapping.
     *
     * @param X                      collection of coordinates in first dimension
     * @param Y                      collection of coordinates in second dimension
     * @param width                  collection of width (each duration should be > 0)
     * @param height                 collection of height (each height should be >= 0)
     * @param addCumulativeReasoning indicates whether or not redundant cumulative constraints should be put on each dimension (advised)
     * @return a non-overlapping constraint
     */
    default Constraint diffN(IntVar[] X, IntVar[] Y, IntVar[] width, IntVar[] height, boolean addCumulativeReasoning) {
        Model model = X[0].getModel();
        Constraint diffNCons = new Constraint(
                ConstraintsName.DIFFN,
                new PropDiffN(X, Y, width, height)
        );
        if (addCumulativeReasoning) {
            IntVar[] EX = new IntVar[X.length];
            IntVar[] EY = new IntVar[X.length];
            Task[] TX = new Task[X.length];
            Task[] TY = new Task[X.length];
            int minx = Integer.MAX_VALUE / 2;
            int maxx = Integer.MIN_VALUE / 2;
            int miny = Integer.MAX_VALUE / 2;
            int maxy = Integer.MIN_VALUE / 2;
            for (int i = 0; i < X.length; i++) {
                EX[i] = model.intVar(model.generateName("diffN_"), X[i].getLB() + width[i].getLB(), X[i].getUB() + width[i].getUB(), true);
                EY[i] = model.intVar(model.generateName("diffN_"), Y[i].getLB() + height[i].getLB(), Y[i].getUB() + height[i].getUB(), true);
                TX[i] = new Task(X[i], width[i], EX[i]);
                TY[i] = new Task(Y[i], height[i], EY[i]);
                minx = Math.min(minx, X[i].getLB());
                miny = Math.min(miny, Y[i].getLB());
                maxx = Math.max(maxx, X[i].getUB() + width[i].getUB());
                maxy = Math.max(maxy, Y[i].getUB() + height[i].getUB());
            }
            IntVar maxX = model.intVar(model.generateName("diffN_"), minx, maxx, true);
            IntVar minX = model.intVar(model.generateName("diffN_"), minx, maxx, true);
            IntVar diffX = model.intVar(model.generateName("diffN_"), 0, maxx - minx, true);
            IntVar maxY = model.intVar(model.generateName("diffN_"), miny, maxy, true);
            IntVar minY = model.intVar(model.generateName("diffN_"), miny, maxy, true);
            IntVar diffY = model.intVar(model.generateName("diffN_"), 0, maxy - miny, true);
            return Constraint.merge(ConstraintsName.DIFFNWITHCUMULATIVE,
                    diffNCons,
                    min(minX, X), max(maxX, EX), scalar(new IntVar[]{maxX, minX}, new int[]{1, -1}, "=", diffX),
                    cumulative(TX, height, diffY),
                    min(minY, Y), max(maxY, EY), scalar(new IntVar[]{maxY, minY}, new int[]{1, -1}, "=", diffY),
                    cumulative(TY, width, diffX)
            );
        } else {
            return diffNCons;
        }
    }

    /**
     * Creates a element constraint: value = table[index-offset]
     * where table is an array of variables.
     *
     * @param value  value variable
     * @param table  array of variables
     * @param index  index variable in range [offset,offset+|table|-1]
     * @param offset int offset, generally 0
     */
    default Constraint element(IntVar value, IntVar[] table, IntVar index, int offset) {
        if (Stream.of(table).allMatch(Variable::isAConstant)) {
            return element(value, Stream.of(table).mapToInt(IntVar::getValue).toArray(), index, offset);
        } else {
            // uses two propagator to perform a fix point
            return new Constraint(
                    ConstraintsName.ELEMENT,
                    new PropElementV_fast(value, table, index, offset));
        }
    }

    /**
     * Creates a global cardinality constraint (GCC):
     * Each value values[i] should be taken by exactly occurrences[i] variables of vars.
     * <br/>
     * This constraint does not ensure any well-defined level of consistency, yet.
     *
     * @param vars        collection of variables
     * @param values      collection of constrained values
     * @param occurrences collection of cardinality variables
     * @param closed      restricts domains of vars to values if set to true
     */
    default Constraint globalCardinality(IntVar[] vars, int[] values, IntVar[] occurrences, boolean closed) {
        assert values.length == occurrences.length;
        if (!closed) {
            return new GlobalCardinality(vars, values, occurrences);
        } else {
            TIntArrayList toAdd = new TIntArrayList();
            TIntSet givenValues = new TIntHashSet();
            for (int i : values) {
                assert !givenValues.contains(i);
                givenValues.add(i);
            }
            for (IntVar var : vars) {
                int ub = var.getUB();
                for (int k = var.getLB(); k <= ub; k = var.nextValue(k)) {
                    if (!givenValues.contains(k)) {
                        if (!toAdd.contains(k)) {
                            toAdd.add(k);
                        }
                    }
                }
            }
            if (toAdd.size() > 0) {
                int n2 = values.length + toAdd.size();
                int[] v2 = new int[n2];
                IntVar[] cards = new IntVar[n2];
                System.arraycopy(values, 0, v2, 0, values.length);
                System.arraycopy(occurrences, 0, cards, 0, values.length);
                for (int i = values.length; i < n2; i++) {
                    v2[i] = toAdd.get(i - values.length);
                    cards[i] = vars[0].getModel().intVar(0);
                }
                return new GlobalCardinality(vars, v2, cards);
            } else {
                return new GlobalCardinality(vars, values, occurrences);
            }
        }
    }

    /**
     * Creates an inverse channeling between vars1 and vars2:
     * vars1[i] = j <=> vars2[j] = i
     * Performs AC if domains are enumerated.
     * If not, then it works on bounds without guaranteeing BC
     * (enumerated domains are strongly recommended)
     * <p>
     * Beware you should have |vars1| = |vars2|
     *
     * @param vars1 vector of variables which take their value in [0,|vars2|-1]
     * @param vars2 vector of variables which take their value in [0,|vars1|-1]
     */
    default Constraint inverseChanneling(IntVar[] vars1, IntVar[] vars2) {
        return inverseChanneling(vars1, vars2, 0, 0);
    }

    /**
     * Creates an inverse channeling between vars1 and vars2:
     * vars1[i-offset2] = j <=> vars2[j-offset1] = i
     * Performs AC if domains are enumerated.
     * If not, then it works on bounds without guaranteeing BC
     * (enumerated domains are strongly recommended)
     * <p>
     * Beware you should have |vars1| = |vars2|
     *
     * @param vars1   vector of variables which take their value in [offset1,offset1+|vars2|-1]
     * @param vars2   vector of variables which take their value in [offset2,offset2+|vars1|-1]
     * @param offset1 lowest value in vars1 (most often 0)
     * @param offset2 lowest value in vars2 (most often 0)
     */
    default Constraint inverseChanneling(IntVar[] vars1, IntVar[] vars2, int offset1, int offset2) {
        return inverseChanneling(vars1, vars2, offset1, offset2, false);
    }

    /**
     * Creates an inverse channeling between vars1 and vars2:
     * vars1[i-offset2] = j <=> vars2[j-offset1] = i
     * Performs AC if domains are enumerated.
     * If not, then it works on bounds without guaranteeing BC
     * (enumerated domains are strongly recommended)
     * <p>
     * Beware you should have |vars1| = |vars2|
     *
     * @param vars1   vector of variables which take their value in [offset1,offset1+|vars2|-1]
     * @param vars2   vector of variables which take their value in [offset2,offset2+|vars1|-1]
     * @param offset1 lowest value in vars1 (most often 0)
     * @param offset2 lowest value in vars2 (most often 0)
     * @param ac      use AC level for embedded alldifferent, if false, domains are scanned
     */
    default Constraint inverseChanneling(IntVar[] vars1, IntVar[] vars2, int offset1, int offset2, boolean ac) {
        if (vars1.length != vars2.length)
            throw new SolverException(Arrays.toString(vars1) + " and " + Arrays.toString(vars2) + " should have same size");
        boolean allEnum = true;
        for (int i = 0; i < vars1.length && allEnum; i++) {
            if (!(vars1[i].hasEnumeratedDomain() && vars2[i].hasEnumeratedDomain())) {
                allEnum = false;
            }
        }
        if (ac) {
            allEnum = true;
        }
        Propagator<IntVar> ip = allEnum ? new PropInverseChannelAC(vars1, vars2, offset1, offset2)
                : new PropInverseChannelBC(vars1, vars2, offset1, offset2);
        Constraint alldiff1 = allDifferent(vars1, ac ? "AC" : "");
        alldiff1.ignore();
        Constraint alldiff2 = allDifferent(vars2, ac ? "AC" : "");
        alldiff2.ignore();
        return new Constraint(ConstraintsName.INVERSECHANNELING, ArrayUtils.append(
                alldiff1.getPropagators(),
                alldiff2.getPropagators(),
                new Propagator[]{ip}
        ));
    }

    /**
     * Creates an intValuePrecedeChain constraint.
     * Ensure that if there exists <code>j</code> such that X[j] = T, then, there must exist <code>i</code> < <code>j</code> such that
     * X[i] = S.
     *
     * @param X an array of variables
     * @param S a value
     * @param T another value
     */
    default Constraint intValuePrecedeChain(IntVar[] X, int S, int T) {
        return new Constraint(ConstraintsName.INT_VALUE_PRECEDE, new PropIntValuePrecedeChain(X, S, T));
    }

    /**
     * Creates an intValuePrecedeChain constraint.
     * Ensure that, for each pair of V[k] and V[l] of values in V, such that k < l,
     * if there exists <code>j</code> such that X[j] = V[l], then, there must exist <code>i</code> < <code>j</code> such that
     * X[i] = V[k].
     *
     * @param X array of variables
     * @param V array of (distinct) values
     */
    default Constraint intValuePrecedeChain(IntVar[] X, int[] V) {
        if (V.length > 1) {
            TIntHashSet values = new TIntHashSet();
            PropIntValuePrecedeChain[] ps = new PropIntValuePrecedeChain[V.length - 1];
            values.add(V[0]);
            for (int i = 1; i < V.length; i++) {
                if (values.contains(V[i])) {
                    throw new SolverException("\"int_value_precede\" requires V to be made of distinct values");
                }
                values.add(V[i]);
                ps[i - 1] = new PropIntValuePrecedeChain(X, V[i - 1], V[i]);
            }
            return new Constraint(ConstraintsName.INT_VALUE_PRECEDE, ps);
        } else {
            return ref().trueConstraint();
        }
    }

    /**
     * Creates a knapsack constraint.
     * Ensures that :
     * <br/>- occurrences[i] * weight[i] = weightSum
     * <br/>- occurrences[i] * energy[i] = energySum
     * <br/>and maximizing the value of energySum.
     * <p>
     * <p>
     * A knapsack constraint
     * <a href="http://en.wikipedia.org/wiki/Knapsack_problem">wikipedia</a>:<br/>
     * "Given a set of items, each with a weight and an energy value,
     * determine the count of each item to include in a collection so that
     * the total weight is less than or equal to a given limit and the total value is as large as possible.
     * It derives its name from the problem faced by someone who is constrained by a fixed-size knapsack
     * and must fill it with the most useful items."
     * The limit over weightSum has to be specified either in its domain or with an additional constraint:
     * <pre>
     *     model.post(solver.arithm(weightSum, "<=", limit);
     * </pre>
     *
     * @param occurrences number of occurrences of every item
     * @param weightSum   load of the knapsack
     * @param energySum   profit of the knapsack
     * @param weight      weight of each item (must be >=0)
     * @param energy      energy of each item (must be >=0)
     */
    default Constraint knapsack(IntVar[] occurrences, IntVar weightSum, IntVar energySum,
                                int[] weight, int[] energy) {
        Constraint scalar1 = scalar(occurrences, weight, "=", weightSum);
        scalar1.ignore();
        Constraint scalar2 = scalar(occurrences, energy, "=", energySum);
        scalar2.ignore();
        return new Constraint(ConstraintsName.KNAPSACK, ArrayUtils.append(
                scalar1.propagators,
                scalar2.propagators,
                new Propagator[]{new PropKnapsack(occurrences, weightSum, energySum, weight, energy)}
        ));
    }

    /**
     * Creates a keySort constraint which ensures that the variables of SORTEDvars correspond to the variables
     * of vars according to a permutation stored in PERMvars (optional, can be null).
     * The variables of SORTEDvars are also sorted in increasing order wrt to K-size tuples.
     * The sort is stable, that is, ties are broken using the position of the tuple in vars.
     * <p>
     * <p>
     * For example:<br/>
     * - vars= (<4,2,2>,<2,3,1>,<4,2,1>,<1,3,0>)<br/>
     * - SORTEDvars= (<1,3,0>,<2,3,1>,<4,2,2>,<4,2,1>)<br/>
     * - PERMvars= (4,2,1,3)<br/>
     * - K = 2<br/>
     *
     * @param vars       a tuple of array of variables
     * @param PERMvars   array of permutation variables, domains should be [1,vars.length]  -- Can be null
     * @param SORTEDvars a tuple of array of variables sorted in increasing order
     * @param K          key prefix size (0 &le; k &le; m, where m is the size of the array of variable)
     * @return a keySort constraint
     */
    default Constraint keySort(IntVar[][] vars, IntVar[] PERMvars, IntVar[][] SORTEDvars, int K) {
        if (PERMvars == null) {
            int n = vars.length;
            PERMvars = new IntVar[n];
            for (int p = 0; p < n; p++) {
                PERMvars[p] = vars[0][0].getModel().intVar("p_" + (p + 1), 1, n, true);
            }
        }
        return new Constraint(ConstraintsName.KEYSORT, new PropKeysorting(vars, SORTEDvars, PERMvars, K));
    }

    /**
     * Creates a lexChainLess constraint.
     * For each pair of consecutive vectors vars<sub>i</sub> and vars<sub>i+1</sub> of the vars collection
     * vars<sub>i</sub> is lexicographically strictly less than than vars<sub>i+1</sub>
     *
     * @param vars collection of vectors of variables
     */
    default Constraint lexChainLess(IntVar[]... vars) {
        return new Constraint(ConstraintsName.LEXCHAIN, new PropLexChain(vars, true));
    }

    /**
     * Creates a lexChainLessEq constraint.
     * For each pair of consecutive vectors vars<sub>i</sub> and vars<sub>i+1</sub> of the vars collection
     * vars<sub>i</sub> is lexicographically less or equal than than vars<sub>i+1</sub>
     *
     * @param vars collection of vectors of variables
     */
    default Constraint lexChainLessEq(IntVar[]... vars) {
        return new Constraint(ConstraintsName.LEXCHAIN, new PropLexChain(vars, false));
    }

    /**
     * Creates a lexLess constraint.
     * Ensures that vars1 is lexicographically strictly less than vars2.
     *
     * @param vars1 vector of variables
     * @param vars2 vector of variables
     */
    default Constraint lexLess(IntVar[] vars1, IntVar[] vars2) {
        if (vars1.length != vars2.length) {
            throw new SolverException("vars1 and vars2 should have the same length for lexLess constraint");
        }
        return new Constraint(ConstraintsName.LEX, new PropLex(vars1, vars2, true));
    }

    /**
     * Creates a lexLessEq constraint.
     * Ensures that vars1 is lexicographically less or equal than vars2.
     *
     * @param vars1 vector of variables
     * @param vars2 vector of variables
     */
    default Constraint lexLessEq(IntVar[] vars1, IntVar[] vars2) {
        if (vars1.length != vars2.length) {
            throw new SolverException("vars1 and vars2 should have the same length for lexLess constraint");
        }
        return new Constraint(ConstraintsName.LEX, new PropLex(vars1, vars2, false));
    }

    /**
     * Creates an Argmax constraint.
     * z is the index of the maximum value of the collection of domain variables vars.
     *
     * @param z      a variable
     * @param offset offset wrt to 'z'
     * @param vars   a vector of variables, of size > 0
     */
    default Constraint argmax(IntVar z, int offset, IntVar[] vars) {
        return new Constraint(ConstraintsName.ARGMAX, new PropArgmax(z, offset, vars));
    }

    /**
     * Creates an Argmin constraint.
     * z is the index of the minimum value of the collection of domain variables vars.
     * @implNote This introduces {@link org.chocosolver.solver.variables.view.integer.IntMinusView}[]
     * and returns an {@link #argmax(IntVar, int, IntVar[])} constraint
     * on this views.
     *
     * @param z      a variable
     * @param offset offset wrt to 'z'
     * @param vars   a vector of variables, of size > 0
     */
    default Constraint argmin(IntVar z, int offset, IntVar[] vars) {
        IntVar[] views = Arrays.stream(vars).map(v -> ref().intMinusView(v)).toArray(IntVar[]::new);
        return new Constraint(ConstraintsName.ARGMAX, new PropArgmax(z, offset, views));
    }

    /**
     * Creates a maximum constraint.
     * max is the maximum value of the collection of domain variables vars
     *
     * @param max  a variable
     * @param vars a vector of variables, of size > 0
     */
    default Constraint max(IntVar max, IntVar[] vars) {
        if (vars.length == 1) {
            return max.eq(vars[0]).decompose();
        } else if (vars.length == 2) {
            return max(max, vars[0], vars[1]);
        } else {
            return new Constraint(ConstraintsName.MAX, new PropMax(vars, max));
        }
    }

    /**
     * Creates a maximum constraint.
     * max is the maximum value of the collection of boolean variables vars
     *
     * @param max  a boolean variable
     * @param vars a vector of boolean variables, of size > 0
     */
    default Constraint max(BoolVar max, BoolVar[] vars) {
        if (vars.length == 1) {
            return max.eq(vars[0]).decompose();
        } else if (vars.length == 2) {
            return max(max, vars[0], vars[1]);
        } else {
            return new Constraint(ConstraintsName.MAX, new PropBoolMax(vars, max));
        }
    }

    /**
     * Create a constraint where solutions (tuples) are encoded by a multi-valued decision diagram.
     * The order of the variables in vars is important and must refer to the MDD.
     *
     * @param vars the array of variables
     * @param MDD  the multi-valued decision diagram encoding solutions
     */
    default Constraint mddc(IntVar[] vars, MultivaluedDecisionDiagram MDD) {
        return new Constraint(ConstraintsName.MDDC, new PropLargeMDDC(MDD, vars));
    }

    /**
     * Creates a minimum constraint.
     * min is the minimum value of the collection of domain variables vars
     *
     * @param min  a variable
     * @param vars a vector of variables, of size > 0
     */
    default Constraint min(IntVar min, IntVar[] vars) {
        if (vars.length == 1) {
            return min.eq(vars[0]).decompose();
        } else if (vars.length == 2) {
            return min(min, vars[0], vars[1]);
        } else {
            return new Constraint(ConstraintsName.MIN, new PropMin(vars, min));
        }
    }

    /**
     * Creates a minimum constraint.
     * min is the minimum value of the collection of boolean variables vars
     *
     * @param min  a boolean variable
     * @param vars a vector of boolean variables, of size > 0
     */
    default Constraint min(BoolVar min, BoolVar[] vars) {
        if (vars.length == 1) {
            return min.eq(vars[0]).decompose();
        } else if (vars.length == 2) {
            return min(min, vars[0], vars[1]);
        } else {
            return new Constraint(ConstraintsName.MIN, new PropBoolMin(vars, min));
        }
    }

    /**
     * Creates a regular constraint that supports a multiple cost function.
     * Ensures that the assignment of a sequence of vars is recognized by costAutomaton, a deterministic finite automaton,
     * and that the sum of the cost vector associated to each assignment is bounded by the variable vector costVars.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts).
     * The precision is set to {@code 1e-4d}.
     *
     * @param vars          sequence of variables
     * @param costVars      cost variables
     * @param costAutomaton a deterministic finite automaton defining the regular language and the costs
     *                      Can be built from method CostAutomaton.makeMultiResources(...)
     */
    default Constraint multiCostRegular(IntVar[] vars, IntVar[] costVars, ICostAutomaton costAutomaton) {
        return multiCostRegular(vars, costVars, costAutomaton, 1e-4d);
    }

    /**
     * Creates a regular constraint that supports a multiple cost function.
     * Ensures that the assignment of a sequence of vars is recognized by costAutomaton, a deterministic finite automaton,
     * and that the sum of the cost vector associated to each assignment is bounded by the variable vector costVars.
     * This version allows to specify different costs according to the automaton state at which the assignment occurs
     * (i.e. the transition starts)
     *
     * @param vars          sequence of variables
     * @param costVars      cost variables
     * @param costAutomaton a deterministic finite automaton defining the regular language and the costs
     *                      Can be built from method CostAutomaton.makeMultiResources(...)
     * @param precision     the smallest used double for MCR algorithm
     */
    default Constraint multiCostRegular(IntVar[] vars, IntVar[] costVars,
                                        ICostAutomaton costAutomaton, double precision) {
        return new Constraint(ConstraintsName.MULTICOSTREGULAR,
                new PropMultiCostRegular(vars, costVars, costAutomaton, precision));
    }

    /**
     * Creates an nValue constraint.
     * Let N be the number of distinct values assigned to the variables of the vars collection.
     * Enforce condition N = nValues to hold.
     * <p>
     * This embeds a light propagator by default.
     * Additional filtering algorithms can be added.
     * <p>
     * see atleast_nvalue and atmost_nvalue
     *
     * @param vars    collection of variables
     * @param nValues limit variable
     * @return the conjunction of atleast_nvalue and atmost_nvalue
     */
    default Constraint nValues(IntVar[] vars, IntVar nValues) {
        int[] vals = getDomainUnion(vars);
        Gci gci = new Gci(vars);
        R[] rules = new R[]{new R1(), new R3(vars.length, nValues.getModel())};
        return new Constraint(
            ConstraintsName.NVALUES,
            new PropNValue(vars, nValues),
            // at least
//            new PropAtLeastNValues(vars, vals, nValues),
            // at most
//            new PropAtMostNValues(vars, vals, nValues),
            new PropAMNV(vars, nValues, gci, new MDRk(gci), rules)
        );
    }

    /**
     * Creates an or constraint that is satisfied if at least one boolean variables in <i>bools</i> is true
     *
     * @param bools an array of boolean variable
     * @return a constraint that is satisfied if at least one boolean variables in <i>bools</i> is true
     */
    default Constraint or(BoolVar... bools) {
        Model s = bools[0].getModel();
        IntVar sum = s.intVar(0, bools.length, true);
        s.sum(bools, "=", sum).post();
        return s.arithm(sum, ">=", 1);
    }

    /**
     * Creates an or constraint that is satisfied if at least one constraint in <i>cstrs</i> are satisfied
     *
     * @param cstrs an array of constraints
     * @return a constraint and ensuring that at least one constraint in <i>cstrs</i> are satisfied
     */
    default Constraint or(Constraint... cstrs) {
        BoolVar[] bools = new BoolVar[cstrs.length];
        for (int i = 0; i < cstrs.length; i++) {
            bools[i] = cstrs[i].reify();
        }
        return or(bools);
    }

    /**
     * Creates a path constraint which ensures that
     * <p/> the elements of vars define a covering path from start to end
     * <p/> where vars[i] = j means that j is the successor of i.
     * <p/> Moreover, vars[end] = |vars|
     * <p/> Requires : |vars|>0
     * <p>
     * Filtering algorithms: see circuit constraint
     *
     * @param vars  vector of variables which take their value in [0,|vars|]
     * @param start variable indicating the index of the first variable in the path
     * @param end   variable indicating the index of the last variable in the path
     * @return a path constraint
     */
    default Constraint path(IntVar[] vars, IntVar start, IntVar end) {
        return path(vars, start, end, 0);
    }

    /**
     * Creates a path constraint which ensures that
     * <p/> the elements of vars define a covering path from start to end
     * <p/> where vars[i] = offset+j means that j is the successor of i.
     * <p/> Moreover, vars[end-offset] = |vars|+offset
     * <p/> Requires : |vars|>0
     * <p>
     * Filtering algorithms: see circuit constraint
     *
     * @param vars   vector of variables which take their value in [offset,offset+|vars|]
     * @param start  variable indicating the index of the first variable in the path
     * @param end    variable indicating the index of the last variable in the path
     * @param offset 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @return a path constraint
     */
    default Constraint path(IntVar[] vars, IntVar start, IntVar end, int offset) {
        assert start != null && end != null && vars != null;
        switch (vars.length) {
            case 0:
                throw new SolverException("|vars| Should be strictly greater than 0");
            case 1:
                return Constraint.merge(ConstraintsName.PATH,
                        arithm(start, "=", offset),
                        arithm(end, "=", offset),
                        arithm(vars[0], "=", 1 + offset)
                );
            default:
                if (start == end) {
                    return start.getModel().falseConstraint();
                } else {
                    return Constraint.merge(ConstraintsName.PATH,
                            arithm(start, "!=", end),
                            circuit(ArrayUtils.concat(vars, start), offset),
                            element(end.getModel().intVar(vars.length + offset), vars, end, offset)
                    );
                }
        }
    }

    /**
     * Creates a regular constraint.
     * Enforces the sequence of vars to be a word
     * recognized by the deterministic finite automaton.
     * For example regexp = "(1|2)(3*)(4|5)";
     * The same dfa can be used for different propagators.
     *
     * @param vars      sequence of variables
     * @param automaton a deterministic finite automaton defining the regular language
     */
    default Constraint regular(IntVar[] vars, IAutomaton automaton) {
        return new Constraint(ConstraintsName.REGULAR, new PropRegular(vars, automaton));
    }

    /**
     * Creates a scalar constraint which ensures that Sum(vars[i]*coeffs[i]) operator scalar
     *
     * @param vars     a collection of IntVar
     * @param coeffs   a collection of int, for which |vars|=|coeffs|
     * @param operator an operator in {"=", "!=", ">","<",">=","<="}
     * @param scalar   an integer
     * @return a scalar constraint
     */
    default Constraint scalar(IntVar[] vars, int[] coeffs, String operator, int scalar) {
        return scalar(vars, coeffs, operator, scalar, ref().getSettings().getMinCardForSumDecomposition());
    }

    /**
     * Creates a scalar constraint which ensures that Sum(vars[i]*coeffs[i]) operator scalar
     *
     * @param vars             a collection of IntVar
     * @param coeffs           a collection of int, for which |vars|=|coeffs|
     * @param operator         an operator in {"=", "!=", ">","<",">=","<="}
     * @param scalar           an integer
     * @param minCardForDecomp minimum number of cardinality threshold to a sum constraint to be decomposed
     * @return a scalar constraint
     */
    default Constraint scalar(IntVar[] vars, int[] coeffs, String operator, int scalar, int minCardForDecomp) {
        assert vars.length > 0;
        Model s = vars[0].getModel();
        IntVar scalarVar = s.intVar(scalar);
        return scalar(vars, coeffs, operator, scalarVar, minCardForDecomp);
    }

    /**
     * Creates a scalar constraint which ensures that Sum(vars[i]*coeffs[i]) operator scalar
     *
     * @param vars     a collection of IntVar
     * @param coeffs   a collection of int, for which |vars|=|coeffs|
     * @param operator an operator in {"=", "!=", ">","<",">=","<="}
     * @param scalar   an IntVar
     * @return a scalar constraint
     */
    default Constraint scalar(IntVar[] vars, int[] coeffs, String operator, IntVar scalar) {
        return scalar(vars, coeffs, operator, scalar,
                ref().getSettings().getMinCardForSumDecomposition());
    }

    /**
     * Creates a scalar constraint which ensures that Sum(vars[i]*coeffs[i]) operator scalar
     *
     * @param vars             a collection of IntVar
     * @param coeffs           a collection of int, for which |vars|=|coeffs|
     * @param operator         an operator in {"=", "!=", ">","<",">=","<="}
     * @param scalar           an IntVar
     * @param minCardForDecomp minimum number of cardinality threshold to a sum constraint to be decomposed
     * @return a scalar constraint
     */
    default Constraint scalar(IntVar[] vars, int[] coeffs, String operator, IntVar scalar,
                              int minCardForDecomp) {
        if (vars.length != coeffs.length) {
            throw new SolverException("vars and coeffs arrays should have same size");
        }
        return IntLinCombFactory.reduce(vars, coeffs, Operator.get(operator), scalar, minCardForDecomp);
    }

    /**
     * Creates a sort constraint which ensures that the variables of sortedVars correspond to the variables
     * of vars according to a permutation. The variables of sortedVars are also sorted in increasing order.
     * <p>
     * <p>
     * For example:<br/>
     * - X= (4,2,1,3)<br/>
     * - Y= (1,2,3,4)
     *
     * @param vars       an array of variables
     * @param sortedVars an array of variables sorted in increasing order
     * @return a sort constraint
     */
    default Constraint sort(IntVar[] vars, IntVar[] sortedVars) {
        if (vars.length != sortedVars.length) {
            throw new SolverException("vars and sortedVars arrays should have same size");
        }
        //        return new Constraint("Sort", new PropSort(vars, sortedVars));
        IntVar[][] X = new IntVar[vars.length][1];
        IntVar[][] Y = new IntVar[sortedVars.length][1];
        for (int i = 0; i < vars.length; i++) {
            X[i][0] = vars[i];
            Y[i][0] = sortedVars[i];
        }
        return keySort(X, null, Y, 1);
    }

    /**
     * Creates a subCircuit constraint which ensures that
     * <p/> the elements of vars define a single circuit of subcircuitSize nodes where
     * <p/> vars[i] = offset+j means that j is the successor of i.
     * <p/> and vars[i] = offset+i means that i is not part of the circuit
     * <p/> the constraint ensures that |{vars[i] =/= offset+i}| = subCircuitLength
     * <p>
     * <p/> Filtering algorithms:
     * <p/> subtour elimination : Caseau & Laburthe (ICLP'97)
     * <p/> allDifferent GAC algorithm: R&eacute;gin (AAAI'94)
     * <p/> dominator-based filtering: Fages & Lorca (CP'11) (adaptive scheme by default, see implementation)
     *
     * @param vars             a vector of variables
     * @param offset           0 by default but 1 if used within MiniZinc
     *                         (which counts from 1 to n instead of from 0 to n-1)
     * @param subCircuitLength expected number of nodes in the circuit
     * @return a subCircuit constraint
     */
    default Constraint subCircuit(IntVar[] vars, int offset, IntVar subCircuitLength) {
        int n = vars.length;
        Model model = vars[0].getModel();
        IntVar nbLoops = model.intVar("nLoops", 0, n, true);
        nbLoops.add(subCircuitLength).eq(n).post();
        Constraint alldiff = allDifferent(vars, "AC");
        alldiff.ignore();
        return new Constraint(ConstraintsName.SUBCIRCUIT, ArrayUtils.append(
                alldiff.getPropagators(),
                ArrayUtils.toArray(
                        new PropKLoops(vars, offset, nbLoops),
                        new PropSubcircuit(vars, offset, subCircuitLength),
                        new PropSubcircuitDominatorFilter(vars, offset, true)
                )
        ));
    }

    /**
     * Creates a subPath constraint which ensures that
     * <p/> the elements of vars define a path of SIZE vertices, leading from start to end
     * <p/> where vars[i] = offset+j means that j is the successor of i.
     * <p/> where vars[i] = offset+i means that vertex i is excluded from the path.
     * <p/> Moreover, vars[end-offset] = |vars|+offset
     * <p/> Requires : |vars|>0
     * <p>
     * Filtering algorithms: see subCircuit constraint
     *
     * @param vars   vector of variables which take their value in [offset,offset+|vars|]
     * @param start  variable indicating the index of the first variable in the path
     * @param end    variable indicating the index of the last variable in the path
     * @param offset 0 by default but typically 1 if used within MiniZinc
     *               (which counts from 1 to n instead of from 0 to n-1)
     * @param SIZE   variable indicating the number of variables to belong to the path
     * @return a subPath constraint
     */
    default Constraint subPath(IntVar[] vars, IntVar start, IntVar end, int offset, IntVar SIZE) {
        assert start != null && end != null && vars != null;
        switch (vars.length) {
            case 0:
                throw new SolverException("|vars| Should be strictly greater than 0");
            case 1:
                return Constraint.merge(ConstraintsName.SUBPATH,
                        arithm(start, "=", offset),
                        arithm(end, "=", offset),
                        arithm(vars[0], "=", 1 + offset),
                        arithm(SIZE, "=", 1)
                );
            default:
                return Constraint.merge(ConstraintsName.SUBPATH,
                        arithm(start, "<", vars.length + offset),
                        subCircuit(ArrayUtils.concat(vars, start), offset, end.getModel().intOffsetView(SIZE, 1)),
                        element(end.getModel().intVar(vars.length + offset), vars, end, offset)
                );
        }
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     *
     * @param vars     a collection of IntVar
     * @param operator operator in {"=", "!=", ">","<",">=","<="}
     * @param sum      an integer
     * @return a sum constraint
     */
    default Constraint sum(IntVar[] vars, String operator, int sum) {
        return sum(vars, operator, sum, ref().getSettings().getMinCardForSumDecomposition());
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>x in vars1</sub>x operator &#8721;<sub>y in vars2</sub>y.
     *
     * @param vars1     a collection of IntVar
     * @param operator operator in {"=", "!=", ">","<",">=","<="}
     * @param vars2     a collection of IntVar
     * @return a sum constraint
     */
    default Constraint sum(IntVar[] vars1, String operator, IntVar[] vars2) {
        int[] coeffs = new int[vars1.length+ vars2.length];
        Arrays.fill(coeffs, 0, vars1.length, 1);
        Arrays.fill(coeffs, vars1.length, vars1.length + vars2.length, -1);
        return scalar(ArrayUtils.append(vars1, vars2), coeffs, operator, 0, ref().getSettings().getMinCardForSumDecomposition());
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     *
     * @param vars             a collection of IntVar
     * @param operator         operator in {"=", "!=", ">","<",">=","<="}
     * @param sum              an integer
     * @param minCardForDecomp minimum number of cardinality threshold to a sum constraint to be decomposed
     * @return a sum constraint
     */
    default Constraint sum(IntVar[] vars, String operator, int sum, int minCardForDecomp) {
        assert vars.length > 0;
        Model s = vars[0].getModel();
        IntVar sumVar = s.intVar(sum);
        return IntLinCombFactory.reduce(vars, Operator.get(operator), sumVar, minCardForDecomp);
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     *
     * @param vars     a collection of IntVar
     * @param operator operator in {"=", "!=", ">","<",">=","<="}
     * @param sum      an IntVar
     * @return a sum constraint
     */
    default Constraint sum(IntVar[] vars, String operator, IntVar sum) {
        return sum(vars, operator, sum, ref().getSettings().getMinCardForSumDecomposition());
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     *
     * @param vars             a collection of IntVar
     * @param operator         operator in {"=", "!=", ">","<",">=","<="}
     * @param sum              an IntVar
     * @param minCardForDecomp minimum number of cardinality threshold to a sum constraint to be decomposed
     * @return a sum constraint
     */
    default Constraint sum(IntVar[] vars, String operator, IntVar sum, int minCardForDecomp) {
        return IntLinCombFactory.reduce(vars, Operator.get(operator), sum, minCardForDecomp);
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     * This constraint is much faster than the one over integer variables
     *
     * @param vars a vector of boolean variables
     * @param sum  an integer
     */
    default Constraint sum(BoolVar[] vars, String operator, int sum) {
        assert vars.length > 0;
        Model s = vars[0].getModel();
        IntVar sumVar = s.intVar(sum);
        return sum(vars, operator, sumVar);
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     * This constraint is much faster than the one over integer variables
     *
     * @param vars a vector of boolean variables
     * @param sum  a variable
     */
    default Constraint sum(BoolVar[] vars, String operator, IntVar sum) {
        return sum(vars, operator, sum, ref().getSettings().getMinCardForSumDecomposition());
    }

    /**
     * Creates a sum constraint.
     * Enforces that &#8721;<sub>i in |vars|</sub>vars<sub>i</sub> operator sum.
     * This constraint is much faster than the one over integer variables
     *
     * @param vars             a vector of boolean variables
     * @param sum              a variable
     * @param minCardForDecomp minimum number of cardinality threshold to a sum constraint to be decomposed
     */
    default Constraint sum(BoolVar[] vars, String operator, IntVar sum, int minCardForDecomp) {
        if (sum.getModel().getSettings().enableDecompositionOfBooleanSum()) {
            int[] bounds = VariableUtils.boundsForAddition(vars);
            IntVar p = sum.getModel().intVar(sum.getModel().generateName("RSLT_"), bounds[0], bounds[1], true);
            IntLinCombFactory.reduce(vars, Operator.EQ, p, minCardForDecomp).post();
            return arithm(p, operator, sum);
        } else {
            return IntLinCombFactory.reduce(vars, Operator.get(operator), sum, minCardForDecomp);
        }
    }

    /**
     * Creates a table constraint specifying that the sequence of variables vars must belong to the list of tuples
     * (or must NOT belong in case of infeasible tuples)
     * <p>
     * Default configuration with GACSTR+ algorithm for feasible tuples and GAC3rm otherwise
     *
     * @param vars   variables forming the tuples
     * @param tuples the relation between the variables (list of allowed/forbidden tuples)
     */
    default Constraint table(IntVar[] vars, Tuples tuples) {
        String algo = "GAC3rm";
        if (tuples.isFeasible()) {
            //noinspection OptionalGetWithoutIsPresent
            if (tuples.nbTuples() > 512 &&
                    (IntStream.range(0, vars.length)
                            .map(i -> tuples.max(i) - tuples.min(i))
                            .max().getAsInt()) < 512) {
                algo = "CT+";
            } else if (tuples.allowUniversalValue()) {
                // STR2+ or CT+, depending on dom size
                algo = "STR2+";
            } else {
                algo = "GACSTR+";
            }
        }
        return table(vars, tuples, algo);
    }

    /**
     * Creates a table constraint, with the specified algorithm defined algo
     * <p>
     * - <b>CT+</b>: Compact-Table algorithm (AC),
     * <br/>
     * - <b>GAC2001</b>: Arc Consistency version 2001 for tuples,
     * <br/>
     * - <b>GAC2001+</b>: Arc Consistency version 2001 for allowed tuples,
     * <br/>
     * - <b>GAC3rm</b>: Arc Consistency version AC3 rm for tuples,
     * <br/>
     * - <b>GAC3rm+</b> (default): Arc Consistency version 3rm for allowed tuples,
     * <br/>
     * - <b>GACSTR+</b>: Arc Consistency version STR for allowed tuples,
     * <br/>
     * - <b>STR2+</b>: Arc Consistency version STR2 for allowed tuples,
     * <br/>
     * - <b>FC</b>: Forward Checking.
     * <br/>
     * - <b>MDD+</b>: uses a multi-valued decision diagram for allowed tuples (see mddc constraint),
     *
     * @param vars   variables forming the tuples
     * @param tuples the relation between the variables (list of allowed/forbidden tuples). Should not be modified once passed to the constraint.
     * @param algo   to choose among {"CT+", "GAC3rm", "GAC2001", "GACSTR", "GAC2001+", "GAC3rm+", "FC", "STR2+"}
     */
    default Constraint table(IntVar[] vars, Tuples tuples, String algo) {
        if (!tuples.allowUniversalValue() && vars.length == 2) {
            switch (algo) {
                case "FC":
                    return table(vars[0], vars[1], tuples, algo);
                case "GAC2001":
                    return table(vars[0], vars[1], tuples, "AC2001");
                case "CT+":
                    return table(vars[0], vars[1], tuples, "CT+");
                case "GAC3rm":
                    return table(vars[0], vars[1], tuples, "AC3rm");
                default:
                    return table(vars[0], vars[1], tuples);
            }
        }
        if (algo.contains("+") && !tuples.isFeasible()) {
            throw new SolverException(algo + " table algorithm cannot be used with forbidden tuples.");
        }
        if (tuples.allowUniversalValue() && !(algo.contains("CT+") || algo.contains("STR2+"))) {
            throw new SolverException(algo + " table algorithm cannot be used with short tuples.");
        }
        Propagator<IntVar> p;
        switch (algo) {
            case "CT+": {
                if (tuples.allowUniversalValue()) {
                    p = new PropCompactTableStar(vars, tuples);
                } else {
                    p = new PropCompactTable(vars, tuples);
                }
            }
            break;
            case "MDD+":
                p = new PropLargeMDDC(new MultivaluedDecisionDiagram(vars, tuples), vars);
                break;
            case "FC":
                p = new PropLargeFC(vars, tuples);
                break;
            case "GAC3rm":
                p = new PropLargeGAC3rm(vars, tuples);
                break;
            case "GAC2001":
                p = new PropLargeGAC2001(vars, tuples);
                break;
            case "GACSTR+":
                p = new PropLargeGACSTRPos(vars, tuples);
                break;
            case "GAC2001+":
                p = new PropLargeGAC2001Positive(vars, tuples);
                break;
            case "GAC3rm+":
                p = new PropLargeGAC3rmPositive(vars, tuples);
                break;
            case "STR2+":
                p = new PropTableStr2(vars, tuples);
                break;
            default:
                throw new SolverException("Table algorithm " + algo + " is unkown");
        }
        return new Constraint(ConstraintsName.TABLE, p);
    }

    /**
     * Creates a tree constraint.
     * Partition succs variables into nbTrees (anti) arborescences
     * <p/> succs[i] = j means that j is the successor of i.
     * <p/> and succs[i] = i means that i is a root
     * <p>
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> However, the filtering over nbTrees is quite light here
     *
     * @param succs   successors variables, taking their domain in [0,|succs|-1]
     * @param nbTrees number of arborescences (=number of loops)
     * @return a tree constraint
     */
    default Constraint tree(IntVar[] succs, IntVar nbTrees) {
        return tree(succs, nbTrees, 0);
    }

    /**
     * Creates a tree constraint.
     * Partition succs variables into nbTrees (anti) arborescences
     * <p/> succs[i] = offset+j means that j is the successor of i.
     * <p/> and succs[i] = offset+i means that i is a root
     * <p>
     * <p/> dominator-based filtering: Fages & Lorca (CP'11)
     * <p/> However, the filtering over nbTrees is quite light here
     *
     * @param succs   successors variables, taking their domain in [offset,|succs|-1+offset]
     * @param nbTrees number of arborescences (=number of loops)
     * @param offset  0 by default but 1 if used within MiniZinc
     *                (which counts from 1 to n instead of from 0 to n-1)
     * @return a tree constraint
     */
    default Constraint tree(IntVar[] succs, IntVar nbTrees, int offset) {
        return new Constraint(ConstraintsName.TREE,
                new PropAntiArborescences(succs, offset, false),
                new PropKLoops(succs, offset, nbTrees)
        );
    }

    /**
     * Get the list of values in the domains of vars
     *
     * @param vars an array of integer variables
     * @return the list of values in the domains of vars
     */
    default int[] getDomainUnion(IntVar... vars) {
        int m = vars[0].getLB(), M = vars[0].getUB(), j, k;
        for (int i = 1; i < vars.length; i++) {
            if (m > (k = vars[i].getLB())) {
                m = k;
            }
            if (M < (j = vars[i].getUB())) {
                M = j;
            }
        }
        BitSet values = new BitSet(M - m + 1);
        DisposableRangeIterator rit;
        for (IntVar v : vars) {
            rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                int from = rit.min();
                int to = rit.max();
                // operate on range [from,to] here
                values.set(from - m, to - m + 1);
                rit.next();
            }
            rit.dispose();
        }

        int[] vs = new int[values.cardinality()];
        k = 0;
        for (int i = values.nextSetBit(0); i >= 0; i = values.nextSetBit(i + 1)) {
            vs[k++] = i + m;
        }
        return vs;
    }
}
