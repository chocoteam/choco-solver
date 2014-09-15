/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.nValue.amnv.mis;

import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;

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
	 * @param graph
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
			nbNeighbours[i] = graph.getNeighOf(i).getSize();
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
		ISet nei = graph.getNeighOf(node);
		inMIS.set(node);
		out.set(node);
		int sizeFifo=0;
		for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
			if (!out.get(j)) {
				out.set(j);
				fifo[sizeFifo++] = j;
			}
		}
		for (int i=0; i<sizeFifo; i++) {
			nei = graph.getNeighOf(fifo[i]);
			for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
				nbNeighbours[j]--;
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
