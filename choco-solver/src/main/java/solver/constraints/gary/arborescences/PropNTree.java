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

package solver.constraints.gary.arborescences;

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.DirectedGraphVar;
import util.ESat;
import util.graphOperations.GraphTools;
import util.graphOperations.connectivity.StrongConnectivityFinder;
import util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import util.graphOperations.dominance.AlphaDominatorsFinder;
import util.objects.graphs.DirectedGraph;
import util.objects.setDataStructures.ISet;

public class PropNTree extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private DirectedGraphVar g;
    private IntVar nTree;
    private int minTree = 0;
    private TIntArrayList nonSinks;
    private StrongConnectivityFinder SCCfinder;
    private DirectedGraph Grs;
    private int n;
    private AbstractLengauerTarjanDominatorsFinder dominatorsFinder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNTree(DirectedGraphVar graph, IntVar nT) {
        super(new Variable[]{graph, nT}, PropagatorPriority.QUADRATIC, true);
        g = (DirectedGraphVar) vars[0];
        nTree = (IntVar) vars[1];
        SCCfinder = new StrongConnectivityFinder(g.getEnvelopGraph());
        nonSinks = new TIntArrayList();
        n = g.getEnvelopGraph().getNbNodes();
        Grs = new DirectedGraph(n + 1, g.getEnvelopGraph().getType(), false);
        dominatorsFinder = new AlphaDominatorsFinder(n, Grs);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    private void filtering() throws ContradictionException {
        computeSinks();
        //1) Bound pruning
        minTreePruning();
        //2) structural pruning
        structuralPruning();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            g.enforceNode(i, aCause);
        }
        filtering();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        filtering();
    }

    private void structuralPruning() throws ContradictionException {
        for (int i = 0; i <= n; i++) {
            Grs.getPredecessorsOf(i).clear();
            Grs.getSuccessorsOf(i).clear();
        }
        Grs.getActiveNodes().clear();
        ISet nei;
        for (int node = 0; node < n; node++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(node);
            for (int suc = nei.getFirstElement(); suc >= 0; suc = nei.getNextElement()) {
                if (suc == node) {
                    Grs.addArc(n, node);
                } else {
                    Grs.addArc(suc, node);
                }
            }
        }
        //dominators
        if (dominatorsFinder.findDominators()) {
            for (int x = 0; x < n; x++) {
                nei = g.getEnvelopGraph().getSuccessorsOf(x);
                for (int y = nei.getFirstElement(); y >= 0; y = nei.getNextElement()) {
                    //--- STANDART PRUNING
                    if (dominatorsFinder.isDomminatedBy(y, x)) {
                        g.removeArc(x, y, aCause);
                    }
                    // ENFORCE ARC-DOMINATORS (redondant)
                }
            }
        } else {
            contradiction(g, "the source cannot reach all nodes");
        }
    }

    private void minTreePruning() throws ContradictionException {
        nTree.updateLowerBound(minTree, aCause);
        if (nTree.getUB() == minTree) {
            int node, scc;
            for (int k = nonSinks.size() - 1; k >= 0; k--) {
                scc = nonSinks.get(k);
                node = SCCfinder.getSCCFirstNode(scc);
                while (node != -1) {
                    if (g.getEnvelopGraph().arcExists(node, node)) {
                        g.removeArc(node, node, aCause);
                    }
                    node = SCCfinder.getNextNode(node);
                }
            }
        }
    }

    private void computeSinks() {
        SCCfinder.findAllSCC();
        int[] sccOf = SCCfinder.getNodesSCC();
        nonSinks.clear();
        boolean looksSink;
        ISet nei;
        int node;
        int nbSinks = 0;
        for (int i = SCCfinder.getNbSCC() - 1; i >= 0; i--) {
            looksSink = true;
            boolean inKer = false;
            node = SCCfinder.getSCCFirstNode(i);
            while (node != -1) {
                if (g.getKernelGraph().getActiveNodes().contain(node)) {
                    inKer = true;
                }
                nei = g.getEnvelopGraph().getSuccessorsOf(node);
                for (int suc = nei.getFirstElement(); suc >= 0 && looksSink; suc = nei.getNextElement()) {
                    if (sccOf[suc] != sccOf[node]) {
                        looksSink = false;
                        break;
                    }
                }
                if (!looksSink) {
                    node = -1;
                } else {
                    node = SCCfinder.getNextNode(node);
                }
            }
            if (looksSink && inKer) {
                nbSinks++;
            } else {
                nonSinks.add(i);
            }
        }
        minTree = nbSinks;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.REMOVENODE.mask;
    }

    //***********************************************************************************
    // ENTAILMENT
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        int MINTREE = calcMinTree();
        int MAXTREE = calcMaxTree();
        ISet nei;
        if (nTree.getLB() <= MAXTREE && nTree.getUB() >= MINTREE) {
            ISet act = g.getEnvelopGraph().getActiveNodes();
            DirectedGraph Grs = new DirectedGraph(n + 1, g.getEnvelopGraph().getType(), false);
            for (int node = act.getFirstElement(); node >= 0; node = act.getNextElement()) {
                if (g.getEnvelopGraph().getSuccessorsOf(node).getSize() < 1 || g.getKernelGraph().getSuccessorsOf(node).getSize() > 1) {
                    return ESat.FALSE;
                }
                nei = g.getEnvelopGraph().getSuccessorsOf(node);
                for (int suc = nei.getFirstElement(); suc >= 0; suc = nei.getNextElement()) {
                    Grs.addArc(suc, node);
                    if (suc == node) {
                        Grs.addArc(node, n);
                        Grs.addArc(n, node);
                    }
                }
            }
            int[] numDFS = GraphTools.performDFS(n, Grs);
            boolean rootFound = false;
            for (int i : numDFS) {
                if (rootFound && i == 0) return ESat.FALSE;
                if (i == 0) rootFound = true;
            }
        } else {
            return ESat.FALSE;
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }

    private int calcMaxTree() {
        int ct = 0;
        ISet act = g.getEnvelopGraph().getActiveNodes();
        for (int node = act.getFirstElement(); node >= 0; node = act.getNextElement()) {
            if (g.getEnvelopGraph().arcExists(node, node)) {
                ct++;
            }
        }
        return ct;
    }

    private int calcMinTree() {
		SCCfinder.findAllSCC();
        int[] sccOf = SCCfinder.getNodesSCC();
        int node;
        TIntArrayList sinks = new TIntArrayList();
        boolean looksSink;
        ISet nei;
        for (int scc = SCCfinder.getNbSCC() - 1; scc >= 0; scc--) {
            looksSink = true;
            node = SCCfinder.getSCCFirstNode(scc);
            while (node != -1) {
                nei = g.getEnvelopGraph().getSuccessorsOf(node);
                for (int suc = nei.getFirstElement(); suc >= 0 && looksSink; suc = nei.getNextElement()) {
                    if (sccOf[suc] != sccOf[node]) {
                        looksSink = false;
                    }
                }
                if (!looksSink) {
                    node = -1;
                } else {
                    node = SCCfinder.getNextNode(node);
                }
            }
            if (looksSink) {
                sinks.add(scc);
            }
        }
        return sinks.size();
    }
}
