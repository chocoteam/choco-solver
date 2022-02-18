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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Channeling between set variables and integer variables
 * x in sets[y-offSet1] <=> ints[x-offSet2] = y
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int nInts;
    private final int nSets;
    private int idx;
    private final SetVar[] sets;
    private final IntVar[] ints;
    private final int offSet1;
    private final int offSet2;
    private final ISetDeltaMonitor[] sdm;
    private final IIntDeltaMonitor[] idm;
    private final IntProcedure elementForced;
    private final IntProcedure elementRemoved;
    private final IntProcedure valRem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Channeling between set variables and integer variables
     * x in sets[y-offSet1] <=> ints[x-offSet2] = y
     */
    public PropIntChannel(SetVar[] setsV, IntVar[] intsV, final int offSet1, final int offSet2) {
        super(ArrayUtils.append(setsV, intsV), PropagatorPriority.LINEAR, true);
        this.nSets = setsV.length;
        this.nInts = intsV.length;
        this.sets = new SetVar[nSets];
        this.ints = new IntVar[nInts];
        this.idm = new IIntDeltaMonitor[nInts];
        this.sdm = new ISetDeltaMonitor[nSets];
        this.offSet1 = offSet1;
        this.offSet2 = offSet2;
        for (int i = 0; i < nInts; i++) {
            this.ints[i] = (IntVar) vars[i + nSets];
            this.idm[i] = this.ints[i].monitorDelta(this);
        }
        for (int i = 0; i < nSets; i++) {
            this.sets[i] = (SetVar) vars[i];
            this.sdm[i] = this.sets[i].monitorDelta(this);
        }
        // procedures
        elementForced = element -> ints[element - offSet2].instantiateTo(idx, this);
        elementRemoved = element -> ints[element - offSet2].removeValue(idx, this);
        valRem = element -> sets[element - offSet1].remove(idx, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < nInts; i++) {
            ints[i].updateBounds(offSet1, nSets - 1 + offSet1, this);
        }
        for (int i = 0; i < nInts; i++) {
            int ub = ints[i].getUB();
            for (int j = ints[i].getLB(); j <= ub; j = ints[i].nextValue(j)) {
                if (!sets[j - offSet1].getUB().contains(i + offSet2)) {
                    ints[i].removeValue(j, this);
                }
            }
            if (ints[i].isInstantiated()) {
                sets[ints[i].getValue() - offSet1].force(i + offSet2, this);
            }
        }
        for (int i = 0; i < nSets; i++) {
            ISetIterator iter = sets[i].getUB().iterator();
            while (iter.hasNext()) {
                int j = iter.nextInt();
                if (j < offSet2 || j > nInts - 1 + offSet2 || !ints[j - offSet2].contains(i + offSet1)) {
                    sets[i].remove(j, this);
                }
            }
            iter = sets[i].getLB().iterator();
            while (iter.hasNext()) {
                ints[iter.nextInt() - offSet2].instantiateTo(i + offSet1, this);
            }
        }
        for (int i = 0; i < nSets; i++) {
            sdm[i].startMonitoring();
        }
        for (int i = 0; i < nInts; i++) {
            idm[i].startMonitoring();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        idx = idxVarInProp;
        if (idx < nSets) {
            idx += offSet1;
            sdm[idxVarInProp].forEach(elementForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        } else {
            idx -= nSets;
            if (ints[idx].isInstantiated()) {
                sets[ints[idx].getValue() - offSet1].force(idx + offSet2, this);
            }
            idx += offSet2;
            idm[idxVarInProp - nSets].forEachRemVal(valRem);
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < nInts; i++) {
            if (ints[i].isInstantiated()) {
                int val = ints[i].getValue();
                if (val < offSet1 || val >= nSets + offSet1 || !sets[val - offSet1].getUB().contains(i + offSet2)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < nSets; i++) {
            ISetIterator iter = sets[i].getLB().iterator();
            while (iter.hasNext()) {
                int j = iter.nextInt();
                if (j < offSet2 || j >= nInts + offSet2 || !ints[j - offSet2].contains(i + offSet1)) {
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
