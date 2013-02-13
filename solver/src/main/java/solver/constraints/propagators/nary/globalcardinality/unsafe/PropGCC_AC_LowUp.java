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
package solver.constraints.propagators.nary.globalcardinality.unsafe;

import common.ESat;
import common.util.procedure.UnaryIntProcedure;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import memory.IStateInt;
import memory.graphs.DirectedGraph;
import memory.graphs.graphOperations.connectivity.StrongConnectivityFinder;
import memory.setDataStructures.ISet;
import memory.setDataStructures.SetType;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import java.util.BitSet;

/**
 * Propagator for Global Cardinality Constraint (GCC) AC for integer variables
 * foreach i, low[i]<=|{v = value[i] | for any v in vars}|<=up[i]
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
public class PropGCC_AC_LowUp extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, n2;
    private DirectedGraph digraph;
    private int[] nodeSCC;
    //	private BitSet free;
    private DirectedRemProc remProc;
    protected final IIntDeltaMonitor[] idms;
    private StrongConnectivityFinder SCCfinder;
    // for augmenting matching (BFS)
    private int[] father;
    private BitSet in;
    private TIntIntHashMap map;
    int[] fifo;
    private int[] lb, ub;
    private IStateInt[] flow;
    private TIntArrayList boundedVariables, valuesToCompute;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Global Cardinality Constraint (GCC) for integer variables
     * foreach i, low[i]<=|{v = value[i] | for any v in vars}|<=up[i]
     *
     * @param vars
     * @param value
     * @param low
     * @param up
     */
    public PropGCC_AC_LowUp(IntVar[] vars, int[] value, int[] low, int[] up) {
        super(vars, PropagatorPriority.QUADRATIC, false);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        n = vars.length;
        map = new TIntIntHashMap();
        IntVar v;
        int ubtmp;
        int idx = n;
        valuesToCompute = new TIntArrayList();
        boundedVariables = new TIntArrayList();
        for (int i = 0; i < n; i++) {
            v = vars[i];
            if (!v.hasEnumeratedDomain()) {
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
        digraph = new DirectedGraph(solver.getEnvironment(), n2 + 1, SetType.LINKED_LIST, false);
        remProc = new DirectedRemProc();
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
        //
        this.lb = new int[n2];
        this.ub = new int[n2];
        this.flow = new IStateInt[n2];
        for (int i = 0; i < n; i++) {
            ub[i] = lb[i] = 1; // 1 unit of flow per variable
            flow[i] = environment.makeInt(0);
        }
        for (int i = n; i < n2; i++) {
            ub[i] = n; // [0,n] units of flow per value (default)
            flow[i] = environment.makeInt(0);
        }
        for (int i = 0; i < value.length; i++) {
            idx = map.get(value[i]);
            if (lb[idx] != 0 && lb[idx] != low[i]
                    || ub[idx] != n && ub[idx] != up[i]) {
                throw new UnsupportedOperationException("error in the use of GCC: duplication of value " + value[i]);
            }
            lb[idx] = low[i];
            ub[idx] = up[i];
            if (low[i] > up[i]) {
                throw new UnsupportedOperationException("GCC error: low[i]>up[i]");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropGCC_LowUp_AC(");
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
        digraph.desactivateNode(n2);
        for (int i = 0; i < n2; i++) {
            flow[i].set(0);
            digraph.getSuccessorsOf(i).clear();
            digraph.getPredecessorsOf(i).clear();
        }
        int j, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.instantiated()) {
                j = map.get(ub);
                if (flow[j].get() < this.ub[j]) {
                    digraph.addArc(j, i);
                    flow[i].add(1);
                    flow[j].add(1);
                } else {
                    contradiction(v, "");
                }
            } else {
                for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                    j = map.get(k);
                    digraph.addArc(i, j);
                }
            }
        }
    }

    //***********************************************************************************
    // MATCHING
    //***********************************************************************************

    private void repairMatching() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            if (flow[i].get() == 0) {
                assignVariable(i);
            }
        }
        for (int i = n; i < n2; i++) {
            while (flow[i].get() < lb[i]) {
                useValue(i);
            }
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
        in.set(root);//TODO CORRECTION JG 15/11/12 to test on minizinc instance
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
                    if (flow[y].get() < this.ub[y]) {
                        if (y < n) {
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
        in.set(root);//TODO CORRECTION JG 15/11/12 to test on minizinc instance
        int x, y;
        ISet succs;
        while (indexFirst != indexLast) {
            x = fifo[indexFirst++];
            succs = digraph.getPredecessorsOf(x);
//			succs = digraph.getSuccessorsOf(x);
            for (y = succs.getFirstElement(); y >= 0; y = succs.getNextElement()) {
                if (!in.get(y)) {
                    father[y] = x;
                    fifo[indexLast++] = y;
                    in.set(y);
                    if (flow[y].get() > this.lb[y]) {
//					if (flow[y].get()<this.ub[y]){
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
            if (flow[i].get() < ub[i]) {
                digraph.addArc(i, n2);
            }
            if (flow[i].get() > lb[i]) {
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
            if (v.getLB() != ub) {// i.e. v is not instantiated
                for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                    j = map.get(k);
                    if (nodeSCC[i] != nodeSCC[j]) {
                        if (digraph.arcExists(j, i)) {
                            v.instantiateTo(k, aCause);
                            ISet nei = digraph.getSuccessorsOf(i);
                            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                                digraph.removeArc(i, s);
                            }
                        } else {
                            v.removeValue(k, aCause);
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
                if (!(digraph.arcExists(i, j) || digraph.arcExists(j, i))) {
                    v.removeValue(k, aCause);
                }
            }
            int lb = v.getLB();
            for (int k = v.getUB(); k >= lb; k--) {
                j = map.get(k);
                if (!(digraph.arcExists(i, j) || digraph.arcExists(j, i))) {
                    v.removeValue(k, aCause);
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
            repairMatching();
        }
        filter();
        for (int i = 0; i < n; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        valuesToCompute.clear();
        idms[varIdx].freeze();
        idms[varIdx].forEach(remProc.set(varIdx), EventType.REMOVE);
        idms[varIdx].unfreeze();
        if (flow[varIdx].get() == 0) {
            assignVariable(varIdx);
        }
        int val;
        for (int i = valuesToCompute.size() - 1; i >= 0; i--) {
            val = valuesToCompute.get(i);
            if (flow[val].get() < lb[val]) {
                useValue(val);
            }
        }
        forcePropagate(EventType.CUSTOM_PROPAGATION);
//		propagate(EventType.FULL_PROPAGATION.mask);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        int[] f = new int[n2];
        if (isCompletelyInstantiated()) {
            for (int i = 0; i < n; i++) {
                f[map.get(vars[i].getValue())]++;
            }
            for (int i = n; i < n2; i++) {
                if (f[i] < lb[i] || f[i] > ub[i]) {
                    return ESat.FALSE;
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private class DirectedRemProc implements UnaryIntProcedure<Integer> {
        int idx;

        public void execute(int i) throws ContradictionException {
            i = map.get(i);
            if (digraph.arcExists(idx, i)) {
                digraph.removeArc(idx, i);
            } else if (digraph.arcExists(i, idx)) {
                digraph.removeArc(i, idx);
                flow[idx].add(-1);
                flow[i].add(-1);
                if (flow[i].get() < lb[i]) {
                    valuesToCompute.add(i);
                }
            }
        }

        @Override
        public UnaryIntProcedure set(Integer idx) {
            this.idx = idx;
            return this;
        }
    }
}
