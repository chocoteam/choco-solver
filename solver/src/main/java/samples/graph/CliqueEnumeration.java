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

package samples.graph;

import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.gary.GraphConstraintFactory;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.GraphStrategyFactory;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;

/**
 * This sample illustrates how to use a graph variable to
 * enumerate all cliques that respect certain conditions
 * In this example, we enumerates cliques which contain edge (1,2)
 * by using a graph variable and a clique partitioning constraint
 *
 * @author Jean-Guillaume Fages
 */
public class CliqueEnumeration extends AbstractProblem {

    // graph variable
    private UndirectedGraphVar graphvar;
    // five nodes are involved
    private int n = 5;

    public static void main(String[] args) {
        new CliqueEnumeration().execute(args);
    }

    @Override
    public void createSolver() {
        solver = new Solver("clique enumeration");
    }

    @Override
    public void buildModel() {
        // input data
        boolean[][] link = new boolean[n][n];
        link[1][2] = true;
        link[2][3] = true;
        link[2][4] = true;
        link[1][3] = true;
        link[1][4] = true;
        link[3][4] = true;
        // graph variable
        graphvar = VariableFactory.undirectedGraph("G", n, solver);
        // initial domain
        for (int i = 0; i < n; i++) {
            graphvar.getEnvelopGraph().activateNode(i);            // potential node
            graphvar.getEnvelopGraph().addEdge(i, i);            // potential loop
            for (int j = i + 1; j < n; j++) {
                if (link[i][j]) {
                    graphvar.getEnvelopGraph().addEdge(i, j);    // potential edge
                }
            }
        }
        // 1 and 2 must belong to the same clique
        graphvar.getKernelGraph().activateNode(1);        // mandatory node
        graphvar.getKernelGraph().activateNode(2);        // mandatory node
        graphvar.getEnvelopGraph().addEdge(1, 1);        // mandatory loop
        graphvar.getEnvelopGraph().addEdge(2, 2);        // mandatory loop
        graphvar.getKernelGraph().addEdge(1, 2);        // mandatory edge
        // constraint : the graph must be a clique
        solver.post(GraphConstraintFactory.nCliques(graphvar, VariableFactory.fixed(1, solver)));
    }

    @Override
    public void configureSearch() {
        // search strategy (lexicographic)
        solver.set(GraphStrategyFactory.graphLexico(graphvar));
        // log
        SearchMonitorFactory.log(solver, true, false);
        solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
            public void onSolution() {
                System.out.println("solution found : " + graphvar.getEnvelopGraph().getNeighborsOf(1));
            }
        });
    }

    @Override
    public void solve() {
        // enumeration
        solver.findAllSolutions();
        // log
        SearchMonitorFactory.log(solver, true, false);
    }

    @Override
    public void prettyOut() {
    }
}
