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

package org.chocosolver.parser.flatzinc.ast.searches;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelectorWithTies;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

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

    public static AbstractStrategy build(IntVar[] variables, VarChoice varChoice, Assignment assignment, Model solver) {
        VariableSelector<IntVar> varsel = variableSelector(varChoice, solver);
        if (varsel == null) { // free search
            return new ActivityBased(solver, variables, 0.999d, 0.02d, 8, 1, seed);
        }
        return valueSelector(variables, varsel, assignment);
    }

    private static VariableSelector<IntVar> variableSelector(VarChoice varChoice, Model solver) {
        switch (varChoice) {
            case input_order:
                return new InputOrder<>(solver);
            case first_fail:
                return new FirstFail();
            case anti_first_fail:
                return new AntiFirstFail();
            case smallest:
                return new Smallest();
            case largest:
                return new Largest();
            case occurrence:
                return new Occurrence<>();
            case most_constrained:
                // It chooses the variable with the smallest value in its domain, breaking ties using the number of propagators
                return new VariableSelectorWithTies<>(new Smallest(), new Occurrence<>());
            case max_regret:
                return new MaxRegret();
            default:
                System.err.println("% No implementation for " + varChoice.name() + ". Set default.");
                return null;
        }
    }

    private static IntStrategy valueSelector(IntVar[] scope, VariableSelector<IntVar> variableSelector,
                                                                            Assignment assignmennt) {
        IntValueSelector valSelector;
        DecisionOperator<IntVar> assgnt = DecisionOperator.int_eq;
        switch (assignmennt) {
            case indomain:
            case indomain_min:
                valSelector = new IntDomainMin();
                break;
            case indomain_max:
                valSelector = new IntDomainMax();
                break;
            case indomain_middle:
                valSelector = new IntDomainMiddle(IntDomainMiddle.FLOOR);
                break;
            case indomain_median:
                valSelector = new IntDomainMedian();
                break;
            case indomain_random:
                valSelector = new IntDomainRandom(seed);
                break;
            case indomain_split:
            case indomain_interval:
                valSelector = new IntDomainMiddle(IntDomainMiddle.FLOOR);
                assgnt = DecisionOperator.int_split;
                break;
            case indomain_reverse_split:
                valSelector = new IntDomainMiddle(!IntDomainMiddle.FLOOR);
                assgnt = DecisionOperator.int_reverse_split;
                break;
            default:
                System.err.println("% No implementation for " + assignmennt.name() + ". Set default.");
                valSelector = new IntDomainMin();
        }
        return new IntStrategy(scope, variableSelector, valSelector, assgnt);
    }


}

