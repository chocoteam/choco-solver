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
package solver.constraints.propagators.nary.globalcardinality;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import java.util.BitSet;

/**
 * Propagator for Global Cardinality Constraint (GCC) AC for integer variables
 * foreach i, |{v = value[i] | for any v in vars}|=cards[i]
 * <p/>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time per propagation
 * also filter cardinality variables
 * <p/>
 * Not incremental
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
public class PropGCC_AC_Cards extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n, n2;
	private DirectedGraph digraph;
	private int[] matching;
	private int[] nodeSCC;
	//	private BitSet free;
	private StrongConnectivityFinder SCCfinder;
	// for augmenting matching (BFS)
	private int[] father,values,lb,ub;
	private BitSet in;
	private TIntIntHashMap map;
	int[] fifo;
	private IntVar[] cards;
	private IStateInt[] flow;
	private TIntArrayList boundedVariables;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Global Cardinality Constraint (GCC) for integer variables
	 * foreach i, |{v = value[i] | for any v in vars}|=cards[i]
	 *
	 * @param vars
	 * @param value
	 * @param cards
	 * @param constraint
	 * @param sol
	 */
	public PropGCC_AC_Cards(IntVar[] vars, int[] value, IntVar[] cards, Constraint constraint, Solver sol) {
		super(ArrayUtils.append(vars,cards), sol, constraint, PropagatorPriority.QUADRATIC, false);
		values = value;
		n = vars.length;
		this.cards = cards;
		map = new TIntIntHashMap();
		IntVar v;
		int ubtmp;
		int idx = n;
		boundedVariables = new TIntArrayList();
		for (int i = 0; i < n; i++) {
			v = vars[i];
			if(!v.hasEnumeratedDomain()){
				boundedVariables.add(i);
			}
			ubtmp = v.getUB();
			for (int j = v.getLB(); j <= ubtmp; j = v.nextValue(j)) {
				if (!map.containsKey(j)) {
					map.put(j, idx);
					idx++;
				}
			}
		}
		n2 = idx;
		fifo = new int[n2];
		matching = new int[n2];
		digraph = new StoredDirectedGraph(solver.getEnvironment(), n2 + 1, GraphType.MATRIX);
		father = new int[n2];
		in = new BitSet(n2);
		SCCfinder = new StrongConnectivityFinder(digraph);
		//
		this.lb = new int[n2];
		this.ub = new int[n2];
		this.flow = new IStateInt[n2];
		for(int i=0; i<n; i++){
			ub[i] = lb[i] = 1; // 1 unit of flow per variable
			flow[i] = environment.makeInt(0);
		}
		for(int i=n; i<n2; i++){
			ub[i] = n; // [0,n] units of flow per value (default)
			flow[i] = environment.makeInt(0);
		}
		for(int i=0; i<value.length; i++){
			idx = map.get(value[i]);
			int low = cards[i].getLB();
			int up = cards[i].getUB();
			if(lb[idx]!=0 && lb[idx]!=low
			|| ub[idx]!=n && ub[idx]!=up){
				throw new UnsupportedOperationException("error in the use of GCC: duplication of value "+value[i]);
			}
			lb[idx] = low;
			ub[idx] = up;
			if(low>up){
				throw new UnsupportedOperationException("GCC error: low[i]>up[i]");
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append("PropGCC_AC(");
		int i = 0;
		for (; i < Math.min(4, vars.length); i++) {
			st.append(vars[i].getName()).append(", ");
		}
		if (i < vars.length - 2) {
			st.append("...,");
		}
		st.append(vars[vars.length - 1].getName()).append(")");
		return st.toString();
	}

	//***********************************************************************************
	// Initialization
	//***********************************************************************************

	private void buildDigraph() throws ContradictionException {
		for (int i = 0; i < n2; i++) {
			flow[i].set(0);
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		int j, k, ub;
		IntVar v;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			if(v.instantiated()){
				j = map.get(v.getValue());
				if (flow[j].get()<this.ub[j]){
					digraph.addArc(j, i);
					flow[i].add(1);
					flow[j].add(1);
				} else {
					contradiction(v, "");
				}
			}else{
				for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
					j = map.get(k);
					if (flow[i].get()<this.ub[i] && flow[j].get()<this.ub[j]){//
						digraph.addArc(j, i);
						flow[i].add(1);
						flow[j].add(1);
					} else {
						digraph.addArc(i, j);
					}
				}
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private void repairMatching() throws ContradictionException {
		for (int i=0; i<n; i++) {
			if(flow[i].get()==0){
				assignVariable(i);
			}
		}
		for (int i=n; i<n2; i++) {
			while(flow[i].get()<lb[i]){
				useValue(i);
			}
		}
		int p;
		for (int i = 0; i < n; i++) {
			p = digraph.getPredecessorsOf(i).getFirstElement();
			matching[p] = i;
			matching[i] = p;
		}
	}

	private void assignVariable(int i) throws ContradictionException {
		int mate = augmentPath_BFS(i);
		if (mate != -1) {
			flow[mate].add(1);
			flow[i].add(1);
			int tmp = mate;
			while (tmp != i) {
				digraph.removeArc(father[tmp], tmp);
				digraph.addArc(tmp, father[tmp]);
				tmp = father[tmp];
			}
		} else {
			contradiction(vars[i], "no match");
		}
	}

	private int augmentPath_BFS(int root) {
		in.clear();
		int indexFirst = 0, indexLast = 0;
		fifo[indexLast++] = root;
		int x, y;
		INeighbors succs;
		while (indexFirst != indexLast) {
			x = fifo[indexFirst++];
			succs = digraph.getSuccessorsOf(x);
			for (y = succs.getFirstElement(); y >= 0; y = succs.getNextElement()) {
				if (!in.get(y)) {
					father[y] = x;
					fifo[indexLast++] = y;
					in.set(y);
					if (flow[y].get()<this.ub[y]){
						if(y<n){
							throw new UnsupportedOperationException();
						}
						return y;
					}
				}
			}
		}
		return -1;
	}

	private void useValue(int i) throws ContradictionException {
		int mate = swapValue_BFS(i);
		if (mate != -1) {
			flow[mate].add(-1);
			flow[i].add(1);
			int tmp = mate;
			while (tmp != i) {
				digraph.removeArc(tmp, father[tmp]);
				digraph.addArc(father[tmp], tmp);
				tmp = father[tmp];
			}
		} else {
			contradiction(null, "no match");
		}
	}

	private int swapValue_BFS(int root) {
		in.clear();
		int indexFirst = 0, indexLast = 0;
		fifo[indexLast++] = root;
		int x, y;
		INeighbors succs;
		while (indexFirst != indexLast) {
			x = fifo[indexFirst++];
			succs = digraph.getSuccessorsOf(x);
			for (y = succs.getFirstElement(); y >= 0; y = succs.getNextElement()) {
				if (!in.get(y)) {
					father[y] = x;
					fifo[indexLast++] = y;
					in.set(y);
					if (flow[y].get()<this.ub[y]){
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
		digraph.desactivateNode(n2);
		digraph.activateNode(n2);
		for (int i = n; i < n2; i++) {
			if (flow[i].get()<ub[i]) {
				digraph.addArc(i, n2);
			}
			if(flow[i].get()>lb[i]){
				digraph.addArc(n2, i);
			}
		}
		SCCfinder.findAllSCC();
		nodeSCC = SCCfinder.getNodesSCC();
		digraph.desactivateNode(n2);
	}

	private void filter() throws ContradictionException {
		buildSCC();
		int j, ub;
		IntVar v;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			if(v.getLB()!=ub) {// i.e. v is not instantiated
				for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
					j = map.get(k);
					if (nodeSCC[i] != nodeSCC[j]) {
						if (matching[i] == j && matching[j] == i) {
							v.instantiateTo(k,this);
						} else {
							v.removeValue(k, this);
							digraph.removeArc(i, j);
						}
					}
				}
			}
		}
		int nb = boundedVariables.size();
		for (int i = 0; i < nb; i++) {
			v = vars[boundedVariables.get(i)];
			ub = v.getUB();
			for (int k = v.getLB(); k <= ub; k++) {
				j = map.get(k);
				if (!(digraph.arcExists(i,j) || digraph.arcExists(j,i))) {
					v.removeValue(k, this);
				}
			}
			int lb = v.getLB();
			for (int k = v.getUB(); k >= lb; k--) {
				j = map.get(k);
				if (!(digraph.arcExists(i,j) || digraph.arcExists(j,i))) {
					v.removeValue(k, this);
				}
			}
		}
		// filter cardinality variables
		int idx;
		INeighbors nei;
		for(int i=0; i<values.length; i++){
			idx = map.get(values[i]);
			nei = digraph.getSuccessorsOf(idx);
			cards[i].updateUpperBound(nei.neighborhoodSize()
					+digraph.getPredecessorsOf(idx).neighborhoodSize()
					,this);
			int min = 0;
			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(vars[j].instantiated()){
					min++;
				}
			}
			cards[i].updateLowerBound(min,this);
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int idx;
		for(int i=0; i<values.length; i++){
			idx = map.get(values[i]);
			lb[idx] = cards[i].getLB();
			ub[idx] = cards[i].getUB();
		}
		buildDigraph();
		repairMatching();
		filter();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public int getPropagationConditions() {
		return EventType.FULL_PROPAGATION.mask + EventType.CUSTOM_PROPAGATION.mask;
	}

	@Override
	public ESat isEntailed() {
		int[] f = new int[n2];
		int idx;
		for(int i=0; i<values.length; i++){
			idx = map.get(values[i]);
			lb[idx] = cards[i].getLB();
			ub[idx] = cards[i].getUB();
		}
		if (isCompletelyInstantiated()) {
			for (int i = 0; i < n; i++) {
				f[map.get(vars[i].getValue())]++;
			}
			for (int i = n; i < n2; i++) {
				if(f[i]<lb[i] || f[i]>ub[i]){
					return ESat.FALSE;
				}
			}
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
