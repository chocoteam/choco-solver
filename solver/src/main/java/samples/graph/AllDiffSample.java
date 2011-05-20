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

import java.io.FileWriter;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.AllDifferent.Type;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class AllDiffSample extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int n;
	int sizeFirstSet;
	UndirectedGraphVar g;
	private IntVar[] vars;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public AllDiffSample(int nbVars, int nbVals){
		super();
		n = nbVars+nbVals;
		sizeFirstSet = n-nbVals;
		readArgs("-quiet");
		System.out.println(nbVars+" : "+nbVals);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		solver = new Solver();
		vars = VariableFactory.boundedArray("vars", sizeFirstSet, 0, n-sizeFirstSet-1, solver);
//		vars = VariableFactory.enumeratedArray("vars", sizeFirstSet, 0, n-sizeFirstSet-1, solver);
		Constraint[] cstrs = new Constraint[]{new AllDifferent(vars, solver, Type.GRAPH)};
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment());
//		AbstractStrategy strategy = StrategyFactory.random(vars, solver.getEnvironment());
		solver.set(strategy);
	}

	@Override
	public void solve() {
		solver.findAllSolutions();
		if(solver.getMeasures().getSolutionCount()!=getNbSols(sizeFirstSet, n-sizeFirstSet)){
			throw new UnsupportedOperationException("error "+solver.getMeasures().getSolutionCount()+"!="+getNbSols(sizeFirstSet, n-sizeFirstSet));
		}
		if(solver.getMeasures().getFailCount()>0){
			throw new UnsupportedOperationException("error "+solver.getMeasures().getFailCount()+" fails");
		}
	}

	private static long getNbSols(int n1, int n2) {
		if(n1==1)return n2;
		return getNbSols(n1-1, n2-1)*n2;
	}

	@Override
	public void prettyOut() {}

	//***********************************************************************************
	// BENCH
	//***********************************************************************************

	public static int testVarVal(int nbVars, int nbVals) {
		int nbIter = 10;
		AllDiffSample ads = new AllDiffSample(nbVars, nbVals);
		ads.execute();
		int[] results = new int[nbIter];
		for(int i = 0; i<nbIter; i++){
			ads = new AllDiffSample(nbVars, nbVals);
			ads.execute();
			results[i] = (int) ads.solver.getMeasures().getTimeCount();
		}
		int minV = results[0];
		int maxV = -1;
		int minI = 0;
		int maxI = 0;
		for(int i = 0; i<nbIter; i++){
			if(results[i]>maxV){
				maxI = i;
				maxV = results[i];
			}
			if(results[i]<minV){
				minI = i;
				minV = results[i];
			}
		}
		int mean = 0;
		for(int i = 0; i<nbIter; i++){
			if(i!=minI && i!=maxI){
				mean += results[i];
			}
		}
		mean = mean/(nbIter-2);
		return mean;
	}

	public static void bench() {
		int val;
//		String fileName = "graphAllDiff_VARS.csv";
//		try{
//			FileWriter out  = new FileWriter(fileName,false);
//			out.write("nbVars;nbVals;time;nbSols\n");
//			out.close();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
		for (int i=4; i<8; i++){
			val = testVarVal(i, i);
//			try{
//				FileWriter out  = new FileWriter(fileName,true);
//				out.write(i+";"+i+";"+val+";"+getNbSols(i, i)+"\n");
//				out.close();
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
		}
//		fileName = "graphAllDiff_VALS.csv";
//		try{
//			FileWriter out  = new FileWriter(fileName,false);
//			out.write("nbVars;nbVals;time;nbSols\n");
//			out.close();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
		int nbVars = 5;
		for (int i=nbVars; i<20; i++){
			val = testVarVal(nbVars, i);
//			try{
//				FileWriter out  = new FileWriter(fileName,true);
//				out.write(nbVars+";"+i+";"+val+";"+getNbSols(i, i)+"\n");
//				out.close();
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
		}
	}
	
	public static void main(String[] args) {
		bench();
//		System.out.println("mean time : "+testVarVal(6,8));
//		System.out.println(getNbSols(6, 12));
	}
}
