/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.variables;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.impl.BitsetArrayIntVarImpl;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;
import org.chocosolver.solver.variables.impl.BoolVarImpl;
import org.chocosolver.solver.variables.impl.FixedBoolVarImpl;
import org.chocosolver.solver.variables.impl.FixedIntVarImpl;
import org.chocosolver.solver.variables.impl.FixedRealVarImpl;
import org.chocosolver.solver.variables.impl.IntervalIntVarImpl;
import org.chocosolver.solver.variables.impl.RealVarImpl;
import org.chocosolver.solver.variables.impl.SetVarImpl;
import org.chocosolver.solver.variables.view.BoolNotView;
import org.chocosolver.solver.variables.view.MinusView;
import org.chocosolver.solver.variables.view.OffsetView;
import org.chocosolver.solver.variables.view.RealView;
import org.chocosolver.solver.variables.view.ScaleView;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

import java.io.IOException;

/**
 * Utility class to write variables
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public abstract class VariableWriter {

    /**
     * Write 'variable' in a writer stream
     *
     * @param var a variable
     */
    public void write(Variable var) throws IOException {
        if (var instanceof IntervalIntVarImpl) {
            IntVar ivar = (IntVar) var;
            writeIntVar(ivar.getName(), ivar.getId(), ivar.getLB(), ivar.getUB());
        } else if (var instanceof BitsetIntVarImpl || var instanceof BitsetArrayIntVarImpl) {
            IntVar ivar = (IntVar) var;
            writeIntVar(ivar.getName(), ivar.getId(), IntIterableSetUtils.extract(ivar));
        } else if (var instanceof BoolVarImpl) {
            IntVar ivar = (IntVar) var;
            writeBoolVar(ivar.getName(), ivar.getId());
        } else if (var instanceof FixedBoolVarImpl || var instanceof FixedIntVarImpl) {
            IntVar ivar = (IntVar) var;
            writeIntVar(ivar.getName(), ivar.getId(), ivar.getLB());
        } else if (var instanceof BoolNotView) {
            BoolNotView not = (BoolNotView) var;
            writeNotView(not.getName(), not.getId(),
                    not.getVariable().getName(), not.getVariable().getId());
        } else if (var instanceof MinusView) {
            MinusView min = (MinusView) var;
            writeMinusView(min.getName(), min.getId(),
                    min.getVariable().getName(), min.getVariable().getId());
        } else if (var instanceof ScaleView) {
            ScaleView sca = (ScaleView) var;
            writeScaleView(sca.getName(), sca.getId(), sca.cste,
                    sca.getVariable().getName(), sca.getVariable().getId());
        } else if (var instanceof OffsetView) {
            OffsetView off = (OffsetView) var;
            writeOffsetView(off.getName(), off.getId(), off.cste,
                    off.getVariable().getName(), off.getVariable().getId());
        } else if (var instanceof SetVarImpl) {
            SetVarImpl svar = (SetVarImpl) var;
            writeSetVar(svar.getName(), svar.getId(), svar.getLB().toArray(), svar.getUB().toArray());
        } else if (var instanceof RealVarImpl) {
            RealVarImpl rvar = (RealVarImpl) var;
            writeRealVar(rvar.getName(), rvar.getId(), rvar.getLB(), rvar.getUB(), rvar.getPrecision());
        } else if (var instanceof FixedRealVarImpl) {
            FixedRealVarImpl rvar = (FixedRealVarImpl) var;
            writeRealVar(rvar.getName(), rvar.getId(), rvar.getLB());
        } else if (var instanceof RealView) {
            RealView rvar = (RealView) var;
            writeRealView(rvar.getName(), rvar.getId(), rvar.getPrecision(),
                    rvar.getVariable().getName(), rvar.getVariable().getId());
        } else {
            throw new UnsupportedOperationException("Cannot write " + var.getClass());
        }
    }

    /**
     * Encodes a BoolVar
     *
     * @param name variable's name
     * @param id   variable's ID
     */
    protected abstract void writeBoolVar(String name, int id) throws IOException;

    /**
     * Encodes an IntVar
     *
     * @param name variable's name
     * @param id   variable's ID
     * @param lb   lower bound
     * @param ub   upper bound
     */
    protected abstract void writeIntVar(String name, int id, int lb, int ub) throws IOException;

    /**
     * Encodes an IntVar
     *
     * @param name   variable's name
     * @param id     variable's ID
     * @param values array of values
     */
    protected abstract void writeIntVar(String name, int id, IntIterableRangeSet values) throws IOException;

    /**
     * Encodes an IntVar
     *
     * @param name  variable's name
     * @param id    variable's ID
     * @param value singleton value
     */
    protected abstract void writeIntVar(String name, int id, int value) throws IOException;

    /**
     * Encodes a 'not' view
     *
     * @param tgtName view name
     * @param tgtId   view ID
     * @param srcName observed variable's name
     * @param srcId   observed variable's ID
     */
    protected abstract void writeNotView(String tgtName, int tgtId, String srcName, int srcId) throws IOException;

    /**
     * Encodes a 'minus' view
     *
     * @param tgtName view name
     * @param tgtId   view ID
     * @param srcName observed variable's name
     * @param srcId   observed variable's ID
     */
    protected abstract void writeMinusView(String tgtName, int tgtId, String srcName, int srcId) throws IOException;

    /**
     * Encodes a 'scale' view
     *
     * @param tgtName view name
     * @param tgtId   view ID
     * @param cste    scale parameter
     * @param srcName observed variable's name
     * @param srcId   observed variable's ID
     */
    protected abstract void writeScaleView(String tgtName, int tgtId, int cste, String srcName, int srcId) throws IOException;

    /**
     * Encodes a 'offset' view
     *
     * @param tgtName view name
     * @param tgtId   view ID
     * @param cste    offset parameter
     * @param srcName observed variable's name
     * @param srcId   observed variable's ID
     */
    protected abstract void writeOffsetView(String tgtName, int tgtId, int cste, String srcName, int srcId) throws IOException;

    /**
     * Encodes a SetVar
     *
     * @param name variable's name
     * @param id   variable's ID
     * @param lbs  lower bound values
     * @param ubs  upper bound values
     */
    protected abstract void writeSetVar(String name, int id, int[] lbs, int[] ubs) throws IOException;

    /**
     * Encode a RealVar
     *
     * @param name variable's name
     * @param id   variable's ID
     * @param lb   lower bound
     * @param ub   upper bound
     * @param pr   precision
     */
    protected abstract void writeRealVar(String name, int id, double lb, double ub, double pr) throws IOException;

    /**
     * Encode a RealVar
     *
     * @param name  variable's name
     * @param id    variable's ID
     * @param value singleton value
     */
    protected abstract void writeRealVar(String name, int id, double value) throws IOException;

    /**
     * Encodes a 'real' view
     *
     * @param tgtName   view name
     * @param tgtId     view ID
     * @param precision precision
     * @param srcName   observed variable's name
     * @param srcId     observed variable's ID
     */
    protected abstract void writeRealView(String tgtName, int tgtId, double precision, String srcName, int srcId) throws IOException;
}
