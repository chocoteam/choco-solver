/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package parser.flatzinc.ast;

import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.EIdentifier;
import parser.flatzinc.ast.expression.Expression;
import parser.flatzinc.parser.FZNParser;
import solver.Solver;
import solver.variables.*;
import solver.variables.view.Views;

import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for Variable definition based on flatzinc-like objects.
* A variable is defined like:
* </br> type : identifier annotations [= expression]
*
*/

public final class PVariable extends ParVar {

    private static final boolean DEBUG = true;
    private static final String NO_NAME = "";

    static Logger LOGGER = LoggerFactory.getLogger("fzn");

    private enum Annotation {
        output_var,
        output_array,
        is_defined_var,
        var_is_introduced
    }

    public PVariable(THashMap<String, Object> map, Declaration type, String identifier, List<EAnnotation> annotations, Expression expression, FZNParser parser) {
        Solver solver = parser.solver;
        // value is always null, except for ARRAY, it can be defined
        // see Flatzinc specifications for more informations.
        switch (type.typeOf) {
            case INT1:
                buildWithInt(identifier, expression, map, solver);
                break;
            case INT2:
                buildWithInt2(identifier, (DInt2) type, expression, map, solver);
                break;
            case INTN:
                buildWithManyInt(identifier, (DManyInt) type, expression, map, solver);
                break;
            case BOOL:
                buildWithBool(identifier, expression, map, solver);
                break;
            case SET:
                buildWithSet(identifier, (DSet) type, map);
                break;
            case ARRAY:
                if (expression == null) {
                    buildWithDArray(identifier, (DArray) type, expression, map, solver);
                } else {
                    buildWithDArray(identifier, (DArray) type, (EArray) expression, map, solver);
                }
                break;
        }
        readAnnotations(identifier, annotations, parser, map);

    }

    private static void readAnnotations(String name, List<EAnnotation> annotations, FZNParser parser, THashMap<String, Object> map) {
        for (int i = 0; i < annotations.size(); i++) {
            Expression expression = annotations.get(i);
            Expression.EType type = expression.getTypeOf();
            Annotation varanno;
            switch (type) {
                case IDE:
                    EIdentifier identifier = (EIdentifier) expression;
                    varanno = Annotation.valueOf((identifier).value);
                    switch (varanno) {
                        case output_var:
                            IntVar var = (IntVar) map.get(name);
                            parser.layout.addOutputVar(name, var);
                            break;
                        default:
                            //LOGGER.warn("% Unknown annotation :" + varanno.toString());
                    }
                    break;
                case ANN:
                    EAnnotation eanno = (EAnnotation) expression;
                    varanno = Annotation.valueOf(eanno.id.value);
                    switch (varanno) {
                        case output_array:
                            IntVar[] vars = (IntVar[]) map.get(name);
                            parser.layout.addOutputArrays(name, vars, eanno.exps);
                            break;
                        default:
//                            LOGGER.warn("% Unknown annotation :" + varanno.toString());
                    }
                    break;
                default:
//                    LOGGER.warn("% Unknown annotation :" + type.toString());
            }
        }
    }


    /**
     * Build a {@link Variable} named {@code name}.
     *
     * @param name   name of the boolean variable
     * @param map
     * @param solver
     * @return {@link Variable}
     */
    private static BoolVar buildWithBool(String name, Expression expression, THashMap<String, Object> map, Solver solver) {
        final BoolVar bi;
        if (expression != null) {
            bi = (BoolVar) buildOnExpression(DEBUG ? name : NO_NAME, expression, map, solver);
        } else {
            bi = VariableFactory.bool(DEBUG ? name : NO_NAME, solver);
        }
        map.put(name, bi);
        return bi;
    }

    /**
     * Build an unbounded {@link Variable} named {@code name}, defined by {@code type}.
     *
     * @param name       name of the variable
     * @param expression
     * @param map
     * @param solver     @return {@link Variable}
     */
    private static IntVar buildWithInt(String name, Expression expression, THashMap<String, Object> map, Solver solver) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, map, solver);
        } else {
            iv = VariableFactory.bounded(DEBUG ? name : NO_NAME, (int) (Integer.MIN_VALUE * .99d),
                    (int) (Integer.MAX_VALUE * .99d), solver);
        }
        map.put(name, iv);
        return iv;
    }

    /**
     * Build a {@link Variable} named {@code name}, defined by {@code type}.
     *
     * @param name   name of the variable
     * @param type   {@link parser.flatzinc.ast.declaration.DInt2} object
     * @param map
     * @param solver
     * @return {@link Variable}
     */
    private static IntVar buildWithInt2(String name, DInt2 type, Expression expression, THashMap<String, Object> map, Solver solver) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, map, solver);
        } else {
            iv = VariableFactory.bounded(DEBUG ? name : NO_NAME, type.getLow(), type.getUpp(), solver);

        }
        map.put(name, iv);
        return iv;
    }

    /**
     * Build a {@link Variable} named {@code name}, defined by {@code type}.
     * {@code type} is expected to be a {@link parser.flatzinc.ast.declaration.DManyInt} object.
     *
     * @param name   name of the variable
     * @param type   {@link parser.flatzinc.ast.declaration.DManyInt} object.
     * @param map
     * @param solver
     * @return {@link Variable}
     */
    private static IntVar buildWithManyInt(String name, DManyInt type, Expression expression, THashMap<String, Object> map, Solver solver) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, map, solver);
        } else {
            iv = VariableFactory.enumerated(DEBUG ? name : NO_NAME, type.getValues(), solver);
        }
        map.put(name, iv);
        return iv;
    }


    private static IntVar buildOnExpression(String name, Expression expression, THashMap<String, Object> map, Solver solver) {
        final IntVar iv;
        switch (expression.getTypeOf()) {
            case INT:
                iv = Views.fixed(name, expression.intValue(), solver);
                break;
            case IDE:
                iv = Views.eq((IntVar) map.get(expression.toString()));
                break;
            default:
                iv = null;
                Exit.log("Unknown expression");
        }
        return iv;
    }

    /**
     * Build a {@link Variable} named {@code name}, defined by {@code type}.
     *
     * @param name name of the variable
     * @param type {@link parser.flatzinc.ast.declaration.DSet} object.
     * @param map
     * @return {@link Variable}.
     */
    private static SetVar buildWithSet(String name, DSet type, THashMap<String, Object> map) {
//        final Declaration what = type.getWhat();
//        final SetVariable sv;
//        switch (what.typeOf) {
//            case INT1:
//                LOGGER.severe("PVariable#buildWithSet INT1: unknown constructor for " + name);
//                throw new UnsupportedOperationException();
//            case INT2:
//                DInt2 bounds = (DInt2) what;
//                sv = Choco.makeSetVar(name, bounds.getLow(), bounds.getUpp());
//                map.put(name, sv);
//                return sv;
//            case INTN:
//                DManyInt values = (DManyInt) what;
//                sv = Choco.makeSetVar(name, values.getValues());
//                map.put(name, sv);
//                return sv;
//        }
//        return null;
        Exit.log("SET VAR");
        return null;
    }


    /**
     * Build an array of <? extends {@link Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name   name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type   {@link parser.flatzinc.ast.declaration.DArray} object.
     * @param map
     * @param solver
     */
    private static void buildWithDArray(String name, DArray type, Expression expression, THashMap<String, Object> map, Solver solver) {
        final DInt2 index = (DInt2) type.getIndex();
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        final int size = index.getUpp();
        final Declaration what = type.getWhat();
        final IntVar[] vs;
        switch (what.typeOf) {
            case BOOL:
                BoolVar[] bs = new BoolVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        bs[i - 1] = buildWithBool(name + '_' + i, expression, map, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    EArray array = (EArray) expression;
                    //build the array
                    for (int i = 0; i < size; i++) {
                        bs[i] = array.getWhat_i(i).boolVarValue(solver);
                    }
                }
                map.put(name, bs);
                break;
            case INT1:
                vs = new IntVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        vs[i - 1] = buildWithInt(name + '_' + i, null, map, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                map.put(name, vs);
                break;
            case INT2:
                vs = new IntVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        vs[i - 1] = buildWithInt2(name + '_' + i, (DInt2) what, expression, map, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                map.put(name, vs);
                break;
            case INTN:
                vs = new IntVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        vs[i - 1] = buildWithManyInt(name + '_' + i, (DManyInt) what, expression, map, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                map.put(name, vs);
                break;
            case SET:
//                final SetVariable[] svs = new SetVariable[size];
//                for (int i = 1; i <= size; i++) {
//                    svs[i - 1] = buildWithSet(name + '_' + i, (DSet) what, map);
//                }
//                map.put(name, svs);
                Exit.log("SET VAR");
                break;
            default:
                break;
        }

    }

    private static void buildFromIntArray(IntVar[] vs, EArray array, int size, Solver solver) {
        //build the array
        for (int i = 0; i < size; i++) {
            vs[i] = array.getWhat_i(i).intVarValue(solver);
        }
    }

    /**
     * Build an array of <? extends {@link Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name   name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type   {@link parser.flatzinc.ast.declaration.DArray} object.
     * @param earr   array of {@link parser.flatzinc.ast.expression.Expression}
     * @param map
     * @param solver
     */
    private static void buildWithDArray(String name, DArray type, Expression expression, EArray earr, THashMap<String, Object> map, Solver solver) {
        final DInt2 index = (DInt2) type.getIndex();
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        final int size = index.getUpp();
        final Declaration what = type.getWhat();

        switch (what.typeOf) {
            case BOOL:
                final BoolVar[] bs = new BoolVar[size];
                for (int i = 0; i < size; i++) {
                    bs[i] = earr.getWhat_i(i).boolVarValue(solver);
                }
                map.put(name, bs);
                break;
            case INT1:
            case INT2:
            case INTN:
                final IntVar[] vs = new IntVar[size];
                for (int i = 0; i < size; i++) {
                    vs[i] = earr.getWhat_i(i).intVarValue(solver);
                }
                map.put(name, vs);
                break;
            case SET:
//                final SetVariable[] svs = new SetVariable[size];
//                for (int i = 0; i < size; i++) {
//                    svs[i] = earr.getWhat_i(i).setVarValue();
//                }
//                map.put(name, svs);
                Exit.log("SET VAR");
                break;
            default:
                break;
        }
    }
}
