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
import solver.search.strategy.enumerations.sorters.ActivityBased;
import solver.search.strategy.selectors.InValueIterator;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.values.*;
import solver.search.strategy.selectors.variables.*;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class IntSearch {

    private static long seed = 29091981;

    private IntSearch() {
    }

    public static AbstractStrategy build(IntVar[] variables, VarChoice varChoice, Assignment assignment, Strategy strategy, Solver solver) {
        VariableSelector<IntVar> varsel = variableSelector(variables, varChoice, solver);
        if (varsel == null) { // free search
            return new ActivityBased(solver, variables, 0.999d, 0.02d, 8, 2.0d, 1, seed);
        }
        return valueIterator(variables, varsel, assignment);
    }

    private static VariableSelector<IntVar> variableSelector(IntVar[] variables, VarChoice varChoice, Solver solver) {
        IEnvironment environment = solver.getEnvironment();
        switch (varChoice) {
            case input_order:
                return new InputOrder(variables, environment);
            case first_fail:
                return new FirstFail(variables);
            case anti_first_fail:
                return new AntiFirstFail(variables);
            case smallest:
                return new Smallest(variables);
            case largest:
                return new Largest(variables);
            case occurrence:
                return new Occurrence(variables);
            case most_constrained:
                return new MostConstrained(variables);
            case max_regret:
                return new MaxRegret(variables);
            default:
                LoggerFactory.getLogger("fzn").error("% No implementation for " + varChoice.name() + ". Set default.");
                return null;
        }
    }

    private static solver.search.strategy.strategy.Assignment valueIterator(IntVar[] variables, VariableSelector<IntVar> variableSelector,
                                                                            Assignment assignmennt) {
        InValueIterator valSelector = null;
        solver.search.strategy.assignments.Assignment assgnt = solver.search.strategy.assignments.Assignment.int_eq;
        switch (assignmennt) {
            case indomain:
            case indomain_min:
                valSelector = new InDomainMin();
                break;
            case indomain_max:
                valSelector = new InDomainMax();
                break;
            case indomain_middle:
                valSelector = new InDomainMiddle();
                break;
            case indomain_median:
                valSelector = new InDomainMedian();
                break;
            case indomain_random:
                valSelector = new InDomainRandom(seed);
                break;
            case indomain_split:
            case indomain_interval:
                valSelector = new InDomainMiddle();
                assgnt = solver.search.strategy.assignments.Assignment.int_split;
                break;
            case indomain_reverse_split:
                valSelector = new InDomainMiddle();
                assgnt = solver.search.strategy.assignments.Assignment.int_reverse_split;
                break;
            default:
                LoggerFactory.getLogger("fzn").error("% No implementation for " + assignmennt.name() + ". Set default.");
                valSelector = new InDomainMin();
        }
        return new solver.search.strategy.strategy.Assignment(variables, variableSelector, valSelector, assgnt);
    }


}

