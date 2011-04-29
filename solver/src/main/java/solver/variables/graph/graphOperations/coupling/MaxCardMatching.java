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

package solver.variables.graph.graphOperations.coupling;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import solver.variables.graph.GraphTools;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

public class MaxCardMatching {

	//***********************************************************************************
	// MAIN ALGORITHM       O(m.rac(n))
	//***********************************************************************************
	
	/** Find a maximal cardinality matching in g
	 * uses Hopcroft & Krap algorithm which runs in O(m.rac(n)) time
	 * on connected components. thus the complexity is O(SUM(m_i.rac(n_i)) where n_i is the number of nodes in CC_i and m_i its number of edges 
	 * @param g input graph considered as undirected whether it is undirected or not
	 * @return a maximal cardinality matching of g : a list of edges or null if g is not bipartite
	 */
	public static LinkedList<int[]> maxCardBipartiteMatching(IGraph g){
		LinkedList<int[]> matching = new LinkedList<int[]>();
		for (INeighbors cc:ConnectivityFinder.findAllCConly(g).getConnectedComponents()){
			findAndSolveCCMatching(g,cc,matching);
		}
		return matching;
	}
	
	//***********************************************************************************
	// Connected Components       O(m+n)
	//***********************************************************************************
	
	/**Find a connected component, compute its maximal cardinality bipartite matching and add
	 * edges of this matching in the matching of g
	 * @param g input graph
	 * @param cc the connected component considered
	 * @param matching of g : list of edges in the maximal cardinality matching (no orientation taken into account)
	 */
	private static void findAndSolveCCMatching(IGraph g, INeighbors cc, LinkedList<int[]> matching){
		//create a graph to work on
		int n = cc.neighborhoodSize();
		IDirectedGraph GG = new DirectedGraph(n*2, g.getType());//should be sparse (matching...)
		int[] oldIndexes = new int[n];
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		AbstractNeighborsIterator<INeighbors> iter = cc.iterator();
		int i;
		int idx=0;
		while(iter.hasNext()){
			i = iter.next();
			oldIndexes[idx] = i;
			map.put(i, idx);
			idx++;
		}
		iter = cc.iterator();
		idx=0;
		while(iter.hasNext()){
			AbstractNeighborsIterator<INeighbors> neighbors = g.neighborsIteratorOf(iter.next());
			int j=0;
			while(neighbors.hasNext()){
				j = neighbors.next();
				if (map.get(j)!=null){
					GG.addArc(idx, map.get(j));
				}
			}
			idx++;
		}
		//run algo
		maxCardBipartiteMatching_JG_simple(GG, n);
		//get results
		INeighbors succs;
		int size;
		for (int a=n; a<n*2; a++){//matched nodes 
			succs = GG.getSuccessorsOf(a);
			size = succs.neighborhoodSize();
			if(size>1)throw new UnsupportedOperationException("error in matching");
			if(size==1){
				matching.add(new int[]{oldIndexes[a-n],oldIndexes[succs.getFirstElement()]});
			}
		}
	}
	
	//***********************************************************************************
	// Greedy Heuristic       O(m)
	//***********************************************************************************

	/**Affect some edges to the matching with a greedy heuristic
	 * @param g directed bipartied graph g=((A,B),U)
	 * @param A subset of nodes
	 * @param free nodes
	 */
	private static void greedyHeuristic(IDirectedGraph g, LinkedList<Integer> A, boolean[] free){
		int j;
		for(int i:A){
			AbstractNeighborsIterator<INeighbors> iter = g.successorsIteratorOf(i);
			while(iter.hasNext()){
				j = iter.next();
				if(free[j]){
					g.removeArc(i, j);
					g.addArc(j, i);
					free[j] = false;
					free[i] = false;
					break;
				}
			}
		}
	}
	
	//***********************************************************************************
	// JG algorithm       O(m.rac(n))
	//***********************************************************************************

	private static void maxCardBipartiteMatching_JG_opt(IDirectedGraph gG, int n) {
		// TODO Auto-generated method stub
		Exception e = new Exception();
		e.printStackTrace();
		System.exit(0);
		
	}
	
	//***********************************************************************************
	// JG algorithm       O(m.n)
	//***********************************************************************************

	private static void maxCardBipartiteMatching_JG_simple(IDirectedGraph g, int n) {
		BitSet free = new BitSet(n);
		for(int i=0;i<n;i++){
			free.set(i);
		}
//		greedyHeuristic(g, n, free);
		for(int f=free.nextSetBit(0); f>=0; f = free.nextSetBit(f+1)){
			tryToConnect(g,n,f, free);
		}
	}
	
	private static void tryToConnect(IDirectedGraph g, int n, int f, BitSet free) {
		int[] parents = new int[n*2];
		BitSet reached = new BitSet(2*n);
		AbstractNeighborsIterator<INeighbors> succs;
		LinkedList<Integer> stack = new LinkedList<Integer>();
		reached.set(f);
		parents[f] = f;
		stack.push(f);
		int a,s;
		int b = -1;
		while(!stack.isEmpty()){
			a = stack.pop();
			succs = g.successorsIteratorOf(a);
			while(succs.hasNext()){
				s = succs.next();
				if(s<n && free.get(s)){// chemin amŽliorant trouvŽ
					stack.clear();
					b = s;
				}
				if(!reached.get(s)){
					parents[s] = a;
					reached.set(s);
					stack.push(s);
				}
			}
		}
		if(b==-1){ // no augmenting path
			
		}else{
			
		}
	}

	/**Find a maximum cardinality matching in the biparted digraph g
	 * This algorithm runs in O(m.rac(n)) time cf LEDA
	 * @param g a directed bipartite graph g=((A,B),U)
	 * @param A set of nodes
	 * initially all edges are oriented from A to B
	 * at the end of the procedure the matching is the set of arcs from B to A
	 */
	private static void maxCardBipartiteMatching_HK(IDirectedGraph g, LinkedList<Integer> A){
		int n = g.getNbNodes();
		//perform greedy heuristic OPTIONAL
		boolean[] free = new boolean[n];
		for(int i=0;i<n;i++){
			free[i]=true;
		}
		greedyHeuristic(g, A, free);
		// data
		BitSet freeInA = new BitSet(A.size());
		for(int i:A){
			if(free[i]){
				freeInA.set(i, true);
			}
		}
		// one value per edge e=(u,v). Key(e) = min(u,v)*n + max(u,v) 
		HashMap<Integer, Integer> useful = new HashMap<Integer, Integer>();
		int[] dist = new int[n];
		int[] reached = new int[n];
		int phaseNumber = 1;
		while(augmentingPathExists(g,freeInA, n, free, useful, dist, reached, phaseNumber)){ // at most O(rac(n)) iterations
			findMaxSetAndAugment(g,freeInA, n, free, useful, dist, reached, phaseNumber);    // run in O(m)
			phaseNumber++;
		}
	}
	
	/**Find whether an augmenting path exists or not
	 * @param g directed bipartite graph
	 * @param freeInA free nodes in A
	 * @param n number of nodes
	 * @param free free nodes
	 * @param useful 
	 * @param dist
	 * @param reached
	 * @param phaseNumber
	 * @return true iff an augmentingPath has been found
	 */
	private static boolean augmentingPathExists(IDirectedGraph g, BitSet freeInA, int n, boolean[] free, HashMap<Integer, Integer> useful, int[] dist,int[] reached, int phaseNumber) {
		if(freeInA.size()==0)return false;
		LinkedList<Integer> stack = new LinkedList<Integer>();
		//nodes
		int v,w;
		int dv;
		//edge
		int e;
		for (v=freeInA.nextSetBit(0); v>=0; v = freeInA.nextSetBit(v+1)){
			stack.addFirst(v);
			dist[v] = 0; reached[v] = phaseNumber;
		}
		boolean augmentingPathFound = false;
		while(!stack.isEmpty()){
			v = stack.removeFirst();
			dv = dist[v];
			AbstractNeighborsIterator<INeighbors> iter = g.successorsIteratorOf(v);
			while(iter.hasNext()){
				w = iter.next();
				if(reached[w] != phaseNumber){
					dist[w] = dv+1; reached[w] = phaseNumber;
					if(free[w])augmentingPathFound = true;
					if(!augmentingPathFound)stack.addFirst(w);
				}
				if(dist[w]==dv+1){
					e = Math.min(v, w)*n+Math.max(v, w);
					useful.put(e, phaseNumber);
				}
			}
		}
		return augmentingPathFound;
	}

	/**Perform the maximum of node disjoint paths (and of restricted length)
	 * @param g bipartite graph
	 * @param freeInA collection of free nodes in A
	 * @param n number of nodes
	 * @param free free nodes
	 * @param useful edges in the layer
	 * @param dist 
	 * @param reached
	 * @param phaseNumber
	 */
	private static void findMaxSetAndAugment(IDirectedGraph g, BitSet freeInA,int n, boolean[] free, HashMap<Integer, Integer> useful,int[] dist, int[] reached, int phaseNumber) {
		// not edges but nodes (predecessors)
		int[] pred = new int[n];
		for(int i=0;i<n;i++){
			pred[i] = -1;
		}
		// not edges but nodes (path)
		LinkedList<Integer> lasts = new LinkedList<Integer>();
		int origin,w,e,last;
		for (origin=freeInA.nextSetBit(0); origin>=0; origin = freeInA.nextSetBit(origin+1)){
			AbstractNeighborsIterator<INeighbors> iter = g.successorsIteratorOf(origin);
			while(iter.hasNext()){
				w = iter.next();
				e = Math.min(origin, w)*n+Math.max(origin, w);
				if(pred[w]==-1 && useful.get(e) == phaseNumber){
					last = findAugmentingPath(g,origin,w,n,free,pred,useful,phaseNumber);
					if (last!=-1){ //an augmenting path starting with (v,w) exists 
						lasts.addFirst(last);
					}
				}
			}
			while(!lasts.isEmpty()){
				w = lasts.removeFirst();
				free[w] = false;
				while(pred[w]!=-1){
					g.removeArc(pred[w], w);
					g.addArc(w, pred[w]);
					w = pred[w];
				}
				free[w] = false;
				freeInA.clear(w);
			}
		}
	}

	/** Find an augmenting path going using edge (v,w). 
	 * The path must be vertex disjoint from previously computed paths
	 * @param g bipartite directed graph
	 * @param v node to visit
	 * @param w node to visit : arc (v,w) is used
	 * @param n number of nodes
	 * @param free free nodes
	 * @param pred predecessors of nodes in the path (trail)
	 * @param useful 
	 * @param phaseNumber
	 * @return x if an augmenting path has been found and end in vertex x (free in B), -1 otherwise
	 */
	private static int findAugmentingPath(IDirectedGraph g, int v, int w, int n, boolean[] free, int[] pred, HashMap<Integer, Integer> useful, int phaseNumber) {
		pred[w] = v;
		if(free[w]) return w;
		int e;
		int rec;
		AbstractNeighborsIterator<INeighbors> iter = g.successorsIteratorOf(w);
		while(iter.hasNext()){
			v = iter.next();
			e = Math.min(v, w)*n+Math.max(v, w);
			if(pred[v]==-1 && useful.get(e)==phaseNumber){
				rec = findAugmentingPath(g, w, v, n, free, pred, useful, phaseNumber);
				if(rec!=-1){
					return rec;
				}
			}
		}
		return -1;
	}
}
