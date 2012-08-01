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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 29/03/12
 * Time: 13:47
 */

package solver.search.strategy;

import choco.kernel.common.util.PoolManager;
import samples.graph.DCMST;
import solver.constraints.propagators.gary.IRelaxation;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.assignments.GraphAssignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public enum TSP_heuristics {
	
	enf_MinDeg {
		public Decision getDecision(UndirectedGraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			INeighbors suc;
			int size = 2*n + 1;
			int sizi;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>1){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if(!g.getKernelGraph().arcExists(i,j)){
							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
							if (sizi < size) {
								size = sizi;
								to = j;
								from = i;
							}
						}
					}
				}
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,from, to, GraphAssignment.graph_enforcer);
			return fd;
		}
	},

	enf_sparse  {

		private int currentNode;
		
		private int[] e;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			INeighbors nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
				if(si<s && si>2){
					s = si;
				}
			}
			for (int i = 0; i < n; i++) {
				e[i] = 0;
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=0;j<n;j++){
					if(g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize()==s){
						e[i]++;
					}
				}
			}
			int bestScore = -1;
			int score;
			int node = -1;
			for (int i = 0; i < n; i++) {
				if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()==s){
					nei = g.getEnvelopGraph().getSuccessorsOf(i);
					score = 0;
					for(int j=0;j<n;j++){
						score += e[j];
					}
					if(score>bestScore){
						bestScore = score;
						node = i;
					}
				}
			}
			if(node == -1){
				throw new UnsupportedOperationException();
			}
			return node;
		}

		public Decision getDecision(UndirectedGraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).neighborhoodSize()==2){
				currentNode = getNextSparseNode(g,n);
			}
//			int currentNode = getNextSparseNode(g,n);
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
			int maxE = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!g.getKernelGraph().arcExists(currentNode,j)){
					if(maxE == -1 || e[maxE]<e[j]){
						maxE=j;
					}
				}
			}
			if(maxE==-1){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,currentNode, maxE, GraphAssignment.graph_enforcer);
			return fd;
		}

		public void init(UndirectedGraphVar g, int n){
			e = new int[n];
			currentNode = -1;
		}
	},

	enf_multisparse  {

		private int currentNode;

		private int[] e;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			INeighbors nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
				if(si<s && si!=g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()){
					s = si;
				}
			}
			for (int i = 0; i < n; i++) {
				e[i] = 0;
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=0;j<n;j++){
					if(nei.neighborhoodSize()!=g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()
					&& g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize()==s){
						e[i]++;
					}
				}
			}
			int bestScore = -1;
			int score;
			int node = -1;
			for (int i = 0; i < n; i++) {
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				if(nei.neighborhoodSize()!=g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()
					&& g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()==s){
					score = 0;
					for(int j=0;j<n;j++){
						score += e[j];
					}
					if(score>bestScore){
						bestScore = score;
						node = i;
					}
				}
			}
			if(node == -1){
				throw new UnsupportedOperationException();
			}
			return node;
		}

		public Decision getDecision(UndirectedGraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).neighborhoodSize() == g.getKernelGraph().getSuccessorsOf(currentNode).neighborhoodSize()){
				currentNode = getNextSparseNode(g,n);
			}
//			int currentNode = getNextSparseNode(g,n);
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
			int maxE = -1;
			int minCost = 0;
//			boolean lowCost = DCMST.solver.getMeasures().getSolutionCount()==0;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!g.getKernelGraph().arcExists(currentNode,j)){
//					boolean ok = (lowCost && DCMST.dist[currentNode][j]<minCost)
//							|| (DCMST.dist[currentNode][j]>minCost && !lowCost);
					if(maxE == -1 || e[maxE]<e[j]

					|| (e[maxE]==e[j] && DCMST.dist[currentNode][j]<minCost)
//					|| (e[maxE]==e[j] && ok)
						){
						maxE=j;
						minCost = DCMST.dist[currentNode][j];
					}
				}
			}
			if(maxE==-1){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,currentNode, maxE, GraphAssignment.graph_enforcer);
			return fd;
		}

		public void init(UndirectedGraphVar g, int n){
			e = new int[n];
			currentNode = -1;
		}
	};

	public abstract Decision getDecision(UndirectedGraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool);
	public void init(UndirectedGraphVar g, int n){}
}