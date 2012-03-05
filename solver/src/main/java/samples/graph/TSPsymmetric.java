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
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.tsp.directed.PropEvalObj;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.directed.PropOneSuccBut;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropFastSymmetricHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.constraints.propagators.gary.undirected.*;
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
import solver.search.strategy.decision.graph.GraphDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;
import java.util.BitSet;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class TSPsymmetric {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 120000;
	private static final int MAX_SIZE = 1200;
	private static long seed = 0;
	private static String outFile;
	private static int upperBound = Integer.MAX_VALUE/4;
	private static IntVar totalCost;
	private static Solver solver;
	private static GraphVar graph;
	private static int[][] dist;
	private static boolean allDiffAC    = false;
	private static boolean optProofOnly = true;
	private static boolean fast;
	private static PropSymmetricHeldKarp mst;
	private static int search;
	private static boolean restart;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		clearFile(outFile = "tsp.csv");
		writeTextInto("instance;sols;fails;time;obj;allDiffAC;search;\n", outFile);
		bench();
	}

	public static int[][] parseInstance(String url) {
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String name = line.split(":")[1].replaceAll(" ", "");
			System.out.println("parsing instance " + name + "...");
			while(!line.contains("DIMENSION")){
				line = buf.readLine();
			}
			int n = Integer.parseInt(line.split(":")[1].replaceAll(" ", ""));
			if(n>MAX_SIZE){
				return null;
			}
			System.out.println("n : "+n);
			int[][] dist = new int[n][n];
			//
			while(!line.contains("EDGE_WEIGHT_TYPE")){
				line = buf.readLine();
			}
			String type = line.split(": ")[1];
			if(type.contains("EXPLICIT")){
				while(!line.contains("EDGE_WEIGHT_FORMAT")){
					line = buf.readLine();
				}
				String format = line.split(": ")[1];
				while(!line.contains("EDGE_WEIGHT_SECTION")){
					line = buf.readLine();
				}
				if(format.contains("UPPER_ROW")){
					halfMatrix(dist,buf);
				}else if(format.contains("FULL_MATRIX")){
					fullMatrix(dist,buf);
				}else if(format.contains("LOWER_DIAG_ROW")){
					lowerDiagMatrix(dist,buf);
				}else if(format.contains("UPPER_DIAG_ROW")){
					upperDiagMatrix(dist,buf);
				}else{
					return null;
				}
			}else if(type.contains("CEIL_2D")||type.contains("EUC_2D")||type.contains("ATT")||type.contains("GEO")){
				while(!line.contains("NODE_COORD_SECTION")){
					line = buf.readLine();
				}
				coordinates(dist,buf,type);
			}else{
				throw new UnsupportedOperationException();
			}

			return dist;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	private static void coordinates(int[][] dist, BufferedReader buf, String type) throws IOException {
		int n = dist.length;
		String line;
		double[] x = new double[n];
		double[] y = new double[n];
		line = buf.readLine();
		String[] lineNumbers;
		for(int i=0;i<n;i++){
			line = line.replaceAll(" * ", " ");
			lineNumbers = line.split(" ");
			if(lineNumbers.length!=4 && lineNumbers.length!=3){
				System.out.println("wrong line "+line);
				throw new UnsupportedOperationException("wrong format");
			}
			x[i] = Double.parseDouble(lineNumbers[lineNumbers.length-2]);
			y[i] = Double.parseDouble(lineNumbers[lineNumbers.length-1]);
			line = buf.readLine();
		}
		if(!line.contains("EOF")){
//			throw new UnsupportedOperationException();
		}
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				dist[i][j] = getDist(x[i],x[j],y[i],y[j],type);
				dist[j][i] = dist[i][j];
			}
		}
	}

	private static int getDist(double x1, double x2, double y1, double y2, String type) {
		double xd = x2-x1;
		double yd = y2-y1;
		if(type.contains("CEIL_2D")){
			double rt = Math.sqrt((xd*xd+yd*yd));
			return (int)Math.ceil(rt);
		}
		if(type.contains("EUC_2D")){
			double rt = Math.sqrt((xd*xd+yd*yd));
			return (int)Math.round(rt);
		}
		if(type.contains("ATT")){
			double rt = Math.sqrt((xd*xd+yd*yd)/10);
			int it = (int)Math.round(rt);
			if(it<rt){
				it++;
			}
			return it;
		}
		if(type.contains("GEO")){
			double PI = 3.141592;
			double min;
			int deg;
			// i
			deg = (int) x1;
			min = x1 - deg;
			double lati = PI*(deg+(5.0*min)/3.0)/180.0;
			deg = (int) y1;
			min = y1 - deg;
			double longi = PI*(deg+(5.0*min)/3.0)/180.0;
			// j
			deg = (int) x2;
			min = x2 - deg;
			double latj = PI*(deg+(5.0*min)/3.0)/180.0;
			deg = (int) y2;
			min = y2 - deg;
			double longj = PI*(deg+(5.0*min)/3.0)/180.0;

			double RRR = 6378.388;
			double q1 = Math.cos(longi-longj);
			double q2 = Math.cos(lati-latj);
			double q3 = Math.cos(lati+latj);

			double dij = RRR*Math.acos(((1+q1)*q2-(1-q1)*q3)/2)+1;
			return (int) dij;
		}
		throw new UnsupportedOperationException("wrong format");
	}

	private static void halfMatrix(int[][] dist, BufferedReader buf) throws IOException {
		int n = dist.length;
		String line;
		line = buf.readLine();
		String[] lineNumbers;
		for(int i=0;i<n-1;i++){
			line = line.replaceAll(" * ", " ");
			lineNumbers = line.split(" ");
			int off = 0;
			if(lineNumbers[0].equals("")){
				off++;
			}
			for(int k=0;k<lineNumbers.length-off;k++){
				dist[i][i+k+1] = Integer.parseInt(lineNumbers[k+off]);
				dist[i+k+1][i] = dist[i][i+k+1];
			}
			line = buf.readLine();
		}
	}
	private static void fullMatrix(int[][] dist, BufferedReader buf) throws IOException {
		int n = dist.length;
		String line;
		line = buf.readLine();
		String[] lineNumbers;
		for(int i=0;i<n;i++){
			line = line.replaceAll(" * ", " ");
			lineNumbers = line.split(" ");
			int off = 0;
			if(lineNumbers[0].equals("")){
				off++;
			}
			for(int k=0;k<n;k++){
				dist[i][k] = Integer.parseInt(lineNumbers[k+off]);
			}
			line = buf.readLine();
		}
	}
	private static void lowerDiagMatrix(int[][] dist, BufferedReader buf) throws IOException {
		int n = dist.length;
		String line;
		line = buf.readLine();
		String[] lineNumbers;
		int l=0,c=0;
		while(true){
			line = line.replaceAll(" * ", " ");
			lineNumbers = line.split(" ");
			int off = 0;
			if(lineNumbers[0].equals("")){
				off++;
			}
			for(int i=off;i<lineNumbers.length;i++){
				dist[l][c] = Integer.parseInt(lineNumbers[i]);
				dist[c][l] = dist[l][c];
				c++;
				if(c>l){
					c=0;
					l++;
				}
				if(l==dist.length){
					return;
				}
			}
			line = buf.readLine();
		}
	}
	private static void upperDiagMatrix(int[][] dist, BufferedReader buf) throws IOException {
		int n = dist.length;
		String line;
		line = buf.readLine();
		String[] lineNumbers;
		int l=0,c=0;
		while(true){
			line = line.replaceAll(" * ", " ");
			lineNumbers = line.split(" ");
			int off = 0;
			if(lineNumbers[0].equals("")){
				off++;
			}
			for(int i=off;i<lineNumbers.length;i++){
				dist[l][c] = Integer.parseInt(lineNumbers[i]);
				dist[c][l] = dist[l][c];
				c++;
				if(c>=n){
					l++;
					c=l;
				}
				if(l==dist.length){
					return;
				}
			}
			line = buf.readLine();
		}
	}

	private static void bench() {
		String dir = "/Users/jfages07/github/In4Ga/benchRousseau";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix;
		optProofOnly = true;
		allDiffAC = false;
		fast = true;
		search = 0;
		for (String s : list) {
			if (s.contains(".tsp") && (!s.contains("gz"))){
				matrix = parseInstance(dir + "/" + s);
				if(matrix!=null && matrix.length>=0 && matrix.length<200){
					int n = matrix.length;
//					int maxArcCost = 0;
//					for(int i=0;i<n;i++){
//						for(int j=0;j<n;j++){
//							if(matrix[i][j]>maxArcCost){
//								maxArcCost = matrix[i][j];
//							}
//						}
//					}
//					upperBound = maxArcCost*n;
					if(optProofOnly){
						setUB(s.split("\\.")[0]);
						System.out.println("optimum : "+upperBound);
					}
//					solveDirected(matrix, s);
//					allDiffAC = false;
//					solveUndirected(matrix, s);
//					restart = true;
//					search = 2;
					degHeur = true;
					solveUndirected(matrix, s);
//					System.exit(0);
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
	}

	private static void setUB(String s) {
		File file = new File("/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			while(!line.contains(s)){
				line = buf.readLine();
			}
			upperBound = Integer.parseInt(line.split(";")[1]);
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
					System.out.println(i+" : "+j);
					System.out.println(matrix[i][j]+" != "+matrix[j][i]);
					throw new UnsupportedOperationException();
				}
			}
		}
		solver = new Solver();
		// variables
		totalCost = VariableFactory.bounded("obj",0,upperBound,solver);
		final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		graph = undi;
		dist = matrix;
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
		if(allDiffAC){
			gc.addAdHocProp(new PropAllDiffGraphIncremental(undi,n,solver,gc));
		}
		if(fast){
			mst = PropFastSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
		}else{
			mst = PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
		}
		gc.addAdHocProp(mst);
		switch (search){
			case 0: solver.set(new RCSearch(undi));break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),new RCSearch(undi)));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),new RCSearch(undi)));break;
			default: throw new UnsupportedOperationException();
		}
		solver.post(gc);
		// config
//		solver.set(StrategyFactory.graphRandom(undi,seed));
//		solver.set(new RCSearch(undi));
//		solver.set(new StrategiesSequencer(solver.getEnvironment(),new BottomUp(totalCost),new RCSearch(undi)));
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		if(restart){
			solver.getSearchLoop().restartAfterEachSolution(true);
		}
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		//solver.findSolution();
		checkUndirected(solver, undi, totalCost, matrix);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost + ";"+allDiffAC+";"+search+";\n";
		writeTextInto(txt, outFile);
	}

	private static void checkUndirected(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
		int n = matrix.length;
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			writeTextInto("BUG\n",outFile);
//			throw new UnsupportedOperationException();
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
//				throw new UnsupportedOperationException();
			}
		}
	}

	private static void solveDirected(int[][] m, String instanceName) {
		int[][] matrix = transformMatrix(m);
		int n = matrix.length;
		solver = new Solver();
		// variables
		totalCost = VariableFactory.bounded("obj",0,upperBound,solver);
		DirectedGraphVar dir = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST);
		graph = dir;
		dist = matrix;
		dir.getKernelGraph().activateNode(n-1);
		for(int i=0;i<n-1;i++){
			dir.getKernelGraph().activateNode(i);
			for(int j=1;j<n;j++){
				if(i!=j && !(i==0 && j==n-1)){
					dir.getEnvelopGraph().addArc(i,j);
				}
			}
		}
		// constraints
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(dir,solver);
		gc.addAdHocProp(new PropOneSuccBut(dir, n - 1, gc, solver));
		gc.addAdHocProp(new PropOnePredBut(dir, 0, gc, solver));
		gc.addAdHocProp(new PropPathNoCycle(dir, 0, n - 1, gc, solver));
		gc.addAdHocProp(new PropEvalObj(dir, totalCost, matrix, gc, solver));
		if(allDiffAC){
			gc.addAdHocProp(new PropAllDiffGraphIncremental(dir,n-1,solver,gc));
		}
		gc.addAdHocProp(PropHeldKarp.mstBasedRelaxation(dir,0,n-1,totalCost,matrix,gc,solver));
		solver.post(gc);
		// config
		switch (search){
			case 0: solver.set(new RCSearch(dir));break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),new RCSearch(dir)));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),new RCSearch(dir)));break;
			default: throw new UnsupportedOperationException();
		}
//		solver.set(StrategyFactory.graphStrategy(dir, null, new MinDomMaxRegretMinCost(dir,matrix), GraphStrategy.NodeArcPriority.ARCS));
//		solver.set(StrategyFactory.graphStrategy(dir, null, new MinNeighMinCost(dir,matrix), GraphStrategy.NodeArcPriority.ARCS));
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		//solver.findSolution();
		checkDirected(solver,dir,totalCost,matrix);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost + ";directed;"+allDiffAC+";\n";
		writeTextInto(txt, outFile);
	}

	private static void checkDirected(Solver solver, DirectedGraphVar dir, IntVar totalCost, int[][] matrix) {
		int n = matrix.length;
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			writeTextInto("BUG\n",outFile);
//				throw new UnsupportedOperationException();
		}
		if (solver.getMeasures().getSolutionCount() > 0){
			int sum = 0;
			for(int i=0;i<n-1;i++){
				int j = dir.getKernelGraph().getSuccessorsOf(i).getFirstElement();
				sum+=matrix[i][j];
			}
			if(sum!=totalCost.getValue()){
				writeTextInto("BUG\n",outFile);
//				throw new UnsupportedOperationException();
			}
		}
	}

	public static int[][] transformMatrix(int[][] m) {
		int n=m.length+1;
		int[][] matrix = new int[n][n];
		for(int i=0;i<n-1;i++){
			for(int j=1;j<n-1;j++){
				matrix[i][j] = m[i][j];
			}
			matrix[i][n-1] = m[i][0];
		}
		return matrix;
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

	private static class ResMon extends VoidSearchMonitor implements ISearchMonitor {
		int nbFails;
		public void onContradiction(ContradictionException cex){
			nbFails++;
			if(nbFails == 200){
				nbFails = 0;
				solver.getSearchLoop().restart();
			}
		}
	}

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

	//***********************************************************************************
	// HEURISTICS
	//***********************************************************************************

	private static class MinCostMinNeigh extends ArcStrategy {
		int[][] dist;

		public MinCostMinNeigh(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
		}

		@Override
		public int nextArc() {
			if(g.instantiated()){
				System.out.println("over");
				return -1;
			}
			INeighbors suc;
			int cost = -1;
			BitSet list = new BitSet(n);
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()){
					if(!g.getKernelGraph().arcExists(i,j)){
						if(cost == -1 || cost>dist[i][j]){
							list.clear();
							list.set(i);
							cost = dist[i][j];
						}else if(cost==dist[i][j]){
							list.set(i);
						}
					}
				}
			}
			int from = -1;
			int size = n + 1;
			size = 0;
			for (int i = list.nextSetBit(0); i>=0; i=list.nextSetBit(i+1)) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() > size) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				throw new UnsupportedOperationException();
			}
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(dist[from][j]==cost && !g.getKernelGraph().arcExists(from,j)){
					System.out.println(from+" -> "+j+" : "+cost);
					return (from + 1) * n + j;
				}
			}
			throw new UnsupportedOperationException("error in branching");
		}
	}

	private static class MaxRegretMinCost extends ArcStrategy {
		int[][] dist;
		int offSet;

		public MaxRegretMinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public int nextArc() {
			INeighbors suc;
			int from;
			if(g.isDirected()){
				from = nextFromDir();
			}else{
				from = nextFromUndir();
			}
			if(from==-1){
				if(!g.instantiated()){
					throw new UnsupportedOperationException();
				}
				return -1;
			}
			int val;
			int to = -1;
			int minCost = -1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					val = dist[from][j];
					if(minCost == -1 || val<minCost){
						minCost = val;
						to = j;
					}
				}
			}
			if (to == -1 || g.getKernelGraph().arcExists(from, to)) {
				throw new UnsupportedOperationException("error in branching");
			}
//			System.out.println("from "+from+" to "+to+" cost "+dist[from][to]);
			return (from+1)*n+to;
		}
		public int nextFromDir() {
			INeighbors suc;
			int from = -1;
			int difMax = -1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize()>1) {
					int best = getBest(i,-2,-2, suc);
					int secondBest = getBest(i,best,-2, suc);
					if(dist[i][secondBest]-dist[i][best]>difMax){
						difMax = dist[i][secondBest]-dist[i][best];
						from = i;
					}
				}
			}
			return from;
		}

		public int nextFromUndir() {
			INeighbors suc;
			int from = -1;
			int difMax = -1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize()>2) {
					int mand = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
					int best = mand;
					if(mand==-1){
						best = getBest(i,-2,-2,suc);
						int secondBest = getBest(i,best,-2,suc);
						int thirdBest = getBest(i,best,secondBest,suc);
						int val = dist[i][thirdBest]+dist[i][thirdBest]-dist[i][secondBest]-dist[i][best];
						if(dist[i][secondBest]<dist[i][best]){
							throw new UnsupportedOperationException();
						}
						if(dist[i][thirdBest]<dist[i][secondBest]){
							throw new UnsupportedOperationException();
						}
						if(val<0){
							throw new UnsupportedOperationException();
						}
						if(val>difMax){
							difMax = val;
							from = i;
						}
					}else{
						int secondBest = getBest(i,best,-2,suc);
						int thirdBest = getBest(i,best,secondBest,suc);
						if(dist[i][thirdBest]-dist[i][secondBest]<0){
							throw new UnsupportedOperationException();
						}
						if(dist[i][thirdBest]-dist[i][secondBest]>difMax){
							difMax = dist[i][thirdBest]-dist[i][secondBest];
							from = i;
						}
					}
//					int secondBest = getBest(i,best,-2,suc);
//					int thirdBest = getBest(i,best,secondBest,suc);
//					if(dist[i][thirdBest]-dist[i][secondBest]>difMax){
//						difMax = dist[i][thirdBest]-dist[i][secondBest];
//						from = i;
//					}
				}
			}
			return from;
		}

		private int getBest(int from, int not1, int not2, INeighbors nei) {
			int best = -1;
			for(int j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				if(j!=not1 && j!=not2){
					if(best==-1 || dist[from][j]<dist[from][best]){
						best = j;
					}
				}
			}
			if(best == -1){
				throw new UnsupportedOperationException();
			}
			return best;
		}
	}

	private static class MSTMaxRegretMinCost extends ArcStrategy {
		int[][] dist;
		int offSet;

		public MSTMaxRegretMinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public int nextArc() {
			INeighbors suc;
			int from=-1,to=-1;
			int size = n+1;
			int val;
			int minCost = -1;
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>2 && suc.neighborhoodSize()<size){
					size = suc.neighborhoodSize();
				}
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if(mst.isInMST(i,j) && !g.getKernelGraph().arcExists(i,j)){
						val = dist[i][j];
						if(minCost == -1 || val<minCost){
							minCost = val;
							from = i;
							to = j;
						}
					}
				}
			}
			if (to == -1 || g.getKernelGraph().arcExists(from, to)) {
				throw new UnsupportedOperationException("error in branching");
			}
//			System.out.println("from "+from+" to "+to+" cost "+dist[from][to]);
			return (from+1)*n+to;
		}

		public int nextFromUndir(int size) {
			INeighbors suc;
			int from = -1;
			int difMax = -1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize()==size) {
					int mand = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
					int best = mand;
					if(mand==-1){
						best = getBest(i,-2,-2,suc);
						int secondBest = getBest(i,best,-2,suc);
						int thirdBest = getBest(i,best,secondBest,suc);
						int val = dist[i][thirdBest]+dist[i][thirdBest]-dist[i][secondBest]-dist[i][best];
						if(dist[i][secondBest]<dist[i][best]){
							throw new UnsupportedOperationException();
						}
						if(dist[i][thirdBest]<dist[i][secondBest]){
							throw new UnsupportedOperationException();
						}
						if(val<0){
							throw new UnsupportedOperationException();
						}
						if(val>difMax){
							difMax = val;
							from = i;
						}
					}else{
						int secondBest = getBest(i,best,-2,suc);
						int thirdBest = getBest(i,best,secondBest,suc);
						if(dist[i][thirdBest]-dist[i][secondBest]<0){
							throw new UnsupportedOperationException();
						}
						if(dist[i][thirdBest]-dist[i][secondBest]>difMax){
							difMax = dist[i][thirdBest]-dist[i][secondBest];
							from = i;
						}
					}
//					int secondBest = getBest(i,best,-2,suc);
//					int thirdBest = getBest(i,best,secondBest,suc);
//					if(dist[i][thirdBest]-dist[i][secondBest]>difMax){
//						difMax = dist[i][thirdBest]-dist[i][secondBest];
//						from = i;
//					}
				}
			}
			return from;
		}

		private int getBest(int from, int not1, int not2, INeighbors nei) {
			int best = -1;
			for(int j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				if(j!=not1 && j!=not2){
					if(best==-1 || dist[from][j]<dist[from][best]){
						best = j;
					}
				}
			}
			if(best == -1){
				throw new UnsupportedOperationException();
			}
			return best;
		}
	}

	private static class MinDomMaxRegretMinCost extends ArcStrategy {
		int[][] dist;
		int offSet;

		public MinDomMaxRegretMinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public int nextArc() {
			INeighbors suc;
			int from;
			int size = n+1;
			for(int i=0;i<n;i++){
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>2 && suc.neighborhoodSize()<size){
					size = suc.neighborhoodSize();
				}
			}
			from = nextFromUndir(size);
			if(from==-1){
				if(!g.instantiated()){
					throw new UnsupportedOperationException();
				}
				return -1;
			}
			int val;
			int to = -1;
			int minCost = -1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					val = dist[from][j];
					if(minCost == -1 || val<minCost){
						minCost = val;
						to = j;
					}
				}
			}
			if (to == -1 || g.getKernelGraph().arcExists(from, to)) {
				throw new UnsupportedOperationException("error in branching");
			}
//			System.out.println("from "+from+" to "+to+" cost "+dist[from][to]);
			return (from+1)*n+to;
		}

		public int nextFromUndir(int size) {
			INeighbors suc;
			int from = -1;
			int difMax = -1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize()==size) {
					int mand = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
					int best = mand;
					if(mand==-1){
						best = getBest(i,-2,-2,suc);
						int secondBest = getBest(i,best,-2,suc);
						int thirdBest = getBest(i,best,secondBest,suc);
						int val = dist[i][thirdBest]+dist[i][thirdBest]-dist[i][secondBest]-dist[i][best];
						if(dist[i][secondBest]<dist[i][best]){
							throw new UnsupportedOperationException();
						}
						if(dist[i][thirdBest]<dist[i][secondBest]){
							throw new UnsupportedOperationException();
						}
						if(val<0){
							throw new UnsupportedOperationException();
						}
						if(val>difMax){
							difMax = val;
							from = i;
						}
					}else{
						int secondBest = getBest(i,best,-2,suc);
						int thirdBest = getBest(i,best,secondBest,suc);
						if(dist[i][thirdBest]-dist[i][secondBest]<0){
							throw new UnsupportedOperationException();
						}
						if(dist[i][thirdBest]-dist[i][secondBest]>difMax){
							difMax = dist[i][thirdBest]-dist[i][secondBest];
							from = i;
						}
					}
//					int secondBest = getBest(i,best,-2,suc);
//					int thirdBest = getBest(i,best,secondBest,suc);
//					if(dist[i][thirdBest]-dist[i][secondBest]>difMax){
//						difMax = dist[i][thirdBest]-dist[i][secondBest];
//						from = i;
//					}
				}
			}
			return from;
		}

		private int getBest(int from, int not1, int not2, INeighbors nei) {
			int best = -1;
			for(int j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				if(j!=not1 && j!=not2){
					if(best==-1 || dist[from][j]<dist[from][best]){
						best = j;
					}
				}
			}
			if(best == -1){
				throw new UnsupportedOperationException();
			}
			return best;
		}
	}

	private static class MinNeighMinCost extends ArcStrategy {
		int[][] dist;
		int offSet;

		public MinNeighMinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public int nextArc() {
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			int sizi;
			for (int i = 0; i < n; i++) {
				sizi = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()-g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
				if (sizi < size && sizi>0) {
					from = i;
					size = sizi;
				}
			}
			if (from == -1) {
//				System.out.println("over");
				return -1;
			}
			int val;
			int to = -1;
			int minCost = -1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					val = dist[from][j];
					if(minCost == -1 || val<minCost){
						minCost = val;
						to = j;
					}
				}
			}
			if (to == -1) {
				throw new UnsupportedOperationException("error in branching");
			}
			if (g.getKernelGraph().arcExists(from, to)) {
				throw new UnsupportedOperationException("error in branching");
			}
//			System.out.println("from "+from+" to "+to+" cost "+dist[from][to]);
			return (from+1)*n+to;
		}
	}

	private static class MinNeighMinCostOriginal extends ArcStrategy {
		int[][] dist;
		int offSet;

		public MinNeighMinCostOriginal(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public int nextArc() {
			if(g.instantiated()){
				return -1;
			}
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n-offSet; i++) {
				if(g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()>2-offSet){
					System.out.println(i+":"+g.getKernelGraph().getSuccessorsOf(i)+":"+g.isDirected()+" : "+n);
					throw new UnsupportedOperationException();
				}
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 2-offSet) {
					from = i;
					size = suc.neighborhoodSize();
				}
			}
			if (from == -1) {
				return -1;
			}
			int val;
			int minArc = -1;
			int minCost = -1;
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					val = dist[from][j];
					if(minCost == -1 || val<minCost){
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

	private static class MinRealNeighMinCost extends ArcStrategy {
		int[][] dist;

		public MinRealNeighMinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
		}

		@Override
		public int nextArc() {
			if(g.instantiated()){
				return -1;
			}
			INeighbors suc;
			int from = -1;
			int size = n + 1;
			for (int i = 0; i < n; i++) {
				if(g.getKernelGraph().getNeighborsOf(i).neighborhoodSize()>2){
					System.out.println(i+":"+g.getKernelGraph().getSuccessorsOf(i)+":"+g.isDirected()+" : "+n);
					throw new UnsupportedOperationException();
				}
				suc = g.getEnvelopGraph().getNeighborsOf(i);
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
			int minCost = -1;
			suc = g.getEnvelopGraph().getNeighborsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if((!g.getKernelGraph().arcExists(from,j)) && (!g.getKernelGraph().arcExists(j,from))){
					if(g.getEnvelopGraph().arcExists(from,j)){
						val = dist[from][j];
						if(minCost == -1 || val<minCost){
							minCost = val;
							minArc = (from + 1) * n + j;
						}
					}else{
						val = dist[j][from];
						if(minCost == -1 || val<minCost){
							minCost = val;
							minArc = (j + 1) * n + from;
						}
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

	private static class MinCost extends ArcStrategy {
		int[][] dist;
		int offSet;

		public MinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public int nextArc() {
			if(g.instantiated()){
				return -1;
			}
			int val;
			int minArc = -1;
			int minCost = -1;
			INeighbors suc;
			int size = n + 1;
			for (int i = 0; i < n-offSet; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if (suc.neighborhoodSize() < size && suc.neighborhoodSize() > 2-offSet) {
					size = suc.neighborhoodSize();
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if(!g.getKernelGraph().arcExists(i,j)){
							val = dist[i][j];
							if(minCost == -1 || val<minCost){
								minCost = val;
								minArc = (i + 1) * n + j;
							}
						}
					}
				}
			}
			if (minArc == -1) {
				return -1;
			}
			return minArc;
		}
	}

//	private static class MaxRC extends ArcStrategy {
//		public MaxRC(GraphVar graphVar) {
//			super(graphVar);
//		}
//		@Override
//		public int nextArc() {
//			int minArc = mst.getHighestRC();
//			if (minArc == -1) {
//				if(!g.instantiated()){
//					throw new UnsupportedOperationException();
//				}
//				return -1;
//			}
//			return minArc;
//		}
//	}

	private static class ConstructorHeur extends ArcStrategy {
		BitSet seen;
		int[][] matrix;
		public ConstructorHeur(GraphVar graphVar, int[][] m) {
			super(graphVar);
			seen = new BitSet(n);
			matrix = m;
		}
		@Override
		public int nextArc() {
			seen.clear();
			int x = 0;
			int y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
			seen.set(x);
			while(y!=-1 && !seen.get(y)){
				x = y;
				seen.set(x);
				y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
				if(y!=-1 && seen.get(y)){
					y = g.getKernelGraph().getSuccessorsOf(x).getNextElement();
				}
			}
			if(y!=-1 && seen.get(y)){
//				if(g.instantiated()){
//				System.out.println("over");
				return -1;
//				}else{
//					throw new UnsupportedOperationException();
//				}
			}
			int minSuc = -1;
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(x);
			if(nei.neighborhoodSize()-g.getKernelGraph().getSuccessorsOf(x).neighborhoodSize()<=0){
//				System.out.println("over");
				return -1;
//				throw new UnsupportedOperationException();
			}
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!seen.get(j)){
					if(minSuc==-1 || matrix[x][j]<matrix[x][minSuc]){
						minSuc = j;
					}
					if(g.getKernelGraph().arcExists(x,j)){
						throw new UnsupportedOperationException();
					}
				}
			}
			if(minSuc==-1){
				throw new UnsupportedOperationException();
			}
//			System.out.println(nei);
//			System.out.println("from "+x+" to "+minSuc+" cost "+matrix[x][minSuc]);
//			System.exit(0);
			return (x+1)*n+minSuc;
//			y = g.getEnvelopGraph().getSuccessorsOf(x).getFirstElement();
//			if(y==-1){
//				throw new UnsupportedOperationException();
//			}
//			if(g.getKernelGraph().edgeExists(x,y)){
//				y = g.getEnvelopGraph().getSuccessorsOf(x).getNextElement();
//				if(y==-1 || g.getKernelGraph().edgeExists(x,y)){
//					throw new UnsupportedOperationException();
//				}
//			}
//			return (x+1)*n+y;
		}
	}

	// ReducedCostBasedSearch
	static boolean degHeur;
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
			int val;
			if(degHeur){
				val = minDomMaxRepCost();
			}else{
				val = maxDomMaxCost();
			}
			if(val == -1){
				if(g.instantiated()){
					return null;
				}throw new UnsupportedOperationException();
			}
			return new GraphDecision(g,val, Assignment.graph_remover);
		}
		public int maxDomMaxCost() {
			int n = g.getEnvelopOrder();
			INeighbors suc;
			int size = 0;
			int sizi;
			int val;
			int to = -1;
			int minCost = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>2){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if((mst.isInMST(i,j)) && (!g.getKernelGraph().arcExists(i,j))){
							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
							if (sizi == size) {
								val = dist[i][j];
								if(minCost == -1 || val>minCost){
									minCost = val;
									to = j;
									from = i;
								}
							}
							if (sizi > size) {
								size = sizi;
								val = dist[i][j];
								minCost = val;
								to = j;
								from = i;
							}
						}
					}
				}
			}
			return (from+1)*n+to;
		}
		public int minDomMaxRepCost() {
			int n = g.getEnvelopOrder();
			INeighbors suc;
			int size = 2*n + 1;
			int sizi;
			double repCost=0,repCostIJ;
			int to = -1;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = g.getEnvelopGraph().getSuccessorsOf(i);
				if(suc.neighborhoodSize()>2){
					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
						if(i<j && mst.isInMST(i,j) && !g.getKernelGraph().arcExists(i,j)){//mst.isInMST(i,j) &&
							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
							repCostIJ = mst.getRepCost(i,j);
							if (sizi == size) {
								if(repCost < repCostIJ){
									repCost = repCostIJ;
									to = j;
									from = i;
								}
							}
							if (sizi < size) {
								size = sizi;
								repCost = repCostIJ;
								to = j;
								from = i;
							}
						}
					}
				}
			}
			return (from+1)*n+to;
		}
//		public int maxDomMaxCost() {
//			int n = g.getEnvelopOrder();
//			INeighbors suc;
//			int size = 0;
//			int sizi;
//			int val;
//			int to = -1;
//			int minCost = -1;
//			int from=-1;
//			for (int i = 0; i < n; i++) {
//				suc = g.getEnvelopGraph().getSuccessorsOf(i);
//				if(suc.neighborhoodSize()>2){
//					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
//						if((mst.isInMST(i,j)) && (!g.getKernelGraph().arcExists(i,j))){
//							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
//							if (sizi == size) {
//								val = dist[i][j];
//								if(minCost == -1 || val>minCost){
//									minCost = val;
//									to = j;
//									from = i;
//								}
//							}
//							if (sizi > size) {
//								size = sizi;
//								val = dist[i][j];
//								minCost = val;
//								to = j;
//								from = i;
//							}
//						}
//					}
//				}
//			}
//			return (from+1)*n+to;
//		}
//		public int minDomMinCost() {
//			int n = g.getEnvelopOrder();
//			INeighbors suc;
//			int size = 2*n + 1;
//			int sizi;
//			int val;
//			int to = -1;
//			int minCost = -1;
//			int from=-1;
//			for (int i = 0; i < n; i++) {
//				suc = g.getEnvelopGraph().getSuccessorsOf(i);
//				if(suc.neighborhoodSize()>2){
//					for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
//						if(mst.isInMST(i,j) && !g.getKernelGraph().arcExists(i,j)){//mst.isInMST(i,j) &&
//							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getPredecessorsOf(j).neighborhoodSize();
////							sizi = suc.neighborhoodSize()+g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
//							if (sizi == size) {
//								val = dist[i][j];
//								if(minCost == -1 || val<minCost){
//									minCost = val;
//									to = j;
//									from = i;
//								}
//							}
//							if (sizi < size) {
//								size = sizi;
//								val = dist[i][j];
//								minCost = val;
//								to = j;
//								from = i;
//							}
//						}
//					}
//				}
//			}
//			return (from+1)*n+to;
//		}
	}

	private static class CompositeSearch extends AbstractStrategy {

		AbstractStrategy s1,s2;
		protected CompositeSearch(AbstractStrategy s1, AbstractStrategy s2) {
			super(ArrayUtils.append(s1.vars,s2.vars));
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