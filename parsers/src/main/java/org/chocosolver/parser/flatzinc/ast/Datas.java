/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.parser.Exit;
import org.chocosolver.parser.Level;
import org.chocosolver.parser.flatzinc.ast.declaration.DArray;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.chocosolver.parser.flatzinc.ast.expression.EArray;
import org.chocosolver.parser.flatzinc.ast.expression.ESetBounds;
import org.chocosolver.parser.flatzinc.ast.expression.ESetList;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.*;

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
    private final HashMap<String, Integer> cstrCounter;

    private Level level = Level.COMPET;
    private boolean oss = false;
    private int nbSolution;
    private final StringBuilder stringBuilder = new StringBuilder();

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
        cstrCounter = new HashMap<>();
    }

    public Datas(Model model, Level theLevel, boolean oss) {
        this();
        this.level = theLevel;
        this.model = model;
        this.oss = oss;
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
                st.append(esb);
                return esb.enumVal();
            case SET_L:
                ESetList esl = (ESetList) exp;
                st.append(esl);
                return esl.enumVal();
            default:
                return new int[0];
        }
    }

    private void printSolution() {
        for (int i = 0; i < output_names.size(); i++) {
            if (level.isLoggable(Level.COMPET)) {
                System.out.printf("%s = %s;\n", output_names.get(i), value(output_vars.get(i), output_types.get(i)));
            }
        }
        Solver solver = model.getSolver();
        for (int i = 0; i < output_arrays_names.size(); i++) {
            String name = output_arrays_names.get(i);
            Variable[] ivars = output_arrays_vars.get(i);
            if (level.isLoggable(Level.COMPET)) {
                if (ivars.length > 0) {
                    Declaration.DType type = output_arrays_types.get(i);
                    stringBuilder.append(value(ivars[0], type));
                    for (int j = 1; j < ivars.length; j++) {
                        stringBuilder.append(", ").append(value(ivars[j], type));
                    }
                    solver.log().printf(name, stringBuilder);
                    stringBuilder.setLength(0);
                } else {
                    solver.log().print(name);
                }
            }
        }
        if (level.isLoggable(Level.COMPET)) {
            solver.log().bold().print("----------\n");
        }
        if (level.isLoggable(Level.INFO)) {
            solver.log().white().printf("%s \n", solver.getMeasures().toOneLineString());
        }
        if (solver.getObjectiveManager().isOptimization()) {
            if (level.is(Level.RESANA)) {
                solver.log().printf(java.util.Locale.US, "o %d %.1f\n",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
            }
            if (level.is(Level.IRACE)) {
                solver.log().printf(Locale.US, "%d %.2f\n",
                        solver.getObjectiveManager().isOptimization() ?
                                (solver.getObjectiveManager().getPolicy().equals(ResolutionPolicy.MAXIMIZE) ? -1 : 1)
                                        * solver.getObjectiveManager().getBestSolutionValue().intValue() :
                                -solver.getSolutionCount(),
                        solver.getTimeCount());
            }
            if (level.is(Level.JSON)) {
                solver.log().printf(Locale.US, "%s{\"bound\":%d,\"time\":%.1f}",
                        solver.getSolutionCount() > 1 ? "," : "",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
            }
        } else {
            if (level.is(Level.JSON)) {
                solver.log().printf("{\"time\":%.1f},",
                        solver.getTimeCount());
            }
        }
    }

    public void onSolution() {
        nbSolution++;
        if (solution == null) {
            solution = new Solution(model, allOutPutVars());
        }
        solution.record();
        printSolution();
    }

    public Variable[] allOutPutVars() {
        ArrayList<Variable> vars = new ArrayList<>(output_vars);
        for (Variable[] vs : output_arrays_vars) {
            Collections.addAll(vars, vs);
        }
        return vars.toArray(new Variable[0]);
    }

    public void doFinalOutPut(boolean complete) {
        Solver solver = model.getSolver();
        // TODO there used to be "isComplete" (e.g. in case LNS stops)
//        boolean complete = solver.getSearchState() == SearchState.TERMINATED;
        if (nbSolution > 0) {
            if (complete && solver.getObjectiveManager().isOptimization()) {
                if (level.isLoggable(Level.COMPET)) {
                    solver.log().bold().green().print("==========\n");
                }
            }
        } else {
            if (complete) {
                if (level.isLoggable(Level.COMPET)) {
                    solver.log().bold().red().print("=====UNSATISFIABLE=====\n");
                }
            } else {
                if (level.isLoggable(Level.COMPET)) {
                    solver.log().bold().black().print("=====UNKNOWN=====\n");
                }
            }
        }
        if (level.is(Level.RESANA)) {
            solver.log().printf(java.util.Locale.US, "s %s %.1f\n",
                    complete ? "T" : "S",
                    solver.getTimeCount());
        }
        if (level.is(Level.JSON)) {
            solver.log().printf(Locale.US, "],\"exit\":{\"time\":%.1f,\"status\":\"%s\"}}",
                    solver.getTimeCount(), complete ? "terminated" : "stopped");
        }
        if (level.is(Level.IRACE)) {
            /*long obj = solver.getObjectiveManager().isOptimization() ?
                    (solver.getObjectiveManager().getPolicy().equals(ResolutionPolicy.MAXIMIZE) ? -1 : 1)
                            * solver.getObjectiveManager().getBestSolutionValue().intValue() :
                    -solver.getSolutionCount();
            long tim = complete ?
                    (int) Math.ceil(solver.getTimeCount()) :
                    999_999; // arbitrary value
            double value = obj + 1e-6*tim;
            solver.log().printf(Locale.US, "%.6f",value);*/
            solver.log().printf(Locale.US, "%d %.2f\n",
                    solver.getObjectiveManager().isOptimization() ?
                            (solver.getObjectiveManager().getPolicy().equals(ResolutionPolicy.MAXIMIZE) ? -1 : 1)
                                    * solver.getObjectiveManager().getBestSolutionValue().intValue() :
                            -solver.getSolutionCount(),
                    complete ?
                            solver.getTimeCount() :
                            86_399.99); // 24h
        }
        if (level.isLoggable(Level.INFO)) {
            solver.log().bold().white().printf("%s \n", solver.getMeasures().toOneLineString());
        }
        if (oss) {
            solver.log().printf(Locale.US, "%%%%%%mzn-stat: initTime=%.3f%n", solver.getReadingTimeCount());
            solver.log().printf(Locale.US, "%%%%%%mzn-stat: solveTime=%.3f%n", solver.getTimeCount());
            solver.log().printf("%%%%%%mzn-stat: solutions=%d%n", solver.getSolutionCount());
            solver.log().printf("%%%%%%mzn-stat: variables=%d%n", solver.getModel().getNbVars());
            solver.log().printf("%%%%%%mzn-stat: constraints=%d%n", solver.getModel().getNbCstrs());
            solver.log().printf("%%%%%%mzn-stat: nodes=%d%n", solver.getNodeCount());
            solver.log().printf("%%%%%%mzn-stat: failures=%d%n", solver.getFailCount());
            solver.log().printf("%%%%%%mzn-stat: restarts=%d%n", solver.getRestartCount());
            solver.log().println("%%%mzn-stat-end");
        }
    }

    public void incCstrCounter(String name) {
        if (level.isLoggable(Level.INFO)) {
            this.cstrCounter.compute(name, (s, c) -> c == null ? 1 : c + 1);
        }
    }

    public Map<String, Integer> cstrCounter() {
        return this.cstrCounter;
    }
}
