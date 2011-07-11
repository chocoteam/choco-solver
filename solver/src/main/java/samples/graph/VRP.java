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
import solver.constraints.propagators.gary.constraintSpecific.PropTruckDepArr;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.selectors.graph.arcs.RandomArc;
import solver.search.strategy.selectors.graph.nodes.LexNode;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.CustomerVisitVariable;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import java.util.Random;
import choco.kernel.ResolutionPolicy;

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

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public VRP(VRPInstance inst, int nbMaxTrucks) {
		solver = new Solver();
		this.instance    = inst;
		this.nbMaxTrucks = nbMaxTrucks;
		// number of nodes : one per customer plus two per truck (departure and arrival points)
		n = inst.getNbCustomers() + nbMaxTrucks * 2;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		// create randomly a distance matrix between nodes
		int[][] distancesMatrix = buildDistances(n, nbMaxTrucks);
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
		// relation
		GraphRelation<CustomerVisitVariable> relation = GraphRelationFactory.customerVisit(nodes, distancesMatrix);
		// graph constraint
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(nodes, relation, solver, PropagatorPriority.LINEAR);
		g = (DirectedGraphVar) gc.getGraph(); // stores the graph variable
		// tree partitioning (loop = arrival spot)
		gc.addProperty(GraphProperty.K_ANTI_ARBORESCENCES, nTrucks);
		// redundant
		gc.addProperty(GraphProperty.K_LOOPS, nTrucks);
		// enables to get paths
		gc.addProperty(GraphProperty.K_PROPER_PREDECESSORS_PER_NODE, VariableFactory.bounded("01", 0, 1, solver));
		// controls the number of visited customers
		gc.addProperty(GraphProperty.K_NODES, nbNodes);

		gc.addAdHocProp(new PropTruckDepArr(g, nTrucks, solver, gc));

		Constraint[] cstrs = new Constraint[n + 1];
		for (int i = 0; i < n; i++) { // meta constraints : when a component variable (e.g. time window variable) is modifyed the meta variable to which it belongs is notifyed
			cstrs[i] = new MetaVarConstraint(nodes[i].getComponents(), nodes[i], solver);
		}
		cstrs[n] = gc;
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		SearchMonitorFactory.log(solver, true, false);
		// branch on the graph variable
		AbstractStrategy strategy = StrategyFactory.graphStrategy(g, new LexNode(g), new RandomArc(g, 0));
		solver.set(strategy);
	}

	@Override
	public void solve() {
		solver.getSearchLoop().getLimitsFactory().setTimeLimit(15000);
		//		solver.findSolution();
		//        solver.findAllSolutions();
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, nTrucks);
	}

	@Override
	public void prettyOut() {
		//		System.out.println("env "+g.getEnvelopGraph());
		for (int i = 0; i < n; i++) {
			//			System.out.println(nodes[i]);
		}
		//		System.out.println("ker "+g.getKernelGraph());

		System.out.println(nTrucks);
	}

	/**
	 * Generate an instance input data
	 *
	 * @param n        number of nodes (nbCusto + 2*nbTrucks)
	 * @param nbTrucks number of trucks
	 * @return matrix of distance between each pair of nodes
	 */
	private int[][] buildRandomDistances(int n, int nbTrucks) {
		// trucks are at the begining :
		// startT1 / endT1 / startT2 / endT2 / custo1 / ... / custo n-2*nbTrucks
		int[][] distancesMatrix = new int[n][n];
		Random rd = new Random(0);
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				distancesMatrix[i][j] = 10 + rd.nextInt(50); // random distance
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < i; j++) {
				distancesMatrix[i][j] = distancesMatrix[j][i] - 10 + rd.nextInt(20); // Dist(x,y) should be pretty similar to Dist(y,x)
			}
			distancesMatrix[i][i] = 1000; // ordinary nodes have no loops
		}
		for (int i=0; i<2*nbTrucks; i++) {
			for (int j=0; j<2*nbTrucks; j++) {
				distancesMatrix[i][j] = 1000; // the departure node has no possible predecessor
			}
		}
		for (int i=0; i<2*nbTrucks; i+=2) {
			for (int j=2*nbTrucks; j<n; j++) {
				distancesMatrix[j][i] = 1000; // the departure node has no possible predecessor
				distancesMatrix[i + 1][j] = 1000; // the arrival node has no possible successor (but himself)
			}
		}
		for (int i = 0; i < 2 * nbTrucks; i += 2) {
			distancesMatrix[i + 1][i + 1] = 0; // (only truck arrival nodes have a loop)
		}
		String s = "";
		for(int i=0; i<n ; i++){
			s+="\n";
			for(int j=0;j<n;j++){
				s+="\t"+distancesMatrix[i][j];
			}
		}
		System.out.println(s);
		System.exit(0);
		return distancesMatrix;
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
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		DirectedGraphVar.seed = 0;
		VRPInstance instance = new VRPInstance("/Users/info/Documents/worktest/galak/trunk/solver/src/main/resources/files/SolomonPotvinBengio/rc_201.1.txt");
		VRP sample = new VRP(instance, 14);
		sample.execute();
		System.out.println("********************************\n");
	}
}
