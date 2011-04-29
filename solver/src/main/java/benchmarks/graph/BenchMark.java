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

package benchmarks.graph;

import solver.variables.graph.GraphType;
import solver.variables.graph.IGraph;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

/**Class to run to get benchmarks
 * @author info */
public class BenchMark {

	//***********************************************************************************
	// BENCH
	//***********************************************************************************

	/**Launch a benchmark on oriented and non oriented graphs
	 * @param resultsFileName to record results (undone yet) */
	private static void bench(String resultsFileName){
		String s = "\nrepresentation;orientation;n;m;nbCC;nbEffectiveCC;nbArts;algo;time;";
		bench(resultsFileName, false);
		bench(resultsFileName, true);
	}

	/**generate a bench of graphs with a specified orientation
	 * run DFS on them and print results in the file named resultsFileName
	 * @param resultsFileName
	 * @param oriented
	 */
	private static void bench(String resultsFileName, boolean oriented){
		IntObj io;
		int[] ns = new int[]{100,500,1000,2000,2500};
		int[] densities = new int[]{0,10,25,50,75,100};
		for (int n:ns){
			int[] CCNumbers = new int[]{0,1,n/10,n/4,n/2,n*3/4,n};
			for (int cc:CCNumbers){
				for (int d:densities){
					int m = n*n*d/100;
					if (cc>0){// les CC sont homogenes donc sont de tailles n/cc d'ou nbArcsMAx = n*n/cc et non n*n
						m = m/cc;
					}
					if(cc<=0 || m >= n-cc){// evite les cas ou il n'y a pas assez d'arcs pour respecter cc
						try{
							io = new IntObj(m);
//							DataWriter.writeTextInto(createAndtest_CC_Arts(n, io, cc,oriented),resultsFileName);
							if (io.getVal()<m){ //evite d'avoir deux fois le mm graphiques (s'il y a deja trop d'arcs)
								break;
							}
						}catch(Exception e){}
					}
				}
			}
		}
	}

	/**Create two graphs (matrix and list representation) regarding specs and test the DFS algorithm on them
	 * @param n expected number of nodes 
	 * @param m targeted number of arcs, but the real amount of arcs can be lower 
	 * @param cc expected number of connected components, 
	 * 			if(cc<=0) then the number of connected components is undefined
	 * @param oriented false iff the matrix must be symmetric, true otherwise
	 * @return the results of the tests
	 */
	private static String createAndtest_CC_Arts(int n, IntObj mObj, int cc, boolean oriented){
		System.out.println(n+" : "+mObj.getVal());
		boolean[][] model = MatrixGenerator.makeSGData(n, mObj, cc, oriented);
		System.out.println("graph ("+n+", "+mObj.getVal()+", "+cc+") generated");
		return test_CC_Arts(model, n, mObj.getVal(), cc, oriented);
	}

	/**Run a DFS test on two representations of a graph
	 * @param dataMatrix adjacency matrix
	 * @param oriented false iff the adjacency matrix is symmetric
	 * @return the results of the tests as a string
	 */
	private static String test_CC_Arts(boolean[][] dataMatrix, boolean oriented){
		return test_CC_Arts(dataMatrix, dataMatrix.length, -1, 0, oriented);
	}

	/**create two graphs with different representations, run DFS test on them
	 * @param dataMatrix
	 * @param n real number of nodes
	 * @param m real number of arcs 
	 * @param cc expected number of connected components
	 * @param oriented 
	 * @return results of the tests as a string
	 */
	private static String test_CC_Arts(boolean[][] dataMatrix, int n, int m, int cc, boolean oriented){
		IGraph g;
		GraphType type = GraphType.DENSE;
		if(m/n<Math.sqrt(n)){
			type = GraphType.SPARSE;
		}
		if(oriented){
			g = new DirectedGraph(n, dataMatrix, type);
		}else{
			g = new UndirectedGraph(n, dataMatrix, type);
		}
		long time = System.currentTimeMillis();
		System.out.println(ConnectivityFinder.findAllCCandAP(g).toString());
		System.out.println("time ms : "+(System.currentTimeMillis()-time));
		if(oriented){
			time = System.currentTimeMillis();
			System.out.println(StrongConnectivityFinder.findAll((IDirectedGraph) g));
			System.out.println("time ms : "+(System.currentTimeMillis()-time));
		}
		return "";
	}

	/** A small test that can be checked easily */
	private static void smallTest(){
		boolean[][] m = new boolean[7][7];
		m[1][2] = true;
		m[2][1] = true;
		m[1][3] = true;
		m[3][1] = true;
		m[3][2] = true;
		m[2][3] = true;
//		m[2][5] = true;//
		m[3][4] = true;
		m[4][3] = true;
		m[4][5] = true;
		m[5][4] = true;
		m[4][6] = true;
		m[6][4] = true;
		m[5][6] = true;
		m[6][5] = true;
		System.out.println(test_CC_Arts(m,true));
	}

	//***********************************************************************************
	// MAIN
	//***********************************************************************************

	
	public static void main(String[] args) {
//		bench("ADAPTED_DFS_SG.csv");
//		System.out.println(createAndtest_CC_Arts(200, new IntObj(1900), 2,true));
		smallTest();
	}
}
