/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.constraints;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.ReificationConstraint;
import org.chocosolver.solver.constraints.binary.PropDistanceXYC;
import org.chocosolver.solver.constraints.binary.PropScale;
import org.chocosolver.solver.constraints.nary.element.PropElementV_fast;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.Reflection;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.writer.constraints.binary.BinaryWriterHelper;
import org.chocosolver.writer.constraints.nary.NaryWriterHelper;
import org.chocosolver.writer.constraints.real.RealWriterHelper;
import org.chocosolver.writer.constraints.set.SetWriterHelper;
import org.chocosolver.writer.constraints.unary.UnaryWriterHelper;

import java.io.IOException;

/**
 * Utility class to write constraints <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public abstract class ConstraintWriter {

    /**
     * Write 'cstr' in writer stream
     *
     * @param cstr a constraint
     */
    public void write(Constraint cstr) throws IOException {
        switch (cstr.getName()) {
            case ConstraintsName.REIFICATIONCONSTRAINT:
                ReificationConstraint reif = (ReificationConstraint) cstr;
                beginReification(Reflection.<BoolVar>getObj(reif, "bool").getId());
                write(Reflection.<Constraint>getObj(reif, "trueCons"));
                endReification();
                break;
            case ConstraintsName.OPPOSITE:
                beginOpposite();
                write(cstr.getOpposite());
                endOpposite();
                break;
            case ConstraintsName.ARITHM: {
                Propagator prop0 = cstr.getPropagator(0);
                // There is only one propagator in that case
                switch (prop0.getClass().getSimpleName()) {
                    case "PropEqualXC":
                    case "PropGreaterOrEqualXC":
                    case "PropLessOrEqualXC":
                    case "PropNotEqualXC":
                        UnaryWriterHelper.writeArithmetic(this, prop0);
                        break;
                    case "PropEqualX_Y":
                    case "PropEqualX_YC":
                    case "PropEqualXY_C":
                    case "PropGreaterOrEqualX_Y":
                    case "PropGreaterOrEqualX_YC":
                    case "PropGreaterOrEqualXY_C":
                    case "PropLessOrEqualXY_C":
                    case "PropNotEqualX_Y":
                    case "PropNotEqualX_YC":
                    case "PropNotEqualXY_C":
                        BinaryWriterHelper.writeArithmetic(this, prop0);
                        break;
                }
                break;
            }
            case ConstraintsName.ABSOLUTE: {
                Propagator prop0 = cstr.getPropagator(0);
                writeAbsolute(
                        prop0.getVar(0).getId(),
                        prop0.getVar(1).getId());
            }
            break;
            case ConstraintsName.ALLDIFFERENT: {
                NaryWriterHelper.writeAlldifferent(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.AMONG: {
                NaryWriterHelper.writeAmong(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.ATLEASTNVALUES: {
                NaryWriterHelper.writeAtleastnvalues(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.ATMOSTNVALUES: {
                NaryWriterHelper.writeAtmostnvalues(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.BASIC_REI:{
                BinaryWriterHelper.writeBasicReification(this, cstr.getPropagator(0));
            }break;
            case ConstraintsName.BINPACKING: {
                NaryWriterHelper.writeBinpacking(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.BOOLCHANNELING: {
                NaryWriterHelper.writeBoolchanneling(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.BITSINTCHANNELING: {
                NaryWriterHelper.writeBitschanneling(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.CLAUSESINTCHANNELING:
                throw new UnsupportedOperationException("CLAUSESINTCHANNELING is not supported");
            case ConstraintsName.CIRCUIT: {
                NaryWriterHelper.writeCircuit(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.COSTREGULAR:
                throw new UnsupportedOperationException("COSTREGULAR is not supported");
            case ConstraintsName.COUNT: {
                NaryWriterHelper.writeCount(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.CUMULATIVE: {
                NaryWriterHelper.writeCumulative(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.DIFFN: {
                NaryWriterHelper.writeDiffn(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.DIFFNWITHCUMULATIVE:
                throw new UnsupportedOperationException("DIFFNWITHCUMULATIVE is not supported, \nset 'addCumulativeReasoning' to false in model.diffn(...) to export the model.");
            case ConstraintsName.DISTANCE: {
                Propagator prop0 = cstr.getPropagator(0);
                if (prop0 instanceof PropDistanceXYC) {
                    int cste = Reflection.getInt(prop0, "cste");
                    Operator operator = Reflection.getObj(prop0, "operator");
                    writeDistance2(
                            prop0.getVar(0).getId(),
                            prop0.getVar(1).getId(), operator, cste
                    );
                } else {
                    Operator operator = Reflection.getObj(prop0, "operator");
                    writeDistance3(
                            prop0.getVar(0).getId(),
                            prop0.getVar(1).getId(),
                            operator,
                            prop0.getVar(2).getId());
                }
            }
            break;
            case ConstraintsName.DIVISION: {
                Propagator prop0 = cstr.getPropagator(0);
                writeDivision(
                        prop0.getVar(0).getId(),
                        prop0.getVar(1).getId(),
                        prop0.getVar(2).getId());
            }
            break;
            case ConstraintsName.ELEMENT: {
                Propagator prop0 = cstr.getPropagator(0);
                if (prop0 instanceof PropElementV_fast) {
                    NaryWriterHelper.writeElement(this, prop0);
                } else {
                    BinaryWriterHelper.writeElement(this, prop0);
                }
            }
            break;
            case ConstraintsName.GCC: {
                NaryWriterHelper.writeGcc(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.INVERSECHANNELING: {
                NaryWriterHelper.writeInversechanneling(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.KNAPSACK: {
                NaryWriterHelper.writeKnapsack(this, cstr.getPropagator(cstr.getPropagators().length - 1));
            }
            break;
            case ConstraintsName.LEXCHAIN: {
                NaryWriterHelper.writeLexchain(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.LEX: {
                NaryWriterHelper.writeLex(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.MAX: {
                NaryWriterHelper.writeMax(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.MDDC: {
                throw new UnsupportedOperationException("MDDC is not supported");
            }
            case ConstraintsName.MEMBER: {
                Propagator prop0 = cstr.getPropagator(0);
                UnaryWriterHelper.writeMember(this, prop0);
            }
            break;
            case ConstraintsName.MIN: {
                NaryWriterHelper.writeMin(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.MULTICOSTREGULAR: {
                throw new UnsupportedOperationException("MULTICOSTREGULAR is not supported");
            }
            case ConstraintsName.NOTMEMBER: {
                Propagator prop0 = cstr.getPropagator(0);
                UnaryWriterHelper.writeMember(this, prop0);
            }
            break;
            case ConstraintsName.NVALUES: {
                NaryWriterHelper.writeNvalues(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.PATH: {
                throw new UnsupportedOperationException("PATH is not supported");
            }
            case ConstraintsName.REGULAR: {
                throw new UnsupportedOperationException("REGULAR is not supported");
            }
            case ConstraintsName.SQUARE: {
                Propagator prop0 = cstr.getPropagator(0);
                writeSquare(
                        prop0.getVar(0).getId(),
                        prop0.getVar(1).getId());
            }
            break;
            case ConstraintsName.SUBCIRCUIT: {
                NaryWriterHelper.writeSubcircuit(this, cstr.getPropagator(cstr.getPropagators().length - 2));
            }
            break;
            case ConstraintsName.SUM: {
                NaryWriterHelper.writeSum(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.TABLE: {
                NaryWriterHelper.writeTable(this, cstr.getPropagator(0));
            }
            break;
            case ConstraintsName.TREE: {
                NaryWriterHelper.writeTree(this, cstr.getPropagator(1));
            }
            break;
            case ConstraintsName.TIMES: {
                Propagator prop0 = cstr.getPropagator(0);
                if (prop0 instanceof PropScale) {
                    PropScale prop1 = (PropScale) prop0;
                    int sc = Reflection.getInt(prop1, "Y");
                    writeScale(
                            prop0.getVar(0).getId(),
                            sc, prop0.getVar(1).getId()
                    );
                } else {
                    writeTimes(
                            prop0.getVar(0).getId(),
                            prop0.getVar(1).getId(),
                            prop0.getVar(2).getId());
                }
            }
            break;
            case ConstraintsName.SETALLDIFFERENT: {
                SetWriterHelper.writeAlldifferent(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETALLDISJOINT: {
                SetWriterHelper.writeAlldisjoint(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETALLEQUAL: {
                SetWriterHelper.writeAllequal(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETBOOLCHANNELING: {
                SetWriterHelper.writeBoolchanneling(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETCARD: {
                SetWriterHelper.writeSetcard(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETELEMENT: {
                SetWriterHelper.writeElement(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETINTCHANNELING: {
                SetWriterHelper.writeIntchanneling(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETINTERSECTION: {
                SetWriterHelper.writeIntersection(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETINTVALUESUNION: {
                SetWriterHelper.writeIntvaluesunion(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETINVERSE: {
                SetWriterHelper.writeInverse(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETMAX: {
                SetWriterHelper.writeMax(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETMEMBER: {
                SetWriterHelper.writeMember(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETMIN: {
                SetWriterHelper.writeMin(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETNBEMPTY: {
                SetWriterHelper.writeNbempty(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETNOTEMPTY: {
                SetWriterHelper.writeNotempty(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETNOTMEMBER: {
                SetWriterHelper.writeNotmember(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETOFFSET: {
                SetWriterHelper.writeOffset(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETPARTITION: {
                SetWriterHelper.writePartition(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETSUBSETEQ: {
                SetWriterHelper.writeSubseteq(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETSUM: {
                SetWriterHelper.writeSum(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETSYMMETRIC: {
                SetWriterHelper.writeSymmetric(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.SETUNION: {
                SetWriterHelper.writeUnion(this, cstr.getPropagators());
            }
            break;
            // REAL CONSTRAINT
            case ConstraintsName.REALCONSTRAINT: {
                RealWriterHelper.writeRealConstraint(this, cstr.getPropagators());
            }
            break;
            case ConstraintsName.INTEQREAL: {
                RealWriterHelper.writeInteqreal(this, cstr.getPropagators());
            }
            break;
        }

    }

    /**
     * Begins encoding a reification constraint. A call to this method is followed by a call to
     * {@link #write(Constraint)} and a call to {@link #endReification()}.
     *
     * @param id variable's ID
     */
    public abstract void beginReification(int id) throws IOException;

    /**
     * Ends encoding the current reification constraint.
     */
    public abstract void endReification() throws IOException;

    /**
     * Begins encoding a generic opposite constraint. A call to this method is followed by {@link
     * #write(Constraint)} and {@link #endOpposite()}.
     */
    public abstract void beginOpposite() throws IOException;

    /**
     * Ends encoding the current oppsite constraint.
     */
    public abstract void endOpposite() throws IOException;

    /**
     * Encodes an arithmetic constraint like "X op c"
     *
     * @param id   variable's ID (X)
     * @param op   an operator
     * @param cste a constante (c)
     */
    public abstract void writeArithm1(int id, Operator op, int cste) throws IOException;

    /**
     * Encodes an arithmetic constraint like "X op Y"
     *
     * @param id1 variable's ID (X)
     * @param op  an operator
     * @param id2 variable's ID (Y)
     */
    public abstract void writeArithm2(int id1, Operator op, int id2) throws IOException;

    /**
     * Encodes a member constraint like "X in [a,b]"
     *
     * @param id variable's ID (X)
     * @param a  minimum value of the range
     * @param b  maximum value of the range
     */
    public abstract void writeMember(int id, int a, int b) throws IOException;

    /**
     * Encodes a not member constraint like "X not in [a,b]"
     *
     * @param id variable's ID (X)
     * @param a  minimum value of the range
     * @param b  maximum value of the range
     */
    public abstract void writeNotMember(int id, int a, int b) throws IOException;

    /**
     * Encodes a member constraint like "X in {values,...}"
     *
     * @param id     variable's ID (X)
     * @param values a set of int
     */
    public abstract void writeMember(int id, int[] values) throws IOException;

    /**
     * Encodes a not member constraint like "X not in {values,...}"
     *
     * @param id     variable's ID (X)
     * @param values a set of int
     */
    public abstract void writeNotMember(int id, int[] values) throws IOException;

    /**
     * Encodes an arithmetic constraint like "X op Y + c"
     *
     * @param id1  variable's ID (X)
     * @param op   an operator
     * @param id2  variable's ID (Y)
     * @param cste a constante (c)
     */
    public abstract void writeArithm3(int id1, Operator op, int id2, int cste) throws IOException;

    /**
     * Encodes an arithmetic constraint like "X + Y op c"
     *
     * @param id1  variable's ID (X)
     * @param id2  variable's ID (Y)
     * @param op   an operator
     * @param cste a constante (c)
     */
    public abstract void writeArithm3(int id1, int id2, Operator op, int cste) throws IOException;

    /**
     * Encodes an absolute constraint like "X = |Y|"
     *
     * @param id1 variable's ID (X)
     * @param id2 variable's ID (Y)
     */
    public abstract void writeAbsolute(int id1, int id2) throws IOException;

    /**
     * Encodes an absolute constraint like "| X - Y | o c"
     *
     * @param id1    variable's ID (X)
     * @param id2    variable's ID (Y)
     * @param op     an operator (o)
     * @param factor scale factor (c)
     */
    public abstract void writeDistance2(int id1, int id2, Operator op, int factor) throws IOException;

    /**
     * Encodes an absolute constraint like "| X - Y | o Z"
     *
     * @param id1 variable's ID (X)
     * @param id2 variable's ID (Y)
     * @param op  an operator (o)
     * @param id3 variable's ID (Z)
     */
    public abstract void writeDistance3(int id1, int id2, Operator op, int id3) throws IOException;

    /**
     * Encodes a division constraint like "X / Y =  Z"
     *
     * @param id1 variable's ID (X)
     * @param id2 variable's ID (Y)
     * @param id3 variable's ID (Z)
     */
    public abstract void writeDivision(int id1, int id2, int id3) throws IOException;

    /**
     * Encodes an element constraint like "X = ARR[Y - o]"
     *
     * @param id1    variable's ID (X)
     * @param values array of int (ARR)
     * @param id2    variable's ID (Y)
     * @param offset offset (o)
     */
    public abstract void writeElement(int id1, int[] values, int id2, int offset) throws IOException;

    /**
     * Encodes an element constraint like "X = ARR[Y - o]"
     *
     * @param id1    variable's ID (X)
     * @param vIds   array of variables' ID (ARR)
     * @param id2    variable's ID (Y)
     * @param offset offset (o)
     */
    public abstract void writeElementVar(int id1, int[] vIds, int id2, int offset) throws IOException;

    /**
     * Encodes a maximum constraint like "X = max(Y1, Y2, ...)"
     *
     * @param id1 variable's ID (X)
     * @param ids variables' ID (Yi)
     */
    public abstract void writeMax(int id1, int... ids) throws IOException;

    /**
     * Encodes a minimum constraint like "X = max(Y1, Y2, ..)"
     *
     * @param id1 variable's ID (X)
     * @param ids variables' ID (Yi)
     */
    public abstract void writeMin(int id1, int... ids) throws IOException;

    /**
     * Encodes a square constraint like "X = Y^2"
     *
     * @param id1 variable's ID (X)
     * @param id2 variable's ID (Y)
     */
    public abstract void writeSquare(int id1, int id2) throws IOException;

    /**
     * Encodes a scale constraint like "X * f = Y"
     *
     * @param id1    variable's ID (X)
     * @param factor scale factor (f)
     * @param id2    variable's ID (Y)
     */
    public abstract void writeScale(int id1, int factor, int id2) throws IOException;

    /**
     * Encodes a times constraint like "X * Y =  Z"
     *
     * @param id1 variable's ID (X)
     * @param id2 variable's ID (Y)
     * @param id3 variable's ID (Z)
     */
    public abstract void writeTimes(int id1, int id2, int id3) throws IOException;

    /**
     * Encodes an alldifferent constraint
     *
     * @param consistency consistency algorithm
     * @param ids         variables' ID
     */
    public abstract void writeAlldifferent(String consistency, int... ids) throws IOException;

    /**
     * Encodes an among constraint
     *
     * @param id     a variable's ID
     * @param values set of values
     * @param ids    variables' ID
     */
    public abstract void writeAmong(int id, int[] values, int[] ids) throws IOException;

    /**
     * Encodes an at_least_n_values constraint
     *
     * @param ids variables' ID
     * @param id  a variable's ID
     * @param ac  boolean for consistency level (AC or not)
     */
    public abstract void writeAtleastnvalues(int[] ids, int id, boolean ac) throws IOException;

    /**
     * Encodes an at_most_n_values constraint
     *
     * @param ids    variables' ID
     * @param id     a variable's ID
     * @param strong boolean for strength of the algorithm level (strong or not)
     */
    public abstract void writeAtmostnvalues(int[] ids, int id, boolean strong) throws IOException;

    /**
     * Encodes a bin_packing constraint
     *
     * @param itemIds     items' ID (variables)
     * @param itemSizes   items' size
     * @param binLoadsIds bin loads' ID (variables)
     * @param offset      offset
     */
    public abstract void writeBinpacking(int[] itemIds, int[] itemSizes, int[] binLoadsIds, int offset) throws IOException;

    /**
     * Encodes a boolean_channeling constraint
     *
     * @param bIds   boolean variables' ID
     * @param vId    integer variable ID
     * @param offset offset
     */
    public abstract void writeBoolchanneling(int[] bIds, int vId, int offset) throws IOException;

    /**
     * Encodes a bits_channeling constraint
     *
     * @param octet integer variable ID
     * @param bits  boolean variables' ID
     */
    public abstract void writeBitschanneling(int octet, int[] bits) throws IOException;

    /**
     * Encodes a circuit constraint
     *
     * @param ids    variables' ID
     * @param offset offset
     * @param conf   circuit configuration
     */
    public abstract void writeCircuit(int[] ids, int offset, String conf) throws IOException;

    /**
     * Encodes a count constraint
     *
     * @param ids   variables' ID
     * @param value a value
     * @param occId counter variable ID
     */
    public abstract void writeCount(int[] ids, int value, int occId) throws IOException;

    /**
     * Encodes a count constraint
     *
     * @param ids     variables' ID
     * @param valueID value variable ID
     * @param occId   counter variable ID
     */
    public abstract void writeCountVar(int[] ids, int valueID, int occId) throws IOException;

    /**
     * Encodes a cumulative constraint
     *
     * @param sIds    starts' ID
     * @param dIds    durations' ID
     * @param eIds    ends' ID
     * @param hIds    heights ID
     * @param C       capacity ID
     * @param inc     incrementality
     * @param filters list of filters
     */
    public abstract void writeCumulative(int[] sIds, int[] dIds, int[] eIds, int[] hIds, int C,
                                         boolean inc, String[] filters) throws IOException;

    /**
     * Encodes a diffn constraint
     *
     * @param xIdx xs' ID
     * @param yIds ys' ID
     * @param wIds widths' ID
     * @param hIds heights' ID
     */
    public abstract void writeDiffn(int[] xIdx, int[] yIds, int[] wIds, int[] hIds) throws IOException;

    /**
     * Encodes a global_cardinality constraint
     *
     * @param vIds   variables' ID
     * @param values set of int
     * @param oIds   occurrences' ID
     */
    public abstract void writeGcc(int[] vIds, int[] values, int[] oIds) throws IOException;

    /**
     * Encodes an inverse_channeling constraint
     *
     * @param xids variables' ID (X)
     * @param yids variables' ID (Y)
     * @param ox   offset for X
     * @param oy   offset for Y
     */
    public abstract void writeInversechanneling(int[] xids, int[] yids, int ox, int oy) throws IOException;


    /**
     * Encodes a knapsack constraint
     *
     * @param oIds objects ID (variables)
     * @param cId  capacity variable ID
     * @param pId  power variable ID
     * @param ws   weights
     * @param es   energies
     */
    public abstract void writeKnapsack(int[] oIds, int cId, int pId, int[] ws, int[] es) throws IOException;

    /**
     * Encodes a lex_chain constraint
     *
     * @param vIds   variables' ID
     * @param n      row size
     * @param strict eq if true, leq otherwise
     */
    public abstract void writeLexchain(int[] vIds, int n, boolean strict) throws IOException;

    /**
     * Encodes a lex constraint
     *
     * @param xIds   variables' ID (X)
     * @param yIds   variables' ID (Y)
     * @param strict eq if true, leq otherwise
     */
    public abstract void writeLex(int[] xIds, int[] yIds, boolean strict) throws IOException;

    /**
     * Encodes a nvalues constraint
     *
     * @param ids array of variables' ID
     * @param id  variable's ID
     */
    public abstract void writeNvalues(int[] ids, int id) throws IOException;

    /**
     * Encodes a sum constraint
     *
     * @param vids variables' ID
     * @param p    position of the last positive coefficient
     * @param o    operator
     * @param b    bound to respect
     */
    public abstract void writeSum(int[] vids, int p, String o, int b) throws IOException;

    /**
     * Encodes a sum constraint
     *
     * @param vids variables' ID
     * @param cs   coefficients
     * @param pos  position of the last positive coefficient
     * @param o    operator
     * @param b    bound to respect
     */
    public abstract void writeScalar(int[] vids, int[] cs, int pos, String o, int b) throws IOException;

    /**
     * Encodes a subcircuit constraint
     *
     * @param ids    variables' ID
     * @param id     variable's ID
     * @param offset offset
     */
    public abstract void writeSubcircuit(int[] ids, int id, int offset) throws IOException;

    /**
     * Encodes a tables constraint
     *
     * @param ids      variables' ID
     * @param tuples   list of <b>feasible</b> tuples
     * @param algo     table algorithm
     * @param feasible feasibility of tuples
     */
    public abstract void writeTable(int[] ids, int[][] tuples, String algo, boolean feasible) throws IOException;

    /**
     * Encodes a tree constraint
     *
     * @param ids    variables' ID
     * @param id     variable's ID
     * @param offset offset
     */
    public abstract void writeTree(int[] ids, int id, int offset) throws IOException;

    //*******
    //* SET *
    //*******

    /**
     * Encodes an alldifferent constraints over sets
     *
     * @param sIds variables' ID
     */
    public abstract void writeSetAlldifferent(int[] sIds) throws IOException;

    /**
     * Encodes an alldisjoint constraints over sets
     *
     * @param sIds variables' ID
     */
    public abstract void writeSetAlldisjoint(int[] sIds) throws IOException;

    /**
     * Encodes an allequal constraints over sets
     *
     * @param sIds variables' ID
     */
    public abstract void writeSetAllequal(int[] sIds) throws IOException;

    /**
     * Encodes a boolean channeling constraint over sets
     *
     * @param sIds   variables' ID
     * @param id     variable's ID
     * @param offSet offset
     */
    public abstract void writeSetBoolchanneling(int[] sIds, int id, int offSet) throws IOException;

    /**
     * Encodes a cardinality constraint
     *
     * @param sid variable's ID
     * @param iid variable's ID
     */
    public abstract void writeSetCard(int sid, int iid) throws IOException;

    /**
     * Encodes an element constraint over sets
     *
     * @param iid    variables' ID
     * @param sids   variables' ID
     * @param sid    variables' ID
     * @param offSet offset
     */
    public abstract void writeSetElement(int iid, int[] sids, int sid, int offSet) throws IOException;

    /**
     * Encodes a set to int channeling constraint
     *
     * @param sids    variables' ID
     * @param iids    variables' ID
     * @param offSet1 offset
     * @param offSet2 offset
     */
    public abstract void writeSetIntchanneling(int[] sids, int[] iids, int offSet1, int offSet2) throws IOException;

    /**
     * Encodes an intersection constraint
     *
     * @param sids variables' ID
     * @param sid  variable's ID
     * @param b    bound consistency
     */
    public abstract void writeSetIntersection(int[] sids, int sid, boolean b) throws IOException;

    /**
     * Encodes an union constraint
     *
     * @param iids variables' ID
     * @param sid  variable's ID
     */
    public abstract void writeSetIntvaluesunion(int[] iids, int sid) throws IOException;

    /**
     * Encodes an inverse constraint over sets
     *
     * @param sids1   variables' ID
     * @param sids2   variables' ID
     * @param offSet1 offset
     * @param offSet2 offset
     */
    public abstract void writeSetInverse(int[] sids1, int[] sids2, int offSet1, int offSet2) throws IOException;

    /**
     * Encodes a max constraint over sets, 'weights' can be null
     *  @param sid      variable's ID
     * @param iid      variable's ID
     * @param weights  coefficients
     * @param offSet   offset
     * @param notEmpty set can be empty or not
     */
    public abstract void writeSetMax(int sid, int iid, int[] weights, int offSet, boolean notEmpty) throws IOException;

    /**
     * Encodes a member constraint
     *
     * @param sid variable's ID
     * @param cst a constant
     */
    public abstract void writeSetMember(int sid, int cst) throws IOException;

    /**
     * Encodes a member constraint
     *
     * @param sid variable's ID
     * @param iid variable's ID
     */
    public abstract void writeSetMemberV(int sid, int iid) throws IOException;

    /**
     * Encodes a min constraint over sets, 'weights' can be null
     *  @param sid      variable's ID
     * @param iid      variable's ID
     * @param weights  coefficients
     * @param offSet   offset
     * @param notEmpty set can be empty or not
     */
    public abstract void writeSetMin(int sid, int iid, int[] weights, int offSet, boolean notEmpty) throws IOException;

    /**
     * Encodes a nb_empty constraint
     *
     * @param sids    variables' ID
     * @param id variable's ID
     */
    public abstract void writeSetNbempty(int[] sids, int id) throws IOException;

    /**
     * Encodes a not_empty constraint
     *
     * @param sid variable's ID
     */
    public abstract void writeSetNotempty(int sid) throws IOException;

    /**
     * Encodes a not member constraint
     *
     * @param sid variable's ID
     * @param cst a constant
     */
    public abstract void writeSetNotmember(int sid, int cst) throws IOException;

    /**
     * Encodes a member constraint
     *
     * @param sid variable's ID
     * @param iid variable's ID
     */
    public abstract void writeSetNotmemberV(int sid, int iid) throws IOException;

    /**
     * Encodes an offset constraint
     *
     * @param sid1   variable's ID
     * @param sid2   variable's ID
     * @param offSet offset
     */
    public abstract void writeSetOffset(int sid1, int sid2, int offSet) throws IOException;

    /**
     * Encodes a partition constraint
     *
     * @param sids variables' ID
     * @param sid  variable's ID
     */
    public abstract void writeSetPartition(int[] sids, int sid) throws IOException;

    /**
     * Encodes a sub_set_eq constraint
     *
     * @param sids variables' ID
     */
    public abstract void writeSetSubseteq(int[] sids) throws IOException;

    /**
     * Encodes a sum over sets constraint
     *
     * @param sid     variable's ID
     * @param iid     variable's ID
     * @param weights coefficient
     * @param offSet  offset
     */
    public abstract void writeSetSum(int sid, int iid, int[] weights, int offSet) throws IOException;

    /**
     * Encodes a symmetric constraint
     *
     * @param sids   variables' ID
     * @param offSet offset
     */
    public abstract void writeSetSymmetric(int[] sids, int offSet) throws IOException;

    /**
     * Encodes an intersection constraint
     *
     * @param sids variables' ID
     * @param sid  variable's ID
     */
    public abstract void writeSetUnion(int[] sids, int sid) throws IOException;

    /**
     * Encodes a int_eq_real constraint
     * @param iids variables' ID
     * @param rids variables' ID
     * @param epsilon precision
     */
    public abstract void writeInteqreal(int[] iids, int[] rids, double epsilon) throws IOException;

    /**
     * Encodes a constraint over reals
     * @param rids variables' ID
     * @param function function to satisfy
     */
    public abstract void writeRealConstraint(int[] rids, String function) throws IOException;

    /**
     * Encodes a constraint over reals which is reified
     * @param rids variables' ID
     * @param function a function to satisfy
     * @param bid variable's ID
     */
    public abstract void writeRealConstraint(int[] rids, String function, int bid) throws IOException;

    /**
     * Encodes a basic reification, like "(x o c) <=> b"
     * @param vid variable's ID
     * @param op operator
     * @param cste int constant
     * @param bid variable's ID
     */
    public abstract void writeBasicreification1(int vid, String op, int cste, int bid) throws IOException;

    /**
     * Encodes a basic reification, like "(x o vs) <=> b"
     * @param vid variable's ID
     * @param op operator
     * @param values set of values
     * @param bid variable's ID
     */
    public abstract void writeBasicreification1(int vid, String op, IntIterableRangeSet values, int bid) throws IOException;

    /**
     * Encodes a basic reification, like "(x o y) <=> b"
     * @param vid1 variable's ID
     * @param op operator
     * @param vid2 variable's ID
     * @param bid variable's ID
     */
    public abstract void writeBasicreification2(int vid1, String op, int vid2, int bid) throws IOException;

    /**
     * Encodes a basic reification, like "(x o y + c) <=> b"
     * @param vid1 variable's ID
     * @param op operator
     * @param vid2 variable's ID
     * @param cste int constant
     * @param bid variable's ID
     */
    public abstract void writeBasicreification2(int vid1, String op, int vid2, int cste, int bid) throws IOException;
}
