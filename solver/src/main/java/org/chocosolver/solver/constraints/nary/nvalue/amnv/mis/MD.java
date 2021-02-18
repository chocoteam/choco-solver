/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.mis;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

import java.util.BitSet;

/**
 * Min Degree heuristic
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class MD implements F{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected UndirectedGraph graph;
	protected int n;
	protected BitSet out, inMIS;
	protected int[] nbNeighbours, fifo;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates an instance of the Min Degree heuristic to compute independent sets on graph
	 * @param graph the graph
	 */
	public MD(UndirectedGraph graph){
		this.graph = graph;
		n = graph.getNbMaxNodes();
		out = new BitSet(n);
		inMIS = new BitSet(n);
		nbNeighbours = new int[n];
		fifo = new int[n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void prepare() {}

	@Override
	public void computeMIS() {
		out.clear();
		inMIS.clear();
		for (int i = 0; i < n; i++) {
			nbNeighbours[i] = graph.getNeighOf(i).size();
		}
		int idx = out.nextClearBit(0);
		while (idx < n) {
			for (int i = out.nextClearBit(idx + 1); i < n; i = out.nextClearBit(i + 1)) {
				if (nbNeighbours[i] < nbNeighbours[idx]) {
					idx = i;
				}
			}
			addToMIS(idx);
			idx = out.nextClearBit(0);
		}
	}

	protected void addToMIS(int node) {
		inMIS.set(node);
		out.set(node);
		int sizeFifo=0;
		ISetIterator nei = graph.getNeighOf(node).iterator();
		while (nei.hasNext()) {
			int j = nei.nextInt();
			if (!out.get(j)) {
				out.set(j);
				fifo[sizeFifo++] = j;
			}
		}
		for (int i=0; i<sizeFifo; i++) {
			nei = graph.getNeighOf(fifo[i]).iterator();
			while (nei.hasNext()) {
				nbNeighbours[nei.nextInt()]--;
			}
		}
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public BitSet getMIS(){
		return inMIS;
	}

	@Override
	public boolean hasNextMIS(){
		return false;
	}

}
