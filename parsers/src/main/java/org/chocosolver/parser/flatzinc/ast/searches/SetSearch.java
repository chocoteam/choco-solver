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
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.SetValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.Occurrence;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.SetStrategy;
import org.chocosolver.solver.variables.SetVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class SetSearch {

    private SetSearch() {
    }

    public static AbstractStrategy<SetVar> build(SetVar[] variables, VarChoice varChoice, Assignment assignment, Model solver) {
        VariableSelector<SetVar> varsel = variableSelector(varChoice, solver);
        return valueSelector(variables, varsel, assignment);
    }

    private static VariableSelector<SetVar> variableSelector(VarChoice varChoice, Model solver) {
        switch (varChoice) {
            case input_order:
                return new InputOrder<>(solver);
            case occurrence:
                return new Occurrence<>();
            case first_fail:
            case anti_first_fail:
            case smallest:
            case largest:
            case most_constrained:
            case max_regret:
            case dom_w_deg:
            default:
                System.err.println("% No implementation for " + varChoice.name() + ". Set default.");
                return new InputOrder<>(solver);
        }
    }

    private static SetStrategy valueSelector(SetVar[] scope, VariableSelector<SetVar> variableSelector,
                                             Assignment assignment) {
        SetValueSelector valSelector;
        switch (assignment) {
            case indomain:
            case indomain_min:
                valSelector = new SetDomainMin();
                break;
            case indomain_max:
            case indomain_middle:
            case indomain_median:
            case indomain_random:
            case indomain_split:
            case indomain_interval:
            case indomain_reverse_split:
            default:
                System.err.println("% No implementation for " + assignment.name() + ". Set default.");
                valSelector = new SetDomainMin();
        }
        return new SetStrategy(scope, variableSelector, valSelector, true);
    }


}

