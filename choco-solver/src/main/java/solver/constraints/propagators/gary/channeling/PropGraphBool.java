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

package solver.constraints.propagators.gary.channeling;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphVar;
import util.ESat;
import util.procedure.PairProcedure;

/**
 * Propagator channeling between arcs of a graph and a boolean matrix
 *
 * @author Jean-Guillaume Fages
 */
public class PropGraphBool extends Propagator<GraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected GraphVar graph;
    GraphDeltaMonitor gdm;
    protected BoolVar[][] relations;
    protected PairProcedure enf, rem;
    protected int n;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropGraphBool(GraphVar graph, BoolVar[][] rel) {
        super(new GraphVar[]{graph}, PropagatorPriority.QUADRATIC,false, true);
        this.graph = graph;
        gdm = (GraphDeltaMonitor) graph.monitorDelta(this);
        relations = rel;
        n = rel.length;
        enf = new EnfArc();
        rem = new RemArc();
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            graph.enforceNode(i, aCause);
            for (int j = 0; j < n; j++) {
                if (!graph.getEnvelopGraph().isArcOrEdge(i, j)) {
                    relations[i][j].setToFalse(aCause);
                }
                if (graph.getKernelGraph().isArcOrEdge(i, j)) {
                    relations[i][j].setToTrue(aCause);
                }
            }
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        if ((mask & EventType.ENFORCEARC.mask) != 0) {
            gdm.forEachArc(enf, EventType.ENFORCEARC);
        }
        if ((mask & EventType.REMOVEARC.mask) != 0) {
            gdm.forEachArc(rem, EventType.REMOVEARC);
        }
        gdm.unfreeze();
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        if (!graph.instantiated()) {
            return ESat.UNDEFINED;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (relations[i][j].instantiated()) {
                    if (graph.getEnvelopGraph().isArcOrEdge(i, j) != (relations[i][j].getValue() == 1)) {
                        return ESat.FALSE;
                    }
                } else {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.TRUE;
    }


    //***********************************************************************************
    // REACT ON GRAPH MODIFICATION
    //***********************************************************************************

    /**
     * When an edge (x,y), is enforced then the relation between x and y is true
     */
    private class EnfArc implements PairProcedure {
        public void execute(int i, int j) throws ContradictionException {
            relations[i][j].setToTrue(aCause);
            if (!graph.isDirected()) {
                relations[j][i].setToTrue(aCause);
            }
        }
    }

    /**
     * When an edge (x,y), is removed then the relation between x and y is false
     */
    private class RemArc implements PairProcedure {
        public void execute(int i, int j) throws ContradictionException {
            relations[i][j].setToFalse(aCause);
            if (!graph.isDirected()) {
                relations[j][i].setToFalse(aCause);
            }
        }
    }
}
