/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Inverse set propagator
 * x in sets[y-offSet1] <=> y in inverses[x-offSet2]
 *
 * @since 14/01/13
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 */
public class PropInverse extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int n2;
    private int idx;
    private final SetVar[] sets;
    private final SetVar[] invsets;
    private SetVar[] toFilter;
    private final int offSet1;
    private final int offSet2;
    private int offSet;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure elementForced;
    private final IntProcedure elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Inverse set propagator
     * x in sets[y-offSet1] <=> y in inverses[x-offSet2]
     */
    public PropInverse(SetVar[] sets, SetVar[] invsets, int offSet1, int offSet2) {
        super(ArrayUtils.append(sets, invsets), PropagatorPriority.LINEAR, true);
        n = sets.length;
        n2 = invsets.length;
        this.offSet1 = offSet1;
        this.offSet2 = offSet2;
        this.sets = Arrays.copyOfRange(vars, 0, sets.length);
        this.invsets = Arrays.copyOfRange(vars, sets.length, vars.length);
        // delta monitors
        sdm = new ISetDeltaMonitor[n + n2];
        for (int i = 0; i < n + n2; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = element -> toFilter[element - offSet].force(idx, this);
        elementRemoved = element -> toFilter[element - offSet].remove(idx, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISetIterator iter;
        for (int i = 0; i < n; i++) {
            iter = sets[i].getUB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                if (j < offSet1 || j >= n2 + offSet1 || !invsets[j - offSet2].getUB().contains(i + offSet1)) {
                    sets[i].remove(j, this);
                }
            }
            iter = sets[i].getLB().iterator();
            while (iter.hasNext()){
                invsets[iter.nextInt() - offSet2].force(i + offSet1, this);
            }
        }
        for (int i = 0; i < n2; i++) {
            iter = invsets[i].getUB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                if (j < offSet2 || j >= n + offSet2 || !sets[j - offSet1].getUB().contains(i + offSet2)) {
                    invsets[i].remove(j, this);
                }
            }
            iter = invsets[i].getLB().iterator();
            while (iter.hasNext()){
                sets[iter.nextInt() - offSet1].force(i + offSet2, this);
            }
        }
        for (int i = 0; i < n + n2; i++) {
            sdm[i].startMonitoring();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        idx = idxVarInProp;
        toFilter = invsets;
        if (idx >= n) {
            idx -= n;
            toFilter = sets;
            idx += offSet2;
            offSet = offSet1;
        } else {
            idx += offSet1;
            offSet = offSet2;
        }
        sdm[idxVarInProp].forEach(elementForced, SetEventType.ADD_TO_KER);
        sdm[idxVarInProp].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j:sets[i].getLB()) {
                if (!invsets[j - offSet2].getUB().contains(i + offSet1)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j:invsets[i].getLB()) {
                if (!sets[j - offSet1].getUB().contains(i + offSet2)) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
