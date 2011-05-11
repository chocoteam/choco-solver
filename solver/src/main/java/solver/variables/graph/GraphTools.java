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

import java.util.HashMap;

import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

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
	public static int[] performDFS(int root, IGraph graph){
		int nb = graph.getNbNodes();
        AbstractNeighborsIterator<INeighbors>[] neighbors = new AbstractNeighborsIterator[nb];
		int[] father = new int[nb];
		int[] num = new int[nb];
		for (int i=0; i<nb; i++){
			father[i] = -1;
            neighbors[i] = graph.neighborsIteratorOf(i);
		}
		int i = root;
		int k = 0;
		num[root] = k;
		father[root] = root;
		int j;
		while((i!=root) || neighbors[i].hasNext()){
			if(!neighbors[i].hasNext()){
				i = father[i];
			}else{
                j = neighbors[i].next();
				if (father[j]==-1) {
					father[j] = i;
					i = j;
					neighbors[i] = graph.neighborsIteratorOf(i);
					k++;
					num[i] = k;
				}
			}
		}
		return num;
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
		AbstractNeighborsIterator<INeighbors> nodeIter = subset.iterator();
		int nb = subset.neighborhoodSize();
		if (skipFirst && nodeIter.hasNext()){
			nodeIter.next();//skip the first element
			nb--;
		}
		boolean[][] matrix = new boolean[nb][nb];
		int[] indexes = new int[nb];
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int nodeInNewGraph = 0;
		int nodeInG;
		while (nodeIter.hasNext()){
			nodeInG = nodeIter.next();
			indexes[nodeInNewGraph] = nodeInG;
			map.put(nodeInG, nodeInNewGraph);
			nodeInNewGraph++;
		}
		nodeInNewGraph = 0;
		nodeIter = subset.iterator();
		if (skipFirst && nodeIter.hasNext()){
			nodeIter.next();
		}		
		while (nodeIter.hasNext()){
			AbstractNeighborsIterator<INeighbors> succIter = graph.successorsIteratorOf(nodeIter.next());
			while (succIter.hasNext()){
				nodeInG = succIter.next();
				if (map.get(nodeInG)!=null){
					if (!reverse){
						matrix[nodeInNewGraph][map.get(nodeInG)] = true;
					}else{
						matrix[map.get(nodeInG)][nodeInNewGraph] = true;
					}
				}
			}
			nodeInNewGraph++;
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
		AbstractNeighborsIterator<INeighbors> nodeIter = subset.iterator();
		int nb = subset.neighborhoodSize();
		boolean[][] matrix = new boolean[nb][nb];
		int[] indexes = new int[nb];
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int nodeInNewGraph = 0;
		int nodeInG;
		while (nodeIter.hasNext()){
			nodeInG = nodeIter.next();
			indexes[nodeInNewGraph] = nodeInG;
			map.put(nodeInG, nodeInNewGraph);
			nodeInNewGraph++;
		}
		nodeInNewGraph = 0;
		nodeIter = subset.iterator();
		while (nodeIter.hasNext()){
			AbstractNeighborsIterator<INeighbors> neighbors = undirectedGraph.neighborsIteratorOf(nodeIter.next());
			while (neighbors.hasNext()){
				nodeInG = neighbors.next();
				if (map.get(nodeInG)!=null){
						matrix[nodeInNewGraph][map.get(nodeInG)] = true;
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
    	AbstractNeighborsIterator<INeighbors> iter = n1.iterator();
    	while(iter.hasNext()){
    		merged.add(iter.next());
    	}
    	iter = n2.iterator();
    	while(iter.hasNext()){
    		merged.add(iter.next());
    	}
		return merged;
    }
}
