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

import choco.kernel.memory.IStateInt;
import samples.AbstractProblem;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraph2;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.relaxationHeldKarp.PropHeldKarp;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.io.*;

/**
 * Parse and solve a Hamiltonian Cycle Problem instance of the TSPLIB
 * */
public class HCP extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 60000;
	private static String outFile = "/Users/jfages07/Documents/code/results/results_hcp";
	static int seed = 0;
	// instance
	private String instanceName;
	private int n;
	// model
	private DirectedGraphVar graph;
	private IntVar[] integers;
	private boolean[][] adjacencyMatrix;
	private Boolean status;
	private GraphConstraint gc;
	// parameters
	private int allDiff;
	private boolean arbo,antiArbo,rg;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public HCP(String inst, boolean[][] matrix) {
		solver = new Solver();
		n = matrix.length;
		adjacencyMatrix = matrix;
		instanceName = inst;
	}

	//***********************************************************************************
	// MODEL
	//***********************************************************************************

	@Override
	public void buildModel() {
		basicModel();
		if(arbo){
			gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
		}
		if(antiArbo){
			gc.addAdHocProp(new PropAntiArborescence(graph,n-1,gc,solver,true));
		}
		if(rg){
			gc.addAdHocProp(new PropReducedGraphHamPath(graph, gc, solver));
		}
		Constraint[] cstrs;
		switch (allDiff){
			case 0:cstrs = new Constraint[]{gc};break;
			case 1:gc.addAdHocProp(new PropAllDiffGraph2(graph,solver,gc));
				cstrs = new Constraint[]{gc};break;
			case 2: cstrs = new Constraint[]{gc, integerAllDiff(false)};break;
			case 3: cstrs = new Constraint[]{gc, integerAllDiff(true)};break;
			default : throw new UnsupportedOperationException();
		}
		solver.post(cstrs);
	}

	private void basicModel(){
		// create model
		graph = new DirectedGraphVar(solver,n, GraphType.LINKED_LIST,GraphType.LINKED_LIST);
		try{
			graph.getKernelGraph().activateNode(n-1);
			for(int i=0; i<n-1; i++){
				graph.getKernelGraph().activateNode(i);
				for(int j=1; j<n ;j++){
					if(adjacencyMatrix[i][j]){
						graph.getEnvelopGraph().addArc(i,j);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();System.exit(0);
		}
		gc = GraphConstraintFactory.makeConstraint(graph, solver);
		gc.addAdHocProp(new PropOneSuccBut(graph,n-1,gc,solver));
		gc.addAdHocProp(new PropOnePredBut(graph,0,gc,solver));
		gc.addAdHocProp(new PropPathNoCycle(graph,0,n-1, gc, solver));
//		gc.addAdHocProp(new PropDegreePatterns(graph,gc,solver));
	}

	private Constraint integerAllDiff(boolean bc) {
		integers = new IntVar[n];
		try{
			int i = n-1;
			if(bc){
				integers[i] = VariableFactory.bounded("vlast", n, n, solver);
			}else{
				integers[i] = VariableFactory.enumerated("vlast", n, n, solver);
			}
			for(i=0; i<n-1; i++){
				if(bc){
					integers[i] = VariableFactory.bounded("v" + i, 1, n - 1, solver);
				}else{
					integers[i] = VariableFactory.enumerated("v" + i, 1, n - 1, solver);
				}
				for(int j=1; j<n ;j++){
					if(!adjacencyMatrix[i][j]){
						integers[i].removeValue(j, Cause.Null);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();System.exit(0);
		}
		gc.addAdHocProp(new PropIntVarChanneling(integers,graph,gc,solver));
		if(bc){
			return new AllDifferent(integers,solver,AllDifferent.Type.BC);
		}else{
			return new AllDifferent(integers,solver, AllDifferent.Type.AC);
		}
	}

	private void addHK(){
		int[][] distanceMatrix = new int[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				distanceMatrix[i][j] = 1;
			}
		}
		IntVar totalCost = VariableFactory.enumerated("cost",n-1,n-1,solver);
		PropHeldKarp propHK_mst = PropHeldKarp.mstBasedRelaxation(graph, 0,n-1, totalCost, distanceMatrix,gc,solver);
		gc.addAdHocProp(propHK_mst);
	}

	private void configParameters(int ad, boolean ab, boolean aab, boolean rg) {
		allDiff  = ad;
		arbo     = ab;
		antiArbo = aab;
		this.rg	 = rg;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void configureSearch() {
		AbstractStrategy strategy;
		strategy = StrategyFactory.graphLexico(graph);
//		strategy = StrategyFactory.inputOrderMinVal(integers,solver.getEnvironment());
//		strategy = StrategyFactory.domwdegMindom(integers,solver);
//		strategy = StrategyFactory.graphStrategy(graph,null,new BuildPath(graph), GraphStrategy.NodeArcPriority.ARCS);
		solver.set(strategy);
//		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
	}

    @Override
    public void configureEngine() {
    }

	@Override
	public void solve() {
		status = solver.findSolution();
	}

	@Override
	public void prettyOut() {
		String txt = instanceName+";"+solver.getMeasures().getSolutionCount()+";"+solver.getMeasures().getFailCount()+";"+solver.getMeasures().getTimeCount()+";"+status+";\n";
		writeTextInto(txt, outFile);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
//		outFile = "resultsHCP.csv";
//		clearFile(outFile);
//		writeTextInto("instance;nbSols;fails;time;status;\n", outFile);
//		bench();
		String instance = "/Users/jfages07/Documents/code/ALL_hcp/alb1000.hcp";
		testInstance(instance);
	}

	private static void testInstance(String instance) {
		String[] st = instance.split("/");
		String name = st[st.length-1];
		GraphGenerator gg = new GraphGenerator(10,0, GraphGenerator.InitialProperty.HamiltonianCircuit);
		boolean[][] matrix = gg.arcBasedGenerator(0.3);//= parseInstance(instance);
//		boolean[][] matrix = parseInstance(instance);
		HCP tspRun = new HCP(name,matrix);
		tspRun.configParameters(3,false, false, false);
		tspRun.execute();
	}

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
			int n = Integer.parseInt(line.split(":")[1].replaceAll(" ", ""))+1;
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
				if(i==0){
					matrix[i][j] = true;
					matrix[j][n-1] = true;
					if(j==0){
						throw new UnsupportedOperationException("no loop please");
					}
				}else{
					if(j==0){
						matrix[i][n-1] = true;
						matrix[j][i] = true;
					}else{
						matrix[i][j] = true;
						matrix[j][i] = true;
					}
				}
				line = buf.readLine();
			}
			return matrix;
		}catch(Exception e){
			e.printStackTrace();
		}
		throw new UnsupportedOperationException();
	}

	private static void bench(){
		String dir = "/Users/jfages07/Documents/code/ALL_hcp";
		File folder = new File(dir);
		String[] list = folder.list();
		for(String s:list){
			if(s.contains(".hcp"))
				testInstance(dir+"/"+s);
		}
	}

	//***********************************************************************************
	// RECORDING RESULTS
	//***********************************************************************************

	private static void writeTextInto(String text, String file) {
		try{
			FileWriter out  = new FileWriter(file,true);
			out.write(text);
			out.flush();
			out.close();
		}
		catch(IOException e){
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

	//***********************************************************************************
	// HEURISTICS
	//***********************************************************************************

	private class BuildPath extends ArcStrategy{

		IStateInt currentNode;

		public BuildPath(GraphVar graphVar) {
			super(graphVar);
			currentNode = solver.getEnvironment().makeInt(0);
		}

		@Override
		public int nextArc() {
			int node = currentNode.get();
			INeighbors succ = graph.getKernelGraph().getSuccessorsOf(node);
			while(succ.neighborhoodSize()==1){
				if(graph.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()!=1){
					throw new UnsupportedOperationException();
				}
				node = succ.getFirstElement();
				succ = graph.getKernelGraph().getSuccessorsOf(node);
			}
			currentNode.set(node);
			succ = graph.getEnvelopGraph().getSuccessorsOf(node);
			int next = succ.getFirstElement();
			if(next==-1){
				return next;
			}
			int minSize = n;
			for(int i=succ.getFirstElement();i>=0;i=succ.getNextElement()){
				if(graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<minSize){
					minSize = graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
					next = i;
				}
			}
			return (node+1)*n+next;
		}
	}
}
