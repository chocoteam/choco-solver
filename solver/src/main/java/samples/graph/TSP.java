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
import choco.kernel.common.util.PoolManager;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.directed.*;
import solver.constraints.propagators.gary.tsp.HeldKarp;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropFastHeldKarp;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropHeldKarp;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTour;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTourGraphReactor;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.undirected.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.undirected.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.vrp.PropSumArcCosts;
import solver.exception.ContradictionException;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import java.io.*;
import java.util.BitSet;
import java.util.Random;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class TSP {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 60000;
	private static String outFile = "atsp.csv";
	private static int seed = 0;
	// instance
	private static String instanceName;
	private static int[][] distanceMatrix;
	private static int n, noVal, optimum, initialUB;
	private static Solver solver;
	// model
	private static DirectedGraphVar graph;
	private static IntVar totalCost;
	private static GraphConstraint gc;
	// RG data structure
	private static IStateInt nR;
	private static IStateInt[] sccOf;
	private static INeighbors[] outArcs;
	private static IDirectedGraph G_R;
	private static IStateInt[] sccFirst, sccNext;
	// Tasks data structure
	private static IntVar[] start, end, duration;
	// Branching data structure
	private static HeldKarp hk;

	//***********************************************************************************
	// MODEL CONFIGURATION
	//***********************************************************************************

	private static int arbo=0,rg=1,undirectedMate=2,pos=3,allDiff=4;//,time=5;
	private static int NB_PARAM = 5;
	private static BitSet config;
	private static boolean bst;

	private static void configParameters(int mask) {
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

	private static int search;
	private static String[] searchMode = new String[]{"top-down","bottom-up","dichotomic"};

	//***********************************************************************************
	// MODEL-SEARCH-RESOLUTION-OUTPUT
	//***********************************************************************************

	public static void buildModel() {
		// create model
		solver = new Solver();
		initialUB = optimum;
		System.out.println("initial UB : "+optimum);
		graph = new DirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		totalCost = VariableFactory.bounded("total cost ", 0, initialUB, solver);
		try {
			for (int i = 0; i < n - 1; i++) {
				graph.getKernelGraph().activateNode(i);
				for (int j = 0; j < n; j++) {
					if (distanceMatrix[i][j] != noVal) {
						graph.getEnvelopGraph().addArc(i, j);
					}
				}
				graph.getEnvelopGraph().removeArc(i, i);
			}
			graph.getKernelGraph().activateNode(n-1);
			graph.getEnvelopGraph().removeArc(0, n-1);
			graph.getEnvelopGraph().removeArc(n-1,0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		gc = GraphConstraintFactory.makeConstraint(graph, solver);
		// BASIC MODEL
		gc.addAdHocProp(new PropOneSuccBut(graph, n - 1, gc, solver));
		gc.addAdHocProp(new PropOnePredBut(graph, 0, gc, solver));
		gc.addAdHocProp(new PropPathNoCycle(graph, 0, n - 1, gc, solver));
		gc.addAdHocProp(new PropSumArcCosts(graph, totalCost, distanceMatrix, gc, solver));
		if(config.get(allDiff)){
			gc.addAdHocProp(new PropAllDiffGraphIncremental(graph,n-1,solver,gc));
		}
		// STRUCTURAL FILTERING
		if (config.get(arbo)) {
			gc.addAdHocProp(new PropArborescence(graph, 0, gc, solver, true));
			gc.addAdHocProp(new PropAntiArborescence(graph, n - 1, gc, solver, true));
		}
		//	gc.addAdHocProp(new PropSeparator(graph,gc,solver)); // USELESS IN PRACTICE
		if (config.get(rg)) {
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			sccFirst = RP.getSCCFirst();
			sccNext = RP.getSCCNext();
			gc.addAdHocProp(RP);
			PropSCCDoorsRules SCCP = new PropSCCDoorsRules(graph, gc, solver, nR, sccOf, outArcs, G_R, sccFirst, sccNext);
			gc.addAdHocProp(SCCP);
		}
		if(config.get(undirectedMate)){
			UndirectedGraphVar undi = new UndirectedGraphVar(solver,n-1,GraphType.LINKED_LIST,GraphType.LINKED_LIST);
			INeighbors nei;
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
			gc.addAdHocProp(new PropCycleNoSubtour(undi,gc,solver));
			gc.addAdHocProp(new PropAtLeastNNeighbors(undi,solver,gc,2));
			gc.addAdHocProp(new PropAtMostNNeighbors(undi,solver,gc,2));
			gc.addAdHocProp(new PropCyclePathChanneling(graph,undi,gc,solver));
		}
		if(config.get(pos)){
			IntVar[] pos = VariableFactory.boundedArray("pos",n,0,n-1,solver);
			try{
				pos[0].instantiateTo(0, Cause.Null);
				pos[n-1].instantiateTo(n - 1, Cause.Null);
			}catch(Exception e){
				e.printStackTrace();System.exit(0);
			}
			gc.addAdHocProp(new PropPosInTour(pos,graph,gc,solver));
			if(config.get(rg)){
				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver,nR,sccOf,outArcs,G_R));
			}else{
				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver));
			}
			solver.post(new AllDifferent(pos,solver, AllDifferent.Type.BC));
		}
//		if(config.get(time)){
//			start = new IntVar[n];
//			end = new IntVar[n];
//			duration = new IntVar[n];
//			for(int i=0;i<n;i++){
//				start[i] = VariableFactory.bounded("start "+i,0,totalCost.getUB(),solver);
//				end[i] = VariableFactory.bounded("end "+i,0,totalCost.getUB(),solver);
//				duration[i] = VariableFactory.bounded("duration "+i,0,totalCost.getUB(),solver);
//			}
//			try{
//				start[0].instantiateTo(0, Cause.Null);
//				duration[n-1].instantiateTo(0,Cause.Null);
//			}catch (Exception e){
//				e.printStackTrace();
//				System.exit(0);
//			}
//			gc.addAdHocProp(new PropTimeInTour(start,graph,distanceMatrix,gc,solver));
//			if(config.get(rg)){
//				gc.addAdHocProp(new PropTimeInTourGraphReactor(start,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
//			}else{
//				gc.addAdHocProp(new PropTimeInTourGraphReactor(start,graph,distanceMatrix,gc,solver));
//			}
//			gc.addAdHocProp(new PropTaskDefinition(start, end, duration, graph, distanceMatrix, gc, solver));
//			gc.addAdHocProp(new PropTaskSweep(start, end, duration, distanceMatrix, gc, solver));
//			gc.addAdHocProp(new PropTaskIntervals(start, end, duration, distanceMatrix, gc, solver));
//			gc.addAdHocProp(new PropTaskDefinition(start, end, duration, graph, distanceMatrix, gc, solver));
//			solver.post(ConstraintFactory.eq(end[n - 1], totalCost, solver));
//		}
		// COST BASED FILTERING
		if(config.get(rg) && bst){// BST-based HK
			System.out.println("BST");
			PropHeldKarp propHK_bst = PropFastHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs);
			hk = propHK_bst;
			gc.addAdHocProp(propHK_bst);
		}
		else{// MST-based HK
			System.out.println("MST");
			PropHeldKarp propHK_mst = PropFastHeldKarp.mstBasedRelaxation(graph, 0, n-1, totalCost, distanceMatrix, gc, solver);
			hk = propHK_mst;
			gc.addAdHocProp(propHK_mst);
		}

//		gc.addAdHocProp(new PropKhun(graph,totalCost,distanceMatrix,solver,gc));
		if(config.get(rg)){
			gc.addAdHocProp(new Prop_LP_GRB(graph,totalCost,distanceMatrix,solver,gc,outArcs,nR));
		}else{
			gc.addAdHocProp(new Prop_LP_GRB(graph,totalCost,distanceMatrix,solver,gc));
		}

		hk.waitFirstSolution(false);//search!=1 && initialUB!=optimum);
		solver.post(gc);
		//SOLVER CONFIG
//		solver.set(StrategyFactory.graphLexico(graph));
//		solver.set(new CompositeSearch(new DichotomicSearch(totalCost), StrategyFactory.graphLexico(graph)));
//		solver.getSearchLoop().restartAfterEachSolution(true);
//		solver.set(StrategyFactory.graphRandom(graph,System.currentTimeMillis()));
		switch (search){
			case 0: solver.set(new RCSearch(graph));
				break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),new RCSearch(graph)));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),new RCSearch(graph)));
				solver.getSearchLoop().restartAfterEachSolution(true);break;
			default: throw new UnsupportedOperationException();
		}
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		//SOLVE
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			throw new UnsupportedOperationException();
		}
		// OUTPUT
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
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
				+ (int)(solver.getMeasures().getTimeCount()) +  ";" + bestCost + ";"+searchMode[search]+";"+configst+"\n";
		writeTextInto(txt, outFile);
	}

	//***********************************************************************************
	// BENCHMARK
	//***********************************************************************************

	public static void main(String[] args) {
		outFile = "atsp_fast.csv";
		clearFile(outFile);
		writeTextInto("instance;sols;fails;nodes;time;obj;search;arbo;rg;undi;pos;bst;\n", outFile);
		config = new BitSet(NB_PARAM);
//		bench();
		bench();
//		String instance = "/Users/jfages07/github/In4Ga/atsp_instances/ft53.atsp";
//		testInstance(instance);
	}

	private static void bench() {
		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
		File folder = new File(dir);
		String[] list = folder.list();
		degHeur = true;
		search = 0;
		configParameters(0);
		for (String s : list) {
			if (s.contains("p43.atsp")  && !s.contains("filter")){
//				if(s.contains("p43.atsp"))System.exit(0);
				loadInstance(dir + "/" + s);
				if(n>0 && n<1070){// || s.contains("p43.atsp")){
//					bst = false;
//					configParameters(0);
//					buildModel();
//					System.exit(0);
//					configParameters((1<<arbo));
//					buildModel();
//					configParameters((1<<pos));
//					buildModel();
//					configParameters((1<<allDiff));
//					buildModel();
//					configParameters((1<<undirectedMate));
//					buildModel();
//					bst = true;
					configParameters((1<<rg));
					buildModel();
					System.exit(0);
//					configParameters((1<<rg)+(1<<arbo));
//					buildModel();
//					configParameters((1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff));
//					buildModel();
				}
			}
		}
	}

	private static void benchRandom() {
		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
		bst = false;
		search = 2;
		int[] sizes = new int[]{200};
		int[] costs = new int[]{10,20};
		for (int s:sizes) {
			for (int c:costs) {
				for (int k=0;k<50;k++) {
					generateInstance(s,c,System.currentTimeMillis());
					bst=false;
					configParameters(0);
					buildModel();
					bst=true;
					configParameters(1<<rg);
					buildModel();
				}
			}
		}
	}

	private static void benchTSPChanged() {
		String dir = "/Users/jfages07/github/In4Ga/ALL_tsp";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix;
		search = 2;
		for (String s : list) {
			if (s.contains(".tsp") && (!s.contains("gz"))){
				matrix = TSPsymmetric.parseInstance(dir + "/" + s);
				if(matrix!=null && matrix.length<170){
					instanceName = s;
					change(TSPsymmetric.transformMatrix(matrix));

					bst = false;
					configParameters(0);
					buildModel();
					configParameters((1<<rg));
					buildModel();
					configParameters((1<<arbo));
					buildModel();
					configParameters((1<<pos));
					buildModel();
					configParameters((1<<allDiff));
					buildModel();
					configParameters((1<<undirectedMate));
					buildModel();
					bst = true;
					buildModel();
					configParameters((1<<rg)+(1<<arbo)+(1<<pos)+(1<<allDiff));
					buildModel();
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
	}

	private static void change(int[][] ints) {
		System.out.println("parsing instance " + instanceName + "...");
		n = ints.length;
		Random rd = new Random(seed);
		double d;
		distanceMatrix = ints;
		for (int i=0; i<n; i++) {
			for(int j=i+1;j<n;j++){
				d =  distanceMatrix[i][j]/10;
				distanceMatrix[j][i] = distanceMatrix[i][j]+(int)(d*rd.nextDouble());
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
		initialUB = optimum = n*maxVal;
	}

	private static void generateInstance(int size, int maxCost, long seed) {
		instanceName = size+";"+maxCost+";"+seed;
		System.out.println("parsing instance " + instanceName + "...");
		n = size;
		Random rd = new Random(seed);
		double d;
		distanceMatrix = new int[n][n];
		for (int i=0; i<n; i++) {
			for(int j=i+1;j<n;j++){
				distanceMatrix[i][j] = rd.nextInt(maxCost+1);
				d =  distanceMatrix[i][j]/10;
				distanceMatrix[j][i] = distanceMatrix[i][j]+(int)(d*rd.nextDouble());
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
		initialUB = optimum = n*maxCost;
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

	//***********************************************************************************
	// SEARCH HEURISTICS
	//***********************************************************************************

	static int lb;
	static int ub;

	private static class DichotomicSearch extends AbstractStrategy<IntVar> {
		IntVar obj;
		long nbSols;
		protected DichotomicSearch(IntVar obj) {
			super(new IntVar[]{obj});
			this.obj = obj;
			lb = -1;
		}
		@Override
		public void init() {}
		@Override
		public Decision getDecision() {
			if(lb==-1){
				lb = obj.getLB();
			}
			if(obj.getLB()==obj.getUB()){
				return null;
			}
			if(nbSols == solver.getMeasures().getSolutionCount()){
				return null;
			}else{
				nbSols = solver.getMeasures().getSolutionCount();
				ub = obj.getUB();
				int target = (lb+ub)/2;
				System.out.println(lb+" : "+ub+" -> "+target);
				FastDecision dec = new FastDecision(new PoolManager<FastDecision>());
				dec.set(obj,target, objCut);
				return dec;
			}
		}
	}

	private static Assignment<IntVar> objCut = new Assignment<IntVar>() {
		@Override
		public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
			var.updateUpperBound(value, cause);
		}
		@Override
		public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
			lb = value+1;
			var.updateLowerBound(value + 1, cause);
		}
		@Override
		public String toString() {
			return " <= ";
		}
	};

	private static class BottomUp extends AbstractStrategy<IntVar> {
		IntVar obj;
		int val;
		protected BottomUp(IntVar obj) {
			super(new IntVar[]{obj});
			this.obj = obj;
			val = -1;
		}
		@Override
		public void init() {}
		@Override
		public Decision getDecision() {
			if(obj.getLB()==obj.getUB()){
				return null;
			}
			if(val==-1){
				val = obj.getLB();
			}
			int target = val;
			val++;
			System.out.println(obj.getLB()+" : "+obj.getUB()+" -> "+target);
			FastDecision dec = new FastDecision(new PoolManager<FastDecision>());
			dec.set(obj,target, objCut);
			return dec;
		}
	}

	static boolean degHeur = true;
	private static class RCSearch extends AbstractStrategy<GraphVar> {
		GraphVar g;
		protected RCSearch(GraphVar g) {
			super(new GraphVar[]{g});
			this.g = g;
		}
		@Override
		public void init() {}
		@Override
		public Decision getDecision() {
			int dec;
			if(degHeur){
				dec = minDomMaxRepCost();
			}else{
				dec = maxRepCost();
			}
			if(dec==-1){
				if(!g.instantiated()){
					throw new UnsupportedOperationException();
				}
				return null;
			}
			return new GraphDecision(g,dec, Assignment.graph_enforcer);
		}

		public int minDomMaxRepCost() {
			int n = g.getEnvelopOrder();
			INeighbors suc;
			int size = 2*n + 1;
			int sizi;
			double repCost=0,repCostij;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>1){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if((hk.getMST().arcExists(i,j)) &&!g.getKernelGraph().arcExists(i,j)){ //hk.getMST().arcExists(i,j) &&
//							if((!config.get(rg)) || sccOf[i].get()!=sccOf[j].get()){
							repCostij = hk.getRepCost(i,j);
							sizi = suc.neighborhoodSize();
							sizi += g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
							if (sizi == size) {
								if(repCost<repCostij){
									repCost = repCostij;
									to = j;
									from = i;
								}
							}
							if (sizi < size) {
								size = sizi;
								to = j;
								from = i;
								repCost = repCostij;
							}
//							}
						}
					}
				}
			}
			return (from+1)*n+to;
		}

		public int p43() {
			int n = g.getEnvelopOrder();
			INeighbors suc;
			int size = 2*n + 1;
			int sizi;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>1){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if(sccOf[i].get()!=sccOf[j].get() && !g.getKernelGraph().arcExists(i,j)){ //hk.getMST().arcExists(i,j) &&
							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
							if (sizi < size) {
								size = sizi;
								to = j;
								from = i;
							}
						}
					}
				}
			}
			return (from+1)*n+to;
		}

		public int maxRepCost() {
			int n = g.getEnvelopOrder();
			INeighbors suc;
			double maxRepCost = -1;
			double repCost;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = hk.getMST().getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if(!g.getKernelGraph().arcExists(i,j)){
						repCost = hk.getRepCost(i,j);
						if(repCost<0){
							System.out.println(i+" : "+j);
							System.out.println(g.getEnvelopGraph());
							throw new UnsupportedOperationException();
						}
						if (repCost > maxRepCost) {
							maxRepCost = repCost;
							to = j;
							from = i;
						}
					}
				}
			}
			return (from+1)*n+to;
		}

		int[] nbP,nbS;
		public int best() {
			IGraph mst = hk.getMST();
			if(nbP==null){
				nbP = new int[n];
				nbS = new int[n];
			}
			for(int i=0;i<n;i++){
				nbP[i] = mst.getPredecessorsOf(i).neighborhoodSize();
				nbS[i] = mst.getSuccessorsOf(i).neighborhoodSize();
			}
			INeighbors suc;
			double maxRepCost = -1;
			double repCost;
			int maxSize = 0;
			int size;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = hk.getMST().getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if(!g.getKernelGraph().arcExists(i,j)){
//						if((!config.get(rg)) || sccOf[i].get()!=sccOf[j].get()){
						size = nbS[i]+nbP[j];
						repCost = hk.getRepCost(i,j);
						if(repCost<0){throw new UnsupportedOperationException();}
						if(repCost > maxRepCost || (repCost == maxRepCost && size>maxSize)) {
							maxRepCost = repCost;
							maxSize = size;
							from = i;
							to = j;
						}
//					}
					}
				}
			}
			return (from+1)*n+to;
		}
	}

	private static class CompositeSearch extends AbstractStrategy {

		AbstractStrategy s1,s2;
		protected CompositeSearch(AbstractStrategy s1, AbstractStrategy s2) {
			super(ArrayUtils.append(s1.vars, s2.vars));
			this.s1 = s1;
			this.s2 = s2;
		}

		@Override
		public void init() {}

		@Override
		public Decision getDecision() {
			Decision d = s1.getDecision();
			if(d==null){
				d = s2.getDecision();
			}
			return d;
		}
	}

}