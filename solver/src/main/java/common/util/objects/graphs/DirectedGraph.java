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

package common.util.objects.graphs;

import memory.IEnvironment;
import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetFactory;
import common.util.objects.setDataStructures.SetType;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 9 f�vr. 2011
 * <p/>
 * *
 * Directed graph implementation : arcs are indexed per endpoints
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

	/**
	 * Creates an empty graph.
	 * Allocates memory for n nodes (but they should then be added explicitely,
	 * unless allNodes is true).
	 * 
	 * @param n			maximum number of nodes
	 * @param type		data structure to use for representing node successors and predecessors
	 * @param allNodes	true iff all nodes must always remain present in the graph
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
	 * Allocates memory for n nodes (but they should then be added explicitely,
	 * unless allNodes is true).
	 * 
	 * @param env		storing environment
	 * @param n		maximum number of nodes
	 * @param type		data structure to use for representing node successors and predecessors
	 * @param allNodes	true iff all nodes must always remain present in the graph
	 */
    public DirectedGraph(IEnvironment env, int n, SetType type, boolean allNodes) {
        this.n = n;
        this.type = type;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeStoredSet(type, n, env);
            successors[i] = SetFactory.makeStoredSet(type, n, env);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeFullSet(n);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, n, env);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("nodes : \n"+nodes);
		sb.append("successors : \n");
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            sb.append(i+" -> {");
            for (int j = successors[i].getFirstElement(); j >= 0; j = successors[i].getNextElement()) {
                sb.append(j+" ");
            }
            sb.append("}\n");
        }
        return sb.toString();
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

    /**
     * remove arc (from,to) from the graph
     *
     * @param from	a node index
     * @param to	a node index
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
     * @param from	a node index
     * @param to	a node index
     * @return true iff arc (from,to) exists in the graph
     */
    public boolean arcExists(int from, int to) {
        if (successors[from].contain(to)) {
			assert (predecessors[to].contain(from)) :"incoherent directed graph";
			return true;
        }
        return false;
    }

	@Override
	public boolean isArcOrEdge(int from, int to){
		return arcExists(from,to);
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	/**
     * add arc (from,to) to the graph
     *
     * @param from	a node index
     * @param to	a node index
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

    /**
     * Get successors of node x
     *
     * @param x node index
     * @return successors of x
     */
    public ISet getSuccessorsOf(int x) {
        return successors[x];
    }

	@Override
	public ISet getSuccsOrNeigh(int x){
		return successors[x];
	}

    /**
     * Get predecessors of node x
     *
     * @param x node index
     * @return predecessors of x
     */
    public ISet getPredecessorsOf(int x) {
        return predecessors[x];
    }

	@Override
	public ISet getPredsOrNeigh(int x){
		return predecessors[x];
	}
}
