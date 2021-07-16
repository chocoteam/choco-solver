/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.List;

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

public class FGoal {

    private enum Search {
        seq_search,
        int_search,
        bool_search,
        set_search
    }

    public static void define_goal(Model aModel, List<EAnnotation> annotations, ResolutionPolicy type, Expression expr) {
        // First define solving process
        if (type != ResolutionPolicy.SATISFACTION) {
            IntVar obj = expr.intVarValue(aModel);
            aModel.setObjective(type == ResolutionPolicy.MAXIMIZE, obj);
        }
        // Then define search goal
        Variable[] vars = aModel. getVars();
        IntVar[] ivars = new IntVar[vars.length];
        for (int i = 0; i < ivars.length; i++) {
            ivars[i] = (IntVar) vars[i];
        }

        StringBuilder description = new StringBuilder();
        // Always read the search strategies, if any
        if (annotations.size() > 0) {
            AbstractStrategy strategy;
            if (annotations.size() > 1) {
                throw new UnsupportedOperationException("SolveGoal:: wrong annotations size");
            } else {
                EAnnotation annotation = annotations.get(0);
                if (annotation.id.value.equals("seq_search")) {
                    EArray earray = (EArray) annotation.exps.get(0);

                    AbstractStrategy[] strategies = new AbstractStrategy[earray.what.size()];
                    for (int i = 0; i < strategies.length; i++) {
                        strategies[i] = readSearchAnnotation((EAnnotation) earray.getWhat_i(i), aModel, description);
                    }
                    strategy = new StrategiesSequencer(aModel.getEnvironment(), strategies);
                } else {
                    strategy = readSearchAnnotation(annotation, aModel, description);
                }
                aModel.getSolver().setSearch(strategy);
            }
        }
    }

    /**
     * Read search annotation and build corresponding strategy
     *
     * @param e      {@link org.chocosolver.parser.flatzinc.ast.expression.EAnnotation}
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private static AbstractStrategy readSearchAnnotation(EAnnotation e, Model solver, StringBuilder description) {
        Expression[] exps = new Expression[e.exps.size()];
        e.exps.toArray(exps);
        Search search = Search.valueOf(e.id.value);
        if(search == Search.seq_search){
            EArray eArray = (EArray)e.exps.get(0);
            AbstractStrategy[] strats = new AbstractStrategy[eArray.what.size()];
            for(int i = 0; i < strats.length; i++){
                strats[i] = readSearchAnnotation((EAnnotation)eArray.getWhat_i(i), solver, description);
            }
            return org.chocosolver.solver.search.strategy.Search.sequencer(strats);
        }
        VarChoice vchoice = VarChoice.valueOf(((EIdentifier) exps[1]).value);
        description.append(vchoice.toString()).append(";");
        Assignment assignment = Assignment.valueOf(((EIdentifier) exps[2]).value);

        switch (search) {
            case int_search:
            case bool_search: {
                IntVar[] scope = exps[0].toIntVarArray(solver);
                return IntSearch.build(scope, vchoice, assignment, solver);
            }
            case set_search: {
                SetVar[] scope = exps[0].toSetVarArray(solver);
                return SetSearch.build(scope, vchoice, assignment, solver);
            }
            default:
                System.err.println("Unknown search annotation " + e.toString());
                throw new ParserException();
        }
    }
}
