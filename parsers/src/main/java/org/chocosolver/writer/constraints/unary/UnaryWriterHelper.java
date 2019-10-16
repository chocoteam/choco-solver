/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.constraints.unary;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.writer.util.Reflection;
import org.chocosolver.writer.constraints.ConstraintWriter;

import java.io.IOException;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 19/09/2017.
 */
public class UnaryWriterHelper {

    public static void writeArithmetic(ConstraintWriter writer, Propagator prop) throws IOException {
        switch (prop.getClass().getSimpleName()) {
            case "PropEqualXC":
                writer.writeArithm1(
                        prop.getVar(0).getId(),
                        Operator.EQ,
                        Reflection.getInt(prop, "constant")
                );
                break;
            case "PropNotEqualXC":
                writer.writeArithm1(
                        prop.getVar(0).getId(),
                        Operator.NQ,
                        Reflection.getInt(prop, "constant")
                );
                break;
            case "PropGreaterOrEqualXC":
                writer.writeArithm1(
                        prop.getVar(0).getId(),
                        Operator.GE,
                        Reflection.getInt(prop, "constant")
                );
                break;
            case "PropLessOrEqualXC":
                writer.writeArithm1(
                        prop.getVar(0).getId(),
                        Operator.LE,
                        Reflection.getInt(prop, "constant")
                );
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void writeMember(ConstraintWriter writer, Propagator prop) throws IOException {
        String name = prop.getClass().getSimpleName();
        switch (name) {
            case "PropMember":
                writer.writeMember(
                        prop.getVar(0).getId(),
                        Reflection.getObj(prop, "range"));
                break;
            case "PropNotMember":
                writer.writeNotMember(
                        prop.getVar(0).getId(),
                    Reflection.getObj(prop, "range"));
                break;
        }
    }
}
