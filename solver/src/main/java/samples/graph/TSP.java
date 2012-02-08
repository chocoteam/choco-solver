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
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import samples.AbstractProblem;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.relaxationHeldKarp.PropHeldKarp;
import solver.constraints.propagators.gary.undirected.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.undirected.PropAtMostNNeighbors;
import solver.exception.ContradictionException;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;
import java.util.ArrayList;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class TSP extends AbstractProblem {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 10000;
	private static String outFile = "atsp.csv";
	static int seed = 0;
	// instance
	private String instanceName;
	private int[][] distanceMatrix;
	private int n, noVal, bestSol;
	// model
	private DirectedGraphVar graph;
	private IntVar totalCost;
	private Boolean status;
	private GraphConstraint gc;
	private boolean arbo, antiArbo, rg, hk, time;
	private IStateInt nR;
	IStateInt[] sccOf;
	INeighbors[] outArcs;
	IDirectedGraph G_R;
	private IStateInt[] sccFirst, sccNext;

	IntVar[] start, end, duration;

	ArrayList<GraphPropagator> propForBranch;

	static boolean undirectedMate;
	private UndirectedGraphVar undi;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TSP(int[][] matrix, String inst, int nv, int bestS) {
		solver = new Solver();
		distanceMatrix = matrix;
		n = matrix.length;
		noVal = nv;
		instanceName = inst;
		bestSol = Integer.MAX_VALUE/4;
//		bestSol = bestS;//(int) ((double)(bestS)*1.05);//(int)((double)(bestS)*1.1);//Integer.MAX_VALUE/4;
		propForBranch = new ArrayList<GraphPropagator>();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		// create model
		graph = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST);
		totalCost = VariableFactory.bounded("total cost ", 0, bestSol, solver);
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
		gc.addAdHocProp(new PropEvalObj(graph, totalCost, distanceMatrix, gc, solver));

		if (arbo) {
			gc.addAdHocProp(new PropArborescence(graph, 0, gc, solver, true));
		}
		if (antiArbo) {
			gc.addAdHocProp(new PropAntiArborescence(graph, n - 1, gc, solver, true));
		}
//		gc.addAdHocProp(new PropSeparator(graph,gc,solver));
		if (rg) {
			// RG
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
			propForBranch.add(RP);
			propForBranch.add(SCCP);
		}
		if(undirectedMate){
			undi = new UndirectedGraphVar(solver,n-1,GraphType.MATRIX,GraphType.LINKED_LIST);
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
//		if(hk){
//			IntVar[] pos = VariableFactory.boundedArray("pos",n,0,n-1,solver);
//			try{
//				pos[0].instantiateTo(0, Cause.Null, false);
//				pos[n-1].instantiateTo(n - 1, Cause.Null, false);
//			}catch(Exception e){
//				e.printStackTrace();System.exit(0);
//			}
//			gc.addAdHocProp(new PropPosInTour(pos,graph,gc,solver));
//			if(rg){
//				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver,nR,sccOf,outArcs,G_R));
//			}else{
//				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver));
//			}
//			solver.post(new AllDifferent(pos,solver, AllDifferent.Type.BC));
//		}
		if(rg){// BST-based HK
			System.out.println("BST");
			PropHeldKarp propHK_bst = PropHeldKarp.bstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver, nR, sccOf, outArcs);
			gc.addAdHocProp(propHK_bst);
			propForBranch.add(propHK_bst);
		}
		else{// MST-based HK
//			System.out.println("MST");
			PropHeldKarp propHK_mst = PropHeldKarp.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver);
			gc.addAdHocProp(propHK_mst);
			propForBranch.add(propHK_mst);
		}
//		if(time){
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
//			if(rg){
//				gc.addAdHocProp(new PropTimeInTourGraphReactor(start,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
//			}else{
//				gc.addAdHocProp(new PropTimeInTourGraphReactor(start,graph,distanceMatrix,gc,solver));
//			}
//			gc.addAdHocProp(new PropTaskDefinition(start, end, duration, graph, distanceMatrix, gc, solver));
//			gc.addAdHocProp(new PropTaskSweep(start, end, duration, distanceMatrix, gc, solver));
//			gc.addAdHocProp(new PropTaskIntervals(start, end, duration, distanceMatrix, gc, solver));
//			gc.addAdHocProp(new PropTaskDefinition(start, end, duration, graph, distanceMatrix, gc, solver));
//			solver.post(ConstraintFactory.eq(end[n-1],totalCost,solver));
//		}
		solver.post(gc);
	}

	private void configParameters(boolean ab, boolean aab, boolean rg, boolean hk) {
		arbo = ab;
		antiArbo = aab;
		this.rg = rg;
		this.hk = hk;
	}

	private void configParameters(int p, boolean ti) {
		configParameters(p % 2 == 1, (p >> 1) % 2 == 1, (p >> 2) % 2 == 1, (p >> 3) % 2 == 1);
		time = ti;
	}

	static int policy;
	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
//		strategy = StrategyFactory.graphStrategy(graph, null, new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS);
//		strategy = StrategyFactory.graphStrategy(graph, null, new FilteredMinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS);

//		strategy = new StrategiesSequencer(solver.getEnvironment(),
//				StrategyFactory.graphStrategy(graph, null, new FilteredMinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS),
//				StrategyFactory.graphLexico(graph));


		if(undi==null){
			strategy = new StrategiesSequencer(solver.getEnvironment(),
					StrategyFactory.graphStrategy(graph, null, new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS)
//					,StrategyFactory.graphLexico(graph)
			);
//			strategy = new StrategiesSequencer(solver.getEnvironment(),
//					StrategyFactory.graphStrategy(graph, null, new FilteredMinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS));
		}else{
//			strategy = new StrategiesSequencer(solver.getEnvironment(),
//					StrategyFactory.graphStrategy(graph, null, new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS),
//					StrategyFactory.graphLexico(graph),StrategyFactory.graphLexico(undi));

//			strategy = new StrategiesSequencer(solver.getEnvironment(),
//					StrategyFactory.graphStrategy(undi, null, new FilteredMinNeiMinCost(undi), GraphStrategy.NodeArcPriority.ARCS),
//					StrategyFactory.graphStrategy(graph, null, new FilteredMinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS),
//					StrategyFactory.graphLexico(graph),StrategyFactory.graphLexico(undi));
//			strategy = new StrategiesSequencer(solver.getEnvironment(),
//					StrategyFactory.graphStrategy(undi, null, new MinNeighMinCost(undi), GraphStrategy.NodeArcPriority.ARCS),
//					StrategyFactory.graphStrategy(graph, null, new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS)
//			);
			strategy = new StrategiesSequencer(solver.getEnvironment(),
					StrategyFactory.graphStrategy(undi, null, new MinNeighMinSumCost(undi), GraphStrategy.NodeArcPriority.ARCS),
					StrategyFactory.graphStrategy(graph, null, new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS),
					StrategyFactory.graphLexico(graph),StrategyFactory.graphLexico(undi)
			);
		}

		solver.set(strategy);

		if(policy>1)
			solver.getSearchLoop().plugSearchMonitor(new MyMon());

		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
//		solver.getSearchLoop().getLimitsBox().setSolutionLimit(2);
		SearchMonitorFactory.log(solver, true, false);
//		if(policy>0)
		solver.getSearchLoop().restartAfterEachSolution(true);
	}

	@Override
	public void solve() {
		status = solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
//		status = solver.findSolution();
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void prettyOut() {
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ solver.getMeasures().getTimeCount() + ";" + policy + ";" + bestCost + ";"+rg+";\n";
		writeTextInto(txt, outFile);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		outFile = "atsp_fast.csv";
		clearFile(outFile);
		writeTextInto("instance;sols;fails;time;policy;obj;rg;\n", outFile);
//		bench();
		String instance = "/Users/jfages07/github/In4Ga/atsp_instances/ftv38.atsp";
		testInstance(instance);
	}

	private static void testInstance(String url) {
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String name = line.split(":")[1].replaceAll(" ", "");
			System.out.println("parsing instance " + name + "...");
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			int n = Integer.parseInt(line.split(":")[1].replaceAll(" ", "")) + 1;
			int[][] dist = new int[n][n];
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
						dist[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
						nbSuccs++;
					}
				}
			}
			int noVal = dist[0][0];
			if (noVal == 0) noVal = Integer.MAX_VALUE / 2;
			int maxVal = 0;
			for (int i = 0; i < n; i++) {
				dist[i][n - 1] = dist[i][0];
				dist[n - 1][i] = noVal;
				dist[i][0] = noVal;
				for (int j = 0; j < n; j++) {
					if (dist[i][j] != noVal && dist[i][j] > maxVal) {
						maxVal = dist[i][j];
					}
				}
			}
			line = buf.readLine();
			line = buf.readLine();
			int best = Integer.parseInt(line.replaceAll(" ", ""));
			int[] params = new int[]{0};
			for(int p:params){
//			for(int p=4;p<16;p++){
//				for(int po = 0;po<3;po++){
//					int p=4;
//					int po = 2;
//					policy = po;
//				undirectedMate = true;
				undirectedMate = true;
				TSP tspRun = new TSP(dist,name,noVal,best);
				tspRun.configParameters(p,false);
				tspRun.execute();
//				undirectedMate = false;
//				tspRun = new TSP(dist,name,noVal,best);
//				tspRun.configParameters(p,false);
//				tspRun.execute();
//				System.exit(0);
//				}
//				System.exit(0);
			}
//			System.exit(0);
//            TSP tspRun = new TSP(dist, name, noVal, best);
//            tspRun.configParameters();
//            tspRun.execute();
//			System.exit(0);
//			tspRun = new TSP(dist,name,noVal,best);
//			tspRun.configParameters(0,true);
//			tspRun.execute();
//			tspRun = new TSP(dist,name,noVal,best);
//			tspRun.configParameters(4,false);
//			tspRun.execute();
//			tspRun = new TSP(dist,name,noVal,best);
//			tspRun.configParameters(4,true);
//			tspRun.execute();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void bench() {
		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
		File folder = new File(dir);
		String[] list = folder.list();
		for (String s : list) {
			if (s.contains(".atsp") && !s.contains("170"))
//				if(s.contains("rbg"))
				testInstance(dir + "/" + s);
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

	public void resetStrategy() {
		solver.set(new StrategiesSequencer(solver.getEnvironment(),new DichotomicSearch(totalCost),
				StrategyFactory.graphStrategy(graph, null, new FilteredMinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS),
				StrategyFactory.graphLexico(graph)));
	}

	//***********************************************************************************
	// HEURISTICS
	//***********************************************************************************

	static int lb;
	static int ub;
	private class MyMon extends VoidSearchMonitor implements ISearchMonitor{
		public MyMon(){
			ub = totalCost.getUB();
			lb = totalCost.getLB();

		}
		public void afterRestart() {
			try {
				totalCost.updateUpperBound(ub, Cause.Null);
				totalCost.updateLowerBound(lb, Cause.Null);
				solver.getEngine().propagate();
			} catch (ContradictionException e) {
				solver.getSearchLoop().interrupt();
			}
			resetStrategy();
		}
		public void onSolution() {
			ub = totalCost.getValue()-1;
		}
		public void afterInitialPropagation() {
			lb = totalCost.getLB();
			ub = totalCost.getValue();
		}
	}

	private class DichotomicSearch extends AbstractStrategy<IntVar> {
		IntVar obj;
		private boolean done;
		protected DichotomicSearch(IntVar obj) {
			super(new IntVar[]{obj});
			this.obj = obj;
			done = false;
		}
		@Override
		public void init() {}
		@Override
		public Decision getDecision() {
			if(done || solver.getMeasures().getSolutionCount()==0 || obj.getLB()==obj.getUB()){
				return null;
			}
			done = true;
			int target = (obj.getLB()+obj.getUB())/2;
			System.out.println(obj.getLB()+" : "+obj.getUB()+" -> "+target);
			FastDecision dec = new FastDecision(new PoolManager<FastDecision>());
			dec.set(obj,target, objCut);
			return dec;
		}
	}

	public static Assignment<IntVar> objCut = new Assignment<IntVar>() {

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

	private class MinArc extends ArcStrategy {

		public MinArc(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			int minCost = -1;
			INeighbors suc;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if (!g.getKernelGraph().arcExists(i, j)) {
						if (minCost == -1 || minCost > distanceMatrix[i][j]) {
							minCost = distanceMatrix[i][j];
							minArc = (i + 1) * n + j;
						}
					}
				}
			}
			return minArc;
		}
	}

	private class FilteredMinDomMinCost extends ArcStrategy {

		int[][] branchingQuality;

		public FilteredMinDomMinCost(GraphVar graphVar) {
			super(graphVar);
			branchingQuality = new int[n][n];
		}

		@Override
		public int nextArc() {
			INeighbors suc;
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
					branchingQuality[i][j] = 0;
				}
			}
			for(GraphPropagator gp:propForBranch){
				gp.provideBranchingOpinion(branchingQuality);
			}
			int maxVal = Integer.MIN_VALUE/2;
			TIntArrayList list = new TIntArrayList();
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>1){
					for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
						if(maxVal<branchingQuality[i][j]){
							maxVal = branchingQuality[i][j];
							list.clear();
							list.add((i+1)*n+j);
						}
						if(maxVal==branchingQuality[i][j]){
							list.add((i+1)*n+j);
						}
					}
				}
			}
			int from,arc;
			int size = n + 1;
			int bestFrom = -1;
			for(int i=0;i<list.size();i++){
				arc = list.get(i);
				from = arc/n-1;
				suc = g.getEnvelopGraph().getSuccessorsOf(from);
				if(suc.neighborhoodSize()<size){
					bestFrom = from;
					size = suc.neighborhoodSize();
				}
			}
			if (bestFrom == -1) {
				return -1;
			}
			int minArc = -1;
			int minCost = -1;
			int to;
			for(int i=0;i<list.size();i++){
				arc = list.get(i);
				from = arc/n-1;
				to   = arc%n;
				if(from == bestFrom){
					if (minCost == -1 || minCost > distanceMatrix[from][to]) {
						minCost = distanceMatrix[from][to];
						minArc = (from + 1) * n + to;
					}
				}
			}
			if (minArc == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(bestFrom, minArc % n)) {
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}

	static boolean branchOnArcs = false;
	private class MinDomMinCost extends ArcStrategy {

		public MinDomMinCost(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			if(branchOnArcs){
				if(!g.instantiated()){
//					System.out.println(g.getEnvelopGraph());
//					System.out.println(g.getKernelGraph());
//					throw new UnsupportedOperationException();
//					System.out.println("pas ouf");
				}else{
					return -1;
				}
			}
			branchOnArcs = true;
			int minArc = -1;
			int minCost = -1;
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n - 1; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 1) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				return -1;
			}
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if (minCost == -1 || minCost > distanceMatrix[from][j]) {
					minCost = distanceMatrix[from][j];
					minArc = (from + 1) * n + j;
				}
			}
			if (minArc == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(from, minArc % n)) {
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}

	private class FilteredMinNeiMinCost extends ArcStrategy {

		int[][] branchingQuality;

		public FilteredMinNeiMinCost(GraphVar graphVar) {
			super(graphVar);
			branchingQuality = new int[n+1][n+1];
		}

		@Override
		public int nextArc() {
			INeighbors suc;
			for(int i=0;i<n;i++){
				branchingQuality[i][n] = 0;
				branchingQuality[n][i] = 0;
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
					branchingQuality[i][j] = 0;
				}
			}
			for(GraphPropagator gp:propForBranch){
				gp.provideBranchingOpinion(branchingQuality);
			}
			int maxVal = Integer.MIN_VALUE/2;
			TIntArrayList list = new TIntArrayList();
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>2){
					for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
						if(maxVal<branchingQuality[i][j]+branchingQuality[j][i]){
							maxVal = branchingQuality[i][j]+branchingQuality[j][i];
							list.clear();
							list.add((i+1)*n+j);
						}
						if(maxVal==branchingQuality[i][j]+branchingQuality[j][i]){
							list.add((i+1)*n+j);
						}
					}
				}
			}
			int from,arc;
			int size = n + 1;
			TIntArrayList list2 = new TIntArrayList();
			for(int i=0;i<list.size();i++){
				arc = list.get(i);
				from = arc/n-1;
				suc = g.getEnvelopGraph().getSuccessorsOf(from);
				if(suc.neighborhoodSize()<size){
					size = suc.neighborhoodSize();
					list2.clear();
					list2.add(arc);
				}
				if(suc.neighborhoodSize()==size){
					list2.add(arc);
				}
			}
			int minArc = -1;
			int minCost = -1;
			int to;
			for(int i=0;i<list2.size();i++){
				arc = list2.get(i);
				from = arc/n-1;
				to   = arc%n;
				if(!g.getKernelGraph().arcExists(from,to)){
					if(graph.getEnvelopGraph().arcExists(from,to)){
						if (minCost == -1 || minCost > distanceMatrix[from][to]) {
							minCost = distanceMatrix[from][to];
							minArc = (from + 1) * n + to;
						}
					}
					if(graph.getEnvelopGraph().arcExists(to,from)){
						if (minCost == -1 || minCost > distanceMatrix[to][from]) {
							minCost = distanceMatrix[to][from];
							minArc = (from + 1) * n + to;
						}
					}
				}
			}
			if (minArc == -1) {
				return -1;//				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}

	private class MinNeighMinCost extends ArcStrategy {

		public MinNeighMinCost(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			int minCost = -1;
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n - 1; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 2) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				return -1;
			}
			int val;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					val = totalCost.getUB();
					if(j==0){
						if(graph.getEnvelopGraph().arcExists(from,n)){
							val = Math.min(val,distanceMatrix[from][n]);
						}
					}else{
						if(graph.getEnvelopGraph().arcExists(from,j)){
							val = Math.min(val,distanceMatrix[from][j]);
						}
					}
					if(from==0){
						if(graph.getEnvelopGraph().arcExists(j,n)){
							val = Math.min(val,distanceMatrix[j][n]);
						}
					}else{
						if(graph.getEnvelopGraph().arcExists(j,from)){
							val = Math.min(val,distanceMatrix[j][from]);
						}
					}
					if(val<minCost){
						minCost = val;
						minArc = (from + 1) * n + j;
					}
				}
			}
			if (minArc == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(from, minArc % n)) {
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}

	private class MinNeighMinSumCost extends ArcStrategy {

		public MinNeighMinSumCost(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			branchOnArcs = false;
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n - 1; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 2) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				return -1;
			}
			int val;
			int minArc = -1;
			int minCost = totalCost.getUB()+1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					val = 0;
					boolean done = false;
					if(from==0){
						if(graph.getEnvelopGraph().arcExists(j,n)){
							val += distanceMatrix[j][n];
							done = true;
						}
						if(graph.getEnvelopGraph().arcExists(0,j)){
							val += distanceMatrix[0][j];
							done = true;
						}
					}else{
						if(j==0){
							if(graph.getEnvelopGraph().arcExists(0,from)){
								val += distanceMatrix[0][from];
								done = true;
							}
							if(graph.getEnvelopGraph().arcExists(from,n)){
								val += distanceMatrix[from][n];
								done = true;
							}
						}else{
							if(graph.getEnvelopGraph().arcExists(from,j)){
								val += distanceMatrix[from][j];
								done = true;
							}
							if(graph.getEnvelopGraph().arcExists(j,from)){
								val += distanceMatrix[j][from];
								done = true;
							}
						}
					}
					if(!done){
//						return -1;
						throw new UnsupportedOperationException();
					}
					if(val<minCost){
						minCost = val;
						minArc = (from + 1) * n + j;
					}
				}
			}
			if (minArc == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(from, minArc % n)) {
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}

	private class MinNeigh extends ArcStrategy {

		public MinNeigh(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n - 1; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 2) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				return -1;
			}
			int minCost = n+1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					if (minCost > graph.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize()) {
						minCost = graph.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
						minArc = (from + 1) * n + j;
					}
				}
			}
			if (minArc == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(from, minArc % n)) {
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}

	private class MinSucc extends ArcStrategy {

		public MinSucc(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n - 1; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 1) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				return -1;
			}
			int minCost = n+1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					if (minCost > graph.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize()) {
						minCost = graph.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
						minArc = (from + 1) * n + j;
					}
				}
			}
			if (minArc == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(from, minArc % n)) {
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}
}