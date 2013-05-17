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
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.monitor.SetDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;
import util.tools.ArrayUtils;

/**
 * Channeling between a set variable and boolean variables
 *
 * @author Jean-Guillaume Fages
 */
public class PropBoolChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int offSet;
    private BoolVar[] bools;
    private SetVar set;
    private SetDeltaMonitor sdm;
    private IntProcedure setForced, setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Channeling between a set variable and boolean variables
     * i in setVar <=> boolVars[i-offSet] = TRUE
     *
     * @param setVar
     * @param boolVars
     */
    public PropBoolChannel(SetVar setVar, BoolVar[] boolVars, final int offSet) {
        super(ArrayUtils.append(boolVars, new Variable[]{setVar}), PropagatorPriority.UNARY);
        this.n = boolVars.length;
        this.bools = new BoolVar[n];
        for (int i = 0; i < n; i++) {
            this.bools[i] = (BoolVar) vars[i];
        }
        this.set = (SetVar) vars[n];
        this.sdm = this.set.monitorDelta(this);
        this.offSet = offSet;
        // PROCEDURES
        setForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                bools[element - offSet].setToTrue(aCause);
            }
        };
        setRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                bools[element - offSet].setToFalse(aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            if (bools[i].instantiated()) {
                if (bools[i].getValue() == 0) {
                    set.removeFromEnvelope(i + offSet, aCause);
                } else {
                    set.addToKernel(i + offSet, aCause);
                }
            } else if (!set.envelopeContains(i + offSet)) {
                bools[i].setToFalse(aCause);
            }
        }
        for (int j=set.getEnvelopeFirst(); j!=SetVar.END; j=set.getEnvelopeNext()) {
            if (j < offSet || j >= n + offSet) {
                set.removeFromEnvelope(j, aCause);
            }
        }
        for (int j=set.getKernelFirst(); j!=SetVar.END; j=set.getKernelNext()) {
            bools[j - offSet].setToTrue(aCause);
        }
        sdm.unfreeze();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i < n) {
            if (bools[i].getValue() == 0) {
                set.removeFromEnvelope(i + offSet, aCause);
            } else {
                set.addToKernel(i + offSet, aCause);
            }
        } else {
            sdm.freeze();
            sdm.forEach(setForced, EventType.ADD_TO_KER);
            sdm.forEach(setRemoved, EventType.REMOVE_FROM_ENVELOPE);
            sdm.unfreeze();
        }
    }

    @Override
    public ESat isEntailed() {
        for (int j=set.getKernelFirst(); j!=SetVar.END; j=set.getKernelNext()) {
            if (bools[j - offSet].instantiatedTo(0)) {
                return ESat.FALSE;
            }
        }
        for (int i = 0; i < n; i++) {
            if (bools[i].instantiatedTo(1)) {
                if (!set.envelopeContains(i + offSet)) {
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
