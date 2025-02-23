/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.channeling;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

import static org.chocosolver.solver.constraints.PropagatorPriority.UNARY;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * Constraints that map the boolean assignments variables (bvars) with the standard assignment variables (var).
 * var = i <-> bvars[i-offSet] = true
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 22/05/13
 */
@Explained
public class PropEnumDomainChanneling extends Propagator<IntVar> {

    private final int n;
    private final IntProcedure rem_proc;
    private final IIntDeltaMonitor idm;
    private final int offSet;

    public PropEnumDomainChanneling(BoolVar[] bvars, IntVar aVar, final int offSet) {
        super(concat(bvars, aVar), UNARY, true);
        assert aVar.hasEnumeratedDomain();
        this.n = bvars.length;
        this.offSet = offSet;
        this.idm = this.vars[n].monitorDelta(this);
        this.rem_proc = i -> vars[i - offSet].instantiateTo(0, this,
                lcg() ? Reason.r(vars[n].getLit(i, IntVar.LR_EQ)) : Reason.undef());
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[n].updateLowerBound(offSet, this, Reason.undef());
        vars[n].updateUpperBound(n - 1 + offSet, this, Reason.undef());
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                if (vars[i].getValue() == 0) {
                    vars[n].removeValue(i + offSet, this,
                            lcg() ? Reason.r(vars[i].getValLit()) : Reason.undef());
                } else {
                    vars[n].instantiateTo(i + offSet, this,
                            lcg() ? Reason.r(vars[i].getValLit()) : Reason.undef());
                }
            } else if (!vars[n].contains(i + offSet)) {
                vars[i].instantiateTo(0, this,
                        lcg() ? Reason.r(vars[n].getLit(i + offSet, IntVar.LR_EQ)) : Reason.undef());
            }
        }
        if (vars[n].isInstantiated()) {
            int v = vars[n].getValue() - offSet;
            vars[v].instantiateTo(1, this,
                    lcg() ? Reason.r(vars[n].getValLit()) : Reason.undef());
            for (int i = 0; i < n; i++) {
                if (i != v) {
                    vars[i].instantiateTo(0, this,
                            lcg() ? Reason.r(vars[n].getLit(i + offSet, IntVar.LR_EQ)) : Reason.undef());
                }
            }
        }
        idm.startMonitoring();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx == n) {
            idm.forEachRemVal(rem_proc);
        } else {
            if (vars[varIdx].getValue() == 1) {
                vars[n].instantiateTo(varIdx + offSet, this,
                        lcg() ? Reason.r(vars[varIdx].getValLit()) : Reason.undef());
                for (int i = 0; i < n; i++) {
                    if (i != varIdx) {
                        vars[i].instantiateTo(0, this,
                                lcg() ? Reason.r(vars[n].getLit(i + offSet, IntVar.LR_EQ)) : Reason.undef());
                    }
                }
            } else {
                vars[n].removeValue(varIdx + offSet, this,
                        lcg() ? Reason.r(vars[varIdx].getValLit()) : Reason.undef());
            }
        }
        if (vars[n].isInstantiated()) {
            vars[vars[n].getValue() - offSet].instantiateTo(1, this,
                    lcg() ? Reason.r(vars[n].getValLit()) : Reason.undef());
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars[n].getLB() > n - 1 + offSet || vars[n].getUB() < offSet) {
            return ESat.FALSE;
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                if (vars[i].getValue() == 1 && !vars[n].contains(i + offSet)) {
                    return ESat.FALSE;
                }
            }
        }
        if (vars[n].isInstantiated()) {
            int v = vars[n].getValue() - offSet;
            if (!vars[v].contains(1)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
