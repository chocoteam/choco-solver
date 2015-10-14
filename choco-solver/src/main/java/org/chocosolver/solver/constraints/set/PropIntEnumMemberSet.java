/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Propagator for Member constraint: iv is in set
 *
 * @author Charles Prud'homme
 */
public class PropIntEnumMemberSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar iv;
    private SetVar set;
    private ISetDeltaMonitor sdm;
    private IntProcedure elemRem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Member constraint
     * val(intVar) is in setVar
     *
     * @param setVar a set variable
     * @param intVar an integer variable
     */
    public PropIntEnumMemberSet(SetVar setVar, IntVar intVar) {
        super(new Variable[]{setVar, intVar}, PropagatorPriority.BINARY, true);
        assert intVar.hasEnumeratedDomain() : iv.toString() + " does not an enumerated domain";
        this.set = (SetVar) vars[0];
        this.iv = (IntVar) vars[1];
        this.sdm = set.monitorDelta(this);
        elemRem = i -> iv.removeValue(i, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        } else {
            return IntEventType.INSTANTIATE.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), this);
            setPassive();
            return;
        }
        int ub = iv.getUB();
        for (int i = iv.getLB(); i <= ub; i = iv.nextValue(i)) {
            if (!set.envelopeContains(i)) {
                iv.removeValue(i, this);
            }
        }
        // now iv \subseteq set
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), this);
            setPassive();
        }
        sdm.unfreeze();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i == 0) {
            sdm.freeze();
            sdm.forEach(elemRem, SetEventType.REMOVE_FROM_ENVELOPE);
            sdm.unfreeze();
        }
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), this);
            setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (iv.isInstantiated()) {
            if (!set.envelopeContains(iv.getValue())) {
                return ESat.FALSE;
            } else {
                if (set.kernelContains(iv.getValue())) {
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
                if (!set.kernelContains(i)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return ESat.TRUE;
            }
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (set.envelopeContains(i)) {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }

}
