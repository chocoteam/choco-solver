/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Inverse set propagator
 * x in sets[y-offSet1] <=> y in inverses[x-offSet2]
 *
 * @author Jean-Guillaume Fages
 */
public class PropInverse extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, n2, idx;
    private SetVar[] sets, invsets, toFilter;
    private int offSet1, offSet2, offSet;
    private ISetDeltaMonitor[] sdm;
    private IntProcedure elementForced, elementRemoved;

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
        for (int i = 0; i < n; i++) {
            for (int j : sets[i].getUB()) {
                if (j < offSet1 || j >= n2 + offSet1 || !invsets[j - offSet2].getUB().contain(i + offSet1)) {
                    sets[i].remove(j, this);
                }
            }
            for (int j:sets[i].getLB()) {
                invsets[j - offSet2].force(i + offSet1, this);
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j:invsets[i].getUB()) {
                if (j < offSet2 || j >= n + offSet2 || !sets[j - offSet1].getUB().contain(i + offSet2)) {
                    invsets[i].remove(j, this);
                }
            }
            for (int j:invsets[i].getLB()) {
                sets[j - offSet1].force(i + offSet2, this);
            }
        }
        for (int i = 0; i < n + n2; i++) {
            sdm[i].unfreeze();
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
        sdm[idxVarInProp].freeze();
        sdm[idxVarInProp].forEach(elementForced, SetEventType.ADD_TO_KER);
        sdm[idxVarInProp].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        sdm[idxVarInProp].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j:sets[i].getLB()) {
                if (!invsets[j - offSet2].getUB().contain(i + offSet1)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j:invsets[i].getLB()) {
                if (!sets[j - offSet1].getUB().contain(i + offSet2)) {
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
