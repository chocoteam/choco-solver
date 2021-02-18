/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.dominance;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Iterator;

/**
 * Class that finds dominators of a given flow graph g(s)
 */
public abstract class AbstractLengauerTarjanDominatorsFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// flow graph
	protected DirectedGraph g;
	// dominator tree
	protected DirectedGraph T;
	protected int root, n, k;
	protected int[] parent, vertex, bucket, ancestor, label, semi, dom;
	protected ISet[] succs;
	protected ISet[] preds;
	protected Iterator<Integer>[] iterator;
	protected TIntArrayList list;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Object that finds dominators of the given flow graph g(s)
	 */
	public AbstractLengauerTarjanDominatorsFinder(int s, DirectedGraph g) {
		root = s;
		n = g.getNbMaxNodes();
		this.g = g;
		parent = new int[n];
		semi = new int[n];
		dom = new int[n];
		ancestor = new int[n];
		label = new int[n];
		vertex = new int[n];
		bucket = new int[n];
		succs = new ISet[n];
		preds = new ISet[n];
		//noinspection unchecked
		iterator = new Iterator[n];
		T = new DirectedGraph(n, SetType.LINKED_LIST, false);
		list = new TIntArrayList();
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	/**
	 * Find immediate dominators of the given graph
	 * and preprocess dominance requests
	 *
	 * @return false iff the source cannot reach all nodes (contradiction)
	 */
	public boolean findDominators() {
		initParams(false);
		DFS();
		if (k != n - 1) {
			return false;
		}
		findAllIdom();
		preprocessDominanceRequests();
		return true;
	}

	/**
	 * Find immediate postdominators of the given graph
	 * and preprocess dominance requests
	 * post dominators are dominators of the inverse graph
	 *
	 * @return false iff the source cannot reach all nodes (contradiction)
	 */
	public boolean findPostDominators() {
		initParams(true);
		DFS();
		if (k != n - 1) {
			return false;
		}
		findAllIdom();
		preprocessDominanceRequests();
		return true;
	}

	protected void initParams(boolean inverseGraph) {
		for (int i = 0; i < n; i++) {
			T.getSuccOf(i).clear();
			T.getPredOf(i).clear();
			if (inverseGraph) {
				succs[i] = g.getPredOf(i);
				preds[i] = g.getSuccOf(i);
			} else {
				succs[i] = g.getSuccOf(i);
				preds[i] = g.getPredOf(i);
			}
			semi[i] = -1;
			ancestor[i] = -1;
			bucket[i] = -1;
		}
	}

	private void DFS() {
		int node = root;
		int next;
		k = 0;
		semi[node] = k;
		label[node] = node;
		vertex[k] = node;
		for(int i=0;i<n;i++){
			iterator[i] = succs[i].iterator();
		}
		while (true) {
			if(iterator[node].hasNext()){
				next = iterator[node].next();
				if (semi[next] == -1) {
					k++;
					semi[next] = k;
					label[next] = next;
					vertex[k] = next;
					parent[next] = node;
					node = next;
				}
			} else {
				if (node == root) {
					break;
				}
				node = parent[node];
			}
		}
	}

	//***********************************************************************************
	// SDOM & IDOM
	//***********************************************************************************

	private void findAllIdom() {
		int w, u;
		ISetIterator prds;
		for (int i = n - 1; i >= 1; i--) {
			w = vertex[i];
			prds = preds[w].iterator();
			while (prds.hasNext()) {
				int v = prds.nextInt();
				u = eval(v);
				if (semi[u] < semi[w]) {
					semi[w] = semi[u];
				}
			}
			if (vertex[semi[w]] != parent[w]) {
				addToBucket(vertex[semi[w]], w);
			} else {
				dom[w] = parent[w];
			}
			link(parent[w], w);
			int oldBI = parent[w];
			int v = bucket[oldBI];
			while (v != -1) {
				bucket[oldBI] = -1;
				u = eval(v);
				if (semi[u] < semi[v]) {
					dom[v] = u;
				} else {
					dom[v] = parent[w];
				}
				oldBI = v;
				v = bucket[v];
			}
		}
		for (int i = 1; i < n; i++) {
			w = vertex[i];
			if (dom[w] != vertex[semi[w]]) {
				dom[w] = dom[dom[w]];
			}
			T.addArc(dom[w], w);
		}
		dom[root] = root;
	}

	private void addToBucket(int buckIdx, int element) {
		if (bucket[buckIdx] == -1) {
			bucket[buckIdx] = element;
		} else {
			int old = bucket[buckIdx];
			bucket[buckIdx] = element;
			bucket[element] = old;
		}
	}

	//***********************************************************************************
	// link-eval
	//***********************************************************************************

	protected abstract void link(int v, int w);

	protected abstract int eval(int v);

	protected abstract void compress(int v);

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	/**
	 * @return the immediate dominator of x in the flow graph
	 */
	public int getImmediateDominatorsOf(int x) {
		return dom[x];
	}

	/**
	 * BEWARE requires preprocessDominanceRequests()
	 *
	 * @return true iff x is dominated by y
	 */
	public boolean isDomminatedBy(int x, int y) {
		return ancestor[x] > ancestor[y] && semi[x] < semi[y];
	}

	/**
	 * Get the dominator tree formed with arcs (x,y)
	 * such that x is the immediate dominator of y
	 *
	 * @return the dominator of the flow graph
	 */
	public DirectedGraph getDominatorTree() {
		return T;
	}

	/**
	 * O(n+m) preprocessing for enabling dominance requests in O(1)
	 * BEWARE : destroy the current data structure (recycling)
	 */
	private void preprocessDominanceRequests() {
		// RECYCLE DATA STRUCTURES
		// ancestor = in  = opening time = preorder
		// semi     = out = closing time = postorder
		for (int i = 0; i < n; i++) {
			parent[i] = -1;
			succs[i] = T.getSuccOf(i);
			iterator[i] = succs[i].iterator();
		}
		//PREPROCESSING
		int time = 0;
		int currentNode = root;
		parent[currentNode] = currentNode;
		ancestor[currentNode] = 0;
		int nextNode;
		while (true) {
			if(iterator[currentNode].hasNext()){
				nextNode = iterator[currentNode].next();
				if (parent[nextNode] == -1) {
					time++;
					ancestor[nextNode] = time;
					parent[nextNode] = currentNode;
					currentNode = nextNode;
				}
			}else{
				time++;
				semi[currentNode] = time;
				if (currentNode == root) {
					break;
				}
				currentNode = parent[currentNode];

			}
		}
	}

	//***********************************************************************************
	// ARC-DOMINATOR //(x,y) existe && x domine y && y domines tous ses autres predecesseurs (sauf x donc)
	//***********************************************************************************
}
