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
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.directed.PropCircuitNoSubtour;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.constraints.propagators.gary.tsp.directed.PropOneSuccBut;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.exception.ContradictionException;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Parse and solve a Hamiltonian Cycle Problem instance of the TSPLIB
 * */
public class TSPNbSols extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private GraphVar graph;
	private GraphConstraint gc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TSPNbSols(int size){
		solver = new Solver();
		n = size;
	}

	//***********************************************************************************
	// MODEL
	//***********************************************************************************

	@Override
	public void buildModel() {
		buildDirected();
//		buildUndirected();
	}

	public void buildUndirected() {
		// create model
		graph = new UndirectedGraphVar(solver,n, GraphType.MATRIX,GraphType.LINKED_LIST);
		try{
			for(int i=0; i<n; i++){
				graph.getKernelGraph().activateNode(i);
				for(int j=i+1; j<n ;j++){
					graph.getEnvelopGraph().addEdge(i,j);
				}
			}
		}catch(Exception e){
			e.printStackTrace();System.exit(0);
		}
		gc = GraphConstraintFactory.makeConstraint(graph, solver);
		gc.addAdHocProp(new PropAtLeastNNeighbors((UndirectedGraphVar)graph,solver,gc,2));
		gc.addAdHocProp(new PropAtMostNNeighbors((UndirectedGraphVar)graph,solver,gc,2));
		gc.addAdHocProp(new PropCycleNoSubtour((UndirectedGraphVar)graph,gc,solver));
		//allDifAC
//		gc.addAdHocProp(new PropAllDiffGraphIncremental(graph,n-1,solver,gc));
		solver.post(gc);
	}

	public void buildDirected() {
		// create model
		graph = new DirectedGraphVar(solver,n, GraphType.MATRIX,GraphType.LINKED_LIST);
		try{
			for(int i=0; i<n; i++){
				graph.getKernelGraph().activateNode(i);
				for(int j=i+1; j<n ;j++){
					graph.getEnvelopGraph().addEdge(i,j);
				}
			}
		}catch(Exception e){
			e.printStackTrace();System.exit(0);
		}
		gc = GraphConstraintFactory.makeConstraint(graph, solver);
		gc.addAdHocProp(new PropOneSuccBut((DirectedGraphVar)graph,-1,gc,solver));
		gc.addAdHocProp(new PropOnePredBut((DirectedGraphVar)graph,-1,gc,solver));
		gc.addAdHocProp(new PropCircuitNoSubtour((DirectedGraphVar)graph,gc,solver));
		GraphPropagator gp = new GraphPropagator(new GraphVar[]{graph},solver,gc, PropagatorPriority.LINEAR) {

			@Override
			public int getPropagationConditions(int vIdx) {
				return EventType.REMOVEARC.getMask();
			}

			@Override
			public void propagate(int evtmask) throws ContradictionException {
				if(ConnectivityFinder.findCCOf(graph.getEnvelopGraph()).size()!=1){
					contradiction(graph,"");
				}
			}

			@Override
			public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
				propagate(0);
			}

			@Override
			public ESat isEntailed() {
				return ESat.UNDEFINED;
			}
		};
		gc.addAdHocProp(gp);
		//allDifAC
//		gc.addAdHocProp(new PropAllDiffGraphIncremental(graph,n-1,solver,gc));
		solver.post(gc);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
		strategy = StrategyFactory.graphRandom(graph, 0);
//		strategy = StrategyFactory.graphLexico(graph);
//		strategy = StrategyFactory.graphStrategy(graph,null,new ConstructorHeur(graph,0), GraphStrategy.NodeArcPriority.ARCS);
//		strategy = StrategyFactory.graphStrategy(graph,null,new MinDomMinVal(graph), GraphStrategy.NodeArcPriority.ARCS);
//		strategy = StrategyFactory.graphStrategy(graph,null,new RandomHeur(graph,seed), GraphStrategy.NodeArcPriority.ARCS);
		solver.set(strategy);
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
	}
	@Override
	public void solve() {
		solver.findAllSolutions();
	}
	@Override
	public void prettyOut() {}

	//***********************************************************************************
	// TESTS
	//***********************************************************************************

	public static void main(String[] args) {
		new TSPNbSols(10).execute();
	}

	//***********************************************************************************
	// BRANCHING
	//***********************************************************************************

	private class MinDomMinVal extends ArcStrategy {
		public MinDomMinVal(GraphVar graphVar) {
			super(graphVar);
		}
		@Override
		public int nextArc() {
			int nextI = -1;
			int size;
			for(int i=0;i<n;i++){
				size = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
				if(size>1){
					if(nextI==-1 || size<g.getEnvelopGraph().getSuccessorsOf(nextI).neighborhoodSize()){
						nextI = i;
					}
				}
			}
			if(nextI==-1){
				return -1;
			}
			int nextJ = g.getEnvelopGraph().getSuccessorsOf(nextI).getFirstElement();
			return (nextI+1)*n+nextJ;
		}
	}
}
