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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.tools.ArrayUtils;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.MetaVarConstraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.GraphProperty;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.PropKCC;
import solver.constraints.propagators.gary.PropKCliques;
import solver.constraints.propagators.gary.PropRelation;
import solver.constraints.propagators.gary.PropTransitivity;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.MetaVariable;
import solver.variables.VariableFactory;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class NVector extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private int k;
	private IntVar[] vars;
	private MetaVariable<IntVar>[] vectors;
	private UndirectedGraphVar g;
	private int d;
	private IntVar nVect;
	private int[][][] boxes;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	
	public NVector(int n, int k, int d, int[][][] b) {
		this.n = n;
		this.d = d;
		this.k = k;
		boxes = b;
		System.out.println(n+" : "+k);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() { 
		solver = new Solver();
		vectors = new MetaVariable[n];
		vars  	= new IntVar[n*d];
		for(int i=0;i<n;i++){
			for(int j=0;j<d;j++){
				vars[i*d+j] = VariableFactory.bounded("v", boxes[i][j][0], boxes[i][j][1], solver);
			}
		}
		Constraint[] meta = new Constraint[n];
		for(int i=0; i<n; i++){
			IntVar[] components = new IntVar[d];
			for(int j=0;j<d;j++){
				components[j] = vars[i*d+j];
			}
			vectors[i] = new MetaVariable("vector "+i, solver, components);
			meta[i] = new MetaVarConstraint(components, vectors[i], solver);
		}
		IntVar nv = VariableFactory.enumerated("n", n,n , solver);
		nVect = VariableFactory.bounded("N_CC", k,k, solver);
		GraphConstraint gc = GraphConstraintFactory.nVectors(vectors, nVect, solver, PropagatorPriority.LINEAR);
		g = (UndirectedGraphVar) gc.getGraph();
		gc.addProperty(GraphProperty.K_LOOPS, nv);
		gc.addProperty(GraphProperty.K_NODES, nv);
		Constraint[] cstrs = ArrayUtils.append(meta,new Constraint[]{gc});
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.graphLexico(g); 
		solver.set(strategy);
	}

	@Override
	public void solve() {
		System.out.println("resolution");
		solver.getSearchLoop().getLimitsFactory().setTimeLimit(100000);
		SearchMonitorFactory.log(solver, false, false);
//		Boolean status = solver.findSolution();
		Boolean status = solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, nVect);
	}

	@Override
	public void prettyOut() {
		System.out.println("nVect : "+nVect);
		System.out.println("KCC time "+PropKCC.duration);
		System.out.println("KCl time "+PropKCliques.duration);
		System.out.println("rel time "+PropRelation.duration);
		System.out.println("trans time "+PropTransitivity.duration);
	}

	//***********************************************************************************
	// MAIN
	//***********************************************************************************
	
	private static IntVar bounded(String name, int min, int max, Solver solver) {
        return VariableFactory.bounded(name, min, max, solver);
    }
	
	public static void main(String[] args) {
		File file = new File("box_set.csv");
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			line = line.replaceAll("\\);\\(","_");
			line = line.replaceAll("\\[","");
			line = line.replaceAll("\\]","");
			line = line.replaceAll(" ","");
			line = line.replaceAll("\\(","");
			line = line.replaceAll("\\)","");
			String[] bl = line.split("_");
			int n = bl.length;
			int d = bl[0].split(";").length;
			int[][][] boxes = new int[n][d][2];
			for(int i=0;i<n;i++){
				for(int j=0;j<d;j++){
					boxes[i][j][0] = Integer.parseInt(bl[i].split(";")[j].split(",")[0]);
					boxes[i][j][1] = Integer.parseInt(bl[i].split(";")[j].split(",")[1]);
				}
			}
			NVector nc = new NVector(n,4,d, boxes);
			nc.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//******************//
	// AD HOC INSTANCES //
	//******************//
	
	// dim = 2
	
//	vars[0] = bounded("v"+0+","+0, 1,6, solver);
//	vars[1] = bounded("v"+0+","+1, 2,7, solver);
//	vars[2] = bounded("v"+1+","+0, 2,5, solver);
//	vars[3] = bounded("v"+1+","+1, 5,9, solver);
//	vars[4] = bounded("v"+2+","+0, 3,10, solver);
//	vars[5] = bounded("v"+2+","+1, 1,4, solver);
//	vars[6] = bounded("v"+3+","+0, 4,11, solver);
//	vars[7] = bounded("v"+3+","+1, 3,8, solver);
//	vars[8] = bounded("v"+4+","+0, 9,12, solver);
//	vars[9] = bounded("v"+4+","+1, 0,7, solver);
	
//	vars[0] = bounded("v"+0+","+0, 10,30, solver);
//	vars[1] = bounded("v"+0+","+1, 0,20, solver);
//	vars[2] = bounded("v"+1+","+0, 1,20, solver);
//	vars[3] = bounded("v"+1+","+1, 9,25, solver);
//	vars[4] = bounded("v"+2+","+0, 5,24, solver);
//	vars[5] = bounded("v"+2+","+1, 17,36, solver);
//	vars[6] = bounded("v"+3+","+0, 20,38, solver);
//	vars[7] = bounded("v"+3+","+1, 21,41, solver);
//	vars[8] = bounded("v"+4+","+0, 25,45, solver);
//	vars[9] = bounded("v"+4+","+1, 22,39, solver);
//	vars[10] = bounded("v"+5+","+0, 2,22, solver);
//	vars[11] = bounded("v"+5+","+1, 13,32, solver);
//	vars[12] = bounded("v"+6+","+0, 16,32, solver);
//	vars[13] = bounded("v"+6+","+1, 7,26, solver);
//	vars[14] = bounded("v"+7+","+0, 3,23, solver);
//	vars[15] = bounded("v"+7+","+1, 22,39, solver);
//	vars[16] = bounded("v"+8+","+0, 5,26, solver);
//	vars[17] = bounded("v"+8+","+1, 28,46, solver);
//	vars[18] = bounded("v"+9+","+0, 0,20, solver);
//	vars[19] = bounded("v"+9+","+1, 24,43, solver);
//	vars[20] = bounded("v"+9+","+0, 10,20, solver);
//	vars[21] = bounded("v"+9+","+1, 2,43, solver);
//	vars[22] = bounded("v"+9+","+0, 5,12, solver);
//	vars[23] = bounded("v"+9+","+1, 2,3, solver);
//	vars[24] = bounded("v"+9+","+0, 20,25, solver);
//	vars[25] = bounded("v"+9+","+1, 14,23, solver);
//	vars[26] = bounded("v"+9+","+0, 10,12, solver);
//	vars[27] = bounded("v"+9+","+1, 32,43, solver);
}
