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
import choco.kernel.memory.IStateInt;
import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.search.strategy.assignments.GraphAssignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.variables.graph.ISet;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public enum TSP_heuristics {

	enf_MinDeg {
		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			ISet suc;
			int size = 2*n + 1;
			int sizi;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.getSize()>1){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if(!g.getKernelGraph().arcExists(i,j)){
							sizi = suc.getSize()+g.getEnvelopGraph().getPredecessorsOf(j).getSize();
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

	enf_sparse_CP12  {

		private int currentNode;

		private int[] e;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			ISet nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).getSize();
				if(si<s && si>2){
					s = si;
				}
			}
			for (int i = 0; i < n; i++) {
				e[i] = 0;
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=0;j<n;j++){
					if(g.getEnvelopGraph().getSuccessorsOf(j).getSize()==s){
						e[i]++;
					}
				}
			}
			int bestScore = -1;
			int score;
			int node = -1;
			for (int i = 0; i < n; i++) {
				if(g.getEnvelopGraph().getSuccessorsOf(i).getSize()==s){
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

		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).getSize()==2){
				currentNode = getNextSparseNode(g,n);
			}
//			int currentNode = getNextSparseNode(g,n);
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
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

	enf_sparse_corrected  {

		private int currentNode;

		private int[] e;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			ISet nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).getSize();
				if(si<s && si>2){
					s = si;
				}
			}
			for (int i = 0; i < n; i++) {
				e[i] = 0;
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(g.getEnvelopGraph().getSuccessorsOf(j).getSize()==s){
						e[i]++;
					}
				}
			}
			int bestScore = -1;
			int score;
			int node = -1;
			for (int i = 0; i < n; i++) {
				if(g.getEnvelopGraph().getSuccessorsOf(i).getSize()==s){
					nei = g.getEnvelopGraph().getSuccessorsOf(i);
					score = 0;
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
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

		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).getSize()==2){
				currentNode = getNextSparseNode(g,n);
			}
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
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

	enf_multisparse_corrected  {

		private int currentNode;

		private int[] e;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			ISet nei;
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).getSize();
				if(si<s && si>g.getKernelGraph().getSuccessorsOf(i).getSize()){
					s = si;
				}
			}
			for (int i = 0; i < n; i++) {
				e[i] = 0;
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(g.getEnvelopGraph().getSuccessorsOf(j).getSize()==s){
						e[i]++;
					}
				}
			}
			int bestScore = -1;
			int score;
			int node = -1;
			for (int i = 0; i < n; i++) {
				if(g.getEnvelopGraph().getSuccessorsOf(i).getSize()==s){
					nei = g.getEnvelopGraph().getSuccessorsOf(i);
					score = 0;
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
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

		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).getSize()==
					g.getKernelGraph().getSuccessorsOf(currentNode).getSize()){
				currentNode = getNextSparseNode(g,n);
			}
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
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

	enf_node_arc_tests  {

		private int currentNode;
		private IGraphRelaxation relax;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			return minKminE(g,n);
		}

		private int nextDCMST(UndirectedGraphVar g, int n){
			ISet nei;
			int node = -1;
			int minDelta = 5*n;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
							if(2-k<minDelta){
								minDelta = 2-k;
								node = i;
							}
						}
					}
			}
			if(node == -1){
				minDelta = 5*n;
				for(int i=0;i<n;i++){
					nei = g.getEnvelopGraph().getNeighborsOf(i);
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k)
						for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
							if(!g.getKernelGraph().arcExists(i,j)){
								if(2-k<minDelta){
									minDelta = 2-k;
									node = i;
								}
							}
						}
				}
				int minDeg = 0;
				for(int i=0;i<n;i++){
					nei = g.getEnvelopGraph().getNeighborsOf(i);
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					if(nei.getSize()!=k && 2-k==minDelta)
						for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
							if(!g.getKernelGraph().arcExists(i,j)){
								int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
										+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
								if(d>minDeg){
									minDeg = d;
									node = i;
								}
							}
						}
				}
			}else{
				int minDeg = 0;
				for(int i=0;i<n;i++){
					nei = g.getEnvelopGraph().getNeighborsOf(i);
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					if(nei.getSize()!=k && 2-k==minDelta)
						for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
							if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
								int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
										+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
								if(d>minDeg){
									minDeg = d;
									node = i;
								}
							}
						}
				}
			}
			if(node == -1){
				throw new UnsupportedOperationException();
			}
			return node;
		}

		private int minKminE(UndirectedGraphVar g, int n){
			ISet nei;
			int node = -1;
			int minK = 0;//5*n;
			for(int i=0;i<n;i++){
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k && k>minK){
					minK = k;
					node = i;
				}
			}
//			int minK = 5*n;
//			for(int i=0;i<n;i++){
//				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
//				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k && k<minK){
//					minK = k;
//					node = i;
//				}
//			}
//			int minE = 0;
//			for(int i=0;i<n;i++){
//				nei = g.getEnvelopGraph().getNeighborsOf(i);
//				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
//				if(nei.getSize()!=k && k==minK)
//					if(nei.getSize()>minE){
//						minE = nei.getSize();
//						node = i;
//					}
//			}
			int minE = 5*n;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(nei.getSize()!=k && k==minK)
					if(nei.getSize()<minE){
						minE = nei.getSize();
						node = i;
					}
			}
			if(node == -1){
				throw new UnsupportedOperationException();
			}
			return node;
		}

		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			if(this.relax==null){
				this.relax = relax;
			}
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).getSize()==2){
				currentNode = getNextSparseNode(g,n);
			}
			double minDeg = 5*n;
			int i = currentNode;
			ISet nei = g.getEnvelopGraph().getNeighborsOf(i);
			int next = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if((relax.contains(i,j)) && !g.getKernelGraph().arcExists(i,j)){
//					int d = g.getEnvelopGraph().getNeighborsOf(j).getSize();
					double d = relax.getReplacementCost(i,j);
					if(next == -1 || d<minDeg){
						minDeg = d;
						next = j;
					}
				}
			}
			if(next == -1){
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().arcExists(i,j)){
						int d = g.getEnvelopGraph().getNeighborsOf(j).getSize();
						if(next == -1 || d<minDeg){
							minDeg = d;
							next = j;
						}
					}
				}
			}
			if(next==-1){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,currentNode, next, GraphAssignment.graph_enforcer);
			return fd;
		}

		public void init(UndirectedGraphVar g, int n){
			currentNode = -1;
			relax = null;
		}
	},

	enf_sparse_dcmst_state  {

		private IStateInt currentNode;
		private IGraphRelaxation relax;

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			ISet nei;
			int node = -1;
			int minDelta = 5*n;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
							if(2-k<minDelta){
								minDelta = 2-k;
								node = i;
							}
						}
					}
			}
			if(node == -1){
				minDelta = 5*n;
				for(int i=0;i<n;i++){
					nei = g.getEnvelopGraph().getNeighborsOf(i);
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k)
						for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
							if(!g.getKernelGraph().arcExists(i,j)){
								if(2-k<minDelta){
									minDelta = 2-k;
									node = i;
								}
							}
						}
				}
				int minDeg = 0;
				for(int i=0;i<n;i++){
					nei = g.getEnvelopGraph().getNeighborsOf(i);
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					if(nei.getSize()!=k && 2-k==minDelta)
						for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
							if(!g.getKernelGraph().arcExists(i,j)){
								int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
										+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
								if(d>minDeg){
									minDeg = d;
									node = i;
								}
							}
						}
				}
			}else{
				int minDeg = 0;
				for(int i=0;i<n;i++){
					nei = g.getEnvelopGraph().getNeighborsOf(i);
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					if(nei.getSize()!=k && 2-k==minDelta)
						for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
							if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
								int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
										+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
								if(d>minDeg){
									minDeg = d;
									node = i;
								}
							}
						}
				}
			}
			if(node == -1){
				throw new UnsupportedOperationException();
			}
			return node;
		}

		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			if(this.relax==null){
				this.relax = relax;
			}
			if (currentNode.get()==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode.get()).getSize()==2){
				currentNode.set(getNextSparseNode(g,n));
			}
			int minDeg = 0;
			int i = currentNode.get();
			ISet nei = g.getEnvelopGraph().getNeighborsOf(i);
			int next = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
					int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
							+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
					if(d>minDeg){
						minDeg = d;
						next = j;
					}
				}
			}
			if(next == -1){
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().arcExists(i,j)){
						int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
								+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
						if(d>minDeg){
							minDeg = d;
							next = j;
						}
					}
				}
			}
			if(next==-1){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,currentNode.get(), next, GraphAssignment.graph_enforcer);
			return fd;
		}

		public void init(UndirectedGraphVar g, int n){
			currentNode = g.getSolver().getEnvironment().makeInt(-1);
			relax = null;
		}
	},

	enf_DCMST  {

		public Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool) {
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			ISet nei;
			int minDelta = 5*n;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
							if(2-k<minDelta){
								minDelta = 2-k;
							}
						}
					}
			}
			int minDeg = 0;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(nei.getSize()!=k && 2-k==minDelta)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(relax.contains(i,j) && !g.getKernelGraph().arcExists(i,j)){
							int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
									+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
							if(d>minDeg){
								minDeg = d;
								fd.setArc(g,i,j, GraphAssignment.graph_enforcer);
							}
						}
					}
			}
			return fd;
		}

		public void init(UndirectedGraphVar g, int n){}
	};

	public abstract Decision getDecision(UndirectedGraphVar g, int n, IGraphRelaxation relax, PoolManager<GraphDecision> pool);
	public void init(UndirectedGraphVar g, int n){}
}