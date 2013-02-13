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

package solver.variables.graph;

import memory.IEnvironment;
import memory.setDataStructures.ISet;
import memory.setDataStructures.SetFactory;
import memory.setDataStructures.SetType;
import solver.variables.graph.graphOperations.GraphTools;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume
 * Date: 9 f�vr. 2011
 * <p/>
 * *
 * Specific implementation of a directed graph
 */
public class DirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    ISet[] successors;
    ISet[] predecessors;
    /**
     * activeIdx represents the nodes available in the graph
     */
    ISet nodes;
    int n;
    SetType type;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public DirectedGraph(int nbits, SetType type, boolean allNodes) {
        this.type = type;
        this.n = nbits;
        predecessors = new ISet[nbits];
        successors = new ISet[nbits];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeSet(type, nbits);
            successors[i] = SetFactory.makeSet(type, nbits);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(nbits);
        } else {
            this.nodes = SetFactory.makeBitSet(nbits);
        }
    }

    public DirectedGraph(int order, boolean[][] matrix, SetType type, boolean allNodes) {
        this(order, type, allNodes);
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                if (matrix[i][j]) {
                    this.successors[i].add(j);
                    this.predecessors[j].add(i);
                }
            }
        }
    }

    public DirectedGraph(IEnvironment env, int nb, SetType type, boolean allNodes) {
        this.n = nb;
        this.type = type;
        predecessors = new ISet[nb];
        successors = new ISet[nb];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeStoredSet(type, nb, env);
            successors[i] = SetFactory.makeStoredSet(type, nb, env);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(nb);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, nb, env);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
        return "Successors :\n" + toStringSuccs() + "\nPredecessors :\n" + toStringPreds();
    }

    public String toStringSuccs() {
        String res = "";
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            res += "pot-" + i + ": ";
            for (int j = successors[i].getFirstElement(); j >= 0; j = successors[i].getNextElement()) {
                res += j + " ";
            }
            res += "\n";
        }
        return res;
    }

    public String toStringPreds() {
        String res = "";
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            res += "pot-" + i + ": ";
            for (int j = predecessors[i].getFirstElement(); j >= 0; j = predecessors[i].getNextElement()) {
                res += j + " ";
            }
            res += "\n";
        }
        return res;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public int getNbNodes() {
        return n;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public ISet getActiveNodes() {
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
    public boolean activateNode(int x) {
		if(!nodes.contain(x)){
			return nodes.add(x);
		}else{
			return false;
		}
    }

    @Override
    public boolean desactivateNode(int x) {
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
		assert (predecessors[x].getSize()==0) :"incoherent directed graph";
		assert (successors[x].getSize()==0) :"incoherent directed graph";
        return false;
    }

    @Override
    public boolean addEdge(int x, int y) {
		assert (nodes.contain(y)) :"incoherent directed graph : node "+y+" has not been added to this yet";
		assert (nodes.contain(x)) :"incoherent directed graph : node "+x+" has not been added to this yet";
        if (x == y) {
            return addArc(x, y);
        }
        boolean b = addArc(x, y);
        b |= addArc(y, x);
		assert (arcExists(y,x)) :"incoherent directed graph";
        return b;
    }

    @Override
    public boolean removeEdge(int x, int y) {
        boolean b = removeArc(x, y);
		b |= removeArc(y, x);
		assert (!arcExists(y,x)):"error while removing edge";
        return b;
    }

    @Override
    public boolean edgeExists(int x, int y) {
        boolean b = arcExists(x, y) || arcExists(y, x);
        return b;
    }

    /**
     * remove arc (from,to) from the graph
     *
     * @param from
     * @param to
     * @return true iff arc (from,to) was in the graph
     */
    public boolean removeArc(int from, int to) {
        if (successors[from].contain(to)) {
			assert (predecessors[to].contain(from)) :"incoherent directed graph";
            successors[from].remove(to);
            predecessors[to].remove(from);
            return true;
        }
        return false;
    }

    /**
     * Test whether arc (from,to) exists or not in the graph
     *
     * @param from
     * @param to
     * @return true iff arc (from,to) exists in the graph
     */
    public boolean arcExists(int from, int to) {
        if (successors[from].contain(to)) {
			assert (predecessors[to].contain(from)) :"incoherent directed graph";
			return true;
        }
        return false;
    }

    /**
     * add arc (from,to) to the graph
     *
     * @param from
     * @param to
     * @return true iff arc (from,to) was not already in the graph
     */
    public boolean addArc(int from, int to) {
        activateNode(from);
        activateNode(to);
        if (!successors[from].contain(to)) {
			assert (!predecessors[to].contain(from)) :"incoherent directed graph";
            successors[from].add(to);
            predecessors[to].add(from);
            return true;
        }
        return false;
    }

    @Override
    /**
     * @inheritedDoc
     * WARNING : not in O(1) but in O(nbSuccs[x]+nbPreds[x])
     */
    public ISet getNeighborsOf(int x) {
        return GraphTools.mergeNeighborhoods(successors[x], predecessors[x], getNbNodes());
    }

    @Override
    public ISet getSuccessorsOf(int x) {
        return successors[x];
    }

    @Override
    public ISet getPredecessorsOf(int x) {
        return predecessors[x];
    }
}
