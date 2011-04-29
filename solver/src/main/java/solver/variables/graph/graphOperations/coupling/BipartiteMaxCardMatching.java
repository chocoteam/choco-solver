package solver.variables.graph.graphOperations.coupling;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import solver.variables.graph.GraphTools;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

public class BipartiteMaxCardMatching {

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
		BitSet iterable = new BitSet(g.getNbNodes());
		ActiveNodesIterator<IActiveNodes> activeNodes = g.activeNodesIterator();
		int i;
		while(activeNodes.hasNext()){
			i = activeNodes.next();
			if (!g.getNeighborsOf(i).isEmpty()){
				iterable.set(i);
			}
		}
		int node = iterable.nextSetBit(0);
		while(node>=0){
			findAndSolveCCMatching(g,iterable,matching);
			node = iterable.nextSetBit(node);
		}
		return matching;
	}
	
	//***********************************************************************************
	// Bipartite Checking      O(m+n)
	//***********************************************************************************
	
	public static boolean isBipartite(IGraph g) {
		BitSet A = new BitSet(g.getNbNodes());
		BitSet B = new BitSet(g.getNbNodes());
		ActiveNodesIterator<IActiveNodes> activeNodes = g.activeNodesIterator();
		int i;
		while(activeNodes.hasNext()){
			i = activeNodes.next();
			if ((!A.get(i))&&(!B.get(i))){
				A.set(i);
				ActiveNodesIterator<IActiveNodes> succs = g.activeNodesIterator();
				int j;
				while(succs.hasNext()){
					j = succs.next();
					if(A.get(j))return false;
					B.set(j);
				}
			}else if (A.get(i)){
				ActiveNodesIterator<IActiveNodes> succs = g.activeNodesIterator();
				int j;
				while(succs.hasNext()){
					j = succs.next();
					if(A.get(j))return false;
					B.set(j);
				}
			}else {
				ActiveNodesIterator<IActiveNodes> succs = g.activeNodesIterator();
				int j;
				while(succs.hasNext()){
					j = succs.next();
					if(B.get(j))return false;
					A.set(j);
				}
			}			
		}
		return true;
	}
	
	//***********************************************************************************
	// Connected Components       O(m+n)
	//***********************************************************************************
	
	/**Find a connected component, compute its maximal cardinality bipartite matching and add
	 * edges of this matching in the matching of g
	 * @param g input graph
	 * @param iterable active nodes that have not been computed yet
	 * @param matching of g : list of edges in the maximal cardinality matching (no orientation taken into account)
	 */
	private static void findAndSolveCCMatching(IGraph g, BitSet iterable, LinkedList<int[]> matching){
		Object[] A_B_CC = findBipartiteCC(g, iterable);
		LinkedList<Integer> A = (LinkedList<Integer>) A_B_CC[0];
		LinkedList<Integer> B = (LinkedList<Integer>) A_B_CC[1];
		INeighbors CC = (INeighbors) A_B_CC[2];
		int n = CC.neighborhoodSize();
		IDirectedGraph newGraph = GraphTools.createSubgraph(g, CC);
		int[] oldIndexes = new int[n];
		AbstractNeighborsIterator<INeighbors> iter = CC.iterator();
		int i=0;
		while(iter.hasNext()){
			oldIndexes[i++] = iter.next();
		}
		// all edges are directed from A to B
		for (int a:A){
			newGraph.getPredecessorsOf(a).clear();
		}
		for (int b:B){
			newGraph.getSuccessorsOf(b).clear();
		}
		maxCardBipartiteMatching_HK(newGraph, A);
		INeighbors preds;
		int size;
		for (int a:A){
			preds = newGraph.getPredecessorsOf(a);
			size = preds.neighborhoodSize();
			if(size>1)throw new UnsupportedOperationException("not a matching");
			if(size==1){
				matching.add(new int[]{oldIndexes[a],oldIndexes[preds.getFirstElement()]});
			}
		}
	}
	
	/**Find a bipartite connected component
	 * @param g the graph
	 * @param iterable available nodes
	 * @return (A,B,V) with V = A+B is a bipartite CC 
	 */
	private static Object[] findBipartiteCC(IGraph g, BitSet iterable){
		int a,b;
		a = iterable.nextSetBit(0);
		if (a<0)return null;
		LinkedList<Integer> A = new LinkedList<Integer>();
		LinkedList<Integer> B = new LinkedList<Integer>();
		LinkedList<Integer> stackA = new LinkedList<Integer>();
		LinkedList<Integer> stackB = new LinkedList<Integer>();
		INeighbors CC = new BitSetNeighbors(g.getNbNodes());
		AbstractNeighborsIterator<INeighbors> iter;
		stackA.add(a);
		iterable.clear(a);		
		while((!stackA.isEmpty())||(!stackB.isEmpty())){
			if(!stackA.isEmpty()){
				a = stackA.pop();
				CC.add(a);
				A.add(a);
				iter = g.neighborsIteratorOf(a);
				while(iter.hasNext()){
					b = iter.next();
					if (iterable.get(b)){
						stackB.push(b);
						iterable.clear(b);
					}
				}
			}
			if(!stackB.isEmpty()){
				b = stackB.pop();
				CC.add(b); 
				B.add(b);
				iter = g.neighborsIteratorOf(b);
				while(iter.hasNext()){
					a = iter.next();
					if (iterable.get(a)){
						stackA.push(a);
						iterable.clear(a);
					}
				}
			}
		}
		return new Object[]{A,B,CC};
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
	// HOPCROFT & KARP algorithm       O(m.rac(n))
	//***********************************************************************************

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
