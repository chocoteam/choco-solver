/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.constraints.unary;

import gnu.trove.set.hash.TIntHashSet;

import org.chocosolver.util.Reflection;
import org.chocosolver.writer.constraints.ConstraintWriter;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;

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
            case "PropMemberBound":
                writer.writeMember(
                        prop.getVar(0).getId(),
                        Reflection.getInt(prop, "lb"),
                        Reflection.getInt(prop, "ub"));
                break;
            case "PropNotMemberBound":
                writer.writeNotMember(
                        prop.getVar(0).getId(),
                        Reflection.getInt(prop, "lb"),
                        Reflection.getInt(prop, "ub"));
                break;
            case "PropMemberEnum": {
                writer.writeMember(
                        prop.getVar(0).getId(),
                        Reflection.<TIntHashSet>getObj(prop, "values").toArray());
            }
            break;
            case "PropNotMemberEnum": {
                writer.writeNotMember(
                        prop.getVar(0).getId(),
                        Reflection.<TIntHashSet>getObj(prop, "values").toArray());
            }
            break;
        }
    }
}
