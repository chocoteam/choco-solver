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
import choco.kernel.common.util.PoolManager;
import choco.kernel.common.util.procedure.PairProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.Sum;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.directed.PropOneSuccBut;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.nary.sum.PropBoolSum;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.*;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.view.Views;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class HCPsymmetricKTP {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 100000;
	private static final int MAX_SIZE = 200000;
	private static String outFile;
	private static Solver solver;
	private static boolean activeBools = true;
	private static boolean activeGraphs = true;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		kingTour();
	}

	// King Tour
	private static void kingTour() {
		outFile = "KING_TOUR.csv";
		HCP_Parser.clearFile(outFile);
		HCP_Parser.writeTextInto("instance;nbSols;nbNodes;nbFails;props;time;model;\n", outFile);
		for(int size=10; size<500;size+=10){
			String s = "king_"+size+"x"+size;
			System.out.println(s);
			boolean[][] matrix = HCPsymmetric.generateKingTourInstance(size);
			if(activeGraphs)
			solveUndiGraph(matrix, s);
//			System.exit(0);
			if(activeBools)
			solveBooleans(matrix, s);
//			System.exit(0);
		}
	}

	private static void solveUndiGraph(boolean[][] matrix, String instanceName) {
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
		solver.post(gc);
		// config
		solver.set(StrategyFactory.graphStrategy(undi,null,new MinNeigh(undi), GraphStrategy.NodeArcPriority.ARCS));

        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findSolution();
		check(solver, undi);
		//output
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getNodeCount() + ";"
				+ (int)(solver.getMeasures().getFailCount()) + ";"
				+ (int)(solver.getMeasures().getPropagationsCount()+solver.getMeasures().getEventsCount()) + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";graph;\n";
		HCP_Parser.writeTextInto(txt, outFile);
		if(solver.getMeasures().getTimeCount()>=TIMELIMIT){
			activeGraphs = false;
			System.exit(0);
		}
	}

	private static void solveBooleans(boolean[][] matrix, String instanceName) {
		int n = matrix.length;
		solver = new Solver();
		// variables
		BoolVar[][] graph = new BoolVar[n][n];
		int ct = 0;
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				if(matrix[i][j]){
					ct++;
					graph[i][j] = graph[j][i] = VariableFactory.bool("b"+i+"-"+j,solver);
				}
			}
		}
		BoolVar[] decisionVars = new BoolVar[ct];
		int[] mapping = new int[ct];
		ct = 0;
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				if(matrix[i][j]){
					decisionVars[ct++] = graph[i][j];
					mapping[ct-1] = i*n+j;
				}
			}
		}
		ArrayList<BoolVar>[] gl = new ArrayList[n];
		for(int i=0;i<n;i++){
			ArrayList<BoolVar> l = new ArrayList();
			for(int j=0;j<n;j++){
				if(matrix[i][j]){
					l.add(graph[i][j]);
				}
			}
			gl[i] = l;
		}
		// constraints
		Constraint gc = new Constraint(solver);
		IntVar two = VariableFactory.bounded("2",2,2,solver);
		for(int i=0;i<n;i++){
			int k = 0;
			for(int j=0;j<n;j++){
				if(matrix[i][j])
				k++;
			}
			BoolVar[] bools = new BoolVar[k];
			k = 0;
			for(int j=0;j<n;j++){
				if(matrix[i][j])
				bools[k++] = graph[i][j];
			}
			gc.addPropagators(new PropBoolSum(bools,two,solver,gc));
		}
		gc.addPropagators(new PropBoolNoSubtour(mapping,decisionVars,graph, gc, solver));
		solver.post(gc);
		// config
		solver.set(new MinNeighBool(decisionVars,gl));
//        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
//		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		// resolution
		solver.findSolution();
		System.out.println(solver.getMeasures());
		check(solver, decisionVars);
		//output
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getNodeCount() + ";"
				+ (int)(solver.getMeasures().getFailCount()) + ";"
				+ (int)(solver.getMeasures().getEventsCount()+solver.getMeasures().getPropagationsCount()) + ";"
				+ (int)(solver.getMeasures().getTimeCount()) + ";bool;\n";
		HCP_Parser.writeTextInto(txt, outFile);
		if(solver.getMeasures().getTimeCount()>=TIMELIMIT){
			activeBools = false;
		}
	}

	private static void check(Solver solver, Variable... vars) {
		if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
			throw new UnsupportedOperationException();
		}
		if(solver.getMeasures().getSolutionCount() ==1){
			for(Variable v:vars)
			if(!v.instantiated()){
				throw new UnsupportedOperationException();
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
//			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
//				if(!g.getKernelGraph().arcExists(from,j)){
//					this.from = from;
//					this.to = j;
//					return true;
//				}
//			}
			this.from = from;
			to = 2*n;
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if(!g.getKernelGraph().arcExists(from,j)){
					if(j<to){
						to = j;
					}
				}
			}
			if(to==2*n){
				throw new UnsupportedOperationException();
			}
			return true;
		}
	}

	private static class MinNeighBool extends AbstractStrategy<BoolVar> {
		int n;
		PoolManager<FastDecision> pool;
		ArrayList<BoolVar>[] gl;

		public MinNeighBool(BoolVar[] vars, ArrayList<BoolVar>[] gl) {
			super(vars);
			n = gl.length;
			this.gl = gl;
			pool = new PoolManager<FastDecision>();
		}

		@Override
		public void init() {}

		@Override
		public Decision getDecision() {
			int from = -1;
			int size = n + 1;
			int sizi;
			for (int i = 0; i < n; i++) {
				sizi = 0;
				ArrayList<BoolVar> l = gl[i];
				for(BoolVar bv:l){
					if(bv.getUB()==1 && bv.getLB()==0){
						sizi++;
					}
				}
				if (sizi < size && sizi>0) {
					from = i;
					size = sizi;
				}
			}
			if (from == -1) {
				System.out.println("over");
				return null;
			}
			ArrayList<BoolVar> l = gl[from];
			for(BoolVar bv:l){
				if(bv.getUB()==1 && bv.getLB()==0){
					FastDecision dec = pool.getE();
					if(dec == null){
						dec = new FastDecision(pool);
					}
					dec.set(bv,1, Assignment.int_eq);
					return dec;
				}
			}
			throw new UnsupportedOperationException();
		}
	}

	private static class PropBoolNoSubtour extends Propagator<BoolVar> {

		//***********************************************************************************
		// VARIABLES
		//***********************************************************************************

		protected BoolVar[][] g;
		protected int n;
		int[] mapping;
		protected IStateInt[] origin,end,size;

		//***********************************************************************************
		// CONSTRUCTORS
		//***********************************************************************************

		/**
		 * Ensures that graph has no circuit, with Caseaux/Laburthe/Pesant algorithm
		 * runs in O(1) per instantiation event
		 * @param mapping
		 * @param graph
		 * @param constraint
		 * @param solver   */
		public PropBoolNoSubtour(int[] mapping, BoolVar[] decVars, BoolVar[][] graph, Constraint constraint, Solver solver) {
			super(decVars, solver, constraint, PropagatorPriority.UNARY,true);
			g = graph;
			this.mapping = mapping;
			this.n = graph.length;
			origin = new IStateInt[n];
			size = new IStateInt[n];
			end = new IStateInt[n];
			for(int i=0;i<n;i++){
				origin[i] = environment.makeInt(i);
				size[i] = environment.makeInt(1);
				end[i] = environment.makeInt(i);
			}
		}

		//***********************************************************************************
		// METHODS
		//***********************************************************************************

		@Override
		public void propagate(int evtmask) throws ContradictionException {
			for(int i=0;i<n;i++){
				end[i].set(i);
				origin[i].set(i);
				size[i].set(1);
			}
			int s = vars.length;
			for(int k=0;k<s;k++){
				if(vars[k].getLB()==1){
					int i = mapping[k]/n;
					int j = mapping[k]%n;
					enforce(i,j);
				}
			}
		}

		@Override
		public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
			if(vars[idxVarInProp].getLB()==1){
				int i = mapping[idxVarInProp]/n;
				int j = mapping[idxVarInProp]%n;
				enforce(i,j);
			}
		}

		@Override
		public int getPropagationConditions(int vIdx) {
			return EventType.INSTANTIATE.mask+EventType.REMOVE.mask+EventType.DECUPP.mask+EventType.INCLOW.mask ;
		}

		@Override
		public ESat isEntailed() {
			return ESat.UNDEFINED;
		}

		protected void enforce(int i, int j) throws ContradictionException {
			if(end[j].get()==end[i].get() && origin[i].get()==origin[j].get()){
				if(size[origin[i].get()].get()==n){
					return;
				}
				contradiction(g[i][j],"");
			}
			if(end[i].get() == i && origin[j].get()==j){
				enforceNormal(i,j);
				return;
			}
			if(origin[i].get() == i && end[j].get()==j){
				enforceNormal(j,i);
				return;
			}
			if(origin[i].get() == i && origin[j].get()==j){
				int newOrigin = end[i].get();
				int newEnd = origin[i].get();
				origin[i].set(newOrigin);
				origin[newOrigin].set(newOrigin);
				end[i].set(newEnd);
				end[newEnd].set(newEnd);
				size[newOrigin].set(size[newEnd].get());
				enforceNormal(i,j);
				return;
			}
			if(end[i].get() == i && end[j].get()==j){
				int newOrigin = end[j].get();
				int newEnd = origin[j].get();
				end[j].set(newEnd);
				end[newEnd].set(newEnd);
				origin[j].set(newOrigin);
				origin[newOrigin].set(newOrigin);
				size[newOrigin].set(size[newEnd].get());
				enforceNormal(i,j);
				return;
			}
			contradiction(g[i][j],"");//this cas should not happen (except if deg=2 is not propagated yet)
		}

		protected  void enforceNormal(int i, int j) throws ContradictionException {
			int last = end[j].get();
			int start = origin[i].get();
			origin[last].set(start);
			end[start].set(last);
			size[start].add(size[j].get());
			if(size[start].get()>n){
				contradiction(g[i][j],"");
			}
			if(size[start].get()<n){
				if(last==j && start == i){
					// rien faire car le chemin ne contient qu'une arrete
				}else{
					if(g[last][start]!=null)
					g[last][start].setToFalse(this,false);
				}
			}
			if(size[start].get()==n){
				if(g[last][start]==null){
					contradiction(null,"");
				}
				g[last][start].setToTrue(this,false);
			}
		}
	}

}