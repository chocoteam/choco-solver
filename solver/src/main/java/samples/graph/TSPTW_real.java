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
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.real.PropEvalObj;

import solver.constraints.propagators.gary.tsp.real.PropHeldKarp;
import solver.constraints.propagators.gary.tsp.real.PropTimeInTour;
import solver.constraints.propagators.gary.tsp.real.PropTimeInTourGraphReactor;

import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.DoubleVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;

import java.io.*;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 * */
public class TSPTW_real extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 600000;
	private static String outFile = "real_tsptw.csv";
	static int seed = 0;
	// instance
	private String instanceName;
	private double[][] distanceMatrix;
	private int n;
	private double[] open, close;
	private double noVal, bestSol;
	// model
	private DirectedGraphVar graph;
	private DoubleVar[] time;
	private DoubleVar totalCost;
	private Boolean status;
	private GraphConstraint gc;
	private boolean arbo,antiArbo,rg,hk,bst;
	private int p;
	private IStateInt nR; IStateInt[] sccOf; INeighbors[] outArcs; IDirectedGraph G_R;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TSPTW_real(double[][] matrix, double[] open, double[] close, String inst, double nv, double maxValue) {
		solver = new Solver();
		distanceMatrix = matrix;
		n = matrix.length;
		noVal = nv;
		bestSol = (int) (n*Math.ceil(maxValue));
		instanceName = inst;
		this.open = open;
		this.close = close;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		// create model
		graph = new DirectedGraphVar(solver,n, GraphType.MATRIX,GraphType.LINKED_LIST);
		totalCost = VariableFactory.bounded("total cost ", 0, Math.ceil(bestSol), solver);
		time = new DoubleVar[n];
		for(int i=0;i<n;i++){
			time[i] = VariableFactory.bounded("time "+i,open[i],close[i],solver);
		}
		try{
			for(int i=0; i<n-1; i++){
				graph.getKernelGraph().activateNode(i);
				for(int j=0; j<n ;j++){
					if(distanceMatrix[i][j]!=noVal){
						graph.getEnvelopGraph().addArc(i,j);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		gc = GraphConstraintFactory.makeConstraint(graph, solver);
		// BASIC MODEL
		gc.addAdHocProp(new PropOneSuccBut(graph,n-1,gc,solver));
		gc.addAdHocProp(new PropOnePredBut(graph,0,gc,solver));
		gc.addAdHocProp(new PropPathNoCycle(graph,0,n-1,gc,solver));
		gc.addAdHocProp(new PropEvalObj(graph,totalCost,distanceMatrix,gc,solver));
		gc.addAdHocProp(new PropTimeInTour(time,graph,distanceMatrix,gc,solver));
		if(arbo){
			gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
		}
		if(antiArbo){
			gc.addAdHocProp(new PropAntiArborescence(graph,n-1,gc,solver,true));
		}
		if(rg){// reduced-graph based filtering
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			gc.addAdHocProp(RP);
			gc.addAdHocProp(new PropSCCDoorsRules(graph,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
		}else{
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
		}
		if(hk){// MST-based HK
			gc.addAdHocProp(PropHeldKarp.mstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver));
		}
		if(bst){// BST-based HK
			gc.addAdHocProp(PropHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs));
		}
		solver.post(gc);
	}

	private void configParameters(boolean ab, boolean aab, boolean rg, boolean hk, boolean bst) {
		arbo     = ab;
		antiArbo = aab;
		this.rg	 = rg;
		this.hk  = hk;
		this.bst = bst;
	}

	private void configParameters(int p,boolean bst) {
		configParameters(p%2==1,(p>>1)%2==1,(p>>2)%2==1,(p>>3)%2==1,bst);
		this.p = p;
		if(bst){
			this.p += 100;
		}
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
//		strategy = StrategyFactory.graphRandom(graph,seed);
//		if(nR==null){
			strategy = StrategyFactory.graphStrategy(graph,null,new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS);
//		}else{
//			strategy = StrategyFactory.graphStrategy(graph,null,new MinCostSCCBreak(graph), GraphStrategy.NodeArcPriority.ARCS);
//		}
		solver.set(strategy);
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
	}

	@Override
	public void solve() {
		status = solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,totalCost);
//		status = solver.findSolution();
		if(solver.getMeasures().getTimeCount()<TIMELIMIT && solver.getMeasures().getSolutionCount()==0){
			throw new UnsupportedOperationException("no solution?");
		}
	}

	@Override
	public void prettyOut() {
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName+";"+solver.getMeasures().getSolutionCount()+";"+solver.getMeasures().getFailCount()+
				";"+solver.getMeasures().getTimeCount()+";"+status+";"+bestSol+";"+bestCost+";"+p+";\n";
		writeTextInto(txt, outFile);
		for(int i=0;i<n;i++){
//			System.out.println(i+" -> "+graph.getEnvelopGraph().getSuccessorsOf(i)+" : "+time[i]);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		clearFile(outFile);
		writeTextInto("instance;sols;fails;time;status;opt;obj;param;\n", outFile);
//		bench();
		String instance = "/Users/jfages07/github/In4Ga/tsptw/rc_201.1.txt";
		testInstance(instance);
	}

	private static void testInstance(String url){
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			System.out.println("parsing instance "+url+"...");
			int n = Integer.parseInt(line)+1;
			double[][] dist = new double[n][n];
			String[] lineNumbers;
			for(int i=0;i<n-1;i++){
				line = buf.readLine();
				lineNumbers = line.split(" ");
				for(int j=0;j<n-1;j++){
					dist[i][j] = Double.parseDouble(lineNumbers[j]);
				}
				dist[i][n-1] = Double.parseDouble(lineNumbers[0]);
			}
			double noVal = dist[0][0];
			if(noVal == 0) noVal = Double.MAX_VALUE/2;
			double maxVal = 0;
			for(int i=0;i<n;i++){
				dist[i][n-1] = dist[i][0];
				dist[n-1][i] = noVal;
				dist[i][0]   = noVal;
				dist[i][i]   = noVal;
				for(int j=0;j<n;j++){
					if(dist[i][j]!=noVal && dist[i][j]>maxVal){
						maxVal = dist[i][j];
					}
				}
			}
			double[] open = new double[n];
			double[] close = new double[n];
			for(int i=0;i<n-1;i++){
				line = buf.readLine();
				line = line.replaceAll(" * ", " ");
				lineNumbers = line.split(" ");
				open[i] = Integer.parseInt(lineNumbers[0]);
				close[i] = Integer.parseInt(lineNumbers[1]);
			}
			open[n-1] = open[0];
			close[n-1]= close[0];
//			int[] params = new int[]{0,1,4,7};
//			for(int p:params){
			lineNumbers = url.split("/");
			String name = lineNumbers[lineNumbers.length-1];
			TSPTW_real tspRun = new TSPTW_real(dist,open,close,name,noVal,maxVal);
			tspRun.configParameters(1,false);
			tspRun.execute();
//			System.exit(0);
			for(int p=0;p<16;p++){
				if((p>>2)%2==1){
					tspRun = new TSPTW_real(dist,open,close,name,noVal,maxVal);
					tspRun.configParameters(p,true);
					tspRun.execute();
				}
				tspRun = new TSPTW_real(dist,open,close,name,noVal,maxVal);
				tspRun.configParameters(p,false);
				tspRun.execute();
			}
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void bench(){
		String dir = "/Users/jfages07/github/In4Ga/tsptw";
		File folder = new File(dir);
		String[] list = folder.list();
		for(String s:list){
			if(s.contains(".txt"))
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

//	private class MinArc extends ArcStrategy{
//
//		public MinArc(GraphVar graphVar) {
//			super(graphVar);
//		}
//
//		@Override
//		public int nextArc() {
//			int minArc = -1;
//			int minCost= -1;
//			INeighbors suc;
//			for(int i=0;i<n;i++){
//				suc = g.getEnvelopGraph().getSuccessorsOf(i);
//				for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
//					if(!g.getKernelGraph().arcExists(i,j)){
//						if(minCost == -1 || minCost>distanceMatrix[i][j]){
//							minCost = distanceMatrix[i][j];
//							minArc  = (i+1)*n+j;
//						}
//					}
//				}
//			}
//			return minArc;
//		}
//	}
//
	private class MinDomMinCost extends ArcStrategy{

		public MinDomMinCost(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			double minCost= -1;
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
	private class MinCostSCCBreak extends ArcStrategy{

		public MinCostSCCBreak(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			double minCost= -1;
			INeighbors suc;
			for(int i=0;i<nR.get();i++){
				suc = outArcs[i];
				if(outArcs[i].neighborhoodSize()>1){
					for(int a=suc.getFirstElement();a>=0;a=suc.getNextElement()){
						if(minCost==-1 || minCost>distanceMatrix[a/n-1][a%n]){
							minCost = distanceMatrix[a/n-1][a%n];
							minArc = a;
						}
					}
				}
			}
			if(minArc == -1 && !graph.instantiated()){
				throw new UnsupportedOperationException("What the f***?!");
			}
			if(minArc != -1 && graph.getKernelGraph().arcExists(minArc/n-1,minArc%n)){
				throw new UnsupportedOperationException("What the f***?!");
			}
			return minArc;
		}
	}
}

//	private void getGreedyFirstSolution() {
//		BitSet inTour = new BitSet(n);
//		greedySol = new int[n];
//		for(int i=0; i<n;i++){
//			greedySol[i] = -1;
//		}
//		greedySol[0] = n-1;
//		inTour.set(0);
//		inTour.set(n-1);
//		int nbNodesInTour = 2;
//		while(nbNodesInTour<n){
//			int nextNode = -1;
//			int minDist  = noVal*2;
//			int minGoBack;
//			for(int i=inTour.nextClearBit(0); i<n;i=inTour.nextClearBit(i+1)){
//				minGoBack = distanceMatrix[0][i]+ distanceMatrix[i][n-1];
//				for(int a=0;a>=0 && a<n-1; a=greedySol[a]){
//					minGoBack = Math.min(minGoBack,distanceMatrix[a][i]+distanceMatrix[i][greedySol[a]]);
//				}
//				if(minDist > minGoBack){
//					minDist = minGoBack;
//					nextNode = i;
//				}
//			}
//			if(nbNodesInTour==2){
//				greedySol[nextNode] = n-1;
//				greedySol[0]  =  nextNode;
//			}else{
//				minGoBack = noVal*2;
//				int bestFrom = 0;
//				int bestTo = 0;
//				for(int a=0;a>=0 && a<n-1; a=greedySol[a]){
//					if(minGoBack > distanceMatrix[a][nextNode]+distanceMatrix[nextNode][greedySol[a]]-distanceMatrix[a][greedySol[a]]){
//						minGoBack = distanceMatrix[a][nextNode]+distanceMatrix[nextNode][greedySol[a]]-distanceMatrix[a][greedySol[a]];
//						bestFrom = a;
//						bestTo = greedySol[a];
//					}
//				}
//				greedySol[nextNode] = bestTo;
//				greedySol[bestFrom] = nextNode;
//			}
//			inTour.set(nextNode);
//			nbNodesInTour++;
//		}
//	}