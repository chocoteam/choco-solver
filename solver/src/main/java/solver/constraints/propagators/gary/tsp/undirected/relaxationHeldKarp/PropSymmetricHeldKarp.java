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
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.HeldKarp;
import solver.constraints.propagators.gary.trees.AbstractTreeFinder;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * TSP Lagrangian relaxation
 * @PropAnn(tested = {BENCHMARK})
 */
public class PropSymmetricHeldKarp extends Propagator implements HeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected UndirectedGraphVar g;
	protected IntVar obj;
	protected int n;
	protected int[][] originalCosts;
	protected double[][] costs;
	double[] penalities;
	double totalPenalities;
	protected UndirectedGraph mst;
	protected TIntArrayList mandatoryArcsList;
	protected  double step;
	protected AbstractTreeFinder HKfilter, HK;
	public static long nbRem;
	protected static boolean waitFirstSol;
	protected int nbSprints = 30;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropSymmetricHeldKarp(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		super(new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		originalCosts = costMatrix;
		costs = new double[n][n];
		totalPenalities = 0;
		penalities = new double[n];
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
	}

	/** ONE TREE based HK */
	public static PropSymmetricHeldKarp oneTreeBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropSymmetricHeldKarp phk = new PropSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalOneTree_GAC(phk.n,phk);
		phk.HK = new PrimOneTreeFinder(phk.n,phk);
		return phk;
	}

	//***********************************************************************************
	// HK Algorithm(s) 
	//***********************************************************************************

	public void HK_algorithm() throws ContradictionException {
		if(waitFirstSol && solver.getMeasures().getSolutionCount()==0){
			return;//the UB does not allow to prune
		}
		// initialisation
		clearStructures();
		rebuildGraph();
		setCosts();
		HK_Pascals();
	}

	protected void setCosts() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	protected void HK_Pascals() throws ContradictionException {
		double hkb;
		double alpha = 2;
//		double beta = 0.95;
		double beta = 0.5;
//		if(nbSprints==31){
//			beta = 0.95;
//		}
		double bestHKB;
		boolean improved;
		int count = 2;
		bestHKB = 0;
		HKfilter.computeMST(costs,g.getEnvelopGraph());
		hkb = HKfilter.getBound()-totalPenalities;
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.001){
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
		for(int iter=5;iter>0;iter--){
			improved = false;
			for(int i=nbSprints;i>0;i--){
				HK.computeMST(costs,g.getEnvelopGraph());
				hkb = HK.getBound()-totalPenalities;
				if(hkb>bestHKB+1){
					bestHKB = hkb;
					improved = true;
				}
				mst = HK.getMST();
				if(hkb-Math.floor(hkb)<0.001){
					hkb = Math.floor(hkb);
				}
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				//	DO NOT FILTER HERE TO FASTEN CONVERGENCE (not always true)
				updateStep(hkb,alpha);
				HKPenalities();
				updateCostMatrix();
			}
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities;
			if(hkb>bestHKB+1){
				bestHKB = hkb;
				improved = true;
			}
			mst = HKfilter.getMST();
			if(hkb-Math.floor(hkb)<0.001){
				hkb = Math.floor(hkb);
			}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
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

	protected void clearStructures() {
		mandatoryArcsList.clear();
	}
	protected void rebuildGraph() {
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
	protected void updateStep(double hkb,double alpha) {
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

	protected void HKPenalities() {
		if(step==0){
			return;
		}
		double sumPenalities = 0;
		int deg;
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			penalities[i] += (deg-2)*step;
			if(penalities[i]>Double.MAX_VALUE/(n-1) || penalities[i]<-Double.MAX_VALUE/(n-1)){
				throw new UnsupportedOperationException();
			}
			sumPenalities += penalities[i];
		}
//		if(mst.getNeighborsOf(0).neighborhoodSize()!=2){
//			throw new UnsupportedOperationException();
//		};
		this.totalPenalities = 2*sumPenalities;
	}

	protected void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
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
	public void enforce(int from, int to) throws ContradictionException {
		g.enforceArc(from,to,this);
	}
	public void contradiction() throws ContradictionException {
		contradiction(g,"mst failure");
	}

	//***********************************************************************************
	// PROP METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
//		nbSprints = n/2;
//		nbSprints = 31;
		_propagate(evtmask);
		nbSprints = 30;
//		for(int i=0;i<n;i++){
//			penalities[i] = 0;
//		}
//		totalPenalities = 0;
	}

	public void _propagate(int evtmask) throws ContradictionException {
		int nb = 0;
		for(int i=0;i<n;i++){
			nb+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
		}
		nb /= 2;
//		System.out.println(nb+" edges");
//		System.out.println(obj);
		HK_algorithm();
		int nb2 = 0;
		for(int i=0;i<n;i++){
			nb2+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
		}nb2 /= 2;
//		System.out.println("current lower bound : "+obj.getLB());
//		System.out.println("initial HK pruned " + nbRem + " arcs ("+((nb-nb2)*100/nb)+"%)");
//		System.out.println(nb2+" edges");
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		HK_algorithm();
	}
	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask+EventType.INCLOW.mask+EventType.INSTANTIATE.mask;
	}
	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	public double getMinArcVal() {
		return -(((double)obj.getUB())+totalPenalities);
	}

	public TIntArrayList getMandatoryArcsList() {
		return mandatoryArcsList;
	}

	public boolean isMandatory(int i, int j){
		return g.getKernelGraph().edgeExists(i,j);
	}

	public void waitFirstSolution(boolean b){
		waitFirstSol = b;
	}

	public boolean contains(int i, int j){
		if(mst==null){
			return true;
		}
		return mst.edgeExists(i,j);
	}
	public UndirectedGraph getMST(){
		return mst;
	}
	
	public double getReplacementCost(int from, int to){
		return HKfilter.getRepCost(from,to);
	}

	public double getMarginalCost(int from, int to){
		return HKfilter.getRepCost(from,to);
	}
}