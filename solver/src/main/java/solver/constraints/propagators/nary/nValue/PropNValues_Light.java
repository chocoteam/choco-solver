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

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * No level of consistency but better than BC in general (for enumerated domains with holes)
 *
 * @author Jean-Guillaume Fages
 */
public class PropNValues_Light extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar nValues;
    private TIntArrayList concernedValues;
    private int n;
    private int[] unusedValues, mate;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for the NValues constraint
     * The number of distinct values among concerned values in the set of variables vars is exactly equal to nValues
     * No level of consistency for the filtering
     *
     * @param vars
     * @param concernedValues will be sorted!
     * @param nValues
     * @param constraint
     * @param solver
     */
    public PropNValues_Light(IntVar[] vars, TIntArrayList concernedValues, IntVar nValues, Constraint constraint, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{nValues}), solver, constraint, PropagatorPriority.QUADRATIC, true);
        n = vars.length;
        concernedValues.sort();
        this.concernedValues = concernedValues;
        this.nValues = nValues;
        unusedValues = new int[concernedValues.size()];
        mate = new int[concernedValues.size()];
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    private void filter() throws ContradictionException {
        int count = 0;
        int countMax = 0;
        int idx = 0;
        for (int i = concernedValues.size() - 1; i >= 0; i--) {
            boolean possible = false;
            boolean mandatory = true;
            mate[i] = -1;
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(concernedValues.get(i))) {
                    possible = true;
                    if (mate[i] == -1) {
                        mate[i] = i;
                    } else {
                        mate[i] = -2;
                    }
                    if (vars[v].instantiated()) {
                        mandatory = true;
                        if (mate[i] == -2) {
                            break;
                        }
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                count++;
            } else {
                unusedValues[idx++] = concernedValues.get(i);
            }
        }
        // filtering cardinality variable
        nValues.updateLowerBound(count, aCause);
        nValues.updateUpperBound(countMax, aCause);
        // filtering decision variables
        if (count == nValues.getUB()) {
            int val;
            for (int i = 0; i < idx; i++) {
                val = unusedValues[i];
                for (int v = 0; v < n; v++) {
                    vars[v].removeValue(val, aCause);
                }
            }
            for (int i = idx - 1; i >= 0; i--) {
                val = unusedValues[i];
                for (int v = 0; v < n; v++) {
                    vars[v].removeValue(val, aCause);
                }
            }
            setPassive();
        } else if (countMax == nValues.getLB()) {
            for (int i = concernedValues.size() - 1; i >= 0; i--) {
                if (mate[i] >= 0) {
                    vars[mate[i]].instantiateTo(concernedValues.get(i), aCause);
                }
            }
        }
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
        int count = 0;
        int countMax = 0;
        for (int i = 0; i < concernedValues.size(); i++) {
            boolean possible = false;
            boolean mandatory = true;
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(concernedValues.get(i))) {
                    possible = true;
                    if (vars[v].instantiated()) {
                        mandatory = true;
                        break;
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                count++;
            }
        }
        if (count > nValues.getUB()) {
            return ESat.FALSE;
        }
        if (countMax < nValues.getLB()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
