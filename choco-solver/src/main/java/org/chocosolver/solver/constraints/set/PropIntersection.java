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

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

public class PropIntersection extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int k;
    private ISetDeltaMonitor[] sdm;
    private IntProcedure intersectionForced, intersectionRemoved, setForced, setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropIntersection(SetVar[] sets, SetVar intersection) {
        super(ArrayUtils.append(sets, new SetVar[]{intersection}), PropagatorPriority.LINEAR, true);
        k = sets.length;
        sdm = new ISetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        // PROCEDURES
        intersectionForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                for (int i = 0; i < k; i++) {
                    vars[i].addToKernel(element, aCause);
                }
            }
        };
        intersectionRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                int mate = -1;
                for (int i = 0; i < k; i++)
                    if (vars[i].envelopeContains(element)) {
                        if (!vars[i].kernelContains(element)) {
                            if (mate == -1) {
                                mate = i;
                            } else {
                                mate = -2;
                                break;
                            }
                        }
                    } else {
                        mate = -2;
                        break;
                    }
                if (mate == -1) {
                    PropIntersection.this.contradiction(vars[k], "");
                } else if (mate != -2) {
                    vars[mate].removeFromEnvelope(element, aCause);
                }
            }
        };
        setForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                boolean allKer = true;
                for (int i = 0; i < k; i++) {
                    if (!vars[i].envelopeContains(element)) {
                        vars[k].removeFromEnvelope(element, aCause);
                        allKer = false;
                        break;
                    } else if (!vars[i].kernelContains(element)) {
                        allKer = false;
                    }
                }
                if (allKer) {
                    vars[k].addToKernel(element, aCause);
                }
            }
        };
        setRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[k].removeFromEnvelope(element, aCause);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SetVar intersection = vars[k];
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int j = vars[0].getKernelFirst(); j != SetVar.END; j = vars[0].getKernelNext()) {
                boolean all = true;
                for (int i = 1; i < k; i++) {
                    if (!vars[i].kernelContains(j)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    intersection.addToKernel(j, aCause);
                }
            }
            for (int j = intersection.getEnvelopeFirst(); j != SetVar.END; j = intersection.getEnvelopeNext()) {
                if (intersection.kernelContains(j)) {
                    for (int i = 0; i < k; i++) {
                        vars[i].addToKernel(j, aCause);
                    }
                } else {
                    for (int i = 0; i < k; i++)
                        if (!vars[i].envelopeContains(j)) {
                            intersection.removeFromEnvelope(j, aCause);
                            break;
                        }
                }
            }
            // ------------------
			for (int i = 0; i <= k; i++)
				sdm[i].unfreeze();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        sdm[idxVarInProp].freeze();
        if (idxVarInProp < k) {
            sdm[idxVarInProp].forEach(setForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(setRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        } else {
            sdm[idxVarInProp].forEach(intersectionForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(intersectionRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        }
        sdm[idxVarInProp].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int j = vars[k].getKernelFirst(); j != SetVar.END; j = vars[k].getKernelNext())
            for (int i = 0; i < k; i++)
                if (!vars[i].envelopeContains(j))
                    return ESat.FALSE;
        for (int j = vars[0].getKernelFirst(); j != SetVar.END; j = vars[0].getKernelNext()) {
            if (!vars[k].envelopeContains(j)) {
                boolean all = true;
                for (int i = 1; i < k; i++) {
                    if (!vars[i].kernelContains(j)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = k;
            SetVar[] svars = new SetVar[size];
            for (int i = 0; i < size; i++) {
                vars[i].duplicate(solver, identitymap);
                svars[i] = (SetVar) identitymap.get(vars[i]);
            }
            vars[k].duplicate(solver, identitymap);
            SetVar I = (SetVar) identitymap.get(vars[k]);
            identitymap.put(this, new PropIntersection(svars, I));
        }
    }
}
