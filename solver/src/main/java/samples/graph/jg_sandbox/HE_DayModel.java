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

import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.IntLinComb;
import solver.constraints.propagators.gary.PropKCliques;
import solver.constraints.propagators.gary.PropTransitivity;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.selectors.graph.arcs.LexArc;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.view.Views;
import java.io.*;
import java.util.BitSet;

public class HE_DayModel {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private Solver solver;
	// parameters
	public int minTime, maxTime;
	public int n, ne, nt;// nbNodes, nbEmployees, nbTasks : n = ne + nt
	public int[] LO,LB,LW;
	private int nbSkills;
	private boolean[][] skills;
	private int[] task_skill, task_start, task_end;
	private boolean[][] matrix;
	// variables
	public UndirectedGraphVar graph;
	public IntVar[] start,end;
	public BoolVar[] workToday;
	public BoolVar[] conge;
	public IntVar kWorkers;
	private GraphConstraint gc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public HE_DayModel(String url, Solver solver) {
		this.solver = solver;
		parseInstance(url);
		transformMatrix();
		buildVariables();
		postConstraints();
	}

	//***********************************************************************************
	// BUILD MODEL
	//***********************************************************************************

	private void buildVariables() {
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j] != matrix[j][i]){
					throw new UnsupportedOperationException();
				}
			}
		}
		// variables
		graph = new UndirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.MATRIX);
//		start = VariableFactory.boundedArray("start", ne, minTime, maxTime, solver);
//		end = VariableFactory.boundedArray("end", ne, minTime, maxTime, solver);
		start = VariableFactory.boundedArray("start", ne, -10000, 30000, solver);
		end = VariableFactory.boundedArray("end", ne, -10000, 30000, solver);
		workToday = VariableFactory.boolArray("work", ne, solver);
		conge = VariableFactory.boolArray("conge", ne, solver);
		kWorkers = VariableFactory.bounded("nbWork",0,ne,solver);
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

	private void postConstraints() {
		gc = GraphConstraintFactory.makeConstraint(graph,solver);
		gc.addAdHocProp(new PropAtLeastNNeighbors(graph,1,gc,solver));
		gc.addAdHocProp(new PropAtLeastOneGuy(graph,ne,gc,solver));
		// presence of nurses
		gc.addAdHocProp(new PropGraphNurse(graph,workToday,gc,solver));
		gc.addAdHocProp(new PropKWorkers(graph,ne,kWorkers,gc,solver));
		gc.addAdHocProp(new PropAtLeastKWorkers(graph,kWorkers,ne,gc,solver));
		gc.addAdHocProp(new PropAtMostKWorkers(graph,kWorkers,ne,gc,solver));
		solver.post(ConstraintFactory.sum(workToday, IntLinComb.Operator.EQ, kWorkers, 1, solver));
		// cliques
		gc.addAdHocProp(new PropTransitivity(graph,solver, gc));
		gc.addAdHocProp(new PropKCliques(graph,solver, gc, kWorkers));
		// time
		gc.addAdHocProp(new PropGraphTime(graph,ne,start,end,task_start,task_end,gc,solver));
		gc.addAdHocProp(new PropTimeGraph(graph,ne,start,end,task_start,task_end,gc,solver));
		for(int i=0;i<ne;i++){
//			solver.post(ConstraintFactory.leq(start[i], end[i],solver));
			solver.post(ConstraintFactory.leq(end[i], Views.offset(start[i],600),solver));
//			Not necessary
//			solver.post(new ReifiedConstraint(workToday[i],
//					ConstraintFactory.neq(end[i], start[i], solver),
//					ConstraintFactory.eq(end[i], start[i], solver), solver));
//
//			solver.post(new ReifiedConstraint(workToday[i],
//					ConstraintFactory.neq(end[i], start[i].getLB(), solver),
//					ConstraintFactory.eq(end[i], start[i].getLB(), solver), solver));
		}
		solver.post(gc);
	}

	//***********************************************************************************
	// TRANSFORMATION DE MATRICE
	//***********************************************************************************

	private void transformMatrix() {
		// matrix = gloutonSimple(matrix);
	}

	private boolean[][] gloutonSimple(boolean[][] matrix) {
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
	// PARSER
	//***********************************************************************************

	private void parseInstance(String url) {
		//System.out.println(url);
		File file = new File(url);
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
			nbSkills = Integer.parseInt(numbers[1] + 1);
			skills = new boolean[ne][nbSkills];
			LO = new int[ne];
			LB = new int[ne];
			LW = new int[ne];
			for(int e=0;e<ne;e++){
				line = buf.readLine();
				numbers = line.split(":")[1].split(",");
				for(int i=0;i<numbers.length;i++){
					skills[e][Integer.parseInt(numbers[i])] = true;
				}
				line = buf.readLine();//LO : nb jours travailles depuis dernier conge
				LO[e] = Integer.parseInt(line.split(": ")[1]);
				line = buf.readLine();//LB : nb minutes sŽparant la fin du dernier repos de 35h au dimanche minuit
				LB[e] = Integer.parseInt(line.split(": ")[1]);
				line = buf.readLine();//LW : nb minutes sŽparant la fin du dernier travail au dimanche minuit
				LW[e] = Integer.parseInt(line.split(": ")[1]);
				line = buf.readLine();
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
			this.matrix = initialDomains;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	//***********************************************************************************
	// UTILITIES
	//***********************************************************************************

	public void print() {
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j]){
					buf.append("1;");
				}else{
					buf.append("0;");
				}
			}
			buf.append("\n");
		}
		System.out.println(buf);
	}

	public static void main(String[] args) {
		String repo  = "/Users/jfages07/Documents/instancesTLF";
		File folder = new File(repo);
		String[] list = folder.list();
		for(String file : list){
			if (file.equals("Day5_Ta100_Ti100_SkC_i000.txt")){
				Solver solver = new Solver();
				HE_DayModel day = new HE_DayModel(repo + "/" + file,solver);
				solver.set(StrategyFactory.graphStrategy(day.graph, null, new LexArc(day.graph), GraphStrategy.NodeArcPriority.ARCS));
				solver.set(Sort.build(Primitive.arcs(day.gc)).clearOut());
				solver.getSearchLoop().getLimitsBox().setTimeLimit(2000);
				SearchMonitorFactory.log(solver, true, false);
				solver.findSolution();
			}
		}
	}

	//***********************************************************************************
	// BRANCHING
	//***********************************************************************************

	public ArcStrategy getHeuristic(){
		return new MilleFeuillesTask(graph);
	}

	private class MilleFeuillesTask extends ArcStrategy<GraphVar> {
		MilleFeuillesTask (GraphVar g){
			super(g);
		}
		@Override
		public int nextArc() {
			int size = n+1;
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