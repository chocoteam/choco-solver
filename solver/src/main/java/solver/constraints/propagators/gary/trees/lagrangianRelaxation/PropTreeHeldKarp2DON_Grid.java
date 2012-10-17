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
import choco.kernel.memory.IStateDouble;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import samples.graph.DCMST;
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropTreeHeldKarp2DON_Grid extends Propagator implements HeldKarp {

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
	double BIG_M = 0;
	double[][] maxRC;
	IStateDouble[][] maxRC_stored;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropTreeHeldKarp2DON_Grid(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
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
	public static PropTreeHeldKarp2DON_Grid mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropTreeHeldKarp2DON_Grid phk = new PropTreeHeldKarp2DON_Grid(graph,cost,maxDegree,costMatrix,constraint,solver);
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
		BIG_M = obj.getUB();
		preprocessOneNodes();
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
					if(hasOffSet(i)){
						costs[i][j] += BIG_M;
					}
					if(hasOffSet(j)){
						costs[i][j] += BIG_M;
					}
					costs[j][i] = costs[i][j];
				}
			}
		}
		double kone = 0;
		for(int i=0;i<n;i++){
			if(hasOffSet(i))
				kone+=maxDegree[i];
		}
		totalPenalities+= kone*BIG_M;
		HK_Pascals();
	}

	BitSet oneNode;
	int[] counter;
	boolean onlyOne = false;

	private boolean hasOffSet(int i){
		if(onlyOne){
			return maxDegree[i]==1;
		}else{
			return oneNode.get(i);
		}
	}

	private void preprocessOneNodes() throws ContradictionException {
		if(onlyOne)return;
		if(oneNode==null){
			oneNode = new BitSet(n);
			counter = new int[n];
		}
		INeighbors nei;
		oneNode.clear();
		for(int i = 0;i<n;i++){
			counter[i] = 0;
		}
		LinkedList<Integer> list = new LinkedList<Integer>();
		for(int first = 0;first<n;first++){
			if(maxDegree[first]==1 && !oneNode.get(first)){
				int k=first;
				list.add(k);
				while(!list.isEmpty()){
					k = list.removeFirst();
					oneNode.set(k);
					nei = g.getKernelGraph().getSuccessorsOf(k);
					for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
//						if(g.getKernelGraph().getSuccessorsOf(s).neighborhoodSize()
//								< g.getEnvelopGraph().getSuccessorsOf(s).neighborhoodSize())
						if(!oneNode.get(s)){
							counter[s]++;
							if(counter[s]>maxDegree[s]){
								throw new UnsupportedOperationException();
								// contradiction mais devrait deja avoir ete capturee
							}
							if(counter[s]==maxDegree[s]){
								int ctt=0;
								for(int z=0;z<n;z++){
									ctt+=g.getKernelGraph().getSuccessorsOf(z).neighborhoodSize();
								}
								ctt/=2;
								System.out.println(ctt+" edges in k / "+n);
								if(ctt!=n-1)
									throw new UnsupportedOperationException();
								// contradiction mais devrait deja avoir ete capturee
							}
							if(counter[s]==maxDegree[s]-1){
								oneNode.set(s);
								list.addLast(s);
							}
						}
					}
				}
			}
		}
	}

	protected void HK_Pascals() throws ContradictionException {
		filterRecord();
		if(firstPropag){
			convergeAndFilter();
			firstPropag = false;
		}
//		filterOnCuts();
//		filterOnCutsRem();
		fastRun(2);
		fastRun(0.5);
		fastRun(0.125);
//		filterOnCuts();
//		filterOnCutsRem();
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
//		System.out.println(hkb);

	}

	protected void convergeAndFilter() throws ContradictionException {
		double hkb = 0;
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
			rec(hkb);
		}
//		if(firstPropag){
//			HKfilter.performPruning(DCMST.optimum+0.001 + totalPenalities);
//			findAllCuts(hkb);
//			findAllCutsRem(hkb);
//		}
	}

	private void rec(double hkb) {
		if(maxRC==null){
			maxRC = new double[n][n];
			maxRC_stored = new IStateDouble[n][n];
			for(int i=0;i<n;i++){
				INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					maxRC_stored[i][j] = environment.makeFloat(0);
				}
			}
		}
		if(solver.getMeasures().timestamp()==0)
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !mst.edgeExists(i,j))
					if(getMarginalCost(i,j)+hkb>maxRC[i][j]){
						maxRC[i][j] = getMarginalCost(i,j)+hkb;
					}
			}
		}
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && !mst.edgeExists(i,j))
					if(getMarginalCost(i,j)+hkb>maxRC_stored[i][j].get()){
						maxRC_stored[i][j].set(getMarginalCost(i,j)+hkb);
					}
			}
		}
	}

//	private void rec(double hkb) {
//		if(maxRC==null){
//			maxRC = new double[n][n];
//			maxRC_stored = new IStateDouble[n][n];
//			for(int i=0;i<n;i++){
//				INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(i<j){
//						if(!mst.edgeExists(i,j)){
//							maxRC[i][j] = getMarginalCost(i,j) + hkb;
//							maxRC_stored[i][j] = environment.makeFloat(maxRC[i][j]);
//						}else{
//							maxRC_stored[i][j] = environment.makeFloat(0);
//						}
//					}
//				}
//			}
//		}
//		for(int i=0;i<n;i++){
//			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
//			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//				if(i<j && !mst.edgeExists(i,j))
//					if(getMarginalCost(i,j)+hkb>maxRC[i][j]){
//						if(solver.getMeasures().timestamp()==0)
//						maxRC[i][j] = getMarginalCost(i,j)+hkb;
//						maxRC_stored[i][j].set(getMarginalCost(i,j)+hkb);
//					}
//			}
//		}
//	}

	protected void convergeFast(double alpha) throws ContradictionException {
		double besthkb = 0;
		double oldhkb = -20;
		double hkb = -1;
		while(oldhkb+0.1<besthkb){
			oldhkb = besthkb;
			for(int i=0;i<nbSprints;i++){
				HK.computeMST(costs,g.getEnvelopGraph());
				mst = HK.getMST();
				hkb = HK.getBound()-totalPenalities;
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
			if(firstPropag){
				HKfilter.computeMST(costs,g.getEnvelopGraph());
				hkb = HKfilter.getBound()-totalPenalities;
				mst = HKfilter.getMST();
				if(hkb-Math.floor(hkb)<0.00001){hkb = Math.floor(hkb);}
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
				rec(hkb);
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
//		for(int i=0;i<n;i++){
//			deg = mst.getNeighborsOf(i).neighborhoodSize();
//			if(hasOffSet(i) && deg!=maxDegree[i] && BIG_M>=obj.getUB()){
//				System.out.println(maxDegree[i]+" =/= "+deg);
//			}
//		}
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			if(deg>maxDegree[i] || penalities[i]>0){
//				if(hasOffSet(i) && deg!=maxDegree[i] && BIG_M>=obj.getUB()){
//					System.out.println(hkb+" hkb");
//					System.out.println(totalPenalities+" totpen");
//					System.out.println("node "+i+" dm "+maxDegree[i]);
//					System.out.println(g.getKernelGraph().getSuccessorsOf(i));
//					System.out.println(mst.getSuccessorsOf(i));
//					INeighbors ne = mst.getSuccessorsOf(i);
//					for(int j=ne.getFirstElement();j>=0;j=ne.getNextElement()){
//						System.out.println(costs[i][j] +" cost");
//					}
//					int ct = 0;
//					for(int j=0;j<n;j++){
//						ct += g.getKernelGraph().getSuccessorsOf(j).neighborhoodSize();
//					}
//					ct/=2;
//					System.out.println(ct+" edges in k");
//					if(ct==n-1){
//						System.out.println("solvedd");
//					}
//					throw new UnsupportedOperationException();
////					contradiction();
//				}
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
		preprocessOneNodes();
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					if(hasOffSet(i)){
						costs[i][j] += BIG_M;
					}
					if(hasOffSet(j)){
						costs[i][j] += BIG_M;
					}
					costs[j][i] = costs[i][j];
				}
			}
//			totalPenalities += coefNode[i]*LDS_PEN_NODE*DCMST_lds.varsLDS[i].getValue();
		}
		double kone = 0;
		for(int i=0;i<n;i++){
			if(hasOffSet(i))
				kone+=maxDegree[i];
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

	public void filterRecord() throws ContradictionException {
		if(maxRC!=null){
			double ub = obj.getUB();
			for(int i=0;i<n;i++){
				INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(i<j && !g.getKernelGraph().edgeExists(i,j)){
						if(maxRC[i][j]>ub+0.001){
							g.removeArc(i,j,this);
						}
					else if(maxRC_stored[i][j].get()>ub+0.001){
						g.removeArc(i,j,this);
					}
					}
				}
			}
		}
	}


	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	private void findAllCuts(double lb){
		if(cutsEnf==null){
			cutsEnf = new ArrayList<int[]>();
			lhsEnf = new TDoubleArrayList();
		}
		TIntArrayList possibleBasisF = new TIntArrayList(n);
		TIntArrayList possibleBasisT = new TIntArrayList(n);
		TDoubleArrayList possibleBasisDelta = new TDoubleArrayList(n);
		double ub = DCMST.optimum;
		double maxRC = 0;
		for(int i=0;i<n;i++){
			INeighbors nei = mst.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && getReplacementCost(i,j)>0 && !g.getKernelGraph().edgeExists(i, j)){
					if(getReplacementCost(i,j)>maxRC){
						maxRC = getReplacementCost(i,j);
					}
				}
			}
		}
		double ubmin = obj.getLB();
		for(int i=0;i<n;i++){
			INeighbors nei = mst.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(lb+maxRC+getReplacementCost(i,j)>=ubmin)
					if(getReplacementCost(i,j)+lb<=ub) // otherwise redundant with basic filtering
						if(i<j && getReplacementCost(i,j)>0 && !g.getKernelGraph().edgeExists(i,j)){
							possibleBasisF.add(i);
							possibleBasisT.add(j);
							possibleBasisDelta.add(getReplacementCost(i,j));
						}
			}
		}
		int s = possibleBasisDelta.size();
		for(int k=0;k<s;k++){
			int i = possibleBasisF.get(k);
			int j = possibleBasisT.get(k);
			double rpij = possibleBasisDelta.get(k);
			for(int l=k+1;l<s;l++){
				int u = possibleBasisF.get(l);
				int v = possibleBasisT.get(l);
				double rpuv = possibleBasisDelta.get(l);
				int[] cut = new int[]{i,j,u,v};
				if(!alreadyIn(cut,cutsEnf)){
					cutsEnf.add(cut);
					lhsEnf.add(rpij+rpuv+lb);
				}
			}
		}
		System.out.println(cutsEnf.size()+" cuts");
	}

	private boolean alreadyIn(int[] cut,ArrayList<int[]> set) {
//		for(int[] c:set){
//			if(cut[0]==c[0] && cut[1]==c[1] && cut[2]==c[2] && cut[3]==c[3]){
//				return true;
//			}
//		}
		return false;
	}

	private void filterOnCuts() throws ContradictionException {
		if(cutsEnf!=null){
			int s = cutsEnf.size();
			double ub = obj.getUB()+0.001;
			for(int k=0;k<s;k++){
				int[] nodes = cutsEnf.get(k);
				if(lhsEnf.get(k)>ub){
					if(!g.getEnvelopGraph().edgeExists(nodes[0],nodes[1])){
						g.enforceArc(nodes[2],nodes[3],this);
					}
					if(!g.getEnvelopGraph().edgeExists(nodes[2],nodes[3])){
						g.enforceArc(nodes[0],nodes[1],this);
					}
				}
			}
		}
	}

	ArrayList<int[]> cutsEnf;
	TDoubleArrayList lhsEnf,lhsRem;

	private void findAllCutsRem(double lb){
		if(cutsRem==null){
			cutsRem = new ArrayList<int[]>();
			lhsRem = new TDoubleArrayList();
		}
		TIntArrayList possibleBasisF = new TIntArrayList(n);
		TIntArrayList possibleBasisT = new TIntArrayList(n);
		TDoubleArrayList possibleBasisDelta = new TDoubleArrayList(n);
		double maxRC = 0;
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && getReplacementCost(i,j)>0 && !mst.edgeExists(i, j)){
					if(getReplacementCost(i,j)>maxRC){
						maxRC = getReplacementCost(i,j);
					}
				}
			}
		}
		double ub = DCMST.optimum;
		double ubmin = obj.getLB();
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(getReplacementCost(i,j)+maxRC+lb>ubmin)
					if(getReplacementCost(i,j)+lb<=ub)
						if(i<j && getReplacementCost(i,j)>0 && !mst.edgeExists(i, j)){
							possibleBasisF.add(i);
							possibleBasisT.add(j);
							possibleBasisDelta.add(getReplacementCost(i,j));
						}
			}
		}
		int s = possibleBasisDelta.size();
//		System.out.println(s+" sizepos");
		for(int k=0;k<s;k++){
			int i = possibleBasisF.get(k);
			int j = possibleBasisT.get(k);
			double rpij = possibleBasisDelta.get(k);
			for(int l=k+1;l<s;l++){
				int u = possibleBasisF.get(l);
				int v = possibleBasisT.get(l);
				double rpuv = possibleBasisDelta.get(l);
//				if(rpij+rpuv+lb>ub+0.0001){
				int[] cut = new int[]{i,j,u,v};
				if(!alreadyIn(cut, cutsRem)){
					cutsRem.add(cut);
					lhsRem.add(rpij+rpuv+lb);
				}
			}
		}
		System.out.println(cutsRem.size()+" cutsRem");
	}

	private void filterOnCutsRem() throws ContradictionException {
		if(cutsRem==null)return;
		int s = cutsRem.size();
		double ub = obj.getUB()+0.001;
		for(int k=0;k<s;k++){
			int[] nodes = cutsRem.get(k);
			if(lhsRem.get(k)>ub){
				if(g.getKernelGraph().edgeExists(nodes[0],nodes[1])){
					g.removeArc(nodes[2], nodes[3], this);
				}
				if(g.getKernelGraph().edgeExists(nodes[2],nodes[3])){
					g.removeArc(nodes[0], nodes[1], this);
				}
			}
		}
//		System.exit(0);
	}

	ArrayList<int[]> cutsRem;

//	private void findAllCutsNaive(double lb){
//		double minoutcost = obj.getUB();
//		for(int i=0;i<n;i++){
//			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
//			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//				if(i<j && !mst.edgeExists(i, j)){
//					if(costs[i][j]<minoutcost){
//						minoutcost = costs[i][j];
//					}
//				}
//			}
//		}
//		if(cutsEnf==null){
//			cutsEnf = new ArrayList<int[]>();
//		}
//		TIntArrayList possibleBasisF = new TIntArrayList();
//		TIntArrayList possibleBasisT = new TIntArrayList();
//		TDoubleArrayList possibleBasisDelta = new TDoubleArrayList();
//		double sumDelta = 0;
//		for(int i=0;i<n;i++){
//			INeighbors nei = mst.getSuccessorsOf(i);
//			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//				if(i<j && minoutcost>costs[i][j] && !g.getKernelGraph().edgeExists(i,j)){
//					possibleBasisF.add(i);
//					possibleBasisT.add(j);
//					possibleBasisDelta.add(minoutcost-costs[i][j]);
//				}
//			}
//		}
//		double ub = DCMST.optimum;
//		int nbCuts = possibleBasisF.size();
//		for(int c=0;c<nbCuts;c++){
//			double delta = possibleBasisDelta.get(c);
//			for(int i=0;i<n;i++){
//				INeighbors nei = mst.getSuccessorsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(i<j && !g.getKernelGraph().edgeExists(i,j)){
//						// pas dans c
//						if(i-2*n*j!=possibleBasisF.get(c)-2*n*possibleBasisT.get(c))
//							if(getReplacementCost(i,j)+delta+lb>ub+0.001){
//								//add
//								int[] cut = new int[]{possibleBasisF.get(c),possibleBasisT.get(c),i,j};
//								if(!alreadyIn(cut,cutsEnf)){
//									cutsEnf.add(cut);
//								}
//							}
////					System.out.println("delta : " +(int)(mincost-costs[i][j]));
////					System.out.println((int)getReplacementCost(i,j)+" / "+(int)(rlb+getReplacementCost(i,j)-12645));
//					}
//				}
//			}
//		}
//		System.out.println(cutsEnf.size()+" cuts");
////		System.exit(0);
//	}

}