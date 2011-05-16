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
import solver.constraints.gary.NTree;
import solver.constraints.propagators.PropagatorPriority;
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
		g = VariableFactory.digraph("G",data, gtype, GraphType.SPARSE,solver);
		nTree = VariableFactory.enumerated("NTREE ", 1,n, solver);
		Constraint[] cstrs = new Constraint[]{new NTree(g,nTree, solver, PropagatorPriority.LINEAR)};
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.randomArcs(g);
		solver.set(strategy);
	}

	@Override
	public void solve() {
		solver.getSearchLoop().getLimitsFactory().setTimeLimit(TIMELIMIT);
		sat = solver.findSolution();
		writeTextInto(n+";"+d+";"+solver.getMeasures().getNodeCount()+";"+
				solver.getMeasures().getBackTrackCount()+";"+solver.getMeasures().getTimeCount()+";"+NTree.filteringCounter+";"+sat+";\n", file);
		if(solver.getMeasures().getBackTrackCount()>0){
			throw new UnsupportedOperationException("error bksCount>0");
		}
	}

	@Override
	public void prettyOut() {}

	public static boolean performOneTest(int n, int d){
		if(n<d)throw new UnsupportedOperationException("n must be greater or equal to d");
		BitSet[] data = DataGenerator.makeTreeData(n, d);
		Tree tsample = new Tree(data,d);
		tsample.execute();
		return tsample.sat!=null && tsample.solver.getMeasures().getTimeCount()<=TIMELIMIT;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		gtype = GraphType.SPARSE;
		testN();
	}
	
	private static void testN(){
		file = "tree_"+(TIMELIMIT/1000)+"sec_d5_"+gtype+".csv";
		clearFile(file);
		writeTextInto("n;d;nodes;bks;time;counter;solved;\n", file);
		int i = 0;
		int[] ds = new int[]{10};
//		int[] ds = new int[]{5,20,50,30000};
		int dMax = 50000;
		int nMax = 50000;
		for(int n=900;n<=10000;n+=100){
			if (n<nMax){
				for(int d:ds){
					if(d<dMax){
						if(d>n){
							d=n;
						}
						boolean success = false;
						for(i=0;i<1;i++){
							seed = i;
							DirectedGraphVar.seed = i;
							DataGenerator.seed = i;
							System.out.println(n+" : "+d);
							if(performOneTest(n, d)){
								success = true;
							}
							for(int k=0;k<100;k++){
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
