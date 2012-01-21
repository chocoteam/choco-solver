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
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropHeldKarp<V extends Variable> extends GraphPropagator<V> implements HeldKarp{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar obj;
	int source, sink;
	int n;
	int[][] originalCosts;
	// ASTP->TSP transfo
	UndirectedGraph g2;
	int source2,sink2;
	int n2;
	double[][] costs;
	IStateDouble[] nodePenalities;
	UndirectedGraph mst;
	TIntArrayList mandatoryArcsList;
	BitSet mandatoryArcsBitSet;
	private double step;
	private IStateDouble totalPenalities;
	private AbstractMSTFinder HKfilter, HK;
	private long nbRem;
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
		n2 = 2*n;
		obj = cost;
		source = from;
		sink   = to;
		sink2 = sink;
		source2 = source+n;
		originalCosts = costMatrix;
		costs = new double[n2][n2];
		g2 = new UndirectedGraph(n2, GraphType.LINKED_LIST);
		nodePenalities = new IStateDouble[n2];
		for(int i=0;i<n2;i++){
			nodePenalities[i] = environment.makeFloat(0);
		}
		totalPenalities= environment.makeFloat(0);
		mandatoryArcsList  = new TIntArrayList();
		mandatoryArcsBitSet= new BitSet(n2*n2);
	}


	/** MST based HK */
	public static PropHeldKarp mstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropHeldKarp phk = new PropHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n2,phk);
		phk.HK = new PrimMSTFinder(phk.n2,phk);
		return phk;
	}

	/** BST based HK */
	public static PropHeldKarp bstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		PropHeldKarp phk = new PropHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalBSTFinderWithFiltering(phk.n2,phk,nR,sccOf,outArcs);
		phk.HK = new PrimBSTFinder(phk.n2,phk,from,nR,sccOf,outArcs);
		return phk;
	}

	//***********************************************************************************
	// HK Algorithm(s)
	//***********************************************************************************

	public void HK_algorithm() throws ContradictionException {
		// initialisation
		clearStructures();
		rebuildGraph();
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j+n] = originalCosts[i][j] + nodePenalities[i].get() + nodePenalities[j+n].get();
				costs[j+n][i] = costs[i][j+n];
			}
			costs[i][i+n] = nodePenalities[i].get() + nodePenalities[i+n].get();
			costs[i+n][i] = costs[i][i+n];
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
		HKfilter.computeMST(costs,g2);
		hkb = HKfilter.getBound()-totalPenalities.get();
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.01){
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities.get() + 0.01);
		for(int iter=3;iter>0;iter--){
			improved = false;
			for(int i=n;i>0;i--){
				HK.computeMST(costs,g2);
				hkb = HK.getBound()-totalPenalities.get();
				if(hkb>bestHKB+0.01){
					bestHKB = hkb;
					improved = true;
				}
				mst = HK.getMST();
				if(DEBUG){
					checkExtremities();
				}
				if(hkb-Math.floor(hkb)<0.001){
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
				// DO NOT FILTER HERE TO FASTEN CONVERGENCE
				//	HK.performPruning((double) (obj.getUB()) + totalPenalities.get() + 0.01);
				updateStep(hkb,alpha);
				HKPenalities();
				updateCostMatrix();
			}
			HKfilter.computeMST(costs,g2);
			hkb = HKfilter.getBound()-totalPenalities.get();
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
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities.get() + 0.01);
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
//		totalPenalities = 0;
		mandatoryArcsList.clear();
		mandatoryArcsBitSet.clear();
		for(int i=0;i<n2;i++){
//			nodePenalities[i] = 0;
			g2.getNeighborsOf(i).clear();
		}
	}
	private void rebuildGraph() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			g2.addEdge(i,i+n);
			mandatoryArcsList.add(i*n2+(i+n));
			mandatoryArcsBitSet.set(i*n2+(i+n));
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				g2.addEdge(i,j+n);
			}
			nei = g.getKernelGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				mandatoryArcsList.add(i*n2+(j+n));
				mandatoryArcsBitSet.set(i*n2+(j+n));
			}
		}
	}
	private void updateStep(double hkb,double alpha) {
		double nb2viol = 0.0001;
		double target = obj.getUB();
		int deg;
		for(int i=0;i<n2;i++){
			if(i!=source2 && i!=sink2){
				deg = mst.getNeighborsOf(i).neighborhoodSize();
				nb2viol += (2-deg)*(2-deg);
			}
		}
		step = alpha*(target-hkb)/nb2viol;
	}
	private void HKPenalities() {
		double totalPenalities = 0;
		for(int i=0;i<n2;i++){
			if(i!=source2 && i!=sink2){
				nodePenalities[i].add((mst.getNeighborsOf(i).neighborhoodSize()-2)*step);
				totalPenalities += nodePenalities[i].get()*2;
			}else{
				nodePenalities[i].set(0);
			}
		}
		this.totalPenalities.set(totalPenalities);
	}
	private void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j+n] = originalCosts[i][j] + nodePenalities[i].get() + nodePenalities[j+n].get();
				costs[j+n][i] = costs[i][j+n];
			}
			costs[i][i+n] = nodePenalities[i].get() + nodePenalities[i+n].get();
			costs[i+n][i] = costs[i][i+n];
		}
	}
	private void checkExtremities() {
		String error = "";
		if(mst.getNeighborsOf(source2).neighborhoodSize()!=1){
			error = "error source2 wrong degree";
		}
		if(mst.getNeighborsOf(sink2).neighborhoodSize()!=1){
			error = "error sink2 wrong degree";
		}
		if(mst.getNeighborsOf(source2).getFirstElement()!=source){
			error = "error bad link source - source+n";
		}
		if(mst.getNeighborsOf(sink2).getFirstElement()!=sink+n){
			error = "error bad link sink - sink+n";
		}
		if(error!=""){
			throw new UnsupportedOperationException(error);
		}
	}
	private boolean tourFound() {
		if(!forceTour){
			return false;
		}
		for(int i=0;i<n2;i++){
			if(i!=source2 && i!=sink2){
				if(2!=mst.getNeighborsOf(i).neighborhoodSize()){
					return false;
				}
			}else{
				if(1!=mst.getNeighborsOf(i).neighborhoodSize()){
					return false;
				}
			}
		}
		return true;
	}
	private void forceTourInstantiation() throws ContradictionException {
		int next;
		INeighbors nei;
		for(int i=0;i<n-1;i++){
			nei = mst.getNeighborsOf(i);
			next = nei.getFirstElement();
			if(next == i+n){
				next = nei.getNextElement();
			}
			if(next<0){
				throw new UnsupportedOperationException("error tour not found");
			}
			next -= n;
			g.enforceArc(i,next,this);
		}
	}

	//***********************************************************************************
	// INFERENCE
	//***********************************************************************************

	public void remove(int from, int to) throws ContradictionException {
		g2.removeEdge(from, to);
		if(from==to+n || from+n==to){
			contradiction(g,"mst failure");
		}
		if(from<n){
			if(g.removeArc(from,to-n,this)){
				nbRem++;
			}
		}else{
			if(g.removeArc(to,from-n,this)){
				nbRem++;
			}
		}
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
		return -(((double)obj.getUB())+totalPenalities.get());
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
}
