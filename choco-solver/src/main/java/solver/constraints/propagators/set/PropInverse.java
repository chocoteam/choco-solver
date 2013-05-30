/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.propagators.set;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.delta.monitor.SetDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;
import util.tools.ArrayUtils;

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
    private SetDeltaMonitor[] sdm;
    private IntProcedure elementForced, elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Inverse set propagator
     * x in sets[y-offSet1] <=> y in inverses[x-offSet2]
     */
    public PropInverse(SetVar[] sets, SetVar[] invsets, int offSet1, int offSet2) {
        super(ArrayUtils.append(sets, invsets), PropagatorPriority.LINEAR, false);
        n = sets.length;
        n2 = invsets.length;
        this.offSet1 = offSet1;
        this.offSet2 = offSet2;
        this.sets = Arrays.copyOfRange(vars, 0, sets.length);
        this.invsets = Arrays.copyOfRange(vars, sets.length, vars.length);
        // delta monitors
        sdm = new SetDeltaMonitor[n + n2];
        for (int i = 0; i < n + n2; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                toFilter[element - offSet].addToKernel(idx, aCause);
            }
        };
        elementRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                toFilter[element - offSet].removeFromEnvelope(idx, aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j=sets[i].getEnvelopeFirst(); j!=SetVar.END; j=sets[i].getEnvelopeNext()) {
                if (j < offSet1 || j >= n2 + offSet1 || !invsets[j - offSet2].envelopeContains(i + offSet1)) {
                    sets[i].removeFromEnvelope(j, aCause);
                }
            }
            for (int j=sets[i].getKernelFirst(); j!=SetVar.END; j=sets[i].getKernelNext()) {
                invsets[j - offSet2].addToKernel(i + offSet1, aCause);
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j=invsets[i].getEnvelopeFirst(); j!=SetVar.END; j=invsets[i].getEnvelopeNext()) {
                if (j < offSet2 || j >= n + offSet2 || !sets[j - offSet1].envelopeContains(i + offSet2)) {
                    invsets[i].removeFromEnvelope(j, aCause);
                }
            }
            for (int j=invsets[i].getKernelFirst(); j!=SetVar.END; j=invsets[i].getKernelNext()) {
                sets[j - offSet1].addToKernel(i + offSet2, aCause);
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
        sdm[idxVarInProp].forEach(elementForced, EventType.ADD_TO_KER);
        sdm[idxVarInProp].forEach(elementRemoved, EventType.REMOVE_FROM_ENVELOPE);
        sdm[idxVarInProp].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j=sets[i].getKernelFirst(); j!=SetVar.END; j=sets[i].getKernelNext()) {
                if (!invsets[j - offSet2].envelopeContains(i + offSet1)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j=invsets[i].getKernelFirst(); j!=SetVar.END; j=invsets[i].getKernelNext()) {
                if (!sets[j - offSet1].envelopeContains(i + offSet2)) {
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
