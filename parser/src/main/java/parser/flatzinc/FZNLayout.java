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

package parser.flatzinc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.Exit;
import parser.flatzinc.ast.declaration.DArray;
import parser.flatzinc.ast.declaration.Declaration;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.ESetBounds;
import parser.flatzinc.ast.expression.ESetList;
import parser.flatzinc.ast.expression.Expression;
import solver.objective.NoObjectiveManager;
import solver.search.loop.AbstractSearchLoop;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public final class FZNLayout extends VoidSearchMonitor implements ISearchMonitor {

    protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

    List<String> output_names;
    List<Declaration.DType> output_types;
    List<IntVar> output_vars;

    List<String> output_arrays_names;
    List<Declaration.DType> output_arrays_types;
    List<IntVar[]> output_arrays_vars;

    StringBuilder stringBuilder = new StringBuilder();

    AbstractSearchLoop searchLoop;

    public FZNLayout() {
        super();
        output_vars = new ArrayList<IntVar>();
        output_names = new ArrayList<String>();
        output_types = new ArrayList<Declaration.DType>();
        output_arrays_names = new ArrayList<String>();
        output_arrays_vars = new ArrayList<IntVar[]>();
        output_arrays_types = new ArrayList<Declaration.DType>();
    }

    @Override
    public void onSolution() {
        if (LOGGER.isInfoEnabled()) {
            for (int i = 0; i < output_vars.size(); i++) {
                LOGGER.info("{} = {};", output_names.get(i), value(output_vars.get(i), output_types.get(i)));

            }
            for (int i = 0; i < output_arrays_vars.size(); i++) {
                String name = output_arrays_names.get(i);
                IntVar[] ivars = output_arrays_vars.get(i);
                Declaration.DType type = output_arrays_types.get(i);
                stringBuilder.append(value(ivars[0], type));
                for (int j = 1; j < ivars.length; j++) {
                    stringBuilder.append(", ").append(value(ivars[j], type));
                }
                LOGGER.info(name, stringBuilder.toString());
                stringBuilder.setLength(0);
            }
            LOGGER.info("----------");
        }
    }

    private String value(IntVar var, Declaration.DType type) {
        switch (type) {
            case BOOL:
                return var.getValue() == 1 ? "true" : "false";
            case INT1:
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
        if (LOGGER.isInfoEnabled()) {
            if (searchLoop.getMeasures().getSolutionCount() == 0) {
                if (searchLoop.getLimitsBox().isReached()) {
                    LOGGER.info("=====UNKNOWN=====");
                } else {
                    LOGGER.info("=====UNSATISFIABLE=====");
                }
            } else {
                if (searchLoop.getLimitsBox().isReached()
                        && !(searchLoop.getObjectivemanager() instanceof NoObjectiveManager)) {
                    LOGGER.info("=====UNBOUNDED=====");
                } else {
                    LOGGER.info("==========");
                }
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("% - Search statistics");
                LOGGER.info("% \t Solutions : {}", searchLoop.getMeasures().getSolutionCount());
                LOGGER.info("% \t Building time : {}ms", searchLoop.getMeasures().getReadingTimeCount());
                LOGGER.info("% \t Initial propagation : {}ms", searchLoop.getMeasures().getInitialPropagationTimeCount());
                LOGGER.info("% \t Resolution : {}ms", searchLoop.getMeasures().getTimeCount());
                LOGGER.info("% \t Nodes : {}", searchLoop.getMeasures().getNodeCount());
                LOGGER.info("% \t Backtracks : {}", searchLoop.getMeasures().getBackTrackCount());
                LOGGER.info("% \t Fails : {}", searchLoop.getMeasures().getFailCount());
                LOGGER.info("% \t Restarts : {}", searchLoop.getMeasures().getRestartCount());
                LOGGER.info("% \t Memory : {}", searchLoop.getMeasures().getUsedMemory());
                LOGGER.info("% \t Variables : {}", searchLoop.getSolver().getVars().length);
                LOGGER.info("% \t Constraints : {}", searchLoop.getSolver().getCstrs().length);
                LOGGER.info("% \t Checks : {} + {}", searchLoop.getMeasures().getEventsCount(),
                        searchLoop.getMeasures().getPropagationsCount());
            }
        }
    }

    public void addOutputVar(String name, IntVar variable, Declaration type) {
        output_names.add(name);
        output_vars.add(variable);
        output_types.add(type.typeOf);
    }

    public void addOutputArrays(String name, IntVar[] variables, List<Expression> indices, Declaration type) {
        EArray array = (EArray) indices.get(0);
        stringBuilder.append(name).append(" = array").append(array.what.size()).append("d(");

        build(stringBuilder, array.getWhat_i(0));
        for (int i = 1; i < array.what.size(); i++) {
            stringBuilder.append(',');
            build(stringBuilder, array.getWhat_i(i));
        }
        stringBuilder.append(",[{}]);");

        output_arrays_names.add(stringBuilder.toString());
        output_arrays_vars.add(variables.clone());
        output_arrays_types.add(((DArray)type).getWhat().typeOf);
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
                LOGGER.warn("output_array:: Unknow index {}", exp.getTypeOf());
                return new int[0];
        }
    }


    public void setSearchLoop(AbstractSearchLoop searchLoop) {
        searchLoop.plugSearchMonitor(this);
        this.searchLoop = searchLoop;
    }
}
