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

package solver.constraints.gary;

import choco.kernel.ESat;
import choco.kernel.memory.setDataStructures.ISet;
import gnu.trove.list.array.TIntArrayList;
import solver.constraints.Constraint;
import solver.constraints.propagators.gary.arborescences.PropNTree;
import solver.constraints.propagators.gary.basic.PropKLoops;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.DirectedGraph;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.graphOperations.GraphTools;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;

/**
 * Constraint for tree partitioning an anti-arborscence
 * In the modelization a root is a loop
 * GAC ensured in O(alpha.m) worst case time (cf. paper Revisiting the tree constraint)
 * where alpha is the inverse of ackermann function
 * <p/>
 * BEWARE this implementation supposes that every node is part of the solution graph
 *
 * @author Jean-Guillaume Fages
 */
public class NTree extends Constraint {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    IntVar nTree;
    StrongConnectivityFinder SCCfinder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Create a constraint for tree partitioning graph
     *
     * @param graph the graph variable (directed)
     * @param nTree the expected number of trees (IntVar)
     */
    public NTree(DirectedGraphVar graph, IntVar nTree) {
        super(new Variable[]{graph, nTree}, graph.getSolver());
        setPropagators(
                new PropNodeDegree_AtLeast(graph, GraphVar.IncidentNodes.SUCCESSORS, 1, this, solver),
                new PropNodeDegree_AtMost(graph, GraphVar.IncidentNodes.SUCCESSORS, 1, this, solver),
                new PropKLoops(graph, solver, this, nTree),
                new PropNTree(graph, nTree, solver, this));
        this.g = graph;
        this.nTree = nTree;
    }


    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public ESat isSatisfied() {
        DirectedGraphVar g = (DirectedGraphVar) vars[0];
        int n = g.getEnvelopGraph().getNbNodes();
        IntVar nTree = (IntVar) vars[1];
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
        if (g.instantiated()) {
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
        int n = g.getEnvelopGraph().getNbNodes();
        if (SCCfinder == null) {
            SCCfinder = new StrongConnectivityFinder(g.getEnvelopGraph());
        }
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
