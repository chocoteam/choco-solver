/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.connectivity;

import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;
import java.util.Iterator;

public class StrongConnectivityFinder  {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// input
	private DirectedGraph graph;
	private BitSet restriction;
	private int n;
	// output
	private int[] sccFirstNode, nextNode, nodeSCC;
	private int nbSCC;

	// util
	private int[] stack, p, inf, dfsNumOfNode;
	private Iterator<Integer>[] iterator;
	private BitSet inStack;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public StrongConnectivityFinder(DirectedGraph graph) {
		this.graph = graph;
		this.n = graph.getNbMaxNodes();
		//
		stack = new int[n];
		p = new int[n];
		inf = new int[n];
		dfsNumOfNode = new int[n];
		inStack = new BitSet(n);
		restriction = new BitSet(n);
		sccFirstNode = new int[n];
		nextNode = new int[n];
		nodeSCC = new int[n];
		nbSCC = 0;
		//noinspection unchecked
		iterator = new Iterator[n];
	}

	//***********************************************************************************
	// ALGORITHM
	//***********************************************************************************

	public void findAllSCC() {
		ISet nodes = graph.getNodes();
		for (int i = 0; i < n; i++) {
			restriction.set(i, nodes.contains(i));
		}
		findAllSCCOf(restriction);
	}

	// exception is a set of nodes that do not need to be found SCC
	public void findAllSCC(BitSet exception) {
		ISet nodes = graph.getNodes();
		for (int i = exception.nextClearBit(0); i >= 0 && i < n; i = exception.nextClearBit(i + 1)) {
		    	restriction.set(i, nodes.contains(i));
		}
		findAllSCCOf(restriction);
	}

	public void findAllSCCOf(BitSet restriction) {
		inStack.clear();
		for (int i = 0; i < n; i++) {
			dfsNumOfNode[i] = 0;
			inf[i] = n + 2;
			nextNode[i] = -1;
			sccFirstNode[i] = -1;
			nodeSCC[i] = -1;
		}
		nbSCC = 0;
		findSingletons(restriction);
		int first = restriction.nextSetBit(0);
		while (first >= 0) {
			findSCC(first, restriction, stack, p, inf, dfsNumOfNode, inStack);
			first = restriction.nextSetBit(first);
		}
	}

	private void findSingletons(BitSet restriction) {
		for (int i = restriction.nextSetBit(0); i >= 0; i = restriction.nextSetBit(i + 1)) {
			if (graph.getPredOf(i).size() == 0 || graph.getSuccOf(i).size() == 0) {
				nodeSCC[i] = nbSCC;
				sccFirstNode[nbSCC++] = i;
				restriction.clear(i);
			}
		}
	}

	private void findSCC(int start, BitSet restriction, int[] stack, int[] p, int[] inf, int[] dfsNumOfNode, BitSet inStack) {
		int nb = restriction.cardinality();
		// trivial case
		if (nb == 1) {
		    	nodeSCC[start] = nbSCC;
		    	sccFirstNode[nbSCC++] = start;
		    	restriction.clear(start);
		    	return;
		}
		//initialization
		int stackIdx = 0;
		int k = 0;
		int i = start, j;
		dfsNumOfNode[i] = k;
		p[i] = i;
		iterator[i] = graph.getSuccOf(i).iterator();
		stack[stackIdx++] = i;
		inStack.set(i);
		// algo
		while (stackIdx != 0) {
		    	if (iterator[i].hasNext()) {
				j = iterator[i].next();
				if (restriction.get(j)) {
			    		if (!inStack.get(j)) {
						k++;
						dfsNumOfNode[j] = k;
						inf[j] = k;
						p[j] = i;
						i = j;
						iterator[i] = graph.getSuccOf(i).iterator();
						stack[stackIdx++] = i;
						inStack.set(i);
			    		} else {
						inf[i] = Math.min(inf[i], dfsNumOfNode[j]);
			    		}
				}
		    	} else {
				if (inf[i] >= dfsNumOfNode[i]) {
			    		int y;
			    		do {
						y = stack[--stackIdx];
						inStack.clear(y);
						restriction.clear(y);
						sccAdd(y);
			    		} while (y != i);
			    		nbSCC++;
				}
				inf[p[i]] = Math.min(inf[p[i]], inf[i]);
				i = p[i];
		    	}
		}
	}

	private void sccAdd(int y) {
		nodeSCC[y] = nbSCC;
		nextNode[y] = sccFirstNode[nbSCC];
		sccFirstNode[nbSCC] = y;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	public int getNbSCC() {
		return nbSCC;
	}

	public int[] getNodesSCC() {
		return nodeSCC;
	}

	public int getSCCFirstNode(int i) {
		return sccFirstNode[i];
	}

	public int getNextNode(int j) {
		return nextNode[j];
	}

}
