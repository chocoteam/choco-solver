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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 19/12/12
 * Time: 14:54
 */

package samples.graph;

import solver.Solver;
import solver.constraints.gary.GraphConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.variables.graph.UndirectedGraphVar;
import solver.variables.view.Views;

/**
 * Example for enumerating some cliques in a given undirected graph
 * @author Jean-Guillaume Fages
 */
public class Cliques {

	public static void main(String[] args) {
		int n = 5;
		boolean[][] ex = new boolean[n][n];
		ex[1][2] = true;
		ex[2][3] = true;
		ex[2][4] = true;
		ex[1][3] = true;
		ex[1][4] = true;
		ex[3][4] = true;
		findCliques(ex,1,2);
	}

	/**
	 * Enumerates all cliques of the input graph which contains edge (x,y)
	 * (A clique has a loop on each of its nodes)
	 * @param adjMatrix (input graph)
	 * @param x a node of the graph
	 * @param y a node of the graph
	 */
	public static void findCliques(boolean[][] adjMatrix,final int x, int y) {
		Solver solver = new Solver();
		// variable
		final UndirectedGraphVar g = createGraphVar(adjMatrix,1,2,solver);
		// constraint
		solver.post(GraphConstraintFactory.nCliques(g, Views.fixed(1, solver), solver));
		// search strategy (lexicographic)
		solver.set(StrategyFactory.graphLexico(g));
		// log
		SearchMonitorFactory.log(solver, true, false);
		solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor(){
			public void onSolution(){
				System.out.println(g.getEnvelopGraph().getNeighborsOf(x));
			}
		});
		// enumeration
		solver.findAllSolutions();
	}

	private static UndirectedGraphVar createGraphVar(boolean[][] adjMatrix, int x, int y, Solver solver){
		int n = adjMatrix.length;
		final UndirectedGraphVar g = new UndirectedGraphVar(solver, n, false);
		for(int i=0;i<n;i++) {
			g.getEnvelopGraph().activateNode(i);		// potential node
			g.getEnvelopGraph().addEdge(i, i);			// potential loop
			for(int j=i+1;j<n;j++) {
				if(adjMatrix[i][j]) {
					g.getEnvelopGraph().addEdge(i, j);	// potential edge
				}
			}
		}
		g.getKernelGraph().activateNode(x);	// mandatory node
		g.getKernelGraph().activateNode(y);	// mandatory node
		g.getEnvelopGraph().addEdge(x,x);	// mandatory loop
		g.getEnvelopGraph().addEdge(y,y);	// mandatory loop
		g.getKernelGraph().addEdge(x,y);	// mandatory edge
		return g;
	}
}
