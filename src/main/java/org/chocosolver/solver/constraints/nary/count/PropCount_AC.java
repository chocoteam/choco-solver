/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.count;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

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
     * @param decvars          array of integer variables
     * @param restrictedValue  int
     * @param valueCardinality integer variable
     */
    public PropCount_AC(IntVar[] decvars, int restrictedValue, IntVar valueCardinality) {
        super(ArrayUtils.append(decvars, new IntVar[]{valueCardinality}), PropagatorPriority.LINEAR, true);
        this.value = restrictedValue;
        this.n = decvars.length;
        this.possibles = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        this.mandatories = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropFastCount_(");
        int i = 0;
        for (; i < Math.min(4, vars.length-1); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("..., ");
        }
        st.append("limit=").append(vars[vars.length - 1].getName());
	    st.append(", value=").append(value).append(')');
        return st.toString();
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {// initialization
            mandatories.clear();
            possibles.clear();
            for (int i = 0; i < n; i++) {
                IntVar v = vars[i];
                int ub = v.getUB();
                if (v.isInstantiated()) {
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
        if (varIdx < n) {
            if (possibles.contains(varIdx)) {
                if (!vars[varIdx].contains(value)) {
                    possibles.remove(varIdx);
                    filter();
                } else if (vars[varIdx].isInstantiated()) {
                    possibles.remove(varIdx);
                    mandatories.add(varIdx);
                    filter();
                }
            }
        } else {
            filter();
        }
    }

    private void filter() throws ContradictionException {
        vars[n].updateBounds(mandatories.size(), mandatories.size() + possibles.size(), this);
        if (vars[n].isInstantiated()) {
            int nb = vars[n].getValue();
            if (possibles.size() + mandatories.size() == nb) {
                for (int j : possibles) {
                    vars[j].instantiateTo(value, this);
                }
                setPassive();
            } else if (mandatories.size() == nb) {
                for (int j : possibles) {
                    if (vars[j].removeValue(value, this)) {
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
        if (vIdx == vars.length - 1) {// cardinality variables
            return IntEventType.boundAndInst();
        }
        return IntEventType.all();
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            if (v.isInstantiatedTo(value)) {
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
        if (!(vars[n].isInstantiated() && max == min)) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = ruleStore.addPropagatorActivationRule(this);
        if (var == vars[n]) {
            boolean isDecUpp = evt == IntEventType.DECUPP;
            for (int i = 0; i < n; i++) {
                if (vars[i].contains(value)) {
                    if (vars[i].isInstantiated()) {
                        nrules |= ruleStore.addFullDomainRule(vars[i]);
                    }
                } else if (isDecUpp) {
                    nrules |= ruleStore.addRemovalRule(vars[i], value);
                }
            }
        } else {
            nrules |= ruleStore.addBoundsRule(vars[n]);
            if (evt == IntEventType.REMOVE) {
                for (int i = 0; i < n; i++) {
                    if (vars[i].isInstantiatedTo(value)) {
                        nrules |= ruleStore.addFullDomainRule(vars[i]);
                    }
                }
            } else {
                for (int i = 0; i < n; i++) {
                    nrules |= ruleStore.addFullDomainRule(vars[i]);
                }
            }
        }
        return nrules;
    }
}
