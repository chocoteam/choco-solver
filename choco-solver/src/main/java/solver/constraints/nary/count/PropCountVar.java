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
package solver.constraints.nary.count;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
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
public class PropCountVar extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private IntVar val, card;

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
    public PropCountVar(IntVar[] decvars, IntVar restrictedValue, IntVar valueCardinality) {
        super(ArrayUtils.append(decvars, new IntVar[]{valueCardinality, restrictedValue}), PropagatorPriority.QUADRATIC, false);
        this.n = decvars.length;
        this.card = this.vars[n];
        this.val = this.vars[n + 1];
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropCountVar_(");
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
        int minCard = Integer.MAX_VALUE / 10;
        int maxCard = -minCard;
        int cardLB = card.getLB();
        int cardUB = card.getUB();
        for (int value = val.getLB(); value <= val.getUB(); value = val.nextValue(value)) {
            int min = 0;
            int max = 0;
            for (int i = 0; i < n; i++) {
                IntVar v = vars[i];
                if (v.contains(value)) {
                    max++;
                    if (v.isInstantiated()) {
                        min++;
                    }
                }
            }
            if (cardLB > max || cardUB < min) {
                val.removeValue(value, aCause);
            } else {
                minCard = Math.min(minCard, min);
                maxCard = Math.max(maxCard, max);
            }
        }
        card.updateLowerBound(minCard, aCause);
        card.updateUpperBound(maxCard, aCause);
        if (val.isInstantiated() && card.isInstantiated()) {
            int nb = card.getValue();
            int value = val.getValue();
            if (maxCard == nb) {
                for (int i = 0; i < n; i++) {
                    if (vars[i].contains(value)) {
                        vars[i].instantiateTo(value, aCause);
                    }
                }
                setPassive();
            } else if (minCard == nb) {
                int nbInst = 0; // security
                for (int i = 0; i < n; i++) {
                    if (vars[i].contains(value)) {
                        if (vars[i].isInstantiated()) {
                            nbInst++;
                        } else {
                            vars[i].removeValue(value, aCause);
                        }
                    }
                }
                card.instantiateTo(nbInst, aCause);
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == n) {// cardinality variables
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        }
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        boolean support = false;
        boolean instSupport = false;
        for (int value = val.getLB(); value <= val.getUB(); value = val.nextValue(value)) {
            int min = 0;
            int max = 0;
            for (int i = 0; i < n; i++) {
                IntVar v = vars[i];
                if (v.contains(value)) {
                    max++;
                    if (v.isInstantiated()) {
                        min++;
                    }
                }
            }
            if (card.getLB() <= max && card.getUB() >= min) {
                support = true;
                if (min == max) {
                    instSupport = true;
                }
            }
        }
        if (!support) {
            return ESat.FALSE;
        }
        if (val.isInstantiated() && instSupport && card.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 2;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            this.vars[size - 1].duplicate(solver, identitymap);
            IntVar aVar1 = (IntVar) identitymap.get(this.vars[size - 1]);
            this.vars[size].duplicate(solver, identitymap);
            IntVar aVar2 = (IntVar) identitymap.get(this.vars[size]);
            identitymap.put(this, new PropCountVar(aVars, aVar2, aVar1));
        }
    }
}
