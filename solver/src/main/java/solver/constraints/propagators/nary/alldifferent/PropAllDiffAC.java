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
package solver.constraints.propagators.nary.alldifferent;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;

import java.util.BitSet;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * <p/>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation and then in O(n+m) time
 * per arc removed from the support
 * Has a good average behavior in practice
 * <p/>
 * Runs incrementally for maintaining a matching
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDiffAC extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n, n2;
	private DirectedGraph digraph;
	private int[] matching;
	private int[] nodeSCC;
	private BitSet free;
	private UnaryIntProcedure remProc;
	protected final IIntDeltaMonitor[] idms;
	private StrongConnectivityFinder SCCfinder;
	// for augmenting matching (BFS)
	private int[] father;
	private BitSet in;
	private TIntIntHashMap map;
	int[] fifo;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * AllDifferent constraint for integer variables
	 * enables to control the cardinality of the matching
	 *
	 * @param vars
	 * @param constraint
	 * @param sol
	 */
	public PropAllDiffAC(IntVar[] vars, Constraint constraint, Solver sol) {
		super(vars, sol, constraint, PropagatorPriority.QUADRATIC, true);
		this.idms = new IIntDeltaMonitor[this.vars.length];
		for (int i = 0; i < this.vars.length; i++){
			idms[i] = this.vars[i].monitorDelta(this);
		}
		n = vars.length;
		map = new TIntIntHashMap();
		IntVar v;
		int ub;
		int idx = n;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
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
		free = new BitSet(n2);
		remProc = new DirectedRemProc();
		father = new int[n2];
		in = new BitSet(n2);
		SCCfinder = new StrongConnectivityFinder(digraph);
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append("PropAllDiffAC(");
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

	private void buildDigraph() {
		for (int i = 0; i < n2; i++) {
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		free.set(0, n2);
		int j, k, ub;
		IntVar v;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
				j = map.get(k);
				if (free.get(i) && free.get(j)) {
					digraph.addArc(j, i);
					free.clear(i);
					free.clear(j);
				} else {
					digraph.addArc(i, j);
				}
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private void repairMatching() throws ContradictionException {
		for (int i = free.nextSetBit(0); i >= 0 && i < n; i = free.nextSetBit(i + 1)) {
			tryToMatch(i);
		}
		int p;
		for (int i = 0; i < n; i++) {
			p = digraph.getPredecessorsOf(i).getFirstElement();
			matching[p] = i;
			matching[i] = p;
		}
	}

	private void tryToMatch(int i) throws ContradictionException {
		int mate = augmentPath_BFS(i);
		if (mate != -1) {
			free.clear(mate);
			free.clear(i);
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
					if (free.get(y)) {
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
		if (n2 > n * 2) {
			digraph.desactivateNode(n2);
			digraph.activateNode(n2);
			for (int i = n; i < n2; i++) {
				if (free.get(i)) {
					digraph.addArc(i, n2);
				} else {
					digraph.addArc(n2, i);
				}
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
		for (int i = 0; i < n; i++) {
			v = vars[i];
			if(!v.hasEnumeratedDomain()){
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
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
			if (n2 < n * 2) {
				contradiction(null, "");
			}
			for (int v = 0; v < n; v++) {
				if (vars[v].instantiated()) {
					int val = vars[v].getValue();
					for (int i = 0; i < n; i++) {
						if (i != v) {
							vars[i].removeValue(val, this);
						}
					}
				}
			}
			buildDigraph();
		} else {
			free.clear();
			for (int i = 0; i < n; i++) {
				if (digraph.getPredecessorsOf(i).neighborhoodSize() == 0) {
					free.set(i);
				}
			}
			for (int i = n; i < n2; i++) {
				if (digraph.getSuccessorsOf(i).neighborhoodSize() == 0) {
					free.set(i);
				}
			}
		}
		repairMatching();
		filter();
		for(int i=0;i<idms.length;i++){
			idms[i].unfreeze();
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
		idms[varIdx].freeze();
		idms[varIdx].forEach(remProc.set(varIdx), EventType.REMOVE);
		idms[varIdx].unfreeze();
		if ((mask & EventType.INSTANTIATE.mask) != 0) {
			int val = vars[varIdx].getValue();
			int j = map.get(val);
			INeighbors nei = digraph.getPredecessorsOf(j);
			for (int i = nei.getFirstElement(); i >= 0; i = nei.getNextElement()) {
				if (i != varIdx) {
					digraph.removeEdge(i, j);
					vars[i].removeValue(val, this);
				}
			}
			int i = digraph.getSuccessorsOf(j).getFirstElement();
			if (i != -1 && i != varIdx) {
				digraph.removeEdge(i, j);
				vars[i].removeValue(val, this);
			}
		}
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
		if (isCompletelyInstantiated()) {
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					if (vars[i].getValue() == vars[j].getValue()) {
						return ESat.FALSE;
					}
				}
			}
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	private class DirectedRemProc implements UnaryIntProcedure<Integer> {
		int idx;

		public void execute(int i) throws ContradictionException {
			digraph.removeEdge(idx, map.get(i));
		}

		@Override
		public UnaryIntProcedure set(Integer idx) {
			this.idx = idx;
			return this;
		}
	}
}
