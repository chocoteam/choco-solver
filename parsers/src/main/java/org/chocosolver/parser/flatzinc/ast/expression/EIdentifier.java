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

import org.chocosolver.parser.Exit;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;


/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for identifier expressions definition based on flatzinc-like objects.
*/
public final class EIdentifier extends Expression {

    public final String value;

    public final Object object;

    public EIdentifier(Datas datas, String s) {
        super(EType.IDE);
        this.value = s;
        object = datas.get(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int intValue() {
        return (Integer) object;
    }

    @Override
    public int[] toIntArray() {
        if (bool_arr.isInstance(object)) {
            return bools_to_ints((boolean[]) object);
        }
        return (int[]) object;
    }

    @Override
    public int[][] toIntMatrix() {
        if (bool_arr.isInstance(object)) {
            return bools_to_ints((boolean[][]) object);
        }
        return (int[][]) object;
    }

    @Override
    public IntVar intVarValue(Model model) {
        if(Integer.class.isInstance(object)){
            return model.intVar(intValue());
        }
        return (IntVar) object;
    }

    @Override
    public IntVar[] toIntVarArray(Model model) {
        if (object.getClass().isArray()) {
            //Can be array of int => array of IntegerConstantVariable
            if (int_arr.isInstance(object)) {
                int[] values = (int[]) object;
                IntVar[] vars = new IntVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = model.intVar(values[i]);
                }
                return vars;
            } else if (bool_arr.isInstance(object)) {
                int[] values = bools_to_ints((boolean[]) object);
                IntVar[] vars = new IntVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = model.intVar(values[i]);
                }
                return vars;
            }
            return (IntVar[]) object;
        }
        Exit.log();
        return null;
    }

    @Override
    public boolean boolValue() {
        return (Boolean) object;
    }

    @Override
    public boolean[] toBoolArray() {
        return (boolean[]) object;
    }

    @Override
    public BoolVar boolVarValue(Model model) {
        if (Integer.class.isInstance(object)) {
            return ((Integer) object == 1) ? model.boolVar(true) : model.boolVar(false);
        } else if (Boolean.class.isInstance(object)) {
            return ((Boolean) object) ? model.boolVar(true) : model.boolVar(false);
        }
        return (BoolVar) object;
    }

    @Override
    public BoolVar[] toBoolVarArray(Model model) {
        if (object.getClass().isArray()) {
            //Can be array of int => array of IntegerConstantVariable
            if (int_arr.isInstance(object)) {
                int[] values = (int[]) object;
                BoolVar[] vars = new BoolVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = ((Integer) object == 1) ? model.boolVar(true) : model.boolVar(false);
                }
                return vars;
            } else if (bool_arr.isInstance(object)) {
                int[] values = bools_to_ints((boolean[]) object);
                BoolVar[] vars = new BoolVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = ((Boolean) object) ? model.boolVar(true) : model.boolVar(false);
                }
                return vars;
            }
            return (BoolVar[]) object;
        }
        Exit.log();
        return null;
    }


    private static int[] bools_to_ints(boolean[] bar) {
        final int[] values = new int[bar.length];
        for (int i = 0; i < bar.length; i++) {
            values[i] = bar[i] ? 1 : 0;
        }
        return values;
    }

    private static int[][] bools_to_ints(boolean[][] bar) {
        final int[][] values = new int[bar.length][];
        for (int i = 0; i < bar.length; i++) {
            values[i] = new int[bar[i].length];
            for(int j = 0; j < bar[i].length; j++){
                values[i][j] = bar[i][j] ? 1 : 0;
            }
        }
        return values;
    }

    @Override
    public SetVar setVarValue(Model solver) {
        return (SetVar) object;
    }

    @Override
    public SetVar[] toSetVarArray(Model solver) {
        if (object.getClass().isArray()) {
            return (SetVar[]) object;
        }
        Exit.log();
        return null;
    }
}
