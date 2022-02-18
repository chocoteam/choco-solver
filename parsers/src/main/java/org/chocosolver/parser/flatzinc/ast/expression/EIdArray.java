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
* Class for array index expressions definition based on flatzinc-like objects.
*/
public final class EIdArray extends Expression {

    public final String name;
    public final int index;
    final Object object;

    public EIdArray(Datas datas, String id, int i) {
        super(EType.IDA);
        this.name = id;
        this.index = i;

        Object array = datas.get(name);
        if (int_arr.isInstance(array)) {
            object = ((int[]) array)[index - 1];
        } else if (bool_arr.isInstance(array)) {
            object = ((boolean[]) array)[index - 1] ? 1 : 0;
        } else {
            object = ((Object[]) array)[index - 1];
        }
    }

    @Override
    public String toString() {
        return name + '[' + index + ']';
    }

    @Override
    public int intValue() {
        return (Integer) object;
    }

    @Override
    public int[] toIntArray() {
        return (int[]) object;
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
    public int[] setValue() {
        return (int[]) object;
    }

    @Override
    public int[][] toSetArray() {
        return (int[][]) object;
    }

    @Override
    public BoolVar boolVarValue(Model model) {
        if (object instanceof Integer) {
            return ((Integer) object == 1) ? model.boolVar(true) : model.boolVar(false);
        } else if (object instanceof Boolean) {
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

    @Override
    public IntVar intVarValue(Model model) {
        if (object instanceof Integer) {
            return model.intVar((Integer) object);
        } else if (object instanceof Boolean) {
            return model.intVar(((Boolean) object) ? 1 : 0);
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

    private static int[] bools_to_ints(boolean[] bar) {
        final int[] values = new int[bar.length];
        for (int i = 0; i < bar.length; i++) {
            values[i] = bar[i] ? 1 : 0;
        }
        return values;
    }
}
