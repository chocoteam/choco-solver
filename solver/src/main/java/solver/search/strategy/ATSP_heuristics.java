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
import solver.ICause;
import solver.constraints.propagators.gary.IRelaxation;
import solver.exception.ContradictionException;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.assignments.GraphAssignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;

public enum ATSP_heuristics {
	enf_MaxRepCost {
		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			INeighbors suc;
			double maxRepCost = -1;
			double repCost;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
						repCost = relax.getReplacementCost(i, j);
						if(repCost<0){
							throw new UnsupportedOperationException();
						}
						if (repCost > maxRepCost) {
							maxRepCost = repCost;
							to = j;
							from = i;
						}
					}
				}
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g, from, to, GraphAssignment.graph_enforcer);
			return fd;
		}
	},

	/** Heuristic introduced by Benchimol et. al.
	 */
	rem_MaxRepCost  {
		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			INeighbors suc;
			double maxRepCost = -1;
			double repCost;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if(relax.contains(i, j) && (!g.getKernelGraph().arcExists(i,j))){
						repCost = relax.getReplacementCost(i, j);
						if(repCost<0){
							System.out.println(repCost);
							throw new UnsupportedOperationException();
						}
						if (repCost > maxRepCost) {
							maxRepCost = repCost;
							to = j;
							from = i;
						}
					}
				}
			}
			if(from<0 || to==0){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,from, to, GraphAssignment.graph_remover);
			return fd;
		}
	},

	rem_MaxMargCost  {
		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			INeighbors suc;
			double maxRepCost = -1;
			double repCost;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if((!relax.contains(i, j)) && (!g.getKernelGraph().arcExists(i,j))){
						repCost = relax.getReplacementCost(i, j);
						if(repCost<0){
							System.out.println(repCost);
							throw new UnsupportedOperationException();
						}
						if (repCost > maxRepCost) {
							maxRepCost = repCost;
							to = j;
							from = i;
						}
					}
				}
			}
			if(from<0 || to==0){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,from, to, GraphAssignment.graph_remover);
			return fd;
		}
	},

	enf_MinDeg {
		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
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

	enf_MinDegMaxRepCost  {
		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			INeighbors suc;
			int size = 2*n + 1;
			int sizi;
			double repCost=0,repCostij;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>1){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if(relax.contains(i,j) &&!g.getKernelGraph().arcExists(i,j)){
							repCostij = relax.getReplacementCost(i, j);
							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
							if (sizi == size) {
								if(repCost<repCostij){
									repCost = repCostij;
									to = j;
									from = i;
								}
							}
							if (sizi < size) {
								size = sizi;
								to = j;
								from = i;
								repCost = repCostij;
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

	/** Heuristic introduced by Pesant et. al.
	 */
	sparse  {
		private int currentNode;
		private int[] e;

		private int getNextSparseNode(GraphVar g, int n) {
			INeighbors nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
				if(si<s && si>1){
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

		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).neighborhoodSize()==1){
				currentNode = getNextSparseNode(g,n);
			}
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
			if(nei.neighborhoodSize() > 5){
				int count = 0;
				for(int j=0;j<n;j++){
					if(g.getEnvelopGraph().arcExists(currentNode,j)){
						count++;
						if(count==nei.neighborhoodSize()/2){
							GraphDecision fd = pool.getE();
							if(fd==null){
								fd = new GraphDecision(pool);
							}
							fd.setArc(g,currentNode,j, GraphAssignment.graph_split);
							return fd;
						}
					}
				}
				throw new UnsupportedOperationException();
			}else{
				GraphDecision fd = pool.getE();
				if(fd==null){
					fd = new GraphDecision(pool);
				}
			fd.setArc(g,currentNode, nei.getFirstElement(), GraphAssignment.graph_enforcer);
				return fd;
			}
		}
		public void init(GraphVar g, int n){
			e = new int[n];
			currentNode = -1;
		}
	},

	enf_sparse  {
		private int currentNode;
		private int[] e;

		private int getNextSparseNode(GraphVar g, int n) {
			INeighbors nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
				if(si<s && si>1){
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

		public Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).neighborhoodSize()==1){
				currentNode = getNextSparseNode(g,n);
			}
//			int currentNode = getNextSparseNode(g,n);
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
			int maxE = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(maxE == -1 || e[maxE]<e[j]){
					maxE=j;
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
		public void init(GraphVar g, int n){
			e = new int[n];
			currentNode = -1;
		}
	};

	public abstract Decision getDecision(GraphVar g, int n, IRelaxation relax, PoolManager<GraphDecision> pool);
	public void init(GraphVar g, int n){}
}