/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import java.util.List;

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

    final THashMap<String, Object> map;
    final List<String> output_names;
    final List<Declaration.DType> output_types;
    final List<Variable> output_vars;
    final List<String> output_arrays_names;
    final List<Declaration.DType> output_arrays_types;
    final List<Variable[]> output_arrays_vars;

    boolean printAll;
    boolean printStat;
    boolean wrongSolution;
    int nbSolution;
    StringBuilder stringBuilder = new StringBuilder();

    Model model;
    Solution solution;

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

    public void declareOutput(String name, Variable variable, Declaration type) {
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

    public void printSolution(){
        for (int i = 0; i < output_names.size(); i++) {
            System.out.printf("%s = %s;\n", output_names.get(i), value(output_vars.get(i), output_types.get(i)));

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
                System.out.printf(name, stringBuilder.toString());
                stringBuilder.setLength(0);
            } else {
                System.out.printf(name);
            }
        }
        System.out.printf("----------\n");
    }

    public void onSolution() {
        wrongSolution = false;
        nbSolution++;
        if(solution == null){
            solution = new Solution(model, allOutPutVars());
        }
        solution.record();
        printSolution();
        if (printStat) {
            // TODO used to use the toOneShortLineString that has been removed
            System.out.printf("%% %s \n", model.getSolver().getMeasures().toOneLineString());
        }
    }

    private Variable[] allOutPutVars() {
        ArrayList<Variable> vars = new ArrayList<>();
        vars.addAll(output_vars);
        for(Variable[] vs:output_arrays_vars){
            for(Variable v:vs) {
                vars.add(v);
            }
        }
        return vars.toArray(new Variable[0]);
    }

    public void doFinalOutPut(boolean userinterruption) {
        Solver solver = model.getSolver();
        // TODO there used to be "isComplete" (e.g. in case LNS stops)
        boolean complete = !solver.isStopCriterionMet() && !solver.hasEndedUnexpectedly();
        if(nbSolution>0){
            if(complete && (printAll || solver.getObjectiveManager().isOptimization())) {
                System.out.printf("==========\n");
            }
//            if (solver.getObjectiveManager().isOptimization()&& !complete) {
//                System.out.printf("=====UNBOUNDED=====\n");
//            }
        }else{
            if(complete){
                System.out.printf("=====UNSATISFIABLE=====\n");
            }else{
                System.out.printf("=====UNKNOWN=====\n");
            }
        }
        if (printStat) {
            // TODO used to use the toOneShortLineString that has been removed
            System.out.printf("%% %s \n", solver.getMeasures().toOneLineString());
        }
    }
}
