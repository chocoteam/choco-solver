/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.count;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

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
     * @param decvars          an array of integer variables
     * @param restrictedValue  integer variable
     * @param valueCardinality integer variable
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
        for (; i < Math.min(4, vars.length-1); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("..., ");
        }
        st.append(vars[vars.length - 1].getName());
	    st.append(", value=").append(val.getName());
	    st.append(", cardinality=").append(card.getName());
        return st.toString();
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int minCard = MAX_VALUE / 10;
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
                val.removeValue(value, this);
            } else {
                minCard = min(minCard, min);
                maxCard = max(maxCard, max);
            }
        }
        card.updateBounds(minCard, maxCard, this);
        if (val.isInstantiated() && card.isInstantiated()) {
            int nb = card.getValue();
            int value = val.getValue();
            if (maxCard == nb) {
                for (int i = 0; i < n; i++) {
                    if (vars[i].contains(value)) {
                        vars[i].instantiateTo(value, this);
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
                            vars[i].removeValue(value, this);
                        }
                    }
                }
                card.instantiateTo(nbInst, this);
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == vars.length - 2) {// cardinality variables
            return IntEventType.boundAndInst();
        }
        return IntEventType.all();
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
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = ruleStore.addPropagatorActivationRule(this);
        if (var == val) { // deal with "val" variable
            assert evt == IntEventType.REMOVE;
            // "value" is the removed value:
            // either not enough variable has "value" in their domain,
            // or too many has not "value"
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(value)) {
                    nrules |= ruleStore.addFullDomainRule(vars[i]);
                } else {
                    nrules |= ruleStore.addRemovalRule(vars[i], value);
                }
            }
            nrules |= ruleStore.addBoundsRule(card);
        } else if (var == card) { // deal with "card" variable
            assert IntEventType.isBound(evt.getMask()) || IntEventType.isInstantiate(evt.getMask());
            for (int i = 0; i < n; i++) {
                nrules |= ruleStore.addFullDomainRule(vars[i]);
            }
            nrules |= ruleStore.addFullDomainRule(val);
        } else {
            assert evt == IntEventType.REMOVE || evt == IntEventType.INSTANTIATE;
            for (int i = 0; i < n; i++) {
                if (var != vars[i]) {
                    if (vars[i].isInstantiatedTo(value)) {
                        nrules |= ruleStore.addFullDomainRule(vars[i]);
                    } else {
                        nrules |= ruleStore.addRemovalRule(vars[i], value);
                    }
                }
            }
            nrules |= ruleStore.addBoundsRule(card);
            nrules |= ruleStore.addFullDomainRule(val);
        }
        return nrules;
    }

}
