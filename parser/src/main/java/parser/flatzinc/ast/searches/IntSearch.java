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

package parser.flatzinc.ast.searches;

import choco.kernel.memory.IEnvironment;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.search.strategy.enumerations.sorters.Seq;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.enumerations.values.comparators.Distance;
import solver.search.strategy.enumerations.values.heuristics.nary.Join;
import solver.search.strategy.enumerations.values.heuristics.unary.DropN;
import solver.search.strategy.enumerations.values.metrics.Median;
import solver.search.strategy.enumerations.values.metrics.Metric;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class IntSearch {

    private IntSearch() {
    }

    public static AbstractStrategy build(IntVar[] variables, VarChoice varChoice, Assignment assignmennt, Strategy strategy, Solver solver) {
        valueIterator(variables, assignmennt);
        return variableSelector(variables, varChoice, solver);
    }

    private static StrategyVarValAssign variableSelector(IntVar[] variables, VarChoice varChoice, Solver solver) {
        IEnvironment environment = solver.getEnvironment();
        switch (varChoice) {
            case input_order:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.inputOrder(variables), ValidatorFactory.instanciated, environment);
            case first_fail:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.minDomain(), ValidatorFactory.instanciated, environment);
            case anti_first_fail:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.maxDomain(), ValidatorFactory.instanciated, environment);
            case smallest:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.smallest(), ValidatorFactory.instanciated, environment);
            case largest:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.largest(), ValidatorFactory.instanciated, environment);
            case occurrence:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.mostConstrained(), ValidatorFactory.instanciated, environment);
            case most_constrained:
                return StrategyVarValAssign.sta(variables,
                        new Seq<IntVar>(SorterFactory.smallest(), SorterFactory.mostConstrained()),
                        ValidatorFactory.instanciated, environment);
            case max_regret:
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.maxRegret(), ValidatorFactory.instanciated, environment);
            default:
                LoggerFactory.getLogger("fzn").error("% No implementation for " + varChoice.name() + ". Set default.");
                return StrategyVarValAssign.sta(variables,
                        SorterFactory.domOverWDeg(solver), ValidatorFactory.instanciated, environment);
        }
    }

    private static void valueIterator(IntVar[] variables, Assignment assignmennt) {
        switch (assignmennt) {
            case indomain:
            case indomain_min:
                for (int i = 0; i < variables.length; i++) {
                    variables[i].setHeuristicVal(HeuristicValFactory.enumVal(variables[i], variables[i].getLB(), 1, variables[i].getUB()));
                }
                break;
            case indomain_max:
                for (int i = 0; i < variables.length; i++) {
                    variables[i].setHeuristicVal(HeuristicValFactory.enumVal(variables[i], variables[i].getUB(), -1, variables[i].getLB()));
                }
                break;
            case indomain_middle:
                for (int i = 0; i < variables.length; i++) {
                    //TODO: EnumVal with Metric as paramater (bounds and delta)
                    Metric median = new Median(variables[i]);
                    variables[i].setHeuristicVal(
                            new Join(new Distance(median),
                                    HeuristicValFactory.enumVal(variables[i], median.getValue() + 1, 1, variables[i].getUB()),
                                    HeuristicValFactory.enumVal(variables[i], median.getValue(), -1, variables[i].getLB())
                            ));
                }
                break;
            case indomain_median:
                for (int i = 0; i < variables.length; i++) {
                    variables[i].setHeuristicVal(
                            new DropN(HeuristicValFactory.enumVal(variables[i]),
                                    new solver.search.strategy.enumerations.values.metrics.Middle(variables[i]))
                    );
                }
                break;
            case indomain_random:
                for (int i = 0; i < variables.length; i++) {
                    variables[i].setHeuristicVal(new solver.search.strategy.enumerations.values.heuristics.zeroary.Random(variables[i]));
                }
                break;
            case indomain_split:
            case indomain_reverse_split:
            case indomain_interval:
            default:
                LoggerFactory.getLogger("fzn").error("% No implementation for " + assignmennt.name() + ". Set default.");
                for (int i = 0; i < variables.length; i++) {
                    variables[i].setHeuristicVal(HeuristicValFactory.enumVal(variables[i]));
                }

        }
    }


}

