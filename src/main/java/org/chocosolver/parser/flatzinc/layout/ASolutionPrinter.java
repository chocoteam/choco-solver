/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.parser.flatzinc.layout;

import org.chocosolver.parser.flatzinc.ast.Exit;
import org.chocosolver.parser.flatzinc.ast.declaration.DArray;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.chocosolver.parser.flatzinc.ast.expression.EArray;
import org.chocosolver.parser.flatzinc.ast.expression.ESetBounds;
import org.chocosolver.parser.flatzinc.ast.expression.ESetList;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cprudhom on 18/06/15.
 * Project: choco-parsers.
 */
public abstract class ASolutionPrinter implements IMonitorSolution {

    boolean printAll;
    boolean printStat;
    boolean wrongSolution;
    int nbSolution;
    boolean userinterruption = true;
    public boolean immutable = false;

    final public Solution bestSolution = new Solution();


    List<String> output_names;
    List<Declaration.DType> output_types;
    List<Variable> output_vars;

    List<String> output_arrays_names;
    List<Declaration.DType> output_arrays_types;
    List<Variable[]> output_arrays_vars;

    StringBuilder stringBuilder = new StringBuilder();

    final Thread statOnKill;


    public ASolutionPrinter(boolean printAll, boolean printStat) {
        this.printAll = printAll;
        this.printStat = printStat;
        output_vars = new ArrayList<>();
        output_names = new ArrayList<>();
        output_types = new ArrayList<>();
        output_arrays_names = new ArrayList<>();
        output_arrays_vars = new ArrayList<>();
        output_arrays_types = new ArrayList<>();
        statOnKill = new Thread() {
            public void run() {
                if (userinterruption) {
                    doFinalOutPut();
                    System.out.printf("%% Unexpected resolution interruption!");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(statOnKill);
    }

    public void addOutputVar(String name, Variable variable, Declaration type) {
        if (!immutable) {
            output_names.add(name);
            output_vars.add(variable);
            output_types.add(type.typeOf);
        }
    }

    public void addOutputArrays(String name, Variable[] variables, List<Expression> indices, Declaration type) {
        if (!immutable) {
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

    public void printSolution(Solution solution) {
        for (int i = 0; i < output_names.size(); i++) {
            System.out.printf("%s = %s;\n", output_names.get(i), value(solution, output_vars.get(i), output_types.get(i)));

        }
        for (int i = 0; i < output_arrays_names.size(); i++) {
            String name = output_arrays_names.get(i);
            Variable[] ivars = output_arrays_vars.get(i);
            if (ivars.length > 0) {
                Declaration.DType type = output_arrays_types.get(i);
                stringBuilder.append(value(solution, ivars[0], type));
                for (int j = 1; j < ivars.length; j++) {
                    stringBuilder.append(", ").append(value(solution, ivars[j], type));
                }
                System.out.printf(name, stringBuilder.toString());
                stringBuilder.setLength(0);
            } else {
                System.out.printf(name);
            }
        }
        System.out.printf("----------\n");
    }

    protected String value(Solution solution, Variable var, Declaration.DType type) {
        switch (type) {
            case BOOL:
                return solution.getIntVal((BoolVar) var) == 1 ? "true" : "false";
            case INT:
            case INT2:
            case INTN:
                return Integer.toString(solution.getIntVal(((IntVar) var)));
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

    @Override
    public void onSolution() {
        wrongSolution = false;
        nbSolution++;
        if (printAll) {
            printSolution(bestSolution);
        }
    }

    public final void finalOutPut() {
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        doFinalOutPut();
    }

    public abstract void doFinalOutPut();

    public void immutable() {
        this.immutable = true;
    }
}
