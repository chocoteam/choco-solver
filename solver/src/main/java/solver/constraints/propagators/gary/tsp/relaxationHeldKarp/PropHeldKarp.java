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

package solver.constraints.propagators.gary.tsp.relaxationHeldKarp;

import choco.kernel.ESat;
import choco.kernel.memory.IStateDouble;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import samples.graph.TSP;
import samples.graph.Tree;
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
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropHeldKarp<V extends Variable> extends GraphPropagator<V> implements HeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar obj;
	int source, sink;
	int n;
	int[][] originalCosts;
	double[][] costs;
	IStateDouble[] inPenalities,outPenalities;
	DirectedGraph mst;
	TIntArrayList mandatoryArcsList;
	BitSet mandatoryArcsBitSet;
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
	private PropHeldKarp(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		source = from;
		sink   = to;
		originalCosts = costMatrix;
		costs = new double[n][n];
		inPenalities = new IStateDouble[n];
		outPenalities = new IStateDouble[n];
		for(int i=0;i<n;i++){
			inPenalities[i] = environment.makeFloat(0);
			outPenalities[i] = environment.makeFloat(0);
		}
		totalPenalities= environment.makeFloat(0);
		mandatoryArcsList  = new TIntArrayList();
		mandatoryArcsBitSet= new BitSet(n*n);
		nbRem  = 0;
	}


	/** MST based HK */
	public static PropHeldKarp mstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropHeldKarp phk = new PropHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n,phk);
		phk.HK = new PrimMSTFinder(phk.n,phk);
		return phk;
	}

	IStateInt[] sccOf;
	IStateInt nr;
	double total;
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

	double sccPen;
	public void HK_algorithm() throws ContradictionException {
//		if(solver.getMeasures().getSolutionCount()==0){
//			return;//the UB does not allow to prune
//		}
//		int size = 0;
//		for(int i=0;i<n;i++){
//			size+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
//		}
//		if(size>n*n/10){
//			return;//the UB does not allow to prune
//		}
		sccPen = 0;//-10000;//-obj.getUB();
		// initialisation
		clearStructures();
		rebuildGraph();
		INeighbors nei;
		total = 0;
		boolean offSet = sccOf!=null;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j] = originalCosts[i][j] + outPenalities[i].get() + inPenalities[j].get();
				if(offSet && sccOf[i].get()!=sccOf[j].get()){
					costs[i][j] += sccPen;
				}
			}
		}
		if(offSet){
			total = (nr.get()-1)*sccPen;
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
		hkb = HKfilter.getBound()-totalPenalities.get()-total;
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.01){
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities.get() + total + 0.01);
		for(int iter=3;iter>0;iter--){
			improved = false;
			for(int i=n;i>0;i--){
				HK.computeMST(costs,g.getEnvelopGraph());
				hkb = HK.getBound()-totalPenalities.get()-total;
				if(hkb>bestHKB+0.01){
					bestHKB = hkb;
					improved = true;
				}
				mst = HK.getMST();
				if(DEBUG){
					checkExtremities();
				}
				if(hkb-Math.floor(hkb)<0.01){
					hkb = Math.floor(hkb);
				}
				obj.updateLowerBound((int)Math.ceil(hkb), this);
				if(DEBUG){
					if(tourFound()){// TODO attention si contraintes autres que TSP ca devient faux
						if(true){
							throw new UnsupportedOperationException("test");
						}
						if(ConnectivityFinder.findCCOf(mst).size()!=1){
							throw new UnsupportedOperationException("mst disconnected");
						}
						forceTourInstantiation();
						return;
					}
				}
				//	DO NOT FILTER HERE TO FASTEN CONVERGENCE (not always true)
				//	HK.performPruning((double) (obj.getUB()) + totalPenalities.get() +total + 0.01);
				updateStep(hkb,alpha);
				HKPenalities();
				updateCostMatrix();
			}
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-totalPenalities.get()-total;
			if(hkb>bestHKB+0.1){
				bestHKB = hkb;
				improved = true;
			}
			mst = HKfilter.getMST();
			if(DEBUG){
				checkExtremities();
			}
			if(hkb-Math.floor(hkb)<0.01){
				hkb = Math.floor(hkb);
			}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			if(DEBUG){
				if(tourFound()){// TODO attention si contraintes autres que TSP ca devient faux
					if(true){
						throw new UnsupportedOperationException("test");
					}
					if(ConnectivityFinder.findCCOf(mst).size()!=1){
						throw new UnsupportedOperationException("mst disconnected");
					}
					forceTourInstantiation();
					return;
				}
			}
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities.get() +total + 0.01);
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
		mandatoryArcsBitSet.clear();
//		for(int i=0;i<n2;i++){
//			nodePenalities[i] = 0;
//			g2.getNeighborsOf(i).clear();
//		}
	}
	private void rebuildGraph() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getKernelGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				mandatoryArcsList.add(i*n+(j));
				mandatoryArcsBitSet.set(i*n+(j));
			}
		}
	}
	private void updateStep(double hkb,double alpha) {
		double nb2viol = 0.0001;
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
		step = alpha*(target-hkb)/nb2viol;
	}
	private void HKPenalities() {
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
	private void updateCostMatrix() {
		INeighbors nei;
		boolean offSet = sccOf!=null;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j] = originalCosts[i][j] + outPenalities[i].get() + inPenalities[j].get();
				if(offSet && sccOf[i].get()!=sccOf[j].get()){
					costs[i][j] += sccPen;
				}
			}
		}
	}
	private void checkExtremities() {
		String error = "";
		if(mst.getPredecessorsOf(source).neighborhoodSize()!=0){
			error = "error source2 wrong degree";
		}
		if(mst.getSuccessorsOf(sink).neighborhoodSize()!=0){
			error = "error sink2 wrong degree";
		}
		if(error!=""){
			throw new UnsupportedOperationException(error);
		}
	}
	private boolean tourFound() {
		if(!forceTour){
			return false;
		}
		//TODO pas sur que ce soit utile
		return false;
	}
	private void forceTourInstantiation() throws ContradictionException {
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
//		int nbArcs;
//		for(int i=0;i<n;i++){
//			nbArcs = 0;
//			for(int j=0;j<n;j++){
//				nbArcs+=g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize();
//			}
//			TSP.writeTextInto(obj.getLB()+";"+nbArcs+";1;\n",file);
//			System.out.println(obj.getLB()+";"+nbArcs);
//			HK_algorithm();
//		}
//		System.exit(0);
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
		return Math.min(2*sccPen,-(((double)obj.getUB())+totalPenalities.get()));
	}

	@Override
	public int getMandatorySuccessorOf(int i) {
		return g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
	}

	public BitSet getMandatoryArcsBitSet() {
		return mandatoryArcsBitSet;
	}

	public TIntArrayList getMandatoryArcsList() {
		return mandatoryArcsList;
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
