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

import samples.graph.input.HCP_Utils;
import samples.graph.output.TextWriter;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
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
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import java.io.*;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class HCP_symImpact {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 10000;
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
		TextWriter.clearFile(outFile);
		TextWriter.writeTextInto("instance;nbSols;nbFails;time;orientation;allDiffAC;\n", outFile);
		alldifferentAC = true;
		useRestarts = false;
		for(int size=10; size<500;size+=10){
			String s = "king_"+size+"x"+size;
			System.out.println(s);
			boolean[][] matrix = HCP_Utils.generateKingTourInstance(size);
			solveUndirected(matrix,s);
			solveDirected(matrix,s);
		}
	}

	// TSP LIB
	private static void tsplib_bench() {
		outFile = "HCP_TSPLIB.csv";
		TextWriter.clearFile(outFile);
		TextWriter.writeTextInto("instance;nbSols;nbFails;time;orientation;allDiffAC;\n", outFile);
		String dir = "/Users/jfages07/Documents/code/ALL_hcp";
		File folder = new File(dir);
		String[] list = folder.list();
		useRestarts = false;
		alldifferentAC = true;
		for (String s : list) {
			if (s.contains(".hcp")){
				boolean[][] matrix = HCP_Utils.parseTSPLIBInstance(dir + "/" + s);
				solveUndirected(matrix,s);
				solveDirected(matrix,s);
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
		UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST,true);
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				if(matrix[i][j]){
					undi.getEnvelopGraph().addEdge(i,j);
				}
			}
		}
		// constraints
		Constraint gc = GraphConstraintFactory.hamiltonianCycle(undi,solver);
		solver.post(gc);
		// config
		solver.set(StrategyFactory.graphStrategy(undi,null,new MinNeigh(undi), GraphStrategy.NodeArcPriority.ARCS));

        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		if(useRestarts){
			solver.getSearchLoop().plugSearchMonitor(new Restarter());
		}
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findSolution();
		checkUndirected(solver, undi);
		//output
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";undirected;"+alldifferentAC+";\n";
		TextWriter.writeTextInto(txt, outFile);
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
		boolean[][] matrix = HCP_Utils.transformCycleToPath(m);
		int n = matrix.length;
		if(n>MAX_SIZE+1){
			return;
		}
		solver = new Solver();
		// variables
		DirectedGraphVar dir = new DirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST,true);
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
		Constraint gc = GraphConstraintFactory.hamiltonianPath(dir,0,n-1,solver);
		if(alldifferentAC){
			gc.addPropagators(new PropAllDiffGraphIncremental(dir, n - 1, solver, gc));
		}
		solver.post(gc);
		// config
		solver.set(StrategyFactory.graphStrategy(dir, null, new MinNeigh(dir), GraphStrategy.NodeArcPriority.ARCS));
		IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		if(useRestarts){
			solver.getSearchLoop().plugSearchMonitor(new Restarter());
		}
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findSolution();
		checkDirected(solver, dir);
		//output
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";directed;"+alldifferentAC+";\n";
		TextWriter.writeTextInto(txt, outFile);
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

	//***********************************************************************************
	// MONITOR
	//***********************************************************************************

	private static class Restarter extends VoidSearchMonitor implements ISearchMonitor {
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

	private static class MinNeigh extends ArcStrategy {
		int n;
		
		public MinNeigh(GraphVar graphVar) {
			super(graphVar);
			n = graphVar.getEnvelopGraph().getNbNodes();
		}

		@Override
		public boolean computeNextArc() {
			ISet suc;
			int from = -1;
			int size = n + 1;
			int sizi;
			for (int i = 0; i < n; i++) {
				sizi = g.getEnvelopGraph().getSuccessorsOf(i).getSize()-g.getKernelGraph().getSuccessorsOf(i).getSize();
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
}