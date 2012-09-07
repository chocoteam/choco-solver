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

package solver.constraints.propagators.gary.degree;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Propagator that ensures that a node has at most N predecessors
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNPredecessors extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private DirectedGraphVar g;
    GraphDeltaMonitor gdm;
    private PairProcedure enf_proc;
    private int[] n_Preds;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropAtMostNPredecessors(DirectedGraphVar graph, int nbPreds, Constraint constraint, Solver solver) {
        super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        int n = g.getEnvelopGraph().getNbNodes();
        n_Preds = new int[n];
        for (int i = 0; i < n; i++) {
            n_Preds[i] = nbPreds;
        }
        enf_proc = new ArcEnf();
    }

    public PropAtMostNPredecessors(DirectedGraphVar graph, int[] nbPreds, Constraint constraint, Solver solver) {
        super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        n_Preds = nbPreds;
        enf_proc = new ArcEnf();
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
        for (int node = act.getFirstElement(); node >= 0; node = act.getNextElement()) {
            checkNode(node);
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        gdm.forEachArc(enf_proc, EventType.ENFORCEARC);
        gdm.unfreeze();
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ENFORCEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        IActiveNodes act = g.getKernelGraph().getActiveNodes();
        for (int node = act.getFirstElement(); node >= 0; node = act.getNextElement()) {
            if (g.getKernelGraph().getPredecessorsOf(node).neighborhoodSize() > n_Preds[node]) {
                return ESat.FALSE;
            }
        }
        act = g.getEnvelopGraph().getActiveNodes();
        for (int node = act.getFirstElement(); node >= 0; node = act.getNextElement()) {
            if (g.getEnvelopGraph().getPredecessorsOf(node).neighborhoodSize() > n_Preds[node]) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    /**
     * When a node has more than N predecessors then it must be removed,
     * If it has N predecessors in the kernel then other incident edges
     * should be removed
     */
    private void checkNode(int i) throws ContradictionException {
        INeighbors ker = g.getKernelGraph().getPredecessorsOf(i);
        INeighbors env = g.getEnvelopGraph().getPredecessorsOf(i);
        int size = ker.neighborhoodSize();
        if (size > n_Preds[i]) {
            g.removeNode(i, aCause);
        } else if (size == n_Preds[i] && env.neighborhoodSize() > size) {
            for (int p = env.getFirstElement(); p >= 0; p = env.getNextElement()) {
                if (!ker.contain(p)) {
                    g.removeArc(p, i, aCause);
                }
            }
        }
    }

    private class ArcEnf implements PairProcedure {
        @Override
        public void execute(int i, int j) throws ContradictionException {
            checkNode(j);
        }
    }
}
