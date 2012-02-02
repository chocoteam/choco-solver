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
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import samples.AbstractProblem;
import solver.Cause;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraph2;
import solver.constraints.propagators.gary.tsp.*;
import solver.constraints.propagators.gary.tsp.relaxationHeldKarp.PropHeldKarp;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Sort;
import solver.search.measure.IMeasures;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import java.io.*;
import java.util.BitSet;
import java.util.Random;

/**
 * Parse and solve a Hamiltonian Cycle Problem instance of the TSPLIB
 * */
public class HamiltonianCircuitProblem extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final int ARBO = 0;
	private static final int RG = 1;
	private static final int NB_PARAM = 2;
	private static final int ALL_MASK = 1<<NB_PARAM-1;
	// too bad to be considered
	private static final int ALLDIFF_AC = 2;
	private static final int ANTI_ARBO = 3;
	private static final int TIME = 4;
	private static final int HK = 5;

	private int n;
	private DirectedGraphVar graph;
	private boolean[][] adjacencyMatrix;
	private GraphConstraint gc;
	// model parameters
	private long seed;
	private BitSet config;
	private static String outFile;
	private final static long TIME_LIMIT = 10000;
	// reduced graph data structures
	private IStateInt nR; IStateInt[] sccOf; INeighbors[] outArcs; IDirectedGraph G_R;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public HamiltonianCircuitProblem(boolean[][] matrix, long s){
		solver = new Solver();
		seed   = s;
		n = matrix.length;
		adjacencyMatrix = matrix;
		config = new BitSet(NB_PARAM);
	}

	private void configParameters(int mask) {
		String bytes = Integer.toBinaryString(mask);
		while(bytes.length()<NB_PARAM){
			bytes = "0"+bytes;
		}
		for(int i=0;i<NB_PARAM;i++){
			config.set(i,bytes.charAt(NB_PARAM-1-i)=='1');
		}
	}

	//***********************************************************************************
	// MODEL
	//***********************************************************************************

	@Override
	public void buildModel() {
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
		gc.addAdHocProp(new PropOnePredBut(graph, 0, gc, solver));
		gc.addAdHocProp(new PropPathNoCycle(graph,0,n-1, gc, solver));
		if(config.get(ALLDIFF_AC)){
			gc.addAdHocProp(new PropAllDiffGraph2(graph,solver,gc));
		}
		if(config.get(ARBO)){
			gc.addAdHocProp(new PropArborescence(graph,0,gc,solver,true));
		}
		if(config.get(ANTI_ARBO)){
			gc.addAdHocProp(new PropAntiArborescence(graph,n-1,gc,solver,true));
		}
		if(config.get(RG)){
			PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
			nR = RP.getNSCC();
			sccOf = RP.getSCCOF();
			outArcs = RP.getOutArcs();
			G_R = RP.getReducedGraph();
			gc.addAdHocProp(RP);
			gc.addAdHocProp(new PropSCCDoorsRules(graph,gc,solver,nR,sccOf,outArcs,G_R));
		}
		if(config.get(TIME)){
			IntVar[] pos = VariableFactory.boundedArray("pos",n,0,n-1,solver);
			try{
				pos[0].instantiateTo(0, Cause.Null);
				pos[n-1].instantiateTo(n - 1, Cause.Null);
			}catch(Exception e){
				e.printStackTrace();System.exit(0);
			}
			gc.addAdHocProp(new PropPosInTour(pos,graph,gc,solver));
			if(config.get(RG)){
				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver,nR,sccOf,outArcs,G_R));
			}else{
				gc.addAdHocProp(new PropPosInTourGraphReactor(pos,graph,gc,solver));
			}
			solver.post(new AllDifferent(pos,solver, AllDifferent.Type.BC));
		}
		if(config.get(HK)){
			IntVar obj = VariableFactory.bounded("obj",0,0,solver);
			int[][] matrix = new int[n][n];
			if(config.get(RG)){
				gc.addAdHocProp(PropHeldKarp.bstBasedRelaxation(graph,0,n-1,obj,matrix,gc,solver,nR,sccOf,outArcs));
			}else{
				gc.addAdHocProp(PropHeldKarp.mstBasedRelaxation(graph,0,n-1,obj,matrix,gc,solver));
			}
		}
		solver.post(gc);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void configureSolver() {
		AbstractStrategy strategy;
//		strategy = StrategyFactory.graphRandom(graph, seed);
//		strategy = StrategyFactory.graphStrategy(graph,null,new ConstructorHeur(graph,0), GraphStrategy.NodeArcPriority.ARCS);
		strategy = StrategyFactory.graphStrategy(graph,null,new MinDomMinVal(graph), GraphStrategy.NodeArcPriority.ARCS);
//		strategy = StrategyFactory.graphStrategy(graph,null,new RandomHeur(graph,seed), GraphStrategy.NodeArcPriority.ARCS);
		solver.set(strategy);
		solver.set(Sort.build(Primitive.arcs(gc)).clearOut());
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIME_LIMIT);
	}
	@Override
	public void solve() {
		solver.findSolution();
		if(solver.getMeasures().getSolutionCount()==0 && solver.getMeasures().getTimeCount()<TIME_LIMIT){
			throw new UnsupportedOperationException();
		}
	}
	@Override
	public void prettyOut() {
//		int nbArcs = 0;
//		for(int i=0;i<n;i++){
//			nbArcs += graph.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
//		}
//		System.out.println(nbArcs+" arcs");
		System.out.println(config);
//		System.out.println(graph.getEnvelopGraph());System.exit(0);
//		if(config.get(RG)){
//			System.out.println(G_R);
//		}
//		System.exit(0);
	}

	//***********************************************************************************
	// TESTS
	//***********************************************************************************

	public static void main(String[] args) {
//		benchmark_neighbors();
//		benchmark_density();
		tsplib_bench();
//		hardInstances();
	}

	private static String getHead() {
		return "n;d;seed;nbSols;nbNodes;nbFails;time;allDiff;arbo;antiArbo;rg;pos;hk\n";
	}

	// TSP LIB
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
	private static void tsplib_bench() {
		outFile = "HCP_TSPLIB.csv";
//		clearFile(outFile);
		writeTextInto("instance;nbSols;nbNodes;nbFails;time;allDiff;arbo;antiArbo;rg;pos;\n",outFile);
		String dir = "/Users/jfages07/Documents/code/ALL_hcp";
		File folder = new File(dir);
		String[] list = folder.list();
		for (String s : list) {
			if (s.contains(".hcp"))
				testInstance(dir + "/" + s);
		}
	}
	private static void testInstance(String instance) {
		String[] st = instance.split("/");
		String name = st[st.length-1];
		boolean[][] matrix = parseInstance(instance);
		HamiltonianCircuitProblem tspRun;
//		int max = 1<<NB_PARAM;
//		for(int i=0;i<max;i++){
//		int i = 1<<3;
		tspRun = new HamiltonianCircuitProblem(matrix,0);
		tspRun.configParameters(0);
		tspRun.execute();
		IMeasures mes = tspRun.solver.getMeasures();
		String res = name+";"+mes.getSolutionCount()+";"+mes.getNodeCount()+";"+mes.getFailCount()+";" + mes.getTimeCount() + ";";
//			for(int k=0;k<NB_PARAM;k++){
//				res+=tspRun.config.get(k)+";";
//			}
		writeTextInto(res + "\n", outFile);
//		}
	}


	// GENERATOR
	// from file
	private static void hardInstances(){
		outFile = "hpp_hard.csv";
		File file = new File("/Users/jfages07/Desktop/hard_instances_av_neigh.csv");
		TLongArrayList seeds = new TLongArrayList();
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			line = buf.readLine();
			long seed;
			do{
				seed = Long.parseLong(line.split(";")[2]);
				seeds.add(seed);
				line = buf.readLine();
			}while(line!=null);
			buf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		GraphGenerator gg;
		boolean[][] matrix;
		for(int i=seeds.size()-1;i>=0;i--){
			gg = new GraphGenerator(100,seeds.get(i), GraphGenerator.InitialProperty.HamiltonianCircuit);
			matrix = gg.neighborBasedGenerator(3);
			System.out.println("graph generated");
			testModels(matrix,100,3,seeds.get(i));
		}
	}

	public static void benchmark_density() {
		outFile = "benchmark_density_MinDomMinVal.csv";
		clearFile(outFile);
		writeTextInto(getHead(),outFile);
		int[] sizes = new int[]{1000};
		long s;
		double[] densities = new double[]{0.1,0.2,0.3};
		boolean[][] matrix;
		for(int n:sizes){
			for(double d:densities){
				for(int ks = 0; ks<10; ks++){
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
		outFile = "benchmark_neigh_minDomMinVal.csv";
		clearFile(outFile);
		writeTextInto(getHead(),outFile);
		int[] sizes = new int[]{1000};
		long s;
		int[] nbVoisins = new int[]{3,5,7,10,20};
		boolean[][] matrix;
		for(int n:sizes){
			for(int nb:nbVoisins){
				for(int ks = 0; ks<30; ks++){
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
		int max = 1<<NB_PARAM;
		for(int mask=0;mask<max;mask++){
			HamiltonianCircuitProblem hcp = new HamiltonianCircuitProblem(matrix, seed);
			hcp.configParameters(mask);
			hcp.execute();
			IMeasures mes = hcp.solver.getMeasures();
			String res = n+";"+d+";"+seed+";"+mes.getSolutionCount()+";"+mes.getNodeCount()+";"+mes.getFailCount()+";" + mes.getTimeCount() + ";";
			for(int i=0;i<NB_PARAM;i++){
				res+=hcp.config.get(i)+";";
			}
			writeTextInto(res + "\n", outFile);
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

	//***********************************************************************************
	// OUTPUT
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
	// BRANCHING
	//***********************************************************************************

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

	private class RandomHeur extends ArcStrategy {
		TIntArrayList arcs;
		public RandomHeur(GraphVar graphVar, long s) {
			super(graphVar);
			arcs = new TIntArrayList(n);
			INeighbors nei;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					arcs.add((i+1)*n+j);
				}
			}
			arcs.shuffle(new Random(s));
		}
		@Override
		public int nextArc() {
			int size = arcs.size();
			int from,to;
			int index = -1;
			do{
				index ++;
				from = arcs.get(index)/n-1;
				to = arcs.get(index)%n;
			}while(index+1<size && (g.getKernelGraph().arcExists(from,to)||!g.getEnvelopGraph().arcExists(from,to)));
			if(g.getKernelGraph().arcExists(from,to)||!g.getEnvelopGraph().arcExists(from,to)){
				return -1;
			}
			return arcs.get(index);
		}
	}

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
