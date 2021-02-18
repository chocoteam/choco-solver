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

import gnu.trove.map.hash.THashMap;
import org.chocosolver.parser.Exit;
import org.chocosolver.parser.flatzinc.ast.declaration.DArray;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.chocosolver.parser.flatzinc.ast.expression.EArray;
import org.chocosolver.parser.flatzinc.ast.expression.ESetBounds;
import org.chocosolver.parser.flatzinc.ast.expression.ESetList;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.chocosolver.parser.RegParser.PRINT_LOG;

/**
 * An object to maintain a link between the model and the solver, during the parsing phase.
 *
 * @author Charles Prud'homme
 * @since 17/05/13
 */
public class Datas {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final THashMap<String, Object> map;
    private final List<String> output_names;
    private final List<Declaration.DType> output_types;
    private final List<Variable> output_vars;
    private final List<String> output_arrays_names;
    private final List<Declaration.DType> output_arrays_types;
    private final List<Variable[]> output_arrays_vars;

    private boolean printAll;
    private boolean printStat;
    private int nbSolution;
    private StringBuilder stringBuilder = new StringBuilder();

    private Model model;
    private Solution solution;

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    public Datas() {
        this.map = new THashMap<>();
        output_vars = new ArrayList<>();
        output_names = new ArrayList<>();
        output_types = new ArrayList<>();
        output_arrays_names = new ArrayList<>();
        output_arrays_vars = new ArrayList<>();
        output_arrays_types = new ArrayList<>();
    }

    public Datas(Model model, boolean printAll, boolean printStat) {
        this();
        this.printAll = printAll;
        this.printStat = printStat;
        this.model = model;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void register(String name, Object o) {
        map.put(name, o);
    }

    public Object get(String id) {
        return map.get(id);
    }

    void declareOutput(String name, Variable variable, Declaration type) {
        output_names.add(name);
        output_vars.add(variable);
        output_types.add(type.typeOf);
    }

    public void declareOutput(String name, Variable[] variables, List<Expression> indices, Declaration type) {
        EArray array = (EArray) indices.get(0);
        // print the size of the type of array
        stringBuilder.append(name).append(" = array").append(array.what.size()).append("d(");

        // print the size
        build(stringBuilder, array.getWhat_i(0));
        for (int i = 1; i < array.what.size(); i++) {
            stringBuilder.append(',');
            build(stringBuilder, array.getWhat_i(i));
        }
        // prepare to print the values
        if (variables.length > 0) {
            stringBuilder.append(",[%s]);");
        } else {
            stringBuilder.append(",[]);");
        }
        stringBuilder.append("\n");

        output_arrays_names.add(stringBuilder.toString());
        output_arrays_vars.add(variables.clone());
        output_arrays_types.add(((DArray) type).getWhat().typeOf);
        stringBuilder.setLength(0);
    }

    protected String value(Variable var, Declaration.DType type) {
        switch (type) {
            case BOOL:
                return solution.getIntVal((BoolVar) var) == 1 ? "true" : "false";
            case INT:
            case INT2:
            case INTN:
                return Integer.toString(solution.getIntVal((IntVar) var));
            case SET:
                StringBuilder st = new StringBuilder();
                st.append('{');
                for (int i : solution.getSetVal((SetVar) var)) {
                    st.append(i).append(',');
                }
                if (st.length() > 1) st.deleteCharAt(st.length() - 1);
                st.append('}');
                return st.toString();
            default:
                Exit.log();
        }
        return "";
    }

    private int[] build(StringBuilder st, Expression exp) {
        switch (exp.getTypeOf()) {
            case INT:
                int idx = exp.intValue();
                st.append(idx);
                return new int[]{idx};
            case SET_B:
                ESetBounds esb = (ESetBounds) exp;
                st.append(esb.toString());
                return esb.enumVal();
            case SET_L:
                ESetList esl = (ESetList) exp;
                st.append(esl.toString());
                return esl.enumVal();
            default:
                return new int[0];
        }
    }

    private void printSolution(){
        for (int i = 0; i < output_names.size(); i++) {
            if(PRINT_LOG)System.out.printf("%s = %s;\n", output_names.get(i), value(output_vars.get(i), output_types.get(i)));

        }
        for (int i = 0; i < output_arrays_names.size(); i++) {
            String name = output_arrays_names.get(i);
            Variable[] ivars = output_arrays_vars.get(i);
            if (ivars.length > 0) {
                Declaration.DType type = output_arrays_types.get(i);
                stringBuilder.append(value(ivars[0], type));
                for (int j = 1; j < ivars.length; j++) {
                    stringBuilder.append(", ").append(value(ivars[j], type));
                }
                if(PRINT_LOG)System.out.printf(name, stringBuilder.toString());
                stringBuilder.setLength(0);
            } else {
                if(PRINT_LOG)System.out.print(name);
            }
        }
        if (printStat) {
            // TODO used to use the toOneShortLineString that has been removed
            if(PRINT_LOG)System.out.printf("%% %s \n", model.getSolver().getMeasures().toOneLineString());
        }
        if(PRINT_LOG)System.out.print("----------\n");
    }

    public void onSolution() {
        nbSolution++;
        if(solution == null){
            solution = new Solution(model, allOutPutVars());
        }
        solution.record();
        printSolution();
    }

    private Variable[] allOutPutVars() {
        ArrayList<Variable> vars = new ArrayList<>(output_vars);
        for(Variable[] vs:output_arrays_vars){
            Collections.addAll(vars, vs);
        }
        return vars.toArray(new Variable[0]);
    }

    public void doFinalOutPut(boolean complete) {
        Solver solver = model.getSolver();
        // TODO there used to be "isComplete" (e.g. in case LNS stops)
//        boolean complete = solver.getSearchState() == SearchState.TERMINATED;
        if(nbSolution>0){
            if(complete && (printAll || solver.getObjectiveManager().isOptimization())) {
                if(PRINT_LOG)System.out.print("==========\n");
            }
        }else{
            if(complete){
                if(PRINT_LOG)System.out.print("=====UNSATISFIABLE=====\n");
            }else{
                if(PRINT_LOG)System.out.print("=====UNKNOWN=====\n");
            }
        }
        if (printStat) {
            // TODO used to use the toOneShortLineString that has been removed
            if(PRINT_LOG)System.out.printf("%% %s \n", solver.getMeasures().toOneLineString());
            if(PRINT_LOG)System.out.print("%% ");
            if(PRINT_LOG)solver.printShortFeatures();
        }
    }
}
