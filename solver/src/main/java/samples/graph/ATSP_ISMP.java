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

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class ATSP_ISMP {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 1000000;
	private static String outFile = "atsp.csv";
	private static int seed = 0;
	// instance
	public static String instanceName;
	public static int[][] distanceMatrix;
	public static int n, noVal, optimum, initialUB;
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
		initialUB = optimum;// = 1027751;
		System.out.println("initial UB : "+optimum);
		graph = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST,true);
		totalCost = VariableFactory.bounded("total cost ", 0, initialUB, solver);
		int l = (int)Math.sqrt(n);
		int ct = 0;
		try {
			for (int i = 0; i < n - 1; i++) {
				graph.getKernelGraph().activateNode(i);
//				int xi = i/l;
//				int yi = i%l;
				for (int j = 1; j < n; j++) {
					if (distanceMatrix[i][j] != noVal) {
//						int xj = j/l;
//						int yj = j%l;
//						if(Math.abs(xi-xj)<=1 && Math.abs(yi-yj)<=1 && Math.abs(yi-yj)+Math.abs(xi-xj)==1){
						ct++;
						graph.getEnvelopGraph().addArc(i, j);
//						}
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
//		System.out.println(graph.getEnvelopGraph());
//		System.exit(0);
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
			propHK_bst.waitFirstSolution(initialUB != optimum);//search!=1 && initialUB!=optimum);
			gc2.addPropagators(propHK_bst);
//			relax = propHK_bst;
		}
		else{
			System.out.println("MST");
			PropLagr_MST_BST propHK_mst = PropLagr_MST_BST.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc2, solver);
			propHK_mst.waitFirstSolution(initialUB != optimum);//search!=1 && initialUB!=optimum);
			gc2.addPropagators(propHK_mst);
			relax = propHK_mst;
		}

//			System.out.println("MST");
		PropLagr_MST_BST propHK_mst = PropLagr_MST_BST.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver);
		propHK_mst.waitFirstSolution(initialUB != optimum);//search!=1 && initialUB!=optimum);
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
//		solver.getSearchLoop().getLimitsBox().setNodeLimit(1);
		//SOLVER CONFIG
		AbstractStrategy mainStrat = null;//StrategyFactory.graphATSP(graph, heuristic, relax);

//		switch(policy){
//			case 7 : mainStrat = StrategyFactory.graphATSP(graph, ATSP_heuristics.enf_sparse, relax);break;
//			case 8 : mainStrat = StrategyFactory.graphATSP(graph, ATSP_heuristics.enf_sparse_corrected, relax);break;
//			case 9 : mainStrat = StrategyFactory.graphATSP(graph, ATSP_heuristics.sparse_corrected, relax);break;
//			default: mainStrat = StrategyFactory.graphStrategy(graph, null,new GraphStrategyBench(graph,distanceMatrix,relax,policy,true), GraphStrategy.NodeArcPriority.ARCS);
//		}
		GraphStrategyBench2 strat = new GraphStrategyBench2(graph,distanceMatrix,relax);
		strat.configure(policy,true,true,false);
		mainStrat = strat;
//		mainStrat = StrategyFactory.graphATSP(graph, ATSP_heuristics.enf_sparse, relax);
//		mainStrat = StrategyFactory.graphLexico(graph);
//		mainStrat = StrategyFactory.ABSrandom(positions,solver,0.999d, 0.2d, 8, 1.1d, 1, 0);
//		mainStrat = StrategyFactory.inputOrderMinVal(positions,solver.getEnvironment());
//		mainStrat = StrategyFactory.random(positions,solver.getEnvironment());
//		mainStrat = StrategyFactory.minDomMinVal(positions,solver.getEnvironment());
//		mainStrat = StrategyFactory.maxRegMinVal(positions,solver.getEnvironment());

//		HeuristicValFactory.random(positions);
//        mainStrat = StrategyVarValAssign.dyn(positions,
//				SorterFactory.random(0),
//				ValidatorFactory.instanciated,
//				solver.getEnvironment());

//		mainStrat = StrategyFactory.domwdegMindom(positions,0);

//		mainStrat = StrategyFactory.graphStrategy(graph, null,new LexArcs(graph), GraphStrategy.NodeArcPriority.ARCS);
		switch (main_search){
			case 0: solver.set(mainStrat);
				break;
			case 1: solver.set(new StaticStrategiesSequencer(new BottomUp_Minimization(totalCost),mainStrat));break;
			case 2: solver.set(new StaticStrategiesSequencer(new Dichotomic_Minimization(totalCost,solver),mainStrat));break;
			default: throw new UnsupportedOperationException();
		}

		IPropagationEngine pengine = new PropagationEngine(solver.getEnvironment());
//		PArc arcs1 = new PArc(pengine, gc);
//		PArc arcs2 = new PArc(pengine, gc2);
//		solver.set(pengine.set(
//				new Sort(
//				new Sort(arcs1).clearOut(),
//				new Queue(arcs2).sweepUp()).clearOut())
//		);
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
//		solver.findAllSolutions();
//		solver.findSolution();
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
//			throw new UnsupportedOperationException();
		}
		// OUTPUT
//		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
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

	public static void main(String[] args) {
		resetFile();
		bench();
//		benchRandom();
//		String instance = "/Users/jfages07/github/In4Ga/atsp_instances/ft53.atsp";
//		testInstance(instance);
//		trans();

	}

	public static void resetFile() {
		outFile = "atsp_fast.csv";
		clearFile(outFile);
		writeTextInto("instance;sols;fails;nodes;time;iniGap;obj;m;search;arbo;rg;undi;pos;adAC;bst;\n", outFile);
	}
	public static void reset(int[][] m, String s, int p, int cp, int ub) {
		distanceMatrix = m;
		instanceName = s;
		policy = p;
		n = distanceMatrix.length;
		configParameters(cp);
		noVal = -1;
		optimum = initialUB = ub;
	}

	private static void trans(){
		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
		File folder = new File(dir);
		String[] list = folder.list();
		for (String s : list) {
			if ((s.contains(".atsp"))){
				trans(dir+"/"+s);
			}
		}
	}

	private static void trans(String url){
		loadInstance(url);
		System.out.println(instanceName);
		int n1 = distanceMatrix.length-1;
		int n2 = 2*n1;
		int[][] dist2 = new int[n2][n2];
		for(int i=0;i<n2;i++){
			for(int j=0;j<n2;j++){
				dist2[i][j] = 999999;
			}
		}
		for(int i=0;i<n1;i++){
			dist2[i][i+n1] = dist2[i+n1][i] = 0;
			for(int j=0;j<n1;j++){
				if(i!=j){
					dist2[j+n1][i] = dist2[i][j+n1] = distanceMatrix[i][j];
				}
			}
		}
		String s="";
		for(int i=0;i<n2;i++){
			String st = "\n";
			for(int j=0;j<n2;j++){
				st+="\t"+dist2[i][j];
			}
			s+=st;
		}
		String debut = "NAME: "+instanceName
				+"\nTYPE: TSP"
				+"\nCOMMENT: Asymmetric TSP (Fischetti) transformed into a (symmetric) TSP"
				+"\nDIMENSION: "+n2
				+"\nEDGE_WEIGHT_TYPE: EXPLICIT"
				+"\nEDGE_WEIGHT_FORMAT: FULL_MATRIX"
				+"\nEDGE_WEIGHT_SECTION";
		String file = "tsp_"+instanceName;
		clearFile(file);
		writeTextInto(debut,file);
		writeTextInto(s,file);
	}

	static int policy;
	private static void bench() {
		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
//		String dir = "/Users/jfages07/github/In4Ga/newATSP";
		File folder = new File(dir);
		String[] list = folder.list();
		main_search = 0;
		policy = 8;
		configParameters(0);
		for (String s : list) {
//			if(s.contains("170"))
			File file = new File(dir + "/" + s);
			if(s.contains("170"))
			if(file.isFile() && !(file.isHidden() || s.contains(".xls") || s.contains(".csv")))
				if ((s.contains(".atsp"))){// && (!s.contains("ftv170")) && (!s.contains("p43"))){
//				if(s.contains("p43.atsp"))System.exit(0);
				if(s.contains("rbg")){
					System.exit(0);
				}
//					khun = true;
					loadInstance(dir + "/" + s);
					bst = false;
					configParameters((1 << allDiff));
//					solve();
//					configParameters((1<<pos)+(1<<allDiff));
//					solve();
					configParameters((1<<rg)+(1<<allDiff));
					solve();
//					configParameters((1<<arbo)+(1<<allDiff));
//					solve();
//					configParameters((1<<rg)+(1<<pos)+(1<<allDiff));
//					solve();
//					configParameters((1<<rg)+(1<<arbo)+(1<<allDiff));
//					solve();
//					configParameters((1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff));
//					solve();
					bst = true;
					solve();
//					configParameters((1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff));
//					solve();
//					configParameters((1<<arbo)+(1<<rg)+(1<<allDiff));
//					solve();
//					System.exit(0);
				}
		}
	}
	private static void benchNew() {
//		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
		String dir = "/Users/jfages07/github/In4Ga/newATSP";
		File folder = new File(dir);
		String[] list = folder.list();
		main_search = 0;
		policy = 4;
		configParameters(0);
		for (String s : list) {
//			if(s.contains("170"))
			File file = new File(dir + "/" + s);
			if(!s.contains("ND122644m"))
				if(!s.contains("ND122943m"))
					if(!s.contains("ND143341b"))
						if(!s.contains("ND163440"))
							if(!s.contains("ND163742b"))
								if(!s.contains("ND184040a"))
									if(file.isFile() && !(file.isHidden() || s.contains(".xls") || s.contains(".csv")))
										if ((s.contains(".atsp") || true)){// && (!s.contains("ftv170")) && (!s.contains("p43"))){
//				if(s.contains("p43.atsp"))System.exit(0);
//				loadInstance(dir + "/" + s);
											loadNewInstance(file);
											loadNewOpt(file.getName());
											if(n>0 && n<1000){// || s.contains("p43.atsp")){
												bst = false;
//					String is = "";
//					for(int i=0;i<n;i++){
//						String st = "\n";
//						for(int j=0;j<n;j++){
//							st+=distanceMatrix[i][j]+"\t";
//						}
//						is += st;
//					}
//					System.out.println(initialUB);
//					System.out.println(is);
//					System.exit(0);
//												khun = true;
												configParameters((1<<allDiff));
												solve();
//					System.exit(0);
					configParameters((1<<pos)+(1<<allDiff));
					solve();
					configParameters((1<<rg)+(1<<allDiff));
					solve();
					configParameters((1<<arbo)+(1<<allDiff));
					solve();
					configParameters((1<<rg)+(1<<pos)+(1<<allDiff));
					solve();
					configParameters((1<<rg)+(1<<arbo)+(1<<allDiff));
					solve();
					configParameters((1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff));
					solve();
					bst = true;
//					solve();
					configParameters((1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff));
					solve();
					configParameters((1<<arbo)+(1<<rg)+(1<<allDiff));
					solve();
//					System.exit(0);
											}
										}
		}
	}

	private static void loadNewOpt(String name) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader("/Users/jfages07/github/In4Ga/newATSP/optima.csv"));
			String line = buf.readLine();
			String[] lineNumbers;
			while(line!=null){
				lineNumbers = line.split(";");
				if(instanceName.contains(lineNumbers[0])){
					if(n==Integer.parseInt(lineNumbers[1])+1){
						optimum = Integer.parseInt(lineNumbers[2]);
						return;
					}
				}
				line = buf.readLine();
			}
			throw new UnsupportedOperationException();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

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

	private static void benchRandom() {
		bst = false;
		main_search = 0;
		policy = 2;
		int[] sizes = new int[]{50};
		int[] costs = new int[]{1000};
		for (int s:sizes) {
			for (int c:costs) {
				for (int k=0;k<10;k++) {
					generateInstance(s,c,System.currentTimeMillis());
					bst = false;
					configParameters((1<<pos)+(1<<allDiff));
					solve();
					configParameters((1<<rg)+(1<<pos)+(1<<allDiff));
					solve();
					bst = true;
					configParameters((1<<rg)+(1<<pos)+(1<<allDiff));
					solve();
//					System.exit(0);
				}
			}
		}
	}

	private static void generateInstance(int size, int maxCost, long seed) {
		instanceName = size+";"+maxCost+";"+seed;
		System.out.println("parsing instance " + instanceName + "...");
		n = size;
		Random rd = new Random(seed);
		double d;
		distanceMatrix = new int[n][n];
		for (int i=0; i<n; i++) {
//			for(int j=i+1;j<n;j++){
//				distanceMatrix[i][j] = rd.nextInt(maxCost+1);
//				d =  distanceMatrix[i][j]/10;
//				distanceMatrix[j][i] = distanceMatrix[i][j]+(int)(d*rd.nextDouble());
//			}
			for(int j=0;j<n;j++){
				distanceMatrix[i][j] = rd.nextInt(maxCost+1);
			}
		}
		noVal = Integer.MAX_VALUE / 2;
		int maxVal = 0;
		for (int i = 0; i < n; i++) {
			distanceMatrix[i][n - 1] = distanceMatrix[i][0];
			distanceMatrix[n - 1][i] = noVal;
			distanceMatrix[i][0] = noVal;
			for (int j = 0; j < n; j++) {
				if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
					maxVal = distanceMatrix[i][j];
				}
			}
		}
		optimum = -1;
		initialUB  = n*maxCost;
	}

	private static void loadInstance(String url) {
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			instanceName = line.split(":")[1].replaceAll(" ", "");
			System.out.println("parsing instance " + instanceName + "...");
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			n = Integer.parseInt(line.split(":")[1].replaceAll(" ", "")) + 1;
			distanceMatrix = new int[n][n];
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			String[] lineNumbers;
			for (int i = 0; i < n - 1; i++) {
				int nbSuccs = 0;
				while (nbSuccs < n - 1) {
					line = buf.readLine();
					line = line.replaceAll(" * ", " ");
					lineNumbers = line.split(" ");
					for (int j = 1; j < lineNumbers.length; j++) {
						if (nbSuccs == n - 1) {
							i++;
							if (i == n - 1) break;
							nbSuccs = 0;
						}
						distanceMatrix[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
						nbSuccs++;
					}
				}
			}
			//TODO remove for atsp->tsp convertion
			noVal = distanceMatrix[0][0];
			if (noVal == 0) noVal = Integer.MAX_VALUE / 2;
			int maxVal = 0;
			for (int i = 0; i < n; i++) {
				distanceMatrix[i][n - 1] = distanceMatrix[i][0];
				distanceMatrix[n - 1][i] = noVal;
				distanceMatrix[i][0] = noVal;
				for (int j = 0; j < n; j++) {
					if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
						maxVal = distanceMatrix[i][j];
					}
				}
			}
			line = buf.readLine();
			line = buf.readLine();
			initialUB = maxVal*n;
			optimum = Integer.parseInt(line.replaceAll(" ", ""));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void loadNewInstance(File file) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			instanceName = file.getName();
			System.out.println("parsing instance " + instanceName + "...");
			n = Integer.parseInt(line.replaceAll(" ", "")) + 1;
			distanceMatrix = new int[n][n];
			String[] lineNumbers;
			for (int i = 0; i < n - 1; i++) {
				int nbSuccs = 0;
				while (nbSuccs < n - 1) {
					line = buf.readLine();
					line = line.replaceAll(" * ", " ");
					lineNumbers = line.split(" ");
					for (int j = 1; j < lineNumbers.length; j++) {
						if (nbSuccs == n - 1) {
							i++;
							if (i == n - 1) break;
							nbSuccs = 0;
						}
						distanceMatrix[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
						nbSuccs++;
					}
				}
			}
			//TODO remove for atsp->tsp convertion
			noVal = distanceMatrix[0][0];
			if (noVal == 0) noVal = Integer.MAX_VALUE / 2;
//			noVal = -1;
			int maxVal = 0;
			for (int i = 0; i < n; i++) {
				distanceMatrix[i][n - 1] = distanceMatrix[i][0];
				distanceMatrix[n - 1][i] = noVal;
				distanceMatrix[i][0] = noVal;
				for (int j = 0; j < n; j++) {
					if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
						maxVal = distanceMatrix[i][j];
					}
				}
			}
//			line = buf.readLine();
//			line = buf.readLine();
			initialUB = maxVal*n;
			optimum = -1;
//			optimum = Integer.parseInt(line.replaceAll(" ", ""));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

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
}