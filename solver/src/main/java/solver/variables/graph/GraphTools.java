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

package solver.variables.graph;

import java.util.BitSet;
import java.util.HashMap;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

/**Class containing some static methods to manipulate graphs
 * @author Jean-Guillaume Fages */
public class GraphTools {


	//***********************************************************************************
	// IGraph explorations
	//***********************************************************************************
	
	/**perform a dfs in graph.
	 * @param root starting point of the dfs
	 * @param graph to perform a dfs on
	 * @return num an array to represent node numbers in the dfs tree
	 */
	public static int[] performDFS(int root, IDirectedGraph graph){
		int nb = graph.getNbNodes();
		INeighbors[] neighbors = new INeighbors[nb];
		int[] father = new int[nb];
		int[] num = new int[nb];
		BitSet notFirstTime = new BitSet(nb);
		for (int i=0; i<nb; i++){
			father[i] = -1;
			neighbors[i] = graph.getSuccessorsOf(i);
		}
		int i = root;
		int k = 0;
		num[root] = k;
		father[root] = root;
		int j=0;
		while(true){
			if(notFirstTime.get(i)){
				j = neighbors[i].getNextElement();
			}else{
				j = neighbors[i].getFirstElement();
				notFirstTime.set(i);
			}
			while(j == i){
				j = neighbors[i].getNextElement();
			}
			if(j<0){
				if(i==root){
					return num;
				}
				i = father[i];
			}else{
				if (father[j]==-1) {
					father[j] = i;
					i = j;
					k++;
					num[i] = k;
				}
			}
		}
	}

	//***********************************************************************************
	// Subgraphs
	//***********************************************************************************

	/**Create a new graph which is a subraph of graph deduced from a subset and some parameters.
	 * done in O(m+n) operations
	 * @param graph original graph
	 * @param subset of nodes to keep in the subgraph
	 * @param skipFirst if the first node of the subset should be skipped 
	 * @param reverse if all arcs should be reversed
	 * @returna new graph which is a subraph of graph deduced from a subset and some parameters.
	 */
	public static IDirectedGraph createSubgraph(IDirectedGraph graph, INeighbors subset, boolean skipFirst, boolean reverse){
		int nb = subset.neighborhoodSize();
		if(subset.neighborhoodSize()==0){
			throw new UnsupportedOperationException("error ");
		}
		if (skipFirst){
			nb--;
		}
		
		boolean[][] matrix = new boolean[nb][nb];
		int[] indexes = new int[nb];
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int nodeInNewGraph = 0;
		boolean first = true;
		for(int k=subset.getFirstElement(); k>=0;k=subset.getNextElement()){
			if(first && skipFirst){
				first = false;
			}else{
				indexes[nodeInNewGraph] = k;
				map.put(k, nodeInNewGraph);
				nodeInNewGraph++;
			}
		}
		nodeInNewGraph = 0;
		first = true;
		INeighbors nei;
		for(int k=subset.getFirstElement(); k>=0;k=subset.getNextElement()){
			if(first && skipFirst){
				first = false;
			}else{
				nei = graph.getSuccessorsOf(k);
				for(int l=nei.getFirstElement(); l>=0;l=nei.getNextElement()){
					if (map.get(l)!=null){
						if (!reverse){
							matrix[nodeInNewGraph][map.get(l)] = true;
						}else{
							matrix[map.get(l)][nodeInNewGraph] = true;
						}
					}
				}
				nodeInNewGraph++;
			}
		}
		return new DirectedGraph(nb, matrix, graph.getType());
	}

	/**Create a new directed graph which is a subraph of an undirected graph
	 * every edge is replaced by two arcs
	 * If the graph is directed then it will be considered as if it was undirected i.e (x,y) => (x,y) & (y,x)
	 * done in O(m+n) operations 
	 * @param undirectedGraph original undirected graph
	 * @param subset of nodes to keep in the subgraph
	 * @returna new directed graph which is a subraph of an undirected graph deduced from a subset.
	 */
	public static IDirectedGraph createSubgraph(IGraph undirectedGraph, INeighbors subset){
		int nb = subset.neighborhoodSize();
		boolean[][] matrix = new boolean[nb][nb];
		int[] indexes = new int[nb];
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int nodeInNewGraph = 0;
		for(int j=subset.getFirstElement(); j>=0;j=subset.getNextElement()){
			indexes[nodeInNewGraph] = j;
			map.put(j, nodeInNewGraph);
			nodeInNewGraph++;
		}
		nodeInNewGraph = 0;
		INeighbors nei;
		for(int j=subset.getFirstElement(); j>=0;j=subset.getNextElement()){
			indexes[nodeInNewGraph] = j;
			map.put(j, nodeInNewGraph);
			nodeInNewGraph++;
			nei = undirectedGraph.getNeighborsOf(j);
			for(int k=nei.getFirstElement(); k>=0;k=nei.getNextElement()){
				if (map.get(k)!=null){
					matrix[nodeInNewGraph][map.get(k)] = true;
				}
			}
			nodeInNewGraph++;
		}
		return new DirectedGraph(nb, matrix, undirectedGraph.getType());
	}

	//***********************************************************************************
	// INeighbors operations
	//***********************************************************************************

	/**Merge two INeighbors of the same IGraph
	 * in O(#n1 + #n2)
	 * @param n1 first neighborhood to merge
	 * @param n2 second neighborhood to merge
	 * @param nbNodes the number of nodes
	 * @return a new INeighbors which is the result of the merging of n1 and n2
	 * this new INeighbors is a BitSet to avoid duplicated elements
	 */
	public static INeighbors mergeNeighborhoods(INeighbors n1, INeighbors n2, int nbNodes){
		BitSetNeighbors merged = new BitSetNeighbors(nbNodes);
		for(int j=n1.getFirstElement(); j>=0;j=n1.getNextElement()){
			merged.add(j);
		}
		for(int j=n2.getFirstElement(); j>=0;j=n2.getNextElement()){
			merged.add(j);
		}
		return merged;
	}
}
