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

import java.util.Random;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropTreeHeldKarp2 extends Propagator implements HeldKarp {

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
	private Random rd;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropTreeHeldKarp2(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
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
		rd = new Random(0);
	}

	/** ONE TREE based HK */
	public static PropTreeHeldKarp2 mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropTreeHeldKarp2 phk = new PropTreeHeldKarp2(graph,cost,maxDegree,costMatrix,constraint,solver);
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
//		if(firstPropag){
//			totalPenalities = 0;
//			for(int i=0;i<n;i++){
//				penalities[i] = 0;
//			}
//		}
		totalPenalities = 0;
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
					costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					if(costs[i][j]<0){
						throw new UnsupportedOperationException();
					}
				}
			}
		}
		HK_Pascals();
//		HK_Pascals();
	}

	private long nbSols = 0;
	private int objUB = -1;

//	TIntArrayList fr,tr,fe,te;

	protected void HK_Pascals() throws ContradictionException {
//		nbSprints = 100;
//		nbSprints = 30;
//		if(firstPropag){
//			firstPropag = false;
//			restartRandom(n);
//		}
//		fastRun();
//		nbSprints = 30;
//		fastRun();
//		convergeAndFilter();
//		int nbIter = 5;
//		int nb = nbIter;
//		while(nb>0){
//			nb--;
//			restartRandom(n/10);
//		}
//		restartRandom(0); //reset

//		convergeAndFilter();
//		nbSprints = n;
//		restartRandom(0);
//		convergeAndFilter();
//		if(true)return;

		nbSprints = 100;
		nbSprints = 30;
		if(obj.getUB()<objUB && obj.instantiated()){
//			nbSprints = 100;
			nbSprints = 30;
			objUB = obj.getUB();
			convergeAndFilter();
//			int nbIter = n/10;
//			int nb = nbIter;
//			while(nb>0 && obj.getUB()>obj.getLB()){
//				nb--;
//				restartRandom(n/10);
//			}
//			restartRandom(0); //reset
		}else{
			nbSprints = 30;
			fastRun();
//			convergeAndFilter();
		}
	}

	protected void restartRandom(double coef) throws ContradictionException {
		totalPenalities = 0;
		double maxPen = 2*obj.getUB();
//		for(int i=0;i<n;i++){
//			penalities[i] = 0;
//			penalities[i] = coef*rd.nextDouble();
//			if(penalities[i]<0 || g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize() <= maxDegree[i]){
//				penalities[i] = 0;
//			}
//			if(penalities[i]>maxPen){
//				penalities[i] = maxPen;
//			}
//			totalPenalities += penalities[i]*maxDegree[i];
//		}
		totalPenalities = 0;
		for(int i=0;i<n;i++){
			totalPenalities += penalities[i]*maxDegree[i];
		}
		for(int k=0;k<coef;k++){
			int i = rd.nextInt(n);
//			penalities[i] += rd.nextDouble();
			penalities[i] = n*rd.nextDouble();
//			penalities[i] = rd.nextGaussian();
//			penalities[i] = coef*rd.nextDouble();
			if(penalities[i]<0 || g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize() <= maxDegree[i]){
				penalities[i] = 0;
			}
			if(penalities[i]>maxPen){
				penalities[i] = maxPen;
			}
			totalPenalities += penalities[i]*maxDegree[i];
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
		convergeAndFilter();
//		fastRun();
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
		while(oldhkb+0.001<besthkb){// || alpha>0.01){
			oldhkb = besthkb;
			convergeFast(alpha);
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities;
			if(hkb>besthkb){
				besthkb = hkb;
			}
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
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				if(updateStep(hkb, alpha))return;
			}
		}
	}

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
//		int k = 0;
//		int[] knode = new int[n];
//		for(int i=0;i<n;i++){
//			nei = mst.getSuccessorsOf(i);
//			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
//				if(i<j){
//					if(rc!=null && rc[i][j]> DCMST_lds.GOOD_LIMIT){
//						k++;
//						knode[i]++;
//					}
//				}
//			}
//		}
//		double coef = k-DCMST_lds.globalVarLDS.getValue();
//		if(rc==null || !DCMST_lds.globalVarLDS.instantiated()){
//			coef = 0;
//		}
//		double[] coefNode = new double[n];
//		for(int i=0;i<n;i++){
//			if(rc==null || !DCMST_lds.varsLDS[i].instantiated()){
//
//			}else{
//				coefNode[i] = knode[i] - DCMST_lds.varsLDS[i].getValue();
//			}
//		}
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
//					if(rc!=null && rc[i][j]> DCMST_lds.GOOD_LIMIT){
//						costs[i][j] += coef*LDS_PEN;
//						costs[i][j] += coefNode[i]*LDS_PEN_NODE;
//						costs[j][i] = costs[i][j];
//					}
				}
			}
//			totalPenalities += coefNode[i]*LDS_PEN_NODE*DCMST_lds.varsLDS[i].getValue();
		}
//		totalPenalities += coef*LDS_PEN*DCMST_lds.globalVarLDS.getValue();
		return false;
	}

	double LDS_PEN = 10;
	double LDS_PEN_NODE = 5;

	//***********************************************************************************
	// INFERENCE
	//***********************************************************************************

	public void remove(int from, int to) throws ContradictionException {
//		if(firstPropag){
		g.removeArc(from,to,this);
//		}else{
//		fr.add(from);
//		tr.add(to);
//		}
		nbRem++;
	}
	public void enforce(int from, int to) throws ContradictionException {
//		if(firstPropag){
		g.enforceArc(from,to,this);
//		}else{
//		fe.add(from);
//		te.add(to);
//		}
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
//		}nb2 /= 2;
//		double r = ((double)((nb-nb2)*100)/(double)(nb));
//		r = Math.round(r*100)/100.0;
//		System.out.println("current lower bound : "+obj.getLB()+"\ninitial HK pruned " + nbRem + " arcs ("+r+"%)\n"+nb2+" edges remaining");
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
//		throw new UnsupportedOperationException();
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
				if(i<j && !mst.edgeExists(i,j))
				rc[i][j] = rc[j][i] = getMarginalCost(i,j);
			}
		}
	}

	public void additiveFiltering(double offset, double maxBasisRC) throws ContradictionException {
		double lb = rlb+offset;
		if(lb-(int)lb<0.001){
			lb = Math.floor(lb);
		}
		lb = Math.ceil(lb);
		obj.updateLowerBound((int) lb,this);
		double delta = obj.getUB()-(rlb+offset);
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !g.getKernelGraph().edgeExists(i,j))
//				if(rc[i][j]>delta+0.001){
				if(rc[i][j]-maxBasisRC>delta+0.001){
//				if(2*rc[i][j]-maxBasisRC>delta+0.001){
//					System.out.println(rc[i][j]+" / "+maxBasisRC+" / "+delta);
//					System.out.println((rc[i][j]-maxBasisRC)+" / "+delta);
//					System.exit(0);
					g.removeArc(i,j,this);
				}
			}
		}
	}

	public void additiveFiltering(double offset, double[] maxBasisRC) throws ContradictionException {
		double lb = rlb+offset;
		if(lb-(int)lb<0.001){
			lb = Math.floor(lb);
		}
		lb = Math.ceil(lb);
		obj.updateLowerBound((int) lb,this);
		double delta = obj.getUB()-(rlb+offset);
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !g.getKernelGraph().edgeExists(i,j))
				if(rc[i][j]-maxBasisRC[i]>delta+0.001){
					g.removeArc(i,j,this);
				}
			}
		}
	}

	public void additiveFilteringOnNode(int node, double offset, double maxBasisRC) throws ContradictionException {
		double lb = rlb+offset;
		if(lb-(int)lb<0.001){
			lb = Math.floor(lb);
		}
		lb = Math.ceil(lb);
		obj.updateLowerBound((int) lb,this);
		double delta = obj.getUB()-(rlb+offset);
		INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(node);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(node<j && !g.getKernelGraph().edgeExists(node,j))
				if(rc[node][j]-maxBasisRC>delta+0.001){
					g.removeArc(node,j,this);
				}
		}
	}
}