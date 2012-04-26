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
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.Prop_LP_GRB;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.constraints.propagators.gary.undirected.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.undirected.PropAtMostNNeighbors;
import solver.exception.ContradictionException;
import solver.propagation.generator.Primitive;
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

	private static final long TIMELIMIT = 100000;
	private static String outFile;
	private static int upperBound = Integer.MAX_VALUE/4;
	private static IntVar totalCost;
	private static Solver solver;
	private static PropSymmetricHeldKarp mst;
	private static int search;
	private static TSP_heuristics heuristic;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		clearFile(outFile = "tsp.csv");
		writeTextInto("instance;sols;fails;nodes;time;obj;search;\n", outFile);
		String dir = "/Users/jfages07/Documents/tree_partitioning/SHRD-Graphs";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix;
		search = 2;
		heuristic = TSP_heuristics.enf_sparse;
		for (String s : list) {
			if (s.contains("str") && (!s.contains("xxx"))){
				matrix = parse(dir + "/" + s, "str");
				if((matrix!=null && matrix.length>=0 && matrix.length<3000)){
					setUB(s.split("\\.")[0]);
					System.out.println("optimum : "+upperBound);
					solveUndirected(matrix, s);
					System.exit(0);
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
	}

	public static int[][] parseCRD(String url, int n) {
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
			// haut droite
//			int idxI = 0;
//			int idxJ = 1;
//			String[] numbers;
//			while(line!=null){
//				line = line.replaceAll(" * ", " ");
//				numbers = line.split(" ");
//				int first = 0;
//				if(numbers[0].equals("")){
//					first++;
//				}
//				for(int i=first;i<numbers.length;i++){
//					dist[idxI][idxJ] = dist[idxJ][idxI] = Integer.parseInt(numbers[i]);
//					idxJ++;
//					if(idxJ==n){
//						idxI++;
//						idxJ = idxI+1;
//					}
//				}
//				line = buf.readLine();
//			}
			// bas gauche
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

	private static void setUB(String s) {
		File file = new File("/Users/jfages07/Documents/tree_partitioning/SHRD-Graphs/bestSolutions.txt");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			while(!line.contains(s)){
				line = buf.readLine();
			}
			line = line.replaceAll(" * ", " ");
			upperBound = Integer.parseInt(line.split(" ")[2]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void solveUndirected(int[][] matrix, String instanceName) {
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
		gc.addAdHocProp(new PropCycleNoSubtour(undi,gc,solver));
		gc.addAdHocProp(new PropAtLeastNNeighbors(undi,solver,gc,2));
		gc.addAdHocProp(new PropAtMostNNeighbors(undi,solver,gc,2));
		gc.addAdHocProp(new PropCycleEvalObj(undi,totalCost,matrix,gc,solver));
		mst = PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
		gc.addAdHocProp(mst);
//		gc.addAdHocProp(new Prop_LP_GRB(undi,totalCost,matrix,solver,gc));
		solver.post(gc);
		// config
		switch (search){
			case 0: solver.set(StrategyFactory.graphTSP(undi, heuristic, mst));break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),StrategyFactory.graphTSP(undi, heuristic, mst)));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),StrategyFactory.graphTSP(undi, heuristic, mst)));
				solver.getSearchLoop().restartAfterEachSolution(true);
				break;
			default: throw new UnsupportedOperationException();
		}
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		checkUndirected(solver, undi, totalCost, matrix);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ solver.getMeasures().getNodeCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost +";"+search+";\n";
		writeTextInto(txt, outFile);
	}

	private static void checkUndirected(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
		int n = matrix.length;
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			writeTextInto("BUG\n",outFile);
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
			if(sum!=solver.getSearchLoop().getObjectivemanager().getBestValue()){
				writeTextInto("BUG\n",outFile);
				throw new UnsupportedOperationException();
			}
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