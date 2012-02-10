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

package solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp;

import choco.kernel.ESat;
import choco.kernel.memory.IStateDouble;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.BitSet;
import java.util.Random;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropSymmetricHeldKarp<V extends Variable> extends GraphPropagator<V> implements HeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	UndirectedGraphVar g;
	IntVar obj;
	int n;
	int[][] originalCosts;
	double[][] costs;
	IStateDouble[] penalities;
	UndirectedGraph mst;
	TIntArrayList mandatoryArcsList;
	private double step;
	private IStateDouble totalPenalities;
	private AbstractMSTFinder HKfilter, HK;
	public static long nbRem;
	private final static boolean forceTour = false;
	private final static boolean DEBUG = false;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	private PropSymmetricHeldKarp(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		originalCosts = costMatrix;
		costs = new double[n][n];
		penalities = new IStateDouble[n];
		for(int i=0;i<n;i++){
			penalities[i] = environment.makeFloat(0);
		}
		totalPenalities= environment.makeFloat(0);
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
	}


	/** MST based HK */
	public static PropSymmetricHeldKarp mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropSymmetricHeldKarp phk = new PropSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n,phk);
		phk.HK = new PrimMSTFinder(phk.n,phk);
//		phk.HKfilter = new PrimMSTFinder(phk.n,phk);
		return phk;
	}

	//***********************************************************************************
	// HK Algorithm(s) 2147483647
	//***********************************************************************************

	public void HK_algorithm() throws ContradictionException {
		if(solver.getMeasures().getSolutionCount()==0){
			return;//the UB does not allow to prune
		}
		// initialisation
		clearStructures();
		rebuildGraph();
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i].get() + penalities[j].get();
					costs[j][i] = costs[i][j];
				}
			}
		}
		HK_Pascals();
	}

	private void HK_Pascals() throws ContradictionException {
		double hkb;
		double alpha = 2;
		double beta = 0.5;
		double bestHKB;
		boolean improved;
		int count = 2;
		bestHKB = 0;
		HKfilter.computeMST(costs,g.getEnvelopGraph());
		hkb = HKfilter.getBound()-totalPenalities.get();
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.01){
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities.get() + 0.01);
		for(int iter=3;iter>0;iter--){
			improved = false;
			for(int i=n;i>0;i--){
				HK.computeMST(costs,g.getEnvelopGraph());
				hkb = HK.getBound()-totalPenalities.get();
				if(hkb>bestHKB+0.01){
					bestHKB = hkb;
					improved = true;
				}
				mst = HK.getMST();
				if(hkb-Math.floor(hkb)<0.01){
					hkb = Math.floor(hkb);
				}
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				//	DO NOT FILTER HERE TO FASTEN CONVERGENCE (not always true)
				//	HK.performPruning((double) (obj.getUB()) + totalPenalities.get() +total + 0.01);
				updateStep(hkb,alpha);
				HKPenalities();
				updateCostMatrix();
			}
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities.get();
			if(hkb>bestHKB+0.1){
				bestHKB = hkb;
				improved = true;
			}
			mst = HKfilter.getMST();
			if(hkb-Math.floor(hkb)<0.01){
				hkb = Math.floor(hkb);
			}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities.get() + 0.01);
			updateStep(hkb,alpha);
			HKPenalities();
			updateCostMatrix();
			if(!improved){
				count--;
				if(count==0){
					return;
				}
			}
			alpha *= beta;
			beta  /= 2;
		}
	}

	//***********************************************************************************
	// DETAILS
	//***********************************************************************************

	private void clearStructures() {
		mandatoryArcsList.clear();
	}
	private void rebuildGraph() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getKernelGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					mandatoryArcsList.add(i * n + j);
				}
			}
		}
	}
	private void updateStep(double hkb,double alpha) {
		double nb2viol = 0;
		double target = obj.getUB();
//		target = (obj.getUB()+obj.getLB())/2; // TODO recently added
		if(target-hkb<0){
			target = hkb+0.1;
		}
		int deg;
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			nb2viol += (2-deg)*(2-deg);
		}
		if(nb2viol == 0){
			step = 0;
		}else{
			step = alpha*(target-hkb)/nb2viol;
		}
	}
	private void HKPenalities() {
		if(step==0){
			return;
		}
		double sumPenalities = 0;
		int deg;
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			penalities[i].add((deg-2)*step);
			sumPenalities += penalities[i].get();
		}
		if(penalities[0].get()!=0){
			throw new UnsupportedOperationException();
		}
		this.totalPenalities.set(2*sumPenalities);
	}
	private void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i].get() + penalities[j].get();
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	//***********************************************************************************
	// INFERENCE
	//***********************************************************************************

	public void remove(int from, int to) throws ContradictionException {
		g.removeArc(from,to,this);
		nbRem++;
	}
	public void contradiction() throws ContradictionException {
		contradiction(g,"mst failure");
	}

	//***********************************************************************************
	// PROP METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		HK_algorithm();
		System.out.println("initial HK pruned " + nbRem + " arcs (" + ((nbRem * 100) / (n * n)) + "%)");
		System.out.println("current lower bound : "+obj.getLB());
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		HK_algorithm();
	}
	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask;
	}
	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	public double getMinArcVal() {
		return -(((double)obj.getUB())+totalPenalities.get());
	}

	public TIntArrayList getMandatoryArcsList() {
		return mandatoryArcsList;
	}

	public boolean isMandatory(int i, int j){
		return g.getKernelGraph().edgeExists(i,j);
	}
}
