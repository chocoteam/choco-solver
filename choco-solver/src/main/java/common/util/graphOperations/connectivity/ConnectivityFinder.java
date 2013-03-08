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

package common.util.graphOperations.connectivity;


import common.util.objects.graphs.UndirectedGraph;
import common.util.objects.setDataStructures.ISet;
import gnu.trove.list.array.TIntArrayList;

/**
 * Class containing algorithms to find all connected components and articulation points of graph by performing one dfs
 * it uses Tarjan algorithm in a non recursive way and can be performed in O(M+N) time c.f. Gondrand Minoux
 *
 * @author Jean-Guillaume Fages
 */
public class ConnectivityFinder {

    //***********************************************************************************
    // CONNECTED COMPONENTS ONLY
    //***********************************************************************************

    private int n;
    private UndirectedGraph graph;
    private ISet[] neighbors;
    private int[] CC_firstNode, CC_nextNode, node_CC, p;
    private int nbCC;
    //bonus biconnection
    private int[] numOfNode, nodeOfNum, inf;

    /**
     * Create an object that can compute Connected Components (CC) of a graph g
     * Can also quickly tell whether g is biconnected or not
     *
     * @param g graph
     */
    public ConnectivityFinder(UndirectedGraph g) {
        graph = g;
        n = g.getNbNodes();
        neighbors = new ISet[n];
        p = new int[n];
        for (int i = graph.getActiveNodes().getFirstElement(); i >= 0; i = graph.getActiveNodes().getNextElement()) {
            neighbors[i] = graph.getNeighborsOf(i);
        }
    }

    /**
     * get the number of CC in g
     * Beware you should call method findAllCC() first
     *
     * @return nbCC the number of CC in g
     */
    public int getNBCC() {
        return nbCC;
    }

    public int[] getCC_firstNode() {
        return CC_firstNode;
    }

    public int[] getCC_nextNode() {
        return CC_nextNode;
    }

    public int[] getNode_CC() {
        return node_CC;
    }

    /**
     * Find all connected components of graph by performing one dfs
     * Complexity : O(M+N) light and fast in practice
     */
    public void findAllCC() {
        if (node_CC == null) {
            CC_firstNode = new int[n];
            CC_nextNode = new int[n];
            node_CC = new int[n];
        }
        ISet act = graph.getActiveNodes();
        for (int i = act.getFirstElement(); i >= 0; i = act.getNextElement()) {
            p[i] = -1;
            CC_firstNode[i] = -1;
            neighbors[i] = graph.getNeighborsOf(i);
        }
        int first = act.getFirstElement();
        int cc = 0;
        while (first >= 0) {
            findCC(first, cc);
            cc++;
            while (first >= 0 && p[first] != -1) {
                first = act.getNextElement();
            }
        }
        nbCC = cc;
    }

    private void findCC(int start, int cc) {
        int i = start;
        p[start] = start;
        add(start, cc);
        int j;
        boolean notFinished = true;
        boolean first = true;
        while (notFinished) {
            if (first) {
                j = neighbors[i].getFirstElement();
                first = false;
            } else {
                j = neighbors[i].getNextElement();
            }
            if (j < 0) {
                if (i == start) {
                    notFinished = false;
                    break;
                }
                i = p[i];
            } else {
                if (p[j] == -1) {
                    p[j] = i;
                    i = j;
                    first = true;
                    add(i, cc);
                }
            }
        }
    }

    private void add(int node, int cc) {
        node_CC[node] = cc;
        CC_nextNode[node] = CC_firstNode[cc];
        CC_firstNode[cc] = node;
    }

    /**
     * Test biconnectivity (i.e. connected with no articulation point and no bridge)
     *
     * @return true iff g is biconnected
     */
    public boolean isBiconnected() {
        if (nodeOfNum == null) {
            nodeOfNum = new int[n];
            numOfNode = new int[n];
            inf = new int[n];
        }
        ISet act = graph.getActiveNodes();
        for (int i = act.getFirstElement(); i >= 0; i = act.getNextElement()) {
            inf[i] = Integer.MAX_VALUE;
            CC_firstNode[i] = -1;
            p[i] = -1;
        }
        //algo
        int start = act.getFirstElement();
        int i = start;
        int k = 0;
        numOfNode[start] = k;
        nodeOfNum[k] = start;
        p[start] = start;
        int j, q;
        int nbRootChildren = 0;
        boolean first = true;
        while (true) {
            if (first) {
                j = neighbors[i].getFirstElement();
                first = false;
            } else {
                j = neighbors[i].getNextElement();
            }
            if (j < 0) {
                if (i == start) {
                    if (k < act.getSize() - 1) {
                        return false;// NOT EVEN CONNECTED
                    } else {
                        return true;
                    }
                }
                q = inf[i];
                i = p[i];
                inf[i] = Math.min(q, inf[i]);
                if (q >= numOfNode[i] && i != start) {
                    return false;
                } // ARTICULATION POINT DETECTED
            } else {
                if (p[j] == -1) {
                    p[j] = i;
                    if (i == start) {
                        nbRootChildren++;
                        if (nbRootChildren > 1) {
                            return false;// ARTICULATION POINT DETECTED
                        }
                    }
                    i = j;
                    first = true;
                    k++;
                    numOfNode[i] = k;
                    nodeOfNum[k] = i;
                    inf[i] = numOfNode[i];
                } else if (p[i] != j) {
                    inf[i] = Math.min(inf[i], numOfNode[j]);
                }
            }
        }
    }


    public TIntArrayList isthmusFrom, isthmusTo;
    private int[] ND, L, H;

    public boolean isConnectedAndFindIsthma() {
        if (numOfNode == null || CC_firstNode == null) {
            CC_firstNode = new int[n];
            CC_nextNode = new int[n];
            node_CC = new int[n];
            nodeOfNum = new int[n];
            numOfNode = new int[n];
            isthmusFrom = new TIntArrayList();
            isthmusTo = new TIntArrayList();
            ND = new int[n];
            L = new int[n];
            H = new int[n];
        }
        ISet act = graph.getActiveNodes();
        for (int i = act.getFirstElement(); i >= 0; i = act.getNextElement()) {
            p[i] = -1;
            CC_firstNode[i] = -1;
        }
        //algo
        int start = act.getFirstElement();
        int i = start;
        int k = 0;
        numOfNode[start] = k;
        nodeOfNum[k] = start;
        p[start] = start;
        int j;
        boolean first = true;
        while (true) {
            if (first) {
                j = neighbors[i].getFirstElement();
                first = false;
            } else {
                j = neighbors[i].getNextElement();
            }
            if (j < 0) {
                if (i == start) {
                    if (k < act.getSize() - 1) {
                        return false;
                    } else {
                        break;
                    }
                }
                i = p[i];
            } else {
                if (p[j] == -1) {
                    p[j] = i;
                    i = j;
                    first = true;
                    add(i, 0);
                    k++;
                    numOfNode[i] = k;
                    nodeOfNum[k] = i;
                }
            }
        }
        // POST ORDER PASS FOR FINDING ISTHMUS
        isthmusFrom.clear();
        isthmusTo.clear();
        int currentNode;
        for (i = k; i >= 0; i--) {
            currentNode = nodeOfNum[i];
            ND[currentNode] = 1;
            L[currentNode] = i;
            H[currentNode] = i;
            ISet nei = neighbors[currentNode];
            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                if (p[s] == currentNode) {
                    ND[currentNode] += ND[s];
                    L[currentNode] = Math.min(L[currentNode], L[s]);
                    H[currentNode] = Math.max(H[currentNode], H[s]);
                } else if (s != p[currentNode]) {
                    L[currentNode] = Math.min(L[currentNode], numOfNode[s]);
                    H[currentNode] = Math.max(H[currentNode], numOfNode[s]);
                }
                if (s != currentNode && p[s] == currentNode && L[s] >= numOfNode[s] && H[s] < numOfNode[s] + ND[s]) {
                    isthmusFrom.add(currentNode);
                    isthmusTo.add(s);
                }
            }
        }
        return true;
    }
}
//***********************************************************************************
// CONNECTED COMPONENTS AND ARTICULATION POINTS IN ONE DFS
//***********************************************************************************

//	/**Find all connected components, articulation points and isthmus of the input graph by performing one dfs
//	 * Complexity : O(M+N)
//	 * @param graph
//	 * @return a ConnectivityObject that encapsulates all connected components, articulation points and isthmus of the given graph
//	 */
//	private static ConnectivityObject findAll(IGraph graph){
//		int nb = graph.getNbNodes();
//		ConnectivityObject co = new ConnectivityObject();
//		int[] p = new int[nb];
//		int[] numOfNode = new int[nb];
//		int[] nodeOfNum = new int[nb];
//		int[] inf = new int[nb];
//		int[] ND  = new int[nb];
//		int[] L  = new int[nb];
//		int[] H  = new int[nb];
//		INeighbors[] neighbors = new INeighbors[nb];
//		BitSet notOpenedNodes = new BitSet(nb);
//		BitSet notFirst = new BitSet(nb);
//		IActiveNodes act = graph.getActiveNodes();
//		for (int i = act.getFirstElement(); i>=0; i = act.getNextElement()) {
//			inf[i] = Integer.MAX_VALUE;
//			p[i] = -1;
//			notOpenedNodes.set(i);
//			neighbors[i] = graph.getNeighborsOf(i);
//		}
//		int first = 0;
//		first = notOpenedNodes.nextSetBit(first);
//		while(first>=0){
//			firstAllOnOneCC(co, neighbors, first, p, numOfNode, nodeOfNum, inf, notOpenedNodes, notFirst, ND, L, H);
//			first = notOpenedNodes.nextSetBit(first);
//		}
//		return co;
//	}
//
//	private static void firstAllOnOneCC(ConnectivityObject co, INeighbors[] neighbors, int start, int[] p, int[] numOfNode, int[] nodeOfNum, int[] inf, BitSet notOpenedNodes, BitSet notFirst, int[] ND, int[] L, int[] H){
//		co.newCC();
//		int i = start;
//		int k = 0;
//		numOfNode[start] = k;
//		nodeOfNum[k] = start;
//		p[start] = start;
//		notOpenedNodes.clear(start);
//		int j=0,q;
//		co.addCCNode(start);
//		int nbRootChildren = 0;
//		boolean notFinished = true;
//		while(notFinished){
//			if(notFirst.get(i)){
//				j = neighbors[i].getNextElement();
//			}else{
//				j = neighbors[i].getFirstElement();
//				notFirst.set(i);
//			}
//			if(j<0){
//				if(i==start){notFinished = false;break;}
//				q = inf[i];
//				i = p[i];
//				inf[i] = Math.min(q, inf[i]);
//				if (q >= numOfNode[i] && i!=start){ co.addArticulationPoint(i);} // ARTICULATION POINT DETECTED
//			}else{
//				if (p[j]==-1) {
//					p[j] = i;
//					if (i == start){
//						nbRootChildren++;
//					}
//					i = j;
//					notOpenedNodes.clear(i);
//					k++;
//					numOfNode[i] = k;
//					nodeOfNum[k] = i;
//					inf[i] = numOfNode[i];
//					co.addCCNode(i);
//				}else if(p[i]!=j){
//					inf[i] = Math.min(inf[i], numOfNode[j]);
//				}
//			}
//		}
//		if(nbRootChildren>1){co.addArticulationPoint(start);} // ARTICULATION POINT DETECTED
//
//		// POST ORDER PASS FOR FINDING ISTHMUS
//		int n = neighbors.length;
//		int currentNode;
//		for(i=k; i>=0; i--){
//			currentNode = nodeOfNum[i];
//			ND[currentNode] = 1;
//			L[currentNode]  = i;
//			H[currentNode]  = i;
//			for(int s=neighbors[currentNode].getFirstElement(); s>=0; s = neighbors[currentNode].getNextElement()){
//				if (p[s]==currentNode){
//					ND[currentNode] += ND[s];
//					L[currentNode] = Math.min(L[currentNode], L[s]);
//					H[currentNode] = Math.max(H[currentNode], H[s]);
//				}else if(s!=p[currentNode]){
//					L[currentNode] = Math.min(L[currentNode], numOfNode[s]);
//					H[currentNode] = Math.max(H[currentNode], numOfNode[s]);
//				}
//				if (s!=currentNode && p[s]==currentNode && L[s]>= numOfNode[s] && H[s] < numOfNode[s]+ND[s]){
//					co.addIsthmus((currentNode+1)*n+s); // ISTHMUS DETECTED
//				}
//			}
//		}
//	}
//
//	public static boolean isBiconnectedNaive(IGraph graph){
//		int n = graph.getNbNodes();
//		int i,j;
//		for(int k=0;k<n;k++){
//			int[] list = new int[n];
//			BitSet inList = new BitSet(n);
//			int indexFirst=0, indexTo=0;
//			i = 0;
//			if(k==0){
//				i = 1;
//			}
//			list[indexTo] = i;
//			inList.set(i);
//			indexTo++;
//			INeighbors nei;
//			while(indexFirst!=indexTo){
//				i = list[indexFirst];
//				indexFirst++;
//				nei = graph.getNeighborsOf(i);
//				for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(j!=k && !inList.get(j)){
//						inList.set(j);
//						list[indexTo] = j;
//						indexTo++;
//					}
//				}
//			}
//			if(indexTo>graph.getActiveNodes().getSize()-1){
//				throw new UnsupportedOperationException();
//			}
//			if(indexTo<graph.getActiveNodes().getSize()-1){
//				return false;
//			}
//		}
//		return true;
//	}
