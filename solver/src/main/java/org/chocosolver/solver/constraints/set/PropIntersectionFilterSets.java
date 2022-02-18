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

import gnu.trove.map.hash.TIntIntHashMap;
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

/**
 *
 * @author jimmy
 */
public class PropIntersectionFilterSets extends Propagator<SetVar> {

    private final int k;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure onSetAddToKer, onIntersectionRemoveFromEnv;

    public PropIntersectionFilterSets(SetVar[] sets, SetVar intersection) {
        super(ArrayUtils.append(sets, new SetVar[]{intersection}), PropagatorPriority.QUADRATIC, true);
        if (sets.length == 0) {
            throw new IllegalArgumentException("The intersection of zero sets is undefined.");
        }
        if (sets.length == 1) {
            throw new IllegalArgumentException("This propagator does not work for a single set, "
                    + "use new PropAllEqual(new SetVar[]{sets[0], intersection}) instead.");
        }
        k = sets.length;
        sdm = new ISetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        onSetAddToKer = j -> {
            if (!vars[k].getUB().contains(j)) {
                SetVar uniqueSet = findUniqueSetThatDoesNotContainJInLB(j);
                if (uniqueSet != null) {
                    uniqueSet.remove(j, this);
                }
            }
        };
        onIntersectionRemoveFromEnv = j -> {
            SetVar uniqueSet = findUniqueSetThatDoesNotContainJInLB(j);
            if (uniqueSet != null) {
                uniqueSet.remove(j, this);
            }
        };
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < k) {
            return SetEventType.ADD_TO_KER.getMask();
        } else {
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        }
    }

    private SetVar findSetThatDoesNotContainJInLB(int j) {
        for (int i = 0; i < k - 1; i++) {
            if (!vars[i].getLB().contains(j)) {
                return vars[i];
            }
        }
        return vars[k - 1];
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SetVar intersection = vars[k];
        // Maps elements in sets to the number of occurences of those elements.
        TIntIntHashMap count = new TIntIntHashMap();
        for (int i = 0; i < k; i++) {
            ISetIterator iter = vars[i].getLB().iterator();
            while (iter.hasNext()) {
                int j = iter.nextInt();
                if (!intersection.getUB().contains(j) &&
                    count.adjustOrPutValue(j, 1, 1) >= k - 1) {
                    // intersection does not contain j but k - 1 sets contains
                    // j, hence the last set must not contain j.
                    findSetThatDoesNotContainJInLB(j).remove(j, this);
                }
            }
        }
        for (int i = 0; i <= k; i++) {
            sdm[i].startMonitoring();
        }
    }

    private SetVar findUniqueSetThatDoesNotContainJInLB(int j) {
        SetVar uniqueSet = null;
        for (int i = 0; i < k; i++) {
            if (!vars[i].getLB().contains(j)) {
                if (uniqueSet != null) {
                    return null;
                }
                uniqueSet = vars[i];
            }
        }
        return uniqueSet;
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < k) {
            sdm[idxVarInProp].forEach(onSetAddToKer, SetEventType.ADD_TO_KER);
        } else {
            sdm[idxVarInProp].forEach(onIntersectionRemoveFromEnv, SetEventType.REMOVE_FROM_ENVELOPE);
        }
    }

    @Override
    public ESat isEntailed() {
        // Let PropIntersection do the work.
        return ESat.TRUE;
    }
}
