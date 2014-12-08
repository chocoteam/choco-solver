/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.parser.flatzinc.ast.expression;

import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.Exit;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

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
    public BoolVar boolVarValue(Solver solver) {
        if (Integer.class.isInstance(object)) {
            return ((Integer) object == 1) ? solver.ONE : solver.ZERO;
        } else if (Boolean.class.isInstance(object)) {
            return ((Boolean) object) ? solver.ONE : solver.ZERO;
        }
        return (BoolVar) object;
    }

    @Override
    public BoolVar[] toBoolVarArray(Solver solver) {
        if (object.getClass().isArray()) {
            //Can be array of int => array of IntegerConstantVariable
            if (int_arr.isInstance(object)) {
                int[] values = (int[]) object;
                BoolVar[] vars = new BoolVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = ((Integer) object == 1) ? solver.ONE : solver.ZERO;
                }
                return vars;
            } else if (bool_arr.isInstance(object)) {
                int[] values = bools_to_ints((boolean[]) object);
                BoolVar[] vars = new BoolVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = ((Boolean) object) ? solver.ONE : solver.ZERO;
                }
                return vars;
            }
            return (BoolVar[]) object;
        }
        Exit.log();
        return null;
    }

    @Override
    public IntVar intVarValue(Solver solver) {
        if (Integer.class.isInstance(object)) {
            return VariableFactory.fixed((Integer) object, solver);
        } else if (Boolean.class.isInstance(object)) {
            return VariableFactory.fixed(((Boolean) object) ? 1 : 0, solver);
        }
        return (IntVar) object;
    }

    @Override
    public IntVar[] toIntVarArray(Solver solver) {
        if (object.getClass().isArray()) {
            //Can be array of int => array of IntegerConstantVariable
            if (int_arr.isInstance(object)) {
                int[] values = (int[]) object;
                IntVar[] vars = new IntVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = VariableFactory.fixed(values[i], solver);
                }
                return vars;
            } else if (bool_arr.isInstance(object)) {
                int[] values = bools_to_ints((boolean[]) object);
                IntVar[] vars = new IntVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = VariableFactory.fixed(values[i], solver);
                }
                return vars;
            }
            return (IntVar[]) object;
        }
        Exit.log();
        return null;
    }

    @Override
    public SetVar setVarValue(Solver solver) {
        return (SetVar) object;
    }

    @Override
    public SetVar[] toSetVarArray(Solver solver) {
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
