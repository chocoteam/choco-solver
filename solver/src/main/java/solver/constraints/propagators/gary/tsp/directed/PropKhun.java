/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.gary.tsp.directed;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.IGraph;
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import java.util.BitSet;

/**
 * WARNING unsafe
 */
public class PropKhun extends Propagator implements IGraphRelaxation {

	DirectedGraphVar g;
	IntVar obj;
	int[][] costs, originalCosts;
	int n, M;
	int[] lineZero;
	BitSet markedRow,markedCol;
	// matching structure
	DirectedGraph digraph;
	BitSet free;
	int[] father;
	BitSet in;
	int[] list;
	int n2;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKhun(DirectedGraphVar graph, IntVar objective, int[][] costsMatrix, Solver sol, Constraint constraint) {
		super(new Variable[]{graph,objective}, sol, constraint, PropagatorPriority.CUBIC);
		g = graph;
		obj = objective;
		originalCosts = costsMatrix;
		n = originalCosts.length-1;
		costs = new int[n][n];
		lineZero = new int[n];
		markedRow   = new BitSet(n);
		markedCol   = new BitSet(n);
		// flow
		n2=2*n;
		digraph = new DirectedGraph(solver.getEnvironment(),n2, GraphType.LINKED_LIST,false);
		free = new BitSet(n2);
		father = new int[n2];
		in = new BitSet(n2);
		list = new int[n2];
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

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		khun();
	}

	public void khun() throws ContradictionException {
		resetMatrix();
		decrease();
		buildDigraph();
		// iteration
		int val;
		int nbIter = -1;
		while(true){
			nbIter++;
			if(nbIter>2*n+2){
//				throw new UnsupportedOperationException();
                contradiction(g, "");
            }
//			print(); sortLines(); encadrerBarrer0();
            val = repairMatching();
            if (val == n) {
                filter();
                for (int i = 0; i < n; i++) {
                    if (lineZero[i] == -1) {
                        throw new UnsupportedOperationException();
                    }
                }
                return;
            } else {
                mark();
                int min = getMinPositiveValue();
                changeMatrix(min);
            }
        }
    }

    private void print() {
//		String s = "\n";
//		for(int i=0;i<n;i++){
//			String l = "";
//			for(int j=0;j<n;j++){
//				if(costs[i][j] == M){
//					l+="x\t";
//				}else{
//					l+=costs[i][j]+"\t";
//				}
//			}
//			s+= l+"\n";
//		}
//		System.out.println(s);
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	private void resetMatrix() {
		ISet suc;
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
		ISet nei;
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

//	private void sortLines() {
//		for(int i=0;i<n;i++){
//			nb0[i] = 0;
//			for(int j=0;j<n;j++){
//				if(costs[i][j]==0){
//					nb0[i]++;
//				}
//			}
//			if(nb0[i]==0){
//				throw new UnsupportedOperationException();
//			}
//		}
//	}
//	private int encadrerBarrer0() {
//		int l;
//		useful.clear(); // zeros qui sont deja encadres
//		markedRow.clear(); // pour marquer les lignes traites
//		for(int i=0;i<n;i++){
//			l = nextLine();
//			markedRow.set(l);
//			lineZero[l] = -1;
//			for(int j=0;j<n;j++){
//				if(costs[l][j] == 0){
//					if(!useful.get(j)){
//						useful.set(j);
//						lineZero[l] = j; // encadrer 0
//						for(int k=markedRow.nextClearBit(0);k<n;k=markedRow.nextClearBit(k+1)){
//							if(costs[k][j]==0){
//								nb0[k]--;
//							}
//						}
////						System.out.println("zero ("+l+") = "+j);
//						break;
//					}
//				}
//			}
//		}
//		return useful.cardinality();
//	}
//	private int nextLine() {
//		int min = markedRow.nextClearBit(0);
//		for(int k=markedRow.nextClearBit(0);k<n;k=markedRow.nextClearBit(k+1)){
//			if(nb0[k]<nb0[min]){
//				min = k;
//			}
//		}
//		return min;
//	}

	private void mark() {
		markedRow.clear();
		markedCol.clear();
		ISet suc;
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
		ISet suc;
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

	private void changeMatrix(int minVal){
		ISet suc;
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
				if(costs[i][j]==0 && !already){
					digraph.addArc(i,j+n);
				}
				if(costs[i][j]!=0 && already){
					if(lineZero[i] == j){
						digraph.removeArc(j+n,i);
						free.set(i);
						free.set(j+n);
					}else{
						digraph.removeArc(i,j+n);
					}
				}
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private void buildDigraph() {
		free.set(0, n2);
		int j;
		ISet nei;
		for(int i=0;i<n2;i++){
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j==n){
					j=0;
				}
				if(costs[i][j]==0){
					digraph.addArc(i,j+n);
				}
			}
		}
	}

	private int repairMatching() throws ContradictionException {
		for(int i=free.nextSetBit(0);i>=0 && i<n; i=free.nextSetBit(i+1)){
			tryToMatch(i);
		}
		int p;
		int cardinality = 0;
		for (int i=0;i<n;i++) {
			p = digraph.getPredecessorsOf(i).getFirstElement();
			if(p!=-1){
				cardinality++;
				lineZero[i] = p-n;
			}else{
				lineZero[i] = -1;
			}
		}
		return cardinality;
	}

	private void tryToMatch(int i) throws ContradictionException {
		int mate = augmentPath_BFS(i);
		if(mate!=-1){
			free.clear(mate);
			free.clear(i);
			int tmp = mate;
			while(tmp!=i){
				digraph.removeArc(father[tmp],tmp);
				digraph.addArc(tmp,father[tmp]);
				tmp = father[tmp];
			}
		}
	}

	private int augmentPath_BFS(int root){
		in.clear();
		int idxFirst = 0;
		int idxLast  = 0;
		list[idxLast++] = root;
		int x,y;
		ISet succs;
		while(idxFirst!=idxLast){
			x = list[idxFirst++];
			succs = digraph.getSuccessorsOf(x);
			for(y=succs.getFirstElement();y>=0;y=succs.getNextElement()){
				if(!in.get(y)){
					father[y] = x;
					list[idxLast++] = y;
					in.set(y);
					if(free.get(y)){
						return y;
					}
				}
			}
		}
		return -1;
	}

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
		ISet nei;
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
//		// is optimum found?
//		int i = 0;
//		in.clear();
//		for(int k=0;k<n;k++){
//			if(in.get(i)){
//				return;
//			}
//			in.set(i);
//			i = lineZero[i];
//		}
//		if(i!=0){
//			throw new UnsupportedOperationException();
//		}
//		for(int k=0;k<n;k++){
//			if(lineZero[k]==0){
//				g.enforceArc(k,n,this);
//			}else{
//				g.enforceArc(k,lineZero[k],this);
//			}
//		}
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

	public boolean contains(int i, int j){
		if(j==n){
			return lineZero[i] == 0;
		}
		return lineZero[i] == j;
	}

	@Override
	public double getReplacementCost(int i, int j){
		return 0;// do not know how to compute it
	}

	@Override
	public double getMarginalCost(int i, int j) {
		if(j==n){
			return costs[i][0];
		}
		return costs[i][j];
	}

	@Override
	public IGraph getSupport() {
		throw new UnsupportedOperationException("not implemented yet");
	}
}
