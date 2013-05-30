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
 * Ensures that all non-empty sets are disjoint
 * In order to forbid multiple empty set, use propagator PropAtMost1Empty in addition
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDisjoint extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, currentSet;
    private SetDeltaMonitor[] sdm;
    private IntProcedure elementForced;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that all non-empty sets are disjoint
     * In order to forbid multiple empty set, use propagator PropAtMost1Empty in addition
     *
     * @param sets
     */
    public PropAllDisjoint(SetVar[] sets) {
        super(sets, PropagatorPriority.LINEAR,false);
        n = sets.length;
        // delta monitors
        sdm = new SetDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                for (int i = 0; i < n; i++) {
                    if (i != currentSet) {
                        vars[i].removeFromEnvelope(element, aCause);
                    }
                }
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ADD_TO_KER.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            for (int i = 0; i < n; i++) {
                for (int j=vars[i].getKernelFirst(); j!=SetVar.END; j=vars[i].getKernelNext()) {
                    for (int i2 = 0; i2 < n; i2++) {
                        if (i2 != i) {
                            vars[i2].removeFromEnvelope(j, aCause);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            sdm[i].unfreeze();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        currentSet = idxVarInProp;
        sdm[currentSet].freeze();
        sdm[currentSet].forEach(elementForced, EventType.ADD_TO_KER);
        sdm[currentSet].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        boolean allInstantiated = true;
        for (int i = 0; i < n; i++) {
            if (!vars[i].instantiated()) {
                allInstantiated = false;
            }
            for (int j=vars[i].getKernelFirst(); j!=SetVar.END; j=vars[i].getKernelNext()) {
                for (int i2 = 0; i2 < n; i2++) {
                    if (i2 != i && vars[i2].kernelContains(j)) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (allInstantiated) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
