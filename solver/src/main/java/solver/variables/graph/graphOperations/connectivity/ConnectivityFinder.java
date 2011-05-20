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

import java.util.BitSet;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;

/**Class containing algorithms to find all connected components and articulation points of graph by performing one dfs
 * it uses Tarjan algorithm in a non recursive way and can be performed in O(M+N) time c.f. Gondrand Minoux
 * @author Jean-Guillaume Fages
 */
public class ConnectivityFinder {

	//***********************************************************************************
	// CONNECTED COMPONENTS AND ARTICULATION POINTS IN ONE DFS
	//***********************************************************************************
	
	/**Find all connected components and articulation points of graph by performing one dfs
	 * Complexity : O(M+N)
	 * @param graph
	 * @return a ConnectivityObject that encapsulates all connected components and articulation points of graph
	 */
	public static ConnectivityObject findAllCCandAP(IGraph graph){
		int nb = graph.getNbNodes();
		ConnectivityObject co = new ConnectivityObject(nb);
		int[] p = new int[nb];
		int[] num = new int[nb];
		int[] inf = new int[nb];
		INeighbors[] neighbors = new INeighbors[nb];
		BitSet notOpenedNodes = new BitSet(nb);
		BitSet notFirst = new BitSet(nb);
		for (int i = graph.getActiveNodes().nextValue(0); i>=0; i = graph.getActiveNodes().nextValue(i+1)) {
			inf[i] = Integer.MAX_VALUE;
			p[i] = -1;
			notOpenedNodes.set(i);
			neighbors[i] = graph.getNeighborsOf(i);
		}
		int first = 0;
		first = notOpenedNodes.nextSetBit(first);
		while(first>=0){
			findCCandAP(co, neighbors, first, p, num, inf, notOpenedNodes, notFirst);
			first = notOpenedNodes.nextSetBit(first);
		}
		return co;
	}
	
	/**
	 * @param co the object which encapsulates CC and AP
	 * @param neighbors iterators for neighbors of nodes
	 * @param start the starting node of the procedure
	 * @param p the array of parents of nodes in the dfs
	 * @param num dfs numerotation
	 * @param inf array used to find AP
	 * @param notOpenedNodes enables to find the next starting point to consider
	 * @param notFirst enables to know whether getFirstElement() or getNextElement() should be called to iterate
	 */
	private static void findCCandAP(ConnectivityObject co, INeighbors[] neighbors, int start, int[] p, int[] num, int[] inf, BitSet notOpenedNodes, BitSet notFirst){
		co.newCC();
		int i = start;
		int k = 1;
		num[start] = 1;
		p[start] = start;
		notOpenedNodes.clear(start);
		int j=0,q;
		co.addCCNode(start);
		int nbRootChildren = 0;
		boolean notFinished = true;
		while(notFinished){
			if(notFirst.get(i)){
				j = neighbors[i].getNextElement();
			}else{
				j = neighbors[i].getFirstElement();
				notFirst.set(i);
			}
			if(j<0){
				if(i==start){
					notFinished = false;
					break;
				}
				if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
				q = inf[i];
				i = p[i];
				inf[i] = Math.min(q, inf[i]);
				if (q >= num[i] && i!=start){
					co.addArticulationPoint(i);
				}
			}else{
				if (p[j]==-1) {
					p[j] = i;
					if (i == start){
						nbRootChildren++;
					}
					i = j;
					notOpenedNodes.clear(i);
					k++;
					num[i] = k;
					inf[i] = num[i];
					co.addCCNode(i);
				}else if(p[i]!=j){
					inf[i] = Math.min(inf[i], num[j]);
				}
			}
		}
		if(nbRootChildren>1){
			co.addArticulationPoint(start);
		}
	}
	
	//***********************************************************************************
	// CONNECTED COMPONENTS ONLY
	//***********************************************************************************
	
	/**Find all connected components of graph by performing one dfs
	 * Complexity : O(M+N) but light and fast in practice
	 * @param graph
	 * @return a ConnectivityObject that encapsulates all connected components of graph but has no articulation points (null pointer)
	 */
	public static ConnectivityObject findAllCConly(IGraph graph){
		int nb = graph.getNbNodes();
		ConnectivityObject co = new ConnectivityObject();
		int[] p = new int[nb];
		INeighbors[] neighbors = new INeighbors[nb];
		BitSet notOpenedNodes = new BitSet(nb);
		BitSet notFirsts = new BitSet(nb);
		for (int i = graph.getActiveNodes().nextValue(0); i>=0; i = graph.getActiveNodes().nextValue(i+1)) {
			p[i] = -1;
			notOpenedNodes.set(i);
			neighbors[i] = graph.getNeighborsOf(i);
		}
		int first = 0;
		first = notOpenedNodes.nextSetBit(first);
		while(first>=0){
			findCC(co, neighbors, first, p, notOpenedNodes, notFirsts);
			first = notOpenedNodes.nextSetBit(first);
		}
		return co;
	}
	
	
	/**
	 * @param co the object which encapsulates CC and AP but here AP will be empty
	 * @param neighbors iterators for neighbors of nodes
	 * @param start the starting node of the procedure
	 * @param p the array of parents of nodes in the dfs
	 * @param notOpenedNodes enables to find the next starting point to consider
	 * @param notFirsts enables to know whether getFirstElement() or getNextElement() should be called to iterate
	 */
	private static void findCC(ConnectivityObject co, INeighbors[] neighbors, int start, int[] p, BitSet notOpenedNodes, BitSet notFirsts){
		co.newCC();
		int i = start;
		int k = 1;
		p[start] = start;
		notOpenedNodes.clear(start);
		int j;
		co.addCCNode(start);
		int nbRemainings = notOpenedNodes.cardinality();
		boolean notFinished = true;
		while(notFinished){
			if(notFirsts.get(i)){
				j = neighbors[i].getNextElement();
			}else{
				notFirsts.set(i);
				j = neighbors[i].getFirstElement();
			}
			if(j<0){
				if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
				if(i==start){
					notFinished = false;
					break;
				}
				i = p[i];
			}else{
				if (p[j]==-1) {
					p[j] = i;
					i = j;
					notOpenedNodes.clear(i);
					nbRemainings--;
					k++;
					co.addCCNode(i);
					if(nbRemainings==0){
						return;
					}
				}
			}
		}
	}
}
