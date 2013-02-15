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
import common.util.tools.ArrayUtils;
import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetFactory;
import common.util.objects.setDataStructures.SetType;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.delta.monitor.SetDeltaMonitor;

public class PropUnion extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int k;
    private SetDeltaMonitor[] sdm;
    private IntProcedure unionForced, unionRemoved, setForced, setRemoved;
    private ISet unionAddToTreat, setRemToTreat;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The union of sets is equal to union
     *
     * @param sets
     * @param union
     */
    public PropUnion(SetVar[] sets, SetVar union) {
        super(ArrayUtils.append(sets, new SetVar[]{union}), PropagatorPriority.LINEAR);
        k = sets.length;
        sdm = new SetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        unionAddToTreat = SetFactory.makeStoredSet(SetType.LINKED_LIST, 0, environment);
        setRemToTreat = SetFactory.makeStoredSet(SetType.LINKED_LIST, 0, environment);
        // PROCEDURES
        unionForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                unionAddToTreat.add(element);
            }
        };
        unionRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                for (int i = 0; i < k; i++) {
                    vars[i].removeFromEnvelope(element, aCause);
                }
            }
        };
        setForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[k].addToKernel(element, aCause);
            }
        };
        setRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                if (!setRemToTreat.contain(element)) {
                    setRemToTreat.add(element);
                }
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISet set;
        SetVar union = vars[k];
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            for (int i = 0; i < k; i++) {
                set = vars[i].getKernel();
                for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement())
                    union.addToKernel(j, aCause);
                set = vars[i].getEnvelope();
                for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement())
                    if (!union.getEnvelope().contain(j))
                        vars[i].removeFromEnvelope(j, aCause);
            }
            set = union.getEnvelope();
            for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement()) {
                if (union.getKernel().contain(j)) {
                    unionAddToTreat.add(j);
                } else {
                    int mate = -1;
                    for (int i = 0; i < k; i++) {
                        if (vars[i].getEnvelope().contain(j)) {
                            mate = i;
                            break;
                        }
                    }
                    if (mate == -1) union.removeFromEnvelope(j, aCause);
                }
            }
        }
        set = unionAddToTreat;
        for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement()) {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].getEnvelope().contain(j))
                    if (mate == -1) {
                        mate = i;
                    } else {
                        mate = -2;
                        break;
                    }
            if (mate == -1) {
                contradiction(vars[k], "");
            } else if (mate != -2) {
                vars[mate].addToKernel(j, aCause);
                unionAddToTreat.remove(j);
            }
        }
        set = setRemToTreat;
        for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement()) {
            if (union.getEnvelope().contain(j) && !union.getKernel().contain(j)) {
                int mate = -1;
                for (int i = 0; i < k; i++)
                    if (vars[i].getEnvelope().contain(j)) {
                        mate = i;
                        break;
                    }
                if (mate == -1)
                    vars[k].removeFromEnvelope(j, aCause);
            }
            setRemToTreat.remove(j);
        }
        // ------------------
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0)
            for (int i = 0; i <= k; i++)
                sdm[i].unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        sdm[idxVarInProp].freeze();
        if (idxVarInProp < k) {
            sdm[idxVarInProp].forEach(setForced, EventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(setRemoved, EventType.REMOVE_FROM_ENVELOPE);
        } else {
            sdm[idxVarInProp].forEach(unionForced, EventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(unionRemoved, EventType.REMOVE_FROM_ENVELOPE);
        }
        sdm[idxVarInProp].unfreeze();
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        ISet set;
        for (int i = 0; i < k; i++) {
            set = vars[i].getKernel();
            for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement())
                if (!vars[k].getEnvelope().contain(j))
                    return ESat.FALSE;
        }
        set = vars[k].getKernel();
        for (int j = set.getFirstElement(); j >= 0; j = set.getNextElement()) {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].getEnvelope().contain(j)) {
                    mate = i;
                    break;
                }
            if (mate == -1) return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }
}
