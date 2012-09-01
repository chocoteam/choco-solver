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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.directed.PropOneSuccBut;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.directed.PropSumArcCosts;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSurroSymmetricHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.TSP_heuristics;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class TSP_heur {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 300000;
	private static final int MAX_SIZE = 60000;
	private static String outFile;
	private static int upperBound = Integer.MAX_VALUE/4;
	private static IntVar totalCost;
	private static Solver solver;
	private static boolean allDiffAC    = false;
	private static boolean optProofOnly = true;
	private static PropSymmetricHeldKarp mst;
	private static int search;
	private static TSP_heuristics heuristic;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
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

	static int policy;

	public static void bench() {
		clearFile(outFile = "tsp.csv");
		writeTextInto("instance;sols;fails;nodes;time;obj;allDiffAC;search;\n", outFile);
//		String dir = "/Users/jfages07/github/In4Ga/benchRousseau";
		String dir = "/Users/jfages07/github/In4Ga/ALL_tsp";
//		String dir = "/Users/jfages07/github/In4Ga/mediumTSP";
		File folder = new File(dir);
		String[] list = folder.list();
		int[][] matrix;
		optProofOnly = true;
		allDiffAC = false;
		search = 0;
		policy = 4;
		heuristic = TSP_heuristics.enf_node_arc_tests;
//		boolean pursue = true;//false;
//		int[] pol = new int[]{4};
		ATSP_ISMP.resetFile();
		for (String s : list) {
//			if (s.contains("lin318")){
//				pursue = true;
//			}
//			if(pursue)
//			if(s.contains("pr299"))
			if(s.contains("pr299.tsp"))
				if (s.contains(".tsp") && (!s.contains("gz")) && (!s.contains("lin"))){
					matrix = parseInstance(dir + "/" + s);
					if((matrix!=null && matrix.length>=0 && matrix.length<4000)){
						if(optProofOnly){
							setUB(s.split("\\.")[0]);
							System.out.println("optimum : "+upperBound);
						}
//					for(int i:pol){
//						policy = i;
						solveUndirected(matrix, s);
//						allDiffAC = true;
//						solveDirected(matrix,s);
//					policy = 1;
//					upperBound = 1000000;
//						solveATSP(matrix,s);
//					}
//					degHeur = false;
//					solveUndirected(matrix, s);
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
		final int n = matrix.length;
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
		gc.addPropagators(new PropCycleEvalObj(undi, totalCost, matrix, gc, solver));
		if(allDiffAC){
			gc.addPropagators(new PropAllDiffGraphIncremental(undi, n, solver, gc));
		}
//		mst = PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
//		gc.addPropagators(mst);
//		gc.addPropagators(PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver));

		gc.addPropagators(PropSurroSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver));
		solver.post(gc);
		// config
//		solver.set(StrategyFactory.graphRandom(undi,seed));
//		solver.set(new RCSearch(undi));
//		solver.set(new StrategiesSequencer(solver.getEnvironment(),new BottomUp(totalCost),new RCSearch(undi)));

		if(search!=0){
			throw new UnsupportedOperationException("not implemented");
		}
//		solver.set(StrategyFactory.graphLexico(undi));
//		solver.set(StrategyFactory.graphTSP(undi, heuristic, mst));

		solver.set(StrategyFactory.graphStrategy(undi, null,new GraphStrategyBench(undi,matrix,mst,policy,true), GraphStrategy.NodeArcPriority.ARCS));

		solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor(){
			public void afterInitialPropagation(){
				System.out.println("cost after prop ini : "+totalCost);
				int e = 0;
				int k = 0;
				for(int i=0;i<n;i++){
					e+=undi.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize();
					k+=undi.getKernelGraph().getNeighborsOf(i).neighborhoodSize();
				}
				e/=2;
				k/=2;
				System.out.println(k+"/"+e);
			}
		});
		IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		checkUndirected(solver, undi, totalCost, matrix);
		//output
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ solver.getMeasures().getNodeCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost + ";"+allDiffAC+";"+search+";\n";
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

	public static void solveATSP(int[][] m, String s) {
		int[][] matrix = transformMatrix(m);
//		ATSP_ISMP.bst=false;
//		ATSP_ISMP.khun=false;
//		for(int cp:ATSP_ISMP.configs){
//			ATSP_ISMP.reset(matrix,s,policy,cp,upperBound);
//			ATSP_ISMP.solve();
//		}
//		ATSP_ISMP.khun=true;
//		for(int cp:ATSP_ISMP.configs){
//			ATSP_ISMP.reset(matrix,s,policy,cp,upperBound);
//			ATSP_ISMP.solve();
//		}
//		ATSP_ISMP.khun=false;
//		ATSP_ISMP.bst=true;
//		for(int cp:ATSP_ISMP.bstconfigs){
//			ATSP_ISMP.reset(matrix,s,policy,cp,upperBound);
//			ATSP_ISMP.solve();
//		}
		ATSP_ISMP.bst=false;
		ATSP_ISMP.reset(matrix,s,policy,(1<<ATSP_ISMP.pos)+(1<<ATSP_ISMP.allDiff),upperBound);
		ATSP_ISMP.solve();
		ATSP_ISMP.bst = true;
		ATSP_ISMP.reset(matrix,s,policy,(1<<ATSP_ISMP.rg)+(1<<ATSP_ISMP.pos)+(1<<ATSP_ISMP.allDiff),upperBound);
		ATSP_ISMP.solve();
	}
	private static void solveDirected(int[][] m, String instanceName) {
		int[][] matrix = transformMatrix(m);
		int n = matrix.length;
		solver = new Solver();
		// variables
		totalCost = VariableFactory.bounded("obj",0,upperBound,solver);
		DirectedGraphVar dir = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST);
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
		Constraint gc = GraphConstraintFactory.makeConstraint(solver);
		gc.addPropagators(new PropOneSuccBut(dir, n - 1, gc, solver));
		gc.addPropagators(new PropOnePredBut(dir, 0, gc, solver));
		gc.addPropagators(new PropPathNoCycle(dir, 0, n - 1, gc, solver));
		gc.addPropagators(new PropSumArcCosts(dir, totalCost, matrix, gc, solver));
		if(allDiffAC){
			gc.addPropagators(new PropAllDiffGraphIncremental(dir, n - 1, solver, gc));
		}
		gc.addPropagators(PropHeldKarp.mstBasedRelaxation(dir, 0, n - 1, totalCost, matrix, gc, solver));
		solver.post(gc);
		// config
//		solver.set(StrategyFactory.graphATSP(dir, ATSP_heuristics.enf_sparse, mst));
		solver.set(StrategyFactory.graphStrategy(dir, null,new GraphStrategyBench(dir,matrix,mst,policy,true), GraphStrategy.NodeArcPriority.ARCS));
		IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
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

}