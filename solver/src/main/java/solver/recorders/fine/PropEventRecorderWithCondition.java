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
package solver.recorders.fine;

import gnu.trove.map.hash.TIntIntHashMap;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.recorders.conditions.ICondition;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * A specialized fine event recorder associated with one or more variable and one propagator.
 * It observes variables, schedules coarse event when calling the filtering algortithm of the propagator
 * is required.
 * <br/>
 * on a pris le parti de ne pas m�moriser les �v�nements fins,
 * partant du principe que ca sera de toute facon plus couteux � d�piler et � traiter
 * que de lancer directement la propag' lourde
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public class PropEventRecorderWithCondition<V extends Variable> extends PropEventRecorder<V> {

    protected TIntIntHashMap idxVinP; // index of each variable within P -- immutable

    final ICondition condition; // condition to run the filtering algorithm of the propagator

    public PropEventRecorderWithCondition(V[] variables, Propagator<V> propagator, int[] idxVinPs,
                                          ICondition condition, Solver solver) {
        super(variables, propagator, solver);
        this.condition = condition;
        condition.linkRecorder(this);

        this.idxVinP = new TIntIntHashMap(variables.length, (float) 0.5, -1, -1);
        for (int i = 0; i < variables.length; i++) {
            V variable = variables[i];
            int vid = variable.getId();
            idxVinP.put(vid, idxVinPs[i]);
        }
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        if (cause != propagator) { // due to idempotency of propagator, it should not be schedule itself
            int vid = var.getId();
            if ((evt.mask & propagator.getPropagationConditions(idxVinP.get(vid))) != 0) {
                // 1. if instantiation, then decrement arity of the propagator
                if (EventType.anInstantiationEvent(evt.mask)) {
                    propagator.decArity();
                }
                // 2. schedule this if condition is valid
                if (condition.validateScheduling(this, propagator, evt)) {
                    propagator.forcePropagate(EventType.FULL_PROPAGATION);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "<< {F} " + Arrays.toString(variables) + "::" + propagator.toString() + "::" + condition.toString() + " >>";
    }

}
