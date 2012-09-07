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

package solver.constraints.propagators.gary.basic;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Ensures that the final graph is antisymmetric
 * i.e. if G has arc (x,y) then it does not have (y,x)
 * Except for loops => (x,x) is allowed
 *
 * @author Jean-Guillaume Fages
 */
public class PropAntiSymmetric extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    GraphDeltaMonitor gdm;
    EnfProc enf;
    int n;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropAntiSymmetric(DirectedGraphVar graph, Constraint constraint, Solver solver) {
        super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.UNARY);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        enf = new EnfProc(this);
        n = g.getEnvelopGraph().getNbNodes();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        IActiveNodes ker = g.getKernelGraph().getActiveNodes();
        INeighbors succ;
        for (int i = ker.getFirstElement(); i >= 0; i = ker.getNextElement()) {
            succ = g.getKernelGraph().getSuccessorsOf(i);
            for (int j = succ.getFirstElement(); j >= 0; j = succ.getNextElement()) {
                g.removeArc(j, i, aCause);
            }
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        gdm.forEachArc(enf, EventType.ENFORCEARC);
        gdm.unfreeze();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ENFORCEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        IActiveNodes ker = g.getKernelGraph().getActiveNodes();
        INeighbors succ;
        for (int i = ker.getFirstElement(); i >= 0; i = ker.getNextElement()) {
            succ = g.getKernelGraph().getSuccessorsOf(i);
            for (int j = succ.getFirstElement(); j >= 0; j = succ.getNextElement()) {
                if (g.getKernelGraph().getSuccessorsOf(j).contain(i)) {
                    return ESat.FALSE;
                }
            }
        }
        if (g.instantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    /**
     * Enable to remove the opposite arc
     */
    private class EnfProc implements PairProcedure {
        private final Propagator p;

        public EnfProc(Propagator p) {
            this.p = p;
        }

        @Override
        public void execute(int from, int to) throws ContradictionException {
            if (from != to) {
                g.removeArc(to, from, p);
            }
        }
    }
}
