/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

public class PropUnion extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int k;
    private ISetDeltaMonitor[] sdm;
    private IntProcedure unionForced, unionRemoved, setForced, setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The union of sets is equal to union
     *
     * @param sets set variables to unify
     * @param union resulting set variable
     */
    public PropUnion(SetVar[] sets, SetVar union) {
        super(ArrayUtils.append(sets, new SetVar[]{union}), PropagatorPriority.LINEAR, true);
        k = sets.length;
        sdm = new ISetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        // PROCEDURES
        unionForced = element -> {
            int mate = -1;
            for (int i = 0; i < k && mate != -2; i++) {
                if (vars[i].getUB().contain(element)) {
                    if (mate == -1) {
                        mate = i;
                    } else {
                        mate = -2;
                    }
                }
            }
            if (mate == -1) {
                fails(); // TODO: could be more precise, for explanation purpose
            } else if (mate != -2) {
                vars[mate].force(element, this);
            }
        };
        unionRemoved = element -> {
            for (int i = 0; i < k; i++) {
                vars[i].remove(element, this);
            }
        };
        setForced = element -> vars[k].force(element, this);
        setRemoved = element -> {
            if (vars[k].getUB().contain(element)) {
                int mate = -1;
                for (int i = 0; i < k && mate != -2; i++) {
                    if (vars[i].getUB().contain(element)) {
                        if (mate == -1) {
                            mate = i;
                        } else {
                            mate = -2;
                        }
                    }
                }
                if (mate == -1) {
                    vars[k].remove(element, this);
                } else if (mate != -2 && vars[k].getLB().contain(element)) {
                    vars[mate].force(element, this);
                }
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            SetVar union = vars[k];
            for (int i = 0; i < k; i++) {
                for (int j : vars[i].getLB())
                    union.force(j, this);
                for (int j : vars[i].getUB())
                    if (!union.getUB().contain(j))
                        vars[i].remove(j, this);
            }
            for (int j : union.getUB()) {
                if (union.getLB().contain(j)) {
                    int mate = -1;
                    for (int i = 0; i < k && mate != -2; i++) {
                        if (vars[i].getUB().contain(j)) {
                            if (mate == -1) {
                                mate = i;
                            } else {
                                mate = -2;
                            }
                        }
                    }
                    if (mate == -1) {
                        fails(); // TODO: could be more precise, for explanation purpose
                    } else if (mate != -2) {
                        vars[mate].force(j, this);
                    }
                } else {
                    int mate = -1;
                    for (int i = 0; i < k; i++) {
                        if (vars[i].getUB().contain(j)) {
                            mate = i;
                            break;
                        }
                    }
                    if (mate == -1) union.remove(j, this);
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
            sdm[idxVarInProp].forEach(unionForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(unionRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        }
        sdm[idxVarInProp].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < k; i++) {
            for (int j : vars[i].getLB())
                if (!vars[k].getUB().contain(j))
                    return ESat.FALSE;
        }
        for (int j : vars[k].getLB()) {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].getUB().contain(j)) {
                    mate = i;
                    break;
                }
            if (mate == -1) return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

}
