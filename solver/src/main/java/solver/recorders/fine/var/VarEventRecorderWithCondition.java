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
package solver.recorders.fine.var;

import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.propagation.PropagationEngine;
import solver.recorders.conditions.ICondition;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * A specialized fine event recorder associated with one variable and one or more propagators.
 * It observes a variable, schedules coarse event when calling the filtering algortithm of the propagator
 * is required.
 * <br/>
 * on a pris le parti de ne pas mémoriser les événements fins,
 * partant du principe que ca sera de toute facon plus couteux à dépiler et à traiter
 * que de lancer directement la propag' lourde
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public class VarEventRecorderWithCondition<V extends Variable> extends VarEventRecorder<V> {

    protected final int[] idxVinPs; // index of the variable within the propagator -- immutable

    final ICondition condition; // condition to run the filtering algorithm of the propagator

    public VarEventRecorderWithCondition(V variable, Propagator<V>[] propagators, int[] idxVinP,
                                         ICondition condition, Solver solver, PropagationEngine engine) {
        super(variable, propagators, solver, engine);
        this.condition = condition;
        condition.linkRecorder(this);
        this.idxVinPs = idxVinP.clone();
    }

    @Override
    public void afterUpdate(int vIdx, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        for (int k = 0; k < propagators.length; k++) {
            int i = propIdx[k];
            Propagator propagator = propagators[i];
            if (cause != propagator && propagator.isActive()) { // due to idempotency of propagator, it should not schedule itself
                if (Configuration.PRINT_PROPAGATION)
                    LoggerFactory.getLogger("solver").info("\t|- {} - {}", this.toString(), propagator);
                int idx = p2i[propagator.getId() - offset];
                if (propagator.advise(idxVinPs[idx], evt.mask)) {

                    // schedule this if condition is valid
                    if (condition.validateScheduling(this, propagator, evt)) {
                        //propagator.forcePropagate(EventType.FULL_PROPAGATION);
                        throw new UnsupportedOperationException("Unsafe");
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "<< " + variables[0].toString() + "::" + Arrays.toString(propagators) + "::" + condition.toString() + " >>";
    }

}
