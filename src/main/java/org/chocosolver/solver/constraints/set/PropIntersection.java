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
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
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
        intersectionForced = element -> {
            for (int i = 0; i < k; i++) {
                vars[i].force(element, this);
            }
        };
        intersectionRemoved = element -> {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].getUB().contains(element)) {
                    if (!vars[i].getLB().contains(element)) {
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
                fails(); // TODO: could be more precise, for explanation purpose
            } else if (mate != -2) {
                vars[mate].remove(element, this);
            }
        };
        setForced = element -> {
            boolean allKer = true;
            for (int i = 0; i < k; i++) {
                if (!vars[i].getUB().contains(element)) {
                    vars[k].remove(element, this);
                    allKer = false;
                    break;
                } else if (!vars[i].getLB().contains(element)) {
                    allKer = false;
                }
            }
            if (allKer) {
                vars[k].force(element, this);
            }
        };
        setRemoved = element -> vars[k].remove(element, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SetVar intersection = vars[k];
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            ISetIterator iter = vars[0].getLB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                boolean all = true;
                for (int i = 1; i < k; i++) {
                    if (!vars[i].getLB().contains(j)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    intersection.force(j, this);
                }
            }
            iter = intersection.getUB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                if (intersection.getLB().contains(j)) {
                    for (int i = 0; i < k; i++) {
                        vars[i].force(j, this);
                    }
                } else {
                    for (int i = 0; i < k; i++)
                        if (!vars[i].getUB().contains(j)) {
                            intersection.remove(j, this);
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
        ISetIterator iter = vars[k].getLB().iterator();
        while (iter.hasNext())
            for (int i = 0; i < k; i++)
                if (!vars[i].getUB().contains(iter.nextInt()))
                    return ESat.FALSE;
        iter = vars[0].getLB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (!vars[k].getUB().contains(j)) {
                boolean all = true;
                for (int i = 1; i < k; i++) {
                    if (!vars[i].getLB().contains(j)) {
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

}
