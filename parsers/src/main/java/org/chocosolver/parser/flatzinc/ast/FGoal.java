/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast;

import org.chocosolver.parser.ParserException;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.EArray;
import org.chocosolver.parser.flatzinc.ast.expression.EIdentifier;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.parser.flatzinc.ast.searches.Assignment;
import org.chocosolver.parser.flatzinc.ast.searches.IntSearch;
import org.chocosolver.parser.flatzinc.ast.searches.SetSearch;
import org.chocosolver.parser.flatzinc.ast.searches.VarChoice;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
 * User : CPRUDHOM
 * Mail : cprudhom(a)emn.fr
 * Date : 12 janv. 2010
 * Since : Choco 2.1.1
 *
 * Class for solve goals definition based on flatzinc-like objects.
 *
 * A solve goal is defined with:
 * </br> 'solve annotations satisfy;'
 * </br> or 'solve annotations maximize expression;'
 * /br> or 'solve annotations minimize expression;'
 */

@SuppressWarnings({"rawtypes", "unchecked"})
public class FGoal {

    private enum Search {
        seq_search,
        int_search,
        bool_search,
        set_search,
        warm_start_bool,
        warm_start_int,
        restart_luby,
        restart_geometric,
        restart_linear,
        restart_constant,
        restart_none
    }

    public static void define_goal(Model aModel, List<EAnnotation> annotations, ResolutionPolicy type, Expression expr) {
        // First define solving process
        if (type != ResolutionPolicy.SATISFACTION) {
            IntVar obj = expr.intVarValue(aModel);
            aModel.setObjective(type == ResolutionPolicy.MAXIMIZE, obj);
        }
        // Then define search goal
        StringBuilder description = new StringBuilder();
        // Always read the search strategies, if any
        AbstractStrategy[] strategies = new AbstractStrategy[annotations.size()];
        for (int a = 0; a < annotations.size(); a++) {
            EAnnotation annotation = annotations.get(a);
            if (annotation.id.value.equals("seq_search")) {
                EArray earray = (EArray) annotation.exps.get(0);

                AbstractStrategy[] substrategies = new AbstractStrategy[earray.what.size()];
                for (int i = 0; i < substrategies.length; i++) {
                    substrategies[i] = readSearchAnnotation((EAnnotation) earray.getWhat_i(i), aModel, description);
                }
                strategies[a] = new StrategiesSequencer(aModel.getEnvironment(),
                        Arrays.stream(substrategies)
                                .filter(Objects::nonNull)
                                .toArray(AbstractStrategy[]::new));
            } else {
                strategies[a] = readSearchAnnotation(annotation, aModel, description);
            }
        }
        strategies = Arrays.stream(strategies).filter(Objects::nonNull).toArray(AbstractStrategy[]::new);
        if(strategies.length > 0){
            aModel.getSolver().setSearch(strategies);
        }
    }

    /**
     * Read search annotation and build corresponding strategy
     *
     * @param e     {@link org.chocosolver.parser.flatzinc.ast.expression.EAnnotation}
     * @param model solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private static AbstractStrategy readSearchAnnotation(EAnnotation e, Model model, StringBuilder description) {
        Expression[] exps = new Expression[e.exps.size()];
        e.exps.toArray(exps);
        Search search;
        try {
            search = Search.valueOf(e.id.value);
        } catch (IllegalArgumentException ex) {
            model.getSolver().log().printf("%% ignored search annotation: %s\n", e);
            return null;
        }
        if (search == Search.seq_search) {
            EArray eArray = (EArray) e.exps.get(0);
            AbstractStrategy[] strats = new AbstractStrategy[eArray.what.size()];
            for (int i = 0; i < strats.length; i++) {
                strats[i] = readSearchAnnotation((EAnnotation) eArray.getWhat_i(i), model, description);
            }
            return org.chocosolver.solver.search.strategy.Search.sequencer(strats);
        }
        if (search == Search.warm_start_int || search == Search.warm_start_bool) {
            IntVar[] scope = exps[0].toIntVarArray(model); // deal with set var?
            int[] values = exps[1].toIntArray();
            for (int i = 0; i < scope.length; i++) {
                model.getSolver().addHint(scope[i], values[i]);
            }
            return null;
        }
        if (search.toString().startsWith("restart")) {
            switch (search) {
                case restart_luby:
                    int scale = exps[0].intValue();
                    model.getSolver().setLubyRestart(scale, new FailCounter(model, 0), Integer.MAX_VALUE);
                    break;
                case restart_geometric:
                    float base = exps[0].floatValue();
                    int scale0 = exps[1].intValue();
                    model.getSolver().setGeometricalRestart(scale0, base, new FailCounter(model, 0), Integer.MAX_VALUE);
                    break;
                case restart_linear:
                    int scale1 = exps[0].intValue();
                    model.getSolver().setLinearRestart(scale1, new FailCounter(model, 0), Integer.MAX_VALUE);
                    break;
                case restart_constant:
                    int scale2 = exps[0].intValue();
                    model.getSolver().setConstantRestart(scale2, new FailCounter(model, 0), Integer.MAX_VALUE);
                    break;
                case restart_none:
                    break;
            }
            return null;
        }

        VarChoice vchoice = VarChoice.valueOf(((EIdentifier) exps[1]).value);
        description.append(vchoice).append(";");
        Assignment assignment = Assignment.valueOf(((EIdentifier) exps[2]).value);

        switch (search) {
            case int_search:
            case bool_search: {
                IntVar[] scope = exps[0].toIntVarArray(model);
                return IntSearch.build(scope, vchoice, assignment, model);
            }
            case set_search: {
                SetVar[] scope = exps[0].toSetVarArray(model);
                return SetSearch.build(scope, vchoice, assignment, model);
            }
            default:
                System.err.println("Unknown search annotation " + e);
                throw new ParserException();
        }
    }
}
