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
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraph2;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.directed.*;
import solver.constraints.propagators.gary.tsp.disjunctive.PropTaskDefinition;
import solver.constraints.propagators.gary.tsp.disjunctive.PropTaskIntervals;
import solver.constraints.propagators.gary.tsp.disjunctive.PropTaskSweep;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropHeldKarp;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
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
import solver.variables.graph.directedGraph.IDirectedGraph;

import java.io.*;
import java.util.BitSet;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 * */
public class TSPTW extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 10000;
	private static String outFile;
	static long seed = 0;
	// instance
	private String instanceName;
	private int[][] distanceMatrix;
	private int n;
	private int[] open, close;
	private int noVal, bestSol;
	// model
	private DirectedGraphVar graph;
	private IntVar[] time;
	private IntVar totalCost;
	private Boolean status;
	private GraphConstraint gc;
	private boolean arbo,antiArbo,rg,hk,disj, pos, allDiffAC;
	static int nbParam = 7;
	static int maxParam = 1<<nbParam;
	private int p;
	private IStateInt nR; IStateInt[] sccOf; INeighbors[] outArcs; IDirectedGraph G_R;
	private IntVar[] end,duration;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TSPTW(int[][] matrix, int[] open, int[] close, String inst, int nv, int maxValue) {
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
//		buildFull();
		buildpreviousModel();
//		buildRGModel();
	}
	public void buildRGModel() {
		// create model
		graph = new DirectedGraphVar(solver,n, GraphType.ENVELOPE_SWAP_ARRAY,GraphType.LINKED_LIST);
		totalCost = VariableFactory.bounded("total cost ", 0, (int)Math.ceil(bestSol), solver);
		time = new IntVar[n];
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
		if(rg){// reduced-graph based filtering
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			gc.addAdHocProp(RP);
			gc.addAdHocProp(new PropSCCDoorsRules(graph,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
			gc.addAdHocProp(new PropAntiArborescence(graph,n-1,gc,solver,true));
			gc.addAdHocProp(PropHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs));
		}
		else{
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
			gc.addAdHocProp(PropHeldKarp.mstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver));
		}
		try{
			time[0].instantiateTo(0, Cause.Null);
		}catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		solver.post(gc);
	}
	public void buildpreviousModel() {
		// create model
		graph = new DirectedGraphVar(solver,n, GraphType.ENVELOPE_SWAP_ARRAY,GraphType.LINKED_LIST);
		totalCost = VariableFactory.bounded("total cost ", 0, (int)Math.ceil(bestSol), solver);
		time = new IntVar[n];
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
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
		}
		else{
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
		}
		if(hk){// MST-based HK
			if(rg){// BST-based HK
				gc.addAdHocProp(PropHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs));
			}else{
				gc.addAdHocProp(PropHeldKarp.mstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver));
			}
		}


		if(disj){
			end = new IntVar[n];
			duration = new IntVar[n];
			for(int i=0;i<n;i++){
				end[i] = VariableFactory.bounded("end "+i,0,totalCost.getUB(),solver);
				duration[i] = VariableFactory.bounded("duration "+i,0,totalCost.getUB(),solver);
			}
			try{
				time[0].instantiateTo(0, Cause.Null);
				duration[n-1].instantiateTo(0, Cause.Null);
			}catch (Exception e){
				e.printStackTrace();
				System.exit(0);
			}
			gc.addAdHocProp(new PropTaskDefinition(time, end, duration, graph, distanceMatrix, gc, solver));
			gc.addAdHocProp(new PropTaskDefinition(time, end, duration, graph, distanceMatrix, gc, solver));
			gc.addAdHocProp(new PropTaskSweep(time, end, duration, distanceMatrix, gc, solver));
			gc.addAdHocProp(new PropTaskIntervals(time, end, duration, distanceMatrix, gc, solver));
		}
		if(pos){
			IntVar[] positions = new IntVar[n];
			for(int i=0;i<n;i++){
				positions[i] = VariableFactory.bounded("positions "+i,0,n-1,solver);
			}
			try{
				positions[0].instantiateTo(0, Cause.Null);
				positions[n-1].instantiateTo(n-1, Cause.Null);
			}catch (Exception e){
				e.printStackTrace();
				System.exit(0);
			}
			gc.addAdHocProp(new PropPosInTour(positions, graph, gc, solver));
			if(rg){
				gc.addAdHocProp(new PropPosInTourGraphReactor(positions, graph, gc, solver,nR,sccOf,outArcs,G_R));
			}else{
				gc.addAdHocProp(new PropPosInTourGraphReactor(positions, graph, gc, solver));
			}
			solver.post(new AllDifferent(positions,solver,AllDifferent.Type.BC));
		}
		if(allDiffAC){
			gc.addAdHocProp(new PropAllDiffGraph2(graph,solver,gc));
		}
		solver.post(gc);
	}
	public void buildFull() {
		// create model
		graph = new DirectedGraphVar(solver,n, GraphType.ENVELOPE_SWAP_ARRAY,GraphType.LINKED_LIST);
		totalCost = VariableFactory.bounded("total cost ", 0, (int)Math.ceil(bestSol), solver);
		time = new IntVar[n];
		for(int i=0;i<n;i++){
			time[i] = VariableFactory.bounded("time "+i,open[i],close[i],solver);
		}
		end = new IntVar[n];
		duration = new IntVar[n];
		for(int i=0;i<n;i++){
			end[i] = VariableFactory.bounded("end "+i,0,totalCost.getUB(),solver);
			duration[i] = VariableFactory.bounded("duration "+i,0,totalCost.getUB(),solver);
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
			time[0].instantiateTo(0, Cause.Null);
			duration[n-1].instantiateTo(0, Cause.Null);
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

		gc.addAdHocProp(new PropTaskDefinition(time, end, duration, graph, distanceMatrix, gc, solver));
		gc.addAdHocProp(new PropTaskDefinition(time, end, duration, graph, distanceMatrix, gc, solver));
		gc.addAdHocProp(new PropTaskSweep(time, end, duration, distanceMatrix, gc, solver));

		if(rg){
			gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
			gc.addAdHocProp(new PropAntiArborescence(graph,n-1,gc,solver,true));
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			gc.addAdHocProp(RP);
			gc.addAdHocProp(new PropSCCDoorsRules(graph,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver,nR,sccOf,outArcs,G_R));
			gc.addAdHocProp(PropHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs));
		}
		else{
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
			gc.addAdHocProp(new PropTimeInTourGraphReactor(time,graph,distanceMatrix,gc,solver));
			gc.addAdHocProp(PropHeldKarp.mstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver));
		}
		gc.addAdHocProp(new PropTaskIntervals(time, end, duration, distanceMatrix, gc, solver));

		solver.post(gc);
	}

	private void configParameters(boolean ab, boolean aab, boolean rg, boolean hk, boolean disj, boolean pos, boolean adAC) {
		arbo     = ab;
		antiArbo = aab;
		this.rg	 = rg;
		this.hk  = hk;
		this.disj = disj;
		this.pos = pos;
		this.allDiffAC = adAC;
	}

	private void configParameters(int p) {
		configParameters(p%2==1,(p>>1)%2==1,(p>>2)%2==1,(p>>3)%2==1,(p>>4)%2==1,(p>>5)%2==1,(p>>6)%2==1);
		this.p = p;
	}
	private void configSingleParameters(int p) {
		configParameters(p==1,p==2,p==3,p==4,p==5,p==6,p==7);
		this.p = p;
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
		strategy = StrategyFactory.graphRandom(graph,seed);
//		if(nR==null){
//			strategy = StrategyFactory.graphLexico(graph);
		strategy = StrategyFactory.graphStrategy(graph, null, new BuildPath(graph), GraphStrategy.NodeArcPriority.ARCS);

//			strategy = StrategyFactory.graphStrategy(graph,null,new MinDomMinVal(graph), GraphStrategy.NodeArcPriority.ARCS);
//		strategy = StrategyFactory.graphStrategy(graph,null,new MinDomMinCost(graph), GraphStrategy.NodeArcPriority.ARCS);
//		}else{
//			strategy = StrategyFactory.graphStrategy(graph,null,new MinCostSCCBreak(graph), GraphStrategy.NodeArcPriority.ARCS);
//		}
		solver.set(strategy);
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		solver.getSearchLoop().restartAfterEachSolution(true);
		SearchMonitorFactory.log(solver, true, false);
	}

	@Override
	public void solve() {
		status = solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,totalCost);
//		status = solver.findSolution();
		if(solver.getMeasures().getTimeCount()<TIMELIMIT && solver.getMeasures().getSolutionCount()==0){
			throw new UnsupportedOperationException("no solution?");
		}
//		if(solver.getMeasures().getSolutionCount()>0 && !checked()){
//			System.out.println(p+" : param");
//			System.out.println(graph.getEnvelopGraph());
//			for(int i=0;i<n;i++){
//				System.out.println(i+"->"+graph.getEnvelopGraph().getSuccessorsOf(i).getFirstElement()+" : "+time[i]);
//			}
//			throw new UnsupportedOperationException("not a solution");
//		}
	}

	private boolean checked() {
		if(!graph.instantiated()){
			return false;
		}
		int node = 0;
		int next = 0;
		int time = open[0];
		BitSet nodes = new BitSet(n);
		do{
			node = next;
//			System.out.println("visit : "+node+" ["+open[node]/10000+","+close[node]/10000+"] at time "+time/10000);
			if(nodes.get(node)){
				return false;
			}
			nodes.set(node);
			next = graph.getEnvelopGraph().getSuccessorsOf(node).getFirstElement();
			if(next == -1 || graph.getEnvelopGraph().getSuccessorsOf(node).getNextElement()!=-1){
				return false;
			}
//			System.out.println(time+"+"+distanceMatrix[node][next]+" = "+(time+distanceMatrix[node][next]));
			time = Math.max(open[next],time+distanceMatrix[node][next]);
			if(time>close[next]){
				return false;
			}
		}while(next != n-1);
		nodes.set(next);
		if(graph.getEnvelopGraph().getSuccessorsOf(next).neighborhoodSize()>0){
			return false;
		}
		if(nodes.nextClearBit(0)<n){
			return false;
		}
		return true;
	}

	@Override
	public void prettyOut() {
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
//		int bestCost = 0;
		String txt = instanceName+";"+solver.getMeasures().getSolutionCount()+";"+solver.getMeasures().getFailCount()+
				";"+solver.getMeasures().getTimeCount()+";"+status+";"+bestSol+";"+bestCost+";"+p+";";
		txt+=arbo+";"+antiArbo+";"+rg+";"+hk+";"+disj+";"+pos+";"+allDiffAC+";\n";
		writeTextInto(txt, outFile);
//		System.out.println(graph.getEnvelopGraph());
//		for(int i=0;i<n;i++){
//			System.out.println(i+" -> "+graph.getEnvelopGraph().getSuccessorsOf(i)+" : "+time[i]);
//		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		outFile = "tsptw_60sec_notasks.csv";
		clearFile(outFile);
		writeTextInto("instance;sols;fails;time;status;opt;obj;param;arbo;antiarbo;rg;hk;disj;pos;allDiff\n", outFile);
		bench();
//		String instance = "/Users/jfages07/github/In4Ga/tsptw/rc_201.1.txt";
//		testInstance(instance);
	}

	private static void testInstance(String url){
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			System.out.println("/n parsing instance "+url+".../n");
			int n = Integer.parseInt(line)+1;
			int[][] dist = new int[n][n];
			String[] lineNumbers;
			for(int i=0;i<n-1;i++){
				line = buf.readLine();
				lineNumbers = line.split(" ");
				for(int j=0;j<n-1;j++){
					dist[i][j] = (int) (Double.parseDouble(lineNumbers[j])*10000);
				}
				dist[i][n-1] = dist[i][0];
			}
			int noVal = dist[0][0];
			if(noVal == 0) noVal = Integer.MAX_VALUE;
			int maxVal = 0;
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
			int[] open = new int[n];
			int[] close = new int[n];
			for(int i=0;i<n-1;i++){
				line = buf.readLine();
				line = line.replaceAll(" * ", " ");
				lineNumbers = line.split(" ");
				open[i] = Integer.parseInt(lineNumbers[0])*10000;
				close[i] = Integer.parseInt(lineNumbers[1])*10000;
			}
			open[n-1] = open[0];
			close[n-1]= close[0];

			lineNumbers = url.split("/");
			String name = lineNumbers[lineNumbers.length-1];

			TSPTW tspRun;
//			int[] params = new int[]{0,4};
//			for(int p:params){
//			for(int p=0;p<maxParam;p++){
//			for(int p=0;p<=nbParam;p++){
				tspRun = new TSPTW(dist,open,close,name,noVal,maxVal);
//				tspRun.configParameters(p);
				tspRun.configSingleParameters(0);
				tspRun.execute();
//			}
//			}
//			if(true){
//				return;
//			}
//			for(int p=0;p<16;p++){
//				if((p>>2)%2==1){
//					tspRun = new TSPTW(dist,open,close,name,noVal,maxVal);
//					tspRun.configParameters(p);
//					tspRun.execute();
//				}
//				tspRun = new TSPTW(dist,open,close,name,noVal,maxVal);
//				tspRun.configParameters(p);
//				tspRun.execute();
//			}
//			System.exit(0);
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
//				if(s.contains("208"))
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

	int[] dec = {498,622,596,565,416,449,461,319,513,368,53,76,105,131,157,184,208,229,260,287,392,339,-1};
	int idx = -1;
	private class MinDomMinCost extends ArcStrategy{

		public MinDomMinCost(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			if(true){
				idx++;
				return dec[idx];
			}
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
				if(!g.instantiated()){
					throw new UnsupportedOperationException();
				}
				System.out.println("DECISION : "+-1);
				return -1;
			}
			int minArc = -1;
			int minCost= -1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			if(g.getKernelGraph().getSuccessorsOf(from).neighborhoodSize()>0){
				throw new UnsupportedOperationException("error in branching");
			}
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
			System.out.println("DECISION : "+minArc);
			return minArc;
		}
	}
	private class MinDomMinVal extends ArcStrategy{

		public MinDomMinVal(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
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
				if(!g.instantiated()){
					throw new UnsupportedOperationException();
				}
				System.out.println("DECISION : "+-1);
				return -1;
			}
			return (from+1)*n+g.getEnvelopGraph().getSuccessorsOf(from).getFirstElement();
		}
	}
	private class MinCostSCCBreak extends ArcStrategy{

		public MinCostSCCBreak(GraphVar graphVar) {
			super(graphVar);
		}

		@Override
		public int nextArc() {
			int minArc = -1;
			int minCost= -1;
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
	private class BuildPath extends ArcStrategy{

		IStateInt currentNode;

		public BuildPath(GraphVar graphVar) {
			super(graphVar);
			currentNode = solver.getEnvironment().makeInt(0);
		}

		@Override
		public int nextArc() {
			int node = currentNode.get();
			INeighbors succ = graph.getKernelGraph().getSuccessorsOf(node);
			while(succ.neighborhoodSize()==1){
				if(graph.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()!=1){
					throw new UnsupportedOperationException();
				}
				node = succ.getFirstElement();
				succ = graph.getKernelGraph().getSuccessorsOf(node);
			}
			currentNode.set(node);
			succ = graph.getEnvelopGraph().getSuccessorsOf(node);
			int next = succ.getFirstElement();
			if(next==-1){
				return next;
			}
			next = selectNext(succ,node);
			if(next == -1){
				throw new UnsupportedOperationException();
			}
			return (node+1)*n+next;
		}

		private int selectNext(INeighbors succ, int from) {
//			return minSuccs(succ);

//			return succ.getFirstElement();
			return earliestTimeLB(succ,from); // this one seems the best

//			return earliestTimeUB(succ,from);
		}

		private int minSuccs(INeighbors succ) {
			int next = -1;
			int minSize = n+1;
			for(int i=succ.getFirstElement();i>=0;i=succ.getNextElement()){
				if(graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<minSize){
					minSize = graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
					next = i;
				}
			}
			return next;
		}
		private int earliestTimeLB(INeighbors succ, int from) {
			if(succ.neighborhoodSize()==0){
				throw new UnsupportedOperationException();
			}
			int next = -1;
			int minTime = time[n-1].getUB()+1;
			int tmp;
			for(int i=succ.getFirstElement();i>=0;i=succ.getNextElement()){
				tmp = Math.max(time[i].getLB(), time[from].getLB() + distanceMatrix[from][i]);
				if(tmp<minTime){
					minTime = tmp;
					next = i;
				}
			}
			return next;
		}
		private int earliestTimeUB(INeighbors succ, int from) {
			if(succ.neighborhoodSize()==0){
				throw new UnsupportedOperationException();
			}
			int next = -1;
			int minTime = time[n-1].getUB()+1;
			int tmp;
			for(int i=succ.getFirstElement();i>=0;i=succ.getNextElement()){
				tmp = time[i].getUB();
				if(tmp<minTime){
					minTime = tmp;
					next = i;
				}
			}
			return next;
		}
	}
}