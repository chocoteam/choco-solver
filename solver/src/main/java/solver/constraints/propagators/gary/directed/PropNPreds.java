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

package solver.constraints.propagators.gary.directed;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;


/**
 * @author Jean-Guillaume Fages
 *
 *         Ensures that each node in the given set of nodes has exactly NPreds predecessors
 */
public class PropNPreds extends GraphPropagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    int nPreds;
    RemArc remArc;
    EnfArc enfArc;
    EnfNode enfNode;
    int n;
    private INeighbors concernedNodes;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNPreds(DirectedGraphVar graph, Solver solver, Constraint constraint, int nbPreds, INeighbors concernedNodes) {
        super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
        this.g = graph;
        this.concernedNodes = concernedNodes;
        this.nPreds = nbPreds;
        this.remArc = new RemArc(this);
        this.enfArc = new EnfArc(this);
        this.enfNode = new EnfNode(this);
        this.n = g.getEnvelopGraph().getNbNodes();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
        IActiveNodes ker = g.getKernelGraph().getActiveNodes();
        for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
            if (concernedNodes.contain(i)) {
                INeighbors preds = g.getEnvelopGraph().getPredecessorsOf(i);
                if (preds.neighborhoodSize() == nPreds && ker.isActive(i)) {
                    for (int j = preds.getFirstElement(); j >= 0; j = preds.getNextElement()) {
                        g.enforceArc(j, i, this, false);
                    }
                } else if (preds.neighborhoodSize() < nPreds) {
                    g.removeNode(i, this);
                }
            }
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {

        if ((mask & EventType.REMOVEARC.mask) != 0) {
            eventRecorder.getDeltaMonitor(g).forEach(remArc, EventType.REMOVEARC);
        }
        if ((mask & EventType.ENFORCEARC.mask) != 0) {
            eventRecorder.getDeltaMonitor(g).forEach(enfArc, EventType.ENFORCEARC);
        }
        if ((mask & EventType.ENFORCENODE.mask) != 0) {
            eventRecorder.getDeltaMonitor(g).forEach(enfNode, EventType.ENFORCENODE);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.ENFORCENODE.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    /**
     * Enable to add arcs to the kernel when only nPreds arcs remain in the envelop
     */
    private static class RemArc implements IntProcedure {
        private final PropNPreds p;
        public RemArc(PropNPreds p) {
            this.p = p;
        }
        @Override
        public void execute(int i) throws ContradictionException {
            if (i >= p.n) {
                int to = i % p.n;
                if (p.concernedNodes.contain(to)) {
                    INeighbors prds = p.g.getEnvelopGraph().getPredecessorsOf(to);
                    if (prds.neighborhoodSize() < p.nPreds) {
                        p.g.removeNode(to, p);
                    }
                    if (prds.neighborhoodSize() == p.nPreds && p.g.getKernelGraph().getActiveNodes().isActive(to) && p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize() != p.nPreds) {
                        for (int j = prds.getFirstElement(); j >= 0; j = prds.getNextElement()) {
                            p.g.enforceArc(j, to, p, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enable to remove useless outgoing arcs of a node when the kernel contains nPreds outgoing arcs
     */
    private static class EnfArc implements IntProcedure {
        private final PropNPreds p;
        public EnfArc(PropNPreds p) {
            this.p = p;
        }
        @Override
        public void execute(int i) throws ContradictionException {
            if (i > p.n) {
                int to = i % p.n;
                if (p.concernedNodes.contain(to)) {
                    INeighbors prds = p.g.getEnvelopGraph().getPredecessorsOf(to);
                    if (p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize() > p.nPreds) {
                        p.contradiction(p.g, "too many predecessors");
                    }
                    if (prds.neighborhoodSize() > p.nPreds && p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize() == p.nPreds) {
                        for (int from = prds.getFirstElement(); from >= 0; from = prds.getNextElement()) {
                            if (!p.g.getKernelGraph().arcExists(from, to)) {
                                p.g.removeArc(from, to, p, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private static class EnfNode implements IntProcedure {
        private final PropNPreds p;
        public EnfNode(PropNPreds p) {
            this.p = p;
        }
        @Override
        public void execute(int i) throws ContradictionException {
            if (i <= p.n && p.concernedNodes.contain(i)) {
                INeighbors envPrds = p.g.getEnvelopGraph().getPredecessorsOf(i);
                INeighbors kerPrds = p.g.getKernelGraph().getPredecessorsOf(i);
                if (envPrds.neighborhoodSize() < p.nPreds) {
                    p.contradiction(p.g, "not enough predecessors");
                } else if (kerPrds.neighborhoodSize() > p.nPreds) {
                    p.contradiction(p.g, "too many predecessors");
                } else if (envPrds.neighborhoodSize() == p.nPreds && kerPrds.neighborhoodSize() < p.nPreds) {
                    for (int from = envPrds.getFirstElement(); from >= 0; from = envPrds.getNextElement()) {
                        p.g.enforceArc(from, i, p, false);
                    }
                }
            }
        }
    }
}