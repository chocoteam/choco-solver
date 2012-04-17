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

package samples.graph.jg_sandbox;

import samples.graph.jg_sandbox.*;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.PropKCliques;
import solver.constraints.propagators.gary.PropTransitivity;
import solver.constraints.propagators.gary.undirected.PropAtLeastNNeighbors;
import solver.exception.ContradictionException;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.selectors.graph.arcs.LexArc;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.view.Views;
import java.io.*;
import java.util.BitSet;

public class HumanExperiments_OneDayOnly {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 2000;
	private final static String repo = "/Users/jfages07/Documents/instancesTLF";
	private static Solver solver;
	// parameters
	private static int minTime, maxTime;
	private static int n, ne, nt;// nbNodes, nbEmployees, nbTasks : n = ne + nt
	private static int nbSkills;
	private static boolean[][] skills;
	private static int[] task_skill, task_start, task_end;
	private static String instanceName;
	// variables
	private static UndirectedGraphVar graph;
	private static IntVar[] start,end;
	private static IntVar kWorkers;
	private static GraphConstraint gc;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		File folder = new File(repo);
		String[] list = folder.list();
		for(String file : list){
//			if(file.contains("Day0_Ta100_Ti130_SkC_i016")
//			|| file.contains("Day0_Ta100_Ti130_SkCR_i000")){
			solve(parseInstance(repo + "/" + file));
//				System.exit(0);
//			}
		}
	}

	//***********************************************************************************
	// BUILD MODEL
	//***********************************************************************************

	private static void solve(boolean[][] matrix) {
		solver = new Solver();
//		print(matrix);
		matrix = transformMatrix(matrix);
//		print(matrix);
//		System.exit(0);
		buildVariables(matrix);
		postConstraints();
		configure();
		solver.findSolution();
		if(solver.getSearchLoop().getMeasures().getSolutionCount()==0 &&
				solver.getSearchLoop().getMeasures().getTimeCount()<TIMELIMIT){
//			throw new UnsupportedOperationException();
			writeTextInto(instanceName+"\n","tl.csv");
		}
//		System.out.println(graph.getEnvelopGraph());
//		for(int i=0;i<ne;i++){
//			System.out.println(start[i]+"  :  "+end[i]);
//		}
	}

	private static void print(boolean[][] matrix) {
		String s = "";
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j]){
					s+="1;";
				}else{
					s+="0;";
				}
			}
			s+="\n";
		}
		System.out.println(s);
	}

	private static void buildVariables(boolean[][] matrix) {
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j] != matrix[j][i]){
					throw new UnsupportedOperationException();
				}
			}
		}
		// variables
		graph = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		start = VariableFactory.boundedArray("start", ne, minTime, maxTime, solver);
		end = VariableFactory.boundedArray("end", ne, minTime, maxTime, solver);
		kWorkers = VariableFactory.bounded("nbWork",0,ne,solver);
//		kWorkers = VariableFactory.bounded("nbWork",Math.min(ne,nt),ne,solver);
		for(int i=ne;i<n;i++){
			graph.getKernelGraph().activateNode(i);
		}
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				if(matrix[i][j]){
					graph.getEnvelopGraph().addEdge(i,j);
				}
			}
		}
	}

	private static void postConstraints() {
		gc = GraphConstraintFactory.makeConstraint(graph,solver);
		gc.addAdHocProp(new PropAtLeastNNeighbors(graph,solver,gc,1));
		gc.addAdHocProp(new PropAtLeastOneGuy(graph,ne,gc,solver));
		// cliques
		gc.addAdHocProp(new PropTransitivity(graph,solver, gc));
		gc.addAdHocProp(new PropKCliques(graph,solver, gc, kWorkers));
		gc.addAdHocProp(new PropKWorkers(graph,ne,kWorkers,gc,solver));
		gc.addAdHocProp(new PropAtLeastKWorkers(graph,kWorkers,ne,gc,solver));
		gc.addAdHocProp(new PropAtMostKWorkers(graph,kWorkers,ne,gc,solver));
		// time
		gc.addAdHocProp(new PropGraphTime(graph,ne,start,end,task_start,task_end,gc,solver));
		gc.addAdHocProp(new PropTimeGraph(graph,ne,start,end,task_start,task_end,gc,solver));
		for(int i=0;i<ne;i++){
			solver.post(ConstraintFactory.leq(end[i], Views.offset(start[i],600),solver));
		}
		solver.post(gc);
	}

	private static void configure() {
//		solver.set(StrategyFactory.graphLexico(graph));
//		solver.set(StrategyFactory.graphStrategy(graph, null, new Strat(graph), GraphStrategy.NodeArcPriority.ARCS));
//		solver.set(StrategyFactory.graphStrategy(graph, null, new MilleFeuillesNurse(graph), GraphStrategy.NodeArcPriority.ARCS));
		solver.set(StrategyFactory.graphStrategy(graph, null, new MilleFeuillesTask(graph), GraphStrategy.NodeArcPriority.ARCS));
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
//		solver.getSearchLoop().getLimitsBox().setFailLimit(1);
//		solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor(){
//			int count = 100;
//			public void onContradiction(ContradictionException cex) {
//				count--;
//				if(count==0){
//					count = 100;
//					solver.getSearchLoop().restart();
//				}
//			}
//		});
	}

	//***********************************************************************************
	//TRANSFORMATION DE MATRICE
	//***********************************************************************************

	private static boolean[][] transformMatrix(boolean[][] matrix) {
		return gloutonSimple(matrix);
	}

	private static boolean[][] gloutonSimple(boolean[][] matrix) {
		boolean[][] m2 = new boolean[n][n];
		int[] mapping = new int[n];
		BitSet done = new BitSet(n);
		for(int i=ne;i<n;i++){
			mapping[i] = i;
		}
		int idx = 0;
		for(int i=ne;i<n;i++){
			for(int j=0;j<ne;j++){
				if(matrix[i][j] && !done.get(j)){
					mapping[idx++] = j;
					done.set(j);
				}
			}
		}
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				m2[i][j] = m2[j][i] = matrix[mapping[i]][mapping[j]];
			}
		}
		return m2;
	}

	//***********************************************************************************
	//PARSER
	//***********************************************************************************

	public static boolean[][] parseInstance(String url) {
		System.out.println(url);
		File file = new File(url);
		instanceName = file.getName();
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String[] numbers = line.split(":");
			minTime = Integer.parseInt(numbers[1]);
			line = buf.readLine();
			numbers = line.split(":");
			maxTime = Integer.parseInt(numbers[1]);
			line = buf.readLine();
			numbers = line.split(":");
			ne = Integer.parseInt(numbers[1]);
			line = buf.readLine();
			numbers = line.split(":");
			nt = Integer.parseInt(numbers[1]);
			n = ne + nt;
			line = buf.readLine();
			numbers = line.split(":");
			nbSkills = Integer.parseInt(numbers[1]+1);
			skills = new boolean[ne][nbSkills];
			for(int e=0;e<ne;e++){
				line = buf.readLine();
				numbers = line.split(":")[1].split(",");
				for(int i=0;i<numbers.length;i++){
					skills[e][Integer.parseInt(numbers[i])] = true;
				}
			}
			task_skill = new int[nt];
			task_start = new int[nt];
			task_end = new int[nt];
			for(int t=0;t<nt;t++){
				line = buf.readLine();
				numbers = line.split(":")[1].split(",");
				task_skill[t] = Integer.parseInt(numbers[0]);
				task_start[t] = Integer.parseInt(numbers[1]);
				task_end[t] = Integer.parseInt(numbers[2]);
			}
			boolean[][] initialDomains = new boolean[n][n];
			for(int i=0;i<ne;i++){
				for(int j=ne; j<n;j++){
					if(skills[i][task_skill[j-ne]]){
						initialDomains[i][j] = initialDomains[j][i] = true;
					}
				}
			}
			for(int i=ne;i<n;i++){
				for(int j=i; j<n;j++){
					if((task_end[i-ne]<=task_start[j-ne] || task_end[j-ne]<=task_start[i-ne])
							&& Math.max(task_end[i-ne],task_end[j-ne])-Math.min(task_start[i-ne],task_start[j-ne])<600){
						initialDomains[i][j] = initialDomains[j][i] = true;
					}
				}
			}
			return initialDomains;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
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
	//BRANCHING HEURISTICS
	//***********************************************************************************

	private static class Strat extends ArcStrategy<GraphVar> {
		Strat (GraphVar g){
			super(g);
		}
		@Override
		public int nextArc() {
			INeighbors envSuc, kerSuc;
			for (int i=n-1;i>ne;i--){
				envSuc = g.getEnvelopGraph().getSuccessorsOf(i);
				kerSuc = g.getKernelGraph().getSuccessorsOf(i);
				if(envSuc.neighborhoodSize()!=kerSuc.neighborhoodSize()){
					for(int j=envSuc.getFirstElement(); j>=0; j=envSuc.getNextElement()){
						if(j<ne && !kerSuc.contain(j)){
							return (i+1)*n+j;
						}
					}
				}
			}
			return -1;
		}
	}

	private static class MilleFeuillesNurse extends ArcStrategy<GraphVar> {
		MilleFeuillesNurse (GraphVar g){
			super(g);
		}
		@Override
		public int nextArc() {
			int size = n;
			int node = -1;
			INeighbors envSuc, kerSuc;
			for(int i=0;i<ne;i++){
				envSuc = g.getEnvelopGraph().getSuccessorsOf(i);
				kerSuc = g.getKernelGraph().getSuccessorsOf(i);
				if(envSuc.neighborhoodSize()!=kerSuc.neighborhoodSize()
						&& kerSuc.neighborhoodSize()<size){
					size = kerSuc.neighborhoodSize();
					node = i;
				}
			}
			if(node == -1){
				return -1;
			}
			envSuc = g.getEnvelopGraph().getSuccessorsOf(node);
			kerSuc = g.getKernelGraph().getSuccessorsOf(node);
			for(int j=envSuc.getFirstElement(); j>=0; j=envSuc.getNextElement()){
				if(!kerSuc.contain(j)){
					return (node+1)*n+j;
				}
			}
			throw new UnsupportedOperationException();
		}
	}

	private static class MilleFeuillesTask extends ArcStrategy<GraphVar> {
		MilleFeuillesTask (GraphVar g){
			super(g);
		}
		@Override
		public int nextArc() {
			int size = n;
			int node = -1;
			INeighbors envSuc, kerSuc;
			for(int i=ne;i<n;i++){
				envSuc = g.getEnvelopGraph().getSuccessorsOf(i);
				kerSuc = g.getKernelGraph().getSuccessorsOf(i);
				if(envSuc.neighborhoodSize()!=kerSuc.neighborhoodSize()
						&& kerSuc.neighborhoodSize()<size){
					size = kerSuc.neighborhoodSize();
					node = i;
				}
			}
			if(node == -1){
				return -1;
			}
			envSuc = g.getEnvelopGraph().getSuccessorsOf(node);
			kerSuc = g.getKernelGraph().getSuccessorsOf(node);
			for(int j=envSuc.getFirstElement(); j>=0; j=envSuc.getNextElement()){
				if(j<ne && !kerSuc.contain(j)){
					return (node+1)*n+j;
				}
			}
			throw new UnsupportedOperationException();
		}
	}
}