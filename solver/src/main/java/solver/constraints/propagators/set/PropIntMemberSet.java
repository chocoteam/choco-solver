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
import memory.setDataStructures.ISet;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.monitor.SetDeltaMonitor;

/**
 * Propagator for Member constraint: iv is in set
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntMemberSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar iv;
    private SetVar set;
    private SetDeltaMonitor sdm;
    private IntProcedure elemRem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Member constraint
     * val(intVar) is in setVar
     *
     * @param setVar
     * @param intVar
     */
    public PropIntMemberSet(SetVar setVar, IntVar intVar) {
        super(new Variable[]{setVar, intVar}, PropagatorPriority.BINARY);
        this.iv = intVar;
        this.set = setVar;
        this.sdm = setVar.monitorDelta(this);
        elemRem = new IntProcedure() {
            @Override
            public void execute(int i) throws ContradictionException {
                iv.removeValue(i, aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVE_FROM_ENVELOPE.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.instantiated()) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
            return;
        }
        ISet tmp = set.getEnvelope();
        int maxVal = tmp.getFirstElement();
        int minVal = maxVal;
        for (int j = maxVal; j >= 0; j = tmp.getNextElement()) {
            if (maxVal < j) {
                maxVal = j;
            }
            if (minVal > j) {
                minVal = j;
            }
        }
        iv.updateUpperBound(maxVal, aCause);
        iv.updateLowerBound(minVal, aCause);
        minVal = iv.getLB();
        maxVal = iv.getUB();
        for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
            if (!set.getEnvelope().contain(i)) {
                iv.removeValue(i, aCause);
            }
        }
        if (iv.instantiated()) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
        }
        sdm.unfreeze();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i == 1) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
        } else {
            sdm.freeze();
            sdm.forEach(elemRem, EventType.REMOVE_FROM_ENVELOPE);
            sdm.unfreeze();
            if (iv.instantiated()) {
                set.addToKernel(iv.getValue(), aCause);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (iv.instantiated()) {
            if (!set.getEnvelope().contain(iv.getValue())) {
                return ESat.FALSE;
            } else {
                if (set.instantiated()) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        } else {
            int minVal = iv.getLB();
            int maxVal = iv.getUB();
            boolean all = true;
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (!set.getKernel().contain(i)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return ESat.TRUE;
            }
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (set.getEnvelope().contain(i)) {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }
}
