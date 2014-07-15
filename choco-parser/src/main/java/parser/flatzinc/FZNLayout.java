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

package parser.flatzinc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.Exit;
import parser.flatzinc.ast.GoalConf;
import parser.flatzinc.ast.declaration.DArray;
import parser.flatzinc.ast.declaration.Declaration;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.ESetBounds;
import parser.flatzinc.ast.expression.ESetList;
import parser.flatzinc.ast.expression.Expression;
import solver.Solver;
import solver.search.loop.monitors.IMonitorClose;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.solution.LastSolutionRecorder;
import solver.search.solution.Solution;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class FZNLayout implements IMonitorSolution, IMonitorClose {

    protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

    List<String> output_names;
    List<Declaration.DType> output_types;
    List<IntVar> output_vars;

    List<String> output_arrays_names;
    List<Declaration.DType> output_arrays_types;
    List<IntVar[]> output_arrays_vars;

    StringBuilder stringBuilder = new StringBuilder();

    Solver solver;

    boolean wrongSolution;
    int nbSolution;
    boolean userinterruption = true;


    protected final String instance;
    protected GoalConf gc;
    private Thread statOnKill;

    public FZNLayout(final String instance, GoalConf gc) {
        super();
        output_vars = new ArrayList<>();
        output_names = new ArrayList<>();
        output_types = new ArrayList<>();
        output_arrays_names = new ArrayList<>();
        output_arrays_vars = new ArrayList<>();
        output_arrays_types = new ArrayList<>();

        this.instance = instance;
        this.gc = gc;
    }

    public FZNLayout() {
        this("", null);
    }

    @Override
    public void onSolution() {
        if (check()) {
            wrongSolution = false;
            nbSolution++;
            if (LOGGER.isInfoEnabled() && gc.all) {
                printSolution();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("% " + solver.getMeasures().toOneShortLineString());
                }
            }
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("%\n% /!\\ ERROR >>>>>>>   Find a solution that does not seem to be correct!!  <<<<<<<<\n%");
            }
            System.exit(-200);
        }
    }

    private void printSolution() {
        for (int i = 0; i < output_names.size(); i++) {
            LOGGER.info("{} = {};", output_names.get(i), value(output_vars.get(i), output_types.get(i)));

        }
        for (int i = 0; i < output_arrays_names.size(); i++) {
            String name = output_arrays_names.get(i);
            IntVar[] ivars = output_arrays_vars.get(i);
            if (ivars.length > 0) {
                Declaration.DType type = output_arrays_types.get(i);
                stringBuilder.append(value(ivars[0], type));
                for (int j = 1; j < ivars.length; j++) {
                    stringBuilder.append(", ").append(value(ivars[j], type));
                }
                LOGGER.info(name, stringBuilder.toString());
                stringBuilder.setLength(0);
            } else {
                LOGGER.info(name);
            }
        }
        LOGGER.info("----------");
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

    private String value(IntVar var, Declaration.DType type) {
        switch (type) {
            case BOOL:
                return var.getValue() == 1 ? "true" : "false";
            case INT:
            case INT2:
            case INTN:
                return Integer.toString(var.getValue());
            default:
                Exit.log();
        }
        return "";
    }

    @Override
    public void beforeClose() {
        finalOutPut();
        Runtime.getRuntime().removeShutdownHook(statOnKill);
    }

    public void finalOutPut() {
        if (LOGGER.isInfoEnabled()) {
            if (solver.getMeasures().getSolutionCount() == 0) {
                if ((wrongSolution && nbSolution == 0) || solver.hasReachedLimit()) {
                    LOGGER.info("=====UNKNOWN=====");
                } else {
                    LOGGER.info("=====UNSATISFIABLE=====");
                }
            } else {
                if (!gc.all) { // print the first/best solution when -a is not enabled
                    printSolution();
                }

                if (solver.hasReachedLimit()
                        && (solver.getObjectiveManager().isOptimization())) {
                    LOGGER.info("=====UNBOUNDED=====");
                } else {
                    LOGGER.info("==========");
                }
            }
            if (!LOGGER.isDebugEnabled()) {
                LOGGER.info("% " + solver.getMeasures().toOneShortLineString());
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("% - Search statistics");
            LOGGER.debug("% \t Solutions : {}", solver.getMeasures().getSolutionCount());
            if (solver.getMeasures().hasObjective()) {
                LOGGER.debug("% \t Objective : {}", solver.getMeasures().getBestSolutionValue().intValue());
            }
            LOGGER.debug("% \t Building time : {}ms", solver.getMeasures().getReadingTimeCount());
            LOGGER.debug("% \t Initial propagation : {}ms", solver.getMeasures().getInitialPropagationTimeCount());
            LOGGER.debug("% \t Resolution : {}ms", solver.getMeasures().getTimeCount());
            LOGGER.debug("% \t Nodes : {}", solver.getMeasures().getNodeCount());
            LOGGER.debug("% \t Backtracks : {}", solver.getMeasures().getBackTrackCount());
            LOGGER.debug("% \t Fails : {}", solver.getMeasures().getFailCount());
            LOGGER.debug("% \t Restarts : {}", solver.getMeasures().getRestartCount());
            LOGGER.debug("% \t Max Depth : {}", solver.getMeasures().getMaxDepth());
            LOGGER.debug("% \t Memory : {}", solver.getMeasures().getUsedMemory());
            LOGGER.debug("% \t Variables : {}", solver.getVars().length);
            LOGGER.debug("% \t Constraints : {}", solver.getCstrs().length);
            LOGGER.debug("% \t Checks : {} + {}", solver.getMeasures().getEventsCount(),
                    solver.getMeasures().getPropagationsCount());
        }
        userinterruption = false;
    }

    public boolean isUserinterruption() {
        return userinterruption;
    }

    @Override
    public void afterClose() {
    }

    public void addOutputVar(String name, Variable variable, Declaration type) {
        Exit.log("Cannot output " + name);
    }

    public void addOutputVar(String name, IntVar variable, Declaration type) {
        output_names.add(name);
        output_vars.add(variable);
        output_types.add(type.typeOf);
    }

    public void addOutputArrays(String name, Variable[] variables, List<Expression> indices, Declaration type) {
        Exit.log("Cannot output " + name);
    }

    public void addOutputArrays(String name, IntVar[] variables, List<Expression> indices, Declaration type) {
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
            stringBuilder.append(",[{}]);");
        } else {
            stringBuilder.append(",[]);");
        }

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
                    LOGGER.warn("output_array:: Unknow index {}", exp.getTypeOf());
                }
                return new int[0];
        }
    }

    public void set(Solver solver) {
        solver.plugMonitor(this);
        this.solver = solver;
        if (!gc.all) {
            solver.set(new LastSolutionRecorder(new Solution(), true, solver));
        }
    }

    public void makeup() {
        statOnKill = new Thread() {
            public void run() {
                if (isUserinterruption()) {
                    finalOutPut();
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("% Unexpected resolution interruption!");
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(statOnKill);
    }
}
