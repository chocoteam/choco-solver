/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp;

import solver.constraints.propagators.gary.tsp.HeldKarp;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;

public class PrimTwoTreeFinder extends PrimMSTFinder {


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PrimTwoTreeFinder(int nbNodes, HeldKarp propagator) {
		super(nbNodes,propagator);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	protected void prim() throws ContradictionException {
		minVal = propHK.getMinArcVal();
		if(FILTER){
			maxTArc = minVal;
		}
		inTree.set(0);
		inTree.set(n-1);
		int first=-1,sizeFirst=n+1;
		for(int i=1;i<n-1;i++){
			if(g.getSuccessorsOf(i).neighborhoodSize()<sizeFirst){
				first = i;
				sizeFirst = g.getSuccessorsOf(i).neighborhoodSize();
			}
		}
		if(first==-1){
			propHK.contradiction();
		}
		addNode(first);
		int from,to;
		while (tSize<n-3 && !heap.isEmpty()){
			to = heap.pop();
			from = heap.getMate(to);
			addArc(from,to);
		}
		if(tSize!=n-3){
			propHK.contradiction();
		}
		addExtremities();
		if(tSize!=n-1){
			propHK.contradiction();
		}
	}

	// STUFF
	protected void addExtremities(){
		int mc1 = -1,mc2 = -1;
		if(g.getSuccessorsOf(0).neighborhoodSize()==1){
			mc1 = g.getSuccessorsOf(0).getFirstElement();
		}
		if(g.getSuccessorsOf(n-1).neighborhoodSize()==1){
			mc2 = g.getSuccessorsOf(n-1).getFirstElement();
		}
		if(mc1!=-1){
			if(mc2!=-1){
			}else{
				mc2 = getBestNot(n-1,mc1);
			}
		}else{
			if(mc2!=-1){
				mc1 = getBestNot(0,mc2);
			}else{
				mc2 = getBestNot(n-1,-2);
				mc1 = getBestNot(0,mc2);
				double k = costs[0][mc1]+costs[n-1][mc2];
				int mc1bis = getBestNot(0,-2);
				int mc2bis = getBestNot(n-1,mc1bis);
				double kbis = costs[0][mc1bis]+costs[n-1][mc2bis];
				if(kbis<k){
					mc2 = mc2bis;
					mc1 = mc1bis;
				}
			}
		}
		if(FILTER){
			if(!propHK.isMandatory(0,mc1)){
				maxTArc = Math.max(maxTArc, costs[0][mc1]);
			}
			if(!propHK.isMandatory(n-1,mc2)){
				maxTArc = Math.max(maxTArc, costs[n-1][mc2]);
			}
		}
		Tree.addEdge(0,mc1);
		Tree.addEdge(n-1,mc2);
		tSize += 2;
		treeCost += costs[0][mc1]+costs[n-1][mc2];
	}

	protected int getBestNot(int i, int not) {
		if(not==0 || not==n-1){
			INeighbors nei = g.getSuccessorsOf(i);
			double cost = -1;
			int idx = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=0 && j!=n-1 && (idx==-1 || cost>costs[i][j])){
					idx = j;
					cost = costs[i][j];
				}
			}
			if(idx==-1){
				throw new UnsupportedOperationException();
			}
			return idx;
		}else{
			INeighbors nei = g.getSuccessorsOf(i);
			double cost = -1;
			int idx = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=not && (idx==-1 || cost>costs[i][j])){
					idx = j;
					cost = costs[i][j];
				}
			}
			if(idx==-1){
				System.out.println(nei);
				System.out.println(propHK.isMandatory(i,nei.getFirstElement()));
				throw new UnsupportedOperationException();
			}
			return idx;
		}
	}
}
