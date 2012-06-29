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

package solver.constraints.propagators.gary.trees.lagrangianRelaxation;

import choco.kernel.ESat;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.HeldKarp;
import solver.constraints.propagators.gary.trees.AbstractTreeFinder;
import solver.constraints.propagators.gary.trees.KruskalMST_GAC;
import solver.constraints.propagators.gary.trees.PrimMSTFinder;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropTreeHeldKarp extends Propagator implements HeldKarp {

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
	protected AbstractTreeFinder HKfilter, HK;
	public long nbRem;
	protected boolean waitFirstSol;
	protected int nbSprints;
	protected int[] maxDegree;
	double step;
	boolean firstPropag = true;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropTreeHeldKarp(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		super(new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		originalCosts = costMatrix;
		costs = new double[n][n];
		penalities = new double[n];
		totalPenalities = 0;
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
		nbSprints = 30;
		this.maxDegree = maxDegree;
	}

	/** ONE TREE based HK */
	public static PropTreeHeldKarp mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropTreeHeldKarp phk = new PropTreeHeldKarp(graph,cost,maxDegree,costMatrix,constraint,solver);
		phk.HK = new PrimMSTFinder(phk.n,phk);
		phk.HKfilter = new KruskalMST_GAC(phk.n,phk);
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
		mandatoryArcsList.clear();
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getKernelGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					mandatoryArcsList.add(i * n + j);
				}
			}
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					if(costs[i][j]<0){
						throw new UnsupportedOperationException();
					}
				}
			}
		}
		HK_Pascals();
	}

//	protected void HK_Pascals() throws ContradictionException {
//		double hkb;
//		double alpha = 2;
//		double beta = 0.5;
//		if(firstPropag || true){
//			firstPropag = false;
//			HKfilter.computeMST(costs,g.getEnvelopGraph());
//			hkb = HKfilter.getBound()-totalPenalities;
//			mst = HKfilter.getMST();
//			if(hkb-Math.floor(hkb)<0.001){
//				hkb = Math.floor(hkb);
//			}
//			obj.updateLowerBound((int)Math.ceil(hkb), this);
//			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
//		}
//		for(int iter=5;iter>0;iter--){
//			for(int i=nbSprints;i>0;i--){
//				HK.computeMST(costs,g.getEnvelopGraph());
//				double kk = 0;
//				for(int j=0;j<n;j++){
//					kk+=penalities[j]*maxDegree[j];
//				}
//				if(kk!=totalPenalities){
//					throw new UnsupportedOperationException("error ktot : "+(kk-totalPenalities));
//				}
//				hkb = HK.getBound()-totalPenalities;
//				mst = HK.getMST();
//				if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
//				obj.updateLowerBound((int)Math.ceil(hkb), this);
//				if(updateStep(hkb, alpha))break;
//			}
////			HKfilter.computeMST(costs,g.getEnvelopGraph());
////			hkb = HKfilter.getBound()-totalPenalities;
////			mst = HKfilter.getMST();
////			if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
////			obj.updateLowerBound((int)Math.ceil(hkb), this);
////			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
////			beta  /= 2;
////			alpha *= beta;
//		}
//		HKfilter.computeMST(costs,g.getEnvelopGraph());
//		hkb = HKfilter.getBound()-totalPenalities;
//		mst = HKfilter.getMST();
//		if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
//		obj.updateLowerBound((int)Math.ceil(hkb), this);
//		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
//	}

//	protected void HK_Pascals() throws ContradictionException {
//		double hkb;
//		double alpha = 2;
//		double beta = 0.5;
//		for(int iter=5;iter>0;iter--){
//			converge(alpha);
//			HKfilter.computeMST(costs,g.getEnvelopGraph());
//			hkb = HKfilter.getBound()-totalPenalities;
//			mst = HKfilter.getMST();
//			if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
//			obj.updateLowerBound((int)Math.ceil(hkb), this);
//			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
//			alpha *= beta;
//			beta/=2;
//		}
//	}

	protected void HK_Pascals() throws ContradictionException {
		if(firstPropag){
			firstPropag = false;
			convergeAndFilter();
		}else{
			fastRun();
		}
	}

	protected void fastRun() throws ContradictionException {
		convergeFast(2);
		HKfilter.computeMST(costs,g.getEnvelopGraph());
		double hkb = HKfilter.getBound()-totalPenalities;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
	}

	protected void convergeAndFilter() throws ContradictionException {
		double hkb;
		double alpha = 2;
		double beta = 0.5;
		double besthkb = -9999998;
		double oldhkb = -9999999;
		while(oldhkb+0.1<besthkb){
			oldhkb = besthkb;
			convergeFast(alpha);
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities;
			if(hkb>besthkb){
				besthkb = hkb;
			}
			mst = HKfilter.getMST();
			if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
			alpha *= beta;
			beta/=2;
		}
	}

	protected void convergeFast(double alpha) throws ContradictionException {
		double besthkb = 0;
		double oldhkb = -20;
		while(oldhkb+0.1<besthkb){
			oldhkb = besthkb;
			for(int i=0;i<nbSprints;i++){
				HK.computeMST(costs,g.getEnvelopGraph());
				mst = HK.getMST();
				double hkb = HK.getBound()-totalPenalities;
				if(hkb-Math.floor(hkb)<0.001){
					hkb = Math.floor(hkb);
				}
				if(hkb>besthkb){
					besthkb = hkb;
				}
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				if(updateStep(hkb, alpha))return;
			}
		}
	}

//	protected void HK_Pascals() throws ContradictionException {
//		double hkb;
//		double alpha = 2;
//		double beta = 0.5;
//		HKfilter.computeMST(costs,g.getEnvelopGraph());
//		hkb = HKfilter.getBound()-totalPenalities;
//		mst = HKfilter.getMST();
//		if(hkb-Math.floor(hkb)<0.001){
//			hkb = Math.floor(hkb);
//		}
//		obj.updateLowerBound((int)Math.ceil(hkb), this);
////		if((int) Math.ceil(hkb)==evalTree(mst))
//		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
//		for(int iter=5;iter>0;iter--){
//			for(int i=nbSprints;i>0;i--){
//				HK.computeMST(costs,g.getEnvelopGraph());
//				double kk = 0;
//				for(int j=0;j<n;j++){
//					kk+=penalities[j]*maxDegree[j];
//				}
////				kk*=maxDegree;
//				if(kk!=totalPenalities){
//					throw new UnsupportedOperationException("error ktot : "+(kk-totalPenalities));
//				}
//				hkb = HK.getBound()-totalPenalities;
//				mst = HK.getMST();
//				if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
//				obj.updateLowerBound((int)Math.ceil(hkb), this);
//				if(updateStep(hkb, alpha))break;
//			}
////			HKfilter.computeMST(costs,g.getEnvelopGraph());
////			hkb = HKfilter.getBound()-totalPenalities;
////			mst = HKfilter.getMST();
////			if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
////			obj.updateLowerBound((int)Math.ceil(hkb), this);
//////			if((int) Math.ceil(hkb)==evalTree(mst))
////			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
////			if(updateStep(hkb, alpha))return;
////			alpha *= beta;
//			beta  /= 2;
//		}
//		HKfilter.computeMST(costs,g.getEnvelopGraph());
//			hkb = HKfilter.getBound()-totalPenalities;
//			mst = HKfilter.getMST();
//			if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
//			obj.updateLowerBound((int)Math.ceil(hkb), this);
////			if((int) Math.ceil(hkb)==evalTree(mst))
//			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
//			if(updateStep(hkb, alpha))return;
////			alpha *= beta;
////			beta  /= 2;
//	}

	protected boolean updateStep(double hkb,double alpha) throws ContradictionException {
		double nb2viol = 0;
		double target = obj.getUB();
		if(target-hkb<0){
			throw new UnsupportedOperationException();
		}
		if(target-hkb<0.001){
			target = hkb+0.001;
		}
		int deg;
		boolean found = true;
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			if(deg>maxDegree[i]){
				found = false;
			}
			nb2viol += (maxDegree[i]-deg)*(maxDegree[i]-deg);
		}
		if(found){
			return true;
		}else{
			step = alpha*(target-hkb)/nb2viol;
		}
		if(step<0.0001){
			return true;
		}
		double maxPen = 2*obj.getUB();
		totalPenalities = 0;
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			penalities[i] += (deg-maxDegree[i])*step;
			if(penalities[i]<0 || g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize() <= maxDegree[i]){
				penalities[i] = 0;
			}
			if(penalities[i]>maxPen){
				penalities[i] = maxPen;
			}
			if(penalities[i]>Double.MAX_VALUE/(n-1) || penalities[i]<0){
				throw new UnsupportedOperationException();
			}
			totalPenalities += penalities[i]*maxDegree[i];
		}
//		totalPenalities *= maxDegree;
		if(totalPenalities>Double.MAX_VALUE/(n-1) || totalPenalities<0){
			throw new UnsupportedOperationException();
		}
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
				}
			}
		}
		return false;
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
//		int nb = 0;
//		for(int i=0;i<n;i++){
//			nb+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
//		}
//		nb /= 2;System.out.println(nb + " edges\n" + obj);
		HK_algorithm();
//		int nb2 = 0;
//		for(int i=0;i<n;i++){
//			nb2+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
//		}nb2 /= 2;System.out.println("current lower bound : "+obj.getLB()+"\ninitial HK pruned " + nbRem + " arcs ("+((nb-nb2)*100/nb)+"%)\n"+nb2+" edges remaining");
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
		return -1;
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
//		throw new UnsupportedOperationException();
		return HKfilter.getRepCost(from,to);
	}

	public double getMarginalCost(int from, int to){
		throw new UnsupportedOperationException();
		//return HKfilter.getRepCost(from,to);
	}
}