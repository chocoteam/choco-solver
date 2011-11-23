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
import org.kohsuke.args4j.Option;
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
import solver.search.strategy.strategy.graph.GraphStrategy.NodeArcPriority;
import solver.variables.CustomerVisitVariable;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

import java.io.File;
import java.io.FileWriter;

public class VRP extends AbstractProblem {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	//settable params
	@Option(name = "-f", usage = "File name.", required = true)
    private String instanceFile;
	@Option(name = "-t", usage = "Time limit.", required = false)
	public static long TIME_LIMIT = 5000;
	@Option(name = "-p", usage = "path model (=/= transitiv model).", required = false)
	private static boolean pathGraph = false; // the transitive model is much better
	
	//params
	private int nbMaxTrucks;
	private long solvingTime;
	private int[][] distancesMatrix;
	private VRPInstance instance;
	//variables and graph
	private CustomerVisitVariable[] nodes; 
	private IntVar nbNodes;
	private DirectedGraphVar g; // graph of the main constraint
	private int n; // number of nodes in the graph of the main constraint
	//results
	private long objVal;
	private IntVar nTrucks;
	

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public VRP() {
		solver = new Solver();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		this.instance    = new VRPInstance(instanceFile);
		this.nbMaxTrucks = instance.getNbCustomers();
		// number of nodes : one per customer plus two per truck (departure and arrival points)
		n = instance.getNbCustomers() + nbMaxTrucks * 2;
		// create a distance matrix between nodes
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

	/**create a graph constraint such that the corresponding graph should lead to a path partition :
	 * branch on "xi's successor in the tour is xj"
	 * @param distancesMatrix
	 * @return a graph constraint
	 */
	private GraphConstraint graphSuccs(int[][] distancesMatrix){
		GraphRelation<CustomerVisitVariable> relation = GraphRelationFactory.customerVisit(nodes, distancesMatrix);
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(nodes, relation, solver);
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

	/**create a graph constraint such that the corresponding graph should lead to the transitive closure of a path partition :
	 * branch on "xi and xj belong to the same truck tour and xi is visited before xj"
	 * @param distancesMatrix
	 * @return a graph constraint
	 */
	private GraphConstraint graphTransClos(int[][] distancesMatrix){
		GraphRelation<CustomerVisitVariable> relation = GraphRelationFactory.customerVisit(nodes, distancesMatrix);
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(nodes, relation, solver);
		gc.addProperty(GraphProperty.TRANSITIVITY);
		gc.addProperty(GraphProperty.ANTI_SYMMETRY);
		g = (DirectedGraphVar) gc.getGraph(); // stores the graph variable
		BitSetNeighbors ends = new BitSetNeighbors(n);
		for(int i=0;i<2*nbMaxTrucks; i+=2){
			ends.set(i+1);
		}
		gc.addAdHocProp(new PropEachNodeHasLoop(g, ends, solver, gc));
		return gc;
	}

	@Override
	public void configureSolver() {
		SearchMonitorFactory.log(solver, true, false);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIME_LIMIT);
		AbstractStrategy objStrat =  StrategyFactory.inputOrderMinVal(new IntVar[]{nTrucks}, solver.getEnvironment()); //AbstractStrategy objStrat =  setDichotomicSearch();
		AbstractStrategy graphStrategy = StrategyFactory.graphStrategy(g, new LexNode(g), new RandomArc(g,0), NodeArcPriority.NODES_THEN_ARCS);
		AbstractStrategy strategy = new StrategiesSequencer(solver.getEnvironment(), objStrat, graphStrategy);
		solver.set(strategy); // branch on the graph variable
	}
	
	/** 
	 * @return dichotomic strategy for the objectif variable
	 */
	private StrategyVarValAssign setDichotomicSearch(){
		Metric metric = new Median(nTrucks);
		HeuristicVal hval1 = HeuristicValFactory.enumVal(nTrucks);
		HeuristicVal hval2 = HeuristicValFactory.enumVal(nTrucks);
		nTrucks.setHeuristicVal(new SeqN(new DropN(hval1, metric, Action.open_node),new FirstN(hval2, metric, Action.open_node)));
		IntVar[] objs = new IntVar[]{nTrucks};
		return  StrategyVarValAssign.dyn(objs,SorterFactory.inputOrder(objs),ValidatorFactory.instanciated,Assignment.int_eq,solver.getEnvironment());
	}

	@Override
	public void solve() {
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, nTrucks);
		solvingTime = solver.getMeasures().getTimeCount();
		objVal = solver.getMeasures().getObjectiveValue();
	}

	@Override
	public void prettyOut() {
		if(true)return;
		String s = "\n*******************************\nDETAILS:";
		for(int i=0;i<n; i++){
			s += nodes[i]+"\n";
		}
		s += "\n*******************************\nTOUR:";
		for(int t=0; t<nbMaxTrucks; t++){
			if(g.getKernelGraph().getActiveNodes().isActive(2*t)){
				s += "\ntruck["+t+"] : ";
				for(int i=1;i<=instance.getNbCustomers();i++){
					for(int j=2*nbMaxTrucks; j<n; j++){
						if(i == g.getKernelGraph().getPredecessorsOf(j).neighborhoodSize() && nodes[j].getTruck().getValue()==t){
							s+= (j-2*nbMaxTrucks+1)+", ";
						}
					}
				}
			}
		}
		s += "\n*******************************";
		System.out.println(s);
		System.out.println(""+nTrucks);
	}

	/**
	 * transforms the input instance to match with the model
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
				distancesMatrix[i][j] = max; 
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
			distancesMatrix[i][i + 1] = 0; // allow to reach the arrival
		}
		return distancesMatrix;
	}

	//***********************************************************************************
	// BENCHMARK
	//***********************************************************************************

	public static void benchCompare(String folderURL, String outputFile) {
		if (folderURL == null){
			folderURL = "/Users/info/Documents/worktest/galak/trunk/solver/src/main/resources/files/SolomonPotvinBengio";
		}
		if (outputFile==null){
			outputFile = "VRP_results_modelCompared.csv";
		}
		clearFile(outputFile);
		File folder = new File(folderURL);
		File[] instances = folder.listFiles();
		String[] params = new String[]{"-f",""};
		long timePath, nbTrucksPath, timeTrans, nbTrucksTrans;
		String results = "timePAth;nbTPath;timeTrans;nbTTrans\n";
		writeTextInto(results, outputFile);
		for(int i=0; i<instances.length; i++){
			params[1] = instances[i].getAbsolutePath();
			//
			System.out.println(pathGraph+" : "+instances[i].getName());
			pathGraph = true;
			VRP sample = new VRP();
			sample.execute(params);
			timePath = sample.solvingTime;
			nbTrucksPath = sample.objVal;
			//
			pathGraph = false;
			System.out.println(pathGraph+" : "+instances[i].getName());
			sample = new VRP();
			sample.execute(params);
			timeTrans = sample.solvingTime;
			nbTrucksTrans = sample.objVal;
			results = timePath+";"+nbTrucksPath+";"+timeTrans+";"+nbTrucksTrans+"\n";
			writeTextInto(results, outputFile);
		}
	}
	
	public static void benchTransModel(String folderURL, String outputFile) {
		pathGraph = false;
		if (folderURL == null){
			folderURL = "/Users/info/Documents/worktest/galak/trunk/solver/src/main/resources/files/SolomonPotvinBengio";
		}
		if (outputFile==null){
			outputFile = "VRP_results_transitivModel.csv";
		}
		clearFile(outputFile);
		File folder = new File(folderURL);
		File[] instances = folder.listFiles();
		String[] params = new String[]{"-f",""};
		long timeTrans, nbTrucksTrans;
		String results = "timePAth;nbTPath;timeTrans;nbTTrans\n";
		writeTextInto(results, outputFile);
		for(int i=0; i<instances.length; i++){
			params[1] = instances[i].getAbsolutePath();
			System.out.println(instances[i].getName());
			VRP sample = new VRP();
			sample.execute(params);
			timeTrans = sample.solvingTime;
			nbTrucksTrans = sample.objVal;
			results = timeTrans+";"+nbTrucksTrans+"\n";
			writeTextInto(results, outputFile);
		}
	}
	
	//***********************************************************************************
	// MAIN
	//***********************************************************************************

	public static void main(String[] args) {
		//VRP sample = new VRP(false);
		//sample.execute(args);
		benchTransModel(null,null);
	}
	
	//***********************************************************************************
	// RECORDING RESULTS
	//***********************************************************************************

	private static void writeTextInto(String text, String file) {
		try{
			FileWriter out  = new FileWriter(file,true);
			out.write(text);
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void clearFile(String file) {
		try{
			FileWriter out  = new FileWriter(file,false);
			out.write("");
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
