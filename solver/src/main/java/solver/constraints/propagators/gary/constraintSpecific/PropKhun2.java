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
package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.StoredDirectedGraph;

import java.util.BitSet;

public class PropKhun2 extends GraphPropagator<Variable> {

	DirectedGraphVar g;
	IntVar obj;
	int[][] costs, originalCosts;
	int n, M;
	int[] lineZero,nb0;
	BitSet markedRow,markedCol, useful;
	IntProcedure proc;
	// matching structure
//	DirectedGraph digraph;
//	BitSet free;
//	int[] father;
//	BitSet in;
//	int[] list;
//	int n2;
//	long nbBks;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKhun2(DirectedGraphVar graph, IntVar objective, int[][] costsMatrix, Solver sol, Constraint constraint) {
		super(new Variable[]{graph,objective}, sol, constraint, PropagatorPriority.CUBIC);
		g = graph;
		obj = objective;
		originalCosts = costsMatrix;
		n = originalCosts.length-1;
		costs = new int[n][n];
		lineZero = new int[n];
		nb0 = new int[n];
		markedRow   = new BitSet(n);
		markedCol   = new BitSet(n);
		useful      = new BitSet(n);
		proc = new ArcRem();
		// flow
//		n2=2*n;
//		digraph = new StoredDirectedGraph(solver.getEnvironment(),n2, GraphType.LINKED_LIST);
//		free = new BitSet(n2);
//		father = new int[n2];
//		in = new BitSet(n2);
//		list = new int[n2];
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		khun();
		int lb = 0;
		for(int i=0;i<n;i++){
			if(lineZero[i] == 0){
				lb += originalCosts[i][n];
			}else{
				lb += originalCosts[i][lineZero[i]];
			}
		}
		System.out.println("khun bound : "+lb);
	}

	boolean zgueg;
	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
//		if(solver.getMeasures().getBackTrackCount()!=nbBks || vars[idxVarInProp] == obj){
//			nbBks = solver.getMeasures().getBackTrackCount();
//			khun();
//		}else{
//			zgueg = false;
//			eventRecorder.getDeltaMonitor(this,g).forEach(proc,EventType.REMOVEARC);
//			if(zgueg){
		khun();
//			}
//		}
	}

	public void khun() throws ContradictionException {
//		System.out.println("%%%%%%%");
//		System.out.println("%%%%%%%");
		resetMatrix();
		decrease();
//		buildDigraph();
		// iteration
		int val;
		int nbIter = -1;
		while(true){
			nbIter++;
			if(nbIter>n+2000){
				print();
				throw new UnsupportedOperationException();
//				contradiction(g,"");
			}
//			print();
			sortLines();
			val = encadrerBarrer0();
//			val = repairMatching();
			if(val == n){
//				System.exit(0);
				filter();
				return;
			}else{
				mark();
				int min = getMinPositiveValue();
				changeMatrix(min);
			}
		}
	}

	private void print() {
		String s = "\n";
		for(int i=0;i<n;i++){
			String l = "";
			for(int j=0;j<n;j++){
				if(g.getEnvelopGraph().arcExists(i,j)){
					l+=costs[i][j]+"\t";
				}else{
					if(j==0 && g.getEnvelopGraph().arcExists(i,n)){
						l+=costs[i][j]+"\t";
					}else{
						l+="x\t";
					}
				}
			}
			s+= l+"\n";
		}
		System.out.println(s);
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	private void resetMatrix() {
		INeighbors suc;
		M = obj.getUB()+1;
		for(int i=0;i<n;i++){
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
				if(j<n){
					costs[i][j] = originalCosts[i][j];
				}else{
					costs[i][0] = originalCosts[i][j];
				}
			}
		}
	}

	private void decrease() {
		double min;
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			min = M;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j==n){
					j = 0;
				}
				if(costs[i][j]<min){
					min = costs[i][j];
				}
			}
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j==n){
					j = 0;
				}
				costs[i][j] -= min;
			}
		}
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getPredecessorsOf(i);
			min = M;
			if(i==n){
				i = 0;
			}
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(costs[j][i]<min){
					min = costs[j][i];
				}
			}
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				costs[j][i] -= min;
			}
		}
	}

	//***********************************************************************************
	// ALGORITHM
	//***********************************************************************************

	private void sortLines() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nb0[i] = 0;
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j==n)j=0;
				if(costs[i][j]==0){
					nb0[i]++;
				}
			}
			if(nb0[i]==0){
				throw new UnsupportedOperationException();
			}
		}
	}

	private int encadrerBarrer0() {
		int l;
		INeighbors nei;
		useful.clear(); // zeros qui sont deja encadres
		markedRow.clear(); // pour marquer les lignes traites
		for(int ik=0;ik<n;ik++){
			l = nextLine();
			markedRow.set(l);
			lineZero[l] = -1;
			nei = g.getEnvelopGraph().getSuccessorsOf(l);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j==n)j=0;
				if(costs[l][j] == 0){
					if(!useful.get(j)){
						useful.set(j);
						lineZero[l] = j; // encadrer 0
//						System.out.println(l+" z: "+j);
						for(int k=markedRow.nextClearBit(0);k<n;k=markedRow.nextClearBit(k+1)){
							if(costs[k][j]==0){
								if(g.getEnvelopGraph().arcExists(k,j) || (j==0 && g.getEnvelopGraph().arcExists(k,n))){
									nb0[k]--;
								}
							}
						}
//						System.out.println("zero ("+l+") = "+j);
						break;
					}
				}
			}
		}
		return useful.cardinality();
	}

	private int nextLine() {
		int min = markedRow.nextClearBit(0);
		for(int k=markedRow.nextClearBit(0);k<n;k=markedRow.nextClearBit(k+1)){
			if(nb0[k]<nb0[min]){
				min = k;
			}
		}
		return min;
	}

	private void mark() {
		markedRow.clear();
		markedCol.clear();
		INeighbors suc;
		for(int i=0;i<n;i++){
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			if(lineZero[i] == -1){
				markedRow.set(i);
				for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
					if(j==n){
						j=0;
					}
					if(costs[i][j]==0){
						markedCol.set(j);
					}
				}
			}
		}
		boolean again = true;
		while (again){
			again = false;
			for(int i=markedRow.nextClearBit(0);i<n;i=markedRow.nextClearBit(i+1)){
				if(markedCol.get(lineZero[i])){
					markedRow.set(i);
					suc = g.getEnvelopGraph().getSuccessorsOf(i);
					for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
						if(j==n){
							j = 0;
						}
						if(costs[i][j]==0 && !markedCol.get(j)){
							again = true;
							markedCol.set(j);
						}
					}
				}
			}
		}
	}

	private int getMinPositiveValue(){
		int minVal = M;
		INeighbors suc;
		for(int i=markedRow.nextSetBit(0);i>=0;i=markedRow.nextSetBit(i+1)){
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
				if(j==n){
					j = 0;
				}
				if(!markedCol.get(j)){
					if(costs[i][j]<minVal){
						minVal = costs[i][j];
					}
				}
			}
		}
		return minVal;
	}

//	private void changeMatrix(int minVal){
//		INeighbors suc;
//		for(int i=0;i<n;i++){
//			suc = g.getEnvelopGraph().getSuccessorsOf(i);
//			for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
//				if(j==n){
//					j = 0;
//				}
//				if(!markedCol.get(j)){
//					costs[i][j] -= minVal;
//				}
//				if(!markedRow.get(i)){
//					costs[i][j] += minVal;
//				}
//			}
//		}
//	}

	private void changeMatrix(int minVal){
		INeighbors suc;
		boolean already;
		for(int i=0;i<n;i++){
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
				if(j==n){
					j = 0;
				}
				already = costs[i][j]==0;
				if(!markedCol.get(j)){
					costs[i][j] -= minVal;
				}
				if(!markedRow.get(i)){
					costs[i][j] += minVal;
				}
//				if(costs[i][j]==0 && !already){
//					digraph.addArc(i,j+n);
//				}
//				if(costs[i][j]!=0 && already){
//					if(lineZero[i] == j){
//						digraph.removeArc(j+n,i);
//						free.set(i);
//						free.set(j+n);
//					}else{
//						digraph.removeArc(i,j+n);
//					}
//				}
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

//	private void buildDigraph() {
//		free.set(0, n2);
//		int j;
//		INeighbors nei;
//		for(int i=0;i<n2;i++){
//			digraph.getSuccessorsOf(i).clear();
//			digraph.getPredecessorsOf(i).clear();
//		}
//		for(int i=0;i<n;i++){
//			nei = g.getEnvelopGraph().getSuccessorsOf(i);
//			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//				if(j==n){
//					j=0;
//				}
//				if(costs[i][j]==0){
//					digraph.addArc(i,j+n);
//				}
//			}
//		}
//	}
//
//	private int repairMatching() throws ContradictionException {
//		for(int i=free.nextSetBit(0);i>=0 && i<n; i=free.nextSetBit(i+1)){
//			tryToMatch(i);
//		}
//		int p;
//		int cardinality = 0;
//		for (int i=0;i<n;i++) {
//			p = digraph.getPredecessorsOf(i).getFirstElement();
//			if(p!=-1){
//				cardinality++;
//				lineZero[i] = p-n;
//			}else{
//				lineZero[i] = -1;
//			}
//		}
//		return cardinality;
//	}
//
//	private void tryToMatch(int i) throws ContradictionException {
//		int mate = augmentPath_BFS(i);
//		if(mate!=-1){
//			free.clear(mate);
//			free.clear(i);
//			int tmp = mate;
//			while(tmp!=i){
//				digraph.removeArc(father[tmp],tmp);
//				digraph.addArc(tmp,father[tmp]);
//				tmp = father[tmp];
//			}
//		}
//	}
//
//	private int augmentPath_BFS(int root){
//		in.clear();
//		int idxFirst = 0;
//		int idxLast  = 0;
//		list[idxLast++] = root;
//		int x,y;
//		INeighbors succs;
//		while(idxFirst!=idxLast){
//			x = list[idxFirst++];
//			succs = digraph.getSuccessorsOf(x);
//			for(y=succs.getFirstElement();y>=0;y=succs.getNextElement()){
//				if(!in.get(y)){
//					father[y] = x;
//					list[idxLast++] = y;
//					in.set(y);
//					if(free.get(y)){
//						return y;
//					}
//				}
//			}
//		}
//		return -1;
//	}

	//***********************************************************************************
	// FILTERING
	//***********************************************************************************

	private void filter() throws ContradictionException {
		int lb = 0;
		for(int i=0;i<n;i++){
			if(lineZero[i] == 0){
				lb += originalCosts[i][n];
			}else{
				lb += originalCosts[i][lineZero[i]];
			}
		}
		obj.updateLowerBound(lb,this);
		INeighbors nei;
		int delta = obj.getUB()-lb;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(j<n && costs[i][j]>delta){
					g.removeArc(i,j,this);
				}
			}
			if(costs[i][0]>delta){
				g.removeArc(i,n,this);
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// INCREMENTAL PROCEDURE
	//***********************************************************************************

//	public void incrementalKhun() throws ContradictionException {
//		resetMatrix();
//		decrease();
//		// iteration
//		int val;
//		int nbIter = -1;
//		buildDigraph();
//		while(true){
//			nbIter++;
//			if(nbIter>n+2){
////				throw new UnsupportedOperationException();
//				contradiction(g,"");
//			}
////			print(); sortLines(); encadrerBarrer0();
//			val = findZeroesWithMatchingAlgorithm();
//			if(val == n){
//				filter();
//				return;
//			}else{
//				mark();
//				int min = getMinPositiveValue();
//				changeMatrix(min);
//			}
//		}
//	}

	private class ArcRem implements IntProcedure{

		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/(n+1)-1;
			int to   = i%(n+1);
			if(to==n){
				to = 0;
			}
			if(lineZero[from] == to||lineZero[to] == from){
				zgueg = true;
//				lineZero[from] = -1;
//				digraph.removeArc(to+n,from);
//				digraph.addArc(from,to+n);
//				free.set(from);
//				free.set(to+n);
			}
		}
	}

}
