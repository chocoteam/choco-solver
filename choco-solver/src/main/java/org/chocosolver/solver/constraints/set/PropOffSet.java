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
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

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
    private ISetDeltaMonitor[] sdm;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * set2 is an offSet view of set1
     * x in set1 <=> x+offSet in set2
     */
    public PropOffSet(SetVar set1, SetVar set2, int offSet) {
        super(new SetVar[]{set1, set2}, PropagatorPriority.UNARY, true);
        this.offSet = offSet;
        sdm = new ISetDeltaMonitor[2];
        sdm[0] = vars[0].monitorDelta(this);
        sdm[1] = vars[1].monitorDelta(this);
        this.forced = i -> tmpSet.addToKernel(i + tmp, aCause);
        this.removed = i -> tmpSet.removeFromEnvelope(i + tmp, aCause);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // kernel
        for (int j = vars[0].getKernelFirst(); j != SetVar.END; j = vars[0].getKernelNext()) {
            vars[1].addToKernel(j + offSet, aCause);
        }
        for (int j = vars[1].getKernelFirst(); j != SetVar.END; j = vars[1].getKernelNext()) {
            vars[0].addToKernel(j - offSet, aCause);
        }
        // envelope
        for (int j = vars[0].getEnvelopeFirst(); j != SetVar.END; j = vars[0].getEnvelopeNext()) {
            if (!vars[1].envelopeContains(j + offSet)) {
                vars[0].removeFromEnvelope(j, aCause);
            }
        }
        for (int j = vars[1].getEnvelopeFirst(); j != SetVar.END; j = vars[1].getEnvelopeNext()) {
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
        sdm[v].forEach(forced, SetEventType.ADD_TO_KER);
        sdm[v].forEach(removed, SetEventType.REMOVE_FROM_ENVELOPE);
        sdm[v].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int j = vars[0].getKernelFirst(); j != SetVar.END; j = vars[0].getKernelNext()) {
            if (!vars[1].envelopeContains(j + offSet)) {
                return ESat.FALSE;
            }
        }
        for (int j = vars[1].getKernelFirst(); j != SetVar.END; j = vars[1].getKernelNext()) {
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
