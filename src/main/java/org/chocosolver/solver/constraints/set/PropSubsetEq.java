/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Ensures that X subseteq Y
 *
 * @author Jean-Guillaume Fages
 */
public class PropSubsetEq extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private ISetDeltaMonitor[] sdm;
    private IntProcedure elementForced, elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that X subseteq Y
     *
     * @param X a set variable
     * @param Y a set variable
     */
    public PropSubsetEq(SetVar X, SetVar Y) {
        super(new SetVar[]{X, Y}, PropagatorPriority.LINEAR, true);
        // delta monitors
        sdm = new ISetDeltaMonitor[2];
        for (int i = 0; i < 2; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = element -> vars[1].force(element, this);
        elementRemoved = element -> vars[0].remove(element, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0)
            return SetEventType.ADD_TO_KER.getMask();
        else
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int j : vars[0].getLB()) {
            vars[1].force(j, this);
        }
        for (int j : vars[0].getUB()) {
            if (!vars[1].getUB().contain(j))
                vars[0].remove(j, this);
        }
        sdm[0].unfreeze();
        sdm[1].unfreeze();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        sdm[i].freeze();
        if (i == 0)
            sdm[i].forEach(elementForced, SetEventType.ADD_TO_KER);
        else
            sdm[i].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        sdm[i].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int j : vars[0].getLB()) {
            if (!vars[1].getUB().contain(j)) {
                return ESat.FALSE;
            }
        }
        for (int j : vars[0].getUB()) {
            if (!vars[1].getLB().contain(j)) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

}
