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

package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.DirectedGraphVar;

/**
 * (incomplete) Propagator that maintains the transitive closure of a directed graph
 * No GAC, no complete checker neither, but fast
 *
 * @author Jean-Guillaume Fages
 */
public class PropTransitiveClosureFastMaintenance extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private DirectedGraphVar g, tc;
    GraphDeltaMonitor gdmG, gdmTC;
    private PairProcedure arcEnforcedInG;
    private PairProcedure arcRemovedFromTC;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropTransitiveClosureFastMaintenance(DirectedGraphVar graph, DirectedGraphVar transitiveClosure, Solver solver, Constraint constraint) {
        super(new DirectedGraphVar[]{graph, transitiveClosure}, solver, constraint, PropagatorPriority.LINEAR);
        g = graph;
        gdmG = (GraphDeltaMonitor) g.monitorDelta(this);
        tc = transitiveClosure;
        gdmTC = (GraphDeltaMonitor) transitiveClosure.monitorDelta(this);
        arcEnforcedInG = new PairProcedure() {
            @Override
            public void execute(int from, int to) throws ContradictionException {
                tc.enforceArc(from, to, aCause);
            }
        };
        arcRemovedFromTC = new PairProcedure() {
            @Override
            public void execute(int from, int to) throws ContradictionException {
                g.removeArc(from, to, aCause);
            }
        };
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int n = g.getEnvelopGraph().getNbNodes();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (g.getKernelGraph().arcExists(i, j)) {
                    tc.enforceArc(i, j, aCause);
                } else if (!tc.getEnvelopGraph().arcExists(i, j)) {
                    g.removeArc(i, j, aCause);
                }
            }
        }
        gdmG.unfreeze();
        gdmTC.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            gdmG.freeze();
            gdmG.forEachArc(arcEnforcedInG, EventType.ENFORCEARC);
            gdmG.unfreeze();
        } else {
            gdmTC.freeze();
            gdmTC.forEachArc(arcRemovedFromTC, EventType.REMOVEARC);
            gdmTC.unfreeze();
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return EventType.ENFORCEARC.mask;
        } else {
            return EventType.REMOVEARC.mask;
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; //not implemented
    }
}
