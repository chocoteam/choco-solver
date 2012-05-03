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

import choco.kernel.common.util.PoolManager;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import samples.AbstractProblem;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.InverseChanneling;
import solver.constraints.nary.NoSubTours;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.tsp.directed.*;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTour;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTourGraphReactor;
import solver.exception.ContradictionException;
import solver.search.measure.IMeasures;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;

import java.io.*;
import java.util.Random;

/**
 * Parse and solve a Hamiltonian Cycle Problem instance of the TSPLIB
 * */
public class HamiltonianCircuitProblem extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private DirectedGraphVar graph;
	private boolean[][] adjacencyMatrix;
	private GraphConstraint gc;
	// model parameters
	private int allDiff;
	private long seed;
	private boolean arbo,antiArbo,rg,hk;
	private static String outFile = "HCP.csv";
	private final static long TIME_LIMIT = 5000;
	private IStateInt nR; IStateInt[] sccOf; INeighbors[] outArcs; IDirectedGraph G_R;

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
//		if(arbo||antiArbo == false){
//			gc.addAdHocProp(new PropPathNoCycle(graph,0,n-1, gc, solver));
//		}
		if(arbo){
			gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
		}
		if(antiArbo){
			gc.addAdHocProp(new PropAntiArborescence(graph,n-1,gc,solver,true));
		}
		if(rg){
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			gc.addAdHocProp(RP);
			gc.addAdHocProp(new PropSCCDoorsRules(graph,gc,solver,nR,sccOf,outArcs,G_R));
		}
		if(hk){
			IntVar[] pos = VariableFactory.boundedArray("pos",n,0,n-1,solver);
			try{
				pos[0].instantiateTo(0, Cause.Null);
				pos[n-1].instantiateTo(n - 1, Cause.Null);
			}catch(Exception e){
				e.printStackTrace();System.exit(0);
			}
			gc.addAdHocProp(new PropPosInTour(pos,graph,gc,solver));
			if(rg){
				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver,nR,sccOf,outArcs,G_R));
			}else{
				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver));
			}
			solver.post(new AllDifferent(pos,solver, AllDifferent.Type.BC));
		}
		Constraint[] cstrs;
//		switch (allDiff){
//			case 0:cstrs = new Constraint[]{gc};break;
//			case 1:gc.addAdHocProp(new PropAllDiffGraph2(graph,solver,gc));
//				cstrs = new Constraint[]{gc};break;
//			case 2: cstrs = new Constraint[]{gc, integerAllDiff(false)};break;
//			case 3: cstrs = new Constraint[]{gc, integerAllDiff(true)};break;
//			default : throw new UnsupportedOperationException();
//		}
		solver.post(gc);
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
	}
	private void configParameters(int ad, boolean ab, boolean aab, boolean rg, boolean hk) {
		allDiff  = ad;
		arbo     = ab;
		antiArbo = aab;
		this.rg	 = rg;
		this.hk  = hk;
	}
	private void configParameters(int ad, int p) {
		configParameters(ad,p%2==1,(p>>1)%2==1,(p>>2)%2==1,(p>>3)%2==1);
	}
	private static void showparam(int p){
		System.out.println(p+"  ;  ");
		System.out.println(p%2==1);
		System.out.println(((p>>1)%2==1)+"  :  "+(p>>1));
		System.out.println(((p>>2)%2==1)+"  :  "+(p>>2));
		System.out.println(((p>>3)%2==1)+"  :  "+(p>>3));
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void configureSearch() {
		AbstractStrategy strategy;
		strategy = StrategyFactory.graphRandom(graph, seed);
//		strategy = StrategyFactory.graphStrategy(graph,null,new ConstructorHeur(graph,0), GraphStrategy.NodeArcPriority.ARCS);
		solver.set(strategy);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIME_LIMIT);
	}
    @Override
    public void configureEngine() {
    }

	@Override
	public void solve() {
//		solver.findAllSolutions();
		solver.findSolution();
	}
	@Override
	public void prettyOut() {}

	//***********************************************************************************
	// TESTS
	//***********************************************************************************

	public static void main(String[] args) {
		benchmark_neighbors();
//
//		String instance = "/Users/jfages07/Documents/code/ALL_hcp/alb1000.hcp";
//		testInstance(instance);

//		benchmark_density();
//		test_benchmark_neighbors();

//		/* PYTHON SCRIPTS */
//		int n = -1;
//		int nb = -1;
//		for(int i = 0;i<args.length; i++){
//			if(args[i].equals("-n")){
//				n = Integer.parseInt(args[i+1]);
//			}
//			if(args[i].equals("-nbVoisins")){
//				nb = Integer.parseInt(args[i+1]);
//			}
//		}
//		System.out.println("n="+n+" ; nb="+nb);
//		benchmark_neighbors_script(n,nb);
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

	private static void testInstance(String instance) {
		String[] st = instance.split("/");
		String name = st[st.length-1];
		boolean[][] matrix = parseInstance(instance);
		HamiltonianCircuitProblem tspRun = new HamiltonianCircuitProblem();
		tspRun.set(matrix,15);
		tspRun.configParameters(0,false, false, false,true);
		tspRun.execute();
	}

	public static void benchmark_density() {
		outFile = "benchmark_density_constructive.csv";
		clearFile(outFile);
		writeTextInto("n;d;seed;nbSols;nbNodes;nbFails;time;allDiff;p\n",outFile);
		int[] sizes = new int[]{100,200,400};
		long s;
		double[] densities = new double[]{0.1,0.3,0.5};
		boolean[][] matrix;
		for(int n:sizes){
			for(double d:densities){
				for(int ks = 0; ks<50; ks++){
					s = System.currentTimeMillis();
					System.out.println("n:"+n+" d:"+d+" s:"+s);
					GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
					matrix = gg.arcBasedGenerator(d);
					System.out.println("graph generated");
					testModels(matrix,n,d,s);
				}
			}
		}
	}

	public static void benchmark_neighbors() {
//		outFile = "benchmark_neigh_random_1005001000.csv";
		clearFile(outFile);
		writeTextInto("n;nbVoisins;seed;nbSols;nbNodes;nbFails;time;allDiff;p\n",outFile);
		int[] sizes = new int[]{100,200,400};
		long s;
		int[] nbVoisins = new int[]{3,5,10};
		boolean[][] matrix;
		for(int n:sizes){
			for(int nb:nbVoisins){
				for(int ks = 0; ks<50; ks++){
					s = System.currentTimeMillis();
//					s = 1326123459991l;
					System.out.println("n:"+n+" nbVoisins:"+nb+" s:"+s);
					GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
					matrix = gg.neighborBasedGenerator(nb);
					System.out.println("graph generated");
					testModels(matrix,n,nb,s);
//					System.exit(0);
				}
			}
		}
	}

	public static void benchmark_neighbors_script(int n, int nb) {
		outFile = "benchmark_neigh_random_script.csv";
		long s=System.currentTimeMillis();
		boolean[][] matrix;
		GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
		matrix = transformMatrix(gg.neighborBasedGenerator(nb));
		System.out.println("graph generated");
		HamiltonianCircuitProblem hcp = new HamiltonianCircuitProblem();
		hcp.set(matrix, s);
		hcp.configParameters(0, 8);
		hcp.execute();

		for(int ks=0; ks<50;ks++){
			s = System.currentTimeMillis();
			System.out.println("n:"+n+" nbVoisins:"+nb+" s:"+s);
			gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
			matrix = transformMatrix(gg.neighborBasedGenerator(nb));
			System.out.println("graph generated");
			testModels(matrix,n,nb,s);
		}
	}

	public static void test_benchmark_neighbors() {
		outFile = "tests.csv";
		clearFile(outFile);
		writeTextInto("n;nbVoisins;seed;nbSols;nbNodes;nbFails;time;allDiff;p\n",outFile);
		int[] sizes = new int[]{3,4,5,6,7,8,9,10,15,20,25,50};
		long s;
		int[] nbVoisins = new int[]{3,5,7};
		boolean[][] matrix;
		for(int n:sizes){
			for(int nb:nbVoisins){
				if(nb<n)
					for(int ks = 0; ks<50; ks++){
						s = System.currentTimeMillis();
						System.out.println("n:"+n+" nbVoisins:"+nb+" s:"+s);
						GraphGenerator gg = new GraphGenerator(n,s, GraphGenerator.InitialProperty.HamiltonianCircuit);
						matrix = gg.neighborBasedGenerator(nb);
						System.out.println("graph generated");
						testModels(matrix,n,nb,s);
					}
			}
		}
	}

	private static void testModels(boolean[][] m, int n, double d, long seed) {
		boolean[][] matrix = transformMatrix(m);
//		int[] params = new int[]{0,1};//,1,4};//,7,15};
//		for(int p:params){
		for(int p=0;p<16;p++){
//			p=4;
			HamiltonianCircuitProblem hcp = new HamiltonianCircuitProblem();
			hcp.set(matrix, seed);
			hcp.configParameters(0, p);
			hcp.execute();
			IMeasures mes = hcp.solver.getMeasures();
			if(mes.getTimeCount()<TIME_LIMIT && 1>mes.getSolutionCount()){
				writeTextInto(n+";"+d+";"+seed+";"+mes.getSolutionCount()+";"+
						mes.getNodeCount()+";"+mes.getFailCount()+";" + mes.getTimeCount() + ";" + 0 + ";" + p + ";BUG\n", outFile);
				throw new UnsupportedOperationException();
			}else{
				writeTextInto(n+";"+d+";"+seed+";"+mes.getSolutionCount()+";"+
						mes.getNodeCount()+";"+mes.getFailCount()+";" + mes.getTimeCount() + ";" + 0 + ";" + p + "\n", outFile);
			}
//			System.exit(0);
		}
		System.exit(0);
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

	private static long referencemodel(boolean[][] matrix, int n, double d, long s) {
		Solver solver = new Solver();
		IntVar[] vars = VariableFactory.enumeratedArray("",n,0,n-1,solver);
		IntVar[] prds = VariableFactory.enumeratedArray("",n,0,n-1,solver);
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
//		solver.set(StrategyFactory.random(vars,solver.getEnvironment(),s));
		solver.set(new IntRand(vars,s));
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIME_LIMIT);
//		solver.getSearchLoop().getLimitsBox().setNodeLimit(2);
//		solver.post(new AllDifferent(vars,solver, AllDifferent.Type.GLOBALNEQS));//,new NoSubTours(vars,solver));
		solver.post(new InverseChanneling(vars,prds,solver),new NoSubTours(vars,solver),new AllDifferent(vars,solver, AllDifferent.Type.NEQS),new AllDifferent(prds,solver, AllDifferent.Type.NEQS));
		Boolean status = solver.findSolution();//AllSolutions();
		IMeasures mes = solver.getMeasures();
		for(int i=0;i<n;i++){
//			System.out.println(i+" : "+vars[i]+"\t ///\t"+prds[i]);
		}
		writeTextInto(n+";"+d+";"+s+";"+mes.getSolutionCount()+";"+mes.getNodeCount()+";"+mes.getFailCount()+";"+mes.getTimeCount()+";-1;-1\n",outFile);
		return solver.getMeasures().getSolutionCount();
	}

	//***********************************************************************************
	// RESULTS
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

	private class ConstructorHeur extends ArcStrategy {
		int source;
		public ConstructorHeur(GraphVar graphVar, int s) {
			super(graphVar);
			source = s;
		}
		@Override
		public int nextArc() {
			int x = source;
			int y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
			int nb = 1;
			while(y!=-1){
				x = y;
				y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
				nb++;
			}
			y = g.getEnvelopGraph().getSuccessorsOf(x).getFirstElement();
			if(y==-1){
				if(x!=n-1 || nb!=n){
					for(int i=0;i<n;i++){
						if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()>1){
							return (i+1)*n+g.getEnvelopGraph().getSuccessorsOf(i).getFirstElement();
						}
					}
					throw new UnsupportedOperationException();
				}
				return -1;
			}
			return (x+1)*n+y;
		}
	}

	private static class IntRand extends AbstractStrategy<IntVar>{

		final PoolManager<FastDecision> decisionPool = new PoolManager<FastDecision>();
		int n;
		TIntArrayList list;
		Random rd;

		protected IntRand(IntVar[] variables, long seed) {
			super(variables);
			n = variables.length;
			list = new TIntArrayList();
			rd = new Random(seed);
		}

		@Override
		public void init() {}

		@Override
		public Decision getDecision() {
			list.clear();
			boolean c0;
			for(int i=0;i<n;i++){
				if(!vars[i].instantiated()){
					c0 = vars[i].contains(0);
					if(c0){
						for(int j=vars[i].nextValue(vars[i].getLB());j<=vars[i].getUB();j=vars[i].nextValue(j)){
							list.add((i+1)*n+j);
						}
						list.add((i+1)*n);
					}else{
						for(int j=vars[i].getLB();j<=vars[i].getUB();j=vars[i].nextValue(j)){
							list.add((i+1)*n+j);
						}
					}
				}
			}
			if(list.size()==0){
				return null;
			}
			int x = list.get(rd.nextInt(list.size()));
			int from = x/n-1;
			int to   = x%n;
			FastDecision d = decisionPool.getE();
			if (d == null) {
				d = new FastDecision(decisionPool);
			}
//			System.out.println("DECISION "+from+"->"+to);
//			for(int i=0;i<n;i++){
//				System.out.println(i+" : "+vars[i]);
//			}System.out.println("endD");
			d.set(vars[from], to, Assignment.int_eq);
			return d;
		}
	}

	//***********************************************************************************
	// TUTTE
	//***********************************************************************************

	private static boolean[][] makeTutteGraph(){
		int n = 46;
		boolean[][] m = new boolean[n][n];
		makeThird(m,0);
		makeThird(m,15);
		makeThird(m,30);
		// center
		add(45,0,m,0);
		add(45,15,m,0);
		add(45,30,m,0);
		// extremities
		add(14,12+15,m,0);
		add(14+15,12+30,m,0);
		add(14+30,12,m,0);
		return m;
	}

	private static void makeThird(boolean[][] m, int i) {
		add(0,1,m,i);
		add(0,3,m,i);
		add(1,2,m,i);
		add(2,3,m,i);
		add(1,4,m,i);
		add(2,6,m,i);
		add(3,8,m,i);
		add(4,5,m,i);
		add(5,6,m,i);
		add(6,7,m,i);
		add(7,8,m,i);
		add(5,9,m,i);
		add(7,10,m,i);
		add(8,11,m,i);
		add(9,10,m,i);
		add(10,11,m,i);
		add(4,12,m,i);
		add(9,13,m,i);
		add(11,14,m,i);
		add(12,13,m,i);
		add(13,14,m,i);
	}

	private static void add(int from, int to, boolean[][] matrix, int offset) {
		matrix[from+offset][to+offset] = true;
		matrix[to+offset][from+offset] = true;
	}
}
