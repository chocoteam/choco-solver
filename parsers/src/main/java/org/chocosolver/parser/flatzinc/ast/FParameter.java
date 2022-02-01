/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast;

import org.chocosolver.parser.Exit;
import org.chocosolver.parser.flatzinc.ast.declaration.DArray;
import org.chocosolver.parser.flatzinc.ast.declaration.DInt2;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.variables.Variable;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Parameter construction with flatzinc-like object in parameter.
* A Parameter is defined like :
* </br> 'type : identifier = expression'
*/
public final class FParameter {

    public static void make_parameter(Datas datas, Declaration type, String identifier, Expression expression) {
        switch (type.typeOf) {
            case BOOL:
                buildBool(identifier, (EBool) expression, datas);
                break;
            case INT:
            case INT2:
            case INTN:
                buildInt(identifier, (EInt) expression, datas);
                break;
            case SET:
            case SETOFINT:
                buildSet(identifier, (ESet) expression, datas);
                break;
            case ARRAY:
                DArray arr = (DArray) type;
                if (arr.getDimension() == 1) {
                    DInt2 index = (DInt2) arr.getIndex(0);
                    buildArray(identifier, index, arr.getWhat(), (EArray) expression, datas);
                } else {
                    Exit.log("cannot handle more than one dimension!");
                }
                break;
        }

    }

    /**
     * Build a boolean primitive and add it to the {@code flatzinc.parser.FZNParser.map}.
     *
     * @param name  key name
     * @param value {@link org.chocosolver.parser.flatzinc.ast.expression.EBool} storing the value
     * @param datas map from Model to Solver
     * @return {@link boolean}
     */
    private static boolean buildBool(String name, EBool value, Datas datas) {
        boolean b = value.value;
        datas.register(name, b);
        return b;
    }

    /**
     * Build a int primitive and add it to the {@code flatzinc.parser.FZNParser.map}.
     *
     * @param name  key name
     * @param value {@link org.chocosolver.parser.flatzinc.ast.expression.EInt} storing the value.
     * @param datas map from Model to Solver
     * @return {@link int}
     */
    private static int buildInt(String name, EInt value, Datas datas) {
        int i = value.value;
        datas.register(name, i);
        return i;
    }

    /**
     * Build a {@link Variable} object
     * and add it to the {@code flatzinc.parser.FZNParser.map}.
     *
     * @param name  key name
     * @param set   {@link org.chocosolver.parser.flatzinc.ast.expression.ESet} defining the set
     * @param datas map from Model to Solver
     * @return {@link Variable}
     */
    private static int[] buildSet(String name, ESet set, Datas datas) {
        final int[] s;
        switch (set.getTypeOf()) {
            case SET_B:
                ESetBounds bset = (ESetBounds) set;
                s = bset.enumVal();
                break;
            case SET_L:
                ESetList lset = (ESetList) set;
                s = lset.enumVal();
                break;
            default:
                s = null;
                Exit.log("Unknown expression");
                break;

        }
        datas.register(name, s);
        return s;
    }

    /**
     * Build an array of object and add it to the {@code flatzinc.parser.FZNParser.map}.
     *
     * @param name  key name
     * @param index size definition
     * @param what  type of object
     * @param value input declaration
     * @param datas map from Model to Solver
     */
    private static void buildArray(String name, DInt2 index, Declaration what, EArray value, Datas datas) {
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        int size = index.getUpp();
        switch (what.typeOf) {
            case BOOL:
                boolean[] barr = new boolean[size];
                for (int i = 0; i < size; i++) {
                    barr[i] = ((EBool) value.getWhat_i(i)).value;
                }
                datas.register(name, barr);
                break;
            case INT:
            case INT2:
            case INTN:
                int[] iarr = new int[size];
                for (int i = 0; i < size; i++) {
                    iarr[i] = ((EInt) value.getWhat_i(i)).value;
                }
                datas.register(name, iarr);
                break;
            case SET: {
                int[][] sarr = new int[size][];
                for (int i = 0; i < size; i++) {
                    sarr[i] = ((ESet) value.getWhat_i(i)).enumVal();
                }
                datas.register(name, sarr);
            }
            break;
            case SETOFINT: {
                int[][] sarr = new int[size][];
                for (int i = 0; i < size; i++) {
                    sarr[i] = ((ESet) value.getWhat_i(i)).enumVal();
                }
                datas.register(name, sarr);
            }
            break;
            default:
                throw new UnsupportedOperationException("Parameter#buildArray ARRAY: unexpected type for " + name);
        }
    }

}
