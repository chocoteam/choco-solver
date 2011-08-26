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
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.GraphProperty;
import solver.constraints.propagators.PropagatorPriority;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.io.FileWriter;
import java.util.BitSet;

public class Tree extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 60000;
	private static String file = "results_tree.csv";
	private BitSet[] data;
	private DirectedGraphVar g;
	private IntVar nTree;
	private int n;
	private int d;
	private Boolean sat;
	static int seed = 0;
	private static GraphType gtype;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public Tree(BitSet[] input, int nbSuccsPerNodes) {
		solver = new Solver();
		data = input;
		n = data.length;
		d = nbSuccsPerNodes;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		IntVar[] vars = VariableFactory.enumeratedArray("v", n, 0, n-1, solver);
		nTree = VariableFactory.enumerated("NTREE ", 1,1, solver);
		try{
		for(int i=0; i<n; i++){
			for(int j=0; j<n ;j++){
				if(!data[i].get(j)){
					vars[i].removeValue(j, Cause.Null);
				}
			}
		}
		}catch(Exception e){}
		GraphConstraint gc = GraphConstraintFactory.nTrees(vars, nTree, solver, PropagatorPriority.LINEAR);
		gc.addProperty(GraphProperty.K_NODES, VariableFactory.bounded("n", n,n, solver));
		g = (DirectedGraphVar) gc.getGraph();
		Constraint[] cstrs = new Constraint[]{gc};
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.graphLexico(g);
		solver.set(strategy);
	}

	@Override
	public void solve() {
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, false, false);
		sat = solver.findSolution();
		writeTextInto(n+";"+d+";"+solver.getMeasures().getNodeCount()+";"+
				solver.getMeasures().getBackTrackCount()+";"+solver.getMeasures().getTimeCount()+";"+sat+";\n", file);
		if(solver.getMeasures().getFailCount()>0){
			throw new UnsupportedOperationException("error gac");
		}
//		System.out.println(g.getEnvelopGraph());
	}

	@Override
	public void prettyOut() {
		System.out.println("iniProp  : "+ solver.getMeasures().getInitialPropagationTimeCount()+"ms");
		System.out.println("duration : "+solver.getMeasures().getTimeCount()+"ms");
		System.out.println("nbnodes  : "+solver.getMeasures().getNodeCount()+" nodes ");
		System.out.println("nbSols  : "+solver.getMeasures().getSolutionCount()+" sols ");
	}

	public static boolean performOneTest(int n, int d){
		if(n<d)throw new UnsupportedOperationException("n must be greater or equal to d");
		BitSet[] data = DataGenerator.makeTreeData(n, d);
		Tree tsample = new Tree(data,d);
		tsample.execute();
		System.out.println("time : "+tsample.solver.getMeasures().getTimeCount());
		return tsample.sat!=null && tsample.solver.getMeasures().getTimeCount()<=TIMELIMIT;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		gtype = GraphType.MATRIX;
		testN();
	}
	
	private static void testN(){
		file = "tree_"+(TIMELIMIT/1000)+"sec_"+gtype+".csv";
		clearFile(file);
		writeTextInto("n;d;nodes;bks;time;solved;\n", file);
		int i = 0;
//		int[] ns = new int[]{10,50,100,150,300,450,600,800,2000,3000,4000,5000};
//		int[] ds = new int[]{5,20,50,5000};
		int[] ns = new int[]{10,50,100,300,1500};
		int[] ds = new int[]{5,20,300}; 
		int dMax = 50000;
		int nMax = 50000;
		for(int n:ns){
			if (n<nMax){
				for(int d:ds){
					if(d<dMax){
						if(d>n){
							d=n;
						}
						boolean success = false;
						for(i=0;i<1;i++){
							seed = i;
							DataGenerator.seed = i;
							System.out.println(n+" : "+d);
							if(performOneTest(n, d)){
								success = true;
							}
							for(int k=0;k<5;k++){
								System.gc();
							}
						}
						if(!success){
							dMax = d;
							if (dMax == 5){
								nMax = n;
							}
						}
						if(d==n){
							break;
						}
					}
				}
			}else{
				break;
			}
		}
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
