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
 * Date: 18/10/12
 * Time: 17:39
 */

package samples.graph;

import choco.kernel.ResolutionPolicy;
import choco.kernel.memory.IStateInt;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.PropCyclePathChanneling;
import solver.constraints.propagators.gary.tsp.directed.*;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTour;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTourGraphReactor;
import solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation.PropLagr_MST_BST;
import solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation.PropLagr_MST_BSTdual;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.objective.strategies.BottomUp_Minimization;
import solver.objective.strategies.Dichotomic_Minimization;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.ISet;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;
import java.util.BitSet;
import java.util.Random;

public class SCC_ATSP {


	private static final long TIMELIMIT = 60000;
	private static String outFile = "scc_atsp.csv";
	// instance
	public static String instanceName;
	public static int[][] distanceMatrix;
	public static int n, noVal, initialUB;
	private static Solver solver;
	// model
	private static DirectedGraphVar graph;
	private static IntVar totalCost;
	private static Constraint gc;
	// RG data structure
	private static IStateInt nR;
	private static IStateInt[] sccOf;
	private static ISet[] outArcs;
	private static IDirectedGraph G_R;
	private static IStateInt[] sccFirst, sccNext;
	// Branching data structure
	private static IGraphRelaxation relax;

	//***********************************************************************************
	// MODEL CONFIGURATION
	//***********************************************************************************

	public static int arbo=0,rg=1,undirectedMate=2,pos=3,allDiff=4;//,time=5;
	public static int NB_PARAM = 5;
	private static BitSet config = new BitSet(NB_PARAM);
	public static boolean bst;
	public static boolean khun;
	private static IntVar[] positions;
	private static int lbini;
	private static int m;
	private static Constraint gc2;

	public static void configParameters(int mask) {
		String bytes = Integer.toBinaryString(mask);
		while(bytes.length()<NB_PARAM){
			bytes = "0"+bytes;
		}
		for(int i=0;i<bytes.length();i++){
			config.set(i,bytes.charAt(NB_PARAM-1-i)=='1');
		}
	}

	//***********************************************************************************
	// SEARCH CONFIGURATION
	//***********************************************************************************

	private static int main_search = 2;
	private static String[] searchMode = new String[]{"top-down","bottom-up","dichotomic"};

	//***********************************************************************************
	// MODEL-SEARCH-RESOLUTION-OUTPUT
	//***********************************************************************************

	public static void solve() {
		createModel();
		addPropagators();
		configureAndSolve();
	}

	public static void createModel() {
		// create model
		solver = new Solver();
		System.out.println("initial UB : "+initialUB);
		graph = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST,true);
		totalCost = VariableFactory.bounded("total cost ", 0, initialUB, solver);
		int ct = 0;
		try {
			for (int i = 0; i < n - 1; i++) {
				graph.getKernelGraph().activateNode(i);
				for (int j = 1; j < n; j++) {
					if (distanceMatrix[i][j] != noVal) {
						ct++;
						graph.getEnvelopGraph().addArc(i, j);
					}
				}
				graph.getEnvelopGraph().removeArc(i, i);
			}
			System.out.println(ct+" / "+n);
			graph.getKernelGraph().activateNode(n-1);
			graph.getEnvelopGraph().removeArc(0, n-1);
			graph.getEnvelopGraph().removeArc(n-1,0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		gc = GraphConstraintFactory.makeConstraint(solver);
		gc2 = gc;//GraphConstraintFactory.makeConstraint(solver);
	}

	public static void addPropagators() {
		// BASIC MODEL
		gc.addPropagators(new PropOneSuccBut(graph, n - 1, gc, solver));
		gc.addPropagators(new PropOnePredBut(graph, 0, gc, solver));

		gc.addPropagators(new PropPathNoCycle(graph, 0, n - 1, gc, solver));
		gc.addPropagators(new PropSumArcCosts(graph, totalCost, distanceMatrix, gc, solver));
		if(config.get(allDiff)){
			gc.addPropagators(new PropAllDiffGraphIncremental(graph, n - 1, solver, gc));
		}
		// STRUCTURAL FILTERING
		if (config.get(arbo)) {
			gc.addPropagators(new PropArborescence(graph, 0, gc, solver, false));
			gc.addPropagators(new PropAntiArborescence(graph, n - 1, gc, solver, false));
		}
		if (config.get(rg)) {
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			sccFirst = RP.getSCCFirst();
			sccNext = RP.getSCCNext();
			gc.addPropagators(RP);
			PropSCCDoorsRules SCCP = new PropSCCDoorsRules(graph, gc, solver, nR, sccOf, outArcs, G_R, sccFirst, sccNext);
			gc.addPropagators(SCCP);
		}
		if(config.get(undirectedMate)){
			UndirectedGraphVar undi = new UndirectedGraphVar(solver,n-1,GraphType.MATRIX,GraphType.LINKED_LIST,true);
			ISet nei;
			for(int i=0;i<n-1;i++){
				undi.getKernelGraph().activateNode(i);
				nei = graph.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(j==n-1){
						undi.getEnvelopGraph().addEdge(i,0);
					}else{
						undi.getEnvelopGraph().addEdge(i,j);
					}
				}
			}
			gc.addPropagators(new PropCycleNoSubtour(undi, gc, solver));
			gc.addPropagators(new PropAtLeastNNeighbors(undi, 2, gc, solver));
			gc.addPropagators(new PropAtMostNNeighbors(undi, 2, gc, solver));
			gc.addPropagators(new PropCyclePathChanneling(graph, undi, gc, solver));
		}
		if(config.get(pos)){
			IntVar[] pos = VariableFactory.enumeratedArray("pos", n, 0, n - 1, solver);
			positions = pos;
			try{
				pos[0].instantiateTo(0, Cause.Null);
				pos[n-1].instantiateTo(n - 1, Cause.Null);
			}catch(Exception e){
				e.printStackTrace();System.exit(0);
			}
			gc.addPropagators(new PropPosInTour(pos, graph, gc, solver));
			if(config.get(rg)){
				gc.addPropagators(new PropPosInTourGraphReactor(pos, graph, gc, solver, nR, sccOf, outArcs, G_R));
			}else{
				gc.addPropagators(new PropPosInTourGraphReactor(pos, graph, gc, solver));
			}
			solver.post(new AllDifferent(pos,solver, AllDifferent.Type.AC));
		}
		// COST BASED FILTERING

// else{
//			System.out.println("MST");
//			PropHeldKarp propHK_mst = PropHeldKarp.mstBasedRelaxation(graph, 0, n-1, totalCost, distanceMatrix, gc, solver);
//			propHK_mst.waitFirstSolution(false);//search!=1 && initialUB!=optimum);
//			gc.addPropagators(propHK_mst);
//			relax = propHK_mst;
//		PropHeldKarp propHK_mst0 = PropHeldKarp.mstBasedRelaxation(graph, 0, n-1, totalCost, distanceMatrix, gc2, solver);
//		propHK_mst0.waitFirstSolution(initialUB!=optimum);//search!=1 && initialUB!=optimum);
//		gc2.addPropagators(propHK_mst0);
//		relax = propHK_mst0;

		if(config.get(rg) && bst){// BST-based HK
			System.out.println("BST");
			PropLagr_MST_BSTdual propHK_bst = PropLagr_MST_BSTdual.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc2, solver, nR, sccOf, outArcs);
//			PropHeldKarp propHK_bst = PropHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs);
			propHK_bst.waitFirstSolution(true);//search!=1 && initialUB!=optimum);
			gc2.addPropagators(propHK_bst);
//			relax = propHK_bst;
		}
		else{
			System.out.println("MST");
			PropLagr_MST_BST propHK_mst = PropLagr_MST_BST.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc2, solver);
			propHK_mst.waitFirstSolution(true);//search!=1 && initialUB!=optimum);
			gc2.addPropagators(propHK_mst);
			relax = propHK_mst;
		}

//			System.out.println("MST");
		PropLagr_MST_BST propHK_mst = PropLagr_MST_BST.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver);
		propHK_mst.waitFirstSolution(true);//search!=1 && initialUB!=optimum);
		gc.addPropagators(propHK_mst);
		relax = propHK_mst;

		if(khun){
			PropKhun map = new PropKhun(graph,totalCost,distanceMatrix,solver,gc);
			gc.addPropagators(map);
//			relax = map;
		}
//		}
		solver.post(gc);
//		solver.post(gc2);
	}

	public static void configureAndSolve() {
		//SOLVER CONFIG
		AbstractStrategy mainStrat = null;
		GraphStrategyBench2 strat = new GraphStrategyBench2(graph,distanceMatrix,relax);
		strat.configure(policy,true,true,false);
		mainStrat = strat;
		switch (main_search){
			case 0: solver.set(mainStrat);
				break;
			case 1: solver.set(new StaticStrategiesSequencer(new BottomUp_Minimization(totalCost),mainStrat));break;
			case 2: solver.set(new StaticStrategiesSequencer(new Dichotomic_Minimization(totalCost,solver),mainStrat));break;
			default: throw new UnsupportedOperationException();
		}

		IPropagationEngine pengine = new PropagationEngine(solver.getEnvironment());
		PArc allArcs = new PArc(pengine, gc);
		solver.set(pengine.set(
				new Sort(allArcs).clearOut())
		);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		lbini = 0;
		solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor(){
			public void afterInitialPropagation() {
				if(totalCost.instantiated()){
					solver.getSearchLoop().stopAtFirstSolution(true);
				}
				System.out.println("%%%%%%%%%%%%%%%%%%%");
				System.out.println("DeltaObj after prop ini: "+(totalCost.getUB()-totalCost.getLB()));
				lbini = totalCost.getLB();
				int ct = 0;
				for(int i=0;i<n;i++){
					ct+=graph.getEnvelopGraph().getSuccessorsOf(i).getSize();
				}
				m = ct;
				System.out.println(ct+" arcs remaining");
				System.out.println("%%%%%%%%%%%%%%%%%%%");
//				lbini = (double)(optimum-totalCost.getLB())/(double)optimum;
//				lbini = Math.round(lbini*1000000)/1000000;
// solver.getSearchLoop().interrupt();
			}
//			public void onSolution() {
//				System.out.println("youhou");
//			}
//			public void onContradiction(ContradictionException cex) {
//				System.out.println("yaha");
//			}
		});
		SearchMonitorFactory.log(solver, true, false);
		//SOLVE
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
//			throw new UnsupportedOperationException();
		}
		// OUTPUT
		int bestCost = totalCost.getLB();

		String configst = "";
		for(int i=0;i<NB_PARAM;i++){
			if(config.get(i)){
				configst += "1;";
			}else{
				configst += "0;";
			}
		}
		if(bst){
			configst += "1;";
		}else{
			configst += "0;";
		}
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" +
				solver.getMeasures().getFailCount() + ";"+solver.getMeasures().getNodeCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount())+";"+lbini +  ";" + bestCost+";"+m + ";"+searchMode[main_search]+";"+configst+"\n";
		writeTextInto(txt, outFile);
	}

	//***********************************************************************************
	// BENCHMARK
	//***********************************************************************************


	static int policy;


	public final static int[] configs = new int[]{
			0,
			(1<<allDiff),
			(1<<arbo),
			(1<<pos),
			(1<<rg),
			(1<<rg)+(1<<arbo)+(1<<allDiff),
			(1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff)
	};
	public final static int[] bstconfigs = new int[]{
			(1<<rg),
			(1<<rg)+(1<<arbo)+(1<<allDiff),
			(1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff)
	};

	//***********************************************************************************
	// RECORDING RESULTS
	//***********************************************************************************

	public static void writeTextInto(String text, String file) {
		try {
			FileWriter out = new FileWriter(file, true);
			out.write(text);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearFile(String file) {
		try {
			FileWriter out = new FileWriter(file, false);
			out.write("");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int[][] generateCosts(int n, int maxCost, int maxSCC, Random rd){
		int[][] dist = new int[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				dist[i][j] = rd.nextInt(maxCost);
			}
		}
		int[] sccs = generateSCCs(n,maxSCC,rd);
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(sccs[i]>sccs[j]){
					dist[i][j] = maxCost;
				}
			}
		}
		return dist;
	}

	private static int[] generateSCCs(int n, int max, Random rd){
		int[] scc = new int[n];
		scc[0] = 0;
		scc[n-1] = max;
		for(int i=1;i<n-1;i++){
			scc[i] = 1+rd.nextInt(max);
		}
		return scc;
	}

	public static void main(String[] args){
		clearFile(outFile);
		writeTextInto("instance;sols;fails;nodes;time;iniGap;obj;m;search;arbo;rg;undi;pos;adAC;bst;\n", outFile);
		main_search = 1;
		policy = 7;
		n = 100;
		khun = true;
		Random rd = new Random(0);
		int maxCost = 100;
		for(int s = 1; s<10; s++){
			distanceMatrix = generateCosts(n,maxCost,s,rd);
			instanceName = n+"_"+s;
			noVal = maxCost;
			initialUB = maxCost * n;
			bst = false;
			configParameters((1<<pos)+(1<<allDiff));
			solve();
			configParameters((1<<rg)+(1<<pos)+(1<<allDiff));
			solve();
			bst = true;
			configParameters((1<<rg)+(1<<pos)+(1<<allDiff));
			solve();
//			System.exit(0);
		}
	}
}
