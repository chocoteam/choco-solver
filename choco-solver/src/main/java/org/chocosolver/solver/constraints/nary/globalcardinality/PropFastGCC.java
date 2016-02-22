/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

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
     * @param decvars array of integer variables
     * @param restrictedValues array of int
     * @param map mapping
     * @param valueCardinalities array of integer variables
     */
    public PropFastGCC(IntVar[] decvars, int[] restrictedValues, TIntIntHashMap map, IntVar[] valueCardinalities) {
        super(ArrayUtils.append(decvars, valueCardinalities), PropagatorPriority.LINEAR, false);
        if (restrictedValues.length != valueCardinalities.length) {
            throw new UnsupportedOperationException();
        }
        this.values = restrictedValues;
        this.n = decvars.length;
        this.n2 = values.length;
        this.possibles = new ISet[n2];
        this.mandatories = new ISet[n2];
        this.map = map;
        for (int idx = 0; idx < n2; idx++) {
            mandatories[idx] = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
            possibles[idx] = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        }
        this.valueToCompute = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        this.boundVar = new TIntArrayList();
        for (int i = 0; i < n; i++) {
            if (!vars[i].hasEnumeratedDomain()) {
                boundVar.add(i);
            }
        }
        super.linkVariables();
    }

    @Override
    protected void linkVariables() {
        // do nothing, the linking is postponed because getPropagationConditions() needs some internal data
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
        do {
			if (PropagatorEventType.isFullPropagation(evtmask)) {// initialization
                valueToCompute.clear();
                for (int i = 0; i < n2; i++) {
                    mandatories[i].clear();
                    possibles[i].clear();
                    valueToCompute.add(i);
                }
                for (int i = 0; i < n; i++) {
                    IntVar v = vars[i];
                    int ub = v.getUB();
                    if (v.isInstantiated()) {
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
                for (int i : valueToCompute) {
                    for (int var : possibles[i]) {
                        if (!vars[var].contains(values[i])) {
                            possibles[i].remove(var);
                        } else if (vars[var].isInstantiated()) {
                            possibles[i].remove(var);
                            mandatories[i].add(var);
                        }
                    }
                }
            }
            // filtering
            evtmask = PropagatorEventType.CUSTOM_PROPAGATION.getMask();
        } while (filter());
    }

    private boolean filter() throws ContradictionException {
        boolean again = false;
        for (int i : valueToCompute) {
            again |= vars[n + i].updateLowerBound(mandatories[i].getSize(), this);
            again |= vars[n + i].updateUpperBound(mandatories[i].getSize() + possibles[i].getSize(), this);
            if (vars[n + i].isInstantiated()) {
                if (possibles[i].getSize() + mandatories[i].getSize() == vars[n + i].getLB()) {
                    for (int j : possibles[i]) {
                        mandatories[i].add(j);
                        again |= vars[j].instantiateTo(values[i], this);
                    }
                    possibles[i].clear();
                    valueToCompute.remove(i);//value[i] restriction entailed
                } else if (mandatories[i].getSize() == vars[n + i].getUB()) {
                    for (int var : possibles[i]) {
                        again |= vars[var].removeValue(values[i], this);
                    }
                    possibles[i].clear();
                    valueToCompute.remove(i);//value[i] restriction entailed
                }
            }
        }
        // manage holes in bounded variables
        if (boundVar.size() > 0) {
            again |= filterBounds();
        }
        //if (again) {// fix point
        //    propagate(PropagatorEventType.CUSTOM_PROPAGATION.mask);
        //}
        return again;
    }

    private boolean filterBounds() throws ContradictionException {
        boolean useful = false;
        for (int i = 0; i < boundVar.size(); i++) {
            int var = boundVar.get(i);
            if (!vars[var].isInstantiated()) {
                int lb = vars[var].getLB();
                int index = -1;
                if (map.containsKey(lb)) {
                    index = map.get(lb);
                }
                boolean b = index != -1 && !(possibles[index].contain(var) || mandatories[index].contain(var));
                while (b) {
                    useful = true;
                    vars[var].removeValue(lb, this);
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
                    vars[var].removeValue(ub, this);
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
            return IntEventType.boundAndInst();
        }
        return IntEventType.all();
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
            if (v.isInstantiated()) {
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
            if (vars[n + i].getLB() > max[i] || vars[n + i].getUB() < min[i]) {
                return ESat.FALSE;
            }
        }
        for (int i = 0; i < n2; i++) {
            if (!(vars[n + i].isInstantiated() && max[i] == min[i])) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

}
