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

import solver.constraints.propagators.gary.HeldKarp;
import solver.constraints.propagators.gary.trees.relaxationHeldKarp.PrimMSTFinder;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;

public class PrimOneTreeFinder extends PrimMSTFinder {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	protected int oneNode;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PrimOneTreeFinder(int nbNodes, HeldKarp propagator) {
		super(nbNodes,propagator);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	protected void prim() throws ContradictionException {
		minVal = propHK.getMinArcVal();
		if(FILTER){
			maxTArc = minVal;
		}
		chooseOneNode();
		inTree.set(oneNode);
		INeighbors nei = g.getSuccessorsOf(oneNode);
		int min1 = -1;
		int min2 = -1;
		boolean b1=false,b2=false;
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(!b1){
				if(min1==-1){
					min1 = j;
				}
				if(costs[oneNode][j]<costs[oneNode][min1]){
					min2 = min1;
					min1 = j;
				}
				if(propHK.isMandatory(oneNode,j)){
					if(min1!=j){
						min2 = min1;
					}
					min1 = j;
					b1 = true;
				}
			}
			if(min1!=j && !b2){
				if(min2==-1 || costs[oneNode][j]<costs[oneNode][min2]){
					min2 = j;
				}
				if(propHK.isMandatory(oneNode,j)){
					min2 = j;
					b2 = true;
				}
			}
		}
		if(min1 == -1 || min2 == -1){
			propHK.contradiction();
		}
		if(FILTER){
			if(!propHK.isMandatory(oneNode,min1)){
				maxTArc = Math.max(maxTArc, costs[oneNode][min1]);
			}
			if(!propHK.isMandatory(oneNode,min2)){
				maxTArc = Math.max(maxTArc, costs[oneNode][min2]);
			}
		}
//		boolean findCuts = nei.neighborhoodSize()==2; // TAG: cut, not very helpful
		int first=-1,sizeFirst=n+1;
		for(int i=0;i<n;i++){
			if(i!=oneNode && g.getSuccessorsOf(i).neighborhoodSize()<sizeFirst){
				first = i;
				sizeFirst = g.getSuccessorsOf(i).neighborhoodSize();
			}
		}
		if(first==-1){
			propHK.contradiction();
		}
		addNode(first);
		int from,to;
		while (tSize<n-2 && !heap.isEmpty()){
			to = heap.pop();
			from = heap.getMate(to);
//			if(findCuts && heap.size()==1 && (inTree.get(min1) == inTree.get(min2))){ // TAG: cut, not very helpful
//				findCuts(from,to);
//			}
			addArc(from,to);
		}
		if(tSize!=n-2){
			propHK.contradiction();
		}
		addArc(oneNode,min1);
		addArc(oneNode,min2);
		if(Tree.getNeighborsOf(oneNode).neighborhoodSize()!=2){
			throw new UnsupportedOperationException();
		}
	}

//	// TAG: cut, not very helpful
//	private void findCuts(int from, int to) throws ContradictionException {
//		INeighbors nei = g.getSuccessorsOf(to);
//		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//			if(j!=from && inTree.get(j)){
//				return;
//			}
//		}
//		propHK.enforce(from,to);
//		to = heap.pop();
//		from = heap.getMate(to);
//		nei = g.getSuccessorsOf(to);
//		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//			if(j!=from && inTree.get(j)){
//				heap.add(to,costs[from][to],from);
//				return;
//			}
//		}
//		propHK.enforce(from,to);
//		heap.add(to,minVal,from);
//	}

	private void chooseOneNode(){
		oneNode = 0;
//		int size = 0;
//		int s;
//		for(int i=0;i<n;i++){
//			s = g.getSuccessorsOf(i).neighborhoodSize();
//			if(s>size){
//				size = g.getSuccessorsOf(i).neighborhoodSize();
//				oneNode = i;
//			}
//		}
	}
}
