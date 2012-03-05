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

package solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp;

import choco.kernel.ESat;
import choco.kernel.memory.IStateDouble;
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
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropSymmetricHeldKarp<V extends Variable> extends GraphPropagator<V> implements HeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected UndirectedGraphVar g;
	protected IntVar obj;
	protected int n;
	protected int[][] originalCosts;
	protected double[][] costs;
	private IStateDouble[] penalities;
	protected UndirectedGraph mst;
	protected TIntArrayList mandatoryArcsList;
	protected  double step;
	private  IStateDouble totalPenalities;
	protected AbstractTreeFinder HKfilter, HK;
	public static long nbRem;
	protected static boolean waitFirstSol;
	protected int treeMode; // 0=MST; 1=OneTree; 2=TwoTree
	protected static final int MST=0,ONE_TREE=1,TWO_TREE=2;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	protected PropSymmetricHeldKarp(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		obj = cost;
		originalCosts = costMatrix;
		costs = new double[n][n];
		totalPenalities= environment.makeFloat(0);
		mandatoryArcsList  = new TIntArrayList();
		nbRem  = 0;
	}

	/** MST based HK */
//	public static PropSymmetricHeldKarp mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
//		PropSymmetricHeldKarp phk = new PropSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
////		phk.HKfilter = new KruskalMSTFinder(phk.n,phk);
//		phk.HK = new PrimMSTFinder(phk.n,phk);
//		phk.HKfilter = phk.HK;
//		phk.treeMode = MST;
//		return phk;
//	}
	/** ONE TREE based HK */
	public static PropSymmetricHeldKarp oneTreeBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropSymmetricHeldKarp phk = new PropSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
//		phk.HKfilter = new KruskalOneTreeFinder(phk.n,phk);
		phk.HK = new PrimOneTreeFinder(phk.n,phk);
		phk.HKfilter = phk.HK;
		phk.treeMode = ONE_TREE;
		return phk;
	}
	/** TWO TREE based HK */
//	public static PropSymmetricHeldKarp twoTreeBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
//		PropSymmetricHeldKarp phk = new PropSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
////		phk.HKfilter = new KruskalTwoTreeFinder(phk.n,phk);
//		phk.HK = new PrimTwoTreeFinder(phk.n,phk);
//		phk.HKfilter = phk.HK;
//		phk.treeMode = TWO_TREE;
//		return phk;
//	}

	//***********************************************************************************
	// HK Algorithm(s) 
	//***********************************************************************************

	public void HK_algorithm() throws ContradictionException {
		if(waitFirstSol && solver.getMeasures().getSolutionCount()==0){
			return;//the UB does not allow to prune
		}
		// initialisation
		clearStructures();
		rebuildGraph();
		setCosts();
		HK_Pascals();
	}

	protected void setCosts() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i].get() + penalities[j].get();
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	protected double getTotalPen(){
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
		hkb = HKfilter.getBound()-getTotalPen();
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if(hkb-Math.floor(hkb)<0.001){
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int)Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + getTotalPen() + 0.001);
		for(int iter=5;iter>0;iter--){
			improved = true;
//			while(improved){
				improved = false;
				for(int i=30;i>0;i--){
//				for(int i=(n-treeMode)/2;i>0;i--){
					HK.computeMST(costs,g.getEnvelopGraph());
					hkb = HK.getBound()-getTotalPen();
					if(hkb>bestHKB+1){
						bestHKB = hkb;
						improved = true;
					}
					mst = HK.getMST();
					if(hkb-Math.floor(hkb)<0.001){
						hkb = Math.floor(hkb);
					}
					obj.updateLowerBound((int)Math.ceil(hkb), this);
					//	DO NOT FILTER HERE TO FASTEN CONVERGENCE (not always true)
//					if(i%10==0){
//						HK.performPruning((double) (obj.getUB()) + getTotalPen() + 0.001);
//					}
					updateStep(hkb,alpha);
					HKPenalities();
					updateCostMatrix();
				}
//			}
			HKfilter.computeMST(costs,g.getEnvelopGraph());
			hkb = HKfilter.getBound()-getTotalPen();
			if(hkb>bestHKB+1){
				bestHKB = hkb;
				improved = true;
			}
			mst = HKfilter.getMST();
			if(hkb-Math.floor(hkb)<0.001){
				hkb = Math.floor(hkb);
			}
			obj.updateLowerBound((int)Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + getTotalPen() + 0.001);
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
				if(i<j){
					mandatoryArcsList.add(i * n + j);
				}
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
		int deg;
		for(int i=1;i<n-1;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			nb2viol += (2-deg)*(2-deg);
		}
		int degFirst = mst.getNeighborsOf(0).neighborhoodSize();
		int degLast = mst.getNeighborsOf(n-1).neighborhoodSize();
		switch(treeMode){
			case MST:
				nb2viol += (1-degFirst)*(1-degFirst)+(1-degLast)*(1-degLast);break;
			case ONE_TREE:
				nb2viol += (2-degLast)*(2-degLast);break;
			case TWO_TREE: break;
		}
		if(nb2viol == 0){
			step = 0;
		}else{
			step = alpha*(target-hkb)/nb2viol;
		}
	}
	protected void HKPenalities() {
		if(step==0){
			return;
		}
		double sumPenalities = 0;
		int deg;
		for(int i=1;i<n-1;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			penalities[i].add((deg-2)*step);
			sumPenalities += penalities[i].get();
		}
		int degFirst = mst.getNeighborsOf(0).neighborhoodSize();
		int degLast = mst.getNeighborsOf(n-1).neighborhoodSize();
		switch(treeMode){
			case MST:
				penalities[0].add((degFirst-1)*step);
				penalities[n-1].add((degLast-1)*step);
				sumPenalities += (penalities[0].get()+penalities[n-1].get())/2;break;
			case ONE_TREE:
				penalities[n-1].add((degLast-2)*step);
				sumPenalities += penalities[n-1].get();
				if(degFirst!=2){
					throw new UnsupportedOperationException();
				}break;
			case TWO_TREE:
				if(degFirst!=1||degFirst!=1){
					throw new UnsupportedOperationException();
				}break;
		}
		this.totalPenalities.set(2*sumPenalities);
	}
	protected void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i].get() + penalities[j].get();
					costs[j][i] = costs[i][j];
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
		if(penalities==null){
			penalities = new IStateDouble[n];
			for(int i=0;i<n;i++){
				penalities[i] = environment.makeFloat(0);
			}
		}
		if(treeMode==ONE_TREE){
			System.out.println("avant");
			int nb = 0;
			for(int i=1;i<n;i++){
				nb+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			nb-=g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize();
			System.out.println((nb/2)+" edges");
		}else if(treeMode==TWO_TREE){
			System.out.println("avant");
			int nb = 0;
			for(int i=1;i<n-1;i++){
				nb+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			nb-=g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize();
			nb-=g.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize();
			System.out.println((nb/2)+" edges");
			System.out.println("size(0)=size(n) : "+
			(g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize()==g.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize()));
		}
			System.out.println(obj);

		HK_algorithm();
		System.out.println("initial HK pruned " + nbRem + " arcs (" + ((nbRem * 200) / (n * (n-1))) + "%)");
		System.out.println("current lower bound : "+obj.getLB());
		System.out.println(obj);
		if(treeMode==ONE_TREE){
			int nb = 0;
			for(int i=1;i<n;i++){
				nb+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			nb-=g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize();
			System.out.println((nb/2)+" edges");
		}else if(treeMode==TWO_TREE){
			int nb = 0;
			for(int i=1;i<n-1;i++){
				nb+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
			}
			nb-=g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize();
			nb-=g.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize();
			System.out.println((nb/2)+" edges");
			System.out.println("size(0)=size(n) : "+
			(g.getEnvelopGraph().getSuccessorsOf(0).neighborhoodSize()==g.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize()));
		}
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
		return -(((double)obj.getUB())+getTotalPen());
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

	public boolean isInMST(int i, int j){
		if(mst==null){
			return true;
		}
		return mst.edgeExists(i,j);
	}
	public UndirectedGraph getMST(){
		return mst;
	}
	public double getRepCost(int from, int to){
		return HKfilter.getRepCost(from,to);
	}
}
