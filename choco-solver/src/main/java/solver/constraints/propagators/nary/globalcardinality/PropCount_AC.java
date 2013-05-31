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

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

/**
 * Define a COUNT constraint setting size{forall v in lvars | v = occval} = occVar
 * assumes the occVar variable to be the last of the variables of the constraint:
 * vars = [lvars | occVar]
 * Arc Consistent algorithm
 * with  lvars = list of variables for which the occurrence of occval in their domain is constrained
 * <br/>
 *
 * @author Jean-Guillaume Fages
 */
public class PropCount_AC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int value;
    private ISet possibles, mandatories;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Count Constraint for integer variables
     * Performs Arc Consistency
     *
     * @param decvars
     * @param restrictedValue
     * @param valueCardinality
     */
    public PropCount_AC(IntVar[] decvars, int restrictedValue, IntVar valueCardinality) {
        super(ArrayUtils.append(decvars, new IntVar[]{valueCardinality}), PropagatorPriority.LINEAR, true);
        this.value = restrictedValue;
        this.n = decvars.length;
        this.possibles = SetFactory.makeStoredSet(SetType.BITSET, n, environment);
        this.mandatories = SetFactory.makeStoredSet(SetType.BITSET, n, environment);
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropFastCount_(");
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
    public boolean advise(int varIdx, int mask) {
        if (super.advise(varIdx, mask)) {
            if (varIdx < n) {
                if (possibles.contain(varIdx)) {
                    if (!vars[varIdx].contains(value)) {
                        possibles.remove(varIdx);
                        return true;
                    } else if (vars[varIdx].instantiated()) {
                        possibles.remove(varIdx);
                        mandatories.add(varIdx);
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {// initialization
            mandatories.clear();
            possibles.clear();
            for (int i = 0; i < n; i++) {
                IntVar v = vars[i];
                int ub = v.getUB();
                if (v.instantiated()) {
                    if (ub == value) {
                        mandatories.add(i);
                    }
                } else {
                    if (v.contains(value)) {
                        possibles.add(i);
                    }
                }
            }
        }
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    private void filter() throws ContradictionException {
        vars[n].updateLowerBound(mandatories.getSize(), aCause);
        vars[n].updateUpperBound(mandatories.getSize() + possibles.getSize(), aCause);
        if (vars[n].instantiated()) {
            int nb = vars[n].getValue();
            if (possibles.getSize() + mandatories.getSize() == nb) {
                for (int j = possibles.getFirstElement(); j >= 0; j = possibles.getNextElement()) {
                    vars[j].instantiateTo(value, aCause);
                }
                setPassive();
            } else if (mandatories.getSize() == nb) {
                for (int j = possibles.getFirstElement(); j >= 0; j = possibles.getNextElement()) {
                    if (vars[j].removeValue(value, aCause)) {
                        possibles.remove(j);
                    }
                }
                if (possibles.isEmpty()) {
                    setPassive();
                }
            }
        }
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
        int min = 0;
        int max = 0;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            if (v.instantiatedTo(value)) {
                min++;
                max++;
            } else {
                if (v.contains(value)) {
                    max++;
                }
            }
        }
        if (vars[n].getLB() > max || vars[n].getUB() < min) {
            return ESat.FALSE;
        }
        if (!(vars[n].instantiated() && max == min)) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }
}
