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

package solver.constraints.propagators.set;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.delta.monitor.SetDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;

/**
 * set2 is an offSet view of set1
 * x in set1 <=> x+offSet in set2
 *
 * @author Jean-Guillaume Fages
 */
public class PropOffSet extends Propagator<SetVar> {

    private int offSet, tmp;
    private SetVar tmpSet;
    private IntProcedure forced, removed;
    private SetDeltaMonitor[] sdm;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * set2 is an offSet view of set1
     * x in set1 <=> x+offSet in set2
     */
    public PropOffSet(SetVar set1, SetVar set2, int offSet) {
        super(new SetVar[]{set1, set2}, PropagatorPriority.UNARY);
        this.offSet = offSet;
        sdm = new SetDeltaMonitor[2];
        sdm[0] = vars[0].monitorDelta(this);
        sdm[1] = vars[1].monitorDelta(this);
        this.forced = new IntProcedure() {
            @Override
            public void execute(int i) throws ContradictionException {
                tmpSet.addToKernel(i + tmp, aCause);
            }
        };
        this.removed = new IntProcedure() {
            @Override
            public void execute(int i) throws ContradictionException {
                tmpSet.removeFromEnvelope(i + tmp, aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVE_FROM_ENVELOPE.mask + EventType.ADD_TO_KER.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // kernel
        for (int j=vars[0].getKernelFirstElement(); j!=SetVar.END; j=vars[0].getKernelNextElement()) {
            vars[1].addToKernel(j + offSet, aCause);
        }
        for (int j=vars[1].getKernelFirstElement(); j!=SetVar.END; j=vars[1].getKernelNextElement()) {
            vars[0].addToKernel(j - offSet, aCause);
        }
        // envelope
        for (int j = vars[0].getEnvelopeFirstElement(); j!=SetVar.END; j = vars[0].getEnvelopeNextElement()) {
            if (!vars[1].envelopeContains(j + offSet)) {
                vars[0].removeFromEnvelope(j, aCause);
            }
        }
        for (int j=vars[1].getEnvelopeFirstElement(); j!=SetVar.END; j=vars[1].getEnvelopeNextElement()) {
            if (!vars[0].envelopeContains(j - offSet)) {
                vars[1].removeFromEnvelope(j, aCause);
            }
        }
        sdm[0].unfreeze();
        sdm[1].unfreeze();
    }

    @Override
    public void propagate(int v, int mask) throws ContradictionException {
        sdm[v].freeze();
        if (v == 0) {
            tmp = offSet;
            tmpSet = vars[1];
        } else {
            tmp = -offSet;
            tmpSet = vars[0];
        }
        sdm[v].forEach(forced, EventType.ADD_TO_KER);
        sdm[v].forEach(removed, EventType.REMOVE_FROM_ENVELOPE);
        sdm[v].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int j=vars[0].getKernelFirstElement(); j!=SetVar.END; j=vars[0].getKernelNextElement()) {
            if (!vars[1].envelopeContains(j + offSet)) {
                return ESat.FALSE;
            }
        }
        for (int j=vars[1].getKernelFirstElement(); j!=SetVar.END; j=vars[1].getKernelNextElement()) {
            if (!vars[0].envelopeContains(j - offSet)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
