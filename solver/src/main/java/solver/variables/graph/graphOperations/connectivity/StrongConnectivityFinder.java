package solver.variables.graph.graphOperations.connectivity;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Stack;
import solver.variables.graph.GraphTools;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;

public class StrongConnectivityFinder {


	/**Find strongly connected components, strong articulation points and strong bridges of graph
	 * in O(m.alpha(m,n)) time
	 * @param graph
	 * @return a StrongConnectivityObject that will store SCC, SAP and SB
	 */
	public static StrongConnectivityObject findAll(IDirectedGraph graph){
		return findAllSAPandSB(graph, findAllSCC(graph));
	}

	//***********************************************************************************
	// STRONG CONNECTED COMPONENTS
	//***********************************************************************************

	/**Test if graph is strongly connected in O(m+n)
	 * @param graph a directed graph
	 * @return true iff graph is strongly connected
	 */
	public static boolean isStronglyConnected(IDirectedGraph graph) {
		LinkedList<INeighbors> allSCC = new LinkedList<INeighbors>();
		Stack<Integer> stack = new Stack<Integer>();
		int nb = graph.getNbNodes();
		int[] parent = new int[nb];
		int[] num = new int[nb];
		int[] inf = new int[nb];
		boolean[] inStack = new boolean[nb];
		AbstractNeighborsIterator<INeighbors>[] successors = new AbstractNeighborsIterator[nb];
		BitSet notOpenedYet = new BitSet(nb);
		ActiveNodesIterator<IActiveNodes> iter = graph.activeNodesIterator();
		int i;
		while (iter.hasNext()){
			i = iter.next();
			notOpenedYet.set(i);
			inf[i] = Integer.MAX_VALUE;
			parent[i] = -1;
		}	
		int first = 0;
		first = notOpenedYet.nextSetBit(first);
		if (first<0){
			throw new UnsupportedOperationException();
		}
		findSCC(stack, inStack, allSCC, graph, successors, first, parent, num, inf, notOpenedYet);
		first = notOpenedYet.nextSetBit(first);
		return first<0;
	}

	/**Get all strongly connected components of a directed graph in O(m+n)
	 * @param graph has to be a directed graph
	 * @return allSCC a linkedList containing CFC of graph
	 */
	public static LinkedList<INeighbors> findAllSCC(IDirectedGraph graph){
		LinkedList<INeighbors> allSCC = new LinkedList<INeighbors>();
		Stack<Integer> stack = new Stack<Integer>();
		int nb = graph.getNbNodes();
		int[] parent = new int[nb];
		int[] num = new int[nb];
		int[] inf = new int[nb];
		boolean[] inStack = new boolean[nb];
		AbstractNeighborsIterator<INeighbors>[] successors = new AbstractNeighborsIterator[nb];
		BitSet notOpenedYet = new BitSet(nb);
		ActiveNodesIterator<IActiveNodes> iter = graph.activeNodesIterator();
		int i;
		while (iter.hasNext()){
			i = iter.next();
			notOpenedYet.set(i);
			inf[i] = Integer.MAX_VALUE;
			parent[i] = -1;
		}	
		int first = 0;
		first = notOpenedYet.nextSetBit(first);
		while(first>=0){
			findSCC(stack, inStack, allSCC, graph, successors, first, parent, num, inf, notOpenedYet);
			first = notOpenedYet.nextSetBit(first);
		}
		return allSCC;
	}

	/**Find some SCC exploring graph in a dfs order from start
	 * @param stack
	 * @param inStack
	 * @param allSCC
	 * @param graph
	 * @param successors
	 * @param start
	 * @param p
	 * @param num
	 * @param inf
	 * @param notOpenedYet
	 */
	private static void findSCC(Stack<Integer> stack, boolean[] inStack, LinkedList<INeighbors> allSCC, IDirectedGraph graph, 
			AbstractNeighborsIterator<INeighbors>[] successors, int start, int[] p, int[] num, int[] inf, BitSet notOpenedYet){
		int i = start;
		int k = 1;
		num[start] = 1;
		stack.push(start);
		inStack[start] = true;
		p[start] = start;
		successors[start] = graph.neighborsIteratorOf(start);
		int j = 0;
		while((i!=start) || successors[i].hasNext()){
			if(!successors[i].hasNext()){
				if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
				if (inf[i] >= num[i]){
					INeighbors scc = new IntLinkedList();
					int y;
					do{
						y = stack.pop();
						inStack[y] = false;
						notOpenedYet.clear(y);
						scc.add(y);
					}while(y!=i);
					allSCC.add(scc);
				}
				inf[p[i]] = Math.min(inf[p[i]], inf[i]);
				i = p[i];
			}else{
				j = successors[i].next();
				if (p[j]==-1) {
					p[j] = i;
					i = j;
					successors[i] = graph.neighborsIteratorOf(i);
					stack.push(i);
					inStack[i] = true;
					k++;
					num[i] = k;
					inf[i] = num[i];
				}else if(inStack[j]){
					inf[i] = Math.min(inf[i], num[j]);
				}
			}
		}
		if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
		if (inf[i] >= num[i]){
			INeighbors scc = new IntLinkedList();
			int y;
			do{
				y = stack.pop();
				inStack[y] = false;
				notOpenedYet.clear(y);
				scc.add(y);
			}while(y!=i);
			allSCC.add(scc);
		}
	}

	//***********************************************************************************
	// STRONG ARTICULATION POINTS AND STRONG BRIDGES
	//***********************************************************************************

	/**Find all strong articulation points of a graph knowing its strongly connected components
	 * Uses Tarjan Lengauer algorithm that runs in O(m.alpha(m,n)+n)
	 * 
	 * @param graph
	 * @param allSCC graphs' strongly connected components
	 * @return
	 */
	public static StrongConnectivityObject findAllSAPandSB(IDirectedGraph graph, LinkedList<INeighbors> allSCC){
		BitSet allSAP = new BitSet(graph.getNbNodes());
		LinkedList<int[]> allSB =  new LinkedList<int[]>();
		for (INeighbors scc:allSCC){
			findSAP(allSAP, allSB, graph, scc);
		}
		return new StrongConnectivityObject(allSCC,allSAP,allSB);
	}

	/**Find and add all strong articulation points of a strongly connected component scc of a directed graph
	 * Uses Tarjan Lengauer algorithm that runs in O(m.alpha(m,n)+n)
	 * 
	 * @param graphSAP bitset containing all SAP of the graph 
	 * @param strongBridges the list of strong bridges of graph
	 * @param graph the studied directed graph
	 * @param scc the focused strongly connected component of graph
	 */
	private static void findSAP(BitSet graphSAP, LinkedList<int[]> strongBridges, IDirectedGraph graph, INeighbors scc) {
		if (scc.neighborhoodSize()<3){
			return; // trivial case
		}
		int size = scc.neighborhoodSize();
		AbstractNeighborsIterator<INeighbors> iter = scc.iterator();
		int[] indexOf = new int[size];
		int y=0;
		while(iter.hasNext()){
			indexOf[y++] = iter.next();
		}
		int root = indexOf[0];
		BitSet sccSAP = new BitSet(size);
		// dominators of the reversed flowgraph rooted in root
		IDirectedGraph flowG = GraphTools.createSubgraph(graph, scc, false, false);
		FlowGraphManager domFG = new FlowGraphManager(0, flowG);
		sccSAP.or(domFG.getImmediateNonTrivialDominators());
		// dominators of the reversed flowgraph rooted in root
		IDirectedGraph flowGR = GraphTools.createSubgraph(graph, scc, false, true);
		FlowGraphManager domFGR = new FlowGraphManager(0, flowGR);
		sccSAP.or(domFGR.getImmediateNonTrivialDominators());
		sccSAP.clear(0); // just in case (but should not be useful)
		// the considered root
		if(isRootSAP(root,scc,graph)){
			sccSAP.set(0);
		}
		// sap and strong bridges
		for (y=sccSAP.nextSetBit(0); y>=0; y=sccSAP.nextSetBit(y+1)){
			graphSAP.set(indexOf[y]);
			int x = domFG.getParentOf(y);
			if (sccSAP.get(x)){ // x and y are both SAP
				if (domFG.isFlowBridge(x,y)){
					strongBridges.add(new int[]{indexOf[x],indexOf[y]});
				}
			}
			int xR = domFGR.getParentOf(y);
			if(sccSAP.get(xR)){
				if (domFGR.isFlowBridge(xR,y)){
					strongBridges.add(new int[]{indexOf[y],indexOf[xR]});
				}
			}
		}
	}

	/**Test whether root is a SAP or not considering cfc
	 * performed in O(m+n) where n and m are respectively the number of nodes and arcs of cfc (not graph)
	 * @param root 
	 * @param cfc studied connected component of graph
	 * @param graph studied graph
	 * @return true iff root is a SAP in cfc
	 */
	private static boolean isRootSAP(int root, INeighbors cfc, IDirectedGraph graph) {
		// subgraph induced by cfc\root
		IDirectedGraph subgraph = GraphTools.createSubgraph(graph, cfc, true, false);
		return !isStronglyConnected(subgraph);
	}
}
