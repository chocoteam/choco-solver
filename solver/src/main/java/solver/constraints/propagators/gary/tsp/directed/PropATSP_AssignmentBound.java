/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.gary.tsp.directed;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IGraphDeltaMonitor;
import solver.variables.graph.DirectedGraph;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.setDataStructures.SetType;
import solver.variables.graph.IGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Assignment Problem relaxation of the ATSP
 * Only computes the lower bound
 * + : the code seems safe
 * incremental algorithm
 * - : the first call is slow (could be improved)
 * do not compute reduced costs -> no filtering (should implement Khun's algorithm)
 *
 * @author Jean-Guillaume Fages
 */
public class PropATSP_AssignmentBound extends Propagator<Variable> implements IGraphRelaxation{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n, n2;
	private DirectedGraphVar g;
	private DirectedGraph digraph;
	private int[] nodeSCC;
	private DirectedRemProc remProc;
	protected final IGraphDeltaMonitor gdm;
	private StrongConnectivityFinder SCCfinder;
	// for augmenting matching (BFS)
	private int[] father;
	private BitSet in;
	int[] fifo;
	private int[] flow;
	private int costValue;
	private IntVar flowCost;
	private int[][] costMatrix;
	private int[] fb_poids;
	private int cycleNode;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * @param g
	 * @param constraint
	 * @param sol
	 */
	public PropATSP_AssignmentBound(DirectedGraphVar g, IntVar cost, int[][] costMatrix, Constraint constraint, Solver sol) {
		super(new Variable[]{g,cost}, sol, constraint, PropagatorPriority.CUBIC, false);
		this.gdm = g.monitorDelta(this);
		this.g = g;
		this.flowCost = cost;
		n = g.getEnvelopGraph().getNbNodes();
		n2 = n*2;
		fifo = new int[n2];
		digraph = new DirectedGraph(solver.getEnvironment(), n2, SetType.LINKED_LIST,false);
		remProc = new DirectedRemProc();
		father = new int[n2];
		in = new BitSet(n2);
		SCCfinder = new StrongConnectivityFinder(digraph);
		//
		this.flow = new int[n2];
		this.costMatrix = new int[n2][n2];
		for(int i=0; i<n2; i++){
			flow[i] = 0;
		}
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				this.costMatrix[i][j] = this.costMatrix[j+n][i] = this.costMatrix[i][j+n] = costMatrix[i][j];
			}
		}
		fb_poids = new int[n2];
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append("PropATSP_AP_bound(");
		st.append(flowCost.getName()).append(",");
		st.append(g.getName()).append(")");
		return st.toString();
	}

	//***********************************************************************************
	// Initialization
	//***********************************************************************************

	private void buildDigraph() throws ContradictionException {
		costValue = 0;
		for (int i = 0; i < n2; i++) {
			flow[i] = 0;
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		for (int i = 0; i < n; i++) {
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				digraph.addArc(i,j+n);
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private void repairMatching() throws ContradictionException {
		// find feasible (min) flow
		for (int i=0; i<n; i++) {
			if(digraph.getPredecessorsOf(i).getSize()==0 && digraph.getSuccessorsOf(i).getSize()>0){
				assignVariable(i);
			}
		}
		// eval support;
		costValue = 0;
		for(int i=0;i<n;i++){
			int j = digraph.getPredecessorsOf(i).getFirstElement();
			if(j!=-1){
				costValue += costMatrix[i][j];
			}
		}
		int c1 = costValue;
		// find min cost max flow
		while(negativeCircuitExists()){
			applyNegativeCircuit();
			costValue = 0;
			for(int i=0;i<n;i++){
				int j = digraph.getPredecessorsOf(i).getFirstElement();
				if(j!=-1){
					costValue += costMatrix[i][j];
				}
			}
		}
		costValue = 0;
		for(int i=0;i<n;i++){
			int j = digraph.getPredecessorsOf(i).getFirstElement();
			if(j!=-1){
				costValue += costMatrix[i][j];
			}
		}
		flowCost.updateLowerBound(costValue, aCause);
	}

	private boolean negativeCircuitExists() {
		// initialization
		int max = costValue+10;// big M
		for(int i=0;i<n2;i++){
			fb_poids[i] = max;
			father[i] = -1;
		}
		for(int i=0;i<n;i++){
			fb_poids[i] = 0;
		}
		// Bellman-Ford algorithm
		for(int iter=1;iter<n2;iter++){
			for(int i=0;i<n2;i++){
				ISet nei = digraph.getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					int paux = fb_poids[i];
					if(i<j){
						paux += costMatrix[i][j];
					}else{
						paux -= costMatrix[i][j];
					}
					if(paux<fb_poids[j]){
						fb_poids[j] = paux;
						father[j] = i;
					}
				}
			}
		}
		for(int i=0;i<n2;i++){
			ISet nei = digraph.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				int paux = fb_poids[i];
				if(i<j){
					paux += costMatrix[i][j];
				}else{
					paux -= costMatrix[i][j];
				}
				if(paux<fb_poids[j]){
					cycleNode = j;
					return true;
				}
			}
		}
		return false;
	}

	private void applyNegativeCircuit() {
		int i = cycleNode;
		int p = father[cycleNode];
		if(p<0){
			throw new UnsupportedOperationException();
		}
		in.clear();
		do{
			in.set(i);
			i = p;
			p = father[p];
			assert p>=0;
		}while(!in.get(p));
		cycleNode = i;
		do{
			digraph.removeArc(p,i);
			digraph.addArc(i,p);
			i = p;
			p = father[p];
			assert p>=0;
		}while(i!=cycleNode);
	}

	private void assignVariable(int i) throws ContradictionException {
		int mate = augmentPath_BFS(i);
		assignVariable(i,mate);
	}

	private void assignVariable(int i, int mate) throws ContradictionException {
		if (mate != -1) {
			flow[mate]++;
			flow[i]++;
			int tmp = mate;
			while (tmp != i) {
				digraph.removeArc(father[tmp], tmp);
				digraph.addArc(tmp, father[tmp]);
				tmp = father[tmp];
			}
		} else {
			contradiction(g, "no match");
		}
	}

	private int augmentPath_BFS(int root) {
		in.clear();
		int indexFirst = 0, indexLast = 0;
		fifo[indexLast++] = root;
		in.set(root);
		int x, y;
		ISet succs;
		while (indexFirst != indexLast) {
			x = fifo[indexFirst++];
			succs = digraph.getSuccessorsOf(x);
			for (y = succs.getFirstElement(); y >= 0; y = succs.getNextElement()) {
				if (!in.get(y)) {
					father[y] = x;
					fifo[indexLast++] = y;
					in.set(y);
					if (flow[y]==0){
						assert (y>=n);
						return y;
					}
				}
			}
		}
		return -1;
	}

	//***********************************************************************************
	// PRUNING
	//***********************************************************************************

	private void buildSCC() {
		SCCfinder.findAllSCC();
		nodeSCC = SCCfinder.getNodesSCC();
	}

	private void filter() throws ContradictionException {
		buildSCC();
		for (int i=0; i<n; i++) {
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if ((nodeSCC[i] != nodeSCC[j+n] && digraph.arcExists(j+n,i))
						&&	(nodeSCC[i+n] != nodeSCC[j] && digraph.arcExists(i+n,j))){
					g.enforceArc(i, j, aCause);
				}else if (nodeSCC[i] != nodeSCC[j+n] && nodeSCC[i+n]!=nodeSCC[j]
						&&	!(digraph.arcExists(i+n,j) || digraph.arcExists(j+n,i))){
					g.removeArc(i,j,aCause);
					digraph.removeEdge(i,j+n);
					digraph.removeEdge(i+n,j);
				}
			}
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
			buildDigraph();
		}
		repairMatching();
		filter();
		gdm.unfreeze();
		System.out.println("Assignment Problem bound : "+costValue);
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		gdm.freeze();
		gdm.forEachArc(remProc, EventType.REMOVEARC);
		repairMatching();
		filter();
		gdm.unfreeze();
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		if(!(g.instantiated()&&flowCost.instantiated())){
			return ESat.UNDEFINED;
		}
		throw new UnsupportedOperationException("Entailment check not implemented");
	}

	@Override
	public boolean contains(int i, int j) {
		return digraph.arcExists(j+n,i);
	}

	@Override
	public double getReplacementCost(int i, int j) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public double getMarginalCost(int i, int j) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public IGraph getSupport() {
		return digraph;
	}

	private class DirectedRemProc implements PairProcedure {
		public void execute(int i, int j) throws ContradictionException {
			removeDiArc(i,j+n);
		}
		private void removeDiArc(int i, int j){
			digraph.removeArc(i,j);
			if(digraph.removeArc(j,i)){
				flow[i]--;
				flow[j]--;
			}
		}
	}
}
