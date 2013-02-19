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

import common.ESat;
import common.util.procedure.IntProcedure;
import common.util.objects.setDataStructures.ISet;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.delta.monitor.SetDeltaMonitor;

/**
 * Propagator for symmetric sets
 * x in set[y-offSet] <=> y in set[x-offSet]
 *
 * @author Jean-Guillaume Fages
 */
public class PropSymmetric extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, currentSet, offSet;
    private SetDeltaMonitor[] sdm;
    private IntProcedure elementForced, elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for symmetric sets
     * x in set[y-offSet] <=> y in set[x-offSet]
     */
    public PropSymmetric(SetVar[] sets, final int offSet) {
        super(sets, PropagatorPriority.LINEAR);
        n = sets.length;
        this.offSet = offSet;
        sdm = new SetDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[element - offSet].addToKernel(currentSet, aCause);
            }
        };
        elementRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[element - offSet].removeFromEnvelope(currentSet, aCause);
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
            ISet s = vars[i].getEnvelope();
            for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
                if (j < offSet || j >= n + offSet || !vars[j - offSet].getEnvelope().contain(i + offSet)) {
                    vars[i].removeFromEnvelope(j, aCause);
                }
            }
            s = vars[i].getKernel();
            for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
                vars[j - offSet].addToKernel(i + offSet, aCause);
            }
        }
        for (int i = 0; i < n; i++) {
            sdm[i].unfreeze();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        currentSet = idxVarInProp + offSet;
        sdm[currentSet].freeze();
        sdm[currentSet].forEach(elementForced, EventType.ADD_TO_KER);
        sdm[currentSet].forEach(elementRemoved, EventType.REMOVE_FROM_ENVELOPE);
        sdm[currentSet].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            ISet tmp = vars[i].getKernel();
            for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
                if (!vars[j - offSet].getEnvelope().contain(i + offSet)) {
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
