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

import choco.kernel.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.directed.PropOneSuccBut;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.directed.PropSumArcCosts;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSurroSymmetricHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.TSP_heuristics;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;

public class TSP_heur {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 300000;
	private static String outFile;
	private static int upperBound = Integer.MAX_VALUE/4;
	private static IntVar totalCost;
	private static Solver solver;
	private static boolean allDiffAC    = false;
	private static boolean optProofOnly = true;
	private static PropSymmetricHeldKarp mst;
	private static int search;
	private static boolean decisionType, trick, constructive;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		bench();
	}

	static int policy;

	public static void bench() {
		TSP_CP12.clearFile(outFile = "tsp_heur.csv");
		TSP_CP12.writeTextInto("instance;sols;fails;nodes;time;obj;strat;enforce;trick;construct;\n", outFile);
		String dir = "/Users/jfages07/github/In4Ga/benchRousseau";
//		String dir = "/Users/jfages07/github/In4Ga/ALL_tsp";
//		String dir = "/Users/jfages07/github/In4Ga/mediumTSP";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix;
		optProofOnly = true;
		allDiffAC = false;
		search = 0;
		policy = 4;
		ATSP_ISMP.resetFile();
		for (String s : list) {
//			if(s.contains("pr299"))
			if (s.contains(".tsp") && (!s.contains("gz")) && (!s.contains("lin"))){
				matrix = TSP_CP12.parseInstance(dir + "/" + s);
				if((matrix!=null && matrix.length>=0 && matrix.length<100)){
					if(optProofOnly){
						setUB(s.split("\\.")[0]);
						System.out.println("optimum : "+upperBound);
					}
					decisionType = true;
					for(policy=GraphStrategyBench2.FIRST;policy<=GraphStrategyBench2.LAST;policy++){
						solve(matrix, s);
					}
					decisionType = false;
					for(policy=GraphStrategyBench2.FIRST;policy<=GraphStrategyBench2.LAST;policy++){
						solve(matrix, s);
					}
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
	}

	private static void setUB(String s) {
		File file = new File("/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			while(!line.contains(s)){
				line = buf.readLine();
			}
			upperBound = Integer.parseInt(line.split(";")[1]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void solve(int[][] matrix, String instanceName) {
		final int n = matrix.length;
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j] != matrix[j][i]){
					System.out.println(i+" : "+j);
					System.out.println(matrix[i][j]+" != "+matrix[j][i]);
					throw new UnsupportedOperationException();
				}
			}
		}
		solver = new Solver();
		// variables
		totalCost = VariableFactory.bounded("obj",0,upperBound,solver);
		final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		for(int i=0;i<n;i++){
			undi.getKernelGraph().activateNode(i);
			for(int j=i+1;j<n;j++){
				undi.getEnvelopGraph().addEdge(i,j);
			}
		}
		// constraints
		Constraint gc = GraphConstraintFactory.makeConstraint(solver);
		gc.addPropagators(new PropCycleNoSubtour(undi, gc, solver));
		gc.addPropagators(new PropAtLeastNNeighbors(undi, 2, gc, solver));
		gc.addPropagators(new PropAtMostNNeighbors(undi, 2, gc, solver));
		gc.addPropagators(new PropCycleEvalObj(undi, totalCost, matrix, gc, solver));
		mst = PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
		gc.addPropagators(mst);
		solver.post(gc);
		// config
		if(search!=0){
			throw new UnsupportedOperationException("not implemented");
		}
		GraphStrategyBench2 strategy = new GraphStrategyBench2(undi,matrix,mst);
		strategy.configure(policy,decisionType,trick,constructive);
		solver.set(strategy);
		IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		checkUndirected(solver, undi, totalCost, matrix);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ solver.getMeasures().getNodeCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost + ";"+policy;
		if(decisionType){
			txt+=";1";
		}else{
			txt+=";0";
		}
		if(trick){
			txt+=";1";
		}else{
			txt+=";0";
		}
		if(constructive){
			txt+=";1";
		}else{
			txt+=";0";
		}
		txt+= ";\n";
		TSP_CP12.writeTextInto(txt, outFile);
	}

	private static void checkUndirected(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
		int n = matrix.length;
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			TSP_CP12.writeTextInto("BUG\n",outFile);
			throw new UnsupportedOperationException();
		}
		if(solver.getMeasures().getSolutionCount() > 0){
			int sum = 0;
			for(int i=0;i<n;i++){
				for(int j=i+1;j<n;j++){
					if(undi.getEnvelopGraph().edgeExists(i,j)){
						sum+=matrix[i][j];
					}
				}
			}
			if(sum!=solver.getSearchLoop().getObjectivemanager().getBestValue()){
				TSP_CP12.writeTextInto("BUG\n",outFile);
				throw new UnsupportedOperationException();
			}
		}
	}

}