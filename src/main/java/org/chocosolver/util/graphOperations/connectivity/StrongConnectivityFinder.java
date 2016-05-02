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
	private int[] stack, p, inf, nodeOfDfsNum, dfsNumOfNode;
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
		nodeOfDfsNum = new int[n];
		dfsNumOfNode = new int[n];
		inStack = new BitSet(n);
		restriction = new BitSet(n);
		sccFirstNode = new int[n];
		nextNode = new int[n];
		nodeSCC = new int[n];
		nbSCC = 0;
		iterator = new Iterator[n];
	}

	//***********************************************************************************
	// ALGORITHM
	//***********************************************************************************

	public void findAllSCC() {
		ISet nodes = graph.getNodes();
		for (int i = 0; i < n; i++) {
			restriction.set(i, nodes.contain(i));
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
			findSCC(first, restriction, stack, p, inf, nodeOfDfsNum, dfsNumOfNode, inStack);
			first = restriction.nextSetBit(first);
		}
	}

	private void findSingletons(BitSet restriction) {
		ISet nodes = graph.getNodes();
		for (int i = restriction.nextSetBit(0); i >= 0; i = restriction.nextSetBit(i + 1)) {
			if (nodes.contain(i) && graph.getPredOf(i).getSize() * graph.getSuccOf(i).getSize() == 0) {
				nodeSCC[i] = nbSCC;
				sccFirstNode[nbSCC++] = i;
				restriction.clear(i);
			}
		}
	}

	private void findSCC(int start, BitSet restriction, int[] stack, int[] p, int[] inf, int[] nodeOfDfsNum, int[] dfsNumOfNode, BitSet inStack) {
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
		int i = k;
		dfsNumOfNode[start] = k;
		nodeOfDfsNum[k] = start;
		stack[stackIdx++] = i;
		inStack.set(i);
		p[k] = k;
		iterator[k] = graph.getSuccOf(start).iterator();
		int j;
		// algo
		while (true) {
			if (iterator[i].hasNext()) {
				j = iterator[i].next();
				if (restriction.get(j)) {
					if (dfsNumOfNode[j] == 0 && j != start) {
						k++;
						nodeOfDfsNum[k] = j;
						dfsNumOfNode[j] = k;
						p[k] = i;
						i = k;
						iterator[i] = graph.getSuccOf(j).iterator();
						stack[stackIdx++] = i;
						inStack.set(i);
						inf[i] = i;
					} else if (inStack.get(dfsNumOfNode[j])) {
						inf[i] = Math.min(inf[i], dfsNumOfNode[j]);
					}
				}
			} else {
				if (i == 0) {
					break;
				}
				if (inf[i] >= i) {
					int y, z;
					do {
						z = stack[--stackIdx];
						inStack.clear(z);
						y = nodeOfDfsNum[z];
						restriction.clear(y);
						sccAdd(y);
					} while (z != i);
					nbSCC++;
				}
				inf[p[i]] = Math.min(inf[p[i]], inf[i]);
				i = p[i];
			}
		}
		if (inStack.cardinality() > 0) {
			int y;
			do {
				y = nodeOfDfsNum[stack[--stackIdx]];
				restriction.clear(y);
				sccAdd(y);
			} while (y != start);
			nbSCC++;
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
