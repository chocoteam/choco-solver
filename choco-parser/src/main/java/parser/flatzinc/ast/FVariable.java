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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;

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

public final class FVariable {

    private static final boolean DEBUG = true;
    private static final String NO_NAME = "";
    private static final String output_var = "out";//"output_var";
    private static final String output_array = "out";//"output_array";
    private static final String is_defined_var = "is_";//"is_defined_var";
    private static final String var_is_introduced = "var";//"var_is_introduced";



    static Logger LOGGER = LoggerFactory.getLogger("fzn");

    public static void make_variable(Datas datas, Declaration type, String identifier, List<EAnnotation> annotations,
                                     Expression expression, Solver aSolver) {
        // value is always null, except for ARRAY, it can be defined
        // see Flatzinc specifications for more informations.
        switch (type.typeOf) {
            case INT: {
                IntVar iv = buildWithInt(identifier, expression, datas, aSolver);
                readAnnotations(identifier, iv, type, annotations, datas);
            }
            break;
            case INT2: {
                IntVar iv = buildWithInt2(identifier, (DInt2) type, expression, datas, aSolver);
                readAnnotations(identifier, iv, type, annotations, datas);
            }
            break;
            case INTN: {
                IntVar iv = buildWithManyInt(identifier, (DManyInt) type, expression, datas, aSolver);
                readAnnotations(identifier, iv, type, annotations, datas);
            }
            break;
            case BOOL: {
                BoolVar bv = buildWithBool(identifier, expression, datas, aSolver);
                readAnnotations(identifier, bv, type, annotations, datas);
            }
            break;
            case SET: {
                SetVar sv = buildWithSet(identifier, (DSet) type, datas);
//                readAnnotations(identifier, sv, type, annotations, datas);
            }
            break;
            case ARRAY: {
                IntVar[] vs;
                if (expression == null) {
                    vs = buildWithDArray(identifier, (DArray) type, expression, datas, aSolver);
                } else {
                    vs = buildWithDArray(identifier, (DArray) type, expression, datas, aSolver);
                }
                readAnnotations(identifier, vs, type, annotations, datas);
            }
            break;
        }

    }

    private static void readAnnotations(String name, IntVar var, Declaration type, List<EAnnotation> expressions, Datas datas) {
        boolean is_introduced = false;
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            Expression.EType etype = expression.getTypeOf();
            //Annotation varanno = Annotation.none;
            String varanno = NO_NAME;
            switch (etype) {
                case IDE:
                    EIdentifier identifier = (EIdentifier) expression;
                    varanno = identifier.value;//;Annotation.valueOf((identifier).value);
                    break;
                case ANN:
                    EAnnotation eanno = (EAnnotation) expression;
                    varanno = eanno.id.value;//Annotation.valueOf(eanno.id.value);
                    break;
            }
            /*switch (varanno) {
                case output_var:
                    datas.declareOutput(name, var, type);
                    break;
                case var_is_introduced:
                    is_introduced = true;
            }*/
            if (varanno.startsWith(output_var)) {
                datas.declareOutput(name, var, type);
            } else if (varanno.startsWith(var_is_introduced)) {
                is_introduced = true;
            }
        }
        if (!is_introduced) {
            datas.addSearchVars(var);
        }
    }

    private static void readAnnotations(String name, IntVar[] vars, Declaration type, List<EAnnotation> expressions, Datas datas) {
        boolean is_introduced = false;
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            Expression.EType etype = expression.getTypeOf();
            //Annotation varanno = Annotation.none;
            String varanno = NO_NAME;
            switch (etype) {
                case IDE:
                    EIdentifier identifier = (EIdentifier) expression;
                    varanno = identifier.value;//Annotation.valueOf((identifier).value);
                    break;
                case ANN:
                    EAnnotation eanno = (EAnnotation) expression;
                    varanno = eanno.id.value;//Annotation.valueOf(eanno.id.value);
                    break;
            }
            /*switch (varanno) {
                case output_array:
                    EAnnotation eanno = (EAnnotation) expression;
                    datas.declareOutput(name, vars, eanno.exps, type);
                    break;
                case is_defined_var:
                    is_introduced = true;
            }*/
            if (varanno.startsWith(output_array)) {
                EAnnotation eanno = (EAnnotation) expression;
                datas.declareOutput(name, vars, eanno.exps, type);
            } else if (varanno.startsWith(var_is_introduced)) {
                is_introduced = true;
            }

        }
        if (!is_introduced) {
            datas.addSearchVars(vars);
        }
    }


    /**
     * Build a {@link solver.variables.Variable} named {@code name}.
     *
     * @param name   name of the boolean variable
     * @param datas
     * @param solver
     * @return {@link solver.variables.Variable}
     */

    private static BoolVar buildWithBool(String name, Expression expression, Datas datas, Solver solver) {
        final BoolVar bi;
        if (expression != null) {
            bi = (BoolVar) buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, solver);
        } else {
            bi = VariableFactory.bool(DEBUG ? name : NO_NAME, solver);
        }
        datas.register(name, bi);
        return bi;
    }

    /**
     * Build an unbounded {@link solver.variables.Variable} named {@code name}, defined by {@code type}.
     *
     * @param name       name of the variable
     * @param expression
     * @param datas
     * @param solver     @return {@link solver.variables.Variable}
     */
    private static IntVar buildWithInt(String name, Expression expression, Datas datas, Solver solver) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, solver);
        } else {
            iv = VariableFactory.bounded(DEBUG ? name : NO_NAME, -999999, 999999, solver);
        }
        datas.register(name, iv);
        return iv;
    }

    /**
     * Build a {@link solver.variables.Variable} named {@code name}, defined by {@code type}.
     *
     * @param name   name of the variable
     * @param type   {@link parser.flatzinc.ast.declaration.DInt2} object
     * @param datas
     * @param solver
     * @return {@link solver.variables.Variable}
     */
    private static IntVar buildWithInt2(String name, DInt2 type, Expression expression, Datas datas, Solver solver) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, solver);
            int lb = type.getLow();
            int ub = type.getUpp();
            solver.post(IntConstraintFactory.member(iv, lb, ub));
        } else {
            int size = type.getUpp() - type.getLow() + 1;
            if (size < 128) {
                iv = VariableFactory.enumerated(DEBUG ? name : NO_NAME, type.getLow(), type.getUpp(), solver);
            } else {
                iv = VariableFactory.bounded(DEBUG ? name : NO_NAME, type.getLow(), type.getUpp(), solver);
            }

        }
        datas.register(name, iv);
        return iv;
    }

    /**
     * Build a {@link solver.variables.Variable} named {@code name}, defined by {@code type}.
     * {@code type} is expected to be a {@link parser.flatzinc.ast.declaration.DManyInt} object.
     *
     * @param name   name of the variable
     * @param type   {@link parser.flatzinc.ast.declaration.DManyInt} object.
     * @param datas
     * @param solver
     * @return {@link solver.variables.Variable}
     */
    private static IntVar buildWithManyInt(String name, DManyInt type, Expression expression, Datas datas, Solver solver) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, solver);
            int[] values = type.getValues();
            solver.post(IntConstraintFactory.member(iv, values));
        } else {
            iv = VariableFactory.enumerated(DEBUG ? name : NO_NAME, type.getValues(), solver);
        }
        datas.register(name, iv);
        return iv;
    }


    private static IntVar buildOnExpression(String name, Expression expression, Datas datas, Solver solver) {
        final IntVar iv;
        switch (expression.getTypeOf()) {
            case BOO:
                iv = expression.boolVarValue(solver);
                break;
            case INT:
                iv = VariableFactory.fixed(name, expression.intValue(), solver);
                break;
            case IDE:
                iv = VariableFactory.eq((IntVar) datas.get(expression.toString()));
                break;
            case IDA:
                EIdArray eida = (EIdArray) expression;
                iv = ((IntVar[]) datas.get(eida.name))[eida.index - 1];
                break;
            default:
                iv = null;
                Exit.log("Unknown expression");
        }
        return iv;
    }

    /**
     * Build a {@link solver.variables.Variable} named {@code name}, defined by {@code type}.
     *
     * @param name  name of the variable
     * @param type  {@link parser.flatzinc.ast.declaration.DSet} object.
     * @param datas
     * @return {@link solver.variables.Variable}.
     */
    private static SetVar buildWithSet(String name, DSet type, Datas datas) {
//        final Declaration what = type.getWhat();
//        final SetVariable sv;
//        switch (what.typeOf) {
//            case INT:
//                LOGGER.severe("PVariable#buildWithSet INT: unknown constructor for " + name);
//                throw new UnsupportedOperationException();
//            case INT2:
//                DInt2 bounds = (DInt2) what;
//                sv = Choco.makeSetVar(name, bounds.getLow(), bounds.getUpp());
//                datas.register(name, sv);
//                return sv;
//            case INTN:
//                DManyInt values = (DManyInt) what;
//                sv = Choco.makeSetVar(name, values.getValues());
//                datas.register(name, sv);
//                return sv;
//        }
//        return null;
        Exit.log("SET VAR");
        return null;
    }


    /**
     * Build an array of <? extends {@link solver.variables.Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name   name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type   {@link parser.flatzinc.ast.declaration.DArray} object.
     * @param datas
     * @param solver
     */
    private static IntVar[] buildWithDArray(String name, DArray type, Expression expression, Datas datas, Solver solver) {
        final DInt2 index = (DInt2) type.getIndex(0);
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        int size = index.getUpp();
        Declaration what = type.getWhat();
        IntVar[] vs = null;
        switch (what.typeOf) {
            case BOOL:
                BoolVar[] bs = new BoolVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        bs[i - 1] = buildWithBool(name + '_' + i, expression, datas, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    EArray array = (EArray) expression;
                    //build the array
                    for (int i = 0; i < size; i++) {
                        bs[i] = array.getWhat_i(i).boolVarValue(solver);
                    }
                }
                datas.register(name, bs);
                vs = bs;
                break;
            case INT:
                vs = new IntVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        vs[i - 1] = buildWithInt(name + '_' + i, null, datas, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                datas.register(name, vs);
                break;
            case INT2:
                vs = new IntVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        vs[i - 1] = buildWithInt2(name + '_' + i, (DInt2) what, expression, datas, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                datas.register(name, vs);
                break;
            case INTN:
                vs = new IntVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        vs[i - 1] = buildWithManyInt(name + '_' + i, (DManyInt) what, expression, datas, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                datas.register(name, vs);
                break;
            case SET:
//                final SetVariable[] svs = new SetVariable[size];
//                for (int i = 1; i <= size; i++) {
//                    svs[i - 1] = buildWithSet(name + '_' + i, (DSet) what, datas);
//                }
//                datas.register(name, svs);
                Exit.log("SET VAR");
                break;
            default:
                break;
        }
        return vs;

    }

    private static void buildFromIntArray(IntVar[] vs, EArray array, int size, Solver solver) {
        //build the array
        for (int i = 0; i < size; i++) {
            vs[i] = array.getWhat_i(i).intVarValue(solver);
        }
    }

    /**
     * Build an array of <? extends {@link solver.variables.Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name   name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type   {@link parser.flatzinc.ast.declaration.DArray} object.
     * @param earr   array of {@link parser.flatzinc.ast.expression.Expression}
     * @param datas
     * @param solver
     */
    private static IntVar[] buildWithDArray(String name, DArray type, Expression expression, EArray earr, Datas datas, Solver solver) {
        final DInt2 index = (DInt2) type.getIndex(0);
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        final int size = index.getUpp();
        final Declaration what = type.getWhat();

        switch (what.typeOf) {
            case BOOL:
                final BoolVar[] bs = new BoolVar[size];
                for (int i = 0; i < size; i++) {
                    bs[i] = earr.getWhat_i(i).boolVarValue(solver);
                }
                datas.register(name, bs);
                return bs;
            case INT:
            case INT2:
            case INTN:
                final IntVar[] vs = new IntVar[size];
                for (int i = 0; i < size; i++) {
                    vs[i] = earr.getWhat_i(i).intVarValue(solver);
                }
                datas.register(name, vs);
                return vs;
            case SET:
//                final SetVariable[] svs = new SetVariable[size];
//                for (int i = 0; i < size; i++) {
//                    svs[i] = earr.getWhat_i(i).setVarValue();
//                }
//                datas.register(name, svs);
                Exit.log("SET VAR");
                break;
            default:
                break;
        }
        return null;
    }
}
