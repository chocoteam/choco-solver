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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/11/12
 * Time: 16:13
 */

package samples;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.relations.GraphRelationFactory;
import solver.constraints.nary.MaxOfAList;
import solver.constraints.nary.NValues;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.channeling.PropGraphRelation;
import solver.constraints.propagators.gary.channeling.PropRelationGraph;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.graph.UndirectedGraphVar;
import solver.variables.view.Views;

/**
 * Dobble problem (card game)
 *
 * @author Alban DERRIEN, Jean-Guillaume Fages
 */
public class Dobble {

    public static void main(String[] args) {
        // problem parameters
        int nbCards = 21;
        int nbSymbols = 21;
        int nbSymbolsPerCard = 5;
        // implied extra filtering
        boolean useGraphs = true;
        boolean useGlobalNValue = false;
        // NValue constraint parameters
        NValues.Type[] types = new NValues.Type[]{NValues.Type.AtLeast_AC};//,NValues.Type.AtMost_BC};
        long timeLimit = 60 * 1000;// (in ms)
        // model and solve the Dobble problem
        solverDobble(nbCards, nbSymbols, nbSymbolsPerCard, useGraphs, useGlobalNValue, types, timeLimit);
    }

    public static void solverDobble(int nbCards, int nbSymbols, int nbSymbolsPerCard,
                                    boolean useGraphs, boolean useGlobalNValue,
                                    NValues.Type[] types, long timeLimit) {
        // solver
        Solver solver = new Solver();
        // variables
        IntVar[][] cardSymbols = new IntVar[nbCards][nbSymbolsPerCard];
        for (int i = 0; i < nbCards; i++) {
            cardSymbols[i] = VariableFactory.enumeratedArray("Card_" + i, nbSymbolsPerCard, 0, nbSymbols - 1, solver);
        }
        IntVar[] flatVars = ArrayUtils.flatten(cardSymbols);
        IntVar nbUsedSymbols = VariableFactory.bounded("nbUsedSymbols", 0, nbSymbols, solver);
        IntVar nbSymbolsPerCardPair = VariableFactory.enumerated("nbSymbolsPerCardPair", nbSymbolsPerCard * 2 - 1, nbSymbolsPerCard * 2 - 1, solver);
        // constraints
        for (int i = 0; i < nbCards; i++) {
            // Symbols on the same card are different
            solver.post(new AllDifferent(cardSymbols[i], solver, AllDifferent.Type.AC));
            for (int j = 0; j < nbSymbolsPerCard - 1; j++) { // symmetry breaking
                solver.post(IntConstraintFactory.arithm(cardSymbols[i][j], "<", cardSymbols[i][j + 1]));
            }
            for (int j = i + 1; j < nbCards; j++) {// for each card pair
                // there should be exactly one symbol in common
                IntVar[] flatIJ = ArrayUtils.append(cardSymbols[i], cardSymbols[j]);
                solver.post(new NValues(flatIJ, nbSymbolsPerCardPair, solver, types));
                if (useGraphs) {// graph-based implied filtering
                    addCardsPairGraphNValues(solver, flatIJ, nbSymbolsPerCardPair);
                }
            }
        }
        for (int i = 0; i < nbCards - 1; i++) {// symmetry breaking
            solver.post(IntConstraintFactory.arithm(cardSymbols[i][0], "<=", cardSymbols[i + 1][0]));
        }
        if (useGlobalNValue) { // nbUsedSymbols symbols are used in total
            solver.post(new NValues(flatVars, nbUsedSymbols, solver, types));
            if (useGraphs) {// graph-based implied filtering
                addGlobalGraphNValues(solver, flatVars, nbUsedSymbols, nbSymbolsPerCard);
            }
            final IntVar max = Views.offset(nbUsedSymbols, -1);
            solver.post(new MaxOfAList(max, flatVars, solver));
            Constraint csym = new Constraint(solver);
            csym.addPropagators(new PropTakeFirstValues(flatVars, max, solver, csym));
        }
        // search strategy
        solver.set(StrategyFactory.inputOrderMinVal(flatVars, solver.getEnvironment()));
        // output
        SearchMonitorFactory.log(solver, true, false);
        // time limit
        solver.getSearchLoop().getLimitsBox().setTimeLimit(timeLimit);
        // run!
        solver.findSolution();
    }

    // NValue which considers the allDifferent on each card
    private static void addGlobalGraphNValues(Solver solver, IntVar[] flatJeu, IntVar nValTotal, int nbSymbCarte) {
        int nbNodes = flatJeu.length;
        UndirectedGraphVar g = new UndirectedGraphVar(solver, nbNodes, true);
        for (int i = 0; i < nbNodes; i++) {
            g.getEnvelopGraph().addEdge(i, i);
            g.getKernelGraph().addEdge(i, i);
            for (int j = i + 1; j < nbNodes; j++) {
                if (i / nbSymbCarte != j / nbSymbCarte) {
                    g.getEnvelopGraph().addEdge(i, j);
                }
            }
        }
        Constraint gc = GraphConstraintFactory.nCliques(g, nValTotal, solver);
        gc.addPropagators(new PropRelationGraph(flatJeu, g, solver, gc, GraphRelationFactory.equivalence(flatJeu)));
        gc.addPropagators(new PropGraphRelation(flatJeu, g, solver, gc, GraphRelationFactory.equivalence(flatJeu)));
        solver.post(gc);
    }

    // NValue which considers the allDifferent on each card
    private static void addCardsPairGraphNValues(Solver solver, IntVar[] flatIJ, IntVar nValues) {
        int nbNodes = flatIJ.length;
        UndirectedGraphVar gpair = new UndirectedGraphVar(solver, nbNodes, true);
        for (int k1 = 0; k1 < nbNodes; k1++) {
            gpair.getEnvelopGraph().addEdge(k1, k1);
            gpair.getKernelGraph().addEdge(k1, k1);
        }
        for (int k1 = 0; k1 < nbNodes / 2; k1++) {
            gpair.getEnvelopGraph().addEdge(k1, k1);
            gpair.getKernelGraph().addEdge(k1, k1);
            for (int k2 = nbNodes / 2; k2 < nbNodes; k2++) {
                gpair.getEnvelopGraph().addEdge(k1, k2);
            }
        }
        Constraint gcpair = GraphConstraintFactory.nCliques(gpair, nValues, solver);
        gcpair.addPropagators(new PropRelationGraph(flatIJ, gpair, solver, gcpair, GraphRelationFactory.equivalence(flatIJ)));
        gcpair.addPropagators(new PropGraphRelation(flatIJ, gpair, solver, gcpair, GraphRelationFactory.equivalence(flatIJ)));
        solver.post(gcpair);
    }

    private static class PropTakeFirstValues extends Propagator<IntVar> {
        int n;
        IIntDeltaMonitor[] idms;
        IntProcedure proc;

        protected PropTakeFirstValues(IntVar[] variables, final IntVar max, Solver solver, Constraint c) {
            super(variables, solver, c, PropagatorPriority.LINEAR, false);
            int n = vars.length;
            idms = new IIntDeltaMonitor[n];
            for (int i = 0; i < n; i++) {
                idms[i] = vars[i].monitorDelta(this);
            }
            proc = new IntProcedure() {
                @Override
                public void execute(int val) throws ContradictionException {
                    for (IntVar v : vars) {
                        if (v.contains(val)) {
                            return;
                        }
                    }
                    max.updateUpperBound(val - 1, aCause);
                }
            };
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            return EventType.REMOVE.mask;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            for (int i = 0; i < n; i++) {
                idms[i].unfreeze();
            }
        }

        @Override
        public void propagate(int idxVarInProp, int mask) throws ContradictionException {
            idms[idxVarInProp].freeze();
            idms[idxVarInProp].forEach(proc, EventType.REMOVE);
            idms[idxVarInProp].unfreeze();
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }

}