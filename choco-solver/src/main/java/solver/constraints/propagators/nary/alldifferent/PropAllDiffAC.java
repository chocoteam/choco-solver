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
package solver.constraints.propagators.nary.alldifferent;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.stack.array.TIntArrayStack;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.graphOperations.connectivity.StrongConnectivityFinder;
import util.objects.graphs.DirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;
import util.procedure.UnarySafeIntProcedure;

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

    protected int n, n2;
    protected DirectedGraph digraph;
    private int[] matching;
    private int[] nodeSCC;
    protected BitSet free;
    private UnarySafeIntProcedure remProc;
    protected final IIntDeltaMonitor[] idms;
    private StrongConnectivityFinder SCCfinder;
    // for augmenting matching (BFS)
    private int[] father;
    private BitSet in;
    private TIntIntHashMap map;
    int[] fifo;
    private TIntArrayStack toCheck = new TIntArrayStack();

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param variables
     */
    public PropAllDiffAC(IntVar[] variables) {
        super(variables, PropagatorPriority.QUADRATIC, true);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
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
        digraph = new DirectedGraph(solver.getEnvironment(), n2 + 1, SetType.BITSET, false);
        free = new BitSet(n2);
        remProc = new DirectedRemProc();
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public boolean advise(int varIdx, int mask) {
        if (super.advise(varIdx, mask)) {
            idms[varIdx].freeze();
            idms[varIdx].forEach(remProc.set(varIdx), EventType.REMOVE);
            idms[varIdx].unfreeze();
            if ((mask & EventType.INSTANTIATE.mask) != 0) {
                int val = vars[varIdx].getValue();
                int j = map.get(val);
                ISet nei = digraph.getPredecessorsOf(j);
                for (int i = nei.getFirstElement(); i >= 0; i = nei.getNextElement()) {
                    if (i != varIdx) {
                        digraph.removeArc(i, j);
                        digraph.removeArc(j, i);
                    }
                }
                int i = digraph.getSuccessorsOf(j).getFirstElement();
                if (i != -1 && i != varIdx) {
                    digraph.removeArc(i, j);
                    digraph.removeArc(j, i);

                }
            }
            return true;
        }
        return false;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            toCheck.clear();
            if (n2 < n * 2) {
                contradiction(null, "");
            }
            for (int v = 0; v < n; v++) {
                if (vars[v].instantiated()) {
                    toCheck.push(v);
                }
            }
            fixpoint();
            buildDigraph();
            for (int i = 0; i < idms.length; i++) {
                idms[i].unfreeze();
            }
        } else { // incremental
            free.clear();
            for (int i = 0; i < n; i++) {
                if (digraph.getPredecessorsOf(i).getSize() == 0) {
                    free.set(i);
                }
            }
            for (int i = n; i < n2; i++) {
                if (digraph.getSuccessorsOf(i).getSize() == 0) {
                    free.set(i);
                }
            }
        }
        repairMatching();
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if ((mask & EventType.INSTANTIATE.mask) != 0) {
            toCheck.push(varIdx);
            fixpoint();
        }
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    private void fixpoint() throws ContradictionException {
        try {
            while (toCheck.size() > 0) {
                int vidx = toCheck.pop();
                int val = vars[vidx].getValue();
                for (int i = 0; i < n; i++) {
                    if (i != vidx) {
                        if (vars[i].removeValue(val, aCause)) {
                            if (vars[i].instantiated()) {
                                toCheck.push(i);
                            }
                        }

                    }
                }
            }
        } catch (ContradictionException cex) {
            toCheck.clear();
            throw cex;
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************
    @Override
    public ESat isEntailed() {
        int nbInst = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].instantiated()) {
                nbInst++;
                for (int j = i + 1; j < n; j++) {
                    if (vars[j].instantiated() && vars[i].getValue() == vars[j].getValue()) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (nbInst == vars.length) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
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

    protected void buildDigraph() {
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

    protected void repairMatching() throws ContradictionException {
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
        ISet succs;
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

    protected void filter() throws ContradictionException {
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
                        v.instantiateTo(k, aCause);
                    } else {
                        v.removeValue(k, aCause);
                        digraph.removeArc(i, j);
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            v = vars[i];
            if (!v.hasEnumeratedDomain()) {
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
    }


    private class DirectedRemProc implements UnarySafeIntProcedure<Integer> {
        int idx;

        public void execute(int i) {
            digraph.removeArc(idx, map.get(i));
            digraph.removeArc(map.get(i), idx);
        }

        @Override
        public UnarySafeIntProcedure set(Integer idx) {
            this.idx = idx;
            return this;
        }
    }
}
