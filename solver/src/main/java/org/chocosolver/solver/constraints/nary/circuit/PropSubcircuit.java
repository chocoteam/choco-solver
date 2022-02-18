/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * Subcircuit propagator (one circuit and several loops)
 *
 * @author Jean-Guillaume Fages
 */
public class PropSubcircuit extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int offset; // lower bound
    private final IntVar length;
    private final IStateInt[] origin;
    private final IStateInt[] end;
    private final IStateInt[] size;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropSubcircuit(IntVar[] variables, int offset, IntVar length) {
        super(ArrayUtils.append(variables, new IntVar[]{length}), PropagatorPriority.UNARY, true);
        n = variables.length;
        this.offset = offset;
        this.length = length;
        origin = new IStateInt[n];
        end = new IStateInt[n];
        size = new IStateInt[n];
        IEnvironment environment = model.getEnvironment();
        for (int i = 0; i < n; i++) {
            origin[i] = environment.makeInt(i);
            end[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************



    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            origin[i].set(i);
            end[i].set(i);
            size[i].set(1);
        }
        TIntArrayList fixedVar = new TIntArrayList();
        for (int i = 0; i < n; i++) {
            vars[i].updateBounds(offset, n - 1 + offset, this);
            if (vars[i].isInstantiated() && i + offset != vars[i].getValue()) {
                fixedVar.add(i);
            }
        }
        for (int i = 0; i < fixedVar.size(); i++) {
            varInstantiated(fixedVar.get(i), vars[fixedVar.get(i)].getValue() - offset);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int next = vars[idxVarInProp].getValue();
        if (idxVarInProp != next - offset) {
            varInstantiated(idxVarInProp, next - offset);
        }
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
        int last = end[val].get();  // last in [0, n-1]
        int start = origin[var].get(); // start in [0, n-1]
        if (origin[val].get() != val) {
            fails(); // TODO: could be more precise, for explanation purpose
        }
        if (end[var].get() != var) {
            fails(); // TODO: could be more precise, for explanation purpose
        }
        if (val == start) {
            length.instantiateTo(size[start].get(), this);
        } else {
            size[start].add(size[val].get());
            if (size[start].get() == length.getUB()) {
                vars[last].instantiateTo(start + offset, this);
                for (int i = 0; i < n; i++) {
                    if (!vars[i].isInstantiated()) {
                        vars[i].instantiateTo(i + offset, this);
                    }
                }
                setPassive();
            }
            boolean isInst = false;
            if (size[start].get() < length.getLB()) {
                if (vars[last].removeValue(start + offset, this)) {
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

    @Override
    public int getPropagationConditions(int vIdx) {
        if(vIdx < n) {
            return IntEventType.instantiation();
        }else{
            return IntEventType.VOID.getMask();
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated() && length.isInstantiated()) {
            int ct = 0;
            int first = -1;
            BitSet visited = new BitSet(n);
            for (int i = 0; i < n; i++) {
                if (vars[i].getValue() == i + offset) {
                    visited.set(i);
                    ct++;
                } else if (first == -1) {
                    first = i;
                }
            }
            if (length.getValue() + ct != n) {
                return ESat.FALSE;
            }
            if (ct == n) {
                return ESat.TRUE;
            }
            int x = first;
            do {
                if (visited.get(x)) {
                    return ESat.FALSE;
                }
                visited.set(x);
                x = vars[x].getValue() - offset;
            } while (x != first);
            if (visited.cardinality() != n) {
                return ESat.FALSE;
            }
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }

}
