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
import solver.ICause;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.trees.PropTreeEvalObj;
import solver.constraints.propagators.gary.trees.PropTreeNoSubtour;
import solver.constraints.propagators.gary.trees.lagrangianRelaxation.PropIterativeMST;
import solver.constraints.propagators.gary.trees.lagrangianRelaxation.PropTreeHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.exception.ContradictionException;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.TSP_heuristics;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class DegreeConstraintMinimumSpanningTree {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 5000;
	private static String outFile;
	private static int upperBound = Integer.MAX_VALUE/4;
	private static IntVar totalCost;
	private static Solver solver;
	private static int search;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		String instType = "shrd";
		int maxDegree = 2;
		search = 2;
		clearFile(outFile = "dcmst_"+instType+"_d"+maxDegree+"_s"+search+".csv");
		writeTextInto("instance;sols;fails;nodes;time;obj;search;\n", outFile);
		String dir = "/Users/jfages07/Documents/tree_partitioning/SHRD-Graphs";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix;
		for (String s : list) {
			if (s.contains(instType) && (!s.contains("shxrd150"))){
				matrix = parse(dir + "/" + s, instType);
				if((matrix!=null && matrix.length>=0 && matrix.length<4000)){
//					upperBound = 100000;
					setUB(s.split("\\.")[0],maxDegree);
					System.out.println("optimum : "+upperBound);
//					if(maxDegree==2)solveTSP(matrix, s);
					solveDCMST(matrix, s, maxDegree);
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
	}

	public static int[][] parseCRD(String url, int n) {
		if(n==10){
			n = 100;
		}
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line;
			int[] x = new int[n];
			int[] y = new int[n];
			int[][] dist = new int[n][n];
			line = buf.readLine();
			int idx = 0;
			String[] numbers;
			while(line!=null){
				line = line.replaceAll(" * ", " ");
				numbers = line.split(" ");
				int first = 0;
				if(numbers[0].equals("")){
					first++;
				}
				for(int i=first;i<numbers.length;i+=2){
					x[idx] = Integer.parseInt(numbers[i]);
					y[idx++] = Integer.parseInt(numbers[i+1]);
				}
				line = buf.readLine();
			}
			for(int i=0;i<n;i++){
				for(int j=i+1;j<n;j++){
					int xd = x[i]-x[j];
					int yd = y[i]-y[j];
					dist[i][j] = dist[j][i] = (int) Math.sqrt((xd*xd+yd*yd));
				}
			}
			return dist;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	public static int[][] parse(String url,String type) {
		int n = Integer.parseInt(url.split(type)[1].substring(0,2));
		System.out.println("parsing instance " + url + "...");
		if(type.equals("crd")){
			return parseCRD(url,n);
		}
		if(n==10){
			n = 100;
		}
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line;
			int[][] dist = new int[n][n];
			line = buf.readLine();
			int idxI = 1;
			int idxJ = 0;
			String[] numbers;
			while(line!=null){
				line = line.replaceAll(" * ", " ");
				numbers = line.split(" ");
				int first = 0;
				if(numbers[0].equals("")){
					first++;
				}
				for(int i=first;i<numbers.length;i++){
					dist[idxI][idxJ] = dist[idxJ][idxI] = Integer.parseInt(numbers[i]);
					idxJ++;
					if(idxJ==idxI){
						idxI++;
						idxJ = 0;
					}
				}
				line = buf.readLine();
			}
//			String s = "";
//			for(int i=0;i<n;i++){
//				s+="\n";
//				for(int j=0;j<n;j++){
//					s+= dist[i][j]+"\t";
//				}
//			}
//			System.out.println(s);
//				System.exit(0);
			return dist;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	private static void setUB(String s, int maxDegree) {
		File file = new File("/Users/jfages07/Documents/tree_partitioning/SHRD-Graphs/bestSolutions.txt");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			while(!line.contains(s)){
				line = buf.readLine();
			}
			while (maxDegree>2){
				maxDegree--;
				line = buf.readLine();
			}
			line = line.replaceAll(" * ", " ");
			upperBound = Integer.parseInt(line.split(" ")[2]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void solveDCMST(int[][] matrix, String instanceName,int maxDegree) {
		int n = matrix.length;
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j] != matrix[j][i]){
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
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(undi,solver);
		gc.addAdHocProp(new PropAtLeastNNeighbors(undi,1,gc,solver));
		gc.addAdHocProp(new PropAtMostNNeighbors(undi,maxDegree,gc,solver));
		gc.addAdHocProp(new PropTreeNoSubtour(undi,gc,solver));
		gc.addAdHocProp(new PropTreeEvalObj(undi,totalCost,matrix,gc,solver));
		gc.addAdHocProp(PropTreeHeldKarp.mstBasedRelaxation(undi, totalCost, maxDegree, matrix, gc, solver));
		gc.addAdHocProp(PropIterativeMST.mstBasedRelaxation(undi, totalCost, maxDegree, matrix, gc, solver));
		solver.post(gc);
		// config
		AbstractStrategy strat = StrategyFactory.graphTSP(undi, TSP_heuristics.enf_MinDeg, null);
		switch (search){
			case 0: solver.set(strat);break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),strat));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),strat));
				solver.getSearchLoop().restartAfterEachSolution(true);
				break;
			default: throw new UnsupportedOperationException();
		}
		solver.set(new Sort(new PArc(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
//		solver.findSolution();
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		if(solver.getMeasures().getSolutionCount()==0 && solver.getMeasures().getTimeCount()<TIMELIMIT){
			throw new UnsupportedOperationException();
		}
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ solver.getMeasures().getNodeCount() + ";"+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost +";"+search+";\n";
		writeTextInto(txt, outFile);
	}

	private static void solveTSP(int[][] originalMatrix, String instanceName) {
		int n = originalMatrix.length+1;
		int[][] matrix = new int[n][n];
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-1;j++){
				matrix[i][j] = originalMatrix[i][j];
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
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(undi,solver);
		gc.addAdHocProp(new PropAtLeastNNeighbors(undi,2,gc,solver));
		gc.addAdHocProp(new PropAtMostNNeighbors(undi,2,gc,solver));
		gc.addAdHocProp(new PropCycleNoSubtour(undi,gc,solver));
		gc.addAdHocProp(new PropCycleEvalObj(undi,totalCost,matrix,gc,solver));
		gc.addAdHocProp(PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver));
		solver.post(gc);
		// config
		AbstractStrategy strat = StrategyFactory.graphTSP(undi, TSP_heuristics.enf_sparse, null);
		switch (search){
			case 0: solver.set(strat);break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),strat));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),strat));
				solver.getSearchLoop().restartAfterEachSolution(true);
				break;
			default: throw new UnsupportedOperationException();
		}
		solver.set(new Sort(new PArc(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
//		solver.findSolution();
//		System.exit(0);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName+";"+solver.getMeasures().getSolutionCount()+";"+solver.getMeasures().getFailCount()+";"
				+ solver.getMeasures().getNodeCount()+";"+(int)(solver.getMeasures().getTimeCount())+";"+bestCost+";"+search+";\n";
		writeTextInto(txt, outFile);
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
	// HEURISTICS
	//***********************************************************************************

	static int lb;
	static int ub;

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

	private static class DichotomicSearchWithoutRestarting extends AbstractStrategy<IntVar> {
		IntVar obj;
		long nbSols;
		protected DichotomicSearchWithoutRestarting(IntVar obj) {
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

	public static Assignment<IntVar> objCut = new Assignment<IntVar>() {
		@Override
		public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
			var.updateUpperBound(value, cause);
		}
		@Override
		public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
			lb = value+1;
			System.out.println("UNAPPLY");
			var.updateLowerBound(value + 1, cause);
		}
		@Override
		public String toString() {
			return " <= ";
		}
	};

	private static class CompositeSearch extends AbstractStrategy {

		AbstractStrategy s1,s2;
		protected CompositeSearch(AbstractStrategy s1, AbstractStrategy s2) {
			super(ArrayUtils.append(s1.vars, s2.vars));
			this.s1 = s1;
			this.s2 = s2;
		}

		@Override
		public void init() {
			s1.init();
			s2.init();
		}

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