/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
 */
package org.chocosolver.writer.constraints.binary;

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
public class BinaryWriterHelper {

    public static void writeArithmetic(ConstraintWriter writer, Propagator prop) throws IOException {
        switch (prop.getClass().getSimpleName()) {
            case "PropEqualX_Y":
                writer.writeArithm2(
                        prop.getVar(0).getId(),
                        Operator.EQ,
                        prop.getVar(1).getId()
                );
                break;
            case "PropEqualX_YC":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        Operator.EQ,
                        prop.getVar(1).getId(),
                        Reflection.getInt(prop, "cste")
                );
                break;
            case "PropEqualXY_C":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        prop.getVar(1).getId(),
                        Operator.EQ,
                        Reflection.getInt(prop, "cste")
                );
                break;
            case "PropNotEqualX_Y":
                writer.writeArithm2(
                        prop.getVar(0).getId(),
                        Operator.NQ,
                        prop.getVar(1).getId()
                );
                break;
            case "PropNotEqualX_YC":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        Operator.NQ,
                        prop.getVar(1).getId(),
                        Reflection.getInt(prop, "cste")
                );
                break;
            case "PropNotEqualXY_C":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        prop.getVar(1).getId(),
                        Operator.NQ,
                        Reflection.getInt(prop, "cste")
                );
                break;
            case "PropGreaterOrEqualX_Y":
                writer.writeArithm2(
                        prop.getVar(0).getId(),
                        Operator.GE,
                        prop.getVar(1).getId()
                );
                break;
            case "PropGreaterOrEqualX_YC":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        Operator.GE,
                        prop.getVar(1).getId(),
                        Reflection.getInt(prop, "cste")
                );
                break;
            case "PropGreaterOrEqualXY_C":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        prop.getVar(1).getId(),
                        Operator.GE,
                        Reflection.getInt(prop, "cste")
                );
                break;
            case "PropLessOrEqualXY_C":
                writer.writeArithm3(
                        prop.getVar(0).getId(),
                        prop.getVar(1).getId(),
                        Operator.LE,
                        Reflection.getInt(prop, "cste")
                );
                break;
        }
    }


    public static void writeElement(ConstraintWriter writer, Propagator prop) throws IOException {
        writer.writeElement(
                prop.getVar(0).getId(),
                Reflection.getObj(prop, "values"),
                prop.getVar(1).getId(),
                Reflection.getInt(prop, "offset")
        );
    }

    public static void writeBasicReification(ConstraintWriter writer, Propagator prop0) throws IOException {
        switch (prop0.getClass().getSimpleName()) {
            case "PropXeqCReif":
                writer.writeBasicreification1(
                        prop0.getVar(0).getId(),
                        "=",
                        Reflection.getInt(prop0, "cste"),
                        prop0.getVar(1).getId()
                );
                break;
            case "PropXeqYReif":
                writer.writeBasicreification2(
                        prop0.getVar(0).getId(),
                        "=",
                        prop0.getVar(1).getId(),
                        prop0.getVar(2).getId()
                );
                break;
            case "PropXgtCReif":
                writer.writeBasicreification1(
                        prop0.getVar(0).getId(),
                        ">",
                        Reflection.getInt(prop0, "cste"),
                        prop0.getVar(1).getId()
                );
                break;
            case "PropXinSReif":
                writer.writeBasicreification1(
                        prop0.getVar(0).getId(),
                        "in",
                        Reflection.getObj(prop0, "set"),
                        prop0.getVar(1).getId()
                );
                break;
            case "PropXltCReif":
                writer.writeBasicreification1(
                        prop0.getVar(0).getId(),
                        "<",
                        Reflection.getInt(prop0, "cste"),
                        prop0.getVar(1).getId()
                );
                break;
            case "PropXltYCReif":
                writer.writeBasicreification2(
                        prop0.getVar(0).getId(),
                        "<",
                        prop0.getVar(1).getId(),
                        Reflection.getInt(prop0, "cste"),
                        prop0.getVar(2).getId()
                );
                break;
            case "PropXltYReif":
                writer.writeBasicreification2(
                        prop0.getVar(0).getId(),
                        "<",
                        prop0.getVar(1).getId(),
                        prop0.getVar(2).getId()
                );
                break;
            case "PropXneCReif":
                writer.writeBasicreification1(
                        prop0.getVar(0).getId(),
                        "!=",
                        Reflection.getInt(prop0, "cste"),
                        prop0.getVar(1).getId()
                );
                break;
            case "PropXneYReif":
                writer.writeBasicreification2(
                        prop0.getVar(0).getId(),
                        "!=",
                        prop0.getVar(1).getId(),
                        prop0.getVar(2).getId()
                );
                break;
        }
    }
}
