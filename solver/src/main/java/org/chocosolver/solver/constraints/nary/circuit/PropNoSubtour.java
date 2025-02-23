/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package org.chocosolver.solver.constraints.nary.circuit;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Simple nocircuit contraint (from NoSubtour of Pesant or noCycle of Caseaux/Laburthe)
 *
 * @author Jean-Guillaume Fages
 */
@Explained
public class PropNoSubtour extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int offset; // lower bound
    private final IStateInt[] origin;
    private final IStateInt[] end;
    private final IStateInt[] size;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that graph has no subCircuit, with Caseaux/Laburthe/Pesant algorithm
     * runs incrementally in O(1) per instantiation event
     *
     * @param variables array of integer variables
     * @param offset    offset
     */
    public PropNoSubtour(IntVar[] variables, int offset) {
        super(variables, PropagatorPriority.UNARY, true);
        n = vars.length;
        origin = new IStateInt[n];
        end = new IStateInt[n];
        size = new IStateInt[n];
        IEnvironment environment = model.getEnvironment();
        for (int i = 0; i < n; i++) {
            origin[i] = environment.makeInt(i);
            end[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
        this.offset = offset;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        TIntArrayList fixedVar = new TIntArrayList();
        for (int i = 0; i < n; i++) {
            vars[i].removeValue(i + offset, this, Reason.undef());
            vars[i].updateLowerBound(offset, this, Reason.undef());
            vars[i].updateUpperBound(n - 1 + offset, this, Reason.undef());
            if (vars[i].isInstantiated()) {
                fixedVar.add(i);
            }
        }
        for (int i = 0; i < fixedVar.size(); i++) {
            varInstantiated(fixedVar.get(i), vars[fixedVar.get(i)].getValue() - offset);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        varInstantiated(idxVarInProp, vars[idxVarInProp].getValue() - offset);
    }

    /**
     * var in [0,n-1] and val in [0,n-1]
     *
     * @param var origin
     * @param val dest
     * @throws ContradictionException if failure occurs
     */
    private void varInstantiated(int var, int val) throws ContradictionException {
        if (isPassive()) {
            return;
        }
        int last = end[val].get(); // last in [0,n-1]
        int start = origin[var].get(); // start in [0,n-1]
        if (origin[val].get() != val) {
            fails(lcg() ? Propagator.reason(null, vars) : Reason.undef());
        }
        if (end[var].get() != var) {
            // should not happen
            throw new UnsupportedOperationException("unexpected situation");
        }
        if (val == start) {
            if (size[start].get() != n) {
                // sub cycle detected ==> global failure
                throw new UnsupportedOperationException("unexpected situation");
            }
        } else {
            size[start].add(size[val].get());
            if (size[start].get() == n) {
                vars[last].instantiateTo(start + offset, this,
                        lcg() ? Propagator.reason(vars[last], vars) : Reason.undef());
                setPassive();
//                throw new UnsupportedOperationException("unexpected situation");
            }
            boolean isInst = false;
            if (size[start].get() < n && vars[last].contains(start + offset)) {
                if (vars[last].removeValue(start + offset, this, explainPrevent(start))) {
                    isInst = vars[last].isInstantiated();
                }
            }
            origin[last].set(start);
            end[start].set(last);
            if (isInst) {
                varInstantiated(last, vars[last].getValue() - offset);
            }
        }
    }

    private Reason explainPrevent(int start) {
        if (lcg()) {
            int[] ps = new int[size[start].get()];
            int m = 1;
            int var = origin[start].get();
            while (m < ps.length) {
                ps[m] = vars[var].getValLit();
                m++;
                var = vars[var].getValue() - offset;
            }
            return Reason.r(ps);
        }
        return Reason.undef();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (!vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }
        BitSet visited = new BitSet(n);
        int i = 0;
        int size = 0;
        while (size != n) {
            size++;
            i = vars[i].getValue() - offset;
            if (visited.get(i)) {
                return ESat.FALSE;
            }
            visited.set(i);
        }
        if (i == 0) {
            return ESat.TRUE;
        } else {
            return ESat.FALSE;
        }
    }

    @Override
    public String toString() {
        return "PropNoSubTour(" + Arrays.toString(vars) + ")";
    }

}
