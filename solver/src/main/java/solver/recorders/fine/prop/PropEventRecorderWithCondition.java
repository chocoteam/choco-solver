/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.recorders.fine.prop;

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.propagation.IPropagationEngine;
import solver.recorders.conditions.ICondition;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * A specialized fine event recorder associated with one or more variable and one propagator.
 * It observes variables, schedules coarse event when calling the filtering algortithm of the propagator
 * is required.
 * <br/>
 * on a pris le parti de ne pas mémoriser les événements fins,
 * partant du principe que ca sera de toute facon plus couteux à dépiler et à traiter
 * que de lancer directement la propag' lourde
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public class PropEventRecorderWithCondition<V extends Variable> extends PropEventRecorder<V> {

    protected final int[] idxVinP; //; // index of each variable within P -- immutable

    final ICondition condition; // condition to run the filtering algorithm of the propagator

    public PropEventRecorderWithCondition(V[] variables, Propagator<V> propagator, int[] idxVinPs,
                                          ICondition condition, Solver solver, IPropagationEngine engine) {
        super(variables, propagator, solver, engine);
        this.condition = condition;
        condition.linkRecorder(this);
        this.idxVinP = idxVinPs.clone();
    }

    @Override
    public void afterUpdate(int vIdx, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        if (cause != propagators[PINDEX]) { // due to idempotency of propagator, it should not be schedule itself
            if (propagators[PINDEX].advise(idxVinP[vIdx], evt.mask)) {
                // schedule this if condition is valid
                if (condition.validateScheduling(this, propagators[PINDEX], evt)) {
                    propagators[PINDEX].forcePropagate(EventType.FULL_PROPAGATION);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "<< {F} " + Arrays.toString(variables) + "::" + propagators[PINDEX].toString() + "::" + condition.toString() + " >>";
    }

}
