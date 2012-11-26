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

package parser.flatzinc.ast;

import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.tools.ArrayUtils;
import org.slf4j.LoggerFactory;
import parser.flatzinc.FZNException;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.EIdentifier;
import parser.flatzinc.ast.expression.Expression;
import parser.flatzinc.ast.searches.IntSearch;
import parser.flatzinc.ast.searches.Strategy;
import parser.flatzinc.ast.searches.VarChoice;
import solver.Solver;
import solver.objective.ObjectiveManager;
import solver.search.loop.AbstractSearchLoop;
import solver.search.loop.monitors.ABSLNS;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.enumerations.sorters.ActivityBased;
import solver.search.strategy.enumerations.sorters.ImpactBased;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.DomOverWDegVS;
import solver.search.strategy.selectors.variables.FirstFail;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.Assignment;
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

public class FGoal {

    private enum Search {
        int_search,
        bool_search,
        set_search
    }


    public static void define_goal(GoalConf gc, Solver aSolver, List<EAnnotation> annotations,
                                   ResolutionPolicy type, Expression expr) {
        // First define solving process
        AbstractSearchLoop search = aSolver.getSearchLoop();
        switch (type) {
            case SATISFACTION:
                search.stopAtFirstSolution(!gc.all);
                break;
            default:
                IntVar obj = expr.intVarValue(aSolver);
                search.setObjectivemanager(new ObjectiveManager(obj, type, aSolver));//                solver.setRestart(true);
                search.stopAtFirstSolution(false);
        }

        // Then define search goal
        Variable[] vars = aSolver.getVars();
        IntVar[] ivars = new IntVar[vars.length];
        for (int i = 0; i < ivars.length; i++) {
            ivars[i] = (IntVar) vars[i];
        }
        if (annotations.size() > 0 && !gc.free) {
            AbstractStrategy strategy = null;
            if (annotations.size() > 1) {
                throw new UnsupportedOperationException("SolveGoal:: wrong annotations size");
            } else {
                EAnnotation annotation = annotations.get(0);
                if (annotation.id.value.equals("seq_search")) {
                    EArray earray = (EArray) annotation.exps.get(0);

                    AbstractStrategy[] strategies = new AbstractStrategy[earray.what.size()];
                    for (int i = 0; i < strategies.length; i++) {
                        strategies[i] = readSearchAnnotation((EAnnotation) earray.getWhat_i(i), aSolver);
                    }
                    strategy = new StrategiesSequencer(aSolver.getEnvironment(), strategies);
                } else {
                    strategy = readSearchAnnotation(annotation, aSolver);
                }
//                solver.set(strategy);
                LoggerFactory.getLogger(FGoal.class).warn("% Fix seed");
                aSolver.set(
                        new StrategiesSequencer(aSolver.getEnvironment(),
                                strategy,
                                StrategyFactory.random(ivars, aSolver.getEnvironment(), gc.seed))
                );

                System.out.println("% t:" + gc.seed);
            }
        } else { // no strategy OR use free search
            if (gc.dec_vars) { // select same decision variables as declared in file?
                Variable[] dvars = new Variable[0];
                if (annotations.size() > 1) {
                    throw new UnsupportedOperationException("SolveGoal:: wrong annotations size");
                } else {
                    EAnnotation annotation = annotations.get(0);
                    if (annotation.id.value.equals("seq_search")) {
                        EArray earray = (EArray) annotation.exps.get(0);
                        for (int i = 0; i < earray.what.size(); i++) {
                            ArrayUtils.append(dvars, extractScope((EAnnotation) earray.getWhat_i(i), aSolver));
                        }
                    } else {
                        dvars = ArrayUtils.append(dvars, extractScope(annotation, aSolver));
                    }
                }
                ivars = new IntVar[dvars.length];
                for (int i = 0; i < dvars.length; i++) {
                    ivars[i] = (IntVar) dvars[i];
                }
            }

            LoggerFactory.getLogger(FGoal.class).warn("% No search annotation. Set default.");
            if (type == ResolutionPolicy.SATISFACTION && gc.all) {
                aSolver.set(new Assignment(ivars, new FirstFail(ivars), new InDomainMin()));
            } else {
                switch (gc.bbss) {
                    case 2:
                        ImpactBased ibs = new ImpactBased(ivars, 2, 3, 10, gc.seed, false);
                        aSolver.set(ibs);
                        break;
                    case 3:
                        DomOverWDegVS dwd = new DomOverWDegVS(ivars, aSolver, gc.seed);
                        aSolver.set(new Assignment(ivars, dwd, new InDomainMin()));
                        break;
                    case 4:
                        aSolver.set(new Assignment(ivars, new FirstFail(ivars), new InDomainMin()));
                        break;
                    case 1:
                    default:
                        ActivityBased abs = new ActivityBased(aSolver, ivars, 0.999d, 0.2d, 8, 1.1d, 1, gc.seed);
                        aSolver.set(abs);
                        if (type != ResolutionPolicy.SATISFACTION) { // also add LNS in optimization
                            aSolver.getSearchLoop().plugSearchMonitor(new ABSLNS(aSolver, ivars, gc.seed, abs, false, ivars.length / 2));
                        }
                        break;
                }
            }
        }
    }

    /**
     * Read search annotation and build corresponding strategy
     *
     * @param e      {@link parser.flatzinc.ast.expression.EAnnotation}
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private static AbstractStrategy readSearchAnnotation(EAnnotation e, Solver solver) {
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
                LoggerFactory.getLogger(FGoal.class).error("Unknown search annotation " + e.toString());
                throw new FZNException();
        }
    }

    /**
     * Read search annotation and build corresponding strategy
     *
     * @param e      {@link parser.flatzinc.ast.expression.EAnnotation}
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private static Variable[] extractScope(EAnnotation e, Solver solver) {
        Expression[] exps = new Expression[e.exps.size()];
        e.exps.toArray(exps);
        Search search = Search.valueOf(e.id.value);
        switch (search) {
            case int_search:
            case bool_search:
                return exps[0].toIntVarArray(solver);
            case set_search:
            default:
                LoggerFactory.getLogger(FGoal.class).error("Unknown search annotation " + e.toString());
                throw new FZNException();
        }
    }
}
