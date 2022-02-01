/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.count;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import static org.chocosolver.solver.constraints.PropagatorPriority.LINEAR;
import static org.chocosolver.util.tools.ArrayUtils.concat;

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

    private final int n;
    private final int value;
    private final ISet possibles;
    private final ISet mandatories;

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
        super(concat(decvars, valueCardinality), LINEAR, true);
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
                ISetIterator iter = possibles.iterator();
                while (iter.hasNext()) {
                    vars[iter.nextInt()].instantiateTo(value, this);
                }
                setPassive();
            } else if (mandatories.size() == nb) {
                ISetIterator iter = possibles.iterator();
                while (iter.hasNext()) {
                    int j = iter.nextInt();
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

}
