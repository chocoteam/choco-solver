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
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropBITreeHeldKarp extends Propagator implements HeldKarp {

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
	double[] deg_penalities;
	double deg_totalPenalities;
	double[] bi_penalities;
	double bi_totalPenalities;
	BitSet inBI;
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
	ArrayList<Blossom> blossoms;
	private boolean activeBI;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropBITreeHeldKarp(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		super(new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		originalCosts = costMatrix;
		costs = new double[n][n];
		deg_penalities = new double[n];
		deg_totalPenalities = 0;
		bi_penalities = new double[n];
		bi_totalPenalities = 0;
		penalities = new double[n];
		totalPenalities = 0;
		inBI = new BitSet(n);
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
		nbSprints = 30;
		this.maxDegree = maxDegree;
		rd = new Random(0);
		blossoms = new ArrayList<Blossom>();
	}

	/** ONE TREE based HK */
	public static PropBITreeHeldKarp mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropBITreeHeldKarp phk = new PropBITreeHeldKarp(graph,cost,maxDegree,costMatrix,constraint,solver);
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
		blossoms.clear();
		inBI.clear();
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
					costs[j][i] = costs[i][j] = originalCosts[i][j] + deg_penalities[i] + deg_penalities[j];
					if(costs[i][j]<0){
						throw new UnsupportedOperationException();
					}
				}
			}
		}
		totalPenalities = deg_totalPenalities;
		HK_Pascals();
	}

	private long nbSols = 0;
	private int objUB = -1;

//	TIntArrayList fr,tr,fe,te;

	protected void HK_Pascals() throws ContradictionException {
		// WHEN OPTIMUM IS NOT GIVEN
//		if(fr==null){
//			fr = new TIntArrayList();
//			tr = new TIntArrayList();
//			fe = new TIntArrayList();
//			te = new TIntArrayList();
//		}
//		fr.clear();
//		tr.clear();
//		fe.clear();
//		te.clear();
		nbSprints = 30;
		if(nbSols!=solver.getMeasures().getSolutionCount()
				|| obj.getUB()<objUB
				|| (firstPropag && !waitFirstSol)){
			nbSprints = 100;
			nbSols = solver.getMeasures().getSolutionCount();
			objUB = obj.getUB();
			activeBI = false;
			convergeAndFilter();
			System.out.println("%%%");
			activeBI = true;
			convergeAndFilter();
			firstPropag = false;
		}else{
			fastRun();
//			convergeAndFilter();
//			convergeAndFilter();
		}
//		int s = tr.size();
//		for(int i=0;i<s;i++){
//			g.removeArc(fr.get(i),tr.get(i),this);
//		}
//		fr.clear();
//		tr.clear();
//		s = te.size();
//		for(int i=0;i<s;i++){
//			g.enforceArc(fe.get(i),te.get(i),this);
//		}
//		fe.clear();
//		te.clear();
	}

	protected void fastRun() throws ContradictionException {
		nbSprints = 30;
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
		while(oldhkb+0.0001<besthkb || alpha>0.01){
			oldhkb = besthkb;
			convergeFast(alpha);
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities;
			if(hkb>besthkb){
				besthkb = hkb;
			}
			System.out.println(hkb);
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
		target=hkb*1.01;
		if(target-hkb<0){
			throw new UnsupportedOperationException();
		}
		if(target-hkb<0.001){
			target = hkb+0.001;
		}
		int deg;
		boolean found = true;
//		inBI.clear();
//		blossoms.clear();
		TIntArrayList tolook = new TIntArrayList();
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			if(deg>maxDegree[i]){
				found = false;
				if(activeBI)
				if(!inBI.get(i))
				tolook.add(i);
			}
			nb2viol += (maxDegree[i]-deg)*(maxDegree[i]-deg);
		}
		for(int k=tolook.size()-1;k>=0;k--){
			int i = tolook.get(k);
			if(!inBI.get(i))
			if(blossoms.size()<n){
				Blossom b = new Blossom(i);
				blossoms.add(b);
			}
		}
		double totalBI = 0;
		for(Blossom b:blossoms){
			b.recompute();
			totalBI+=b.getViol()*b.getViol();
		}
		nb2viol += totalBI;
		if(found){
			return true;
		}else{
			step = alpha*(target-hkb)/nb2viol;
		}
		if(step<0.0001){
			return true;
		}

		bi_totalPenalities = 0;
		double stepBI = step;
		if(step<0){
			throw new UnsupportedOperationException();
		}
//		if(stepBI>1000){
//			System.out.println("stepBI = "+stepBI + " >100; totalBI:"+totalBI);
//			System.exit(0);
//			stepBI = 1000;
//		}
		for(Blossom b:blossoms){
			b.addPen(b.getViol()*stepBI);
			if(b.getPen()>=Integer.MAX_VALUE) {
				System.out.println(stepBI);
				System.out.println("OVEr "+totalPenalities);
				throw new UnsupportedOperationException();
			}
			if(b.active()){
				bi_totalPenalities += b.getMaxPen();
			}
		}
		deg_totalPenalities = 0;
		double maxPen = 2*obj.getUB();
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
//			double pen = (deg-maxDegree[i])*step;
//			if(alpha<=2 && firstPropag){
//				if(pen<0)pen = - pen;
//				pen *= (deg-maxDegree[i]);
//				pen *= (deg-maxDegree[i])*(deg-maxDegree[i]);
//			}
//			deg_penalities[i] += pen;
			deg_penalities[i] += (deg-maxDegree[i])*step;
			if(deg_penalities[i]<0 || g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize() <= maxDegree[i]){
				deg_penalities[i] = 0;
			}
			if(deg_penalities[i]>maxPen){
				deg_penalities[i] = maxPen;
			}
			if(deg_penalities[i]>Double.MAX_VALUE/(n-1) || deg_penalities[i]<0){
				throw new UnsupportedOperationException();
			}
			deg_totalPenalities += deg_penalities[i]*maxDegree[i];
		}
//		System.out.println(deg_totalPenalities+" / "+bi_totalPenalities);
		totalPenalities = deg_totalPenalities + bi_totalPenalities;
		if(totalPenalities>Double.MAX_VALUE/(n-1) || totalPenalities<0){
			throw new UnsupportedOperationException(totalPenalities+" - "+totalPenalities);
		}
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + deg_penalities[i] + deg_penalities[j];
					for(Blossom b:blossoms){
						if(b.active()){
							if(b.gT.arcExists(i,j)){// || (b.in.get(i) && b.in.get(j) && mst.edgeExists(i,j))){
								costs[i][j] += b.getPen();
							}
//							if(b.in.get(i) && b.in.get(j) && mst.edgeExists(i,j)){
//								costs[i][j] += b.getPen();
//							}
						}
					}
					costs[j][i] = costs[i][j];
				}
			}
		}
		return false;
	}

	private class Blossom{
		UndirectedGraph gT;
		BitSet outlink;
		int b;
		// arc T, arc T dans MST, arcs dans MST inter NOEUDS de gT
		int T,xT,xEH;
		BitSet in;
		double pen;
//		TIntArrayList fromIn,toOut;

		Blossom(int root){
			b = T = xT = 0;
			xEH = -1;
//			fromIn = new TIntArrayList();
//			toOut = new TIntArrayList();
			in = new BitSet(n);
			outlink = new BitSet(n);
			gT = new UndirectedGraph(n, GraphType.LINKED_LIST);
			gT.getActiveNodes().clear();
			addNode(root);
			inBI.set(root);
//			System.out.println("ENDFIRST");
			while(addNext() && xEH<10){
//				System.out.println("ADDED");
//				int tx2 = xT;
//				System.out.println(getViol() + " avant");
//				recompute();
//				System.out.println(getViol() + " apres");
//				if(tx2!=xT){
//					System.out.println("in " + in);
//					System.exit(0);
//				}
			}
//			System.out.println(getViol());
			recompute();
//			System.out.println("rec");
//			System.out.println(getViol());
//			System.exit(0);
		}

		void addNode(int node){
			if(gT.getNeighborsOf(node).neighborhoodSize()>0){
				T-=gT.getNeighborsOf(node).neighborhoodSize();
				INeighbors nei = mst.getNeighborsOf(node);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(gT.edgeExists(node,j)){
//						System.out.println("REM "+node+"-"+j);
						xT--;
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(in.get(j)){
//						System.out.println("ADD "+node+"-"+j);
						xT++;
						gT.addEdge(node,j);
						break;
					}
				}
				gT.desactivateNode(node);
			}
			gT.activateNode(node);
			inBI.set(node);
			outlink.clear(node);
			in.set(node);
			xEH++;
			b += maxDegree[node];
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!in.get(j)){
//					outlink.set(j);
					boolean  addit = true;
					if(addit){
						if(mst.edgeExists(node,j)){
							xT++;
//							System.out.println(node+"-"+j);
							T++;
							gT.addEdge(node, j);
							outlink.set(j);
						}
					}
				}
			}
//			if(!odd())//add one edge
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(!gT.edgeExists(node,j)){
//						T++;
////							System.out.println(node+"-"+j);
//						gT.addEdge(node,j);
//						return;
//					}
//				}
//			if(!odd())//add one edge
//			for(int i=gT.getActiveNodes().getFirstElement();i>=0;i=gT.getActiveNodes().getNextElement()){
//				nei = g.getEnvelopGraph().getSuccessorsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(!gT.edgeExists(node,j)){
//						T++;
////							System.out.println(node+"-"+j);
//						gT.addEdge(node,j);
//						return;
//					}
//				}
//			}
			if(!odd()){//remove one edge
				for(int i=gT.getActiveNodes().getFirstElement();i>=0;i=gT.getActiveNodes().getNextElement()){
					nei = gT.getSuccessorsOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!in.get(j)){
							T--;
//							System.out.println("REM "+node+"-"+j);
							if(mst.edgeExists(i,j)){
								xT--;
							}
							gT.removeEdge(node,j);
							return;
						}
					}
				}
			}
			if(!odd())//add one edge
			for(int i=gT.getActiveNodes().getFirstElement();i>=0;i=gT.getActiveNodes().getNextElement()){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!gT.edgeExists(node,j)){
						T++;
//							System.out.println(node+"-"+j);
						gT.addEdge(node,j);
						return;
					}
				}
			}
			if(!odd()){
//				System.out.println("care");
//				System.out.println(gT);
				throw new UnsupportedOperationException();
			}
		}

		private boolean odd() {
			return (b+T)%2==1;
		}

		private int eval(int node){
			int xe2 = xEH+1;
			int xT2 = xT;
			int T2=T;
			int b2=b+maxDegree[node];
			INeighbors nei = mst.getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!in.get(j)){
					T2++;
					xT2++;
				}
			}
			return 2*(xe2+xT2)-(b2+T2);
		}

		private boolean addNext(){
			int max = 2*(xEH+xT)-(b+T);
			int next = -1;
			for(int i=outlink.nextSetBit(0);i>=0 && i<n;i=outlink.nextSetBit(i+1)){
				int f = eval(i);
				if(f>=max ){//&& !inBI.get(i)){
					max = f;
					next = i;
				}
			}
			if(next==-1){
				return false;
			}
			addNode(next);
			return true;
		}

		private double getViol(){
			double v = 2*(xEH+xT)-(b+T);
			return v/2;
		}

		private void setPen(double p){
			pen = p;
		}

		private void addPen(double p){
			pen += p;
			pen = Math.max(pen, 0);
		}

		private double getPen(){
			return pen;
		}

		private double getMaxPen(){
			double p = b+T;
			return p*pen/2;
		}

		private boolean active() {
//			int ct = 0;
//			for(int i=in.nextSetBit(0);i>=0&&i<n;i=in.nextSetBit(i+1)){
//				ct+=gT.getNeighborsOf(i).neighborhoodSize();
//			}
//			if(ct==0){
//				throw new UnsupportedOperationException();
//			}
			return (b+T)%2==1 && getPen()>0 && xEH>=2;
		}

		public void recompute() {
			xT = 0;
			xEH = 0;
			for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
				INeighbors nei = mst.getNeighborsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(in.get(j)){
						if(i<j)
						xEH++;
					}else{
						if(gT.edgeExists(i,j)){
							xT++;
						}
					}
				}
			}
		}
	}

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
		System.out.println("%%%%%");
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
		throw new UnsupportedOperationException();
		//return HKfilter.getRepCost(from,to);
	}
}