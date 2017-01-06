/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.searches;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMedian;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.ActivityBased;
import org.chocosolver.solver.search.strategy.selectors.variables.AntiFirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.Largest;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.search.strategy.selectors.variables.Occurrence;
import org.chocosolver.solver.search.strategy.selectors.variables.Smallest;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelectorWithTies;
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
                return new FirstFail(solver);
            case anti_first_fail:
                return new AntiFirstFail(solver);
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
        DecisionOperator<IntVar> assgnt = DecisionOperatorFactory.makeIntEq();
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
                assgnt = DecisionOperatorFactory.makeIntSplit();
                break;
            case indomain_reverse_split:
                valSelector = new IntDomainMiddle(!IntDomainMiddle.FLOOR);
                assgnt = DecisionOperatorFactory.makeIntReverseSplit();
                break;
            default:
                System.err.println("% No implementation for " + assignmennt.name() + ". Set default.");
                valSelector = new IntDomainMin();
        }
        return new IntStrategy(scope, variableSelector, valSelector, assgnt);
    }


}

