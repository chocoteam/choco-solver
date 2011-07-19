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
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.MetaVarConstraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.GraphProperty;
import solver.constraints.gary.relations.GraphRelation;
import solver.constraints.gary.relations.GraphRelationFactory;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.basic.PropEachNodeHasLoop;
import solver.constraints.propagators.gary.constraintSpecific.PropTruckDepArr;
import solver.constraints.propagators.gary.directed.PropNPreds;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.nary.SeqN;
import solver.search.strategy.enumerations.values.heuristics.unary.DropN;
import solver.search.strategy.enumerations.values.heuristics.unary.FirstN;
import solver.search.strategy.enumerations.values.metrics.Median;
import solver.search.strategy.enumerations.values.metrics.Metric;
import solver.search.strategy.selectors.graph.arcs.RandomArc;
import solver.search.strategy.selectors.graph.nodes.LexNode;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy.NodeArcPriority;
import solver.variables.CustomerVisitVariable;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

import java.util.Random;

public class VRP extends AbstractProblem {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private VRPInstance instance;
	private int nbMaxTrucks;
	private int n; // number of nodes
	private CustomerVisitVariable[] nodes;
	private IntVar nTrucks;
	private DirectedGraphVar g;
	private IntVar nbNodes;
	private int[][] distancesMatrix;
	private boolean pathGraph = true;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public VRP(VRPInstance inst) {
		solver = new Solver();
		this.instance    = inst;
		this.nbMaxTrucks = inst.getNbCustomers();
		// number of nodes : one per customer plus two per truck (departure and arrival points)
		n = inst.getNbCustomers() + nbMaxTrucks * 2;
		pathGraph = true; // the transitive closure method is not operational yet
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		// create randomly a distance matrix between nodes
		distancesMatrix = buildDistances(n, nbMaxTrucks);
		nodes = new CustomerVisitVariable[n];
		nbNodes = VariableFactory.bounded("nbNodes", instance.getNbCustomers()+2, n, solver);
		// trucks
		nTrucks = VariableFactory.bounded("nbTrucks", 1, nbMaxTrucks, solver);
		String s;
		for (int i=0; i<2*nbMaxTrucks; i++) {
			if (i % 2 == 0) {
				s = " departure";
			} else {
				s = " arrival";
			}
			IntVar truck = VariableFactory.bounded("num", i / 2, i / 2, solver);
			IntVar tstart = VariableFactory.bounded("time", instance.getDepotOpening(), instance.getDepotClosure(), solver);
			nodes[i] = new CustomerVisitVariable("truck " + i / 2 + s, truck, tstart, solver);
		}
		// customers
		for (int i=2*nbMaxTrucks; i<n; i++) {
			IntVar truck = VariableFactory.bounded("num", 0, nbMaxTrucks - 1, solver);
			IntVar tstart = VariableFactory.bounded("time", instance.getOpenings()[i-2*nbMaxTrucks+1], instance.getClosures()[i-2*nbMaxTrucks+1], solver);
			nodes[i] = new CustomerVisitVariable("custo " + i, truck, tstart, solver);
		}

		Constraint[] cstrs = new Constraint[n + 1];
		for (int i = 0; i < n; i++) { // meta constraints : when a component variable (e.g. time window variable) is modifyed the meta variable to which it belongs is notifyed
			cstrs[i] = new MetaVarConstraint(nodes[i].getComponents(), nodes[i], solver);
		}
		cstrs[n] = graphCons(distancesMatrix);
		solver.post(cstrs);
	}

	private GraphConstraint graphCons(int[][] distancesMatrix){
		GraphConstraint gc;
		if(pathGraph){
			gc = graphSuccs(distancesMatrix);
		}else{
			gc = graphTransClos(distancesMatrix);
		}
		gc.addProperty(GraphProperty.K_LOOPS, nTrucks);
		gc.addProperty(GraphProperty.K_NODES, nbNodes);
		g = (DirectedGraphVar) gc.getGraph(); // stores the graph variable
		gc.addAdHocProp(new PropTruckDepArr(g, nTrucks, solver, gc));// controls the number of visited customers
		return gc; 
	}

	private GraphConstraint graphSuccs(int[][] distancesMatrix){
		GraphRelation<CustomerVisitVariable> relation = GraphRelationFactory.customerVisit(nodes, distancesMatrix);
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(nodes, relation, solver, PropagatorPriority.LINEAR);
		gc.addProperty(GraphProperty.K_ANTI_ARBORESCENCES, nTrucks);
		gc.addProperty(GraphProperty.K_PROPER_PREDECESSORS_PER_NODE, VariableFactory.bounded("01", 0, 1, solver));
		g = (DirectedGraphVar) gc.getGraph(); // stores the graph variable
		BitSetNeighbors ends = new BitSetNeighbors(n);
		BitSetNeighbors custos = new BitSetNeighbors(n);
		for(int i=0;i<2*nbMaxTrucks; i+=2){
			ends.set(i+1);
		}
		for(int i=2*nbMaxTrucks; i<n; i++){
			custos.set(i);
		}
		gc.addAdHocProp(new PropNPreds(g, solver, gc, 2, ends));
		gc.addAdHocProp(new PropEachNodeHasLoop(g, ends, solver, gc));
		gc.addAdHocProp(new PropNPreds(g, solver, gc, 1, custos));
		return gc;
	}
	
	private GraphConstraint graphTransClos(int[][] distancesMatrix){
		GraphRelation<CustomerVisitVariable> relation = GraphRelationFactory.customerVisit(nodes, distancesMatrix);
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(nodes, relation, solver, PropagatorPriority.LINEAR);
		gc.addProperty(GraphProperty.TRANSITIVITY);
		gc.addProperty(GraphProperty.ANTI_SYMMETRY);
		g = (DirectedGraphVar) gc.getGraph(); // stores the graph variable
		BitSetNeighbors ends = new BitSetNeighbors(n);
		for(int i=0;i<2*nbMaxTrucks; i+=2){
			ends.set(i+1);
		}
		gc.addAdHocProp(new PropEachNodeHasLoop(g, ends, solver, gc));
		
//		gc.addAdHocProp(new GraphPropagator<DirectedGraphVar>(new DirectedGraphVar[]{g}, solver, gc, PropagatorPriority.BINARY, false) {
//
//			private IntProcedure remArc = new IntProcedure() {
//				
//				@Override
//				public void execute(int i) throws ContradictionException {
//					int from = i/n-1;
//					int to   = i%n;
//					INeighbors suc = g.getEnvelopGraph().getSuccessorsOf(from);
//					if(suc.neighborhoodSize()<1){
//						g.removeNode(from, null);
//					}else{
//						if(suc.neighborhoodSize()==1 && g.getKernelGraph().getActiveNodes().isActive(from)){
//							g.enforceArc(from, g.getEnvelopGraph().getSuccessorsOf(from).getFirstElement(), null);
//						}
//					}
//				}
//			};
//			private IntProcedure enforceNodeProc = new IntProcedure() {
//				
//				@Override
//				public void execute(int i) throws ContradictionException {
//					INeighbors suc = g.getEnvelopGraph().getSuccessorsOf(i);
//					if(suc.neighborhoodSize()<1){
//						g.removeNode(i, null);
//					}else{
//						if(suc.neighborhoodSize()==1){
//							g.enforceArc(i, g.getEnvelopGraph().getSuccessorsOf(i).getFirstElement(), null);
//						}
//					}
//				}
//			};
//
//			@Override
//			public int getPropagationConditions(int vIdx) {
//				return EventType.ENFORCENODE.mask + EventType.REMOVEARC.mask;
//			}
//
//			@Override
//			public void propagate() throws ContradictionException {
//				IActiveNodes envNodes = g.getEnvelopGraph().getActiveNodes();
//				IActiveNodes kerNodes = g.getKernelGraph().getActiveNodes();
//				for(int i=envNodes.getFirstElement(); i<=0; i=envNodes.getNextElement()){
//					if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<1){
//						g.removeNode(i, this);
//					}
//					if(kerNodes.isActive(i) && g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()==1){
//						g.enforceArc(i, g.getEnvelopGraph().getSuccessorsOf(i).getFirstElement(), this);
//					}
//				}
//			}
//
//			@Override
//			public void propagateOnRequest(IRequest request, int idxVarInProp,int mask) throws ContradictionException {
//				if (request instanceof GraphRequest) {
//					GraphRequest gv = (GraphRequest) request;
//					if ((mask & EventType.REMOVEARC.mask)!=0){
//						IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
//						d.forEach(remArc, gv.fromArcRemoval(), gv.toArcRemoval());
//					}if ((mask & EventType.ENFORCENODE.mask) != 0){
//						IntDelta d = (IntDelta) g.getDelta().getNodeEnforcingDelta();
//						d.forEach(enforceNodeProc, gv.fromNodeEnforcing(), gv.toNodeEnforcing());
//					}
//				}
//			}
//
//			@Override
//			public ESat isEntailed() {
//				return ESat.UNDEFINED;
//			}
//		});
		
		
		return gc;
	}

	@Override
	public void configureSolver() {
		SearchMonitorFactory.log(solver, true, false);
		solver.getSearchLoop().getLimitsFactory().setTimeLimit(30000);
		// approche dichotomique pour l'objectif
		/*nTrucks.setHeuristicVal(new HeuristicVal() {
			private IStateInt idx = solver.getEnvironment().makeInt(nTrucks.getLB());
			@Override
			public void remove() {
			}
			@Override
			public int next() {
				if(idx.get() == nTrucks.getUB()+1){
					idx.set((int)Math.ceil((double)((nTrucks.getLB()+nTrucks.getUB()))/2));
				}else{
					idx.set((int)Math.ceil((double)((idx.get()+nTrucks.getUB()))/2));
				}
				return idx.get();
			}
			@Override
			public boolean hasNext() {return !nTrucks.instantiated();}
			@Override
			public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {return null;}
			@Override
			protected void doUpdate(Action action) {}
		});*/

        Metric metric = new Median(nTrucks);
        HeuristicVal hval1 = HeuristicValFactory.enumVal(nTrucks);
        HeuristicVal hval2 = HeuristicValFactory.enumVal(nTrucks);
        nTrucks.setHeuristicVal(
                new SeqN(
                        new DropN(hval1, metric, Action.open_node),
                        new FirstN(hval2, metric, Action.open_node))
        );
		IntVar[] objs = new IntVar[]{nTrucks};
		AbstractStrategy objStrat =  StrategyVarValAssign.dyn(objs,
                SorterFactory.inputOrder(objs),
                ValidatorFactory.instanciated,
                Assignment.int_eq,
                solver.getEnvironment());

		AbstractStrategy graphStrategy;
		if(pathGraph){
			graphStrategy = StrategyFactory.graphStrategy(g, new LexNode(g), new HomogeniousTour(g,false), NodeArcPriority.ARCS);
		}else{
			objStrat = StrategyFactory.inputOrderMaxVal(objs, solver.getEnvironment());
			graphStrategy = StrategyFactory.graphLexico(g);
			graphStrategy = StrategyFactory.graphStrategy(g, new LexNode(g), new RandomArc(g,0), NodeArcPriority.NODES_THEN_ARCS);
		}
		AbstractStrategy strategy = new StrategiesSequencer(solver.getEnvironment(), objStrat, graphStrategy);
		solver.set(strategy); // branch on the graph variable
	}

	@Override
	public void solve() {
//				solver.findSolution();
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, nTrucks);
	}

	@Override
	public void prettyOut() {
		//	System.out.println("env "+g.getEnvelopGraph());
		for(int i=0;i<n; i++){
			//System.out.println(nodes[i]);
		}
		//	System.out.println("ker "+g.getKernelGraph());
		System.out.println("\n"+nTrucks);
	}

	/**
	 * Generate an instance input data
	 *
	 * @param n        number of nodes (nbCusto + 2*nbTrucks)
	 * @param nbTrucks number of trucks
	 * @return matrix of distance between each pair of nodes
	 */
	private int[][] buildDistances(int n, int nbTrucks) {
		// trucks are at the begining :
		// startT1 / endT1 / startT2 / endT2 / custo1 / ... / custo n-2*nbTrucks
		int[][] distancesMatrix = new int[n][n];
		int nt = 2*nbTrucks;
		int max = instance.getDepotClosure()+1;
		for (int i = nt; i < n; i++) {
			for (int j = nt; j < n; j++) {
				distancesMatrix[i][j] = instance.getDistMatrix()[i-nt+1][j-nt+1]; // random distance
			}
		}
		for (int i = 0; i<nt; i++) {
			for (int j = nt; j < n; j++) {
				distancesMatrix[i][j] = instance.getDistMatrix()[0][j-nt+1]; // random distance
				distancesMatrix[j][i] = instance.getDistMatrix()[j-nt+1][0]; // random distance
			}
		}
		for (int i=0; i<2*nbTrucks; i++) {
			for (int j=0; j<2*nbTrucks; j++) {
				distancesMatrix[i][j] = max; // the departure node has no possible predecessor
			}
		}
		for (int i=0; i<2*nbTrucks; i+=2) {
			for (int j=2*nbTrucks; j<n; j++) {
				distancesMatrix[j][i] = max; // the departure node has no possible predecessor
				distancesMatrix[i + 1][j] = max; // the arrival node has no possible successor (but himself)
			}
		}
		for (int i = 0; i < 2 * nbTrucks; i += 2) {
			distancesMatrix[i + 1][i + 1] = 0; // (only truck arrival nodes have a loop)
		}

		//		String s = "";
		//		for(int i=0; i<n ; i++){
		//			s+="\n";
		//			for(int j=0;j<n;j++){
		//				s+="\t"+distancesMatrix[i][j];
		//			}
		//		}
		//		System.out.println(s);
		//		System.exit(0);
		return distancesMatrix;
	}


	//***********************************************************************************
	// HEURISTIC
	//***********************************************************************************

	private class HomogeniousTour extends ArcStrategy<DirectedGraphVar>{

		Random rd;

		public HomogeniousTour(DirectedGraphVar g, boolean random) {
			super(g);
			if(random){
				rd = new Random(0);
			}
		}

		@Override
		public int nextArc() {
			int lowestVal = -1;
			int[] dec;
			int index = -1;
			for(int i=envNodes.getFirstElement(); i>=0 && i<2*nbMaxTrucks; i=envNodes.getNextElement()){
				if(i%2==0){
					dec = calcVal(i,i,0);
					if(dec!=null && (lowestVal==-1 || dec[0]<lowestVal)){
						index = dec[1];
						lowestVal = dec[0];
					}
				}
			}
			if(index==-1){
				if(!g.instantiated()){
					throw new UnsupportedOperationException("error ");
				}
				return -1;
			}else{
				INeighbors envSuc = g.getEnvelopGraph().getSuccessorsOf(index);
				INeighbors kerSuc = g.getKernelGraph().getSuccessorsOf(index);
				int delta = envSuc.neighborhoodSize() - g.getKernelGraph().getSuccessorsOf(index).neighborhoodSize();
				if(rd!=null){
					delta = rd.nextInt(delta);
					for(int i=envSuc.getFirstElement(); i>=0; i=envSuc.getNextElement()){
						if(!kerSuc.contain(i)){
							if(delta==0){
								return (index+1)*n+i;
							}else{
								delta--;
							}
						}
					}
				}else{
					int minVal = -1;
					int minIndex = 0;
					int valTmp;
					for(int i=envSuc.getFirstElement(); i>=0; i=envSuc.getNextElement()){
						if(i<2*nbMaxTrucks){
							minIndex = i;
						}else if(!kerSuc.contain(i)){
							valTmp = distancesMatrix[index][i];
							if(minVal == -1 || valTmp<minVal){
								minVal = valTmp;
								minIndex = i;
							}
						}
					}
					return (index+1)*n+minIndex;
				}


			}
			throw new UnsupportedOperationException("error ");
		}

		private int[] calcVal(int start, int node, int nbNodes) {
			INeighbors suc = g.getKernelGraph().getSuccessorsOf(node);
			if(g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()==0){
				throw new UnsupportedOperationException("error ");
			}
			if(suc.neighborhoodSize()==0){
				return new int[]{nbNodes, node};
			}
			int next = suc.getFirstElement();
			if(next==start+1){
				return null;
			}else{
				return calcVal(start, next, nbNodes+1);
			}
		}
//		private int[] calcVal(int start, int node, int nbNodes) {
//			INeighbors suc = g.getKernelGraph().getSuccessorsOf(node);
//			if(g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()==0){
//				System.out.println(start+" : "+node+" :" +nbNodes);
//				throw new UnsupportedOperationException("error ");
//			}
//			if(suc.neighborhoodSize()==0){
//				return new int[]{nbNodes, node};
//			}
//			int next = suc.getFirstElement();
//			if(suc.contain(start+1)){
//				if(suc.neighborhoodSize()==1){
//					return null;
//				}else{
//					next = suc.getNextElement();
//				}
//			}
//			if(next==start+1){
//				return null;
//			}else{
//				return calcVal(start, next, nbNodes+1);
//			}
//		}
	}

	//***********************************************************************************
	// MAIN
	//***********************************************************************************

	public static void main(String[] args) {
		DirectedGraphVar.seed = 0;
		VRPInstance instance = new VRPInstance("/Users/info/Documents/worktest/galak/trunk/solver/src/main/resources/files/SolomonPotvinBengio/rc_201.1.txt");
		VRP sample = new VRP(instance);
		sample.execute();
		System.out.println("********************************\n");
	}
}
