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
            sdm[i].unfreeze();
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
        sdm[idxVarInProp].freeze();
        if (idxVarInProp < k) {
            sdm[idxVarInProp].forEach(onSetAddToKer, SetEventType.ADD_TO_KER);
        } else {
            sdm[idxVarInProp].forEach(onIntersectionRemoveFromEnv, SetEventType.REMOVE_FROM_ENVELOPE);
        }
        sdm[idxVarInProp].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        // Let PropIntersection do the work.
        return ESat.TRUE;
    }
}
