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

package parser.flatzinc.ast;

import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.declaration.DArray;
import parser.flatzinc.ast.declaration.DInt2;
import parser.flatzinc.ast.declaration.Declaration;
import parser.flatzinc.ast.expression.*;
import solver.variables.Variable;

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
     * @param value {@link parser.flatzinc.ast.expression.EBool} storing the value
     * @param datas
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
     * @param value {@link parser.flatzinc.ast.expression.EInt} storing the value.
     * @param datas
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
     * @param name key name
     * @param set  {@link parser.flatzinc.ast.expression.ESet} defining the set
     * @param datas
     * @return {@link Variable}
     */
    private static Variable buildSet(String name, ESet set, Datas datas) {
//        final SetConstantVariable s;
//        switch (set.getTypeOf()){
//            case SET_B:
//                ESetBounds bset = (ESetBounds)set;
//                s = Choco.constant(bset.enumVal());
//                map.put(name, s);
//                return s;
//            case SET_L:
//                ESetList lset = (ESetList)set;
//                s = Choco.constant(lset.enumVal());
//                map.put(name, s);
//                return s;
//            default:
//                return null;
//        }
        Exit.log();
        return null;
    }

    /**
     * Build an array of object and add it to the {@code flatzinc.parser.FZNParser.map}.
     *
     * @param name  key name
     * @param index size definition
     * @param what  type of object
     * @param value input declaration
     * @param datas
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
            case SET:
                int[][] sarr = new int[size][];
                for (int i = 0; i < size; i++) {
                    sarr[i] = ((ESet) value.getWhat_i(i)).enumVal();
                }
                datas.register(name, sarr);
//                SetConstantVariable[] sarr = new SetConstantVariable[size];
//                for(int i = 0; i < size; i++){
//                    sarr[i] = buildSet(name+ '_' +i, (ESet)value.getWhat_i(i), map);
//                }
//                map.put(name, sarr);
//                Exit.log("% array of set unavailable");
                break;
            case ARRAY:
                LoggerFactory.getLogger("parser").error("Parameter#buildArray ARRAY: unexpected type for " + name);
                throw new UnsupportedOperationException();
        }
    }

}
