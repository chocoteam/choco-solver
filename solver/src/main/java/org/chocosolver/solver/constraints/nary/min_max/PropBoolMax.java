/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropBoolMax extends Propagator<BoolVar> {

    private final int n;
    private final int[] lits;

    public PropBoolMax(BoolVar[] variables, BoolVar maxVar) {
        super(ArrayUtils.concat(variables, maxVar), PropagatorPriority.UNARY, true);
        n = variables.length;
        lits = new int[]{n-1, 0};
        assert n > 0;
    }

    private void find(int l) throws ContradictionException {
        int last = lits[l];
        int otl = lits[1-l];
        int last_cache = last;
        do{
            last++;
            if(last >= n){
                last = 0;
            }
            if(otl != last &&
                    (!vars[last].isInstantiated() || vars[last].isInstantiatedTo(1))){
                lits[l] = last;
                return;
            }
        }while(last != last_cache);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(vars[n].isInstantiatedTo(0)) {
            for (int i = 0; i < n; i++) {
                vars[i].instantiateTo(0, this);
            }
            return;
        }
        for(int i = 0; i < n; i++){
            if (vars[i].isInstantiatedTo(1)) {
                vars[n].instantiateTo(1, this);
                if (vars[n].isInstantiatedTo(1)) {
                    setPassive();
                    return;
                }
            }
        }
        find(0);
        find(1);
        filter();
    }

    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == n) {
            if(vars[lits[0]].isInstantiated()){
                find(0);
            }
            if (vars[lits[1]].isInstantiated()) {
                find(1);
            }
            filter();
        } else {
            if (vars[idxVarInProp].isInstantiatedTo(1)) {
                vars[n].instantiateTo(1, this);
                if (vars[n].isInstantiatedTo(1)) {
                    setPassive();
                }
            } else if (idxVarInProp == lits[0]){
                find(0);
                if(vars[lits[1]].isInstantiated()){
                    find(1);
                }
                filter();
            } else if(idxVarInProp == lits[1]) {
                find(1);
                if(vars[lits[0]].isInstantiated()){
                    find(0);
                }
                filter();
            }
        }
    }

    public void filter() throws ContradictionException {
        int l0 = lits[0];
        int l1 = lits[1];
        int b1 = vars[l0].isInstantiated()? vars[l0].getValue():2;
        int b2 = vars[l1].isInstantiated()? vars[l1].getValue():2;
        int bn = vars[n].isInstantiated()? vars[n].getValue():2;

        if(bn == 0) {
            for (int i = 0; i < n; i++) {
                vars[i].instantiateTo(0, this);
            }
        }else if(b1 == 0 && b2 == 0){
            vars[n].instantiateTo(0, this);
            if(!isPassive())setPassive();
        }else if(bn == 1){
            if(b1 == 0){
                vars[l1].instantiateTo(1, this);
            }else if(b2 == 0){
                vars[l0].instantiateTo(1, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int ub = vars[n].getUB();
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() > ub) {
                return ESat.FALSE;
            }
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].getUB() > ub) {
                return ESat.UNDEFINED;
            }
        }
        if (vars[n].isInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(ub)) {
                    return ESat.TRUE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vars[n]).append(" = max(");
        sb.append(vars[0]);
        for (int i = 1; i < n; i++) {
            sb.append(", ");
            sb.append(vars[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
