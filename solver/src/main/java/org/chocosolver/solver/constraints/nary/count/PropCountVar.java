/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.count;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
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
        super(ArrayUtils.concat(decvars, valueCardinality, restrictedValue), PropagatorPriority.QUADRATIC, false);
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


}
