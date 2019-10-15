/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.constraints.set;

import org.chocosolver.solver.Identity;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.set.PropIntCstMemberSet;
import org.chocosolver.solver.constraints.set.PropIntCstNotMemberSet;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.writer.util.Reflection;
import org.chocosolver.writer.constraints.ConstraintWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Utility class to encode set constraints
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 26/09/2017.
 */
public class SetWriterHelper {

    private SetWriterHelper() {
    }

    public static void writeAlldifferent(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetAlldifferent(
                IntStream.range(0, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray()
        );
    }

    public static void writeAlldisjoint(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetAlldisjoint(
                IntStream.range(0, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray()
        );
    }

    public static void writeAllequal(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetAllequal(
                IntStream.range(0, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray()
        );
    }

    public static void writeBoolchanneling(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = prop0.getNbVars();
        writer.writeSetBoolchanneling(
                IntStream.range(0, n - 1).map(i -> prop0.getVar(i).getId()).toArray(),
                prop0.getVar(n - 1).getId(),
                Reflection.getInt(prop0, "offSet")
        );
    }

    public static void writeSetcard(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetCard(
                prop0.getVar(0).getId(),
                prop0.getVar(1).getId()
        );
    }

    public static void writeElement(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = prop0.getNbVars();
        writer.writeSetElement(
                prop0.getVar(n - 1).getId(),
                IntStream.range(0, n - 2).map(i -> prop0.getVar(i).getId()).toArray(),
                prop0.getVar(n - 2).getId(),
                Reflection.getInt(prop0, "offSet")
        );
    }

    public static void writeIntchanneling(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = Reflection.getInt(prop0, "nSets");
        writer.writeSetIntchanneling(
                IntStream.range(0, n).map(i -> prop0.getVar(i).getId()).toArray(),
                IntStream.range(n, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray(),
                Reflection.getInt(prop0, "offSet1"),
                Reflection.getInt(prop0, "offSet2")
        );
    }

    public static void writeIntersection(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = prop0.getNbVars();
        writer.writeSetIntersection(
                IntStream.range(0, n - 1).map(i -> prop0.getVar(i).getId()).toArray(),
                prop0.getVar(n - 1).getId(),
                propagators.length > 1
        );
    }

    public static void writeIntvaluesunion(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetIntvaluesunion(
                Arrays.stream(Reflection.<IntVar[]>getObj(prop0, "X")).mapToInt(Identity::getId).toArray(),
                Reflection.<SetVar>getObj(prop0, "values").getId()
        );
    }

    public static void writeInverse(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = Reflection.getInt(prop0, "n");
        writer.writeSetInverse(
                IntStream.range(0, n).map(i -> prop0.getVar(i).getId()).toArray(),
                IntStream.range(n, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray(),
                Reflection.getInt(prop0, "offSet1"),
                Reflection.getInt(prop0, "offSet2")
        );
    }

    public static void writeMax(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator propN = propagators[propagators.length - 1];
        writer.writeSetMax(
                propN.getVar(0).getId(),
                propN.getVar(1).getId(),
                Reflection.getObj(propN, "weights"),
                Reflection.getInt(propN, "offSet"),
                Reflection.getObj(propN, "notEmpty")
        );
    }

    public static void writeMember(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        if (prop0 instanceof PropIntCstMemberSet) {
            writer.writeSetMember(
                    prop0.getVar(0).getId(),
                    Reflection.getInt(prop0, "cst")
            );
        } else {
            writer.writeSetMemberV(
                    prop0.getVar(0).getId(),
                    prop0.getVar(1).getId()
            );
        }
    }

    public static void writeMin(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator propN = propagators[propagators.length - 1];
        writer.writeSetMin(
                propN.getVar(0).getId(),
                propN.getVar(1).getId(),
                Reflection.getObj(propN, "weights"),
                Reflection.getInt(propN, "offSet"),
                Reflection.getObj(propN, "notEmpty")
        );
    }

    public static void writeNbempty(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetNbempty(
                Arrays.stream(Reflection.<SetVar[]>getObj(prop0, "sets")).mapToInt(Identity::getId).toArray(),
                Reflection.<IntVar>getObj(prop0, "nbEmpty").getId()
        );
    }

    public static void writeNotempty(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetNotempty(
                prop0.getVar(0).getId()
        );
    }

    public static void writeNotmember(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        if (prop0 instanceof PropIntCstNotMemberSet) {
            writer.writeSetNotmember(
                    prop0.getVar(0).getId(),
                    Reflection.getInt(prop0, "cst")
            );
        } else {
            writer.writeSetNotmemberV(
                    prop0.getVar(1).getId(),
                    prop0.getVar(0).getId()
            );
        }
    }

    public static void writeOffset(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetOffset(
                prop0.getVar(0).getId(),
                prop0.getVar(1).getId(),
                Reflection.getInt(prop0, "offSet")
        );
    }

    public static void writePartition(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator propN = propagators[propagators.length - 1];
        int n = propN.getNbVars();
        writer.writeSetPartition(
                IntStream.range(0, n - 1).map(i -> propN.getVar(i).getId()).toArray(),
                propN.getVar(n - 1).getId()
        );
    }

    public static void writeSubseteq(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        int[] ids = new int[propagators.length + 1];
        ids[0] = propagators[0].getVar(0).getId();
        for (int i = 0; i < propagators.length; i++) {
            ids[i + 1] = propagators[i].getVar(1).getId();
        }
        writer.writeSetSubseteq(ids);
    }

    public static void writeSum(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetSum(
                prop0.getVar(0).getId(),
                prop0.getVar(1).getId(),
                Reflection.getObj(prop0, "weights"),
                Reflection.getInt(prop0, "offSet")
        );
    }

    public static void writeSymmetric(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeSetSymmetric(
                IntStream.range(0, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray(),
                Reflection.getInt(prop0, "offSet")
        );
    }

    public static void writeUnion(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = prop0.getNbVars();
        writer.writeSetUnion(
                IntStream.range(0, n - 1).map(i -> prop0.getVar(i).getId()).toArray(),
                prop0.getVar(n - 1).getId()
        );
    }
}
