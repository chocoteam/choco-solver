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
package samples.graph.jg_sandbox;

import choco.kernel.ESat;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import java.util.BitSet;
import java.util.Random;

public class PropAtMostKWorkers extends GraphPropagator<Variable> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private GraphVar g;
	private IntVar kWorkers;
	private TIntArrayList[] bound;
	private int[] minVal,maxVal;
	private int firstTaskIndex;
	private int[] mapping;
	private BitSet in;
//	private TIntArrayList firstIndexes;
//	Random rd = new Random();

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtMostKWorkers(GraphVar graph, IntVar kworkers, int firstTaskIndex, Constraint constraint, Solver sol) {
		super(new Variable[]{graph,kworkers}, sol, constraint, PropagatorPriority.QUADRATIC);
		n = graph.getEnvelopGraph().getNbNodes();
		g = graph;
		this.firstTaskIndex = firstTaskIndex;
		bound = new TIntArrayList[firstTaskIndex];
		for (int i=0;i<firstTaskIndex;i++){
			if(g.getEnvelopGraph().getActiveNodes().isActive(i)){
				bound[i] = new TIntArrayList(g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize());
			}
		}
		minVal = new int[n];
		maxVal = new int[n];
		mapping = new int[firstTaskIndex];
		this.kWorkers = kworkers;
		in = new BitSet(firstTaskIndex);
//		firstIndexes = new TIntArrayList(firstTaskIndex);
//		for(int i=0;i<firstTaskIndex;i++){
//			firstIndexes.add(i);
//		}
	}

	//***********************************************************************************
	// Initialization
	//***********************************************************************************

	private void relabelling() throws ContradictionException {
		in.clear();
		INeighbors nei;
		int index = 0;
		for(int i=0;i<firstTaskIndex;i++){
			mapping[i] = -1;
		}
		for(int i=firstTaskIndex;i<n;i++){
			nei = g.getEnvelopGraph().getNeighborsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j<firstTaskIndex && !in.get(j)){
					in.set(j);
					mapping[j] = index++;
				}
			}
		}
//		firstIndexes.shuffle(rd);
//		for(int i=0;i<firstTaskIndex;i++){
//			mapping[i] = firstIndexes.get(i);//seulement changer de 0 ˆ firstTaskIndex
//		}
	}

	private void computeBounds() throws ContradictionException {
		INeighbors nei;
		int min, max;
		for(int i=firstTaskIndex;i<n;i++){
			nei = g.getEnvelopGraph().getNeighborsOf(i);
			min = max = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j<firstTaskIndex){
					j = mapping[j];
					if(min == -1){
						min = max = j;
					}
					if(j<min){
						min = j;
					}
					if(j>max){
						max = j;
					}
				}
			}
			if(min == -1){
				contradiction(g,"");
			}
			minVal[i] = min;
			maxVal[i] = max;
		}
	}

	private void sortLB(){
		for (int i=0;i<firstTaskIndex;i++){
			bound[i].clear();
		}
		for(int i=firstTaskIndex;i<n;i++){
			bound[minVal[i]].add(i);
		}
	}

	private void sortUB(){
		for (int i=0;i<firstTaskIndex;i++){
			bound[i].clear();
		}
		for(int i=firstTaskIndex;i<n;i++){
			bound[maxVal[i]].add(i);
		}
	}

	//***********************************************************************************
	// PRUNING
	//***********************************************************************************

	private void pruneLB() throws ContradictionException {
		int node;
		int min = -1;
		int max = n+1;
		int nbKer = 0;
		for(int i=0;i<firstTaskIndex;i++){
			for(int k=bound[i].size()-1;k>=0;k--){
				node = bound[i].get(k);
				if(min == -1){
					min = minVal[node];
					max = maxVal[node];
					nbKer++;
				}
				if(minVal[node]<=max){
					min = Math.max(min, minVal[node]);
					max = Math.min(max, maxVal[node]);
				}else{
					min = minVal[node];
					max = maxVal[node];
					nbKer++;
				}
			}
		}
		kWorkers.updateLowerBound(nbKer,this);
		if(nbKer==kWorkers.getUB()){
			nbKer = 0;
			min = -1;
			max = n+1;
			for(int i=0;i<firstTaskIndex;i++){
				for(int k=bound[i].size()-1;k>=0;k--){
					node = bound[i].get(k);
					if(min == -1){
						min = minVal[node];
						max = maxVal[node];
						nbKer++;
					}
					if(minVal[node]<=max){
						min = Math.max(min, minVal[node]);
						max = Math.min(max, maxVal[node]);
						updateLowerNeighbor(node,min);
					}else{
						min = minVal[node];
						max = maxVal[node];
						nbKer++;
					}
				}
			}
		}
	}

	private void updateLowerNeighbor(int node, int lowerNeighbor) throws ContradictionException {
		INeighbors nei = g.getEnvelopGraph().getNeighborsOf(node);
		minVal[node] = Math.max(minVal[node],lowerNeighbor);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(j<firstTaskIndex && mapping[j]<lowerNeighbor){
				g.removeArc(node,j,this);
			}
		}
	}

	private void pruneUB() throws ContradictionException {
		int node;
		int min = -1;
		int max = n+1;
		int nbKer = 0;
		for(int i=firstTaskIndex-1; i>=0; i--){
			for(int k=bound[i].size()-1;k>=0;k--){
				node = bound[i].get(k);
				if(min == -1){
					min = minVal[node];
					max = maxVal[node];
					nbKer++;
				}
				if(maxVal[node]>=min){
					max = Math.min(max,maxVal[node]);
					min = Math.max(min, minVal[node]);
				}else{
					min = minVal[node];
					max = maxVal[node];
					nbKer++;
				}
			}
		}
		kWorkers.updateLowerBound(nbKer,this);
		if(nbKer==kWorkers.getUB()){
			min = -1;
			max = n+1;
			nbKer = 0;
			for(int i=firstTaskIndex-1; i>=0; i--){
				for(int k=bound[i].size()-1;k>=0;k--){
					node = bound[i].get(k);
					if(min == -1){
						min = minVal[node];
						max = maxVal[node];
						nbKer++;
					}
					if(maxVal[node]>=min){
						max = Math.min(max,maxVal[node]);
						min = Math.max(min, minVal[node]);
						updateUpperNeighbor(node,max);
					}else{
						min = minVal[node];
						max = maxVal[node];
						nbKer++;
					}
				}
			}
		}
	}

	private void updateUpperNeighbor(int node, int upperNeighbor) throws ContradictionException {
		INeighbors nei = g.getEnvelopGraph().getNeighborsOf(node);
		maxVal[node] = Math.min(maxVal[node],upperNeighbor);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(j<firstTaskIndex && mapping[j]>upperNeighbor){
				g.removeArc(node,j,this);
			}
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		relabelling();
		computeBounds();
		sortLB();
		pruneLB();
		sortUB();
		pruneUB();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.REMOVENODE.mask
				+ EventType.INCLOW.mask+ EventType.INSTANTIATE.mask+ EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;
	}
}
