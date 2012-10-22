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

import solver.variables.graph.directedGraph.DirectedGraph;

/**Class that finds dominators of a given flow graph g(s)
 * Uses the LT algorithm which runs in O(alpha.m)*/
public class AlphaDominatorsFinder extends AbstractLengauerTarjanDominatorsFinder{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int[] size,child;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Object that finds dominators of the given flow graph g(s)
	 * It uses the LT algorithm which runs in O(alpha.m)*/
	public AlphaDominatorsFinder(int s, DirectedGraph g){
		super(s,g);
		size = new int[n];
		child = new int[n];
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	@Override
	protected void initParams(boolean inverseGraph){
		super.initParams(inverseGraph);
		for(int i=0;i<n;i++){
			size[i]  = 0;
			child[i] = root;
		}
	}

	//***********************************************************************************
	// LINK-EVAL
	//***********************************************************************************

	protected void LINK(int v, int w) {
		int s = w;
		while(semi[label[w]]<semi[label[child[s]]]){
			if(size[s] + size[child[child[s]]] >= 2*size[child[s]]){
				ancestor[child[s]] = s;
				child[s] = child[child[s]];
			}else{
				size[child[s]] = size[s];
				ancestor[s] = child[s];
				s = ancestor[s];
			}
		}
		label[s] = label[w];
		size[v] =size[v]+size[w];
		if(size[v]<2*size[w]){
			int k = s;
			s = child[v];
			child[v] = k;
		}
		while(s!=root){
			ancestor[s] = v;
			s = child[s];
		}
	}

	protected int EVAL(int v) {
		if(ancestor[v] == -1){
			return label[v];
		}else{
			COMPRESS(v);
			if(semi[label[ancestor[v]]] >= semi[label[v]]){
				return label[v];
			}else{
				return label[ancestor[v]];
			}
		}

	}

	protected void COMPRESS(int v) {
		int k = v;
		list.clear();
		while(ancestor[ancestor[k]]!=-1){
			list.add(k);
			k = ancestor[k];
		}
		for(k=list.size()-1;k>=0;k--){
			v = list.get(k);
			if(semi[label[ancestor[v]]] < semi[label[v]]){
				label[v] = label[ancestor[v]];
			}
			ancestor[v] = ancestor[ancestor[v]];
		}
	}
}
