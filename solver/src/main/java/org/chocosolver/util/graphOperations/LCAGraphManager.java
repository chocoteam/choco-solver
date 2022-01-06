/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations;

import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Iterator;

/**
 * Class enabling to compute LCA queries in constant time over the DFS tree of a given graph
 * use a O(n+m) time preprocessing
 *
 * @author Jean-Guillaume Fages
 */
public class LCAGraphManager {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	//
	private int root;
	private DirectedGraph graph;
	private final int nbNodes;
    private int nbActives;
	//
	private final int[] father;
	private final int[] nodeOfDfsNumber;
	private final int[] dfsNumberOfNode;
	//
	private final int[] I;
    private final int[] L;
    private final int[] h;
    private final int[] A;
    private final int[] htmp;
	private final ISet[] successors;
	private final Iterator<Integer>[] iterator;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public LCAGraphManager(int nb) {
		nbNodes = nb;
		successors = new ISet[nbNodes];
		father = new int[nbNodes];
		nodeOfDfsNumber = new int[nbNodes];
		dfsNumberOfNode = new int[nbNodes];
		htmp = new int[nbNodes];
		A = new int[nbNodes];
		I = new int[nbNodes];
		L = new int[nbNodes];
		h = new int[nbNodes];
		iterator = new Iterator[nbNodes];
	}

	public void preprocess(int r, DirectedGraph g) {
		root = r;
		graph = g;
		initParams();
		proceedFirstDFS();
		performLCAPreprocessing();
	}

	//***********************************************************************************
	// QUERIES
	//***********************************************************************************

	public int getLCA(int x, int y) {
		return nodeOfDfsNumber[getLCADFS(dfsNumberOfNode[x], dfsNumberOfNode[y])];
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	private void initParams() {
		nbActives = graph.getNodes().size();
		for (int i = 0; i < nbNodes; i++) {
			successors[i] = graph.getSuccessorsOf(i);
			dfsNumberOfNode[i] = -1;
			father[i] = -1;
			A[i] = -1;
		}
	}

	/**
	 * perform a dfs in graph to label nodes
	 */
	private void proceedFirstDFS() {
		for (int i = 0; i < nbNodes; i++) {
			iterator[i] = successors[i].iterator();
		}
		int i = root;
		int k = 0;
		father[k] = k;
		dfsNumberOfNode[root] = k;
		nodeOfDfsNumber[k] = root;
		int j;
		k++;
		while (true) {
			if (iterator[i].hasNext()) {
				j = iterator[i].next();
				if (dfsNumberOfNode[j] == -1) {
					father[k] = dfsNumberOfNode[i];
					dfsNumberOfNode[j] = k;
					nodeOfDfsNumber[k] = j;
					i = j;
					k++;
				}
			} else {
				if (i == root) {
					break;
				} else {
					i = nodeOfDfsNumber[father[dfsNumberOfNode[i]]];
				}
			}
		}
		if (k != nbActives) {
			throw new UnsupportedOperationException("LCApreprocess did not reach all nodes");
		}
		for (; k < nbNodes; k++) {
			father[k] = -1;
		}
	}

	//***********************************************************************************
	// LCA  attention : numerotation 1 - n (et non 0 - n-1) pour les manipulations de bit
	//***********************************************************************************

	/**
	 * LCA PREPROCESSING O(m+n) by Chris Lewis
	 * 1.	Do a depth first traversal of the general tree to assign depth-first search numbers to the nodes. For each node set a pointer to its parent.
	 * 2.	Using a linear time bottom up algorithm, compute I(v) for each node. For each k such that I(v) = k for some v, set L(k) to point to the head of the run containing k.
	 * (=>I(v) pointe sur le dernier element, L(I(v)) vers le premier)
	 * o	The head of a run is identified when computing I values. v is identified as the head of its run if the I value of v's parent is not I(v).
	 * o	After this step, the head of a run containing an arbitrary node v can be located in constant time. First compute I(v) then look up the value L(I(v)).
	 * 3.	Given a complete binary tree with node-depth ceiling(log n)-1, map each node v in the general tree to I(v) in the binary tree (Fig 4).
	 * 4.	For each node v in the general tree create on O(log n) bit number A v. Bit A v(i) is set to 1 if and only if node v has an ancestor
	 * in the general tree that maps to height i in the binary tree. i.e. iff v has an ancestor u such that h(I(u)) = i.
	 */
	private void performLCAPreprocessing() {
		// step 1 : DFS already done
		// step 2
		ISet nei;
		for (int i = nbActives - 1; i >= 0; i--) {
			h[i] = BitOperations.getFirstExp(i + 1);
			if (h[i] == -1) {
				throw new UnsupportedOperationException();
			}
			htmp[i] = h[i];
			I[i] = i;
			L[i] = i;
			int sucInRun = -1;
			nei = graph.getSuccessorsOf(nodeOfDfsNumber[i]);
			int s;
			for (int k : nei) {
				s = dfsNumberOfNode[k];
				if (i != s && father[s] == i && htmp[s] > htmp[i]) {
					htmp[i] = htmp[s];
					sucInRun = s;
				}
			}
			if (sucInRun != -1) {
				I[i] = I[sucInRun];
				L[I[i]] = i;
			}
		}
		// step 3 : mapping to a binary tree : the binary tree is virtual here so there is nothing to do
		//step 4
		// A[0] = I[0];
		int exp = BitOperations.getFirstExp(I[0] + 1);
		if (exp == -1) {
			throw new UnsupportedOperationException();
		}
		A[0] = BitOperations.pow(2, exp);
		for (int i = 0; i < nbActives; i++) {
			A[i] = A[father[i]];
			if (I[i] != I[father[i]]) {
				exp = BitOperations.getFirstExp(I[i] + 1);
				if (exp == -1) {
					throw new UnsupportedOperationException();
				}
				A[i] += BitOperations.pow(2, exp);
			}
		}
	}

	/**
	 * Get the lowest common ancestor of two nodes in O(1) time
	 * Query by Chris Lewis
	 * 1.	Find the lowest common ancestor b in the binary tree of nodes I(x) and I(y).
	 * 2.	Find the smallest position j &ge; h(b) such that both numbers A x and A y have 1-bits in position j.
	 * This gives j = h(I(z)).
	 * 3.	Find node x', the closest node to x on the same run as z:
	 * a.	Find the position l of the right-most 1 bit in A x
	 * b.	If l = j, then set x' = x {x and z are on the same run in the general graph} and go to step 4.
	 * c.	Find the position k of the left-most 1-bit in A x that is to the right of position j.
	 * Form the number consisting of the bits of I(x) to the left of the position k,
	 * followed by a 1-bit in position k, followed by all zeros. {That number will be I(w)}
	 * Look up node L(I(w)), which must be node w. Set node x' to be the parent of node w in the general tree.
	 * 4.	Find node y', the closest node to y on the same run as z using the approach described in step 3.
	 * 5.	If x' < y' then set z to x' else set z to y'
	 *
	 * @param x node dfs number
	 * @param y node dfs number
	 * @return the dfs number of the lowest common ancestor of a and b in the dfs tree
	 */
	private int getLCADFS(int x, int y) {
		// trivial cases
		if (x == y || x == father[y]) {
			return x;
		}
		if (y == father[x]) {
			return y;
		}
		// step 1
		int b = BitOperations.binaryLCA(I[x] + 1, I[y] + 1);
		// step 2
		int hb = BitOperations.getFirstExp(b);
		if (hb == -1) {
			throw new UnsupportedOperationException();
		}
		int j = BitOperations.getFirstExpInBothXYfromI(A[x], A[y], hb);
		if (j == -1) {
			throw new UnsupportedOperationException();
		}
		// step 3 & 4
		int xPrim = closestFrom(x, j);
		int yPrim = closestFrom(y, j);
		// step 5
		if (xPrim < yPrim) {
			return xPrim;
		}
		return yPrim;
	}

	private int closestFrom(int x, int j) {
		// 3.a
		int l = BitOperations.getFirstExp(A[x]);
		if (l == -1) {
			throw new UnsupportedOperationException();
		}
		if (l == j) {    // 3.b
			return x;
		} else {        // 3.c
			int k = BitOperations.getMaxExpBefore(A[x], j);
			int IW = I[x] + 1;
			if (k != -1) { // there is at least one 1-bit at the right of j
				IW = BitOperations.replaceBy1and0sFrom(IW, k);
			} else {
				throw new UnsupportedOperationException();
			}
			IW--;
			return father[L[IW]];
		}
	}
}
