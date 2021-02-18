/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.expression;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;



/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for int expressions definition based on flatzinc-like objects.
*/
public final class EInt extends Expression {

    public final int value;

    private static EInt[] cache;
    final static int low = -128;

    // high value may be configured by property
    final static int high = 127;

    static {
        cache = new EInt[(high - low) + 1];
        int j = low;
        for (int k = 0; k < cache.length; k++)
            cache[k] = new EInt(j++);
    }


//    public EInt(String sign, String svalue) {
//        super(EType.INT);
//        value = Integer.parseInt((sign.equals("-")?sign:"") + svalue);
//    }

    public static EInt make(String svalue) {
        int value = Integer.parseInt(svalue);
        return make(value);
    }

    public static EInt make(int value) {
        if (value >= -128 && value <= high)
            return cache[value + 128];
        else
            return new EInt(value);
    }

    private EInt(int value) {
        super(EType.INT);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public int[] toIntArray() {
        return new int[]{intValue()};
    }

    @Override
    public int[][] toIntMatrix() {
        return new int[][]{{intValue()}};
    }


    @Override
    public IntVar intVarValue(Model model) {
        return model.intVar(intValue());
    }

    @Override
    public IntVar[] toIntVarArray(Model solver) {
        return new IntVar[]{intVarValue(solver)};
    }
}
