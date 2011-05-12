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

package parser.flatzinc.ast;

import org.slf4j.LoggerFactory;
import parser.flatzinc.FZNException;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.EIdentifier;
import parser.flatzinc.ast.expression.Expression;
import parser.flatzinc.ast.searches.Assignment;
import parser.flatzinc.ast.searches.IntSearch;
import parser.flatzinc.ast.searches.Strategy;
import parser.flatzinc.ast.searches.VarChoice;
import parser.flatzinc.parser.FZNParser;
import solver.Solver;
import solver.objective.MaxObjectiveManager;
import solver.objective.MinObjectiveManager;
import solver.search.loop.AbstractSearchLoop;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.Variable;

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

public class SolveGoal {

    final List<EAnnotation> annotations;
    final Resolution type;
    final Expression expr;

    public enum Resolution {
        SATISFY, MINIMIZE, MAXIMIZE
    }

    private enum Search {
        int_search,
        bool_search,
        set_search
    }


    public SolveGoal(FZNParser parser, List<EAnnotation> annotations, Resolution type, Expression expr) {
        this.annotations = annotations;
        this.type = type;
        this.expr = expr;
        defineGoal(parser.solver);
    }

    private void defineGoal(Solver solver) {
        AbstractStrategy strategy = null;
        if (annotations.size() > 0) {
            if (annotations.size() > 1) {
                throw new UnsupportedOperationException("SolveGoal:: wrong annotations size");
            } else {
                EAnnotation annotation = annotations.get(0);
                if (annotation.id.value.equals("seq_search")) {
                    EArray earray = (EArray) annotation.exps.get(0);

                    AbstractStrategy[] strategies = new AbstractStrategy[earray.what.size()];
                    for (int i = 0; i < strategies.length; i++) {
                        strategies[i] = readSearchAnnotation((EAnnotation) earray.getWhat_i(i), solver);
                    }
                    strategy = new StrategiesSequencer(solver.getEnvironment(), strategies);
                } else {
                    strategy = readSearchAnnotation(annotation, solver);
                }
            }
        } else {
            LoggerFactory.getLogger(SolveGoal.class).warn("% No search annotation. Set default.");
            Variable[] vars = solver.getVars();
            IntVar[] ivars = new IntVar[vars.length];
            for (int i = 0; i < ivars.length; i++) {
                ivars[i] = (IntVar) vars[i];
            }
            strategy = IntSearch.build(ivars,
                    VarChoice.input_order, Assignment.indomain_min, Strategy.complete, solver);
        }

        solver.set(strategy);

        AbstractSearchLoop search = solver.getSearchLoop();
        switch (type) {
            case SATISFY:
                search.stopAtFirstSolution(true);
                break;
            case MAXIMIZE:
                IntVar max = expr.intVarValue(solver);
                MaxObjectiveManager maom = new MaxObjectiveManager(max);
                maom.setMeasures(solver.getMeasures());
                search.setObjectivemanager(maom);
//                solver.setRestart(true);
                search.stopAtFirstSolution(false);
                break;
            case MINIMIZE:
                IntVar min = expr.intVarValue(solver);
                MinObjectiveManager miom = new MinObjectiveManager(min);
                miom.setMeasures(solver.getMeasures());
                search.setObjectivemanager(miom);
//                solver.setRestart(true);
                search.stopAtFirstSolution(false);
                break;
        }
    }

    /**
     * Read search annotation and build corresponding strategy
     *
     * @param e      {@link parser.flatzinc.ast.expression.EAnnotation}
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private AbstractStrategy readSearchAnnotation(EAnnotation e, Solver solver) {
        Expression[] exps = new Expression[e.exps.size()];
        e.exps.toArray(exps);
        Search search = Search.valueOf(e.id.value);
        VarChoice vchoice = VarChoice.valueOf(((EIdentifier) exps[1]).value);
        parser.flatzinc.ast.searches.Assignment assignment = parser.flatzinc.ast.searches.Assignment.valueOf(((EIdentifier) exps[2]).value);

        switch (search) {
            case int_search:
            case bool_search:
                IntVar[] scope = exps[0].toIntVarArray(solver);
                return IntSearch.build(scope, vchoice, assignment, Strategy.complete, solver);
            case set_search:
            default:
                LoggerFactory.getLogger(SolveGoal.class).error("Unknown search annotation " + e.toString());
                throw new FZNException();
        }
    }
}
