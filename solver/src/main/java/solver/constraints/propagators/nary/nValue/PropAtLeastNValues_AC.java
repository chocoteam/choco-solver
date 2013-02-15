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
package solver.constraints.propagators.nary.nValue;

import common.ESat;
import common.util.procedure.UnaryIntProcedure;
import common.util.tools.ArrayUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import common.util.objects.graphs.DirectedGraph;
import common.util.graphOperations.connectivity.StrongConnectivityFinder;
import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetType;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import java.util.BitSet;

/**
 * AtLeastNValues Propagator (similar to SoftAllDiff)
 * The number of distinct values in vars is at least nValues
 * Performs Generalized Arc Consistency based on Maximum Bipartite Matching
 * The worst case time complexity is O(nm) but this is very pessimistic
 * In practice it is more like O(m) where m is the number of variable-value pairs
 * <p/>
 * BEWARE UNSAFE : BUG DETECTED THROUGH DOOBLE(3,4,6)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtLeastNValues_AC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar nValues;
    private int n, n2;
    private DirectedGraph digraph;
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
     * AtLeastNValues Propagator (similar to SoftAllDiff)
     * The number of distinct values in vars is at least nValues
     * Performs Generalized Arc Consistency based on Maximum Bipartite Matching
     * The worst case time complexity is O(nm) but this is very pessimistic
     * In practice it is more like O(m) where m is the number of variable-value pairs
     *
     * @param vars
     * @param nValues
     */
    public PropAtLeastNValues_AC(IntVar[] vars, IntVar nValues) {
        super(ArrayUtils.append(vars, new IntVar[]{nValues}), PropagatorPriority.QUADRATIC, true);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        n = vars.length;
        this.nValues = nValues;
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
        digraph = new DirectedGraph(solver.getEnvironment(), n2 + 2, SetType.LINKED_LIST, false);
        free = new BitSet(n2);
        remProc = new DirectedRemProc();
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
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
        for (int i = 0; i < n2 + 2; i++) {
            digraph.desactivateNode(i);
        }
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                j = map.get(k);
                digraph.addArc(i, j);
            }
        }
    }

    //***********************************************************************************
    // MATCHING
    //***********************************************************************************

    private int repairMatching() throws ContradictionException {
        for (int i = free.nextSetBit(0); i >= 0 && i < n; i = free.nextSetBit(i + 1)) {
            tryToMatch(i);
        }
        int card = 0;
        for (int i = 0; i < n; i++) {
            if (digraph.getPredecessorsOf(i).getFirstElement() != -1) {
                card++;
            }
        }
        return card;
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
        digraph.desactivateNode(n2);
        digraph.desactivateNode(n2 + 1);
        digraph.activateNode(n2);
        digraph.activateNode(n2 + 1);
        //TODO CHECK THIS PART
        for (int i = 0; i < n; i++) {
            if (free.get(i)) {
                digraph.addArc(n2, i);
            } else {
                digraph.addArc(i, n2);
            }
        }
        for (int i = n; i < n2; i++) {
            if (free.get(i)) {
                digraph.addArc(i, n2 + 1);
            } else {
                digraph.addArc(n2 + 1, i);
            }
        }
        SCCfinder.findAllSCC();
        nodeSCC = SCCfinder.getNodesSCC();
        digraph.desactivateNode(n2);
        digraph.desactivateNode(n2 + 1);
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
                    if (digraph.getPredecessorsOf(i).getFirstElement() == j) {
                        v.instantiateTo(k, aCause);
                    } else {
                        v.removeValue(k, aCause);
                        digraph.removeArc(i, j);
                    }
                }
            }
            if (!v.hasEnumeratedDomain()) {
                ub = v.getUB();
                for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                    j = map.get(k);
                    if (digraph.arcExists(i, j) || digraph.arcExists(j, i)) {
                        break;
                    } else {
                        v.removeValue(k, aCause);
                    }
                }
                int lb = v.getLB();
                for (int k = ub; k >= lb; k = v.previousValue(k)) {
                    j = map.get(k);
                    if (digraph.arcExists(i, j) || digraph.arcExists(j, i)) {
                        break;
                    } else {
                        v.removeValue(k, aCause);
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
            if (n2 < n + nValues.getLB()) {
                contradiction(nValues, "");
            }
            buildDigraph();
        }
        digraph.desactivateNode(n2);
        digraph.desactivateNode(n2 + 1);
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
        int card = repairMatching();
        nValues.updateUpperBound(card, aCause);
        if (nValues.getLB() == card) {
            filter();
        }
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx < n) {
            idms[varIdx].freeze();
            idms[varIdx].forEach(remProc.set(varIdx), EventType.REMOVE);
            idms[varIdx].unfreeze();
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
    public ESat isEntailed() {
        BitSet values = new BitSet(n2);
        BitSet mandatoryValues = new BitSet(n2);
        IntVar v;
        int ub;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.instantiated()) {
                mandatoryValues.set(map.get(ub));
            }
            for (int j = v.getLB(); j <= ub; j++) {
                values.set(map.get(j));
            }
        }
        if (mandatoryValues.cardinality() >= vars[n].getUB()) {
            return ESat.TRUE;
        }
        if (values.cardinality() < vars[n].getLB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    private class DirectedRemProc implements UnaryIntProcedure<Integer> {

        int idx;

        public void execute(int i) throws ContradictionException {
			digraph.removeArc(idx,map.get(i));
			digraph.removeArc(map.get(i),idx);
        }

        @Override
        public UnaryIntProcedure set(Integer integer) {
            this.idx = integer;
            return this;
        }
    }
}
