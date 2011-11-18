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
import samples.AbstractProblem;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.PropReducedGraphHamPath;
import solver.constraints.propagators.gary.tsp.*;
import solver.exception.ContradictionException;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.IncrArityP;
import solver.propagation.engines.comparators.predicate.Predicates;
import solver.propagation.engines.group.Group;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.BitSet;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 * */
public class TSP extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 10000;
	private static String outFile = "/Users/jfages07/Documents/code/results/results_atsp_"+(TIMELIMIT/1000)+".csv";
	static int seed = 0;
	// instance
	private String instanceName;
	private int[][] distanceMatrix;
	private int n, maxValue, noVal, bestSol;
	// model
	private DirectedGraphVar graph;
	private IntVar totalCost;
	private int greedyUB;
	private Boolean status;

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
		totalCost = VariableFactory.enumerated("total cost ", 0,maxValue*n, solver);
		graph = new DirectedGraphVar(solver,n, GraphType.ENVELOPE_SWAP_HASH,GraphType.LINKED_LIST);
		try{
			for(int i=0; i<n; i++){
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
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(graph, solver);
		
		gc.addAdHocProp(new PropOneSuccBut((DirectedGraphVar) graph,n-1,gc,solver));
		gc.addAdHocProp(new PropOnePredBut((DirectedGraphVar) graph,0,gc,solver));
		gc.addAdHocProp(new PropPathNoCycle((DirectedGraphVar) graph,gc,solver));
		gc.addAdHocProp(new PropDegreePatterns((DirectedGraphVar) graph,gc,solver));
		gc.addAdHocProp(new PropEvalObj((DirectedGraphVar) graph,totalCost,distanceMatrix,gc,solver));
//		gc.addAdHocProp(new PropRGPath((DirectedGraphVar) graph,0,gc,solver));
//		gc.addAdHocProp(new PropRGPathincr((DirectedGraphVar) graph,0,gc,solver));
		gc.addAdHocProp(new PropArborescence((DirectedGraphVar) graph,0,gc,solver));
		gc.addAdHocProp(new PropReducedGraphHamPath((DirectedGraphVar) graph,gc,solver));
//		gc.addAdHocProp(new PropWSTCCincr((DirectedGraphVar) graph,totalCost,distanceMatrix, gc, solver));

		// find a first solution with a greedy algorithm
		greedyUB = getGreedyBound();
//		try {
//			totalCost.updateUpperBound(greedyUB, Cause.Null, false);
//		} catch (ContradictionException e) {
//			e.printStackTrace();
//			System.exit(0);
//		}
		System.out.println("\nBOUND : "+greedyUB+"\n");
		Constraint[] cstrs = new Constraint[]{gc};
		solver.post(cstrs);
	}

	private int getGreedyBound() {
		int ub = 0;
		BitSet inTour = new BitSet(n);
		int[] tourNext = new int[n];
		for(int i=0; i<n;i++){
			tourNext[i] = -1;
		}
		tourNext[0] = n-1;
		inTour.set(0);
		inTour.set(n-1);
		int nbNodesInTour = 2;
		while(nbNodesInTour<n){
			int nextNode = -1;
			int minDist  = noVal*2;
			int minGoBack;
			for(int i=inTour.nextClearBit(0); i<n;i=inTour.nextClearBit(i+1)){
				minGoBack = distanceMatrix[0][i]+ distanceMatrix[i][n-1];
				for(int a=0;a>=0 && a<n-1; a=tourNext[a]){
					minGoBack = Math.min(minGoBack,distanceMatrix[a][i]+distanceMatrix[i][tourNext[a]]);
				}
				if(minDist > minGoBack){
					minDist = minGoBack;
					nextNode = i;
				}
			}
			if(nbNodesInTour==2){
				tourNext[nextNode] = n-1;
				tourNext[0]  =  nextNode;
			}else{
				minGoBack = noVal*2;
				int bestFrom = 0;
				int bestTo = 0;
				for(int a=0;a>=0 && a<n-1; a=tourNext[a]){
					if(minGoBack > distanceMatrix[a][nextNode]+distanceMatrix[nextNode][tourNext[a]]-distanceMatrix[a][tourNext[a]]){
						minGoBack = distanceMatrix[a][nextNode]+distanceMatrix[nextNode][tourNext[a]]-distanceMatrix[a][tourNext[a]];
						bestFrom = a;
						bestTo = tourNext[a];
					}
				}
				tourNext[nextNode] = bestTo;
				tourNext[bestFrom] = nextNode;
			}
			inTour.set(nextNode);
			nbNodesInTour++;
		}
		for(int a=0;a>=0 && a<n-1; a=tourNext[a]){
			ub += distanceMatrix[a][tourNext[a]];
		}
		return ub;
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.graphRandom(graph,seed);
		solver.set(strategy);
		solver.getEngine().addGroup(Group.buildGroup(Predicates.all(), IncrArityP.get(), Policy.FIXPOINT));
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
//		System.out.println(graph.getKernelGraph());
		writeTextInto(instanceName+";"+solver.getMeasures().getFailCount()+";"+solver.getMeasures().getTimeCount()+";"
				+status+";"+greedyUB+";"+totalCost.getValue()+";"+bestSol+"\n", outFile);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		bench();
//		String instance = "/Users/jfages07/Documents/code/atsp instances/br17.atsp";
//		testInstance(instance);
	}

	private static void testInstance(String url){
		File file = new File(url);
		try {
			System.out.println(file);
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
//						if(i<n-1){
						dist[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
//						}else{
//							nbSuccs = n;
//						}
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
		clearFile(outFile);
		writeTextInto("instance;fails;time;status;greedyUB;obj;best;\n", outFile);
		String dir = "/Users/jfages07/Documents/code/atsp instances";
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
			FileWriter out  = new FileWriter(outFile,true);
			out.write(text);
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void clearFile(String file) {
		try{
			FileWriter out  = new FileWriter(outFile,false);
			out.write("");
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
