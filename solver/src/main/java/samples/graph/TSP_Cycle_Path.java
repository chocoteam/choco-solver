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

import choco.kernel.ESat;
import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.PoolManager;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.tsp.undirected.*;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.exception.ContradictionException;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;
import java.util.ArrayList;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class TSP_Cycle_Path {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 100000;
	private static final int MAX_SIZE = 40;
	private static long seed = 0;
	private static String outFile;
	private static int upperBound = Integer.MAX_VALUE/4;
	private static IntVar totalCost;
	private static Solver solver;
	private static UndirectedGraphVar graph;
	private static int[][] dist;
	private static boolean allDiffAC    = false;
	private static boolean optProofOnly = true;
	private static PropSymmetricHeldKarp propHK;
	private static int search;
	private static boolean restart;
	private static GraphConstraint gc;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		clearFile(outFile = "tsp.csv");
		writeTextInto("instance;sols;fails;time;obj;model;\n", outFile);
		bench();
	}

	private static void bench() {
		String dir = "/Users/jfages07/github/In4Ga/ALL_tsp";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix, pmatrix;
		optProofOnly = true;
//		fast = true;
		restart = true;
		search = 0;
		allDiffAC = false;
		for (String s : list) {
			if (s.contains("22.tsp") && (!s.contains("gz"))){
				matrix = parseInstance(dir + "/" + s);
				if(matrix!=null){
					setUB(s,matrix);
					solveByCycle(matrix, s);
					pmatrix = transformMatrix(matrix);
					solveByPath(pmatrix,s);
					int nl = l1.size();
//					if(l2.size()!=nl){
//						throw new UnsupportedOperationException();
//					}
					for(int i=0;i<nl;i++){
						if(!l1.get(i).equals(l2.get(i))){
							System.out.println(l1.get(i)+" /= "+l2.get(i));
							throw new UnsupportedOperationException("dec "+i);
						}
					}
					l1 = null;
					l2 = null;
					System.out.println("OK");
				}else{
					System.out.println("CANNOT LOAD");
				}
			}
		}
	}

	//***********************************************************************************
	// LOADING INSTANCES
	//***********************************************************************************

	private static int[][] parseInstance(String url) {
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
			throw new UnsupportedOperationException();
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

	private static void setUB(String s, int[][] matrix){
		if(optProofOnly){
			File file = new File("/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv");
			try {
				BufferedReader buf = new BufferedReader(new FileReader(file));
				String line = buf.readLine();
				while(!line.contains(s.split("\\.")[0])){
					line = buf.readLine();
				}
				upperBound = Integer.parseInt(line.split(";")[1]);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			System.out.println("initial UB set to optimum : "+upperBound);
		}else{
			int n = matrix.length;
			int maxArcCost = 0;
			for(int i=0;i<n;i++){
				for(int j=0;j<n;j++){
					if(matrix[i][j]>maxArcCost){
						maxArcCost = matrix[i][j];
					}
				}
			}
			upperBound = maxArcCost*n;
			System.out.println("default initial UB : "+upperBound);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	private static void solveByCycle(int[][] matrix, String instanceName) {
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
		final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.MATRIX);
		graph = undi;
		dist = matrix;
		for(int i=0;i<n;i++){
			undi.getKernelGraph().activateNode(i);
			for(int j=i+1;j<n;j++){
				undi.getEnvelopGraph().addEdge(i,j);
			}
		}
		// constraints
		gc = GraphConstraintFactory.makeConstraint(undi,solver);
		gc.addAdHocProp(new PropCycleNoSubtour(undi,gc,solver));
		gc.addAdHocProp(new PropAtLeastNNeighbors(undi,2,solver,gc));
		gc.addAdHocProp(new PropAtMostNNeighbors(undi,2,solver,gc));
		gc.addAdHocProp(new PropCycleEvalObj(undi,totalCost,matrix,gc,solver));
		propHK = PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
//		gc.addAdHocProp(propHK);
		solver.post(gc);
		configAndSolve(matrix,instanceName,"cycle");
	}

	private static void solveByPath(int[][] matrix, String instanceName) {
		// variables
		int n = matrix.length;
		solver = new Solver();
		totalCost = VariableFactory.bounded("obj",0,upperBound,solver);
		final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.MATRIX);
		graph = undi;
		dist = matrix;
		int[] nbNeigh = new int[n];
		for(int i=0;i<n;i++){
			nbNeigh[i] = 2;
			undi.getKernelGraph().activateNode(i);
			for(int j=i+1;j<n;j++){
				undi.getEnvelopGraph().addEdge(i,j);
			}
		}
		nbNeigh[0] = nbNeigh[n-1] = 1;
		undi.getEnvelopGraph().removeEdge(0,n-1);
		// constraints
		gc = GraphConstraintFactory.makeConstraint(undi,solver);

//		gc.addAdHocProp(new MyGraphPropagator(new UndirectedGraphVar[]{undi},solver,gc,PropagatorPriority.UNARY));

//		gc.addAdHocProp(new GraphPropagator(new Variable[]{undi},solver,gc, PropagatorPriority.LINEAR) {
//
//			@Override
//			public int getPropagationConditions(int vIdx) {
//				return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
//			}
//
//			@Override
//			public void propagate(int evtmask) throws ContradictionException {
//				int n = undi.getEnvelopGraph().getNbNodes();
//				if(ConnectivityFinder.findCCOf(undi.getEnvelopGraph()).size()!=1){
//					contradiction(undi,"");
//				}
//				for(int i=1;i<n-1;i++){
//					if(undi.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize()<2
//					|| undi.getKernelGraph().getNeighborsOf(i).neighborhoodSize()>2){
//						contradiction(undi,"");
//					}
//				}
//				if(undi.getEnvelopGraph().getNeighborsOf(0).neighborhoodSize()<1
//				|| undi.getKernelGraph().getNeighborsOf(0).neighborhoodSize()>1){
//					contradiction(undi,"");
//				}
//				if(undi.getEnvelopGraph().getNeighborsOf(n-1).neighborhoodSize()<1
//				|| undi.getKernelGraph().getNeighborsOf(n-1).neighborhoodSize()>1){
//					contradiction(undi,"");
//				}
//			}
//
//			@Override
//			public void propagate(AbstractFineEventRecorder	eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
//				propagate(0);
//			}
//
//			@Override
//			public ESat isEntailed() {
//				return ESat.UNDEFINED;
//			}
//		});
		gc.addAdHocProp(new GraphPropagator(new Variable[]{undi},solver,gc, PropagatorPriority.LINEAR) {
			@Override
			public int getPropagationConditions(int vIdx) {
				return EventType.REMOVEARC.mask;
			}
			@Override
			public void propagate(int evtmask) throws ContradictionException {
				int n = undi.getEnvelopGraph().getNbNodes();
				INeighbors nei;
				for(int i=1;i<n-1;i++){
					nei=undi.getEnvelopGraph().getSuccessorsOf(i);
					if(nei.neighborhoodSize()==3 &&nei.contain(0)&&nei.contain(n-1)){
						int j = nei.getFirstElement();
						while(j==0 || j==n-1){
							j = nei.getNextElement();
						}
						undi.enforceArc(i,j, Cause.Null);
						undi.enforceArc(i,0, Cause.Null);
					}
				}
				nei=undi.getEnvelopGraph().getSuccessorsOf(n-1);
				if(nei.neighborhoodSize()==2 && undi.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize()==2){
					undi.enforceArc(0,nei.getFirstElement(),Cause.Null);
					undi.enforceArc(n-1,nei.getNextElement(),Cause.Null);
				}
			}

			@Override
			public void propagate(AbstractFineEventRecorder	eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
				propagate(0);
			}

			@Override
			public ESat isEntailed() {
				return ESat.UNDEFINED;
			}
		});

		gc.addAdHocProp(new PropChainNoSubtour2(undi,gc,solver));
		gc.addAdHocProp(new PropAtLeastNNeighbors(undi,nbNeigh,solver,gc));
		gc.addAdHocProp(new PropAtMostNNeighbors(undi,nbNeigh,solver,gc));
		gc.addAdHocProp(new PropChainEvalObj(undi,totalCost,matrix,0,n-1,gc,solver));

//		gc.addAdHocProp(propHK);
		solver.post(gc);
		configAndSolve(matrix,instanceName,"path");
	}

	private static void configAndSolve(int[][] matrix, String instanceName,String cycleOrPath) {
		// search config
		switch (search){
			case 0: solver.set(new TSPSearch(graph,cycleOrPath.equals("cycle")));break;
			case 1: solver.set(new CompositeSearch(new BottomUp(totalCost),new TSPSearch(graph,cycleOrPath.equals("cycle"))));break;
			case 2: solver.set(new CompositeSearch(new DichotomicSearch(totalCost),new TSPSearch(graph,cycleOrPath.equals("cycle"))));break;
			default: throw new UnsupportedOperationException();
		}
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		solver.getSearchLoop().getLimitsBox().setNodeLimit(378726);
		if(restart){
			solver.getSearchLoop().restartAfterEachSolution(true);
		}
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		check(solver, graph, totalCost, matrix);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost + ";"+cycleOrPath+";\n";
		writeTextInto(txt, outFile);


		int n = graph.getEnvelopOrder();
		if(cycleOrPath.equals("cycle")){
			int nb = 0;
			for(int i=1;i<n;i++){
				nb+=graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			nb-=graph.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize();
			System.out.println((nb/2)+" edges");
		}else{
			int nb = 0;
			for(int i=1;i<n-1;i++){
				nb+=graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			nb-=graph.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize();
			nb-=graph.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize();
			System.out.println((nb/2)+" edges");
			System.out.println("size(0)=size(n) : "+
					(graph.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize()==graph.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize()));
		}
	}

	private static int[][] transformMatrix(int[][] m) {
		int n=m.length+1;
		int[][] matrix = new int[n][n];
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-1;j++){
				matrix[i][j] = m[i][j];
			}
			matrix[i][n-1] = matrix[n-1][i] = m[0][i];
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

	static ArrayList<String> l1,l2;
	private static class TSPSearch extends AbstractStrategy<GraphVar> {
		UndirectedGraphVar g;
		UndirectedGraph mst;
		int[] nsize;
		boolean oneTree;
		int n;
		int count;
		protected TSPSearch(UndirectedGraphVar g, boolean oneTree) {
			super(new GraphVar[]{g});
			n = g.getEnvelopGraph().getNbNodes();
			this.g = g;
			this.oneTree = oneTree;
			nsize = new int[n];
			if(l1==null){
				l1 = new ArrayList<String>();
			}else{
				l2 = new ArrayList<String>();
			}
			count = 0;
		}
		@Override
		public void init() {}
		@Override
		public Decision getDecision() {
			for(int i=0;i<n;i++){
				nsize[i] = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			if(!oneTree){
				int k = 0;
				for(int i=1;i<n-1;i++){
					if(g.getEnvelopGraph().edgeExists(0,i) || g.getEnvelopGraph().edgeExists(n-1,i)){
						k++;
						if(g.getEnvelopGraph().edgeExists(0,i) && g.getEnvelopGraph().edgeExists(n-1,i)){
							nsize[i]--;
						}
					}
				}
				nsize[n-1] = nsize[0] = k;
			}
			mst = propHK.getMST();
			if(mst==null){
				mst=g.getEnvelopGraph();
			}
			int dec = minDomMinCost();
			if(dec==-1){
				return null;
			}
			Assignment<GraphVar> ass = Assignment.graph_enforcer;
			if(!oneTree){
				ass = graph_enforcer_forPath;
			}
			return new GraphDecision(g,dec, ass);
		}
		public int minDomMinCost() {
			int n = g.getEnvelopOrder();
			INeighbors suc;
			int size = 2*n + 1;
			int sizi;
			int val;
			int to = -1;
			int minCost = Integer.MAX_VALUE;
			int from=-1;
			for (int i = 0; i < n; i++) {
				suc = mst.getSuccessorsOf(i);
				for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
					if(i<j && !g.getKernelGraph().arcExists(i,j)){
						sizi = nsize[i]+nsize[j];
//						System.out.println(i+","+j+" : "+sizi+" : "+nsize[i]+","+nsize[j]);
						if (sizi < size) {
							size = sizi;
							val = dist[i][j];
							minCost = val;
							to = j;
							from = i;
						}
						if (sizi == size) {
							size = sizi;
							val = dist[i][j];
							if(val<minCost || (val==minCost && j==n-1 && from!=0 && from!=n-1 && !oneTree)){
//								if(count==437){
//									System.out.println(i+" // "+j);
//								}
								minCost = val;
								to = j;
								from = i;
								if(to==n-1 && !oneTree){
									from = j;
									to = i;
								}
							}
						}
					}
				}
			}
//			System.out.println("dec "+from+" - "+to);
			count++;
			if(count==378726){
				System.out.println(totalCost);
				System.out.println(graph.getEnvelopGraph());
				System.out.println("dec "+from+" - "+to+" : "+size+" : "+dist[from][to]+"$");
				System.exit(0);
//				if(l2!=null)System.exit(0);
			}
			if(l2==null){
				l1.add(from+"-"+to);
			}else{
				if(from==n-1){
					l2.add(0+"-"+to);
				}else if(to==n-1){
					l2.add(0+"-"+from);
				}else{
					l2.add(from+"-"+to);
				}
			}
			return (from+1)*n+to;
		}
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

	//***********************************************************************************
	// CHECKER
	//***********************************************************************************

	private static void check(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
//		System.out.println(totalCost);
//			System.out.println(graph.getKernelGraph());

//		int n = matrix.length;
//		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
//			throw new UnsupportedOperationException();
//		}
//		if(solver.getMeasures().getSolutionCount() > 1 && optProofOnly){
//			throw new UnsupportedOperationException();
//		}
//		if(solver.getMeasures().getSolutionCount() > 0){
//			int sum = 0;
//			for(int i=0;i<n;i++){
//				for(int j=i+1;j<n;j++){
//					if(undi.getEnvelopGraph().edgeExists(i,j)){
//						sum+=matrix[i][j];
//					}
//				}
//			}
//			if(sum!=solver.getSearchLoop().getObjectivemanager().getBestValue()){
////				writeTextInto("BUG\n",outFile);
//				throw new UnsupportedOperationException();
//			}
//		}
	}

	//		gc.addAdHocProp(new GraphPropagator(new Variable[]{undi},solver,gc, PropagatorPriority.LINEAR) {
//
//			@Override
//			public int getPropagationConditions(int vIdx) {
//				return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
//			}
//
//			@Override
//			public void propagate(int evtmask) throws ContradictionException {
//				int n = undi.getEnvelopGraph().getNbNodes();
//				if(ConnectivityFinder.findCCOf(undi.getEnvelopGraph()).size()!=1){
//					contradiction(undi,"");
//				}
//				for(int i=1;i<n-1;i++){
//					if(undi.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize()<2
//					|| undi.getKernelGraph().getNeighborsOf(i).neighborhoodSize()>2){
//						contradiction(undi,"");
//					}
//				}
//				if(undi.getEnvelopGraph().getNeighborsOf(0).neighborhoodSize()<1
//				|| undi.getKernelGraph().getNeighborsOf(0).neighborhoodSize()>1){
//					contradiction(undi,"");
//				}
//				if(undi.getEnvelopGraph().getNeighborsOf(n-1).neighborhoodSize()<1
//				|| undi.getKernelGraph().getNeighborsOf(n-1).neighborhoodSize()>1){
//					contradiction(undi,"");
//				}
//			}
//
//			@Override
//			public void propagate(AbstractFineEventRecorder	eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
//				propagate(0);
//			}
//
//			@Override
//			public ESat isEntailed() {
//				return ESat.UNDEFINED;
//			}
//		});

	private static class MyGraphPropagator extends GraphPropagator<UndirectedGraphVar>{

		IntProcedure proc;
		UndirectedGraphVar g;
		protected MyGraphPropagator(UndirectedGraphVar[] vars, Solver solver, Constraint<UndirectedGraphVar,
				Propagator<UndirectedGraphVar>> undirectedGraphVarPropagatorConstraint, PropagatorPriority priority) {
			super(vars, solver, undirectedGraphVarPropagatorConstraint, priority);
			g=vars[0];
			final int n = g.getEnvelopOrder();
			final GraphPropagator inst = this;
			proc = new IntProcedure() {
				@Override
				public void execute(int i) throws ContradictionException {
					int from = i/n-1;
					int to = i%n;
					if(to<from){
						int k = from;
						from=to;
						to=k;
					}
					if(from==0 && (!g.getKernelGraph().edgeExists(n-1,to)) &&
							(g.getEnvelopGraph().getSuccessorsOf(to).neighborhoodSize()>1 || g.getEnvelopGraph().getSuccessorsOf(to).getFirstElement()!=n-1)){
						g.removeArc(n-1,to,inst);
					}
					if(to==n-1 && (!g.getKernelGraph().edgeExists(0,from))&&
							(g.getEnvelopGraph().getSuccessorsOf(from).neighborhoodSize()>1 || g.getEnvelopGraph().getSuccessorsOf(from).getFirstElement()!=0)){
						g.removeArc(0,from,inst);
					}
				}
			};
		}

		@Override
		public int getPropagationConditions(int vIdx) {
			return EventType.REMOVEARC.mask;
		}

		@Override
		public void propagate(int evtmask) throws ContradictionException {
			if(g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize()!=
					g.getEnvelopGraph().getSuccessorsOf(g.getEnvelopOrder()-1).neighborhoodSize()){
				throw new UnsupportedOperationException("to implement");
			}
		}

		@Override
		public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
			eventRecorder.getDeltaMonitor(this,g).forEach(proc,EventType.REMOVEARC);
		}

		@Override
		public ESat isEntailed() {
			return ESat.UNDEFINED;
		}
	}

	public static Assignment<GraphVar> graph_enforcer_forPath = new Assignment<GraphVar>() {

		@Override
		public void apply(GraphVar var, int value, ICause cause) throws ContradictionException {
			int n = var.getEnvelopGraph().getNbNodes();
			if(value<n){
				throw new UnsupportedOperationException();
			}
			int from = value/n-1;
			int to   = value%n;
			var.enforceArc(from, to, cause);
		}

		@Override
		public void unapply(GraphVar var, int value, ICause cause) throws ContradictionException {
			int n = var.getEnvelopGraph().getNbNodes();
			if (value>=n){
				int from = value/n-1;
				int to   = value%n;
				if(to<from){
					int k = from;
					from = to;
					to = k;
				}
				var.removeArc(from, to, cause);
				if(from==0){
					var.removeArc(n-1, to, cause);
				}
				if(to==n-1){
					var.removeArc(0, from, cause);
				}
			}
		}

		@Override
		public String toString() {
			return " enforcing ";
		}
	};
}