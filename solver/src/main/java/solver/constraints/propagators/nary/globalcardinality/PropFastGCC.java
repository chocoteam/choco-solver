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
package solver.constraints.propagators.nary.globalcardinality;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.setDataStructures.ISet;
import choco.kernel.memory.setDataStructures.SetFactory;
import choco.kernel.memory.setDataStructures.SetType;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Propagator for Global Cardinality Constraint (GCC) for integer variables
 * Basic filter: no particular consistency but fast and with a correct checker
 *
 * @author Jean-Guillaume Fages
 */
public class PropFastGCC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, n2;
    private int[] values;
    private IntVar[] cards;
    private ISet[] possibles, mandatories;
    private ISet valueToCompute;
    private TIntIntHashMap map;
    private TIntArrayList boundVar;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Global Cardinality Constraint (GCC) for integer variables
     * Basic filter: no particular consistency but fast and with a correct checker
     *
     * @param decvars
     * @param restrictedValues
     * @param map
     * @param valueCardinalities
     * @param constraint
     * @param sol
     */
    public PropFastGCC(IntVar[] decvars, int[] restrictedValues, TIntIntHashMap map, IntVar[] valueCardinalities, Constraint constraint, Solver sol) {
        super(ArrayUtils.append(decvars, valueCardinalities), PropagatorPriority.LINEAR, false);
        if (restrictedValues.length != valueCardinalities.length) {
            throw new UnsupportedOperationException();
        }
        this.values = restrictedValues;
        this.cards = valueCardinalities;
        this.n = decvars.length;
        this.n2 = values.length;
        this.possibles = new ISet[n2];
        this.mandatories = new ISet[n2];
        this.map = map;
        for (int idx = 0; idx < n2; idx++) {
            mandatories[idx] = SetFactory.makeStoredSet(SetType.LINKED_LIST, n, environment);
            possibles[idx] = SetFactory.makeStoredSet(SetType.LINKED_LIST, n, environment);
        }
        this.valueToCompute = SetFactory.makeStoredSet(SetType.SWAP_ARRAY, n2, environment);
        this.boundVar = new TIntArrayList();
        for (int i = 0; i < decvars.length; i++) {
            if (!decvars[i].hasEnumeratedDomain()) {
                boundVar.add(i);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropFastGCC_(");
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
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {// initialization
            valueToCompute.clear();
            for (int i = 0; i < n2; i++) {
                mandatories[i].clear();
                possibles[i].clear();
                valueToCompute.add(i);
            }
            for (int i = 0; i < n; i++) {
                IntVar v = vars[i];
                int ub = v.getUB();
                if (v.instantiated()) {
                    if (map.containsKey(v.getValue())) {
                        int j = map.get(v.getValue());
                        mandatories[j].add(i);
                    }
                } else {
                    for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                        if (map.containsKey(k)) {
                            int j = map.get(k);
                            possibles[j].add(i);
                        }
                    }
                }
            }
        } else {//lazy update
            for (int i = valueToCompute.getFirstElement(); i >= 0; i = valueToCompute.getNextElement()) {
                for (int var = possibles[i].getFirstElement(); var >= 0; var = possibles[i].getNextElement()) {
                    if (!vars[var].contains(values[i])) {
                        possibles[i].remove(var);
                    } else if (vars[var].instantiated()) {
                        possibles[i].remove(var);
                        mandatories[i].add(var);
                    }
                }
            }
        }
        // filtering
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    private void filter() throws ContradictionException {
        boolean again = false;
        for (int i = valueToCompute.getFirstElement(); i >= 0; i = valueToCompute.getNextElement()) {
            cards[i].updateLowerBound(mandatories[i].getSize(), aCause);
            cards[i].updateUpperBound(mandatories[i].getSize() + possibles[i].getSize(), aCause);
            if (cards[i].instantiated()) {
                if (possibles[i].getSize() + mandatories[i].getSize() == cards[i].getLB()) {
                    for (int j = possibles[i].getFirstElement(); j >= 0; j = possibles[i].getNextElement()) {
                        mandatories[i].add(j);
                        if (vars[j].instantiateTo(values[i], aCause)) {
                            again = true;
                        }
                    }
                    possibles[i].clear();
                    valueToCompute.remove(i);//value[i] restriction entailed
                } else if (mandatories[i].getSize() == cards[i].getUB()) {
                    for (int var = possibles[i].getFirstElement(); var >= 0; var = possibles[i].getNextElement()) {
                        if (vars[var].removeValue(values[i], aCause)) {
                            again = true;
                        }
                    }
                    possibles[i].clear();
                    valueToCompute.remove(i);//value[i] restriction entailed
                }
            }
        }
        // manage holes in bounded variables
        if (boundVar.size() > 0) {
            if (filterBounds()) {
                again = true;
            }
        }
        if (again) {// fix point
            propagate(EventType.CUSTOM_PROPAGATION.mask);
        }
    }

    private boolean filterBounds() throws ContradictionException {
        boolean useful = false;
        for (int i = 0; i < boundVar.size(); i++) {
            int var = boundVar.get(i);
            if (!vars[var].instantiated()) {
                int lb = vars[var].getLB();
                int index = -1;
                if (map.containsKey(lb)) {
                    index = map.get(lb);
                }
                boolean b = index != -1 && !(possibles[index].contain(var) || mandatories[index].contain(var));
                while (b) {
                    useful = true;
                    vars[var].removeValue(lb, aCause);
                    lb = vars[var].getLB();
                    index = -1;
                    if (map.containsKey(lb)) {
                        index = map.get(lb);
                    }
                    b = index != -1 && !(possibles[index].contain(var) || mandatories[index].contain(var));
                }
                int ub = vars[var].getUB();
                index = -1;
                if (map.containsKey(ub)) {
                    index = map.get(ub);
                }
                b = index != -1 && !(possibles[index].contain(var) || mandatories[index].contain(var));
                while (b) {
                    useful = true;
                    vars[var].removeValue(ub, aCause);
                    ub = vars[var].getUB();
                    index = -1;
                    if (map.containsKey(ub)) {
                        index = map.get(ub);
                    }
                    b = index != -1 && !(possibles[index].contain(var) || mandatories[index].contain(var));
                }
            } else {
                int val = vars[var].getValue();
                if (map.containsKey(val)) {
                    int index = map.get(val);
                    if (!(possibles[index].contain(var) || mandatories[index].contain(var))) {
                        contradiction(vars[var], "");
                    }
                }
            }
        }
        return useful;
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx >= n) {// cardinality variables
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        }
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        int[] min = new int[n2];
        int[] max = new int[n2];
        int j, k, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.instantiated()) {
                if (map.containsKey(v.getValue())) {
                    j = map.get(v.getValue());
                    min[j]++;
                    max[j]++;
                }
            } else {
                for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                    if (map.containsKey(k)) {
                        j = map.get(k);
                        max[j]++;
                    }
                }
            }
        }
        for (int i = 0; i < n2; i++) {
            if (cards[i].getLB() > max[i] || cards[i].getUB() < min[i]) {
                return ESat.FALSE;
            }
        }
        for (int i = 0; i < n2; i++) {
            if (!(cards[i].instantiated() && max[i] == min[i])) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }
}
