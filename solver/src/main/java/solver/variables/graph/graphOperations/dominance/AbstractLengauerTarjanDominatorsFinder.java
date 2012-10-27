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

package solver.variables.graph.graphOperations.dominance;

import gnu.trove.list.array.TIntArrayList;
import solver.variables.graph.DirectedGraph;
import solver.variables.setDataStructures.SetType;
import solver.variables.setDataStructures.ISet;

/**Class that finds dominators of a given flow graph g(s) */
public abstract class AbstractLengauerTarjanDominatorsFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// flow graph
	protected DirectedGraph g;
	// dominator tree
	protected DirectedGraph T;
	protected int root, n, k;
	protected int[] parent,vertex,bucket,ancestor,label,semi,dom;
	protected ISet[] succs;
	protected ISet[] preds;
	protected TIntArrayList list;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Object that finds dominators of the given flow graph g(s)*/
	public AbstractLengauerTarjanDominatorsFinder(int s, DirectedGraph g){
		root = s;
		n = g.getNbNodes();
		this.g = g;
		parent = new int[n];
		semi = new int[n];
		dom = new int[n];
		ancestor = new int[n];
		label = new int[n];
		vertex = new int[n];
		bucket= new int[n];
		succs = new ISet[n];
		preds = new ISet[n];
		T = new DirectedGraph(n, SetType.LINKED_LIST,false);
		list  = new TIntArrayList();
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	/**Find immediate dominators of the given graph
	 * and preprocess dominance requests
	 * @return false iff the source cannot reach all nodes (contradiction)
	 * */
	public boolean findDominators() {
		initParams(false);
		DFS();
		if(k!=n-1){
			return false;
		}
		findAllIdom();
		preprocessDominanceRequests();
		return true;
	}

	/**Find immediate postdominators of the given graph
	 * and preprocess dominance requests
	 * post dominators are dominators of the inverse graph
	 * @return false iff the source cannot reach all nodes (contradiction)
	 * */
	public boolean findPostDominators() {
		initParams(true);
		DFS();
		if(k!=n-1){
			return false;
		}
		findAllIdom();
		preprocessDominanceRequests();
		return true;
	}

	protected void initParams(boolean inverseGraph){
		for(int i=0;i<n;i++){
			T.getSuccessorsOf(i).clear();
			T.getPredecessorsOf(i).clear();
			if(inverseGraph){
				succs[i] = g.getPredecessorsOf(i);
				preds[i] = g.getSuccessorsOf(i);
			}else{
				succs[i] = g.getSuccessorsOf(i);
				preds[i] = g.getPredecessorsOf(i);
			}
			semi[i]  = -1;
			ancestor[i] = -1;
			bucket[i] = -1;
		}
	}

	protected void DFS(){
		int node = root;
		int next;
		k=0;
		semi[node] = k;
		label[node] = node;
		vertex[k] = node;
		boolean notFinished = true;
		boolean first = true;
		while(notFinished){
			if(first){
				next = succs[node].getFirstElement();
				first = false;
			}else{
				next = succs[node].getNextElement();
			}
			if(next>=0){
				if (semi[next]==-1) {
					k++;
					semi[next] = k;
					label[next] = next;
					vertex[k] = next;
					parent[next] = node;
					node = next;
					first = true;
				}
			}else{
				if(node== root){
					notFinished = false;
					break;
				}
				node = parent[node];
				first= false;
			}
		}
	}

	//***********************************************************************************
	// SDOM & IDOM
	//***********************************************************************************

	protected void findAllIdom(){
		int w,v,u;
		ISet prds;
		for (int i=n-1; i>=1; i--){
			w = vertex[i];
			prds = preds[w];
			for(v= prds.getFirstElement();v>=0; v=prds.getNextElement()){
				u = EVAL(v);
				if (semi[u]<semi[w]){
					semi[w] = semi[u];
				}
			}
			if(vertex[semi[w]]!=parent[w]){
				addToBucket(vertex[semi[w]],w);
			}else{
				dom[w] = parent[w];
			}
			LINK(parent[w], w);
			int oldBI = parent[w];
			v = bucket[oldBI];
			while(v!=-1){
				bucket[oldBI] = -1;
				u = EVAL(v);
				if(semi[u]<semi[v]){
					dom[v] = u;
				}else{
					dom[v] = parent[w];
				}
				oldBI = v;
				v = bucket[v];
			}
		}
		for (int i=1; i<n; i++){
			w = vertex[i];
			if(dom[w] != vertex[semi[w]]){
				dom[w] = dom[dom[w]];
			}
			T.addArc(dom[w],w);
		}
		dom[root]= root;
	}

	protected void addToBucket(int buckIdx, int element) {
		if(bucket[buckIdx]==-1){
			bucket[buckIdx]=element;
		}else{
			int old = bucket[buckIdx];
			bucket[buckIdx]=element;
			bucket[element]=old;
		}
	}

	//***********************************************************************************
	// LINK-EVAL
	//***********************************************************************************

	protected abstract void LINK(int v, int w);

	protected abstract int EVAL(int v);

	protected abstract void COMPRESS(int v);

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	/**
	 * @return the immediate dominator of x in the flow graph
	 */
	public int getImmediateDominatorsOf(int x){
		return dom[x];
	}

	/**BEWARE requires preprocessDominanceRequests()
	 * @return true iff x is dominated by y
	 */
	public boolean isDomminatedBy(int x, int y){
		return ancestor[x]>ancestor[y] && semi[x]<semi[y];
	}

	/**Get the dominator tree formed with arcs (x,y)
	 * such that x is the immediate dominator of y
	 * @return the dominator of the flow graph
	 */
	public DirectedGraph getDominatorTree(){
		return T;
	}

	/**O(n+m) preprocessing for enabling dominance requests in O(1)
	 * BEWARE : destroy the current data structure (recycling)
	 * */
	protected void preprocessDominanceRequests() {
		// RECYCLE DATA STRUCTURES
		// ancestor = in  = opening time = preorder
		// semi     = out = closing time = postorder
		for(int i=0;i<n;i++){
			parent[i]=-1;
			succs[i] = T.getSuccessorsOf(i);
		}
		//PREPROCESSING
		int time = 0;
		int currentNode = root;
		parent[currentNode] = currentNode;
		ancestor[currentNode] = 0;
		int nextNode;
		boolean first = true;
		boolean finished = false;
		while(!finished){
			if(first){
				nextNode = succs[currentNode].getFirstElement();
				first = false;
			}else{
				nextNode = succs[currentNode].getNextElement();
			}
			if(nextNode<0){
				time++;
				semi[currentNode] = time;
				if(currentNode== root){
					finished = true;
					break;
				}
				first = false;
				currentNode = parent[currentNode];
			}else{
				if (parent[nextNode]==-1) {
					time++;
					ancestor[nextNode] = time;
					parent[nextNode] = currentNode;
					currentNode = nextNode;
					first = true;
				}
			}
		}
	}

	//***********************************************************************************
	// ARC-DOMINATOR
	//***********************************************************************************

	public boolean isArcDominator(int x, int y) {
		//(x,y) existe && x domine y && y domines tous ses autres predecesseurs (sauf x donc)
		throw new UnsupportedOperationException("method not implemented yet");
	}
}
