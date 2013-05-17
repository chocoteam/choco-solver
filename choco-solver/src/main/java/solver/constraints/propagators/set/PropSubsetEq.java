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

/**
 * Ensures that X subseteq Y
 *
 * @author Jean-Guillaume Fages
 */
public class PropSubsetEq extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private SetDeltaMonitor[] sdm;
    private IntProcedure elementForced, elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that X subseteq Y
     *
     * @param X
     * @param Y
     */
    public PropSubsetEq(SetVar X, SetVar Y) {
        super(new SetVar[]{X, Y}, PropagatorPriority.LINEAR);
        // delta monitors
        sdm = new SetDeltaMonitor[2];
        for (int i = 0; i < 2; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[1].addToKernel(element, aCause);
            }
        };
        elementRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[0].removeFromEnvelope(element, aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0)
            return EventType.ADD_TO_KER.mask;
        else
            return EventType.REMOVE_FROM_ENVELOPE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int j=vars[0].getKernelFirst(); j!=SetVar.END; j=vars[0].getKernelNext()) {
            vars[1].addToKernel(j, aCause);
        }
        for (int j=vars[0].getEnvelopeFirst(); j!=SetVar.END; j=vars[0].getEnvelopeNext()) {
            if (!vars[1].envelopeContains(j))
                vars[0].removeFromEnvelope(j, aCause);
        }
        sdm[0].unfreeze();
        sdm[1].unfreeze();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        sdm[i].freeze();
        if (i == 0)
            sdm[i].forEach(elementForced, EventType.ADD_TO_KER);
        else
            sdm[i].forEach(elementRemoved, EventType.REMOVE_FROM_ENVELOPE);
        sdm[i].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int j=vars[0].getKernelFirst(); j!=SetVar.END; j=vars[0].getKernelNext()) {
            if (!vars[1].envelopeContains(j)) {
                return ESat.FALSE;
            }
        }
        for (int j=vars[0].getEnvelopeFirst(); j!=SetVar.END; j=vars[0].getEnvelopeNext()) {
            if (!vars[1].kernelContains(j)) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }
}
