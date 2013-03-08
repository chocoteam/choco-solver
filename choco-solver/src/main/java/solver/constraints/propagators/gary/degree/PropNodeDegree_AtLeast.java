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

import common.ESat;
import common.util.objects.graphs.IGraph;
import common.util.objects.graphs.Orientation;
import common.util.objects.setDataStructures.ISet;
import common.util.procedure.IntProcedure;
import common.util.procedure.PairProcedure;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.UndirectedGraphVar;

/**
 * Propagator that ensures that a node has at most N successors/predecessors/neighbors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeDegree_AtLeast extends Propagator<GraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private GraphVar g;
    GraphDeltaMonitor gdm;
    private IntProcedure enf_nodes_proc;
    private PairProcedure rem_arc_proc;
    private int[] degrees;
    private IncidentSet target;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNodeDegree_AtLeast(DirectedGraphVar graph, Orientation setType, int degree) {
        this(graph, setType, buildArray(degree, graph.getEnvelopGraph().getNbNodes()));
    }

    public PropNodeDegree_AtLeast(DirectedGraphVar graph, Orientation setType, int[] degrees) {
        super(new DirectedGraphVar[]{graph}, PropagatorPriority.BINARY);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.degrees = degrees;
        switch (setType) {
            case SUCCESSORS:
                target = new SNIS();
                rem_arc_proc = new PairProcedure() {
                    public void execute(int i, int j) throws ContradictionException {
                        checkAtLeast(i);
                    }
                };
                break;
            case PREDECESSORS:
                target = new PIS();
                rem_arc_proc = new PairProcedure() {
                    public void execute(int i, int j) throws ContradictionException {
                        checkAtLeast(j);
                    }
                };
                break;
            default:
                throw new UnsupportedOperationException("");
        }
        enf_nodes_proc = new IntProcedure() {
            @Override
            public void execute(int i) throws ContradictionException {
                enforceNode(i);
            }
        };
    }

    public PropNodeDegree_AtLeast(UndirectedGraphVar graph, int degree) {
        this(graph, buildArray(degree, graph.getEnvelopGraph().getNbNodes()));
    }

    public PropNodeDegree_AtLeast(UndirectedGraphVar graph, int[] degrees) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.BINARY);
        target = new SNIS();
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.degrees = degrees;
        rem_arc_proc = new PairProcedure() {
            public void execute(int i, int j) throws ContradictionException {
                checkAtLeast(i);
                checkAtLeast(j);
            }
        };
        enf_nodes_proc = new IntProcedure() {
            @Override
            public void execute(int i) throws ContradictionException {
                enforceNode(i);
            }
        };
    }

    private static int[] buildArray(int degree, int n) {
        int[] degrees = new int[n];
        for (int i = 0; i < n; i++) {
            degrees[i] = degree;
        }
        return degrees;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISet act = g.getEnvelopGraph().getActiveNodes();
        ISet kerAct = g.getKernelGraph().getActiveNodes();
        for (int node = act.getFirstElement(); node >= 0; node = act.getNextElement()) {
            checkAtLeast(node);
            if (kerAct.contain(node)) {
                enforceNode(node);
            }
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        if ((mask & EventType.REMOVEARC.mask) != 0) {
            gdm.forEachArc(rem_arc_proc, EventType.REMOVEARC);
        }
        if ((mask & EventType.ENFORCENODE.mask) != 0) {
            gdm.forEachNode(enf_nodes_proc, EventType.ENFORCENODE);
        }
        gdm.unfreeze();
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCENODE.mask;
    }

    @Override
    public ESat isEntailed() {
        ISet act = g.getKernelGraph().getActiveNodes();
        for (int i = act.getFirstElement(); i >= 0; i = act.getNextElement()) {
            if (target.getSet(g.getEnvelopGraph(), i).getSize() < degrees[i]) {
                return ESat.FALSE;
            }
        }
        if (!g.instantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private void checkAtLeast(int i) throws ContradictionException {
        ISet nei = target.getSet(g.getEnvelopGraph(), i);
        ISet ker = target.getSet(g.getKernelGraph(), i);
        int size = nei.getSize();
        if (size < degrees[i]) {
            g.removeNode(i, aCause);
        } else if (size == degrees[i] && g.getKernelGraph().getActiveNodes().contain(i) && ker.getSize() < size) {
            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                target.enforce(g, i, s, aCause);
            }
        }
    }

    /**
     * When a node is enforced,
     * if it has less than N successors/predecessors/neighbors then a contradiction should be raised
     * if it has N successors/predecessors/neighbors in the envelop then they must figure in the kernel
     */
    private void enforceNode(int i) throws ContradictionException {
        ISet nei = target.getSet(g.getEnvelopGraph(), i);
        ISet ker = target.getSet(g.getKernelGraph(), i);
        int size = nei.getSize();
        if (size < degrees[i]) {
            contradiction(g, "");
        } else if (size == degrees[i] && ker.getSize() < size) {
            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                target.enforce(g, i, s, aCause);
            }
        }
    }

    private class SNIS implements IncidentSet {

        @Override
        public ISet getSet(IGraph graph, int i) {
            return graph.getSuccsOrNeigh(i);
        }

        @Override
        public void enforce(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            g.enforceArc(from, to, cause);
        }

        @Override
        public void remove(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            g.removeArc(from, to, cause);
        }
    }

    private class PIS implements IncidentSet {

        @Override
        public ISet getSet(IGraph graph, int i) {
            return graph.getPredsOrNeigh(i);
        }

        @Override
        public void enforce(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            g.enforceArc(to, from, cause);
        }

        @Override
        public void remove(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            g.removeArc(to, from, cause);
        }
    }
}