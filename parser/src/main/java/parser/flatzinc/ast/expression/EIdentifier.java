/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.flatzinc.ast.expression;

import gnu.trove.THashMap;
import parser.flatzinc.ast.Exit;
import solver.Solver;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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

    public EIdentifier(THashMap<String, Object> map, String s) {
        super(EType.IDE);
        this.value = s;
        object = map.get(value);
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
    public IntVar intVarValue(Solver solver) {
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
                    vars[i] = VariableFactory.fixed(values[i]);
                }
                return vars;
            } else if (bool_arr.isInstance(object)) {
                int[] values = bools_to_ints((boolean[]) object);
                IntVar[] vars = new IntVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = VariableFactory.fixed(values[i]);
                }
                return vars;
            }
            return (IntVar[]) object;
        }
        Exit.log();
        return null;
    }

    @Override
    public BoolVar boolVarValue(Solver solver) {
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
                    vars[i] = (BoolVar) VariableFactory.fixed(values[i]);
                }
                return vars;
            } else if (bool_arr.isInstance(object)) {
                int[] values = bools_to_ints((boolean[]) object);
                BoolVar[] vars = new BoolVar[values.length];
                for (int i = 0; i < values.length; i++) {
                    vars[i] = (BoolVar) VariableFactory.fixed(values[i]);
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
}
