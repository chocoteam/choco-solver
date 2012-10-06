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
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Random;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropTreeHeldKarpLMR extends Propagator implements HeldKarp {

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
	private boolean activeCut;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropTreeHeldKarpLMR(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
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
		cuts = new ArrayList<Cut>();
	}

	/** ONE TREE based HK */
	public static PropTreeHeldKarpLMR mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropTreeHeldKarpLMR phk = new PropTreeHeldKarpLMR(graph,cost,maxDegree,costMatrix,constraint,solver);
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
	}

	private long nbSols = 0;
	private int objUB = -1;

//	TIntArrayList fr,tr,fe,te;

	protected void HK_Pascals() throws ContradictionException {
//		nbSprints = 100;
		if(firstPropag){
			objUB = obj.getUB();
			convergeAndFilter();
			firstPropag = false;
		}
		fastRun();
//		if(solver.getSearchLoop().getMeasures().timestamp()==0){
//			fastRun();
//			fastRun();
//			fastRun();
//		}
	}

	protected void fastRun() throws ContradictionException {
		convergeFast(2);
		HKfilter.computeMST(costs,g.getEnvelopGraph());
		double hkb = HKfilter.getBound()-totalPenalities;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
		obj.updateLowerBound((int) Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
		findCycles(hkb);
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
			obj.updateLowerBound((int) Math.ceil(hkb), this);
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
				nb2viol += (maxDegree[i]-deg)*(maxDegree[i]-deg);
			}
		}
		for(Cut c:cuts){
			nb2viol+=c.getViol();
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
		for(Cut c:cuts){
			if(c.getViol()>0){
				throw new UnsupportedOperationException();
			}
			c.multiplier += (c.getViol()*2-1)*step;
			c.multiplier = Math.max(c.multiplier,0);
			totalPenalities += c.multiplier*(c.from.size()-1);
			if(c.multiplier>0){
				System.out.println("youhou");
				throw new UnsupportedOperationException("it works!");
			}
		}
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
		for(Cut c:cuts){
			for(int k=c.from.size()-1;k>=0;k--){
				int i = c.from.get(k);
				int j = c.to.get(k);
				costs[i][j] += c.multiplier;
				costs[j][i] = costs[i][j];
			}
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
		g.removeArc(from,to,this);
		nbRem++;
	}
	public void enforce(int from, int to) throws ContradictionException {
		g.enforceArc(from, to, this);
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
//		throw new UnsupportedOperationException();
		return HKfilter.getRepCost(from,to);
	}

	public double getMarginalCost(int from, int to){
//		throw new UnsupportedOperationException();
		return HKfilter.getRepCost(from,to);
	}

	//***********************************************************************************
	// CYCLES REASONING
	//***********************************************************************************

	BitSet visited;
	int[] parent;
	TIntArrayList secondPath;
	UndirectedGraph cyclesSupport;
	ArrayList<Cut> cuts;
	
	private void findCycles(double hkb){
		if(solver.getSearchLoop().getMeasures().timestamp()!=0){
			return;
		}
//		if(visited!=null){
//			return;
//		}
		if(visited==null){
			visited = new BitSet(n);
			parent = new int[n];
			secondPath = new TIntArrayList();
		}
		prepareCyclesDetection();
		cyclesSupport = new UndirectedGraph(n, GraphType.LINKED_LIST);
		for(int i=0;i<n;i++){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			if(nei.neighborhoodSize()>1)
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j && g.getEnvelopGraph().getNeighborsOf(j).neighborhoodSize()>1){
					cyclesSupport.addEdge(i, j);
				}
			}
		}
		reduceSupport();
		do{
		Cut cut = new Cut();
		int wf,wt;
		double wrc;
		do{
			wf=-1;
			wt=-1;
			wrc = 0;
			for(int i=0;i<n;i++){
				INeighbors nei = cyclesSupport.getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(i<j && !mst.edgeExists(i,j))
						if(getMarginalCost(i,j)>wrc){
							wrc = getMarginalCost(i,j);
							wf = i;
							wt = j;
						}
				}
			}
			cut.add(wf,wt,wrc);
			TIntArrayList c = findCycle(wf,wt);
			cyclesSupport.removeEdge(wf,wt);
			int s = c.size()-1;
			for(int i=0;i<s;i++){
				cyclesSupport.removeEdge(c.get(i),c.get(i+1));
			}
			reduceSupport();
			if(cut.cost+hkb>obj.getUB()){
				cuts.add(cut);
			}
//			System.out.println(wf+" : "+wt+" : "+wrc);
		}while(wf!=-1 && hkb+cut.cost<=obj.getUB() && cyclesSupport.getActiveNodes().neighborhoodSize()>0);
		System.out.println((int)(hkb+cut.cost-obj.getUB())+" from filtering // "+cut.from.size());
			System.out.println((int)Math.ceil(hkb));
//			System.exit(0);
		if(cut.from.size()>=10)break;
		}while(cyclesSupport.getActiveNodes().neighborhoodSize()>0);
		System.out.println("///////");
//		System.out.println(hkb);
//		System.exit(0);
	}

	private void reduceSupport() {
		boolean modified = true;
		IActiveNodes a = cyclesSupport.getActiveNodes();
		while (modified){
			modified = false;
			for(int i=a.getFirstElement();i>=0;i=a.getNextElement()){
				if(cyclesSupport.getNeighborsOf(i).neighborhoodSize()<2){
					modified = true;
					cyclesSupport.desactivateNode(i);
				}
			}
		}
	}

	private TIntArrayList findCycle(int from, int to) {
		int link = -1;
		int x = from;
		visited.clear();
		visited.set(x);
		while(parent[x]!=x){
			x = parent[x];
			visited.set(x);
			if(x==to){
				link = to;
				break;
			}
		}
		secondPath.clear();
		if(x!=to){
			x = to;
			visited.set(x);
			while(parent[x]!=x){
				secondPath.add(x);
				x = parent[x];
				if(visited.get(x)){
					link = x;
					break;
				}
			}
		}
		if(link==-1){
			throw new UnsupportedOperationException();
		}
		TIntArrayList cycle = new TIntArrayList();
		x = from;
		while(parent[x]!=x){
			cycle.add(x);
			x = parent[x];
			if(x==link){
				break;
			}
		}
		for(int k=secondPath.size()-1;k>=0;k--){
			cycle.add(secondPath.get(k));
		}
		return cycle;
	}

	protected void prepareCyclesDetection(){
		INeighbors nei;
		int first = 0;
		for(int i=0;i<n;i++){
			parent[i] = -1;
			if(mst.getNeighborsOf(i).neighborhoodSize()>mst.getNeighborsOf(first).neighborhoodSize()){
				first = i;
			}
		}
		int k=first;
		visited.clear();
		visited.set(first);
		parent[first]=first;
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(k);
		while(!list.isEmpty()){
			k = list.removeFirst();
			nei = mst.getSuccessorsOf(k);
			for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(parent[s]==-1){
					parent[s] = k;
					if(!visited.get(s)){
						list.addLast(s);
						visited.set(s);
					}
				}
			}
		}
	}

	private class Cut{
		TIntArrayList from,to;
		double cost,multiplier;
		public Cut(){
			from = new TIntArrayList();
			to = new TIntArrayList();
		}
		public void add(int i, int j, double rc){
			from.add(i);
			to.add(j);
			cost += rc;
		}

		public double getViol() {
			for(int k=from.size()-1;k>=0;k--){
				if(!mst.edgeExists(from.get(k),to.get(k))){
					return 0;
				}
			}
			return 1;
		}
	}
}