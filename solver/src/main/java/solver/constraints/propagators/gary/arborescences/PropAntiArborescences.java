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

package solver.constraints.propagators.gary.arborescences;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import solver.variables.graph.graphOperations.dominance.AlphaDominatorsFinder;
import solver.variables.graph.graphOperations.dominance.SimpleDominatorsFinder;
import solver.variables.setDataStructures.ISet;

/**
 * AntiArborescences constraint (simplification from tree constraint) based on dominators
 * CONSIDERS THAT EACH NODE WITH NO PREDECESSOR IS A SINK (needs at least one such node)
 * Uses simple LT algorithm which runs in O(m.log(n)) worst case time
 * but very efficient in practice
 */
public class PropAntiArborescences extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // flow graph
    DirectedGraphVar g;
    DirectedGraph connectedGraph;
    // number of nodes
    int n;
    // dominators finder that contains the dominator tree
    AbstractLengauerTarjanDominatorsFinder domFinder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropAntiArborescences(DirectedGraphVar graph, Constraint constraint, Solver solver, boolean simple) {
        super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.QUADRATIC);
        g = graph;
        n = g.getEnvelopGraph().getNbNodes();
        connectedGraph = new DirectedGraph(n + 1, GraphType.LINKED_LIST, false);
        if (simple) {
            domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        } else {
            domFinder = new AlphaDominatorsFinder(n, connectedGraph);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        structuralPruning();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    private void structuralPruning() throws ContradictionException {
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccessorsOf(i).clear();
            connectedGraph.getPredecessorsOf(i).clear();
        }
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(i);
            if (nei.isEmpty()) {
                connectedGraph.addArc(i, n);
            } else {
                for (int y = nei.getFirstElement(); y >= 0; y = nei.getNextElement()) {
                    connectedGraph.addArc(i, y);
                }
            }
        }
        if (domFinder.findPostDominators()) {
            for (int x = 0; x < n; x++) {
                nei = g.getEnvelopGraph().getSuccessorsOf(x);
                for (int y = nei.getFirstElement(); y >= 0; y = nei.getNextElement()) {
                    //--- STANDART PRUNING
                    if (domFinder.isDomminatedBy(y, x)) {
                        g.removeArc(x, y, aCause);
                    }
                    // ENFORCE ARC-DOMINATORS (redondant)
                }
            }
        } else {
            contradiction(g, "the source cannot reach all nodes");
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            try {
                structuralPruning();
            } catch (Exception e) {
                return ESat.FALSE;
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
