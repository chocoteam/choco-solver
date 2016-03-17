/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.objects.graphs;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Directed graph implementation : arcs are indexed per endpoints
 * @author Jean-Guillaume Fages, Xavier Lorca
 */
public class DirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    ISet[] successors;
    ISet[] predecessors;
    ISet nodes;
    int n;
    SetType type;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an empty graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param n        maximum number of nodes
     * @param type     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph.
	 *                 i.e. The node set is fixed to [0,n-1] and will never change
     */
    public DirectedGraph(int n, SetType type, boolean allNodes) {
        this.type = type;
        this.n = n;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeSet(type, 0);
            successors[i] = SetFactory.makeSet(type, 0);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeBitSet(0);
        }
    }

    /**
     * Creates an empty backtrable graph of n nodes
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param model   model providing the backtracking environment
     * @param n        maximum number of nodes
     * @param type     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph
     */
    public DirectedGraph(Model model, int n, SetType type, boolean allNodes) {
        this.n = n;
        this.type = type;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeStoredSet(type, 0, model);
            successors[i] = SetFactory.makeStoredSet(type, 0, model);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes : \n").append(nodes).append("\n");
        sb.append("successors : \n");
        for (int i : nodes) {
            sb.append(i).append(" -> {");
            for (int j : successors[i]) {
                sb.append(j).append(" ");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    @Override
    public int getNbMaxNodes() {
        return n;
    }

    @Override
    public ISet getNodes() {
        return nodes;
    }

    @Override
    public SetType getType() {
        return type;
    }

    @Override
    public boolean addNode(int x) {
        return !nodes.contain(x) && nodes.add(x);
    }

    @Override
    public boolean removeNode(int x) {
        if (nodes.remove(x)) {
            for (int j : successors[x]) {
                predecessors[j].remove(x);
            }
            successors[x].clear();
            for (int j : predecessors[x]) {
                successors[j].remove(x);
            }
            predecessors[x].clear();
            return true;
        }
        assert (predecessors[x].getSize() == 0) : "incoherent directed graph";
        assert (successors[x].getSize() == 0) : "incoherent directed graph";
        return false;
    }

    /**
     * remove arc (from,to) from the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff arc (from,to) was in the graph
     */
    public boolean removeArc(int from, int to) {
        if (successors[from].contain(to)) {
            assert (predecessors[to].contain(from)) : "incoherent directed graph";
            successors[from].remove(to);
            predecessors[to].remove(from);
            return true;
        }
        return false;
    }

    /**
     * Test whether arc (from,to) exists or not in the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff arc (from,to) exists in the graph
     */
    public boolean arcExists(int from, int to) {
        if (successors[from].contain(to)) {
            assert (predecessors[to].contain(from)) : "incoherent directed graph";
            return true;
        }
        return false;
    }

    @Override
    public boolean isArcOrEdge(int from, int to) {
        return arcExists(from, to);
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    /**
     * add arc (from,to) to the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff arc (from,to) was not already in the graph
     */
    public boolean addArc(int from, int to) {
        addNode(from);
        addNode(to);
        if (!successors[from].contain(to)) {
            assert (!predecessors[to].contain(from)) : "incoherent directed graph";
            successors[from].add(to);
            predecessors[to].add(from);
            return true;
        }
        return false;
    }

    /**
     * Get successors of node x
     *
     * @param x node index
     * @return successors of x
     */
    public ISet getSuccOf(int x) {
        return successors[x];
    }

    @Override
    public ISet getSuccOrNeighOf(int x) {
        return successors[x];
    }

    /**
     * Get predecessors of node x
     *
     * @param x node index
     * @return predecessors of x
     */
    public ISet getPredOf(int x) {
        return predecessors[x];
    }

    @Override
    public ISet getPredOrNeighOf(int x) {
        return predecessors[x];
    }
}
