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

package solver.variables.graph.graphOperations.connectivity;

import gnu.trove.TIntArrayList;
import java.util.ArrayList;
import java.util.BitSet;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;

public class StrongConnectivityFinder {

	/**find all strongly connected components of the partial subgraph of graph 
	 * uses Tarjan algorithm 
	 * run in O(n+m) worst case time
	 * @param graph the input graph
	 * @return all strongly connected components of the partial subgraph of graph
	 */
	public static ArrayList<TIntArrayList> findAllSCCOf(IDirectedGraph graph){
		int n = graph.getNbNodes();
		BitSet bitSCC = new BitSet(n);
		for (int i = graph.getActiveNodes().getFirstElement(); i>=0; i = graph.getActiveNodes().getNextElement()) {
			bitSCC.set(i);
		}
		return findAllSCCOf(graph, bitSCC);
	}
	
	/**find all strongly connected components of the partial subgraph of graph defined by restriction
	 * uses Tarjan algorithm 
	 * run in O(n+m) worst case time
	 * @param graph the input graph
	 * @param restriction provide a restriction : nodes set to 0 (false) will be ignored
	 * @return all strongly connected components of the partial subgraph of graph defined by restriction
	 */
	public static ArrayList<TIntArrayList> findAllSCCOf(IDirectedGraph graph, BitSet restriction){
		int nb = restriction.cardinality();
		int[] stack = new int[nb];
		int[] p = new int[nb];
		int[] inf = new int[nb];
		int[] nodeOfDfsNum = new int[nb];
		int[] dfsNumOfNode = new int[graph.getNbNodes()];
		BitSet inStack = new BitSet(nb);
		ArrayList<TIntArrayList> allSCC = new ArrayList<TIntArrayList>(nb);
		findSingletons(graph, restriction, allSCC);
		int first = restriction.nextSetBit(0);
		while(first>=0){
			findSCC(allSCC, graph, first, restriction,
					stack, p, inf, nodeOfDfsNum, dfsNumOfNode, inStack);
			first = restriction.nextSetBit(first);
		}
		return allSCC;
	}
	
	/**Find singletons
	 * run in O(n)
	 * @param graph
	 * @param iterable
	 * @param allSCC
	 */
	private static void findSingletons(IDirectedGraph graph, BitSet iterable, ArrayList<TIntArrayList> allSCC){
		for(int i=iterable.nextSetBit(0); i>=0; i=iterable.nextSetBit(i+1)){
			if(graph.getPredecessorsOf(i).neighborhoodSize()*graph.getSuccessorsOf(i).neighborhoodSize()==0){
				TIntArrayList scc = new TIntArrayList();
				scc.add(i);
				allSCC.add(scc);
				iterable.clear(i);
			}
		}
	}

	/** find all strongly connected components of the partial subgraph of graph defined by restriction reachable from the node start, and add them to the set allSCC
	 * uses Tarjan algorithm 
	 * run in O(n+m) worst case time
	 *  
	 * @param allSCC the set of all scc of graph
	 * @param graph the input graph
	 * @param start the node to start the search at
	 * @param restriction provide a restriction : nodes set to 0 (false) will be ignored
	 * @param stack 
	 * @param p
	 * @param inf
	 * @param nodeOfDfsNum
	 * @param dfsNumOfNode
	 * @param inStack
	 */
	private static void findSCC(ArrayList<TIntArrayList> allSCC, IDirectedGraph graph, int start, BitSet restriction,int[] stack, int[] p, int[] inf, int[] nodeOfDfsNum, int[] dfsNumOfNode, BitSet inStack){
		int nb = restriction.cardinality();
		// trivial case
		if (nb==1){
			TIntArrayList scc = new TIntArrayList();
			scc.add(start);
			allSCC.add(scc);
			restriction.clear(start);
			return;
		}
		//initialization
		int stackIdx= 0;
		INeighbors[] successors = new INeighbors[nb];
		BitSet notFirsts = new BitSet(nb);
		for(int m=0;m<nb;m++){
			inf[m] = nb+2;
		}	
		int k = 0;
		int i = k;
		dfsNumOfNode[start]=k;
		nodeOfDfsNum[k] = start;
		stack[stackIdx++] = i;
		inStack.set(i);
		p[k] = k;
		successors[k] = graph.getSuccessorsOf(start);
		int j = 0;
		// algo
		boolean notFinished = true;
		while(notFinished){
			if(notFirsts.get(i)){
				j = successors[i].getNextElement();
			}else{
				j = successors[i].getFirstElement();
				notFirsts.set(i);
			}
			if(j>=0){
				if(restriction.get(j)){
					if (dfsNumOfNode[j]==0 && j!=start) {
						k++;
						nodeOfDfsNum[k] = j;
						dfsNumOfNode[j] = k;
						p[k] = i;
						i = k;
						successors[i] = graph.getSuccessorsOf(j);
						stack[stackIdx++] = i;
						inStack.set(i);
						inf[i] = i;
					}else if(inStack.get(dfsNumOfNode[j])){
						inf[i] = Math.min(inf[i], dfsNumOfNode[j]);
					}
				}
			}else{
				if(i==0){
					notFinished = false;
					break;
				}
				if (inf[i] >= i){
					TIntArrayList scc = new TIntArrayList();
					int y,z;
					do{
						z = stack[--stackIdx];
						inStack.clear(z);
						y = nodeOfDfsNum[z];
						restriction.clear(y);
						scc.add(y);
					}while(z!= i);
					allSCC.add(scc);
				}
				inf[p[i]] = Math.min(inf[p[i]], inf[i]);
				i = p[i];
			}
		}
		if (inStack.cardinality()>0){
			TIntArrayList scc = new TIntArrayList();
			int y;
			do{
				y = nodeOfDfsNum[stack[--stackIdx]];
				restriction.clear(y);
				scc.add(y);
			}while(y!= start);
			allSCC.add(scc);
		}
	}
	

	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		DirectedGraph dig = new DirectedGraph(5, GraphType.LINKED_LIST);
		dig.addArc(0, 1);
		dig.addArc(2, 3);
		dig.addArc(1, 0);
		dig.addArc(3, 2);
		dig.addArc(0, 3);
		dig.addArc(2, 1);
		dig.addArc(4, 3);
		dig.addArc(4, 1);
		System.out.println(dig);
		System.out.println(StrongConnectivityFinder.findAllSCCOf(dig));
	}
	
	
	
	
	
	
	
	
	
	
	
//	/**Find strongly connected components, strong articulation points and strong bridges of graph
//	 * in O(m.alpha(m,n)) time
//	 * @param graph
//	 * @return a StrongConnectivityObject that will store SCC, SAP and SB
//	 */
//	public static StrongConnectivityObject findAll(IDirectedGraph graph){
//		return findAllSAPandSB(graph, findAllSCC(graph));
//	}

	//***********************************************************************************
	// STRONG CONNECTED COMPONENTS
	//***********************************************************************************

//	/**Test if graph is strongly connected in O(m+n)
//	 * @param graph a directed graph
//	 * @return true iff graph is strongly connected
//	 */
//	public static boolean isStronglyConnected(IDirectedGraph graph) {
//		LinkedList<INeighbors> allSCC = new LinkedList<INeighbors>();
//		TIntStack stack = new TIntStack();
//		int nb = graph.getNbNodes();
//		int[] parent = new int[nb];
//		int[] num = new int[nb];
//		int[] inf = new int[nb];
//		BitSet inStack = new BitSet(nb);
//		AbstractNeighborsIterator<INeighbors>[] successors = new AbstractNeighborsIterator[nb];
//		BitSet notOpenedYet = new BitSet(nb);
//		ActiveNodesIterator<IActiveNodes> iter = graph.activeNodesIterator();
//		int i;
//		while (iter.hasNext()){
//			i = iter.next();
//			notOpenedYet.set(i);
//			inf[i] = Integer.MAX_VALUE;
//			parent[i] = -1;
//		}	
//		int first = 0;
//		first = notOpenedYet.nextSetBit(first);
//		if (first<0){
//			throw new UnsupportedOperationException();
//		}
//		findSCC(stack, inStack, allSCC, graph, successors, first, parent, num, inf, notOpenedYet);
//		first = notOpenedYet.nextSetBit(first);
//		return first<0;
//	}

//	/**Get all strongly connected components of a directed graph in O(m+n)
//	 * @param graph has to be a directed graph
//	 * @return allSCC a linkedList containing CFC of graph
//	 */
//	public static LinkedList<INeighbors> findAllSCC(IDirectedGraph graph){
//		LinkedList<INeighbors> allSCC = new LinkedList<INeighbors>();
//		TIntStack stack = new TIntStack();
//		int nb = graph.getNbNodes();
//		int[] parent = new int[nb];
//		int[] num = new int[nb];
//		int[] inf = new int[nb];
//		BitSet inStack = new BitSet(nb);
//		AbstractNeighborsIterator<INeighbors>[] successors = new AbstractNeighborsIterator[nb];
//		BitSet notOpenedYet = new BitSet(nb);
//		ActiveNodesIterator<IActiveNodes> iter = graph.activeNodesIterator();
//		int i;
//		while (iter.hasNext()){
//			i = iter.next();
//			notOpenedYet.set(i);
//			inf[i] = Integer.MAX_VALUE;
//			parent[i] = -1;
//		}	
//		int first = 0;
//		first = notOpenedYet.nextSetBit(first);
//		while(first>=0){
//			findSCC(stack, inStack, allSCC, graph, successors, first, parent, num, inf, notOpenedYet);
//			first = notOpenedYet.nextSetBit(first);
//		}
//		return allSCC;
//	}
//
//	private static void findSCC(TIntStack stack, BitSet inStack, LinkedList<INeighbors> allSCC, IDirectedGraph graph, 
//			AbstractNeighborsIterator<INeighbors>[] successors, int start, int[] p, int[] num, int[] inf, BitSet notOpenedYet){
//		inStack.clear();
//		stack.clear();
//		for(int i=0;i<p.length;i++){
//			p[i] = -1;
//			inf[i] = Integer.MAX_VALUE;
//		}
//		int i = start;
//		int k = 1;
//		num[start] = 1;
//		inf[start] = 1;
//		inStack.clear();
//		stack.clear();
//		stack.push(start);
//		inStack.set(start);
//		p[start] = start;
//		successors[start] = graph.successorsIteratorOf(start);
//		int j = 0;
//		while((i!=start) || successors[i].hasNext()){
//			if(!successors[i].hasNext()){
//				if (inf[i] >= num[i]){
//					INeighbors scc = new IntLinkedList();
//					int y;
//					do{
//						y = stack.pop();
//						inStack.clear(y);
//						notOpenedYet.clear(y);
//						scc.add(y);
//					}while(y!=i);
//					allSCC.add(scc);
//				}
//				inf[p[i]] = Math.min(inf[p[i]], inf[i]);
//				i = p[i];
//			}else{
//				j = successors[i].next();
//				if(notOpenedYet.get(j)){
//					if (p[j]==-1) {
//						p[j] = i;
//						i = j;
//						successors[i] = graph.successorsIteratorOf(i);
//						stack.push(i);
//						inStack.set(i);
//						k++;
//						num[i] = k;
//						inf[i] = num[i];
//					}
//					//				else {
//					//					inf[i] = Math.min(inf[i], num[j]);
//					//				}
//					else if(inStack.get(j)){
//						inf[i] = Math.min(inf[i], num[j]);
//					}
//				}
//			}
//		}
//		if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
//		if (inf[i] >= num[i]){
//			INeighbors scc = new IntLinkedList();
//			int y;
//			do{
//				y = stack.pop();
//				inStack.clear(y);
//				notOpenedYet.clear(y);
//				scc.add(y);
//			}while(y!=i);
//			allSCC.add(scc);
//		}
//	}

	//***********************************************************************************
	// STRONG ARTICULATION POINTS AND STRONG BRIDGES
	//***********************************************************************************

//	/**Find all strong articulation points of a graph knowing its strongly connected components
//	 * Uses Tarjan Lengauer algorithm that runs in O(m.alpha(m,n)+n)
//	 * 
//	 * @param graph
//	 * @param allSCC graphs' strongly connected components
//	 * @return
//	 */
//	public static StrongConnectivityObject findAllSAPandSB(IDirectedGraph graph, LinkedList<INeighbors> allSCC){
//		BitSet allSAP = new BitSet(graph.getNbNodes());
//		LinkedList<int[]> allSB =  new LinkedList<int[]>();
//		for (INeighbors scc:allSCC){
//			findSAP(allSAP, allSB, graph, scc);
//		}
//		return new StrongConnectivityObject(allSCC,allSAP,allSB);
//	}

//	/**Find and add all strong articulation points of a strongly connected component scc of a directed graph
//	 * Uses Tarjan Lengauer algorithm that runs in O(m.alpha(m,n)+n)
//	 * 
//	 * @param graphSAP bitset containing all SAP of the graph 
//	 * @param strongBridges the list of strong bridges of graph
//	 * @param graph the studied directed graph
//	 * @param scc the focused strongly connected component of graph
//	 */
//	private static void findSAP(BitSet graphSAP, LinkedList<int[]> strongBridges, IDirectedGraph graph, INeighbors scc) {
//		if (scc.neighborhoodSize()<3){
//			return; // trivial case
//		}
//		int size = scc.neighborhoodSize();
//		AbstractNeighborsIterator<INeighbors> iter = scc.iterator();
//		int[] indexOf = new int[size];
//		int y=0;
//		while(iter.hasNext()){
//			indexOf[y++] = iter.next();
//		}
//		int root = indexOf[0];
//		BitSet sccSAP = new BitSet(size);
//		// dominators of the reversed flowgraph rooted in root
//		IDirectedGraph flowG = GraphTools.createSubgraph(graph, scc, false, false);
//		FlowGraphManager domFG = new FlowGraphManager(0, flowG);
//		sccSAP.or(domFG.getImmediateNonTrivialDominators());
//		// dominators of the reversed flowgraph rooted in root
//		IDirectedGraph flowGR = GraphTools.createSubgraph(graph, scc, false, true);
//		FlowGraphManager domFGR = new FlowGraphManager(0, flowGR);
//		sccSAP.or(domFGR.getImmediateNonTrivialDominators());
//		sccSAP.clear(0); // just in case (but should not be useful)
//		// the considered root
//		if(isRootSAP(root,scc,graph)){
//			sccSAP.set(0);
//		}
//		// sap and strong bridges
//		for (y=sccSAP.nextSetBit(0); y>=0; y=sccSAP.nextSetBit(y+1)){
//			graphSAP.set(indexOf[y]);
//			int x = domFG.getParentOf(y);
//			if (sccSAP.get(x)){ // x and y are both SAP
//				if (domFG.isFlowBridge(x,y)){
//					strongBridges.add(new int[]{indexOf[x],indexOf[y]});
//				}
//			}
//			int xR = domFGR.getParentOf(y);
//			if(sccSAP.get(xR)){
//				if (domFGR.isFlowBridge(xR,y)){
//					strongBridges.add(new int[]{indexOf[y],indexOf[xR]});
//				}
//			}
//		}
//	}

//	/**Test whether root is a SAP or not considering cfc
//	 * performed in O(m+n) where n and m are respectively the number of nodes and arcs of cfc (not graph)
//	 * @param root 
//	 * @param cfc studied connected component of graph
//	 * @param graph studied graph
//	 * @return true iff root is a SAP in cfc
//	 */
//	private static boolean isRootSAP(int root, INeighbors cfc, IDirectedGraph graph) {
//		// subgraph induced by cfc\root
//		IDirectedGraph subgraph = GraphTools.createSubgraph(graph, cfc, true, false);
//		return !isStronglyConnected(subgraph);
//	}
}
