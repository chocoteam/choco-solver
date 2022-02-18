/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.expression;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for boolean expressions definition based on flatzinc-like objects.
*/
public final class EBool extends Expression {

    public final boolean value;

    public static final EBool instanceTrue = new EBool(true);
    public static final EBool instanceFalse = new EBool(false);

    public static EBool make(boolean value) {
        if (value) return instanceTrue;
        else return instanceFalse;
    }

    private EBool(boolean value) {
        super(EType.BOO);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int intValue() {
        return value ? 1 : 0;
    }

    @Override
    public int[] toIntArray() {
        return new int[]{intValue()};
    }

    @Override
    public boolean boolValue() {
        return value;
    }

    @Override
    public boolean[] toBoolArray() {
        return new boolean[]{boolValue()};
    }

    @Override
    public BoolVar boolVarValue(Model model) {
        return intValue() == 1 ? model.boolVar(true) : model.boolVar(false);
    }

    @Override
    public BoolVar[] toBoolVarArray(Model model) {
        return new BoolVar[]{boolVarValue(model)};
    }

    @Override
    public IntVar intVarValue(Model model) {
        return model.intVar(intValue());
    }

    @Override
    public IntVar[] toIntVarArray(Model solver) {
        return new IntVar[]{intVarValue(solver)};
    }

    @Override
    public int[][] toIntMatrix() {
        return new int[][]{{intValue()}};
    }
}
