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

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import samples.AbstractProblem;
import samples.graph.GraphGenerator;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.NoSubTours;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraph2;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.relaxationHeldKarp.PropHeldKarp;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Parse and solve a Hamiltonian Cycle Problem instance of the TSPLIB
 * */
public class HamiltonianCircuitProblem extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static GraphType gt;

	private int n;
	private DirectedGraphVar graph;
	private IntVar[] integers;
	private boolean[][] adjacencyMatrix;
	private GraphConstraint gc;
	// model parameters
	private int allDiff;
	private long seed;
	private boolean arbo,antiArbo,rg;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public HamiltonianCircuitProblem() {
		solver = new Solver();
	}
	private void set(boolean[][] matrix, long s){
		seed   = s;
		n = matrix.length;
		adjacencyMatrix = matrix;
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
		graph = new DirectedGraphVar(solver,n, gt, GraphType.LINKED_LIST);
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
			return new AllDifferent(integers,solver,AllDifferent.Type.CLIQUE);
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
	private void configParameters(int ad, int p) {
		configParameters(ad,p%2==1,(p>>1)%2==1,(p>>2)%2==1);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
		strategy = StrategyFactory.graphRandom(graph, seed);
		solver.set(strategy);
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

	@Test(groups = "1m")
	public static void smalltest() {
		int[] sizes = new int[]{5,6,7,10,15,20};
		int[] seeds = new int[]{0,10,42};
		double[] densities = new double[]{0.1,0.2};
		boolean[][] matrix;
		for(int n:sizes){
			for(double d:densities){
				for(int s:seeds){
					System.out.println("n:"+n+" d:"+d+" s:"+s);
					GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
					matrix = gg.arcBasedGenerator(d);
					System.out.println("graph generated");
					testModels(matrix,s);
				}
			}
		}
	}

	@Test(groups = "1s")
	public static void testBug() {
		int[] sizes = new int[]{15};
		int[] seeds = new int[]{42};
		double[] densities = new double[]{0.2};
		boolean[][] matrix;
		for(int n:sizes){
			for(double d:densities){
				for(int s:seeds){
					System.out.println("n:"+n+" d:"+d+" s:"+s);
					GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
					matrix = gg.arcBasedGenerator(d);
					System.out.println("graph generated");
					testModels(matrix,s);
				}
			}
		}
	}

	@Test(groups = "1h")
	public static void smalltest2() {
		int[] sizes = new int[]{10};
		double[] densities = new double[]{0.1,0.2,0.5,0.8,1};
		boolean[][] matrix;
		for(int n:sizes){
			for(double d:densities){
				for(int ks=0;ks<50;ks++){
					long s = System.currentTimeMillis();
					System.out.println("n:"+n+" d:"+d+" s:"+s);
					GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
					matrix = gg.arcBasedGenerator(d);
					System.out.println("graph generated");
					testModels(matrix,s);
				}
			}
		}
	}

	@Test(groups = "1s")
	public static void lorcaTest() {
		int n = 8;
		boolean[][] matrix = new boolean[n][n];
		matrix[0][6] = true;
		matrix[1][0] = true;matrix[1][7] = true;
		matrix[2][3] = true;matrix[2][5] = true;matrix[2][7] = true;
		matrix[3][4] = true;matrix[3][5] = true;matrix[3][6] = true;
		matrix[4][3] = true;matrix[4][5] = true;matrix[4][6] = true;matrix[4][7] = true;
		matrix[5][0] = true;matrix[5][4] = true;
		matrix[6][1] = true;matrix[6][4] = true;
		matrix[7][0] = true;matrix[7][1] = true;matrix[7][2] = true;matrix[7][3] = true;matrix[7][5] = true;
		long nbSols = referencemodel(matrix);
		System.out.println(nbSols);
	}

	private static void testModels(boolean[][] m, long seed) {
		long nbSols = referencemodel(m);
		if(nbSols==-1){
			throw new UnsupportedOperationException();
		}
		System.out.println(nbSols + " sols expected");
		boolean[][] matrix = transformMatrix(m);
		for(int i=0;i<4;i++){
			for(int p=0;p<8;p++){
				for(GraphType type : GraphType.ENVELOPE_TYPES){
					gt = type;
					HamiltonianCircuitProblem hcp = new HamiltonianCircuitProblem();
					hcp.set(matrix,seed);
					hcp.configParameters(i,p);
					hcp.execute();
					Assert.assertEquals(nbSols, hcp.solver.getMeasures().getSolutionCount(), "nb sol incorrect "+i+" ; "+p+" ; "+gt);
				}
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
		}
		return matrix;
	}

	private static long referencemodel(boolean[][] matrix) {
		int n = matrix.length;
		Solver solver = new Solver();
		IntVar[] vars = VariableFactory.enumeratedArray("",n,0,n-1,solver);
		try {
			for(int i=0;i<n;i++){
				for(int j=0;j<n;j++){
					if(!matrix[i][j]){
						vars[i].removeValue(j,Cause.Null);
					}
				}
			}
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		solver.post(new AllDifferent(vars,solver, AllDifferent.Type.CLIQUE),new NoSubTours(vars,solver));
		Boolean status = solver.findAllSolutions();
		if(status==null){
			return -1;
		}
		return solver.getMeasures().getSolutionCount();
	}

}
