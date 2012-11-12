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

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphType;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * No level of consistency but better than BC in general (for enumerated domains with holes)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNValues_Greedy extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar nValues;
    // graph model
    private int n;
    private UndirectedGraph digraph;
    // required data structure
    private int[] nbNeighbors;
    private BitSet in, inMIS, nodes;
    private TIntArrayList list;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for the atMostNValues constraint
     * The number of distinct values in the set of variables vars is at most equal to nValues
     * No level of consistency but better than BC in general (for enumerated domains with holes)
     *
     * @param vars
     * @param nValues
     * @param constraint
     * @param solver
     */
    public PropAtMostNValues_Greedy(IntVar[] vars, IntVar nValues, Constraint constraint, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{nValues}), solver, constraint, PropagatorPriority.QUADRATIC, true);
        n = vars.length;
        this.nValues = nValues;
        digraph = new UndirectedGraph(solver.getEnvironment(), n, GraphType.LINKED_LIST, false);
        in = new BitSet(n);
        inMIS = new BitSet(n);
        nodes = new BitSet(n);
        nbNeighbors = new int[n];
        list = new TIntArrayList();
    }

    //***********************************************************************************
    // ALGORITHMS
    //***********************************************************************************

    private void buildDigraph() {
        for (int i = 0; i < n; i++) {
            digraph.getSuccessorsOf(i).clear();
            digraph.getPredecessorsOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            for (int i2 = i + 1; i2 < n; i2++) {
                if (intersect(i, i2)) {
                    digraph.addEdge(i, i2);
                }
            }
        }
    }

    private boolean intersect(int i, int j) {
        IntVar x = vars[i];
        IntVar y = vars[j];
        if (x.getLB() > y.getUB() || y.getLB() > x.getUB()) {
            return false;
        }
        int ub = x.getUB();
        for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
            if (y.contains(val)) {
                return true;
            }
        }
        return false;
    }

    private void prefilter() throws ContradictionException {
        in.clear();
        inMIS.clear();
        for (int i = 0; i < n; i++) {
            if (vars[i].instantiated()) {
                inMIS.set(i);
                in.set(vars[i].getValue());
            }
        }
        int nv = in.cardinality();
        nValues.updateLowerBound(nv, aCause);
        if (nv == nValues.getUB()) { // remove all other values
            IntVar v;
            int ub;
            for (int i = 0; i < n; i++) {
                if (!vars[i].instantiated()) {
                    v = vars[i];
                    ub = v.getUB();
                    for (int val = v.getLB(); val <= ub; val = v.nextValue(val)) {
                        if (!in.get(val)) {
                            v.removeValue(val, aCause);
                        }
                    }
                }
            }
            setPassive();
        }
        if (nv + 1 == nValues.getUB()) { // remove values that cannot be taken by all unfixed variables
            IntVar v;
            int ub;
            nodes.clear();
            for (int i = inMIS.nextSetBit(0); i >= 0; i = inMIS.nextSetBit(i)) {
                v = vars[i];
                ub = v.getUB();
                for (int val = v.getLB(); val <= ub; val = v.nextValue(val)) {
                    if (!in.get(val)) {
                        v.removeValue(val, aCause);
                        boolean remove = false;
                        for (int k = inMIS.nextSetBit(0); k >= 0; k = inMIS.nextSetBit(k)) {
                            if (!vars[k].contains(val)) {
                                remove = true;
                                break;
                            }
                        }
                        if (remove) {
                            for (int k = inMIS.nextSetBit(0); k >= 0; k = inMIS.nextSetBit(k)) {
                                if (vars[k].removeValue(val, aCause)) {
                                    nodes.set(k);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                propagate(i, 0);
            }
        }
    }

    private int greedySearch() {
        // prepare data structures
        for (int i = 0; i < n; i++) {
            nbNeighbors[i] = 0;
        }
        in.clear();
        inMIS.clear();
        int j;
        for (int i = 0; i < n; i++) {
            in.set(i);
            nbNeighbors[i] = vars[i].getDomainSize();
        }
        ISet nei;
        list.clear();
        int min = 0;
        // find MIS
        int idx = in.nextSetBit(0);
        while (idx >= 0) {
            for (int i = in.nextSetBit(idx + 1); i >= 0; i = in.nextSetBit(i + 1)) {
                if (nbNeighbors[i] < nbNeighbors[idx]) {
                    idx = i;
                }
            }
            nei = digraph.getNeighborsOf(idx);
            in.clear(idx);
            inMIS.set(idx);
            for (j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (in.get(j)) {
                    in.clear(j);
                    list.add(j);
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                nei = digraph.getNeighborsOf(list.get(i));
                for (j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    nbNeighbors[j]--;
                }
            }
            list.clear();
            min++;
            idx = in.nextSetBit(0);
        }
        return min;
    }

    private void filter() throws ContradictionException {
        ISet nei;
        in.clear();
        int mate;
        for (int i = 0; i < n; i++) {
            if (!inMIS.get(i)) {
                mate = -1;
                nei = digraph.getNeighborsOf(i);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if (inMIS.get(j)) {
                        if (mate == -1) {
                            mate = j;
                        } else {
                            mate = -2;
                            break;
                        }
                    }
                }
                if (mate >= 0) {
                    enforce(i, mate);
                }
            }
        }
        for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
            propagate(i, 0);
        }
    }

    private void enforce(int i, int j) throws ContradictionException {
        if (i > j) {
            enforce(j, i);
        } else {
            IntVar x = vars[i];
            IntVar y = vars[j];
            boolean bx = false;
            boolean by = false;
            bx |= x.updateUpperBound(y.getUB(), aCause);
            by |= y.updateUpperBound(x.getUB(), aCause);
            bx |= x.updateLowerBound(y.getLB(), aCause);
            by |= y.updateLowerBound(x.getLB(), aCause);
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!y.contains(val)) {
                    bx |= x.removeValue(val, aCause);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!x.contains(val)) {
                    by |= y.removeValue(val, aCause);
                }
            }
            if (bx) {
                in.set(i);
            }
            if (by) {
                in.set(j);
            }
        }
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask &= EventType.FULL_PROPAGATION.mask) != 0) {
            buildDigraph();
        }
//		prefilter(); //bug?
        int min = greedySearch();
        nValues.updateLowerBound(min, aCause);
        if (min == nValues.getUB()) {
            filter();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < n) {
            ISet nei = digraph.getNeighborsOf(idxVarInProp);
            for (int v = nei.getFirstElement(); v >= 0; v = nei.getNextElement()) {
                if (!intersect(idxVarInProp, v)) {
                    digraph.removeEdge(idxVarInProp, v);
                }
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
    public ESat isEntailed() {
        BitSet values = new BitSet(nValues.getUB());
        BitSet mandatoryValues = new BitSet(nValues.getUB());
        IntVar v;
        int ub;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.instantiated()) {
                mandatoryValues.set(ub);
            }
            for (int j = v.getLB(); j <= ub; j++) {
                values.set(j);
            }
        }
        if (values.cardinality() <= vars[n].getLB()) {
            return ESat.TRUE;
        }
        if (mandatoryValues.cardinality() > vars[n].getUB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
