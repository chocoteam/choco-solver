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
import samples.AbstractProblem;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.gary.tsp.PropReducedGraphHamPath;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.relaxationHeldKarp.PropHeldKarp;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.IncrPriorityP;
import solver.propagation.engines.comparators.predicate.Predicates;
import solver.propagation.engines.group.Group;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.io.*;
import java.util.BitSet;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 * */
public class TSP extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 1800000;
	private static String outFile = "/Users/jfages07/Documents/code/results/results_atsp";
	static int seed = 0;
	// instance
	private String instanceName;
	private int[][] distanceMatrix;
	private int n, maxValue, noVal, bestSol;
	// model
	private DirectedGraphVar graph;
	private IntVar totalCost;
	private int greedyUB;
	private int[] greedySol;
	private Boolean status;
	static boolean randomHeur = false;
	static String LastHope="";

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TSP(int[][] matrix, int max, String inst, int nv, int bestS) {
		solver = new Solver();
		distanceMatrix = matrix;
		n = matrix.length;
		maxValue = max;
		noVal = nv;
		instanceName = inst;
		bestSol = bestS;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		// first solution
		getGreedyFirstSolution();
		greedyUB = bestSol;//getGreedyBound();
		System.out.println("\nBOUND : "+greedyUB+"\n");
		//permutNodes();// relabeling of nodes to optimize boundAllDiff
		// create model
		IntVar[] intVars = new IntVar[n];
		graph = new DirectedGraphVar(solver,n, GraphType.ENVELOPE_SWAP_ARRAY,GraphType.LINKED_LIST);
		totalCost = VariableFactory.enumerated("total cost ", 0, greedyUB, solver);
		try{
			intVars[n-1] = VariableFactory.enumerated("vlast", n, n, solver);
			for(int i=0; i<n-1; i++){
				intVars[i] = VariableFactory.bounded("v" + i, 1, n - 1, solver);
				graph.getKernelGraph().activateNode(i);
				for(int j=0; j<n ;j++){
					if(distanceMatrix[i][j]!=noVal){
						graph.getEnvelopGraph().addArc(i,j);
					}else{
						intVars[i].removeValue(j,Cause.Null,false);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(graph, solver);
		// CHECKED
		gc.addAdHocProp(new PropOneSuccBut(graph,n-1,gc,solver));
		gc.addAdHocProp(new PropOnePredBut(graph,0,gc,solver));
		gc.addAdHocProp(new PropPathNoCycle(graph,gc,solver));
		gc.addAdHocProp(new PropDegreePatterns(graph,gc,solver));
		gc.addAdHocProp(new PropEvalObj(graph,totalCost,distanceMatrix,gc,solver));
		gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
		gc.addAdHocProp(new PropIntVarChanneling(intVars,graph,gc,solver));

		// ID anti arborescence?

//		PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
//		gc.addAdHocProp(RP);
//		IStateInt nR = RP.getNSCC();
//		IStateInt[] sccOf = RP.getSCCOF();
//		INeighbors[] outArcs = RP.getOutArcs();

		// BST-based HK
//		PropHeldKarp propHK = new PropHeldKarp(graph, 0,n-1, totalCost, distanceMatrix,gc,solver, nR, sccOf, outArcs);
//		PropHeldKarp propHK = PropHeldKarp.bstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver, nR, sccOf, outArcs);
		// MST-based HK
		PropHeldKarp propHK = PropHeldKarp.mstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver);
		gc.addAdHocProp(propHK);

//		PropBST BST = new PropBST(graph, totalCost, distanceMatrix, gc, solver);
//		gc.addAdHocProp(BST);
//		BST.setRGStructure(nR, sccOf, outArcs);

		Constraint[] cstrs = new Constraint[]{gc, new AllDifferent(intVars,solver,AllDifferent.Type.AC)};
		solver.post(cstrs);
	}

	private int getGreedyBound() {
		int ub = 0;
		for(int a=0;a>=0 && a<n-1; a=greedySol[a]){
			ub += distanceMatrix[a][greedySol[a]];
		}
		return ub;
	}

	private void permutNodes() {
//		permutFirstSol();
		permutMinCost();
	}

	private void permutFirstSol() {
		int[][] costs = new int[n][n];
		int[] nodeAtPos = new int[n];
		int i=0;
		for(int a=0;a>=0 && a<n-1; a=greedySol[a]){
			nodeAtPos[i] = a;
			i++;
		}
		nodeAtPos[n-1] = n-1;
		for (i=0;i<n;i++){
			for(int j=0;j<n;j++){
				costs[i][j] = distanceMatrix[nodeAtPos[i]][nodeAtPos[j]];
			}
		}
		distanceMatrix = costs;
	}

	private void permutMinCost() {
		int[][] costs = new int[n][n];
		int[] nodeAtPos = new int[n];
		BitSet done = new BitSet(n);
		done.set(n-1);
		done.set(0);
		int next,nextCost;
		for(int i=0;i<n-2;i++){
			next = done.nextClearBit(0);
			nextCost = distanceMatrix[i][next];
			for(int j=1;j<n-1;j++){
				if(distanceMatrix[i][j]<nextCost && !done.get(j)){
					next = j;
					nextCost = distanceMatrix[i][next];
				}
			}
			nodeAtPos[i+1] = next;
			done.set(next);
		}
		nodeAtPos[0] = 0;
		nodeAtPos[n-1] = n-1;
		for (int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				costs[i][j] = distanceMatrix[nodeAtPos[i]][nodeAtPos[j]];
			}
		}
		distanceMatrix = costs;
	}

	private void getGreedyFirstSolution() {
		BitSet inTour = new BitSet(n);
		greedySol = new int[n];
		for(int i=0; i<n;i++){
			greedySol[i] = -1;
		}
		greedySol[0] = n-1;
		inTour.set(0);
		inTour.set(n-1);
		int nbNodesInTour = 2;
		while(nbNodesInTour<n){
			int nextNode = -1;
			int minDist  = noVal*2;
			int minGoBack;
			for(int i=inTour.nextClearBit(0); i<n;i=inTour.nextClearBit(i+1)){
				minGoBack = distanceMatrix[0][i]+ distanceMatrix[i][n-1];
				for(int a=0;a>=0 && a<n-1; a=greedySol[a]){
					minGoBack = Math.min(minGoBack,distanceMatrix[a][i]+distanceMatrix[i][greedySol[a]]);
				}
				if(minDist > minGoBack){
					minDist = minGoBack;
					nextNode = i;
				}
			}
			if(nbNodesInTour==2){
				greedySol[nextNode] = n-1;
				greedySol[0]  =  nextNode;
			}else{
				minGoBack = noVal*2;
				int bestFrom = 0;
				int bestTo = 0;
				for(int a=0;a>=0 && a<n-1; a=greedySol[a]){
					if(minGoBack > distanceMatrix[a][nextNode]+distanceMatrix[nextNode][greedySol[a]]-distanceMatrix[a][greedySol[a]]){
						minGoBack = distanceMatrix[a][nextNode]+distanceMatrix[nextNode][greedySol[a]]-distanceMatrix[a][greedySol[a]];
						bestFrom = a;
						bestTo = greedySol[a];
					}
				}
				greedySol[nextNode] = bestTo;
				greedySol[bestFrom] = nextNode;
			}
			inTour.set(nextNode);
			nbNodesInTour++;
		}
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
		if(randomHeur){
			strategy = StrategyFactory.graphRandom(graph,seed);
		}else{
			strategy = StrategyFactory.graphStrategy(graph,null,new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS);
		}
//		AbstractStrategy strategy = StrategyFactory.graphLexico(graph);
		solver.set(strategy);
		solver.getEngine().addGroup(Group.buildGroup(Predicates.all(), IncrPriorityP.get(), Policy.FIXPOINT));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
	}

	@Override
	public void solve() {
		status = solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,totalCost);
//		status = solver.findSolution();
	}

	@Override
	public void prettyOut() {
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName+";"+solver.getMeasures().getFailCount()+";"+solver.getMeasures().getTimeCount()+";"+status+";"+greedyUB+";"+bestCost+";"+bestSol+";\n";
		writeTextInto(txt, outFile);
		writeTextInto(txt,"/Users/jfages07/Documents/GOD_MIGHT_EXISTS");
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
//		clearFile(outFile);
//		clearFile("/Users/jfages07/Documents/GOD_EXISTS");
//		writeTextInto("instance;fails;time;status;greedyUB;obj;best;\n", outFile);
//		randomHeur = false;
//		outFile = "resultsATSP_"+randomHeur+".csv";
//		bench();
//		randomHeur = !randomHeur;
//		outFile = "resultsATSP_"+randomHeur+".csv";
//		bench();
//		writeTextInto(LastHope,"/Users/jfages07/Documents/GOD_EXISTS");
//		randomHeur = true;
		String instance = "/Users/jfages07/github/In4Ga/atsp_instances/ftv70.atsp";
		testInstance(instance);
	}

	private static void testInstance(String url){
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String name = line.split(":")[1].replaceAll(" ", "");
			System.out.println("parsing instance "+name+"...");
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			int n = Integer.parseInt(line.split(":")[1].replaceAll(" ", ""))+1;
			int[][] dist = new int[n][n];
			line = buf.readLine();line = buf.readLine();line = buf.readLine();
			String[] lineNumbers;
			for(int i=0;i<n-1;i++){
				int nbSuccs = 0;
				while(nbSuccs<n-1){
					line = buf.readLine();
					line = line.replaceAll(" * ", " ");
					lineNumbers = line.split(" ");
					for(int j=1;j<lineNumbers.length;j++){
						if(nbSuccs==n-1){
							i++;
							if(i==n-1)break;
							nbSuccs = 0;
						}
						dist[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
						nbSuccs ++;
					}
				}
			}
			int noVal = dist[0][0];
			if(noVal == 0) noVal = Integer.MAX_VALUE/2;
			int maxVal = 0;
			for(int i=0;i<n;i++){
				dist[i][n-1] = dist[i][0];
				dist[n-1][i] = noVal;
				dist[i][0]   = noVal;
				for(int j=0;j<n;j++){
					if(dist[i][j]!=noVal && dist[i][j]>maxVal){
						maxVal = dist[i][j];
					}
				}
			}
			line = buf.readLine();
			line = buf.readLine();
			int best = Integer.parseInt(line.replaceAll(" ",""));
			TSP tspRun = new TSP(dist,maxVal,name,noVal,best);
			tspRun.execute();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void bench(){
		String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
		File folder = new File(dir);
		String[] list = folder.list();
		for(String s:list){
			if(s.contains(".atsp"))
				testInstance(dir+"/"+s);
		}
	}

	//***********************************************************************************
	// RECORDING RESULTS
	//***********************************************************************************

	private static void writeTextInto(String text, String file) {
		try{
			FileWriter out  = new FileWriter(file,true);
			out.write(text);
			LastHope += text;
			out.flush();
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private static void clearFile(String file) {
		try{
			FileWriter out  = new FileWriter(file,false);
			out.write("");
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//***********************************************************************************
	// HEURISTICS
	//***********************************************************************************

	private class MinArc extends ArcStrategy{

		public MinArc(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			int minCost= -1;
			INeighbors suc;
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
					if(!g.getKernelGraph().arcExists(i,j)){
						if(minCost == -1 || minCost>distanceMatrix[i][j]){
							minCost = distanceMatrix[i][j];
							minArc  = (i+1)*n+j;
						}
					}
				}
			}
			return minArc;
		}
	}

	private class MinDomMinCost extends ArcStrategy{

		public MinDomMinCost(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			int minCost= -1;
			INeighbors suc;
			int from = -1;
			int size = n+1;
			for(int i=0;i<n-1;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()<size && suc.neighborhoodSize()>1){
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if(from == -1){
				return -1;
			}
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
				if(minCost == -1 || minCost>distanceMatrix[from][j]){
					minCost = distanceMatrix[from][j];
					minArc  = (from+1)*n+j;
				}
			}
			if(minArc==-1){
				throw new UnsupportedOperationException("error in branching");
			}
			if(g.getKernelGraph().arcExists(from,minArc%n)){
				throw new UnsupportedOperationException("error in branching");
			}
			return minArc;
		}
	}
}