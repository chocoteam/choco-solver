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

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Specific implementation of an undirected graph
 *
 * @author Jean-Guillaume Fages, Xavier Lorca
 */
public class UndirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    ISet[] neighbors;
    ISet nodes;
    int n;
    SetType type;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an empty backtrable undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param model   model providing the backtracking environment
     * @param n        max number of nodes
     * @param type     data structure storing for node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(Model model, int n, SetType type, boolean allNodes) {
        this.type = type;
        this.n = n;
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeStoredSet(type, n, model);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(n);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, n, model);
        }
    }

    /**
     * Creates an empty (non-backtrackable) undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param n        max number of nodes
     * @param type     data structure used for storing node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(int n, SetType type, boolean allNodes) {
        this.type = type;
        this.n = n;
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeSet(type, n);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(n);
        } else {
            this.nodes = SetFactory.makeBitSet(n);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes : \n").append(nodes).append("\n");
        sb.append("neighbors : \n");
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            sb.append(i).append(" -> {");
            for (int j = neighbors[i].getFirstElement(); j >= 0; j = neighbors[i].getNextElement()) {
                sb.append(j).append(" ");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    @Override
    /**
     * @inheritedDoc
     */
    public int getNbMaxNodes() {
        return n;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public ISet getNodes() {
        return nodes;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public SetType getType() {
        return type;
    }

    @Override
    public boolean addNode(int x) {
        return nodes.add(x);
    }

    @Override
    public boolean removeNode(int x) {
        if (nodes.remove(x)) {
            ISet nei = getNeighOf(x);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                neighbors[j].remove(x);
            }
            neighbors[x].clear();
            return true;
        }
        return false;
    }

    /**
     * Add edge (x,y) to the graph
     *
     * @param x a node index
     * @param y a node index
     * @return true iff (x,y) was not already in the graph
     */
    public boolean addEdge(int x, int y) {
        if (x == y && !neighbors[x].contain(y)) {
            neighbors[x].add(y);
            return true;
        }
        if (!neighbors[x].contain(y)) {
            assert (!neighbors[y].contain(x)) : "asymmetric adjacency matrix in an undirected graph";
            neighbors[x].add(y);
            neighbors[y].add(x);
            return true;
        }
        return false;
    }

    /**
     * test whether edge (x,y) is in the graph or not
     *
     * @param x a node index
     * @param y a node index
     * @return true iff edge (x,y) is in the graph
     */
    public boolean edgeExists(int x, int y) {
        if (neighbors[x].contain(y)) {
            assert (neighbors[y].contain(x)) : "asymmetric adjacency matrix in an undirected graph";
            return true;
        }
        return false;
    }

    @Override
    public boolean isArcOrEdge(int x, int y) {
        return edgeExists(x, y);
    }

    /**
     * Remove edge (x,y) from the graph
     *
     * @param x a node index
     * @param y a node index
     * @return true iff (x,y) was in the graph
     */
    public boolean removeEdge(int x, int y) {
        if (x == y && neighbors[x].contain(y)) {
            neighbors[y].remove(x);
            return true;
        }
        if (neighbors[x].contain(y)) {
            assert (neighbors[y].contain(x)) : "asymmetric adjacency matrix in an undirected graph";
            neighbors[x].remove(y);
            neighbors[y].remove(x);
            return true;
        }
        return false;
    }

    /**
     * Get neighbors of node x
     *
     * @param x node index
     * @return neighbors of x (predecessors and/or successors)
     */
    public ISet getNeighOf(int x) {
        return neighbors[x];
    }

    @Override
    public ISet getPredOrNeighOf(int x) {
        return neighbors[x];
    }

    @Override
    public ISet getSuccOrNeighOf(int x) {
        return neighbors[x];
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public void duplicate(Model model, THashMap<Object, Object> identitymap) {
        throw new UnsupportedOperationException("Cannot duplicate DirectedGraph yet");
    }
}
