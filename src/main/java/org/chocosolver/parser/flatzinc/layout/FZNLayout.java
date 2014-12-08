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

package org.chocosolver.parser.flatzinc.layout;

import org.chocosolver.parser.flatzinc.ast.Exit;
import org.chocosolver.parser.flatzinc.ast.declaration.DArray;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.chocosolver.parser.flatzinc.ast.expression.EArray;
import org.chocosolver.parser.flatzinc.ast.expression.ESetBounds;
import org.chocosolver.parser.flatzinc.ast.expression.ESetList;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.solution.LastSolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class FZNLayout implements IMonitorSolution, IMonitorClose {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FZNLayout.class);

    List<String> output_names;
    List<Declaration.DType> output_types;
    List<Variable> output_vars;

    List<String> output_arrays_names;
    List<Declaration.DType> output_arrays_types;
    List<Variable[]> output_arrays_vars;

    StringBuilder stringBuilder = new StringBuilder();

    Solver solver;

    boolean wrongSolution;
    int nbSolution;
    boolean userinterruption = true;
    boolean printAll;


    protected final String instance;
    private Thread statOnKill;

    public FZNLayout(final String instance, boolean printAll) {
        super();
        this.printAll = printAll;
        output_vars = new ArrayList<>();
        output_names = new ArrayList<>();
        output_types = new ArrayList<>();
        output_arrays_names = new ArrayList<>();
        output_arrays_vars = new ArrayList<>();
        output_arrays_types = new ArrayList<>();

        this.instance = instance;
    }

    @Override
    public void onSolution() {
        if (check()) {
            wrongSolution = false;
            nbSolution++;
            if (printAll) {
                printSolution();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("% " + solver.getMeasures().toOneShortLineString());
                }
            }
        } else {
            System.err.println("%\n% /!\\ ERROR >>>>>>>   Find a solution that does not seem to be correct!!  <<<<<<<<\n%");
            System.exit(-200);
        }
    }

    private void printSolution() {
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

    private boolean check() {
        /*Constraint[] cstrs = searchLoop.getSolver().getCstrs();
        for (int c = 0; c < cstrs.length; c++) {
            ESat satC = cstrs[c].isSatisfied();
            if (!ESat.TRUE.equals(satC)) {
                return false;
            }
        }*/
        return true;
    }

    private String value(Variable var, Declaration.DType type) {
        switch (type) {
            case BOOL:
                return ((BoolVar) var).getValue() == 1 ? "true" : "false";
            case INT:
            case INT2:
            case INTN:
                return Integer.toString(((IntVar) var).getValue());
            case SET:
                StringBuilder st = new StringBuilder();
                st.append('{');
                for (int i : ((SetVar) var).getValues()) {
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
    public void beforeClose() {
        finalOutPut();
        if (statOnKill != null) Runtime.getRuntime().removeShutdownHook(statOnKill);
    }

    public void finalOutPut() {
        userinterruption = false;
        if (solver.getMeasures().getSolutionCount() == 0) {
            if ((wrongSolution && nbSolution == 0) || solver.hasReachedLimit()) {
                System.out.printf("=====UNKNOWN=====\n");
            } else {
                System.out.printf("=====UNSATISFIABLE=====\n");
            }
        } else {
            if (!printAll) { // print the first/best solution when -a is not enabled
                try {
                    solver.getSolutionRecorder().getLastSolution().restore();
                } catch (ContradictionException e) {
                    e.printStackTrace();
                }
                printSolution();
            }

            if (solver.hasReachedLimit()
                    && (solver.getObjectiveManager().isOptimization())) {
                System.out.printf("=====UNBOUNDED=====\n");
            } else {
                System.out.printf("==========\n");
            }
        }
        if (!LOGGER.isDebugEnabled()) {
            System.out.printf("%% " + solver.getMeasures().toOneShortLineString());
        } else {
            LOGGER.debug("%% - Search statistics");
            LOGGER.debug("%% \t Solutions : {}", solver.getMeasures().getSolutionCount());
            if (solver.getMeasures().hasObjective()) {
                LOGGER.debug("% \t Objective : {}", solver.getMeasures().getBestSolutionValue().intValue());
            }
            LOGGER.debug("%% \t Building time : {}ms", solver.getMeasures().getReadingTimeCount());
            LOGGER.debug("%% \t Initial propagation : {}ms", solver.getMeasures().getInitialPropagationTimeCount());
            LOGGER.debug("%% \t Resolution : {}ms", solver.getMeasures().getTimeCount());
            LOGGER.debug("%% \t Nodes : {}", solver.getMeasures().getNodeCount());
            LOGGER.debug("%% \t Backtracks : {}", solver.getMeasures().getBackTrackCount());
            LOGGER.debug("%% \t Fails : {}", solver.getMeasures().getFailCount());
            LOGGER.debug("%% \t Restarts : {}", solver.getMeasures().getRestartCount());
            LOGGER.debug("%% \t Max Depth : {}", solver.getMeasures().getMaxDepth());
            LOGGER.debug("%% \t Memory : {}", solver.getMeasures().getUsedMemory());
            LOGGER.debug("%% \t Variables : {}", solver.getVars().length);
            LOGGER.debug("%% \t Constraints : {}", solver.getCstrs().length);
            LOGGER.debug("%% \t Checks : {} + {}", solver.getMeasures().getEventsCount(),
                    solver.getMeasures().getPropagationsCount());
        }
    }

    public boolean isUserinterruption() {
        return userinterruption;
    }

    @Override
    public void afterClose() {
    }

    public void addOutputVar(String name, Variable variable, Declaration type) {
        output_names.add(name);
        output_vars.add(variable);
        output_types.add(type.typeOf);
    }

    public void addOutputArrays(String name, Variable[] variables, List<Expression> indices, Declaration type) {
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
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("output_array:: Unknown index {}", exp.getTypeOf());
                }
                return new int[0];
        }
    }

    public void set(Solver solver) {
        solver.plugMonitor(this);
        this.solver = solver;
        if (!printAll) {
            solver.set(new LastSolutionRecorder(new Solution(), true, solver));
        }
        makeup();
    }

    public void makeup() {
        statOnKill = new Thread() {
            public void run() {
                if (isUserinterruption()) {
                    finalOutPut();
                    System.out.printf("%% Unexpected resolution interruption!");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(statOnKill);
    }
}
