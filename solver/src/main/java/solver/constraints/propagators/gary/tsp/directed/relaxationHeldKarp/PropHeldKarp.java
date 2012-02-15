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

package solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp;

import choco.kernel.ESat;
import choco.kernel.memory.IStateDouble;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.tsp.HeldKarp;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropHeldKarp<V extends Variable> extends GraphPropagator<V> implements HeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected DirectedGraphVar g;
	protected IntVar obj;
	protected int source, sink;
	protected int n;
	protected int[][] originalCosts;
	protected double[][] costs;
	protected IStateDouble[] inPenalities,outPenalities;
	protected DirectedGraph mst;
	protected TIntArrayList mandatoryArcsList;
	protected double step;
	protected IStateDouble totalPenalities;
	protected AbstractMSTFinder HKfilter, HK;
	public static long nbRem;
	protected static boolean waitFirstSol;
//	protected final static boolean forceTour = false;
//	protected final static boolean DEBUG = false;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropHeldKarp(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		source = from;
		sink   = to;
		originalCosts = costMatrix;
		costs = new double[n][n];
		totalPenalities= environment.makeFloat(0);
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
	}


	/** MST based HK */
	public static PropHeldKarp mstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropHeldKarp phk = new PropHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n,phk);
//		phk.HKfilter = new PrimMSTFinder(phk.n,phk);
		phk.HK = new PrimMSTFinder(phk.n,phk);
		return phk;
	}

	protected IStateInt[] sccOf;
	protected IStateInt nr;
	/** BST based HK */
	public static PropHeldKarp bstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		PropHeldKarp phk = new PropHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalBSTFinderWithFiltering(phk.n,phk,nR,sccOf,outArcs);
//		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n,phk);
		phk.sccOf = sccOf;
		phk.nr = nR;
//		phk.HKfilter = new PrimBSTFinder(phk.n,phk,from,nR,sccOf,outArcs);
		phk.HK = new PrimBSTFinder(phk.n,phk,from,nR,sccOf,outArcs);
//		phk.HK = new PrimMSTFinder(phk.n,phk);
		return phk;
	}

	//***********************************************************************************
	// HK Algorithm(s)
	//***********************************************************************************

	public void HK_algorithm() throws ContradictionException {
		if(waitFirstSol && solver.getMeasures().getSolutionCount()==0){
			return;//the UB does not allow to prune
		}
		if(inPenalities==null){
			inPenalities = new IStateDouble[n];
			outPenalities = new IStateDouble[n];
			for(int i=0;i<n;i++){
				inPenalities[i] = environment.makeFloat(0);
				outPenalities[i] = environment.makeFloat(0);
			}
		}
		// initialisation
		clearStructures();
		rebuildGraph();
		setupMatrix();
		HK_Pascals();
	}

	protected void setupMatrix() throws ContradictionException {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j] = originalCosts[i][j] + outPenalities[i].get() + inPenalities[j].get();
			}
		}
	}

	protected double getTotalPenalties(){
		return totalPenalities.get();
	}

	protected void HK_Pascals() throws ContradictionException {
		double hkb;
		double alpha = 2;
		double beta = 0.5;
		double bestHKB;
		boolean improved;
		int count = 2;
		bestHKB = 0;
		HKfilter.computeMST(costs,g.getEnvelopGraph());
		hkb = HKfilter.getBound()-getTotalPenalties();
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.001){
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + getTotalPenalties() + 0.001);
		for(int iter=5;iter>0;iter--){
			improved = true;
			while(improved){
				improved = false;
				for(int i=n/2;i>0;i--){
					HK.computeMST(costs,g.getEnvelopGraph());
					hkb = HK.getBound()-getTotalPenalties();
					if(hkb>bestHKB+1){
						bestHKB = hkb;
						improved = true;
					}
					mst = HK.getMST();
//					if(DEBUG){
//						checkExtremities();
//					}
					if(hkb-Math.floor(hkb)<0.001){
						hkb = Math.floor(hkb);
					}
					obj.updateLowerBound((int)Math.ceil(hkb), this);
//					if(DEBUG){
//						if(tourFound()){// TODO attention si contraintes autres que TSP ca devient faux
//							if(true){
//								throw new UnsupportedOperationException("test");
//							}
//							if(ConnectivityFinder.findCCOf(mst).size()!=1){
//								throw new UnsupportedOperationException("mst disconnected");
//							}
//							forceTourInstantiation();
//							return;
//						}
//					}
					//	DO NOT FILTER HERE TO FASTEN CONVERGENCE (not always true)
					//	HK.performPruning((double) (obj.getUB()) + totalPenalities.get() +total + 0.001);
					updateStep(hkb,alpha);
					HKPenalities();
					updateCostMatrix();
				}
			}
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-getTotalPenalties();
			if(hkb>bestHKB+1){
				bestHKB = hkb;
				improved = true;
			}
			mst = HKfilter.getMST();
//			if(DEBUG){
//				checkExtremities();
//			}
			if(hkb-Math.floor(hkb)<0.001){
				hkb = Math.floor(hkb);
			}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
//			if(DEBUG){
//				if(tourFound()){// TODO attention si contraintes autres que TSP ca devient faux
//					if(true){
//						throw new UnsupportedOperationException("test");
//					}
//					if(ConnectivityFinder.findCCOf(mst).size()!=1){
//						throw new UnsupportedOperationException("mst disconnected");
//					}
//					forceTourInstantiation();
//					return;
//				}
//			}
			HKfilter.performPruning((double) (obj.getUB()) + getTotalPenalties() + 0.001);
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
				mandatoryArcsList.add(i*n+(j));
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
		int inDeg,outDeg;
		for(int i=0;i<n;i++){
			inDeg = mst.getPredecessorsOf(i).neighborhoodSize();
			outDeg = mst.getSuccessorsOf(i).neighborhoodSize();
			if(i==source){
				nb2viol += (1-outDeg)*(1-outDeg);
			}else if(i==sink){
				nb2viol += (1-inDeg)*(1-inDeg);
			}else{
				nb2viol += (1-inDeg)*(1-inDeg)+(1-outDeg)*(1-outDeg);
			}
		}
		if(nb2viol==0){
			step = 0;
			return;
		}
		step = alpha*(target-hkb)/nb2viol;
	}
	protected void HKPenalities() {
		if(step==0){
			return;
		}
		double totalPenalities = 0;
		int inDeg,outDeg;
		for(int i=0;i<n;i++){
			inDeg = mst.getPredecessorsOf(i).neighborhoodSize();
			outDeg = mst.getSuccessorsOf(i).neighborhoodSize();
			if(i==source){
				outPenalities[i].add((outDeg-1)*step);
				if(inPenalities[i].get()!=0){
					throw new UnsupportedOperationException();
				}
			}else if(i==sink){
				inPenalities[i].add((inDeg-1)*step);
				if(outPenalities[i].get()!=0){
					throw new UnsupportedOperationException();
				}
			}else{
				inPenalities[i].add((inDeg-1)*step);
				outPenalities[i].add((outDeg-1)*step);
			}
			totalPenalities += inPenalities[i].get()+outPenalities[i].get();
		}
		this.totalPenalities.set(totalPenalities);
	}
	protected void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j] = originalCosts[i][j] + outPenalities[i].get() + inPenalities[j].get();
			}
		}
	}
//	protected void checkExtremities() {
//		String error = "";
//		if(mst.getPredecessorsOf(source).neighborhoodSize()!=0){
//			error = "error source2 wrong degree";
//		}
//		if(mst.getSuccessorsOf(sink).neighborhoodSize()!=0){
//			error = "error sink2 wrong degree";
//		}
//		if(error!=""){
//			throw new UnsupportedOperationException(error);
//		}
//	}
//	protected boolean tourFound() {
//		if(!forceTour){
//			return false;
//		}
//		//TODO pas sur que ce soit utile
//		return false;
//	}
	protected void forceTourInstantiation() throws ContradictionException {
		throw new UnsupportedOperationException("error tour not found");
//		int next;
//		INeighbors nei;
//		for(int i=0;i<n;i++){
//			nei = mst.getSuccessorsOf(i);
//			next = nei.getFirstElement();
//			if(i!=sink){
//				if(next<0){
//					throw new UnsupportedOperationException("error tour not found");
//				}
//				g.enforceArc(i,next,this);
//			}
//		}
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
//		String file = "HK.csv";
//		int nbArcs = 0;
//		for(int i=0;i<n;i++){
//			for(int j=0;j<n;j++){
//				nbArcs+=g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
//			}
//			TSP.writeTextInto(obj.getLB()+";"+nbArcs+";1;\n",file);
//			System.out.println(obj.getLB()+";"+nbArcs);
//			HK_algorithm();
//		}
//		System.exit(0);
		HK_algorithm();
		System.out.println("initial HK pruned " + nbRem + " arcs (" + ((nbRem * 100) / (n * (n-1))) + "%)");
		System.out.println("current lower bound : "+obj.getLB());
//		int lb2 = Simplex.directedLPbound(g.getEnvelopGraph(), originalCosts);
//		System.out.println("LP bound : "+lb2);
//		System.exit(0);
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
		return -(((double)obj.getUB())+getTotalPenalties());
	}

	@Override
	public boolean isInMST(int i, int j) {
		if(mst==null){
			return true;
		}
		return mst.arcExists(i,j);
	}

	public boolean isMandatory(int i, int j) {
		return g.getKernelGraph().arcExists(i,j);
	}

	public TIntArrayList getMandatoryArcsList() {
		return mandatoryArcsList;
	}

	public void waitFirstSolution(boolean b){
		waitFirstSol = b;
	}

	public void provideBranchingOpinion(int[][] branchingQuality){
		if(mst!=null){
			INeighbors succs;
			int bonus = 10;
			for (int i = 0; i < n; i++) {
				succs = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=succs.getFirstElement(); j>=0; j=succs.getNextElement()){
					if(mst.arcExists(i,j)){
						branchingQuality[i][j] += bonus;
					}
				}
			}
		}
	}
}
