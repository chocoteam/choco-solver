package samples.parallel.schema;
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

import samples.parallel.Parser;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.File;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class TSPmaster extends AbstractParallelMaster<TSPslave>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// general
	private static String outFile;
	// instance
	private int optimum;
	private int[][] distMatrix;
	private int n;
	// LNS
	private int[] bestSolution;
	private int bestCost;
	private int SIZE = 10;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public TSPmaster(int[][] distMatrix, int opt){
		super();
		this.optimum = opt;
		this.distMatrix = distMatrix;
		this.n = distMatrix.length;
		this.SIZE = n/2;
	}

	public static void main(String[] args) {
		outFile = "tsp_lns.csv";
		Parser.clearFile(outFile);
		Parser.writeTextInto("instance;sols;fails;nodes;time;obj;search;\n", outFile);
		String dir = "/Users/jfages07/github/In4Ga/benchRousseau";
		String optFile = "/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv";
//		String dir = "/Users/jfages07/github/In4Ga/mediumTSP/OneMinute";
		File folder = new File(dir);
		String[] list = folder.list();
		long time = System.currentTimeMillis();
		for (String s : list) {
			if (s.contains(".tsp") && (!s.contains("a280")) && (!s.contains("gz")) && (!s.contains("lin"))){
				int[][] distMatrix = Parser.parseInstance(dir + "/" + s);
				if(distMatrix!=null){
					int n = distMatrix.length;
					if(n>=10 && n<40){
						System.gc();
						System.out.println("\n SOLVING INSTANCE "+s+"\n");
						int optimum = Parser.getOpt(s.split("\\.")[0],optFile);
						TSPmaster master = new TSPmaster(distMatrix,optimum);
						System.out.println("optimum : " + optimum);
						master.checkMatrix();
						master.initLNS();
						master.computeFirstSolution();
						long timeInst = System.currentTimeMillis();
						System.out.println("start LNS...");
						master.LNS();
						System.out.println("end LNS...");
						System.out.println("time : " + (System.currentTimeMillis() - timeInst) + " ms");
					}
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
		System.out.println("time : " + (System.currentTimeMillis() - time) + " ms");
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	private void checkMatrix() {
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(distMatrix[i][j] != distMatrix[j][i]){
					System.out.println(i+" : "+j);
					System.out.println(distMatrix[i][j]+" != "+distMatrix[j][i]);
					throw new UnsupportedOperationException();
				}
			}
		}
	}

	private void computeFirstSolution() {
		Solver solver = new Solver();
		// variables
		int max = 100*optimum;
		IntVar totalCost = VariableFactory.bounded("obj", 0, max, solver);
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
		gc.addPropagators(new PropCycleEvalObj(undi, totalCost, distMatrix, gc, solver));
		solver.post(gc);
		// config
		solver.set(StrategyFactory.graphStrategy(undi, null, new MinCost(undi), GraphStrategy.NodeArcPriority.ARCS));
        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		// resolution
		solver.findSolution();
		checkUndirected(solver, undi, totalCost, distMatrix);
		//output
		if(!undi.instantiated()){
			throw new UnsupportedOperationException();
		}
		System.out.println("first solution");
		bestCost = totalCost.getValue();
		System.out.println("cost : "+bestCost);
		bestSolution = new int[n];
		int x = 0;
		INeighbors nei = undi.getEnvelopGraph().getSuccessorsOf(x);
		int y = nei.getFirstElement();
		int tmp;
		String s = "";
		for(int i=0;i<n;i++){
			bestSolution[i] = x;
			tmp = x;
			x = y;
			nei = undi.getEnvelopGraph().getSuccessorsOf(x);
			y = nei.getFirstElement();
			if(y==tmp){
				y = nei.getNextElement();
			}
			s += bestSolution[i]+", ";
		}
		System.out.println(s);
	}

	private void checkUndirected(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
		if (solver.getMeasures().getSolutionCount() == 0) {
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
			if(sum!=solver.getSearchLoop().getObjectivemanager().getBestValue() && sum!=totalCost.getValue()){
				throw new UnsupportedOperationException();
			}
		}
	}

	//***********************************************************************************
	// RESOLUTION
	//***********************************************************************************

	private void initLNS() {
		int nb = n/SIZE;
		slaves = new TSPslave[nb];
		for(int i=0;i<nb-1;i++){
			slaves[i] = new TSPslave(this,i,SIZE);
		}
		slaves[nb-1] = new TSPslave(this,nb-1,SIZE+n%SIZE);
	}

	private void LNS() {
		int step = SIZE/3;
		boolean impr = true;
		while(impr){
			impr = false;
			for(int off = step;off<n;off += step){
				slideSolution(step);
				impr |= LNS_one_run();
			}
		}
	}

	private boolean LNS_one_run() {
		int idx = 0;
		int nb = slaves.length;
		int linkCost = distMatrix[bestSolution[n-1]][bestSolution[0]];
		for(int i=0;i<nb;i++){
			int[] fr = slaves[i].getInputFragment();
			for(int j=0;j<fr.length;j++){
				fr[j] = bestSolution[idx++];
			}
			slaves[i].set(fr,distMatrix);
			if(idx<n){
				linkCost+=distMatrix[bestSolution[idx-1]][bestSolution[idx]];
			}
		}
		if(idx!=n)throw new UnsupportedOperationException();
		// solve
		distributedSlavery();
		// regroup
		int obj = linkCost;
		System.out.println("link : "+linkCost);
		idx = 0;
		for(int i=0;i<nb;i++){
			obj += slaves[i].getOutputCost();
			System.out.println("frcost "+slaves[i].getOutputCost());
			int[] fr = slaves[i].getOutputFragment();
			for(int j=0;j<fr.length;j++){
				bestSolution[idx++] = fr[j];
			}
		}
		// output
		System.out.println("old objective : "+bestCost);
		System.out.println("new objective : "+obj);
		if(obj>bestCost){
			throw new UnsupportedOperationException();
		}
		boolean improved = obj<bestCost;
		bestCost = obj;
		checkCost();
		return improved;
	}

	private void slideSolution(int offSet){
		checkCost();
		int[] ns = new int[n];
		for(int i=0;i<n;i++){
			if(i+offSet<n){
				ns[i+offSet] = bestSolution[i];
			}else{
				ns[i+offSet-n] = bestSolution[i];
			}
		}
		bestSolution = ns;
		checkCost();
	}

	private void checkCost(){
		int obj = distMatrix[bestSolution[n-1]][bestSolution[0]];
		for(int i=0;i<n-1;i++){
			obj += distMatrix[bestSolution[i]][bestSolution[i+1]];
		}
		if(obj!=bestCost){
			throw new UnsupportedOperationException();
		}
	}

	//***********************************************************************************
	// BRANCHING
	//***********************************************************************************

	private class MinCost extends ArcStrategy<UndirectedGraphVar> {
		public MinCost(UndirectedGraphVar undirectedGraphVar) {
			super(undirectedGraphVar);
		}
		@Override
		public boolean computeNextArc() {
			int cost=-1;
			INeighbors nei,ker;
			for(int i=0;i<n;i++){
				ker = g.getKernelGraph().getSuccessorsOf(i);
				if(ker.neighborhoodSize()<2){
					nei = g.getEnvelopGraph().getSuccessorsOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!ker.contain(j)){
							if(cost==-1 || distMatrix[i][j]<cost){
								cost = distMatrix[i][j];
								this.from = i;
								this.to   = j;
							}
						}
					}
				}
			}
			return cost!=-1;
		}
	}
}