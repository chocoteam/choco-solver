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
import choco.kernel.memory.IStateInt;
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

public class Prop_LP_GRB extends GraphPropagator<Variable> {

	DirectedGraphVar g;
	IntVar obj;
	int[][] originalCosts;
	int n;
	int m;
	int[] arcsArray;
	int[][] arcsMatrix;
	// RG data structures
	private INeighbors[] outArcs;
	private IStateInt nR;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public Prop_LP_GRB(DirectedGraphVar graph, IntVar objective, int[][] costsMatrix, Solver sol, Constraint constraint) {
		super(new Variable[]{graph,objective}, sol, constraint, PropagatorPriority.CUBIC);
		g = graph;
		obj = objective;
		originalCosts = costsMatrix;
		n = originalCosts.length;
		arcsMatrix = new int[n][n];
		arcsArray  = new int[n*n];
	}

	public Prop_LP_GRB(DirectedGraphVar graph, IntVar objective, int[][] costsMatrix, Solver sol, Constraint constraint,INeighbors[] outArcs, IStateInt nr) {
		this(graph, objective, costsMatrix, sol, constraint);
		this.nR  = nr;
		this.outArcs = outArcs;
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		grbOpt();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// LP
	//***********************************************************************************

	private void grbOpt() throws ContradictionException {
		try {
			// Model
			INeighbors nei;
			m=0;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					arcsArray[m] = i*n+j;
					arcsMatrix[i][j] = m;
					m++;
				}
			}
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			model.set(GRB.StringAttr.ModelName, "tsp");
			// Create decision variables for the nutrition information, which we limit via bounds
			int idx = 0;
			GRBVar[] arcs = new GRBVar[m];
			for (int i = 0; i < n; i++) {
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j= nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					arcs[idx++] = model.addVar(0, 1, originalCosts[i][j], GRB.CONTINUOUS,"v"+i+"-"+j);
				}
			}
			// The objective is to minimize the costs
			model.set(GRB.IntAttr.ModelSense, 1);
			// Update model to integrate new variables
			model.update();

			/* CONSTRAINTS */
			// 1 successor per node
			GRBLinExpr succ;
			for (int i = 0; i < n-1; i++) {
				succ = new GRBLinExpr();
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j= nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					succ.addTerm(1,arcs[arcsMatrix[i][j]]);
				}
				model.addConstr(succ, GRB.EQUAL, 1, "l"+i);
			}
			// 1 predecessor per node
			GRBLinExpr pred;
			for (int i = 1; i < n; i++) {
				pred = new GRBLinExpr();
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j= nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					pred.addTerm(1,arcs[arcsMatrix[j][i]]);
				}
				model.addConstr(pred, GRB.EQUAL, 1, "c"+i);
			}
			// no 2 ways tickets
			GRBLinExpr cut;
			for (int i = 0; i < n-1; i++) {
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j= nei.getFirstElement(); j>=0; j=nei.getNextElement()){
					if(g.getEnvelopGraph().getSuccessorsOf(j).contain(i)){
						cut = new GRBLinExpr();
						cut.addTerm(1,arcs[arcsMatrix[i][j]]);
						cut.addTerm(1,arcs[arcsMatrix[j][i]]);
						model.addConstr(cut, GRB.LESS_EQUAL, 1, "cut"+i+"-"+j);
					}
				}
			}
			// rg cuts
			if(nR!=null){
				GRBLinExpr rgCut;
				for (int i = nR.get()-1; i>=0; i--) {
					nei = outArcs[i];
					if(nei.neighborhoodSize()>0){
						rgCut = new GRBLinExpr();
						for(int j= nei.getFirstElement(); j>=0; j=nei.getNextElement()){
							rgCut.addTerm(1,arcs[arcsMatrix[j/n-1][j%n]]);
						}
						model.addConstr(rgCut, GRB.EQUAL, 1, "rgcut"+i);
					}
				}
			}

			// Use dual simplex to solve model
			model.getEnv().set(GRB.IntParam.Method, GRB.METHOD_DUAL);
			// Solve
			model.optimize();
			// filter
			double lb = model.get(GRB.DoubleAttr.ObjVal);
			double ub = obj.getUB();
			System.out.println(lb+" LP LOWER BOUND");
			obj.updateLowerBound((int)Math.ceil(lb),this);
			int nbRem = 0;
			for(int i=0;i<m;i++){
				if(lb+model.getVar(i).get(GRB.DoubleAttr.RC)>ub){
					g.removeArc(arcsArray[i]/n,arcsArray[i]%n,this);
					nbRem++;
				}
			}
			System.out.println("removed "+nbRem+" arcs");
			// Dispose of model and environment
			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			//System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
			contradiction(g,"");
		}
	}
}
