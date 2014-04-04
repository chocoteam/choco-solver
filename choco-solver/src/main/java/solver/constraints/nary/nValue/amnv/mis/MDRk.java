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

import solver.constraints.nary.nValue.amnv.graph.G;
import util.objects.setDataStructures.ISet;

import java.util.Random;

/**
 * Min Degree + Random k heuristic
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class MDRk extends MD{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected int k, iter;
	protected Random rd;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates an instance of the Min Degree + Random k heuristic to compute independent sets on graph
	 * @param graph
	 */
	public MDRk(G graph, int k){
		super(graph);
		this.k=k;
		this.rd = new Random(0);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void prepare() {
		iter=0;
	}

	@Override
	public void computeMIS() {
		iter++;
		if(iter==1){
			super.computeMIS();
		}else{
			computeMISRk();
		}
	}

	protected void computeMISRk() {
		iter++;
		out.clear();
		inMIS.clear();
		while (out.cardinality() < n) {
			int nb = rd.nextInt(n - out.cardinality());
			int idx = out.nextClearBit(0);
			for (int i = idx; i >= 0 && i < n && nb >= 0; i = out.nextClearBit(i + 1)) {
				idx = i;
				nb--;
			}
			inMIS.set(idx);
			out.set(idx);
			ISet nei = graph.getNeighborsOf(idx);
			for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
				out.set(j);
			}
		}
	}

	@Override
	public boolean hasNextMIS(){
		return iter<k;
	}
}
