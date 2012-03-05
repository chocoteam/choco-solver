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
package solver.constraints.propagators.gary.tsp.directed;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.StoredDirectedGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

public class PropLP extends GraphPropagator<Variable> {

	DirectedGraphVar g;
	IntVar obj;
	int[][] originalCosts;
	int n;
	int m;
	int[] arcsArray;
	int[][] arcsMatrix;
	static final String input = "/Users/jfages07/Documents/code/glpk-4.35/examples/tsp/model.mod", output="";

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropLP(DirectedGraphVar graph, IntVar objective, int[][] costsMatrix, Solver sol, Constraint constraint) {
		super(new Variable[]{graph,objective}, sol, constraint, PropagatorPriority.CUBIC);
		g = graph;
		obj = objective;
		originalCosts = costsMatrix;
		n = originalCosts.length;
		arcsMatrix = new int[n][n];
		arcsArray  = new int[n*n];
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		// build input file
		buildModel();
		System.exit(0);
		// solve
		// read output file
		// filter
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// INPUT
	//***********************************************************************************

	private void buildModel(){
		clearFile(input);
		m=0;
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
				arcsArray[m] = i*n+j;
				arcsMatrix[i][j] = m;
				m++;
			}
		}

		String model = "param n, integer, >= 3;\n set V := 1..n;\n param c{int in V};\n var x{i in V}, >= 0;\n";

		model += "minimize total: sum{i in V} c[i] * x[i];\n";
		try {
			FileWriter out = new FileWriter(input, true);
			out.write(model);

			String cons = "";

			for(int i=0;i<n-1;i++){
				cons = "s.t. l"+i+": 0";
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					cons += " + x["+(arcsMatrix[i][j]+1)+"]";
				}
				cons+=" = 1;\n";
				out.write(cons);
			}

			for(int i=1;i<n;i++){
				cons = "s.t. p"+i+": 0";
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					cons += " + x["+(arcsMatrix[j][i]+1)+"]";
				}
				cons+=" = 1;\n";
				out.write(cons);
			}

			out.write("solve;\n");

//			out.write("data;\n param n := "+m+";\n param : c :=\n");
////			out.write("data;\n param n := "+m+";\n param : V : c :=\n");
//			for(int i=0;i<n-1;i++){
//				nei = g.getEnvelopGraph().getSuccessorsOf(i);
//				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
//					out.write("\t"+originalCosts[i][j]+"\n");
////					out.write("\t"+(arcsMatrix[i][j]+1)+"\t"+originalCosts[i][j]+"\n");
//				}
//			}
			out.write("data;\n param n := "+m+";\n param : c :=\t");
			nei = g.getEnvelopGraph().getSuccessorsOf(0);
			out.write(""+originalCosts[0][nei.getFirstElement()]);
			for(int j=nei.getNextElement(); j>=0; j=nei.getNextElement()){
				out.write(",\t"+originalCosts[0][j]);
			}
			for(int i=1;i<n-1;i++){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					out.write(",\t"+originalCosts[i][j]);
				}
			}
			out.write(";");
			out.flush();
			out.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeTextInto(String text, String file) {
		try {
			FileWriter out = new FileWriter(file, true);
			out.write(text);



			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearFile(String file) {
		try {
			FileWriter out = new FileWriter(file, false);
			out.write("");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//***********************************************************************************
	// INPUT
	//***********************************************************************************



}
