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
import java.util.Iterator;
import java.util.LinkedList;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.IDirectedGraph;

/**Class enabling to find dominators and strong bridges of a directed flow graph, i.e. a strongly connected graph
 * with a specified root node.
 * 
 * BEWARE : consider all nodes of the input graph as active
 * 
 * This part is the most complicated of the connectivity package
 * many algorithms exists : 
 * - Tarjan Lengauer O(m.alpha(m,n)) (alpha is the inverse of Ackermann function)
 * - Alstrup O(m+n) but extremely complicated in practice
 * - Various iterative algorithms in O(n^2) for most of them
 * 
 * It has been decided to use Tarjan Lengauer algorithm. For that we will refer to
 * "A fast algorithm for finding dominators in a flowgraph" of Tarjan and lengauer
 * 
 * @author Jean-Guillaume Fages
 */
public class FlowGraphManager {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	//
	private int root;
	private IDirectedGraph graph;
	private int nbNodes;
	//
	private int[] father;
	private int[] nodeOfDfsNumber;
	private int[] dfsNumberOfNode;
	//
	private int[] sdom;
	private int[] idom;
	private int[] ancestor;
	private int[] best;
	private LinkedList<Integer>[] buckets;
	private int[] size;
	private int[] childs;
	private BitSet immediateDominators;
	private boolean simple;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Create a flow graph of g rooted in r and find its dominators 
	 * and its flow bridges
	 * @param r root node of the flowgraph
	 * @param g strongly connected graph
	 */
	public FlowGraphManager(int r, IDirectedGraph g){
		root = r;
		graph = g;
		simple = false;// enable a O(alpha.m) otherwise it is O(m.log(n))
		initParams();
		proceedFirstDFS();
		findAllIdom();
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	private void initParams(){
		nbNodes = graph.getNbNodes();
		nodeOfDfsNumber = new int[nbNodes];
		father = new int[nbNodes];
		sdom = new int[nbNodes];
		idom = new int[nbNodes];
		ancestor = new int[nbNodes];
		best = new int[nbNodes];
		size = new int[nbNodes];
		childs = new int[nbNodes];
		buckets = new LinkedList[nbNodes];
		dfsNumberOfNode = new int[nbNodes];
		immediateDominators = new BitSet(nbNodes);
		for (int i=0; i<nbNodes; i++){
			father[i] = -1;
			sdom[i] = i;
			idom[i] = -1;
			ancestor[i] = -1;
			best[i] = i;
			size[i] = 1;
			childs[i] = 0;
			dfsNumberOfNode[i]=-1;
			nodeOfDfsNumber[i]=-1;
			buckets[i] = new LinkedList<Integer>();
		}		
	}

	/** perform a dfs in graph to label nodes */
	private void proceedFirstDFS(){
		INeighbors[] successors = new INeighbors[nbNodes];
		for (int i=0; i<nbNodes; i++){
			successors[i] = graph.getSuccessorsOf(i);
		}
		int i = root;
		father[0] = 0;
		dfsNumberOfNode[root] = 0;
		nodeOfDfsNumber[0] = root;
		BitSet notFirsts = new BitSet(nbNodes);
		boolean finish = false;
		int k = 0;
		int j;
		while(!finish){
			if(notFirsts.get(i)){
				j = successors[i].getNextElement();
			}else{
				notFirsts.set(i);
				j = successors[i].getFirstElement();
			}
			if(j<0){
				if(i==root){
					finish = true;
					break;
				}
				i = nodeOfDfsNumber[father[dfsNumberOfNode[i]]];
			}else{
				if (dfsNumberOfNode[j]==-1) {
					k++;
					father[k] = dfsNumberOfNode[i];
					dfsNumberOfNode[j] = k;
					nodeOfDfsNumber[k] = j;
					i = j;
				}
			}
		}
		INeighbors nei = graph.getActiveNodes();
		for(i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
			if(dfsNumberOfNode[i]==-1) {
				throw new UnsupportedOperationException();
			}
		}
	}

	//***********************************************************************************
	// SDOM & IDOM
	//***********************************************************************************

	private void findAllIdom(){
		idom[0] = 0;
		sdom[0] = 0;
		INeighbors nei;
		int u,v;
		for (int w=nbNodes-1; w>=1; w--){
			if(nodeOfDfsNumber[w]!=-1){
				nei = graph.getPredecessorsOf(nodeOfDfsNumber[w]);
				for(int k = nei.getFirstElement(); k>=0; k = nei.getNextElement()){
					v = dfsNumberOfNode[k];
					u = EVAL(v);
					if(sdom[u]<sdom[w]){
						sdom[w] = sdom[u];
					}
					buckets[sdom[w]].add(w);
				}
				LINK(father[w],w);
				Iterator<Integer> buciter = buckets[father[w]].iterator();
				while(buciter.hasNext()){
					v = buciter.next();
					buciter.remove();
					u = EVAL(v);
					if(sdom[u]<sdom[v]){
						idom[v] = u;
					}else{
						idom[v] = father[w];
					}
				}
			}
		}//step 4
		for (int w=1; w<nbNodes; w++){
			if(nodeOfDfsNumber[w]!=-1 && idom[w] != sdom[w]){
				idom[w] = sdom[sdom[w]];
			}
		}
	}

	private void LINK(int v, int w) {
		if(simple){
			LINK_SIMPLE(v, w);
		}else{
			LINK_ALPHA(v, w);
		}
	}

	private void LINK_SIMPLE(int v, int w) {
		ancestor[w] = v;
	}

	private void LINK_ALPHA(int v, int w) {
		int s = w;
		while(sdom[best[w]]<sdom[best[childs[s]]]){
			if(size[s] + size[childs[childs[s]]] >= 2*size[childs[s]]){
				father[childs[s]] = s;
				childs[s] = childs[childs[s]];
			}else{
				size[childs[s]] = size[s];
				father[s] = childs[s];
				s = father[s];
			}
			best[s] = best[w];
			size[v] =size[v]+size[w];
			if(size[v]<2*size[w]){
				int k = s;
				s = childs[v];
				childs[v] = k;
			}else{
				
			}
			while(s!=-1){
				father[s] = v;
				s = childs[s];
			}
		}
	}

	private int EVAL(int v) {
		if(simple){
			return EVAL_SIMPLE(v);
		}else{
			return EVAL_ALPHA(v);
		}
	}

	private int EVAL_SIMPLE(int v) {
		if(ancestor[v] == -1){
			return v;
		}else{
			COMPRESS(v);
			return best[v];
		}
	}

	private int EVAL_ALPHA(int v) {
		if(ancestor[v] == -1){
			return best[v];
		}else{
			COMPRESS(v);
			System.out.println(v+" = "+ancestor[v]);
			System.out.println(v+" = "+best[v]);
			if(sdom[best[ancestor[v]]] >= sdom[best[v]]){
				return best[v];
			}else{
				return best[ancestor[v]];
			}
		}
	}

	private void COMPRESS(int v) {
		if(ancestor[ancestor[v]]!=-1){
			COMPRESS(ancestor[v]);
		}
		if(sdom[best[ancestor[v]]] < sdom[best[v]]){
			best[v] = best[ancestor[v]];
		}
		ancestor[v] = ancestor[ancestor[v]];
	}

	//***********************************************************************************
	// FLOW BRIDGES
	//***********************************************************************************

	boolean preprocessed;
	private LCAGraphManager lcaManager;

	/**Test if (x,y) is a flowbridge
	 * @param x node 
	 * @param y node 
	 * @return true iff (x,y) is a flowbridge
	 */
	public boolean isFlowBridge(int x, int y) {
		if(!preprocessed){
			preprocessed = true;
			lcaManager = new LCAGraphManager(root, graph);
		}
		if (idom[dfsNumberOfNode[y]] != dfsNumberOfNode[x] || father[dfsNumberOfNode[y]] != dfsNumberOfNode[x]){
			return false;
		}
		INeighbors preds = graph.getPredecessorsOf(y);
		int p;
		for(int k = preds.getFirstElement(); k >=0; k = preds.getNextElement()){
			p = dfsNumberOfNode[k];
			if (p!=dfsNumberOfNode[y] && p!=dfsNumberOfNode[x] && (idom[p]<dfsNumberOfNode[y] || lcaManager.getLCA(nodeOfDfsNumber[p],y)!=y)){
				return false;
			}
		}
		return true;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	/**Get the parent of x in the dfs tree
	 * @param x the focused node
	 * @return the parent of x in the dfs tree
	 */
	public int getParentOf(int x){
		return nodeOfDfsNumber[father[dfsNumberOfNode[x]]];
	}

	/**
	 * @return the immediate dominator of x in the flow graph 
	 */
	public int getImmediateDominatorsOf(int x){
		return nodeOfDfsNumber[idom[dfsNumberOfNode[x]]];
	}
	/**
	 * @return a bitset representing non trivial dominators of the flow graph 
	 */
	public BitSet getImmediateNonTrivialDominators(){
		return immediateDominators;
	}
	public boolean isImmediateNonTrivialDominators(int x){
		return immediateDominators.get(x);
	}
	/**
	 * @return an array containing the nodes sorted according to the dfs order
	 */
	public int[] getNodeOfDfsNumber() {
		return nodeOfDfsNumber;
	}
	/**
	 * @return an array containing the dfs numbers of nodes
	 */
	public int[] getDfsNumberOfNode() {
		return dfsNumberOfNode;
	}
}
