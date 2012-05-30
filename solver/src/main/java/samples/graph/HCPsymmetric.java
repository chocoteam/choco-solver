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

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.directed.PropOneSuccBut;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.exception.ContradictionException;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
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
public class HCPsymmetric {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 5000;
	private static final int MAX_SIZE = 200000;
	private static String outFile;
	private static Solver solver;
	private static boolean alldifferentAC;
	private static boolean useRestarts;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
//		tsplib_bench();
		kingTour();
	}

	// King Tour
	private static void kingTour() {
		outFile = "KING_TOUR.csv";
		clearFile(outFile);
		writeTextInto("instance;nbSols;nbFails;time;orientation;allDiffAC;\n",outFile);
//		int size = 50;
//		int[] sizes = {10,20,50,100,200,500};
//		for(int size:sizes){
		for(int size=90; size<500;size+=2){
			String s = "king_"+size;
			System.out.println(s);
			boolean[][] matrix = generateKingTourInstance(size);
			alldifferentAC = false;
			solveUndirected(matrix,s);
//			System.exit(0);
//			solveDirected(matrix,s);
//			alldifferentAC = true;
//			solveUndirected(matrix,s);
//			solveDirected(matrix,s);
		}
	}

	private static boolean[][] generateKingTourInstance(int size){
		int n = size*size;
		int node,next,a,b;
		boolean[][] matrix = new boolean[n][n];
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				node = i*size+j;
				// move
				a = i+1;
				b = j+2;
				next = a*size+(b);
				if(next>=0 && next<n){
					matrix[node][next] = inChessboard(a,b,size);
					matrix[next][node] = matrix[node][next];
				}
				// move
				a = i+1;
				b = j-2;
				next = a*size+(b);
				if(next>=0 && next<n){
					matrix[node][next] = inChessboard(a,b,size);
					matrix[next][node] = matrix[node][next];
				}
				// move
				a = i+2;
				b = j+1;
				next = a*size+(b);
				if(next>=0 && next<n){
					matrix[node][next] = inChessboard(a,b,size);
					matrix[next][node] = matrix[node][next];
				}
				// move
				a = i+2;
				b = j-1;
				next = a*size+(b);
				if(next>=0 && next<n){
					matrix[node][next] = inChessboard(a,b,size);
					matrix[next][node] = matrix[node][next];
				}
			}
		}
		return matrix;
	}

	private static boolean inChessboard(int a, int b, int n) {
		if(a<0 || a>=n || b<0 || b>=n){
			return false;
		}
		return true;
	}

	// TSP LIB
	private static boolean[][] parseInstance(String url){
		File file = new File(url);
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String name = line.split(":")[1].replaceAll(" ", "");
			System.out.println("parsing instance "+name+"...");
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			int n = Integer.parseInt(line.split(":")[1].replaceAll(" ", ""));
			boolean[][] matrix = new boolean[n][n];
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			String[] lineNumbers;
			int i,j;
			while(!line.equals("-1")){
				line = line.replaceAll(" +",";");
				lineNumbers = line.split(";");
				i = Integer.parseInt(lineNumbers[1])-1;
				j = Integer.parseInt(lineNumbers[2])-1;
				matrix[i][j] = true;
				matrix[j][i] = true;
				line = buf.readLine();
			}
			return matrix;
		}catch(Exception e){
			e.printStackTrace();
		}
		throw new UnsupportedOperationException();
	}

	private static void tsplib_bench() {
		outFile = "HCP_TSPLIB.csv";
		clearFile(outFile);
		writeTextInto("instance;nbSols;nbFails;time;orientation;allDiffAC;\n",outFile);
		String dir = "/Users/jfages07/Documents/code/ALL_hcp";
		File folder = new File(dir);
		String[] list = folder.list();
		useRestarts = true;
		for (String s : list) {
			if (s.contains(".hcp")){
				boolean[][] matrix = parseInstance(dir + "/" + s);
				alldifferentAC = false;
				solveUndirected(matrix,s);
//				alldifferentAC = true;
//				solveDirected(matrix,s);
			}
		}
	}

	private static void solveUndirected(boolean[][] matrix, String instanceName) {
		int n = matrix.length;
		if(n>MAX_SIZE){
			return;
		}
		solver = new Solver();
		// variables
		UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		for(int i=0;i<n;i++){
			undi.getKernelGraph().activateNode(i);
			for(int j=i+1;j<n;j++){
				if(matrix[i][j]){
					undi.getEnvelopGraph().addEdge(i,j);
				}
			}
		}
		// constraints
		Constraint gc = GraphConstraintFactory.makeConstraint(solver);
		gc.addPropagators(new PropCycleNoSubtour(undi, gc, solver));
		gc.addPropagators(new PropAtLeastNNeighbors(undi, 2, gc, solver));
		gc.addPropagators(new PropAtMostNNeighbors(undi, 2, gc, solver));
		if(alldifferentAC){
			gc.addPropagators(new PropAllDiffGraphIncremental(undi, n, solver, gc));
		}
		solver.post(gc);
		// config
		solver.set(StrategyFactory.graphStrategy(undi,null,new MinNeigh(undi), GraphStrategy.NodeArcPriority.ARCS));
		solver.set(new Sort(new PArc(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		if(useRestarts){
			solver.getSearchLoop().plugSearchMonitor(new MyMon());
		}
		SearchMonitorFactory.log(solver, true, false);
		// resolution
//		solver.findAllSolutions();
		solver.findSolution();
		checkUndirected(solver, undi);
		//output
//		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
//				+ (int)(solver.getMeasures().getTimeCount()) + ";undirected;"+alldifferentAC+";\n";
//		writeTextInto(txt, outFile);
	}

	private static void checkUndirected(Solver solver, UndirectedGraphVar undi) {
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			throw new UnsupportedOperationException();
		}
		if(solver.getMeasures().getSolutionCount() ==1){
			if(!undi.instantiated()){
				throw new UnsupportedOperationException();
			}
		}
	}

	private static void solveDirected(boolean[][] m, String instanceName) {
		boolean[][] matrix = transformMatrix(m);
		int n = matrix.length;
		if(n>MAX_SIZE+1){
			return;
		}
		solver = new Solver();
		// variables
		DirectedGraphVar dir = new DirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		dir.getKernelGraph().activateNode(n-1);
		for(int i=0;i<n-1;i++){
			dir.getKernelGraph().activateNode(i);
			for(int j=1;j<n;j++){
				if(matrix[i][j]){
					dir.getEnvelopGraph().addArc(i,j);
				}
			}
		}
		// constraints
		Constraint gc = GraphConstraintFactory.makeConstraint(solver);
		gc.addPropagators(new PropOneSuccBut(dir, n - 1, gc, solver));
		gc.addPropagators(new PropOnePredBut(dir, 0, gc, solver));
		gc.addPropagators(new PropPathNoCycle(dir, 0, n - 1, gc, solver));
		if(alldifferentAC){
			gc.addPropagators(new PropAllDiffGraphIncremental(dir, n - 1, solver, gc));
		}
		solver.post(gc);
		// config
		solver.set(StrategyFactory.graphStrategy(dir, null, new MinNeigh(dir), GraphStrategy.NodeArcPriority.ARCS));
		solver.set(new Sort(new PArc(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		if(useRestarts){
			solver.getSearchLoop().plugSearchMonitor(new MyMon());
		}
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findSolution();
//		solver.findAllSolutions();
		checkDirected(solver, dir);
		//output
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";directed;"+alldifferentAC+";\n";
		writeTextInto(txt, outFile);
	}

	private static void checkDirected(Solver solver, DirectedGraphVar dir) {
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			throw new UnsupportedOperationException();
		}
		if (solver.getMeasures().getSolutionCount() == 1){
			if(!dir.instantiated()){
				throw new UnsupportedOperationException();
			}
		}
	}

	private static boolean[][] transformMatrix(boolean[][] m) {
		int n=m.length+1;
		boolean[][] matrix = new boolean[n][n];
		for(int i=0;i<n-1;i++){
			for(int j=1;j<n-1;j++){
				matrix[i][j] = m[i][j];
			}
			matrix[i][n-1] = m[i][0];
			matrix[i][0] = false;
		}
		matrix[0][n-1] = false;
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

	private static class MyMon extends VoidSearchMonitor implements ISearchMonitor {
		int nbFails = 0;
		public void onContradiction(ContradictionException cex){
			nbFails++;
			if(nbFails==100){
				nbFails=0;
				solver.getSearchLoop().restart();
			}
		}
	}

	//***********************************************************************************
	// HEURISTICS
	//***********************************************************************************

	private static class MaxRegretMinCost extends ArcStrategy {
		int[][] dist;
		int offSet;
		int n;

		public MaxRegretMinCost(GraphVar graphVar, int[][] costMatrix) {
			super(graphVar);
			n = graphVar.getEnvelopGraph().getNbNodes();
			dist = costMatrix;
			if(graphVar.isDirected()){
				offSet = 1;
			}else{
				offSet = 0;
			}
		}

		@Override
		public boolean computeNextArc() {
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
				return false;
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
			this.from = from;
			this.to = to;
			return true;
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

	private static class MinNeigh extends ArcStrategy {
		int n;
		
		public MinNeigh(GraphVar graphVar) {
			super(graphVar);
			n = graphVar.getEnvelopGraph().getNbNodes();
		}

		@Override
		public boolean computeNextArc() {
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
				System.out.println("over");
				return false;
			}
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					this.from = from;
					this.to = j;
					return true;
				}
			}
			throw new UnsupportedOperationException();
		}
	}

	private static class ConstructorHeur extends ArcStrategy {
		BitSet seen;
		int n;
		int[][] matrix;
		public ConstructorHeur(GraphVar graphVar, int[][] m) {
			super(graphVar);
			n = graphVar.getEnvelopGraph().getNbNodes();
			seen = new BitSet(n);
			matrix = m;
		}
		@Override
		public boolean computeNextArc() {
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
				return false;
			}
			int minSuc = -1;
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(x);
			if(nei.neighborhoodSize()-g.getKernelGraph().getSuccessorsOf(x).neighborhoodSize()<=0){
				return false;
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
			this.from = x;
			this.to   = minSuc;
			return true;
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
}