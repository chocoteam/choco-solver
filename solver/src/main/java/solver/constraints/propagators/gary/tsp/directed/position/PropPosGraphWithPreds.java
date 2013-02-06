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

package solver.constraints.propagators.gary.tsp.directed.position;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.setDataStructures.ISet;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IGraphDeltaMonitor;
import solver.variables.graph.DirectedGraph;
import solver.variables.graph.DirectedGraphVar;

import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 */
public class PropPosGraphWithPreds extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    IGraphDeltaMonitor gdm;
    int n;
    IntVar[] intVars;
    IStateInt nR;
    IStateInt[] sccOf;
    ISet[] outArcs;
    DirectedGraph rg;
    // data for algorithms
    BitSet done;
    TIntArrayList nextSCCnodes = new TIntArrayList();
    TIntArrayList currentSet = new TIntArrayList();
    TIntArrayList nextSet = new TIntArrayList();
    TIntArrayList tmp = null;
    int[][] dist;
    // precedences
    TIntArrayList pfrom, pto;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropPosGraphWithPreds(IntVar[] intVars, DirectedGraphVar graph, int[][] dist, Constraint constraint, Solver solver) {
        super(ArrayUtils.append(new Variable[]{graph}, intVars), PropagatorPriority.LINEAR);
        g = graph;
        gdm = g.monitorDelta(this);
        this.intVars = intVars;
        this.n = g.getEnvelopGraph().getNbNodes();
        done = new BitSet(n);
        this.dist = dist;
        pfrom = new TIntArrayList();
        pto = new TIntArrayList();
        for (int i = 1; i < n - 1; i++) {
            for (int j = 1; j < n - 1; j++) {
                if (i != j && dist[j][i] == -1) {
                    pfrom.add(i);
                    pto.add(j);
                }
            }
        }
    }

    public PropPosGraphWithPreds(IntVar[] intVars, DirectedGraphVar graph, int[][] dist, Constraint constraint, Solver solver,
                                 IStateInt nR, IStateInt[] sccOf, ISet[] outArcs, DirectedGraph rg) {
        this(intVars, graph, dist, constraint, solver);
        this.nR = nR;
        this.sccOf = sccOf;
        this.outArcs = outArcs;
        this.rg = rg;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        graphTrasversal();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask
                + EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; //not implemented
    }

    //***********************************************************************************
    // GRAPH TRASVERSALS
    //***********************************************************************************

    private void graphTrasversal() throws ContradictionException {
        int s = pfrom.size();
        for (int k = 0; k < s; k++) {
            int i = pfrom.get(k);
            int j = pto.get(k);
            if (rg == null) {
                BFS(i, j);
                BFSfromEnd(i, j);
            } else {
                BFS_RG(i, j);
                BFSfromEnd_RG(i, j);
            }
        }
    }

    private void BFS(int from, int to) throws ContradictionException {
        done.clear();
        currentSet.clear();
        nextSet.clear();
        tmp = null;
        int x = from;
        nextSet.add(x);
        int level = intVars[from].getLB();
        while (nextSet.size() > 0) {
            tmp = currentSet;
            currentSet = nextSet;
            nextSet = tmp;
            nextSet.clear();
            for (int i = currentSet.size() - 1; i >= 0; i--) {
                x = currentSet.get(i);
                if (x == to) {
                    intVars[x].updateLowerBound(level, aCause);
                    return;
                }
                ISet nei = g.getEnvelopGraph().getSuccessorsOf(x);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if (!done.get(j)) {
                        nextSet.add(j);
                        done.set(j);
                    }
                }
            }
            level++;
        }
        contradiction(g, "");
    }

    private void BFS_RG(int from, int to) throws ContradictionException {
        done.clear();
        currentSet.clear();
        nextSet.clear();
        nextSCCnodes.clear();
        tmp = null;
        /// --------
        int x = from;
        int nbNode = 0;
        nextSet.add(x);
        int level = 0;
        int scc = sccOf[x].get();
        int sccfrom = scc;
        int min = -1;
        while (scc != -1) {
            while (nextSet.size() > 0) {
                tmp = currentSet;
                currentSet = nextSet;
                nextSet = tmp;
                nextSet.clear();
                for (int i = currentSet.size() - 1; i >= 0; i--) {
                    nbNode++;
                    x = currentSet.get(i);
                    if (x == to) {
                        intVars[x].updateLowerBound(intVars[from].getLB() + level, aCause);
                        return;
                    }
                    ISet nei = g.getEnvelopGraph().getSuccessorsOf(x);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (!done.get(j)) {
                            done.set(j);
                            if (sccOf[j].get() == scc) {
                                nextSet.add(j);
                            } else {
                                nextSCCnodes.add(j);
                                if (min == -1) {
                                    min = level;
                                }
                            }
                        }
                    }
                }
                level++;
            }
            if (scc == sccfrom) {
                nbNode = min;
            }
            scc = rg.getSuccessorsOf(scc).getFirstElement();
            tmp = nextSet;
            nextSet = nextSCCnodes;
            nextSCCnodes = tmp;
            nextSCCnodes.clear();
            if (level > nbNode) {
//				throw new UnsupportedOperationException();
            }
            level = nbNode;
        }
        contradiction(g, "");
    }

    private void BFSfromEnd(int from, int to) throws ContradictionException {
        done.clear();
        currentSet.clear();
        nextSet.clear();
        tmp = null;
        /// --------
        int x = to;
        nextSet.add(x);
        int level = intVars[to].getUB();
        while (nextSet.size() > 0) {
            tmp = currentSet;
            currentSet = nextSet;
            nextSet = tmp;
            nextSet.clear();
            for (int i = currentSet.size() - 1; i >= 0; i--) {
                x = currentSet.get(i);
                if (x == from) {
                    intVars[x].updateUpperBound(level, aCause);
                    return;
                }
                ISet nei = g.getEnvelopGraph().getPredecessorsOf(x);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if (!done.get(j)) {
                        nextSet.add(j);
                        done.set(j);
                    }
                }
            }
            level--;
        }
//		throw new UnsupportedOperationException(from+" fail "+to);
        contradiction(g, "");
    }

    private void BFSfromEnd_RG(int from, int to) throws ContradictionException {
        done.clear();
        currentSet.clear();
        nextSet.clear();
        nextSCCnodes.clear();
        tmp = null;
        /// --------
        int x = to;
        nextSet.add(x);
        int level = 0;
        int nbNodes = 0;
        int scc = sccOf[x].get();
        int sccto = scc;
        int min = -1;
        while (scc != -1) {
            while (nextSet.size() > 0) {
                tmp = currentSet;
                currentSet = nextSet;
                nextSet = tmp;
                nextSet.clear();
                for (int i = currentSet.size() - 1; i >= 0; i--) {
                    nbNodes++;
                    x = currentSet.get(i);
                    if (x == from) {
                        intVars[x].updateUpperBound(intVars[to].getUB() - level, aCause);
                        return;
                    }
                    ISet nei = g.getEnvelopGraph().getPredecessorsOf(x);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (!done.get(j)) {
                            done.set(j);
                            if (sccOf[j].get() == scc) {
                                nextSet.add(j);
                            } else {
                                nextSCCnodes.add(j);
                                if (min == -1) {
                                    min = level;
                                }
                            }
                        }
                    }
                }
                level++;
            }
            if (scc == sccto) {
                nbNodes = min;
            }
            scc = rg.getPredecessorsOf(scc).getFirstElement();
            tmp = nextSet;
            nextSet = nextSCCnodes;
            nextSCCnodes = tmp;
            nextSCCnodes.clear();
            if (level < nbNodes) {
//				throw new UnsupportedOperationException();
            }
            level = nbNodes;
        }
        contradiction(g, "");
    }
}
