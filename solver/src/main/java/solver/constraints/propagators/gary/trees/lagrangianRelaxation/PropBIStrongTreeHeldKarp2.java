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
public class PropBIStrongTreeHeldKarp2 extends Propagator implements HeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected UndirectedGraphVar g;
	protected IntVar obj;
	protected int n;
	protected int[][] originalCosts;
	protected double[][] costs;
	double totalPenalities;
	double[] deg_penalities;
	double deg_totalPenalities;
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
	ArrayList<Blossom> blossoms;
	private boolean activeBI;
	Random rd = new Random(0);

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropBIStrongTreeHeldKarp2(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		super(new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		originalCosts = costMatrix;
		costs = new double[n][n];
		deg_penalities = new double[n];
		deg_totalPenalities = 0;
		bi_totalPenalities = 0;
		totalPenalities = 0;
		inBI = new BitSet(n);
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
		nbSprints = 30;
		this.maxDegree = maxDegree;
		blossoms = new ArrayList<Blossom>();
	}

	/** ONE TREE based HK */
	public static PropBIStrongTreeHeldKarp2 mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropBIStrongTreeHeldKarp2 phk = new PropBIStrongTreeHeldKarp2(graph,cost,maxDegree,costMatrix,constraint,solver);
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
		deg_totalPenalities = 0;
		for(int i=0;i<n;i++){
			deg_totalPenalities += deg_penalities[i]*maxDegree[i];
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

	private double objUB = -1;
	private boolean firstPropag = true;
	protected void HK_Pascals() throws ContradictionException {
//		nbSprints = 30;
//		nbSprints = 100;
//		blossoms.clear();
//		inBI.clear();
//		activeBI = false;
//		convergeAndFilter();
//		convergeFast(2);
//		System.out.println("%%%");
//		activeBI = true;
//		convergeAndFilter();
//		System.out.println(obj+" : "+blossoms.size()+" : "+bi_totalPenalities);
//		System.exit(0);

		nbSprints = 30;
		if(firstPropag){
			activeBI = false;
			firstPropag = false;
			convergeAndFilter();
//			activeBI = true;
		}
		fastRun();
//		convergeAndFilter();
//		if(obj.getUB()!=objUB && obj.instantiated()){
//			System.out.println(obj.getUB());
//			System.exit(0);
//			objUB = obj.getUB();
//			convergeAndFilter();
//		}else{
//			fastRun();
//		}
//		System.out.println(obj+" : "+blossoms.size()+" : "+bi_totalPenalities);
	}

	protected void fastRun() throws ContradictionException {
			convergeFast(2);
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			double hkb = HKfilter.getBound()-totalPenalities;
			mst = HKfilter.getMST();
			if(hkb-Math.floor(hkb)<0.001){hkb = Math.floor(hkb);}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
//		System.out.println(hkb+"    /    "+bi_totalPenalities);
//			System.out.println(hkb+"    /    "+blossoms.size());
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
			if(activeBI){
			System.out.println(hkb+"    /    "+bi_totalPenalities);
			System.out.println(hkb+"    /    "+blossoms.size());
//			System.exit(0);
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
				if(updateStep(hkb, alpha)){
					return;
				}
			}
//			HKfilter.computeMST(costs,g.getEnvelopGraph());
//			double hkb = HKfilter.getBound()-totalPenalities;
//			if(hkb>besthkb){
//				besthkb = hkb;
//			}
//			if(activeBI){
////			System.out.println(hkb+"    /    "+bi_totalPenalities);
//			}
//			mst = HKfilter.getMST();
//			if(hkb-Math.floor(hkb)<0.00001){hkb = Math.floor(hkb);}
//			obj.updateLowerBound((int)Math.ceil(hkb), this);
//			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
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
			if (deg_penalities[i]>0 || deg>maxDegree[i]){
				found = false;
			}
//			if (activeBI && deg>maxDegree[i] && blossoms.size()<n*n){
			if (activeBI && deg>maxDegree[i] && blossoms.size()<n){
//			if (activeBI && deg>maxDegree[i] && blossoms.size()<n && !inBI.get(i)){
				Blossom b = new Blossom(i);
				if(b.isValid()){
					inBI.set(i);
					blossoms.add(b);
				}
				else{
//					throw new UnsupportedOperationException();
				}
			}
//			if(deg_penalities[i]>0 || deg>maxDegree[i])//optim Beasley 1993
			nb2viol += (maxDegree[i]-deg)*(maxDegree[i]-deg);
		}
		if(activeBI){// find BI
			for(int i=0;i<blossoms.size();i++){
				Blossom b = blossoms.get(i);
				if(b.getPen()>Blossom.PEN_LIMIT || b.getViol()>Blossom.PEN_LIMIT){//optim Beasley 1993
					nb2viol += b.getViol()*b.getViol();
					b.uselessCounter = 0;
				}else{
					b.uselessCounter++;
					if(b.uselessCounter>=n){
						inBI.clear(b.root);
						blossoms.remove(i);
						i--;
					}
				}
			}
		}
		if(nb2viol==0 || found){
			return true;
		}else{
			step = alpha*(target-hkb)/nb2viol;
		}
		if(step<0.0001){
			return true;
		}
		if(step<0){
			throw new UnsupportedOperationException();
		}
		bi_totalPenalities = 0;
		if(activeBI){// find BI
			for(Blossom b:blossoms){
				b.recompute();
//				if(b.getViol()>0){
//					b.addPen(5);
//				}
//				if(b.getViol()<0){
//					b.addPen(-1);
//				}
				b.addPen(b.getViol()*step);
				if(b.getPen()>Blossom.PEN_LIMIT)
				bi_totalPenalities += b.getMaxPen();
			}
		}
		deg_totalPenalities = 0;
		double maxPen = 2*obj.getUB();
		for(int i=0;i<n;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
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
					if(activeBI)
						for(Blossom b:blossoms){
							if(b.getPen()>Blossom.PEN_LIMIT)
							if(b.gT.arcExists(i,j) || ((b.in.get(i) && b.in.get(j)))){
								costs[i][j] += b.getPen();
							}
						}
					costs[j][i] = costs[i][j];
				}
			}
		}
		return false;
	}

//	private class Blossom{
//		UndirectedGraph gT;
//		double b,T,xT,xEH;
//		BitSet in;
//		double pen;
//
//		Blossom(){
//			in = new BitSet(n);
//			gT = new UndirectedGraph(n, GraphType.LINKED_LIST);
//			generate();
//			recompute();
//		}
//
//		Blossom(int i){
//			in = new BitSet(n);
//			gT = new UndirectedGraph(n, GraphType.LINKED_LIST);
//			generate(i);
//			recompute();
//		}
//
//		private void generate(int root) {
//			in.set(root);
//			for(int i = in.nextSetBit(0);i<n&&i>=0&&in.cardinality()<8;i=in.nextSetBit(i+1)){
//				INeighbors nei = mst.getNeighborsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(!in.get(j)){
//						if(!inBI.get(j))
//						if(mst.getNeighborsOf(j).neighborhoodSize()-maxDegree[j]>0){
//							in.set(j);
//							inBI.set(j);
//							i = -1;
//						}
//					}
//				}
//			}
//			if(in.cardinality()==1){
//				for(int i = in.nextSetBit(0);i<n&&i>=0&&in.cardinality()<5;i=in.nextSetBit(i+1)){
//					INeighbors nei = mst.getNeighborsOf(i);
//					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//						if(!in.get(j)){
//							if(!inBI.get(j))
//							if(mst.getNeighborsOf(j).neighborhoodSize()-maxDegree[j]>=0){
//								in.set(j);
//								inBI.set(j);
//								i = -1;
//							}
//						}
//					}
//				}
//			}
//			b = 0;
//			for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//				b+=maxDegree[i];
//			}
//			for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//				INeighbors nei = mst.getNeighborsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(!in.get(j)){
//						gT.addEdge(i,j);
//						T++;
//					}
//				}
//			}
//			if(!odd()){
//				for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//					INeighbors nei = mst.getNeighborsOf(i);
//					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//						if(!in.get(j)){
//							gT.removeEdge(i, j);
//							T--;
//							return;
//						}
//					}
//				}
//			}
//		}
//
//		private void generate() {
//			int nbNodes = rd.nextInt(10)+1;
//			for(int i=0;i<nbNodes;i++){
//				in.set(rd.nextInt(n));
//			}
//			b = 0;
//			for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//				b+=maxDegree[i];
//			}
//			for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//				INeighbors nei = mst.getNeighborsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(!in.get(j)){
//						gT.addEdge(i,j);
//						T++;
//					}
//				}
//			}
//			if(!odd()){
//				for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//					INeighbors nei = mst.getNeighborsOf(i);
//					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//						if(!in.get(j)){
//							gT.removeEdge(i, j);
//							T--;
//							return;
//						}
//					}
//				}
//			}
//		}
//
//		private boolean isValid() {
//			return odd() && getViol()>0; // && xEH>=2
//		}
//
//		private boolean odd() {
//			return (b+T)%2==1;
//		}
//
//		private double getViol(){
//			return (xEH+xT)-(int)((b+T)/2);
//		}
//
//		private void setPen(double p){
//			pen = p;
//			pen = Math.max(pen, 0);
//			pen = Math.min(pen, 100);
//		}
//
//		private void addPen(double p){
//			pen += p;
//			pen = Math.max(pen, 0);
//			pen = Math.min(pen, 100);
//		}
//
//		private double getPen(){
//			return pen;
//		}
//
//		private double getMaxPen(){
//			return (b+T)*pen/2;
//		}
//
//		public void recompute() {
//			xT = 0;
//			xEH = 0;
//			for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
//				INeighbors nei = mst.getNeighborsOf(i);
//				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
//					if(in.get(j)){
//						if(i<j)
//						xEH++;
//					}else{
//						if(gT.edgeExists(i,j)){
//							xT++;
//						}
//					}
//				}
//			}
//		}
//	}

	private class Blossom{
		UndirectedGraph gT;
		BitSet outlink;
		double b,T,xT,xEH;
		BitSet in;
		double pen;
		boolean DEBUG = false;
		private int uselessCounter;
		public int root;
		public static final double PEN_LIMIT = 0.01;

		Blossom(int root){
			xEH = -1;
			this.root = root;
			in = new BitSet(n);
			outlink = new BitSet(n);
			gT = new UndirectedGraph(n, GraphType.LINKED_LIST);
			addNode(root);
			while(addNext()){}
//			recompute();
			//check correctness
			if(DEBUG){
				check();
				if(!odd()){
//					throw new UnsupportedOperationException();
					System.out.println("warning not odd");
				}
			}
		}

		void addNode(int node){
			if(gT.getNeighborsOf(node).neighborhoodSize()>0){
				INeighbors nei = gT.getNeighborsOf(node);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					gT.removeEdge(node,j);
					T--;
					if(mst.edgeExists(node,j)){
						xT--;
					}
				}
			}
//			inBI.set(node);
			xEH++;
			outlink.clear(node);
			in.set(node);
			b += maxDegree[node];
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!in.get(j)){
					outlink.set(j);
					if(gT.edgeExists(node,j)){
						throw new UnsupportedOperationException();
					}
					if(mst.edgeExists(node,j)){
						T++;
						xT++;
						gT.addEdge(node, j);
					}
				}
			}
			if(DEBUG)check();
			if(!odd())//add one tree edge
				for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
					nei = mst.getSuccessorsOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!(gT.edgeExists(i,j)||in.get(j))){
							T++;
							xT++;
							gT.addEdge(i,j);
							if(DEBUG)check();
							return;
						}
					}
				}
			if(!odd()){//remove one tree edge
				for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
					nei = gT.getSuccessorsOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						T--;
						if(mst.edgeExists(i,j)){
							xT--;
						}
						gT.removeEdge(i,j);
						return;
					}
				}
			}
			if(!odd())//add one non-tree edge
				for(int i = in.nextSetBit(0);i<n&&i>=0;i=in.nextSetBit(i+1)){
					nei = g.getEnvelopGraph().getSuccessorsOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!(gT.edgeExists(i,j)||in.get(j))){
							T++;
							gT.addEdge(i,j);
							return;
						}
					}
				}
			if(!odd()){
//				throw new UnsupportedOperationException();
			}
		}

		private boolean addNext(){
//			recompute();
			double max = 0;
//			max = getViol();
			int next = -1;
			for(int i=outlink.nextSetBit(0);i>=0 && i<n;i=outlink.nextSetBit(i+1)){
				if(in.get(i)){
					throw new UnsupportedOperationException();
				}
				double k = eval(i);
				if(k>max ){//&& !inBI.get(i)){
					max = k;
					next = i;
				}
			}
			if(next==-1){
				return false;
			}
			addNode(next);
			return true;
		}

		private void check() {
			int T2=0;
			for(int i=0;i<n;i++){
				T2+=gT.getNeighborsOf(i).neighborhoodSize();
				INeighbors nei = gT.getNeighborsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(in.get(i) && in.get(j)){
						throw new UnsupportedOperationException();
					}
					if(!(in.get(i) || in.get(j))){
						throw new UnsupportedOperationException();
					}
				}
			}
			T2/=2;
			if(T2!=T){
				throw new UnsupportedOperationException(T2+" =/= "+T);
			}
		}

		private double eval(int i) {
			double t = mst.getNeighborsOf(i).neighborhoodSize()-gT.getNeighborsOf(i).neighborhoodSize();
			double xEH2 = xEH+1;
			double xT2 = xT+t-1;
			double b2 = b+maxDegree[i];
			double T2 = T+t-1;
			if((int)(b2+T2)%2==1){
				xT2--;
				T2--;
			}
			return (xEH2+xT2)-(int)((b2+T2)/2);  
		}

		private boolean odd() {
			return (int)(b+T)%2==1;
		}

		private double getViol(){
			return (xEH+xT)-(int)((b+T)/2);
		}

		private void setPen(double p){
			pen = p;
		}

		private void addPen(double p){
			pen += p;
			pen = Math.max(pen, 0);
			pen = Math.min(pen, 100);
		}

		private double getPen(){
			return pen;
		}

		private double getMaxPen(){
			return Math.floor((b+T)/2)*pen;
		}

		private boolean isValid() {
			return odd() && getViol()>PEN_LIMIT && in.cardinality()>1;
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
//		throw new UnsupportedOperationException();
		return HKfilter.getRepCost(from,to);
	}

	public double getMarginalCost(int from, int to){
		throw new UnsupportedOperationException();
		//return HKfilter.getRepCost(from,to);
	}
}