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
package solver.recorders.coarse;

import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.recorders.conditions.ICondition;
import solver.variables.EventType;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class CoarseEventRecorderWithCondition extends CoarseEventRecorder {

    protected ICondition<CoarseEventRecorder> condition;
    protected boolean forceInitialPropag = true;

    public CoarseEventRecorderWithCondition(Propagator propagator, Solver solver, ICondition condition) {
        super(propagator, solver);
        this.condition = condition;
    }

    public void update(EventType e) {
        if ((e.mask & propagator.getPropagationConditions()) != 0) {
            if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
            // 1. store information concerning event
            if ((e.mask & evtmask) == 0) { // if the event has not been recorded yet (through strengthened event also).
                evtmask |= e.strengthened_mask;
            }
            // 3. schedule this
//            LoggerFactory.getLogger("solver").info("try to schedule");
            // TODO: generify and remove the call to this method, must be replaced by isValid()
            if ((condition.validateScheduling(this, propagator, e) && !enqueued)
                    || forceInitialPropag) {
//                LoggerFactory.getLogger("solver").info("... schedule!");
                scheduler.schedule(this);
                forceInitialPropag = false;
            }
        }
    }

    /*@Override
    public boolean execute() throws ContradictionException {
        boolean activate = false;
        if (propagator.isStateLess()) {
            //promote event to top level event FULL_PROPAGATION
            evtmask |= EventType.FULL_PROPAGATION.strengthened_mask;
            activate = true;
            propagator.setActive();
        }
        boolean ret = super.execute();
        if (activate) {
            condition.activate();
        }
        return ret;
    } */


    @Override
    public String toString() {
        return "<< ::" + propagator.toString() + "::" + condition + ">>";
    }
}
