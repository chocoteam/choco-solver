/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.util.objects.graphs;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
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
            predecessors[i] = SetFactory.makeSet(type, n);
            successors[i] = SetFactory.makeSet(type, n);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(n);
        } else {
            this.nodes = SetFactory.makeBitSet(n);
        }
    }

    /**
     * Creates an empty backtrable graph of n nodes
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param solver   solver providing the backtracking environment
     * @param n        maximum number of nodes
     * @param type     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph
     */
    public DirectedGraph(Solver solver, int n, SetType type, boolean allNodes) {
        this.n = n;
        this.type = type;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeStoredSet(type, n, solver);
            successors[i] = SetFactory.makeStoredSet(type, n, solver);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(n);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, n, solver);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes : \n").append(nodes).append("\n");
        sb.append("successors : \n");
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            sb.append(i).append(" -> {");
            for (int j = successors[i].getFirstElement(); j >= 0; j = successors[i].getNextElement()) {
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
            for (int j = successors[x].getFirstElement(); j >= 0; j = successors[x].getNextElement()) {
                predecessors[j].remove(x);
            }
            successors[x].clear();
            for (int j = predecessors[x].getFirstElement(); j >= 0; j = predecessors[x].getNextElement()) {
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

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        throw new UnsupportedOperationException("Cannot duplicate DirectedGraph yet");
    }
}
