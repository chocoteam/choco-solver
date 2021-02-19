/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast;

import org.chocosolver.parser.Exit;
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.*;

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

@SuppressWarnings("ConstantConditions")
public final class FVariable {

    private static final boolean DEBUG = true;
    private static final String NO_NAME = "";
    private static final String output_var = "out";//"output_var";
    private static final String output_array = "out";//"output_array";
    private static final String is_defined_var = "is_";//"is_defined_var";
    private static final String var_is_introduced = "var";//"var_is_introduced";

    public static void make_variable(Datas datas, Declaration type, String identifier, List<EAnnotation> annotations,
                                     Expression expression, Model aModel) {
        // value is always null, except for ARRAY, it can be defined
        // see Flatzinc specifications for more informations.
        switch (type.typeOf) {
            case INT: {
                IntVar iv = buildWithInt(identifier, expression, datas, aModel);
                readAnnotations(identifier, iv, type, annotations, datas);
            }
            break;
            case INT2: {
                IntVar iv = buildWithInt2(identifier, (DInt2) type, expression, datas, aModel);
                readAnnotations(identifier, iv, type, annotations, datas);
            }
            break;
            case INTN: {
                IntVar iv = buildWithManyInt(identifier, (DManyInt) type, expression, datas, aModel);
                readAnnotations(identifier, iv, type, annotations, datas);
            }
            break;
            case BOOL: {
                BoolVar bv = buildWithBool(identifier, expression, datas, aModel);
                readAnnotations(identifier, bv, type, annotations, datas);
            }
            break;
            case SET: {
                SetVar sv = buildWithSet(identifier, (DSet) type, expression, datas, aModel);
                readAnnotations(identifier, sv, type, annotations, datas);
            }
            break;
            case ARRAY: {
                Variable[] vs;
                if (expression == null) {
                    vs = buildWithDArray(identifier, (DArray) type, null, datas, aModel);
                } else {
                    vs = buildWithDArray(identifier, (DArray) type, expression, datas, aModel);
                }
                readAnnotations(identifier, vs, type, annotations, datas);
            }
            break;
        }

    }

    private static void readAnnotations(String name, Variable var, Declaration type, List<EAnnotation> expressions, Datas datas) {
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
            if (varanno.startsWith(output_var)) {
                datas.declareOutput(name, var, type);
            }
        }
    }

    private static void readAnnotations(String name, Variable[] vars, Declaration type, List<EAnnotation> expressions, Datas datas) {
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
            if (varanno.startsWith(output_array)) {
                EAnnotation eanno = (EAnnotation) expression;
                datas.declareOutput(name, vars, eanno.exps, type);
            }
        }
    }


    /**
     * Build a {@link org.chocosolver.solver.variables.Variable} named {@code name}.
     *
     * @param name   name of the boolean variable
     * @param datas map from Model to Solver
     * @param model the solver
     * @return {@link org.chocosolver.solver.variables.Variable}
     */

    private static BoolVar buildWithBool(String name, Expression expression, Datas datas, Model model) {
        final BoolVar bi;
        if (expression != null) {
            bi = (BoolVar) buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, model);
        } else {
            bi = model.boolVar(DEBUG ? name : NO_NAME);
        }
        datas.register(name, bi);
        return bi;
    }

    /**
     * Build an unbounded {@link org.chocosolver.solver.variables.Variable} named {@code name}, defined by {@code type}.
     *
     * @param name       name of the variable
     * @param expression the expression
     * @param datas map from Model to Solver
     * @param model     @return {@link org.chocosolver.solver.variables.Variable}
     */
    private static IntVar buildWithInt(String name, Expression expression, Datas datas, Model model) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, model);
        } else {
            iv = model.intVar(DEBUG ? name : NO_NAME, -999999, 999999);
        }
        datas.register(name, iv);
        return iv;
    }

    /**
     * Build a {@link org.chocosolver.solver.variables.Variable} named {@code name}, defined by {@code type}.
     *
     * @param name   name of the variable
     * @param type   {@link org.chocosolver.parser.flatzinc.ast.declaration.DInt2} object
     * @param datas map from Model to Solver
     * @param model the solver
     * @return {@link org.chocosolver.solver.variables.Variable}
     */
    private static IntVar buildWithInt2(String name, DInt2 type, Expression expression, Datas datas, Model model) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, model);
            int lb = type.getLow();
            int ub = type.getUpp();
            model.member(iv, lb, ub).post();
        } else {
            iv = model.intVar(DEBUG ? name : NO_NAME, type.getLow(), type.getUpp());
        }
        datas.register(name, iv);
        return iv;
    }

    /**
     * Build a {@link org.chocosolver.solver.variables.Variable} named {@code name}, defined by {@code type}.
     * {@code type} is expected to be a {@link org.chocosolver.parser.flatzinc.ast.declaration.DManyInt} object.
     *
     * @param name   name of the variable
     * @param type   {@link org.chocosolver.parser.flatzinc.ast.declaration.DManyInt} object.
     * @param datas map from Model to Solver
     * @param model the solver
     * @return {@link org.chocosolver.solver.variables.Variable}
     */
    private static IntVar buildWithManyInt(String name, DManyInt type, Expression expression, Datas datas, Model model) {
        final IntVar iv;
        if (expression != null) {
            iv = buildOnExpression(DEBUG ? name : NO_NAME, expression, datas, model);
            int[] values = type.getValues();
            model.member(iv, values).post();
        } else {
            iv = model.intVar(DEBUG ? name : NO_NAME, type.getValues());
        }
        datas.register(name, iv);
        return iv;
    }


    private static IntVar buildOnExpression(String name, Expression expression, Datas datas, Model model) {
        final IntVar iv;
        switch (expression.getTypeOf()) {
            case BOO:
                iv = expression.boolVarValue(model);
                break;
            case INT:
                iv = model.intVar(name, expression.intValue());
                break;
            case IDE:
                iv = (IntVar) datas.get(expression.toString());
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
     * Build a {@link org.chocosolver.solver.variables.Variable} named {@code name}, defined by {@code type}.
     *
     * @param name  name of the variable
     * @param type  {@link org.chocosolver.parser.flatzinc.ast.declaration.DSet} object.
     * @param datas map from Model to Solver
     * @return {@link org.chocosolver.solver.variables.Variable}.
     */
    private static SetVar buildWithSet(String name, DSet type, Expression expression, Datas datas, Model model) {
        Declaration what = type.getWhat();
        SetVar sv = null;
        if(expression != null){
            Exit.log("Unknown expression");
        }else {
            int[] ub = null;
            switch (what.typeOf) {
                case INT2:
                    DInt2 bounds = (DInt2) what;
                    ub = new int[bounds.getUpp()-bounds.getLow()+1];
                    for(int i=0;i<ub.length;i++){
                        ub[i] = i+bounds.getLow();
                    }
                    break;
                case INTN:
                    DManyInt mint = (DManyInt) what;
                    ub = mint.getValues();
                    break;
                default:
                    Exit.log("Unknown set type");
                    break;
            }
            if(ub != null){
                sv = model.setVar(DEBUG ? name : NO_NAME, new int[]{}, ub);
            }
        }
        datas.register(name, sv);
        return sv;
    }


    /**
     * Build an array of <? extends {@link org.chocosolver.solver.variables.Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name   name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type   {@link org.chocosolver.parser.flatzinc.ast.declaration.DArray} object.
     * @param datas map from Model to Solver
     * @param solver the solver
     */
    private static Variable[] buildWithDArray(String name, DArray type, Expression expression, Datas datas, Model solver) {
        final DInt2 index = (DInt2) type.getIndex(0);
        // no need to get lowB, it is always 1 (see specification of FZN for more information)
        int size = index.getUpp();
        Declaration what = type.getWhat();
        Variable[] vs = null;
        switch (what.typeOf) {
            case BOOL:
                BoolVar[] bs = new BoolVar[size];
                if (expression == null) {
                    for (int i = 1; i <= size; i++) {
                        bs[i - 1] = buildWithBool(name + '_' + i, null, datas, solver);
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
                        vs[i - 1] = buildWithInt2(name + '_' + i, (DInt2) what, null, datas, solver);
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
                        vs[i - 1] = buildWithManyInt(name + '_' + i, (DManyInt) what, null, datas, solver);
                    }
                } else if (expression.getTypeOf().equals(Expression.EType.ARR)) {
                    buildFromIntArray(vs, (EArray) expression, size, solver);
                }
                datas.register(name, vs);
                break;
            case SET:
                vs = new SetVar[size];
                for (int i = 1; i <= size; i++) {
                    vs[i - 1] = buildWithSet(name + '_' + i, (DSet) what, expression, datas, solver);
                }
                datas.register(name, vs);
                break;
            default:
                break;
        }
        return vs;

    }

    private static void buildFromIntArray(Variable[] vs, EArray array, int size, Model solver) {
        //build the array
        for (int i = 0; i < size; i++) {
            vs[i] = array.getWhat_i(i).intVarValue(solver);
        }
    }
}
