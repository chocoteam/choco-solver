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

package solver.recorders.conditions;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import solver.constraints.propagators.Propagator;
import solver.recorders.IEventRecorder;
import solver.variables.EventType;

/**
 * An abstract class to declare a specific conditions to be satisfied for a conditionnal recorder.
 * <br/>
 * It provides 2 services: one to check validity, another to keep informed of event occuring on related recorder(s).
 * </br>
 * In basic behaviour (no condition recorder), events are propagated automatically after popping.
 * In conditionnal recorder, previously popped (but not propagated) events should be propagated.
 * That's why on a newly validation, modified fine event recorder must be "added" to the propagation engine,
 * recorder should act like "no condition one".
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public abstract class AbstractCondition<R extends IEventRecorder> implements ICondition<R> {

    R[] relatedRecorder; // array of conditionnal records declaring this -- size >= number of elements!
    int idxLastRecorder; // index of the last not null recorder in relatedRecorder
    final IStateBool wasValid;
    ICondition next = Default.NO_CONDITION;


    protected AbstractCondition(IEnvironment environment) {
        wasValid = environment.makeBool(false);
        relatedRecorder = (R[]) new IEventRecorder[8];
    }

    /**
     * Keep informed the condition of the modification of one of its related recorders.
     * If the condition is newly validate, push all related records in the propagation engine.
     * todo: preciser les raisons
     *
     * @param recorder   recently modified recorder
     * @param propagator
     * @param event
     */
    public final boolean validateScheduling(R recorder, Propagator propagator, EventType event) {
        if (wasValid.get()) {
            return true;
        } else {
            update(recorder, propagator, event);
            if (isValid()) {
                wasValid.set(alwaysValid());
                next.validateScheduling(recorder, propagator, event);
                return true;
            }
            //return false;
            return next.validateScheduling(recorder, propagator, event);
        }
    }

    @Override
    public ICondition next() {
        return null;
    }

    /**
     * Check if the condition is valid
     *
     * @return true if the condition is satisfied, false otherwise
     */
    abstract boolean isValid();

    /**
     * Return true if the condition, once validate, won't change anymore in the current branch,
     * avoiding validation computation each time.
     * This simulates "no condition" recorder behaviour.
     *
     * @return true if the condition, once validate, won't change anymore in the current branch
     */
    abstract boolean alwaysValid();

    /**
     * Updates the current condition on the modification of one its related records.
     *
     * @param recorder   recently modified recorder
     * @param propagator
     * @param event
     */
    abstract void update(R recorder, Propagator propagator, EventType event);

    /**
     * Link the <code>recorder</code> to the condition
     *
     * @param recorder condition recorder
     */
    public void linkRecorder(R recorder) {
        if (idxLastRecorder >= relatedRecorder.length) {
            R[] tmp = relatedRecorder;
            relatedRecorder = (R[]) new IEventRecorder[tmp.length * 2];
            System.arraycopy(tmp, 0, relatedRecorder, 0, tmp.length);
        }
        relatedRecorder[idxLastRecorder++] = recorder;
        next.linkRecorder(recorder);
    }


}
