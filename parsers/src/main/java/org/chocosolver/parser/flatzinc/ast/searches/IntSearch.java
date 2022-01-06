/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.searches;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
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

    private IntSearch() {
    }

    public static AbstractStrategy<IntVar> build(IntVar[] variables, VarChoice varChoice, Assignment assignment, Model model) {
        VariableSelector<IntVar> varsel = variableSelector(variables, varChoice, model);
        if (varsel == null) { // free search
            model.getSolver().setNoGoodRecordingFromRestarts();
            model.getSolver().setLubyRestart(500, new FailCounter(model, 0), 500);
            return Search.intVarSearch(variables);
        }
        return valueSelector(variables, varsel, assignment);
    }

    private static VariableSelector<IntVar> variableSelector(IntVar[] variables, VarChoice varChoice, Model model) {
        switch (varChoice) {
            case input_order:
                return new InputOrder<>(model);
            case first_fail:
                return new FirstFail(model);
            case anti_first_fail:
                return new AntiFirstFail(model);
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
            case dom_w_deg:
                return new DomOverWDeg(variables, variables[0].getModel().getSeed());
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
                valSelector = new IntDomainRandom(scope[0].getModel().getSeed());
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

