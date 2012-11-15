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

package solver.constraints.propagators.gary.tsp.directed;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.DirectedGraph;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.setDataStructures.ISet;

import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 */
public class PropSCCDoorsRules extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private DirectedGraphVar g;
    GraphDeltaMonitor gdm;
    private int n;
    private PairProcedure arcRemoved;
    private BitSet sccComputed;
    private TIntArrayList inDoors;
    private TIntArrayList outDoors;
    // rg data structures
    private IStateInt nR;
    IStateInt[] sccOf;
    ISet[] outArcs;
    DirectedGraph rg;
    private IStateInt[] sccFirst, sccNext;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropSCCDoorsRules(DirectedGraphVar graph, Constraint constraint, Solver solver,
                             IStateInt nR, IStateInt[] sccOf, ISet[] outArcs,
                             DirectedGraph rg) {
        super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.n = g.getEnvelopGraph().getNbNodes();
        arcRemoved = new RemArc();
        this.nR = nR;
        this.sccOf = sccOf;
        this.outArcs = outArcs;
        this.rg = rg;
        sccComputed = new BitSet(n);
        inDoors = new TIntArrayList();
        outDoors = new TIntArrayList();
    }

    public PropSCCDoorsRules(DirectedGraphVar graph, Constraint constraint, Solver solver,
                             IStateInt nR, IStateInt[] sccOf, ISet[] outArcs,
                             DirectedGraph rg, IStateInt[] sccFirst, IStateInt[] sccNext) {
        this(graph, constraint, solver, nR, sccOf, outArcs, rg);
        this.sccFirst = sccFirst;
        this.sccNext = sccNext;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = nR.get() - 1; i >= 0; i--) {
            checkSCCLink(i);
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        sccComputed.clear();
        gdm.freeze();
        gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
        gdm.unfreeze();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private void checkSCCLink(int sccFrom) throws ContradictionException {
        inDoors.clear();
        outDoors.clear();
        for (int i = outArcs[sccFrom].getFirstElement(); i >= 0; i = outArcs[sccFrom].getNextElement()) {
            outDoors.add(i / n - 1);
            inDoors.add(i % n);
        }
        if (inDoors.size() == 1) {
            forceInDoor(inDoors.get(0));
        }
        if (outDoors.size() == 1) {
            forceOutDoor(outDoors.get(0));
            // if 1 in & 1 out and scc>2 forbid in->out
            if (sccFirst != null) {
                int sizeSCC = 0;
                int idx = sccFirst[sccFrom].get();
                while (idx != -1 && sizeSCC < 4) {
                    sizeSCC++;
                    idx = sccNext[idx].get();
                }
                if (sizeSCC > 2) {
                    int p = rg.getPredecessorsOf(sccFrom).getFirstElement();
                    if (p != -1) {
                        int in = -1;
                        for (int i = outArcs[p].getFirstElement(); i >= 0; i = outArcs[p].getNextElement()) {
                            if (in == -1) {
                                in = i % n;
                            } else if (in != i % n) {
                                return;
                            }
                        }
                        if (in == -1) {
                            throw new UnsupportedOperationException();
                        }
                        g.removeArc(in, outDoors.get(0), aCause);
                    }
                }
            }
        }
    }

    private void forceInDoor(int x) throws ContradictionException {
        ISet pred = g.getEnvelopGraph().getPredecessorsOf(x);
        int scc = sccOf[x].get();
        for (int i = pred.getFirstElement(); i >= 0; i = pred.getNextElement()) {
            if (sccOf[i].get() == scc) {
                g.removeArc(i, x, aCause);
            }
        }
    }

    private void forceOutDoor(int x) throws ContradictionException {
        ISet succ = g.getEnvelopGraph().getSuccessorsOf(x);
        int scc = sccOf[x].get();
        for (int i = succ.getFirstElement(); i >= 0; i = succ.getNextElement()) {
            if (sccOf[i].get() == scc) {
                g.removeArc(x, i, aCause);
            }
        }
    }

    private class RemArc implements PairProcedure {
        @Override
        public void execute(int from, int to) throws ContradictionException {
            int x = sccOf[from].get();
            int y = sccOf[to].get();
            if (x != y) {
                if (!sccComputed.get(x)) {
                    sccComputed.set(x);
                    checkSCCLink(x);
                }
                if (rg.getSuccessorsOf(x).getFirstElement() != y) {
                    x = rg.getPredecessorsOf(y).getFirstElement();
                    if (x >= 0 && !sccComputed.get(x)) {
                        sccComputed.set(x);
                        checkSCCLink(x);
                    }
                }
            }
        }
    }
}
