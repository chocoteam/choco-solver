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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Channeling between set variables and integer variables
 * x in sets[y-offSet1] <=> ints[x-offSet2] = y
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int nInts, nSets, idx;
    private SetVar[] sets;
    private IntVar[] ints;
    private int offSet1, offSet2;
    private ISetDeltaMonitor[] sdm;
    private IIntDeltaMonitor[] idm;
    private IntProcedure elementForced, elementRemoved, valRem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Channeling between set variables and integer variables
     * x in sets[y-offSet1] <=> ints[x-offSet2] = y
     */
    public PropIntChannel(SetVar[] setsV, IntVar[] intsV, final int offSet1, final int offSet2) {
        super(ArrayUtils.append(setsV, intsV), PropagatorPriority.LINEAR, true);
        this.nSets = setsV.length;
        this.nInts = intsV.length;
        this.sets = new SetVar[nSets];
        this.ints = new IntVar[nInts];
        this.idm = new IIntDeltaMonitor[nInts];
        this.sdm = new ISetDeltaMonitor[nSets];
        this.offSet1 = offSet1;
        this.offSet2 = offSet2;
        for (int i = 0; i < nInts; i++) {
            this.ints[i] = (IntVar) vars[i + nSets];
            this.idm[i] = this.ints[i].monitorDelta(this);
        }
        for (int i = 0; i < nSets; i++) {
            this.sets[i] = (SetVar) vars[i];
            this.sdm[i] = this.sets[i].monitorDelta(this);
        }
        // procedures
        elementForced = element -> ints[element - offSet2].instantiateTo(idx, this);
        elementRemoved = element -> ints[element - offSet2].removeValue(idx, this);
        valRem = element -> sets[element - offSet1].remove(idx, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < nInts; i++) {
            ints[i].updateBounds(offSet1, nSets - 1 + offSet1, this);
        }
        for (int i = 0; i < nInts; i++) {
            int ub = ints[i].getUB();
            for (int j = ints[i].getLB(); j <= ub; j = ints[i].nextValue(j)) {
                if (!sets[j - offSet1].getUB().contain(i + offSet2)) {
                    ints[i].removeValue(j, this);
                }
            }
            if (ints[i].isInstantiated()) {
                sets[ints[i].getValue() - offSet1].force(i + offSet2, this);
            }
        }
        for (int i = 0; i < nSets; i++) {
            for (int j : sets[i].getUB()) {
                if (j < offSet2 || j > nInts - 1 + offSet2 || !ints[j - offSet2].contains(i + offSet1)) {
                    sets[i].remove(j, this);
                }
            }
            for (int j : sets[i].getLB()) {
                ints[j - offSet2].instantiateTo(i + offSet1, this);
            }
        }
        for (int i = 0; i < nSets; i++) {
            sdm[i].unfreeze();
        }
        for (int i = 0; i < nInts; i++) {
            idm[i].unfreeze();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        idx = idxVarInProp;
        if (idx < nSets) {
            idx += offSet1;
            sdm[idxVarInProp].freeze();
            sdm[idxVarInProp].forEach(elementForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
            sdm[idxVarInProp].unfreeze();
        } else {
            idx -= nSets;
            if (ints[idx].isInstantiated()) {
                sets[ints[idx].getValue() - offSet1].force(idx + offSet2, this);
            }
            idx += offSet2;
            idm[idxVarInProp - nSets].freeze();
            idm[idxVarInProp - nSets].forEachRemVal(valRem);
            idm[idxVarInProp - nSets].unfreeze();
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < nInts; i++) {
            if (ints[i].isInstantiated()) {
                int val = ints[i].getValue();
                if (val < offSet1 || val >= nSets + offSet1 || !sets[val - offSet1].getUB().contain(i + offSet2)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < nSets; i++) {
            for (int j : sets[i].getLB()) {
                if (j < offSet2 || j >= nInts + offSet2 || !ints[j - offSet2].contains(i + offSet1)) {
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
