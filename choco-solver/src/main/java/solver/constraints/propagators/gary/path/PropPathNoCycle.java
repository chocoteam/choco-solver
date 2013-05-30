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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.path;

import choco.annotations.PropAnn;
import memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.DirectedGraphVar;
import util.ESat;
import util.objects.setDataStructures.ISet;
import util.procedure.PairProcedure;

/**
 * Simple nocircuit contraint (from noCycle of Caseaux/Laburthe)
 *
 * @author Jean-Guillaume Fages
 */
@PropAnn(tested = PropAnn.Status.BENCHMARK)
public class PropPathNoCycle extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    GraphDeltaMonitor gdm;
    int n;
    private PairProcedure arcEnforced;
    private IStateInt[] origin, end, size;
    private int source, sink;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that graph has no circuit, with Caseaux/Laburthe/Pesant algorithm
     * runs in O(1) per instantiation event
     *
     * @param graph
     */
    public PropPathNoCycle(DirectedGraphVar graph, int source, int sink) {
        super(new DirectedGraphVar[]{graph}, PropagatorPriority.LINEAR,false);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.n = g.getEnvelopGraph().getNbNodes();
        arcEnforced = new EnfArc();
        origin = new IStateInt[n];
        size = new IStateInt[n];
        end = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            origin[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
            end[i] = environment.makeInt(i);
        }
        this.source = source;
        this.sink = sink;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int j;
        for (int i = 0; i < n; i++) {
            end[i].set(i);
            origin[i].set(i);
            size[i].set(1);
        }
        for (int i = 0; i < n; i++) {
            j = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
            if (j != -1) {
                enforce(i, j);
            }
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
        gdm.unfreeze();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ENFORCEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        if (!g.instantiated()) {
            return ESat.UNDEFINED;
        }
        int x;
        int nb = 0;
        ISet nei;
        int y = source;
        while (y != sink) {
            nb++;
            x = y;
            nei = g.getEnvelopGraph().getSuccessorsOf(x);
            y = nei.getFirstElement();
            if (nei.getSize() != 1 || y == x) {
                return ESat.FALSE;
            }
        }
        nb++;
        if (nb != g.getEnvelopOrder() || g.getEnvelopGraph().getSuccessorsOf(sink).getSize() > 0) {
            return ESat.FALSE;
        }
        return ESat.TRUE;
    }

    private void enforce(int i, int j) throws ContradictionException {
        int last = end[j].get();
        int start = origin[i].get();
        if (origin[j].get() != j) {
            contradiction(g, "");
        }
        g.removeArc(last, start, aCause);
        origin[last].set(start);
        end[start].set(last);
        size[start].add(size[j].get());
//		if(start==source && size[start].get()<n-1){
//			g.removeArc(last,sink,this);
//		}
        if (start == source || last == sink) {
            if (size[source].get() + size[origin[sink].get()].get() < n) {
                g.removeArc(end[source].get(), origin[sink].get(), aCause);
            }
            if (size[source].get() + size[origin[sink].get()].get() == n) {
                g.enforceArc(end[source].get(), origin[sink].get(), aCause);
            }
        }
        if (origin[sink].get() == source && size[source].get() != n) {
//			throw new UnsupportedOperationException("should be already treated");
            contradiction(g, "non hamiltonian path");
        }
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class EnfArc implements PairProcedure {
        @Override
        public void execute(int i, int j) throws ContradictionException {
            enforce(i, j);
        }
    }
}
