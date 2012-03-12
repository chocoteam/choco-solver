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
import choco.kernel.common.util.tools.ArrayUtils;
import gurobi.*;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.io.*;

public class PropLP extends GraphPropagator<Variable> {

	DirectedGraphVar g;
	IntVar obj;
	int[][] originalCosts;
	int n;
	int m;
	int nbC;
	int[] arcsArray;
	int[][] arcsMatrix;
	static final String input = "/Users/jfages07/Documents/code/glpk-4.35/examples/tsp/model.mod",
			command="/Users/jfages07/Documents/code/glpk-4.35/examples/glpsol --model /Users/jfages07/Documents/code/glpk-4.35/examples/tsp/model.mod --output /Users/jfages07/Documents/code/glpk-4.35/examples/tsp/soluce.txt",
			output="/Users/jfages07/Documents/code/glpk-4.35/examples/tsp/soluce.txt";

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
//		buildModel();
//		solve();
//		filter();
		grbOpt();
	}

	private void grbOpt(){
		try {
			int n2 = n*(n-1);
			double cost[] = new double[n2];
			for(int i=0;i<n-1;i++){
				for(int j=0;j<n;j++){
					if(g.getEnvelopGraph().arcExists(i,j)){
						cost[i*n+j] = originalCosts[i][j];
					}else{
						cost[i*n+j] = GRB.INFINITY;
					}
				}
			}

			// Model
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			model.set(GRB.StringAttr.ModelName, "tsp");

			// Create decision variables for the nutrition information,
			// which we limit via bounds
			GRBVar[] arcs = new GRBVar[n2];
			for (int i = 0; i < n2; i++) {
				arcs[i] = model.addVar(0, 1, cost[i], GRB.CONTINUOUS,"v"+i);
			}

			// The objective is to minimize the costs
			model.set(GRB.IntAttr.ModelSense, 1);

			// Update model to integrate new variables
			model.update();

			// Nutrition constraints
			GRBLinExpr line = new GRBLinExpr();
			GRBLinExpr col = new GRBLinExpr();
			for (int j = 0; j < n; j++) {
				line.addTerm(1,arcs[j]);
				col.addTerm(1,arcs[(n-1)*n-1]);
			}
			model.addConstr(line, GRB.EQUAL, 1, "l"+0);
			model.addConstr(col, GRB.EQUAL, 1, "c"+0);
			for (int i = 1; i < n-1; i++) {
				line = new GRBLinExpr();
				col = new GRBLinExpr();
				for (int j = 0; j < n; j++) {
					line.addTerm(1,arcs[i*n+j]);
					col.addTerm(1,arcs[j*n+i]);
				}
				model.addConstr(line, GRB.EQUAL, 1, "l"+i);
				model.addConstr(col, GRB.EQUAL, 1, "c"+i);
			}


			for (int i = 0; i < n-1; i++) {
				line = new GRBLinExpr();
				for (int j = 0; j < n; j++) {
					line.addTerm(1,arcs[i*n+j]);
				}
				model.addConstr(line, GRB.EQUAL, 1, "l"+i);
			}

			for (int i = 0; i < n-1; i++) {
				col = new GRBLinExpr();
				for (int j = 0; j < n; j++) {
					col.addTerm(1,arcs[j*n+i]);
				}
				model.addConstr(col, GRB.EQUAL, 1, "c"+i);
			}


			// Use barrier to solve model
			model.getEnv().set(GRB.IntParam.Method, GRB.METHOD_BARRIER);

			// Solve
			model.optimize();
//			printSolution(model, buy, nutrition);

			// Dispose of model and environment
			model.dispose();
			env.dispose();
			System.exit(0);

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
					e.getMessage());
		}
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
		nbC = 0;
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
				arcsArray[m] = i*n+j;
				arcsMatrix[i][j] = m;
				m++;
			}
		}
		String model = "param n, integer, >= 3;\n set V ;\n param c{int in V};\n var x{i in V}, >= 0, <=1;\n";
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
				nbC++;
			}
			for(int i=1;i<n;i++){
				cons = "s.t. p"+i+": 0";
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					cons += " + x["+(arcsMatrix[j][i]+1)+"]";
				}
				cons+=" = 1;\n";
				out.write(cons);
				nbC++;
			}
//			for(int i=1;i<n;i++){
//				nei = g.getEnvelopGraph().getSuccessorsOf(i);
//				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
//					if(g.getEnvelopGraph().getSuccessorsOf(j).contain(i)){
//						out.write("s.t. cut"+i+"to"+j+": x["+(arcsMatrix[i][j]+1)+"] + x["+(arcsMatrix[j][i]+1)+"] <= 1;\n");
//						nbC++;
//					}
//				}
//			}
			out.write("solve;\n");
			out.write("data;\n param n := "+m+";\n param : V : c :=");
			int k = 1;
			for(int i=0;i<n-1;i++){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					out.write("\n\t"+k+"\t"+originalCosts[i][j]);
					k++;
				}
			}
			out.write("\t;");
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}

	}
	private void solve(){
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(command);
			p.waitFor();
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}

	}
	private void filter() throws ContradictionException {
		try {
			FileReader sol = new FileReader(output);
			BufferedReader buf = new BufferedReader(sol);
			String line = buf.readLine();
			String[] lineElements;
			for(int i=0;i<5;i++){
				line = buf.readLine();
			}
			line = line.split("=")[1];
			lineElements = line.split(" ");
			double lb = Double.parseDouble(lineElements[1]);
			obj.updateLowerBound((int)Math.ceil(lb),this);
			System.out.println(lb);System.exit(0);
			double ub = obj.getUB();
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			line = buf.readLine();
			for(int i=nbC; i>=0;i--){
				line = buf.readLine();
			}
			line = buf.readLine();
			line = buf.readLine();
			double rc;
			for(int i=0; i<m;i++){
				line = buf.readLine().replaceAll(" * "," ");
				lineElements = line.split(" ");
				if(lineElements[3].equals("NL") && !line.contains("eps")){
					rc = Double.parseDouble(lineElements[6]);
					if(lb+rc>ub+0.1){
						g.removeArc(arcsArray[m]/n,arcsArray[m]%n,this);
					}
				}
			}
			buf.close();
			sol.close();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
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

}
