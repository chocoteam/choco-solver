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
import gnu.trove.list.array.TDoubleArrayList;
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

import java.util.Random;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropTreeHeldKarp2DynaOneNode extends Propagator implements HeldKarp {

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
	private int kone,oneNode;
	double BIG_M = 0;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropTreeHeldKarp2DynaOneNode(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
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
		for(int i=0;i<n;i++){
			if(maxDegree[i]==1){
				kone++;
			}
		}
		System.out.println("kone = "+kone);
	}

	/** ONE TREE based HK */
	public static PropTreeHeldKarp2DynaOneNode mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropTreeHeldKarp2DynaOneNode phk = new PropTreeHeldKarp2DynaOneNode(graph,cost,maxDegree,costMatrix,constraint,solver);
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
		totalPenalities = 0;
		BIG_M = obj.getUB()+1;
//		BIG_M = 0;
		for(int i=0;i<n;i++){
			totalPenalities += penalities[i]*maxDegree[i];
			nei = g.getKernelGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					mandatoryArcsList.add(i * n + j);
				}
			}
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					if(maxDegree[i]==1 || maxDegree[j]==1){
						costs[i][j] += BIG_M;
					}
					costs[j][i] = costs[i][j];
				}
			}
		}
		totalPenalities+= kone*BIG_M;
		HK_Pascals();
	}

	protected void HK_Pascals() throws ContradictionException {
		filterRecord();
		if(firstPropag){
			firstPropag = false;
			convergeAndFilter();
			record();
		}
//		convergeAndFilter();
//		fastRun();
//		int lb;
//		do{
//			lb = obj.getLB();
			fastRun(2);
			fastRun(0.5);
			fastRun(0.125);
//		}while (lb<obj.getLB());
	}

	protected void fastRun() throws ContradictionException {
		fastRun(2);
	}
	protected void fastRun(double coef) throws ContradictionException {
		convergeFast(coef);
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
		while(oldhkb+0.001<besthkb || alpha>0.01){
			oldhkb = besthkb;
			convergeFast(alpha);
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities;
			if(hkb>besthkb){
				besthkb = hkb;
			}
//			System.out.println(hkb);
			mst = HKfilter.getMST();
			if(hkb-Math.floor(hkb)<0.00001){hkb = Math.floor(hkb);}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
			alpha *= beta;
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
//				System.out.println(hkb);
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				if(updateStep(hkb, alpha))return;
			}
		}
	}

	protected boolean updateStep(double hkb,double alpha) throws ContradictionException {
		double nb2viol = 0;
		double target = obj.getUB();
//		target *= 1.01;//TODO a retirer peut-etre (a ete ajoute pour esperer resoudre DE)
		if(target-hkb<0){
			throw new UnsupportedOperationException();
		}
		if(target-hkb<0.001){
			target = hkb+0.001;
		}
		int deg;
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			if(deg>maxDegree[i] || penalities[i]>0){
				if(maxDegree[i]==1 && BIG_M>=obj.getUB())throw new UnsupportedOperationException();
				nb2viol += (maxDegree[i]-deg)*(maxDegree[i]-deg);
			}
		}
		if(nb2viol==0){
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
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					if(maxDegree[i]==1 || maxDegree[j]==1){
						costs[i][j] += BIG_M;
					}
					costs[j][i] = costs[i][j];
				}
			}
//			totalPenalities += coefNode[i]*LDS_PEN_NODE*DCMST_lds.varsLDS[i].getValue();
		}
		totalPenalities+= kone*BIG_M;
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
		HK_algorithm();
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
		return HKfilter.getRepCost(from,to);
	}

	public double getMarginalCost(int from, int to){
		return HKfilter.getRepCost(from,to);
	}

	public double[][] rc;
	public double rlb;

	public void record() throws ContradictionException {
		if(rc!=null)return;
		rc = new double[n][n];
		rlb = HKfilter.getBound()-totalPenalities;
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !mst.edgeExists(i, j))
				rc[i][j] = rc[j][i] = getMarginalCost(i,j);
			}
		}
	}

	public void filterRecord() throws ContradictionException {
		if(rc==null)return;
		double lb = rlb;
		if(lb-(int)lb<0.001){
			lb = Math.floor(lb);
		}
		lb = Math.ceil(lb);
		double delta = obj.getUB()-rlb;
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !g.getKernelGraph().edgeExists(i,j))
				if(rc[i][j]>delta+0.001){
					g.removeArc(i,j,this);
				}
			}
		}
	}


	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	private void findAllCuts(double lb){
		double minoutcost = obj.getUB();
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !mst.edgeExists(i, j)){
					if(costs[i][j]<minoutcost){
						minoutcost = costs[i][j];
					}
				}
			}
		}
		TDoubleArrayList possibleBasisF = new TDoubleArrayList();
		TDoubleArrayList possibleBasisT = new TDoubleArrayList();
		TDoubleArrayList possibleBasisDelta = new TDoubleArrayList();
		double sumDelta = 0;
		for(int i=0;i<n;i++){
			INeighbors nei = mst.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && minoutcost>costs[i][j] && !g.getKernelGraph().edgeExists(i,j)){
					possibleBasisF.add(i);
					possibleBasisT.add(j);
					possibleBasisDelta.add(minoutcost-costs[i][j]);
				}
			}
		}
		double ub = obj.getUB();
		int nbCuts = possibleBasisF.size();
		for(int c=0;c<nbCuts;c++){
			double delta = possibleBasisDelta.get(c);
		for(int i=0;i<n;i++){
			INeighbors nei = mst.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !g.getKernelGraph().edgeExists(i,j)){
					// pas dans c
					if(i-2*n*j!=possibleBasisF.get(c)-2*n*possibleBasisT.get(c))
					if(getReplacementCost(i,j)+delta+lb>ub+0.001){
						//add
					}
//					System.out.println("delta : " +(int)(mincost-costs[i][j]));
//					System.out.println((int)getReplacementCost(i,j)+" / "+(int)(rlb+getReplacementCost(i,j)-12645));
				}
			}
		}
		}
		int ct2 = 0;
		for(int i=0;i<n;i++){
			INeighbors nei = mst.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(Math.ceil(getReplacementCost(i,j)+sumDelta+rlb-12645)>0)
				if(i<j && !g.getKernelGraph().edgeExists(i,j)){
					ct2++;
					System.out.println((int)getReplacementCost(i,j)+" / "+(int)(rlb+getReplacementCost(i,j)-12645));
//					System.out.println("=> "+Math.ceil(getReplacementCost(i,j)+sumDelta+rlb-12645));
				}
			}
		}
		System.out.println(ct2);
		System.exit(0);
	}

}