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

package solver.requests.conditions;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import solver.requests.ConditionnalRequest;

/**
 * An abstract class to declare a specific conditions to be satisfied for a conditionnal request.
 * <br/>
 * It provides 2 services: one to check validity, another to keep informed of event occuring on related request(s).
 * </br>
 * In basic behaviour (no condition requests), events are propagated automatically after popping.
 * In conditionnal requests, previously popped (but not propagated) events should be propagated.
 * That's why on a newly validation, modified variable request must be "added" to the propagation engine,
 * request should act like "no condition one".
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public abstract class AbstractCondition {

    ConditionnalRequest[] relatedRequests; // array of conditionnal requests declaring this -- size >= number of elements!
    int idxLastRequest; // index of the last not null request in relatedRequests
    final IStateBool wasValid;

    protected AbstractCondition(IEnvironment environment) {
        wasValid = environment.makeBool(false);
        relatedRequests = new ConditionnalRequest[8];
    }

    /**
     * Keep informed the condition of the modification of one of its related requests.
     * If the condition is newly validate, push all related requests in the propagation engine.
     * todo: preciser les raisons
     *
     * @param request    recently modified request
     * @param evtmask variable modification event
     */
    public final void updateAndValid(ConditionnalRequest request, int evtmask) {
        update(request, evtmask);
        if (wasValid.get()) {
            request.schedule();
        } else if (isValid()) {
            for (int i = 0; i < idxLastRequest; i++) {
                ConditionnalRequest crequest = relatedRequests[i];
                if (crequest.hasChanged()) {
                    crequest.schedule();
                }
            }
            wasValid.set(alwaysValid());
        }
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
     * This simulates "no condition" request behaviour.
     *
     * @return true if the condition, once validate, won't change anymore in the current branch
     */
    abstract boolean alwaysValid();

    /**
     * Updates the current condition on the modification of one its related requests.
     *
     * @param request    recently modified request
     * @param evtMask
     */
    abstract void update(ConditionnalRequest request, int evtMask);

    /**
     * Link the <code>request</code> to the condition
     *
     * @param request condition request
     */
    public void linkRequest(ConditionnalRequest request) {
        if (idxLastRequest >= relatedRequests.length) {
            ConditionnalRequest[] tmp = relatedRequests;
            relatedRequests = new ConditionnalRequest[tmp.length * 2];
            System.arraycopy(tmp, 0, relatedRequests, 0, tmp.length);
        }
        relatedRequests[idxLastRequest++] = request;
    }


}
